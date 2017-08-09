package alerm.vpclub.com.download.utils;

import java.util.concurrent.Future;

import alerm.vpclub.com.download.StateInfo;

/**
 * Created by tp on 2017/8/9.
 */

public class DownLoadStateBind {
    public DownLoadStateBind(StateInfo stateInfo, Future future) {
        this.stateInfo = stateInfo;
        this.future = future;
    }

    public StateInfo stateInfo;//状态

    public Future future; // 运行
}
