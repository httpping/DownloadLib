package alerm.vpclub.com.download;


import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import javax.crypto.IllegalBlockSizeException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import alerm.vpclub.com.download.utils.Constants;
import alerm.vpclub.com.download.utils.Util;


class DownLoad implements Runnable {

	public static final String TAG = "DownLoad";

	private StateInfo info;// 下载文件的信息

	public static int num;

	public int timeout = 10000; // 30 s 下载的文件 小于500KB应该没问题
	public int connTimeout = 5000; // 连接超时时间
	public long lastDataLength;// 最后读到的内容长度

	public long MIX_SPACE = 30;// 最小空间30M小于次空间不下载。
	public URLConnection urlConnection;

	private BufferedInputStream buffer;

	private File temp;

	/**
	 * url
	 * 
	 * @param
	 */
	public DownLoad(StateInfo info) {
		if (info == null) {
			throw new IllegalArgumentException("info is null");
		}
		this.info = info;
	}

	@Override
	public void run() {

		num++;
		String threadName = Thread.currentThread().getName();
		String md5 = Util.getMd5(info.url);
		System.out.println(threadName + " === " + md5);

		//没有init 池
		if (ExecutorDownLoadReactor.getInstance() == null){
			info.error = true;
			info.errorInfo = "not init pool";
			info.update();
			return;
		}

		// 理论不可能为null 特殊情况被回收
		if (ExecutorDownLoadReactor.getInstance().fileLock == null) {
			ExecutorDownLoadReactor.getInstance().fileLock = new HashMap<String, Object>();
		}


		Object fileLock = null;
		// 统一进程 不同线程必须锁， 不然无法保证下载的正常.
		synchronized (ExecutorDownLoadReactor.getInstance().fileLock) { // 拿去到 file lock ；
			Object object = ExecutorDownLoadReactor.getInstance().fileLock.get(md5);
			if (object == null) { // put
				ExecutorDownLoadReactor.getInstance().fileLock.put(md5, new Object());
				ExecutorDownLoadReactor.getInstance().fileStateInfo.put(md5, info);
			}
			fileLock = ExecutorDownLoadReactor.getInstance().fileLock.get(md5);
		}

		/**
		 * Android Filelock 机制 同一进程 不同线程无法锁
		 */
		synchronized (fileLock) {
			temp = null;
			if (info.isRepeatDown) {// clear data repeat to down
				try {
					File file = new File(
							ExecutorDownLoadReactor.getRootFilePath(), md5);
					file.delete();
					temp =  ExecutorDownLoadReactor.openDownLoadFile(file.getPath()+".txt", true);
					temp.delete();
				} catch (Exception e) {
					info.errorInfo = e.getMessage();
					info.error = true;
					return;
				}
			}

			// 后面的代码在 java中没有问题 在Android进程间没有问题， 但是在一个进程 中的多线程无法锁住
			File file = null;
			FileOutputStream fos = null;
			FileChannel channel = null;
			FileLock lock = null;

			try {
				// 重建目录
				new File(ExecutorDownLoadReactor.getRootFilePath()).mkdirs();

				file = new File(ExecutorDownLoadReactor.getRootFilePath(), md5);
				info.startTime = System.currentTimeMillis();

				if (file.exists() && file.length() != 0) {
					System.out.println("已经下载过，无需重新下载");
					info.progress = 1.0f;
					info.filePath = file.getPath();
					info.complete = true;// 完成
					info.errorInfo ="已经下载过，无需重新下载";
					info.size = file.length();
					return;
				}

				temp =  ExecutorDownLoadReactor.openDownLoadFile(file.getPath()+".txt", true);
				
				
				try {// 去除尾部数据,最后面几次写的内容，并不一定有错，但是丢掉比较安全
					long remove = Constants.DOWN_BUFFER_SIZE*2;
					long newLength = temp.length() - remove;
					System.out.println(newLength + " last=" + lastDataLength
							+ " temp.len=" + temp.length());
					if (newLength < 0) {
						newLength = 0;
					}
					RandomAccessFile mrandow = new RandomAccessFile(temp, "rw");
					mrandow.setLength(newLength);
					mrandow.close();
				} catch (Exception e2) {
					System.out.println("delete error");
					e2.printStackTrace();
				}
				
				
				fos = new FileOutputStream(temp, true);

				channel = fos.getChannel();
				lock = channel.lock(); // 流锁

				// 当 temp length 不为0 的时候 用range，当不支持range的时候怎么办呢，删除掉 temp 重新下载
				InputStream is = getConnect(temp.length());
				if (is == null) { // 连接失败
					info.error = true;
					return;
				}

				/**
				 * info.size 是从服务器返回的 下载文件的长度，如果返回为-1
				 * 这种极端情况就没办法判断,最小有MIX_SPACEM的空间，小于此空间不下载
				 */
				if (!ExecutorDownLoadReactor.isAvaiableSpace(info.size / 1024)
						|| !ExecutorDownLoadReactor.isAvaiableSpace(MIX_SPACE)) {
					System.out.println("磁盘不足，不建议下载");
					info.progress = 0f;
					info.complete = false;// 完成
					info.error = true;
					info.errorInfo = "磁盘空间不足";
					try {
						//Toast.makeText(BjApplication.getAppContext(), "磁盘不足", 0).show();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}

				BufferedInputStream buffer = new BufferedInputStream(is);
				byte[] data = new byte[Constants.DOWN_BUFFER_SIZE];// 一次读

				long count = temp.length();
				int lastProgess = 0;
				while (true) {

					if (info.stop) {
						info.error = true;
						info.errorInfo = "主动停止";
						throw new Exception("主动停止");
					}

					if (info.mustWifi) {// 必须WiFi
						if (ExecutorDownLoadReactor.NET_STATUS != ExecutorDownLoadReactor.NET_WIFI) {
							info.error = true;
							info.errorInfo = "wifi断了";
							throw new Exception("wifi断了");
						}
					}

					if (info.maxWaitTime != -1) { // 超时处理。。。
						if (System.currentTimeMillis() - info.startTime > info.maxWaitTime) {
							info.error = true;
							info.errorInfo = "timeout";
							throw new Exception("超时");
						}
					}

					int len = buffer.read(data);
					lastDataLength = len;// 最后一次读得长度
					if (len <= 0) {
						info.complete = true;
						break;
					}
					long dur = System.currentTimeMillis() - info.lastStartTime;
					info.lastStartTime = System.currentTimeMillis();

					count += len;
					info.speed = len * 1.0f / (dur + 1); // 求速度 加1 是为了防止时间段为0

					lastProgess = (int) (info.progress * 100);
					info.progress = count * 1.0f / info.size; // 求进度

					if (info.size == -1) {// 没办法算进度
						info.progress = -1;
					}

					if (info.progress > 1) {// 出错了
						throw new IllegalBlockSizeException("文件内容出错了");
					}

					if (!temp.exists()) {// 出错了
						throw new IllegalBlockSizeException("文件被删了");
					}

					// 进度条增长百分之一后才更新进度，不要太频繁
					if (lastProgess != (int) (info.progress * 100)
							&& lastProgess != -1) {
						info.update();
					}

					ByteBuffer buffer2 = ByteBuffer.wrap(data, 0, len);
					channel.write(buffer2);// 写入文件
				}

				// 文件下载成功有两种情况 一种是 size = -1 无法获取服务器长度的情况
				if (info.size != -1 && info.size != temp.length()) {// 文件异常了
					throw new IllegalBlockSizeException("文件内容出错了");
				}
				if (info.complete == true) { // 文件下载成功
					System.out.println("文件下载成功");
					temp.renameTo(file);
					info.filePath = file.getPath();
					info.complete = true;// 完成

					if (info.type != null) {
						String[] ss = info.type.split("/");
						if (ss.length > 1) {
							info.extendName = ss[1];
						}
					}
				}

				if (info.url.toLowerCase().trim().startsWith("https")) {
					HttpsURLConnection hs = (HttpsURLConnection) urlConnection;
					hs.disconnect();
				} else {
					HttpURLConnection hConnection = (HttpURLConnection) urlConnection;
					hConnection.disconnect();
				}

				// info.update();

			} catch (IOException e) {
				// io 异常
				System.out.println("io异常");
				info.error = true;
				info.errorInfo = e.getMessage();
				// info.update();
				e.printStackTrace();
				try {
					file.delete();
				} catch (Exception e2) {
					e.printStackTrace();
				}

				try {
					fos.close();// 关闭掉文件写流
				} catch (Exception eee) {
				}
				try {// 去除脏数据,最后面几次写的内容

					long remove = lastDataLength * 3;
					if (remove < Constants.DOWN_BUFFER_SIZE) { // 一块一块的，删除掉最后两块
						remove = Constants.DOWN_BUFFER_SIZE * 2;
					}
					long newLength = temp.length() - remove;
					System.out.println(newLength + " last=" + lastDataLength
							+ " temp.len=" + temp.length());
					if (newLength < 0) {
						newLength = 0;
					}
					RandomAccessFile mrandow = new RandomAccessFile(temp, "rw");
					mrandow.setLength(newLength);
					mrandow.close();
				} catch (Exception e2) {
					System.out.println("delete error");
					e.printStackTrace();
				}
				return;

			} catch (IllegalBlockSizeException e) {
				System.out.println("异常");
				info.error = true;
				info.errorInfo = e.getMessage();
				// info.update();
				e.printStackTrace();
				try {
					file.delete();
				} catch (Exception e2) {
					e.printStackTrace();
				}
				try {
					temp.delete();
				} catch (Exception e2) {
					System.out.println("delete error");
					e.printStackTrace();
				}

				return;

			} catch (Exception e) {
				System.out.println("异常");
				info.error = true;
				info.errorInfo = e.getMessage();
				// info.update();
				e.printStackTrace();
				try {
					file.delete();
				} catch (Exception e2) {
					System.out.println("delete error");
					e.printStackTrace();
				}
				return;
			} finally {

				try {
//					if (info.complete) {
						ExecutorDownLoadReactor.getInstance().fileLock.remove(md5);// 释放锁
						ExecutorDownLoadReactor.getInstance().fileStateInfo.remove(md5);// 释放状态

//					}
				} catch (Exception e2) {
				}

				try {
					lock.release();
				} catch (Exception e2) {
					// TODO: handle exception
				}

				try {
					buffer.close();
				} catch (Exception e2) {
				}

				// 回收
				try {
					System.out.println("channel cl");
					channel.close();
				} catch (Exception e) {
				}

				try {
					fos.close();
				} catch (Exception e) {
				}
				info.update();
				System.out.println(info.url + " -- " + threadName + " -- md5："
						+ md5 + " -- download 线程结束了" + num);
			}
		}
		return;
	}

	public InputStream getConnect(long range) {
		// URL url = new URL(info.url);
		// 获取文件大小
		int length = 0;/* = conn.getContentLength(); */
		// 创建输入流
		InputStream is = null;/* is = conn.getInputStream(); */
		try {
			HttpURLConnection urlConnection;

			urlConnection = (HttpURLConnection) new URL(info.url)
					.openConnection();
//			urlConnection.setConnectTimeout(connTimeout);
			// urlConnection.setReadTimeout(timeout);

			// 添加请求头
			urlConnection.addRequestProperty("Connection", "Keep-Alive");
//			urlConnection.addRequestProperty("imtl", "imtl="
//					+ BjApplication.g_LoginEnity.misdn);
//			urlConnection.addRequestProperty("ver", "version="
//					+ BjApplication.g_Version);
//			urlConnection.addRequestProperty("net", "net="
//					+ isNetworkAvailable());
//			urlConnection.addRequestProperty("chnn", "chnn="
//					+ BjApplication.g_channel);
//			urlConnection.addRequestProperty("mobile-type", "mobile-type="
//					+ android.os.Build.MODEL);
//			urlConnection.addRequestProperty("mobile-os", "mobile-os=" + "B_"
//					+ android.os.Build.VERSION.SDK); // 2015-04-16
//														// added by ltt
//			urlConnection.addRequestProperty("isupg", "isupg="
//					+ BjApplication.g_LoginEnity.upginfo.isupg);
			
			if (range > 0) {// 断点续传
				urlConnection.setRequestProperty("RANGE", "bytes=" + range
						+ "-");
			}
			this.urlConnection = urlConnection;

			info.type = urlConnection.getContentType();
			length = urlConnection.getContentLength();

			info.size = length;

			int code = urlConnection.getResponseCode();

			if (range != 0 && code != 206) {// 不支持断点续传
				temp.delete();// 删除temp文件
				throw new Exception("不支持断点续传");
			}

			info.reqestHead = urlConnection.getHeaderFields();
			info.printHeadInfo();
			if (code == 206 && info.size!=0) {
				info.size += range;
			}
			// System.err.println(timeout);

			urlConnection.connect();
			length = urlConnection.getContentLength();

			is = urlConnection.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("出异常了狗日的");

			info.error = true;
			info.errorInfo = e.getMessage();
			return null;
		}

		return is;
	}

	/**
	 * 获取网络
	 * 
	 * @return
	 */
	private String isNetworkAvailable() {
//		ConnectivityManager manager = (ConnectivityManager) BjApplication
//				.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//		State gprs = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
//				.getState();
//		State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
//				.getState();
//		if (gprs == State.CONNECTED || gprs == State.CONNECTING) {
//			return "gprs";// gprs connect
//		}
//		if (wifi == State.CONNECTED || wifi == State.CONNECTING) {
//			return "wifi";// wifi connect
//		}

		return "disconnect";
	}

	private class HarmonyManager implements X509TrustManager {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			// TODO Auto-generated method stub

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}

	/**
	 * 跳过的字节
	 * 
	 * @param in
	 * @param howMany
	 * @return
	 * @throws Exception
	 */
	public static InputStream skipFully(InputStream in, long howMany)
			throws Exception {
		long remainning = howMany;
		long len = 0;
		while (remainning > 0) {
			len = in.skip(remainning);
			Log.d(TAG, "跳过 ：" + len);
			remainning -= len;
		}
		return in;
	}

	private class HarmonyVerifier implements HostnameVerifier {

		@Override
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}

	}

	public void throwException() throws FileNotFoundException, IOException,
            TimeoutException {
		throw new TimeoutException();
	}

}
