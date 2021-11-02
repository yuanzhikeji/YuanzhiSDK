package com.work.api.open.model.client;

public class UserRemark {
    private String fromUserId;
    private String toUserId;
    private String remark;

    public UserRemark() {
    }

    public UserRemark(String fromUserId, String toUserId, String remark) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.remark = remark;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
