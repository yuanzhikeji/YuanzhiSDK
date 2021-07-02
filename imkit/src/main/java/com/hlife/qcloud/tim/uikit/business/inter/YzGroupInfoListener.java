package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;

import java.util.List;

public interface YzGroupInfoListener {
    void success(List<GroupInfo> groupInfoList);
    void error(int code,String desc);
}
