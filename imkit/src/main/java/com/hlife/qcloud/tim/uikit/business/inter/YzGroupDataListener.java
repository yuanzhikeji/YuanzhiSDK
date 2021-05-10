package com.hlife.qcloud.tim.uikit.business.inter;

import com.work.api.open.model.client.OpenGroupInfo;

public interface YzGroupDataListener {
    void onCreate(OpenGroupInfo info);
    void addMember(int code,String msg);
    void deleteMember(int code,String msg);
}
