package com.hlife.qcloud.tim.uikit.modules.chat.base;

import com.hlife.qcloud.tim.uikit.modules.conversation.base.DraftInfo;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMGroupAtInfo;
import com.tencent.imsdk.v2.V2TIMMessage;

import java.io.Serializable;
import java.util.List;

/**
 * 聊天信息基本类
 */
public class ChatInfo implements Serializable {

    private String chatName;
    private int type = V2TIMConversation.V2TIM_C2C;
    private boolean isGroup;
    private String id;
    private String groupType;
    private boolean isTopChat;
    private V2TIMMessage mLocateTimMessage;
    private List<V2TIMGroupAtInfo> atInfoList;
    private boolean showAddGroup;

    /**
     * 草稿
     */
    private DraftInfo draft;

    public ChatInfo() {

    }

    public DraftInfo getDraft() {
        return draft;
    }

    public void setDraft(DraftInfo draft) {
        this.draft = draft;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setLocateTimMessage(V2TIMMessage mLocateTimMessage) {
        this.mLocateTimMessage = mLocateTimMessage;
    }

    public V2TIMMessage getLocateTimMessage() {
        return mLocateTimMessage;
    }

    /**
     * 获取聊天的标题，单聊一般为对方名称，群聊为群名字
     *
     * @return
     */
    public String getChatName() {
        return chatName;
    }

    /**
     * 设置聊天的标题，单聊一般为对方名称，群聊为群名字
     *
     * @param chatName
     */
    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    /**
     * 获取聊天类型，C2C为单聊，Group为群聊
     *
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     * 设置聊天类型，C2C为单聊，Group为群聊
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
        this.isGroup = type == V2TIMConversation.V2TIM_GROUP;
    }

    /**
     * 获取聊天唯一标识
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     * 设置聊天唯一标识
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 是否为置顶的会话
     *
     * @return
     */
    public boolean isTopChat() {
        return isTopChat;
    }

    /**
     * 设置会话是否置顶
     *
     * @param topChat
     */
    public void setTopChat(boolean topChat) {
        isTopChat = topChat;
    }

    public boolean isShowAddGroup() {
        return showAddGroup;
    }

    public void setShowAddGroup(boolean showAddGroup) {
        this.showAddGroup = showAddGroup;
    }

    public List<V2TIMGroupAtInfo> getAtInfoList() {
        return atInfoList;
    }

    public void setAtInfoList(List<V2TIMGroupAtInfo> atInfoList) {
        this.atInfoList = atInfoList;
    }

    public void setGroup(boolean group) {
        isGroup = group;
        type = group?V2TIMConversation.V2TIM_GROUP:V2TIMConversation.V2TIM_C2C;
    }

    public boolean isGroup() {
        return isGroup;
    }
}
