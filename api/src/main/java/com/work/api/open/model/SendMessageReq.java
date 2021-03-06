package com.work.api.open.model;

import com.work.api.open.model.client.OpenTIMElem;

public class SendMessageReq extends BaseReq{
    private String fromUserId;
    private String toUserId;
    private String msgType;
    private OpenTIMElem msgContent;

    public void setMsgContent(OpenTIMElem msgContent) {
        this.msgContent = msgContent;
    }

    public OpenTIMElem getMsgContent() {
        return msgContent;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }
}
