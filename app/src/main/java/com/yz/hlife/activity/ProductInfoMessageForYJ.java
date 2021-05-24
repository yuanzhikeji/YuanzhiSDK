package com.yz.hlife.activity;

import com.hlife.qcloud.tim.uikit.modules.message.MessageCustom;

/**
 * Description: 研究院商品卡片消息
 */

public class ProductInfoMessageForYJ extends MessageCustom {
//     "type": "1//0 商品信息  1订单信息",
    //              "imgUrl": "//商品图片地址",
//              "productName": "商品名称",
//              "moneyText": "¥5899.00 //订单金额文案",
//              "productCount": "//商品个数",
//              "orderNum": "//订单编号",
//              "createTimeStr": "2020-01-10 16:18:36 //下单时间",
//              "link": "//点击跳转连接"

    private String type;
    private String imgUrl;
    private String link;
    private String productName;
    private String moneyText;
    private String productCount;
    private String orderNum;
    private String createTimeStr;

    private String shopName;
    private String messageType;//商品卡片messageType = YZDMCardGoodsMessageData   订单= YZDMCardOrderMessageData
    private String order;

    public ProductInfoMessageForYJ() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getMoneyText() {
        return moneyText;
    }

    public void setMoneyText(String moneyText) {
        this.moneyText = moneyText;
    }

    public String getProductCount() {
        return productCount;
    }

    public void setProductCount(String productCount) {
        this.productCount = productCount;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }



    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
