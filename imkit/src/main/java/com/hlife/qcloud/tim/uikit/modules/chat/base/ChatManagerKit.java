package com.hlife.qcloud.tim.uikit.modules.chat.base;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IBaseInfo;
import com.hlife.qcloud.tim.uikit.base.IBaseMessageSender;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatMessageListener;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.hlife.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageListGetOption;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.tencent.imsdk.v2.V2TIMSendCallback;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.work.util.SLog;
import com.work.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ChatManagerKit extends V2TIMAdvancedMsgListener implements MessageRevokedManager.MessageRevokeHandler, IBaseMessageSender {

    // 消息已读上报时间间隔
    private static final int READ_REPORT_INTERVAL = 100; // 单位： 毫秒

    protected static final int MSG_PAGE_COUNT = 20;
    protected static final int REVOKE_TIME_OUT = 20016;
    protected ChatProvider mCurrentProvider;

    protected boolean mIsMore;
    private boolean mIsLoading;
    private boolean mIsChatFragmentShow = false;

    private MessageInfo mLastMessageInfo;
    private YzChatMessageListener mYzChatMessageListener;

    private long lastReadReportTime = 0L;
    private boolean canReadReport = true;
    private final MessageReadReportHandler readReportHandler = new MessageReadReportHandler();

    protected void init() {
        destroyChat();
        V2TIMManager.getMessageManager().addAdvancedMsgListener(this);
        MessageRevokedManager.getInstance().addHandler(this);
    }

    public void destroyChat() {
        mCurrentProvider = null;
    }

    public void markMessageAsRead(ChatInfo chatInfo) {
        if (chatInfo == null) {
            SLog.e("markMessageAsRead() chatInfo is null");
            return;
        }
        boolean isGroup = chatInfo.getType() != V2TIMConversation.V2TIM_C2C;
        String chatId = chatInfo.getId();
        if (isGroup) {
            groupReadReport(chatId);
        } else {
            c2cReadReport(chatId);
        }
    }

    public boolean isChatFragmentShow() {
        return mIsChatFragmentShow;
    }

    public void setChatFragmentShow(boolean isChatFragmentShow) {
        this.mIsChatFragmentShow = isChatFragmentShow;
    }

    private void groupReadReport(String groupId) {
        V2TIMManager.getMessageManager().markGroupMessageAsRead(groupId, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("markGroupMessageAsRead failed, code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
            }
        });
    }

    private void c2cReadReport(String userId) {
        V2TIMManager.getMessageManager().markC2CMessageAsRead(userId, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("maonNewMessagerkC2CMessageAsRead setReadMessage failed, code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
            }
        });
    }

    public abstract ChatInfo getCurrentChatInfo();

    public void setCurrentChatInfo(ChatInfo info) {
        if (info == null) {
            return;
        }
        mCurrentProvider = new ChatProvider();
        mIsMore = true;
        mIsLoading = false;
    }

    public void setYzChatMessageListener(YzChatMessageListener mYzChatMessageListener) {
        this.mYzChatMessageListener = mYzChatMessageListener;
    }

    public void onReadReport(List<V2TIMMessageReceipt> receiptList) {
        SLog.i("onReadReport:" + receiptList.size());
        if (!safetyCall()) {
            return;
        }
        if (receiptList.size() == 0) {
            return;
        }
        V2TIMMessageReceipt max = receiptList.get(0);
        for (V2TIMMessageReceipt msg : receiptList) {
            if (!TextUtils.equals(msg.getUserID(), getCurrentChatInfo().getId())) {
                continue;
            }
            if (max.getTimestamp() < msg.getTimestamp()) {
                max = msg;
            }
        }
        mCurrentProvider.updateReadMessage(max);
    }

    @Override
    public void onRecvNewMessage(V2TIMMessage msg) {
        SLog.i("onRecvNewMessage msgID:" + msg.getMsgID());
        int elemType = msg.getElemType();
        if (elemType == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
            if (MessageInfoUtil.isTyping(msg.getCustomElem().getData())) {
                notifyTyping();
                return;
            } else if (MessageInfoUtil.isOnlineIgnoredDialing(msg.getCustomElem().getData())) {
                // 这类消息都是音视频通话邀请的在线消息，忽略
                SLog.i("ignore online invitee message");
                return;
            }
        }

        onReceiveMessage(msg);
    }

    private void notifyTyping() {
        if (!safetyCall()) {
            SLog.w("notifyTyping unSafetyCall");
            return;
        }
        mCurrentProvider.notifyTyping();
    }

    public void notifyNewFriend(List<V2TIMFriendInfo> timFriendInfoList) {
        if (timFriendInfoList == null || timFriendInfoList.size() == 0) {
            return;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("已和");
        for (V2TIMFriendInfo v2TIMFriendInfo : timFriendInfoList) {
            stringBuilder.append(v2TIMFriendInfo.getUserProfile().getNickName()).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("成为好友");
        ToastUtil.info(TUIKit.getAppContext(),stringBuilder.toString());
    }

    protected void onReceiveMessage(final V2TIMMessage msg) {
        if (!safetyCall()) {
            return;
        }
        addMessage(msg);
    }

    protected abstract boolean isGroup();

    protected void addMessage(V2TIMMessage msg) {
        if (!safetyCall()) {
            return;
        }
        final MessageInfo messageInfo = MessageInfoUtil.TIMMessage2MessageInfo(msg);
        if (messageInfo != null) {
            ChatInfo chatInfo = getCurrentChatInfo();
            boolean isGroupMessage = false;
            String groupID = null;
            String userID = null;
            if (!TextUtils.isEmpty(msg.getGroupID())) {
                // 群组消息
                if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C
                        || !chatInfo.getId().equals(msg.getGroupID())) {
                    return;
                }
                isGroupMessage = true;
                groupID = msg.getGroupID();
            } else if (!TextUtils.isEmpty(msg.getUserID())) {
                // C2C 消息
                if (chatInfo.getType() == V2TIMConversation.V2TIM_GROUP
                        || !chatInfo.getId().equals(msg.getUserID())) {
                    return;
                }
                userID = msg.getUserID();
            } else {
                return;
            }
            mCurrentProvider.addMessageInfo(messageInfo);
            if (isChatFragmentShow()) {
                messageInfo.setRead(true);
            }
            addGroupMessage(messageInfo);

            if (isChatFragmentShow()) {
                if (isGroupMessage) {
                    limitReadReport(null, groupID);
                } else {
                    limitReadReport(userID, null);
                }
            }
        }
    }

    protected void addGroupMessage(MessageInfo msgInfo) {
        // GroupChatManagerKit会重写该方法
    }

    public void deleteMessage(final int position, MessageInfo messageInfo) {
        if (!safetyCall()) {
            SLog.w("deleteMessage unSafetyCall");
            return;
        }
        List<V2TIMMessage> msgs = new ArrayList<>();
        msgs.add(mCurrentProvider.getDataSource().get(position).getTimMessage());
        V2TIMManager.getMessageManager().deleteMessages(msgs, new V2TIMCallback(){

            @Override
            public void onError(int code, String desc) {
                SLog.w( "deleteMessages code:" + code + "|desc:" + desc);
            }

            @Override
            public void onSuccess() {
                SLog.i("deleteMessages success");
                mCurrentProvider.remove(position);
                ConversationManagerKit.getInstance().loadConversation(null);
            }
        });
    }

    public void revokeMessage(final int position, final MessageInfo messageInfo) {
        if (!safetyCall()) {
            SLog.w("revokeMessage unSafetyCall");
            return;
        }
        V2TIMManager.getMessageManager().revokeMessage(messageInfo.getTimMessage(), new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                if (code == REVOKE_TIME_OUT) {
                    ToastUtil.info(TUIKit.getAppContext(),"消息发送已超过2分钟");
                } else {
                    ToastUtil.info(TUIKit.getAppContext(),"撤回失败:" + code + "=" + desc);
                }
            }

            @Override
            public void onSuccess() {
                if (!safetyCall()) {
                    SLog.w("revokeMessage unSafetyCall");
                    return;
                }
                mCurrentProvider.updateMessageRevoked(messageInfo.getId());
                ConversationManagerKit.getInstance().loadConversation(null);
            }
        });
    }

    public void sendMessage(final MessageInfo message, boolean retry, final IUIKitCallBack callBack) {
        if (!safetyCall()) {
            SLog.w("sendMessage unSafetyCall");
            return;
        }
        if (message == null || message.getStatus() == MessageInfo.MSG_STATUS_SENDING) {
            return;
        }
        message.setSelf(true);
        message.setRead(true);
        assembleGroupMessage(message);

        OfflineMessageContainerBean containerBean = new OfflineMessageContainerBean();
        OfflineMessageBean entity = new OfflineMessageBean();
        entity.content = message.getExtra().toString();
        entity.sender = message.getFromUser();
        entity.nickname = TUIKitConfigs.getConfigs().getGeneralConfig().getUserNickname();
        entity.faceUrl = TUIKitConfigs.getConfigs().getGeneralConfig().getUserFaceUrl();
        containerBean.entity = entity;

        String userID = "";
        String groupID = "";
        boolean isGroup = false;
        if (getCurrentChatInfo().getType() == V2TIMConversation.V2TIM_GROUP) {
            groupID = getCurrentChatInfo().getId();
            isGroup = true;
            entity.chatType = V2TIMConversation.V2TIM_GROUP;
            entity.sender = groupID;
        } else {
            userID = getCurrentChatInfo().getId();
        }

        V2TIMOfflinePushInfo v2TIMOfflinePushInfo = new V2TIMOfflinePushInfo();
        v2TIMOfflinePushInfo.setExt(new Gson().toJson(containerBean).getBytes());
        // OPPO必须设置ChannelID才可以收到推送消息，这个channelID需要和控制台一致
        v2TIMOfflinePushInfo.setAndroidOPPOChannelID("tuikit");

        V2TIMMessage v2TIMMessage = message.getTimMessage();
        String msgID = V2TIMManager.getMessageManager().sendMessage(v2TIMMessage, isGroup ? null : userID, isGroup ? groupID : null,
                V2TIMMessage.V2TIM_PRIORITY_DEFAULT, false, v2TIMOfflinePushInfo, new V2TIMSendCallback<V2TIMMessage>() {
                    @Override
                    public void onProgress(int progress) {

                    }

                    @Override
                    public void onError(int code, String desc) {
                        SLog.v("sendMessage fail:" + code + "=" + desc+">>");
                        if (!safetyCall()) {
                            return;
                        }
//                        if(code==20009){//不是好友
//                            ToastUtil.error(TUIKit.getAppContext(),"您和对方不是好友，发送失败");
//                        }else if(code == 20003){//userId错误
//                            ToastUtil.error(TUIKit.getAppContext(),"对方ID错误或不存在，发送失败");
//                        }
                        if (callBack != null) {
                            callBack.onError("SLog",code, desc);
                        }
                        if(mYzChatMessageListener!=null){
                            mYzChatMessageListener.onChatSendMessageError(code,desc);
                        }
                        message.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                        mCurrentProvider.updateMessageInfo(message);
                    }

                    @Override
                    public void onSuccess(V2TIMMessage v2TIMMessage) {
                        SLog.v("sendMessage onSuccess:" + v2TIMMessage.getMsgID());
                        if (!safetyCall()) {
                            SLog.w("sendMessage unSafetyCall");
                            return;
                        }
                        if (callBack != null) {
                            callBack.onSuccess(mCurrentProvider);
                        }
                        message.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                        message.setMsgTime(v2TIMMessage.getTimestamp());
                        mCurrentProvider.updateMessageInfo(message);
                    }
                });

        //消息先展示，通过状态来确认发送是否成功
        SLog.i("sendMessage msgID:" + msgID);
        message.setId(msgID);
        if (message.getMsgType() < MessageInfo.MSG_TYPE_TIPS) {
            message.setStatus(MessageInfo.MSG_STATUS_SENDING);
            if (retry) {
                mCurrentProvider.resendMessageInfo(message);
            } else {
                mCurrentProvider.addMessageInfo(message);
            }
        }
    }

    protected void assembleGroupMessage(MessageInfo message) {
        // GroupChatManager会重写该方法
    }

    public void getAtInfoChatMessages(long atInfoMsgSeq, V2TIMMessage lastMessage, final IUIKitCallBack callBack){
        final ChatInfo chatInfo = getCurrentChatInfo();
        if (atInfoMsgSeq == -1 ||  lastMessage == null || lastMessage.getSeq() <= atInfoMsgSeq){
            return;
        }
        if (chatInfo.getType() == V2TIMConversation.V2TIM_GROUP) {
            V2TIMManager.getMessageManager().getGroupHistoryMessageList(chatInfo.getId(),
                    (int)(lastMessage.getSeq() - atInfoMsgSeq), lastMessage, new V2TIMValueCallback<List<V2TIMMessage>>() {
                        @Override
                        public void onError(int code, String desc) {
                            SLog.e("loadChatMessages getGroupHistoryMessageList failed, code = " + code + ", desc = " + desc);
                        }

                        @Override
                        public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                            processHistoryMsgs(v2TIMMessages, chatInfo, true, callBack);
                        }
                    });
        }
    }


    public void loadChatMessages(int getMessageType, V2TIMMessage lastMessage, final IUIKitCallBack callBack) {
        if (!safetyCall()) {
            return;
        }
        if (mIsLoading) {
            return;
        }
        mIsLoading = true;
        if (!mIsMore) {
            mCurrentProvider.addMessageInfo(null);
            callBack.onSuccess(null);
            mIsLoading = false;
            return;
        }

        V2TIMMessage lastTIMMsg = null;
        if (lastMessage == null) {
            mCurrentProvider.clear();
        } else {
            lastTIMMsg = lastMessage;
        }
//        final int unread = (int) mCurrentConversation.getUnreadMessageNum();
        final ChatInfo chatInfo = getCurrentChatInfo();
        if (getMessageType == TUIKitConstants.GET_MESSAGE_FORWARD) {
            if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
                V2TIMManager.getMessageManager().getC2CHistoryMessageList(chatInfo.getId(), MSG_PAGE_COUNT, lastTIMMsg, new V2TIMValueCallback<List<V2TIMMessage>>() {
                    @Override
                    public void onError(int code, String desc) {
                        mIsLoading = false;
                        callBack.onError("SLog", code, desc);
                    }

                    @Override
                    public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                        processHistoryMsgs(v2TIMMessages, chatInfo, true, callBack);
                    }
                });
            } else {
                V2TIMManager.getMessageManager().getGroupHistoryMessageList(chatInfo.getId(), MSG_PAGE_COUNT, lastTIMMsg, new V2TIMValueCallback<List<V2TIMMessage>>() {
                    @Override
                    public void onError(int code, String desc) {
                        mIsLoading = false;
                        callBack.onError("SLog", code, desc);
                        SLog.e("loadChatMessages getGroupHistoryMessageList failed, code = " + code + ", desc = " + desc);
                    }

                    @Override
                    public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                        processHistoryMsgs(v2TIMMessages, chatInfo, true, callBack);
                    }
                });
            }
        } else if (getMessageType == TUIKitConstants.GET_MESSAGE_TWO_WAY){
            //older
            V2TIMMessageListGetOption optionForward = new V2TIMMessageListGetOption();
            optionForward.setCount(MSG_PAGE_COUNT);
            optionForward.setGetType(V2TIMMessageListGetOption.V2TIM_GET_CLOUD_OLDER_MSG);
            optionForward.setLastMsg(lastTIMMsg);
            if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
                optionForward.setUserID(chatInfo.getId());
            } else {
                optionForward.setGroupID(chatInfo.getId());
            }

            final V2TIMMessage finalLastTIMMsg = lastTIMMsg;
            V2TIMManager.getMessageManager().getHistoryMessageList(optionForward, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int code, String desc) {
                    mIsLoading = false;
                    callBack.onError("SLog", code, desc);
                    SLog.e("loadChatMessages getHistoryMessageList optionForward failed, code = " + code + ", desc = " + desc);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    if (v2TIMMessages == null) {
                        v2TIMMessages = new ArrayList<>();
                    }
                    v2TIMMessages.add(0, finalLastTIMMsg);
                    processTwoWayHistoryMsgs(v2TIMMessages, chatInfo, true, false, callBack);

                    //newer
                    V2TIMMessageListGetOption optionBackward = new V2TIMMessageListGetOption();
                    optionBackward.setCount(MSG_PAGE_COUNT);
                    optionBackward.setGetType(V2TIMMessageListGetOption.V2TIM_GET_CLOUD_NEWER_MSG);
                    optionBackward.setLastMsg(finalLastTIMMsg);
                    if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
                        optionBackward.setUserID(chatInfo.getId());
                    } else {
                        optionBackward.setGroupID(chatInfo.getId());
                    }

                    V2TIMManager.getMessageManager().getHistoryMessageList(optionBackward, new V2TIMValueCallback<List<V2TIMMessage>>() {
                        @Override
                        public void onError(int code, String desc) {
                            mIsLoading = false;
                            callBack.onError("SLog", code, desc);
                            SLog.e( "loadChatMessages getHistoryMessageList optionBackward failed, code = " + code + ", desc = " + desc);
                        }

                        @Override
                        public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                            processTwoWayHistoryMsgs(v2TIMMessages, chatInfo, false, true, callBack);
                        }
                    });
                }
            });
        } else if (getMessageType == TUIKitConstants.GET_MESSAGE_BACKWARD){
            //newer
            V2TIMMessageListGetOption optionBackward = new V2TIMMessageListGetOption();
            optionBackward.setCount(MSG_PAGE_COUNT);
            optionBackward.setGetType(V2TIMMessageListGetOption.V2TIM_GET_CLOUD_NEWER_MSG);
            optionBackward.setLastMsg(lastTIMMsg);
            if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
                optionBackward.setUserID(chatInfo.getId());
            } else {
                optionBackward.setGroupID(chatInfo.getId());
            }

            V2TIMManager.getMessageManager().getHistoryMessageList(optionBackward, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int code, String desc) {
                    mIsLoading = false;
                    callBack.onError("SLog", code, desc);
                    SLog.e( "loadChatMessages getHistoryMessageList optionBackward failed, code = " + code + ", desc = " + desc);
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    processHistoryMsgs(v2TIMMessages, chatInfo, false, callBack);
                }
            });
        } else {
            SLog.e("loadChatMessages getMessageType is invalid");
        }
    }

    private void processTwoWayHistoryMsgs(List<V2TIMMessage> v2TIMMessages, ChatInfo chatInfo, boolean front, boolean calllback, IUIKitCallBack callBack) {
        // 两者不一致说明加载到的消息已经不是当前聊天的消息
        if (chatInfo != getCurrentChatInfo()) {
            return;
        }
        mIsLoading = false;
        if (!safetyCall()) {
            return;
        }

        if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            c2cReadReport(chatInfo.getId());
        } else {
            groupReadReport(chatInfo.getId());
        }

       /* if (v2TIMMessages.size() < MSG_PAGE_COUNT) {
            mIsMore = false;
        }*/
        ArrayList<V2TIMMessage> messages = new ArrayList<>(v2TIMMessages);
        if (front) {
            Collections.reverse(messages);
        }

        List<MessageInfo> msgInfos = MessageInfoUtil.TIMMessages2MessageInfos(messages, isGroup());
        mCurrentProvider.addMessageList(msgInfos, front);
        for (int i = 0; i < msgInfos.size(); i++) {
            MessageInfo info = msgInfos.get(i);
            if (info.getStatus() == MessageInfo.MSG_STATUS_SENDING) {
                sendMessage(info, true, null);
            }
        }

        if (calllback) {
            callBack.onSuccess(mCurrentProvider);
        }
    }

    private void processHistoryMsgs(List<V2TIMMessage> v2TIMMessages, ChatInfo chatInfo, boolean front, IUIKitCallBack callBack) {
        // 两者不一致说明加载到的消息已经不是当前聊天的消息
        if (chatInfo != getCurrentChatInfo()) {
            return;
        }
        mIsLoading = false;
        if (!safetyCall()) {
            return;
        }

        if (chatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            c2cReadReport(chatInfo.getId());
        } else {
            groupReadReport(chatInfo.getId());
        }

        if (v2TIMMessages.size() < MSG_PAGE_COUNT) {
            mIsMore = false;
        }
        ArrayList<V2TIMMessage> messages = new ArrayList<>(v2TIMMessages);
        if (front) {
            Collections.reverse(messages);
        }

        List<MessageInfo> msgInfos = MessageInfoUtil.TIMMessages2MessageInfos(messages, isGroup());
        mCurrentProvider.addMessageList(msgInfos, front);
        for (int i = 0; i < msgInfos.size(); i++) {
            MessageInfo info = msgInfos.get(i);
            if (info.getStatus() == MessageInfo.MSG_STATUS_SENDING) {
                sendMessage(info, true, null);
            }
        }
        callBack.onSuccess(mCurrentProvider);
    }
    /**
     * 收到消息上报已读加频率限制
     * @param userId 如果是 C2C 消息， userId 不为空， groupId 为空
     * @param groupId 如果是 Group 消息， groupId 不为空， userId 为空
     */
    private void limitReadReport(final String userId, final String groupId) {
        final long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastReadReportTime;
        if (timeDifference >= READ_REPORT_INTERVAL) {
            readReport(userId, groupId);
            lastReadReportTime = currentTime;
        } else {
            if (!canReadReport) {
                return;
            }
            long delay = READ_REPORT_INTERVAL - timeDifference;
            canReadReport = false;
            readReportHandler.postDelayed(() -> {
                readReport(userId, groupId);
                lastReadReportTime = System.currentTimeMillis();
                canReadReport = true;
            }, delay);
        }
    }

    private void readReport(String userId, String groupId) {
        if (!TextUtils.isEmpty(userId)) {
            c2cReadReport(userId);
        } else if (!TextUtils.isEmpty(groupId)) {
            groupReadReport(groupId);
        }
    }

    @Override
    public void handleInvoke(String msgID) {
        if (!safetyCall()) {
            SLog.w("handleInvoke unSafetyCall");
            return;
        }
        SLog.i("handleInvoke msgID = " + msgID);
        mCurrentProvider.updateMessageRevoked(msgID);
    }

    protected boolean safetyCall() {
        if (mCurrentProvider == null
                || getCurrentChatInfo() == null) {
            return false;
        }
        return true;
    }

    public void setLastMessageInfo(MessageInfo mLastMessageInfo) {
        this.mLastMessageInfo = mLastMessageInfo;
    }

    public MessageInfo getLastMessageInfo() {
        return mLastMessageInfo;
    }

    @Override
    public void sendMessage(IBaseInfo baseInfo, V2TIMOfflinePushInfo pushInfo, String receiver, boolean isGroup, boolean onlineUserOnly, final IUIKitCallBack callBack) {
        if (!safetyCall()) {
            SLog.w("sendMessage unSafetyCall ,baseInfo : " + baseInfo);
            return;
        }
        if (baseInfo instanceof MessageInfo) {
            final MessageInfo message = (MessageInfo) baseInfo;
            String groupType = "";
            if (isGroup) {
                ChatInfo chatInfo = getCurrentChatInfo();
                groupType = chatInfo.getGroupType();
            }
            if (!isGroup) {
                message.getTimMessage().setExcludedFromUnreadCount(TUIKitConfigs.getConfigs().getGeneralConfig().isExcludedFromUnreadCount());
            } else if (!TextUtils.isEmpty(groupType)) {
                message.getTimMessage().setExcludedFromUnreadCount(TUIKitConfigs.getConfigs().getGeneralConfig().isExcludedFromUnreadCount());
            }

            message.getTimMessage().setExcludedFromLastMessage(TUIKitConfigs.getConfigs().getGeneralConfig().isExcludedFromLastMessage());

            message.setSelf(true);
            message.setRead(true);
            assembleGroupMessage(message);
            String msgID = V2TIMManager.getMessageManager().sendMessage(((MessageInfo) baseInfo).getTimMessage(),
                    isGroup ? null : receiver, isGroup ? receiver : null, V2TIMMessage.V2TIM_PRIORITY_DEFAULT,
                    onlineUserOnly, pushInfo, new V2TIMSendCallback<V2TIMMessage>() {

                        @Override
                        public void onError(int code, String desc) {
                            if (!safetyCall()) {
                                return;
                            }
                            if (callBack != null) {
                                callBack.onError("SLog", code, desc);
                            }
                            message.setStatus(MessageInfo.MSG_STATUS_SEND_FAIL);
                            mCurrentProvider.updateMessageInfo(message);
                        }

                        @Override
                        public void onSuccess(V2TIMMessage v2TIMMessage) {
                            if (!safetyCall()) {
                                return;
                            }
                            if (callBack != null) {
                                callBack.onSuccess(mCurrentProvider);
                            }
                            message.setStatus(MessageInfo.MSG_STATUS_SEND_SUCCESS);
                            message.setMsgTime(v2TIMMessage.getTimestamp());
                            mCurrentProvider.updateMessageInfo(message);
                        }

                        @Override
                        public void onProgress(int progress) {

                        }
                    });
            //消息先展示，通过状态来确认发送是否成功
            if (message.getIsIgnoreShow()) {
                return;
            }
            message.setId(msgID);
            if (message.getMsgType() < MessageInfo.MSG_TYPE_TIPS || message.getMsgType() > MessageInfo.MSG_STATUS_REVOKE) {
                message.setStatus(MessageInfo.MSG_STATUS_SENDING);
                mCurrentProvider.addMessageInfo(message);
            }
        }
    }
    static class MessageReadReportHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
