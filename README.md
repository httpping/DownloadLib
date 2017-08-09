# DownloadLib
Android 下载中间件

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
  
