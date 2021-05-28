package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;

import java.util.List;

public interface YzChatHistoryMessageListener {
    void onChatMessageHistory(List<MessageInfo> messageInfos);
    void onError(int code,String desc);
}
