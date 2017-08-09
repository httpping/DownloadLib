package alerm.vpclub.com.download;


import android.app.Application;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import alerm.vpclub.com.download.utils.Constants;
import alerm.vpclub.com.download.utils.FileUtils;
import alerm.vpclub.com.download.utils.Util;


/**
 * 下载池
 * @author tanping
 *
 */
public class  ExecutorDownLoadReactor {
	/**
	 * 网络为WiFi 状态
	 */
	public static final int NET_WIFI = 1;

	private Application application;

	/**
	 * 网络不为WiFi状态
	 */
	public static final int NET_NOT_WIFI = 2;
	
	public static int NET_STATUS = 2;// 1.WiFi，2.gprs
	
	private static ExecutorDownLoadReactor pool ; // 单利
	
	private ThreadFactory threadFactory ;// 默认的
	private int nThreads ;// 默认一次只下载一个
	
	public static final int DEFAULT_THREAD_SIZE =2;//默认的下载线程数量
	
	public  Map<String, Object> fileLock = new HashMap<String, Object>(); // 文件锁控制同一个url 只允许一把锁
	public  Map<String, StateInfo> fileStateInfo = new HashMap<String, StateInfo>(); // 文件锁控制同一个url 只允许一把锁

	private ExecutorService executors ;

	public static String ROOT_FILE_PATH  ;
	
	private ExecutorDownLoadReactor(){
		
		try {
			if (ROOT_FILE_PATH == null) {
				getRootFilePath();
			}
			File file = new File(ROOT_FILE_PATH);
			file.mkdirs();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	
	/**
	 * 默认一次执行一个
	 * @return
	 */
	public static ExecutorDownLoadReactor newDefaultThreadPool() {
    	return newFixedThreadPool(DEFAULT_THREAD_SIZE, null);
    }
	
	/**
	 * 执行多个但是 使用默认的线程工厂
	 * @param nThreads 同时执行下载任务的数量
	 * @return
	 */
    public static ExecutorDownLoadReactor newFixedThreadPool(int nThreads) {
    	return newFixedThreadPool(nThreads,null);
    }
	
	/**
	 * 使用自定义的线程工厂
	 * @param nThreads
	 * @param threadFactory
	 * @return
	 */
    public static ExecutorDownLoadReactor  newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
    	
    	
    	if (pool != null) {
			return pool;
		}
    	
    	if (nThreads <= 0) {
			throw new IllegalArgumentException("nthread 要大于0");
		}
    	
    	if (pool == null) {
			pool = new ExecutorDownLoadReactor();
		}
    	pool.threadFactory = threadFactory;
        pool.nThreads = nThreads;
        
        
        if (threadFactory == null) {
            pool.executors = Executors.newFixedThreadPool(pool.nThreads);
		}else {
	        pool.executors = Executors.newFixedThreadPool(pool.nThreads, pool.threadFactory);
		}
        

    	return pool;
    }


	/**
	 * init 环境
	 * @param application
	 * @return
	 */
	public  ExecutorDownLoadReactor initEnv(Application application){
		pool.application = application;
		return  this;
	}
    
    
	
    /**
     * 如果没有可用路径会出现异常
     * @return
     * @throws Exception
     */
    public static String getRootFilePath() throws Exception {
    	Log.d("root", "getRootFilePath");
    	if (ROOT_FILE_PATH == null) {
    		String path =   pool.application.getCacheDir().getPath();
			if (path == null) {
				throw new Exception("没有可用的存储路径");
			}
			ROOT_FILE_PATH =path + Constants.DEFAULT_ROOT_FILE_PATH;
			try {
				new File(ROOT_FILE_PATH).mkdirs();
				Log.d("root", "ok");
			} catch (Exception e) {
				e.printStackTrace();
				return null; //没有可用的路径
			}
			
	 	}
		return ROOT_FILE_PATH;
	}



//	public static void setRootFilePath(String rOOT_FILE_PATH) {
//		
//		File file = new File(rOOT_FILE_PATH);
//		file.mkdirs();
//		ROOT_FILE_PATH = rOOT_FILE_PATH;
//	}



	/**
     * 传人url 执行下载任务
     * @param url
     * @return
     */
	public static StateInfo execute(String url){
		if (url == null) {
			throw new IllegalArgumentException();
		}
		
		if (pool == null) {
			pool = newDefaultThreadPool();
		}
		StateInfo stateInfo = new StateInfo(url);
		DownLoad downLoad = new DownLoad(stateInfo);
		pool.executors.execute(downLoad);
		return stateInfo;
	}
	
	
    /**
     * 传人url 执行下载任务
     * @param url
     * @return
     */
	public static StateInfo execute(String url, CallBack callBack){
		
		if (url == null || callBack == null) {
			throw new IllegalArgumentException();
		}
		
		if (pool == null) {
			pool = newDefaultThreadPool();
		}
		StateInfo stateInfo = new StateInfo(url);
		stateInfo.addObserver(callBack); // 注册观察者
		DownLoad downLoad = new DownLoad(stateInfo);
		pool.executors.execute(downLoad);
		return stateInfo;
	}
	
	 /**
     * 传人url 执行下载任务，重新下载
     * @param url
     * repeatdown = true 重新下载不管以前的文件
     * @return
     */
	public static StateInfo execute(String url, CallBack callBack, boolean repeatDown){
		
		if (url == null || callBack == null) {
			throw new IllegalArgumentException();
		}
		
		if (pool == null) {
			pool = newDefaultThreadPool();
		}
		StateInfo stateInfo = new StateInfo(url);
		stateInfo.isRepeatDown= repeatDown;
		stateInfo.addObserver(callBack); // 注册观察者
		DownLoad downLoad = new DownLoad(stateInfo);
		//Future<?> future = pool.executors.submit(downLoad);
		pool.executors.execute(downLoad);
		 
		return stateInfo;
	}
	
	
	 /**
     * 传人url 执行下载任务，重新下载
     * @param url
     * isMustWifi = true 必须WiFi
     * @return
     */
	public static StateInfo execute(String url, boolean isMustWifi, CallBack callBack){
		
		if (url == null || callBack == null) {
			throw new IllegalArgumentException();
		}
		
		if (pool == null) {
			pool = newDefaultThreadPool();
		}
		StateInfo stateInfo = new StateInfo(url);
		stateInfo.mustWifi= isMustWifi;
		stateInfo.isRepeatDown =false;
		stateInfo.addObserver(callBack); // 注册观察者
		DownLoad downLoad = new DownLoad(stateInfo);
		//Future<?> future = pool.executors.submit(downLoad);
		pool.executors.execute(downLoad);
		 
		return stateInfo;
	}
	
	
	 /**
     * 传人url 执行下载任务，重新下载
     * @param url
     * repeatdown = true 重新下载不管以前的文件
     * @return
     */
	public static StateInfo execute(String url, CallBack callBack, boolean repeatDown, long maxWaitTime){
		
		if (url == null || callBack == null) {
			throw new IllegalArgumentException();
		}
		
		if (pool == null) {
			pool = newDefaultThreadPool();
		}
		StateInfo stateInfo = new StateInfo(url);
		stateInfo.isRepeatDown= repeatDown;
		stateInfo.maxWaitTime = maxWaitTime;
		stateInfo.addObserver(callBack); // 注册观察者
		DownLoad downLoad = new DownLoad(stateInfo);
		pool.executors.execute(downLoad);
		
		return stateInfo;
	}
	

	/**
	 * 存储空间判断
	 * @param sizeMb
	 * @return
	 */
	public static boolean  isAvaiableSpace(long sizeMb) {
		boolean ishasSpace = true;
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			String sdcard = Environment.getExternalStorageDirectory()
					.getPath();
			StatFs statFs = new StatFs(sdcard);
			long blockSize = statFs.getBlockSize();
			long blocks = statFs.getAvailableBlocks();
			long availableSpare = (blocks * blockSize) / (1024 * 1024);
			//AspLog.i(TAG, "availableSpare = " + availableSpare);
			if (availableSpare > sizeMb) {
				ishasSpace = true;
			}
		}
		return ishasSpace;
	}	
	
