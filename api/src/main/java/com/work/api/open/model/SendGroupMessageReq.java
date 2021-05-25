package com.work.api.open.model;

import com.work.api.open.model.client.OpenTIMElem;

public class SendGroupMessageReq extends BaseReq{
    private String fromUserId;
    private String groupId;
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

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}
