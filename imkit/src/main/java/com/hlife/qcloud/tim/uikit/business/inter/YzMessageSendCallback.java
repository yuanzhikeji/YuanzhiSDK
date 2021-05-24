package com.hlife.qcloud.tim.uikit.business.inter;

/**
 * Created by tangyx
 * Date 2021/5/24
 * email tangyx@live.com
 */

public interface YzMessageSendCallback {
    void success();
    void error(int code,String desc);
}
