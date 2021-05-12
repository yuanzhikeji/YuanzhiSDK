package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.work.api.open.model.client.OpenGroupInfo;

import java.util.List;

public interface YzGroupDataListener {
    void onCreate(int code,String groupId,String msg);
    void update(int code,String msg);
    void addMember(int code,String msg);
    void deleteMember(int code,String msg);
    void joinMember(List<GroupApplyInfo> applies);
}
