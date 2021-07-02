package com.hlife.qcloud.tim.uikit.business.modal;

import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.imsdk.v2.V2TIMMessage;

public class SearchDataMessage extends ConversationInfo {
    private String subTitle;
    private int subTextMatch;
    private int count;
    private V2TIMMessage locateTimMessage;
    private long messageTime;

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setLocateTimMessage(V2TIMMessage locateTimMessage) {
        this.locateTimMessage = locateTimMessage;
        if(locateTimMessage!=null){
            setMessageTime(locateTimMessage.getTimestamp());
        }
    }

    public V2TIMMessage getLocateTimMessage() {
        return locateTimMessage;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getSubTitle()
    {
        return this.subTitle;
    }

    public void setSubTitle(String subTitle)
    {
        this.subTitle = subTitle;
    }

    public int getSubTextMatch() {
        return subTextMatch;
    }

    public void setSubTextMatch(int subTextMatch) {
        this.subTextMatch = subTextMatch;
    }

}
