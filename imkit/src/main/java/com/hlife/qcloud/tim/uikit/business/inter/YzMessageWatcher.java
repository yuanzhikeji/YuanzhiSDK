package com.hlife.qcloud.tim.uikit.business.inter;

/**
 * Created by tangyx
 * Date 2021/4/4
 * email tangyx@live.com
 */

public interface YzMessageWatcher {
    void updateUnread(int count);
    void updateContacts();
    void updateConversion();
}
