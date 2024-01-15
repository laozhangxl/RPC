package com.xxxx.rpc.common.constants;

public interface FaultTolerantRules {

    //故障转移
    String FailOver = "failOver";

    //快速失败
    String FailFast = "failFast";

    //忽略
    String FailPass = "failPass";

}
