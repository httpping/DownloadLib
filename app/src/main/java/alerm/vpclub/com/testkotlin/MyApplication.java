package alerm.vpclub.com.testkotlin;

import android.app.Application;

import alerm.vpclub.com.download.ExecutorDownLoadReactor;

/**
 * Created by tp on 2017/8/9.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //init环境
        ExecutorDownLoadReactor.newFixedThreadPool(3).initEnv(this);

    }
}
