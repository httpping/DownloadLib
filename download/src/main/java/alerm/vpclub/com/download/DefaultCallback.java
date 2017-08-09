package alerm.vpclub.com.download;


import android.util.Log;

import java.util.Observable;


/**
 * 自己实现的观察者模式
 * @author tanping
 *
 */
public class DefaultCallback implements CallBack {

	public final static String TAG = "DefaultCallback";
	
	public void update(Observable o, Object arg) {
		/*Log.d(TAG,Thread.currentThread().getId());
		Log.d(TAG,Thread.currentThread().getName());*/

		/*try {
			Thread.sleep(1000);
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		if (o instanceof StateInfo) {
			StateInfo info = (StateInfo) o ;
			
			Log.d(TAG,"size =" + info.size + ",进度 =" + info.getProgress() +" 速度：" + info.getSpeed() +"Kb/s" + " 完成："+info.isComplete() + " error="+ info.isError() +" errorinfo=" +info.getErrorInfo());
			Log.d(TAG,"url ="+info.url);
			
			
			if (info.isComplete()) {
				Log.d(TAG,"文件类型：" + info.type);
				Log.d(TAG,"文件后缀：" + info.extendName);
				Log.d(TAG,"文件地址：" + info.getFilePath());
				Log.d(TAG,"所花时间：" + (info.getEndTime() - info.getStartTime()));
			}
			if (info.isError()) {
				Log.d(TAG,"size =" + info.size + ",进度 =" + info.getProgress() +" 速度：" + info.getSpeed() +"Kb/s" + " 完成："+info.isComplete() + " error="+ info.isError() +" errorinfo=" +info.getErrorInfo());
			}
			
		}
	}

}
