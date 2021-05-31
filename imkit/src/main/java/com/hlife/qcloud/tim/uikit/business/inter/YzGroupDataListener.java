package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;

import java.util.List;

public abstract class YzGroupDataListener {
    public void onCreate(int code,String groupId,String msg){};
    public void update(int code,String msg){};
    public void addMember(int code,String msg){};
    public void deleteMember(int code,String msg){};
    public void joinMember(List<GroupApplyInfo> applies){};
}