 /**
  * 判断url 是否下载成功，如果不为null 则表示下载成功	
  * @param url
  * @return
  */
  public static String getFilePathForUrl(String url ){
		if (url == null) {
			return null;
		}
		
		String path =null;
		File file = null ;
		try {
			if (getRootFilePath() != null  ) {
				path  = getRootFilePath() +"/" + Util.getMd5(url);
				file = new File(path);
			}
			// DIR_LOGO
			
			//ExecutorDownLoadReactor.
			if (file != null && file.exists()) {
				return file.getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		
		return null; 

	}
  
  
  
	public static File openDownLoadFile(String saveToFileName,
                                        boolean isSetPermissions) throws IOException {
		File file = new File(saveToFileName);
		try {
			if (isSetPermissions) {
				FileUtils.setPermissions(file.getParent(), 0777, -1, -1);
				FileUtils.setPermissions(saveToFileName, 0777, -1, -1);
			}
		} catch (Exception e) {
			Log.d("open", "open file downlaod");
			e.printStackTrace();
		}

		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}


	/**
	 * 暂停
	 * @param url
	 */
	public static void stop(String url){
		if (pool == null)
			return;
		String md5 = Util.getMd5(url);
		StateInfo info = pool.fileStateInfo.get(md5);
		if (info !=null){
			info.stop = true;//停止
		}
	}

	
	/**
	 * 关闭
	 * @return
	 */
	public static boolean shutdown(){
		
 		
		if (pool != null && pool.executors!=null) {
			pool.executors.shutdown();
			pool = null;
		}
		
		//pool = null;
		return true;
	}

	public static ExecutorDownLoadReactor getInstance(){
			return  pool;
	}

}
