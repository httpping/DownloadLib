# DownloadLib
Android 下载中间件

# init
 //init环境
 ExecutorDownLoadReactor.newFixedThreadPool(1).initEnv(this);
 
 下载默认使用路径为cache dir 中建的 download目录
 
 # use
 ## 1、下载
     ExecutorDownLoadReactor.execute(url ,MainActivity.this);
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
  
  
