package com.hlife.qcloud.tim.uikit.business.message;

import com.hlife.qcloud.tim.uikit.base.IBaseInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;

import java.io.Serializable;

/**
 * 自定义消息的bean实体，用来与json的相互转化
 */
public class CustomMessage implements Serializable, IBaseInfo {
    private String businessID = "";
    private String title = "";
    private String desc = "";
    private String link = "";
    private String logo = "";

    public final int version = IMKitConstants.version;

    public String getBusinessID() {
        return businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
