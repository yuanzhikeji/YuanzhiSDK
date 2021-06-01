package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;

import java.util.List;

public interface YzGroupMemberListener {
    void groupMember(List<GroupMemberInfo> members);
    void error(int code,String desc);
}
