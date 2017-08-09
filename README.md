# DownloadLib
Android 下载中间件
## 功能
   ### 1、支持断点续传下载
   ### 2、下载成功后通过MD5等加密算法智能分析是否需要重新去服务器获取数据，解决流量

# init
## init环境
 ExecutorDownLoadReactor.newFixedThreadPool(1).initEnv(Application);
 
 下载默认使用路径为cache dir 中建的 download目录
 
 # use
 ## 1、下载
     ExecutorDownLoadReactor.execute(url ,MainActivity.this);
 ### 下载进度获取
  	 ExecutorDownLoadReactor.execute(url, new CallBack() {
            @Override
            public void update(Observable observable, Object o) {
                if (observable instanceof StateInfo) {
                    final StateInfo info = (StateInfo) observable;
                }
            }
        });
	
	/**
	 *文件下载状态和信息 状态
	 * @author tanping
	 *
	 */
	public class StateInfo  extends Observable {

	public float speed ;// 速度
	public float progress;// 下载进度
	public long  size ;// 下载大小
	public String filePath ;// 下载完成的目录
	public boolean complete ;// 是否完成
	public boolean error ;// 下载错误
	public boolean isRepeatDown;// 是否重新下载
	public String errorInfo ;//错误信息
	public String url;
	public String type ; // 文件类型
	public String extendName ;// 文件扩展名
	public String resultCode ;//请求返回
	
	public boolean stop = false ;//手动是否停止
	public boolean mustWifi = false; // 必须wifi


	public long lastStartTime = System.currentTimeMillis();// 上一次开始时间
	public long startTime  = System.currentTimeMillis();// 开始时间
	public long endTime ;//结束时间
	public long maxWaitTime = -1;// 最长下载时间 -1 表示无穷大
	
	public Map<String, List<String>> reqestHead ;//请求返回头
 	
	....
	}
     
 ## 2、停止
     ExecutorDownLoadReactor.stop(url);  
 ## 3、重复下载
  	 /**
     * 传人url 执行下载任务，重新下载
     * @param url
     * repeatdown = true 重新下载不管以前的文件
     * @return
     */
	public static StateInfo execute(String url, CallBack callBack, boolean repeatDown){
    ....
  }
 
      ExecutorDownLoadReactor.execute(url,MainActivity.this,true);
 
## 4、查看是否下载过
### Api
  /**
  * 判断url 是否下载成功，如果不为null 则表示下载成功	
  * @param url
  * @return
  */
  public static String getFilePathForUrl(String url ){
	...				 
  }
  
#### use
  ExecutorDownLoadReactor.getFilePathForUrl(url)
  
