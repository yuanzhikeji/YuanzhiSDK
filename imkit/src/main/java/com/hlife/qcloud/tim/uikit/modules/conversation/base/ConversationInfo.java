package com.hlife.qcloud.tim.uikit.modules.conversation.base;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConversationInfo implements Serializable, Comparable<ConversationInfo> {

    public static final int TYPE_COMMON = 1;
    public static final int TYPE_CUSTOM = 2;
    /**
     * 会话类型，自定义会话or普通会话
     */
    private int type;

    /**
     * 消息未读数
     */
    private int unRead;
    /**
     * 会话ID
     */
    private String conversationId;
    /**
     * 会话标识，C2C为对方用户ID，群聊为群组ID
     */
    private String id;
    /**
     * 会话头像url
     */
    private List<Object> iconUrlList = new ArrayList<>();

    public List<Object> getIconUrlList() {
        return iconUrlList;
    }

    public void setIconUrlList(List<Object> iconUrlList) {
        this.iconUrlList = iconUrlList;
    }

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话头像
     */
    private Bitmap icon;
    /**
     * 是否为群会话
     */
    private boolean isGroup;
    /**
     * 是否为置顶会话
     */
    private boolean top;
    /**
     * 最后一条消息时间
     */
    private long lastMessageTime;
    /**
     * 最后一条消息，MessageInfo对象
     */
    private MessageInfo lastMessage;
    /**
     * 会话界面显示的@提示消息
     */
    private String atInfoText;
    /**
     * 会话界面显示消息免打扰图标
     */
    private boolean isRevOpt;

    /**
     * 草稿
     */
    private DraftInfo draft;
    /**
     * 群类型
     */
    private String groupType;

    public void setRevOpt(boolean revOpt) {
        isRevOpt = revOpt;
    }

    public boolean isRevOpt() {
        return isRevOpt;
    }

    public void setDraft(DraftInfo draft) {
        this.draft = draft;
    }

    public DraftInfo getDraft() {
        return draft;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public String getGroupType() {
        return groupType;
    }

    public ConversationInfo() {

    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUnRead() {
        return unRead;
    }

    public void setUnRead(int unRead) {
        this.unRead = unRead;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    /**
     * 获得最后一条消息的时间，单位是秒
     */
    public long getLastMessageTime() {
        return lastMessageTime;
    }

    /**
     * 设置最后一条消息的时间，单位是秒
     * @param lastMessageTime
     */
    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public MessageInfo getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(MessageInfo lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setAtInfoText(String atInfoText) {
        this.atInfoText = atInfoText;
    }

    public String getAtInfoText() {
        return atInfoText;
    }

    @Override
    public int compareTo(@NonNull ConversationInfo other) {
        if (this.lastMessageTime > other.lastMessageTime) {
            return -1;
        } else if (this.lastMessageTime == other.lastMessageTime) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return "ConversationInfo{" +
                "type=" + type +
                ", unRead=" + unRead +
                ", conversationId='" + conversationId + '\'' +
                ", id='" + id + '\'' +
                ", iconUrl='" + iconUrlList.size() + '\'' +
                ", title='" + title + '\'' +
                ", icon=" + icon +
                ", isGroup=" + isGroup +
                ", top=" + top +
                ", lastMessageTime=" + lastMessageTime +
                ", lastMessage=" + lastMessage.getExtra() +
                '}';
    }
}
