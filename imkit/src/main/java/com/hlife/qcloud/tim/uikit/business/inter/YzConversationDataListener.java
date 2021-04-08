package com.hlife.qcloud.tim.uikit.business.inter;


import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;

import java.util.List;

/**
 * Created by tangyx
 * Date 2021/4/4
 * email tangyx@live.com
 */

public abstract class YzConversationDataListener {
    public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq){

    }
    public void onConversationData(ConversationInfo data){

    }
    public void onConversationError(int code,String desc){

    }
    public void onUnReadCount(long singleUnRead,long groupUnRead){

    }
}
