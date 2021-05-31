package com.work.api.open.model.client;

import com.http.network.model.ClientModel;

public class OpenGroupLife extends ClientModel {

    /**
     * ownerAccount : 18368251632
     * destroyStatus : 1
     * appid : 7b9533618a47e947d4fd83b966081a52
     * groupId : @TGS#23O676FHQ
     * introduction : null
     * notification : null
     * faceUrl : https://yzkj-pro.oss-cn-beijing.aliyuncs.com/avatar/lPto9oLiOp.jfif
     * type : Public
     * name : 测试群
     */

    private String ownerAccount;
    private int destroyStatus;
    private String appid;
    private String groupId;
    private String introduction;
    private String notification;
    private String faceUrl;
    private String type;
    private String name;

    public String getOwnerAccount() {
        return ownerAccount;
    }

    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public int getDestroyStatus() {
        return destroyStatus;
    }

    public void setDestroyStatus(int destroyStatus) {
        this.destroyStatus = destroyStatus;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getFaceUrl() {
        return faceUrl;
    }

    public void setFaceUrl(String faceUrl) {
        this.faceUrl = faceUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
