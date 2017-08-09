package alerm.vpclub.com.download;
 


import java.util.Observer;

/**
 * 观察 下载状态
 * 注意 ： callback 回调是在异步线程中。和下载线程同一线程 回调方法不要做复杂逻辑.会影响回调效率。
 * 注意： 速度有可能达到无限
 * 
 * @author tanping
 *
 */
public interface CallBack extends Observer {
	
 
}

