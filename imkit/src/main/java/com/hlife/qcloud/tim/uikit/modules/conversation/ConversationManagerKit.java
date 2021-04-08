package com.hlife.qcloud.tim.uikit.modules.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.hlife.qcloud.tim.uikit.utils.SharedPreferenceUtils;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationResult;
import com.tencent.imsdk.v2.V2TIMGroupAtInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.util.SLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ConversationManagerKit implements MessageRevokedManager.MessageRevokeHandler {

    private final static String SP_NAME = "_top_conversion_list";
    public final static String SP_IMAGE = "_conversation_group_face";
    private final static String TOP_LIST = "top_list";

    private static final ConversationManagerKit instance = new ConversationManagerKit();

    private ConversationProvider mProvider;
    private final List<YzMessageWatcher> mUnreadWatchers = new ArrayList<>();
    private SharedPreferences mConversationPreferences;
    private LinkedList<ConversationInfo> mTopLinkedList = new LinkedList<>();
    private int mUnreadTotal;

    private ConversationManagerKit() {
        init();
    }

    public static ConversationManagerKit getInstance() {
        return instance;
    }

    private void init() {
        MessageRevokedManager.getInstance().addHandler(this);
    }

    public void loadConversation(long nextSeq, final YzChatType type, final YzConversationDataListener callBack) {
        V2TIMManager.getConversationManager().getConversationList(nextSeq, 100, new V2TIMValueCallback<V2TIMConversationResult>() {
            @Override
            public void onError(int code, String desc) {
                if(callBack!=null){
                    callBack.onConversationError(code,desc);
                }
            }

            @Override
            public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                if(callBack==null){
                    return;
                }
                ArrayList<ConversationInfo> data = new ArrayList<>();
                List<V2TIMConversation> v2TIMConversationList = v2TIMConversationResult.getConversationList();
                long unRead = 0;
                for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
                    ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
                    if(conversationInfo==null){
                        continue;
                    }
                    if(type== YzChatType.GROUP && conversationInfo.isGroup()
                        || (type == YzChatType.SINGLE && !conversationInfo.isGroup())
                        || (type == YzChatType.ALL)){
                        unRead += conversationInfo.getUnRead();
                        data.add(conversationInfo);
                    }
                }
                callBack.onConversationData(data,unRead,v2TIMConversationResult.isFinished()?-1:v2TIMConversationResult.getNextSeq());
            }
        });
    }
    public void getConversation(String conversionId, final YzConversationDataListener listener){
        V2TIMManager.getConversationManager().getConversation(conversionId, new V2TIMValueCallback<V2TIMConversation>() {
            @Override
            public void onError(int code, String desc) {

            }

            @Override
            public void onSuccess(V2TIMConversation v2TIMConversation) {
                ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
                if(listener!=null){
                    listener.onConversationData(conversationInfo);
                }
            }
        });
    }
    public void conversationUnRead(final YzConversationDataListener callBack){
        int singleUnRead = 0;
        int groupUnRead = 0;
        this.conversationUnRead(singleUnRead,groupUnRead,0,callBack);
    }
    private void conversationUnRead(final int singleUnRead, final int groupUnRead, long nextSeq, final YzConversationDataListener callBack){
        V2TIMManager.getConversationManager().getConversationList(nextSeq, 100, new V2TIMValueCallback<V2TIMConversationResult>() {
            @Override
            public void onError(int code, String desc) {
                if(callBack!=null){
                    callBack.onConversationError(code,desc);
                }
            }

            @Override
            public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                if(callBack==null){
                    return;
                }
                List<V2TIMConversation> v2TIMConversationList = v2TIMConversationResult.getConversationList();
                int sUnRead = singleUnRead;
                int gUnRead = groupUnRead;
                for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
                    ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
                    if(conversationInfo==null){
                        continue;
                    }
                    if(conversationInfo.isGroup()){
                        sUnRead+=conversationInfo.getUnRead();
                    }else{
                        gUnRead = conversationInfo.getUnRead();
                    }
                }
                if(v2TIMConversationResult.isFinished()){
                    callBack.onUnReadCount(sUnRead,gUnRead);
                }else{
                    conversationUnRead(sUnRead,gUnRead,v2TIMConversationResult.getNextSeq(),callBack);
                }
            }
        });
    }
    /**
     * 加载会话信息
     *
     * @param callBack
     */
    public void loadConversation(final IUIKitCallBack callBack) {
        mConversationPreferences = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + "-"
                        + V2TIMManager.getInstance().getLoginUser() + SP_NAME,
                Context.MODE_PRIVATE);
        mTopLinkedList = SharedPreferenceUtils.getListData(mConversationPreferences, TOP_LIST, ConversationInfo.class);
        //mProvider初始化值为null,用户注销时会销毁，登录完成进入需再次实例化
        if (mProvider == null) {
            mProvider = new ConversationProvider();
        }

        V2TIMManager.getConversationManager().getConversationList(0, 100, new V2TIMValueCallback<V2TIMConversationResult>() {
            @Override
            public void onError(int code, String desc) {
                SLog.v( "loadConversation getConversationList error, code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                ArrayList<ConversationInfo> infos = new ArrayList<>();
                List<V2TIMConversation> v2TIMConversationList = v2TIMConversationResult.getConversationList();
                mUnreadTotal = 0;
                for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
                    //将 imsdk v2TIMConversation 转换为 UIKit ConversationInfo
                    ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
                    if (conversationInfo != null && !V2TIMManager.GROUP_TYPE_AVCHATROOM.equals(v2TIMConversation.getGroupType())) {
                        mUnreadTotal = mUnreadTotal + conversationInfo.getUnRead();
                        conversationInfo.setType(ConversationInfo.TYPE_COMMON);
                        infos.add(conversationInfo);
                    }
                }
                //排序，imsdk加载处理的已按时间排序，但应用层有置顶会话操作，所有需根据置顶标识再次排序（置顶可考虑做到imsdk同步到服务器？）
                mProvider.setDataSource(sortConversations(infos));
                SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
                //更新消息未读总数
                updateUnreadTotal(mUnreadTotal);
                if (callBack != null) {
                    callBack.onSuccess(mProvider);
                }
            }
        });
    }

    private ConversationProvider mSearchProvide;
    public void loadConversation(final String keyword, final IUIKitCallBack callBack){
        V2TIMManager.getConversationManager().getConversationList(0, 100, new V2TIMValueCallback<V2TIMConversationResult>() {
            @Override
            public void onError(int code, String desc) {
                SLog.v( "loadConversation getConversationList error, code = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess(V2TIMConversationResult v2TIMConversationResult) {
                ArrayList<ConversationInfo> infos = new ArrayList<>();
                List<V2TIMConversation> v2TIMConversationList = v2TIMConversationResult.getConversationList();
                for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
                    //将 imsdk v2TIMConversation 转换为 UIKit ConversationInfo
                    ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
                    if (conversationInfo != null && conversationInfo.getTitle().contains(keyword)) {
                        conversationInfo.setType(ConversationInfo.TYPE_COMMON);
                        infos.add(conversationInfo);
                    }
                }
                if(mSearchProvide==null){
                    mSearchProvide = new ConversationProvider();
                }
                mSearchProvide.setDataSource(infos);
                //排序，imsdk加载处理的已按时间排序，但应用层有置顶会话操作，所有需根据置顶标识再次排序（置顶可考虑做到imsdk同步到服务器？）
                //更新消息未读总数
                if (callBack != null) {
                    callBack.onSuccess(mSearchProvide);
                }
            }
        });
    }
    /**
     * 部分会话刷新（包括多终端已读上报同步）
     *
     * @param v2TIMConversationList 需要刷新的会话列表
     */
    public void onRefreshConversation(List<V2TIMConversation> v2TIMConversationList) {
        SLog.v( "onRefreshConversation conversations:" + v2TIMConversationList.size());
        if (mProvider == null) {
            return;
        }
        ArrayList<ConversationInfo> infos = new ArrayList<>();
        for (int i = 0; i < v2TIMConversationList.size(); i++) {
            V2TIMConversation v2TIMConversation = v2TIMConversationList.get(i);
            SLog.v( "refreshConversation v2TIMConversation " + v2TIMConversation.getShowName());
            ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
            if (conversationInfo != null && !V2TIMManager.GROUP_TYPE_AVCHATROOM.equals(v2TIMConversation.getGroupType())) {
                infos.add(conversationInfo);
            }
        }
        if (infos.size() == 0) {
            return;
        }
        List<ConversationInfo> dataSource = mProvider.getDataSource();
        ArrayList<ConversationInfo> exists = new ArrayList<>();
        for (int j = 0; j < infos.size(); j++) {
            ConversationInfo update = infos.get(j);
            boolean exist = false;
            for (int i = 0; i < dataSource.size(); i++) {
                ConversationInfo cacheInfo = dataSource.get(i);
                //单个会话刷新时找到老的会话数据，替换，这里需要增加群组类型的判断，防止好友id与群组id相同
                if (cacheInfo.getId().equals(update.getId()) && cacheInfo.isGroup() == update.isGroup()) {
                    dataSource.remove(i);
                    dataSource.add(i, update);
                    exists.add(update);
                    //infos.remove(j);
                    //需同步更新未读计数
                    mUnreadTotal = mUnreadTotal - cacheInfo.getUnRead() + update.getUnRead();
                    SLog.v( "onRefreshConversation after mUnreadTotal = " + mUnreadTotal);
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                mUnreadTotal += update.getUnRead();
                SLog.i( "onRefreshConversation exist = " + exist + ", mUnreadTotal = " + mUnreadTotal);
            }
        }
        updateUnreadTotal(mUnreadTotal);
        infos.removeAll(exists);
        if (infos.size() > 0) {
            dataSource.addAll(infos);
        }
        mProvider.setDataSource(sortConversations(dataSource));
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    /**
     * TIMConversation转换为ConversationInfo
     *
     * @param conversation
     * @return
     */
    private ConversationInfo TIMConversation2ConversationInfo(final V2TIMConversation conversation) {
        if (conversation == null) {
            return null;
        }
//        SLog.i( "TIMConversation2ConversationInfo id:" + conversation.getConversationID()
//                + "|name:" + conversation.getShowName()
//                + "|unreadNum:" + conversation.getUnreadCount());
        V2TIMMessage message = conversation.getLastMessage();
        if (message == null) {
            return null;
        }
        final ConversationInfo info = new ConversationInfo();
        int type = conversation.getType();
        if (type != V2TIMConversation.V2TIM_C2C && type != V2TIMConversation.V2TIM_GROUP) {
            return null;
        }

        boolean isGroup = type == V2TIMConversation.V2TIM_GROUP;
        info.setLastMessageTime(message.getTimestamp());
        List<MessageInfo> list = MessageInfoUtil.TIMMessage2MessageInfo(message);
        if (list != null && list.size() > 0) {
            info.setLastMessage(list.get(list.size() - 1));
        }

        int atInfoType = getAtInfoType(conversation);
        switch (atInfoType){
            case V2TIMGroupAtInfo.TIM_AT_ME:
                info.setAtInfoText(TUIKit.getAppContext().getString(R.string.ui_at_me));
                break;
            case V2TIMGroupAtInfo.TIM_AT_ALL:
                info.setAtInfoText(TUIKit.getAppContext().getString(R.string.ui_at_all));
                break;
            case V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME:
                info.setAtInfoText(TUIKit.getAppContext().getString(R.string.ui_at_all_me));
                break;
            default:
                info.setAtInfoText("");
                break;

        }
        info.setTitle(conversation.getShowName());
        if (isGroup) {
            fillConversationUrlForGroup(conversation, info);
        } else {
            List<Object> faceList = new ArrayList<>();
            if (TextUtils.isEmpty(conversation.getFaceUrl())) {
                faceList.add(R.drawable.default_head);
            } else {
                faceList.add(conversation.getFaceUrl());
            }
            info.setIconUrlList(faceList);
        }
        if (isGroup) {
            info.setId(conversation.getGroupID());
        } else {
            info.setId(conversation.getUserID());
        }
        info.setConversationId(conversation.getConversationID());
        info.setGroup(isGroup);
        info.setUnRead(conversation.getUnreadCount());
        return info;
    }

    private int getAtInfoType(V2TIMConversation conversation){
        int atInfoType;
        boolean atMe = false;
        boolean atAll = false;

        List<V2TIMGroupAtInfo> atInfoList = conversation.getGroupAtInfoList();

        if (atInfoList == null || atInfoList.isEmpty()){
            return V2TIMGroupAtInfo.TIM_AT_UNKNOWN;
        }

        for(V2TIMGroupAtInfo atInfo : atInfoList){
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ME){
                atMe = true;
                continue;
            }
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ALL){
                atAll = true;
                continue;
            }
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME){
                atMe = true;
                atAll = true;
            }
        }

        if (atAll && atMe){
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME;
        } else if (atAll){
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ALL;
        } else if (atMe){
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ME;
        } else {
            atInfoType = V2TIMGroupAtInfo.TIM_AT_UNKNOWN;
        }

        return atInfoType;
    }

    private void fillConversationUrlForGroup(final V2TIMConversation conversation, final ConversationInfo info) {
        if (TextUtils.isEmpty(conversation.getFaceUrl())) {
            String savedIcon = getGroupConversationAvatar(conversation.getConversationID());
            if (TextUtils.isEmpty(savedIcon)) {
                fillFaceUrlList(conversation.getGroupID(), info);
            } else {
                List<Object> list = new ArrayList<>();
                list.add(savedIcon);
                info.setIconUrlList(list);
            }
        } else {
            List<Object> list = new ArrayList<>();
            list.add(conversation.getFaceUrl());
            info.setIconUrlList(list);
        }
    }

    private void fillFaceUrlList(final String groupID, final ConversationInfo info) {
        V2TIMManager.getGroupManager().getGroupMemberList(groupID, V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL, 0, new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e( "getGroupMemberList failed! groupID:" + groupID + "|code:" + code + "|desc: " + desc);
            }

            @Override
            public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                List<V2TIMGroupMemberFullInfo> v2TIMGroupMemberFullInfoList = v2TIMGroupMemberInfoResult.getMemberInfoList();
                int faceSize = Math.min(v2TIMGroupMemberFullInfoList.size(), 9);
                List<Object> urlList = new ArrayList<>();
                for (int i = 0; i < faceSize; i++) {
                    V2TIMGroupMemberFullInfo v2TIMGroupMemberFullInfo = v2TIMGroupMemberFullInfoList.get(i);
                    if (TextUtils.isEmpty(v2TIMGroupMemberFullInfo.getFaceUrl())) {
                        urlList.add(R.drawable.default_user_icon);
                    } else {
                        urlList.add(v2TIMGroupMemberFullInfo.getFaceUrl());
                    }
                }
                info.setIconUrlList(urlList);
                mProvider.updateAdapter(info.getConversationId());
                if(mSearchProvide!=null){
                    mSearchProvide.updateAdapter(info.getConversationId());
                }
            }
        });
    }

    /**
     * 将某个会话置顶
     *
     * @param index
     * @param conversation
     */
    public void setConversationTop(int index, ConversationInfo conversation) {
        SLog.i( "setConversationTop index:" + index + "|conversation:" + conversation);
        if (!conversation.isTop()) {
            mTopLinkedList.remove(conversation);
            mTopLinkedList.addFirst(conversation);
            conversation.setTop(true);
        } else {
            conversation.setTop(false);
            mTopLinkedList.remove(conversation);
        }
        mProvider.setDataSource(sortConversations(mProvider.getDataSource()));
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    /**
     * 会话置顶操作
     *
     * @param id   会话ID
     * @param flag 是否置顶
     */
    public void setConversationTop(String id, boolean flag) {
        SLog.i( "setConversationTop id:" + id + "|flag:" + flag);
        handleTopData(id, flag);
        mProvider.setDataSource(sortConversations(mProvider.getDataSource()));
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    private boolean isTop(String id) {
        if (mTopLinkedList == null || mTopLinkedList.size() == 0) {
            return false;
        }
        for (ConversationInfo info : mTopLinkedList) {
            if (TextUtils.equals(info.getId(), id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 会话置顶的本地储存操作，目前用SharePreferences来持久化置顶信息
     *
     * @param id
     * @param flag
     */
    private void handleTopData(String id, boolean flag) {
        ConversationInfo conversation = null;
        if(mProvider==null){
            return;
        }
        List<ConversationInfo> conversationInfos = mProvider.getDataSource();
        for (int i = 0; i < conversationInfos.size(); i++) {
            ConversationInfo info = conversationInfos.get(i);
            if (info.getId().equals(id)) {
                conversation = info;
                break;
            }
        }
        if (conversation == null) {
            return;
        }
        if (flag) {
            if (!isTop(conversation.getId())) {
                mTopLinkedList.remove(conversation);
                mTopLinkedList.addFirst(conversation);
                conversation.setTop(true);
            } else {
                return;
            }
        } else {
            if (isTop(conversation.getId())) {
                conversation.setTop(false);
                mTopLinkedList.remove(conversation);
            } else {
                return;
            }
        }
        SharedPreferenceUtils.putListData(mConversationPreferences, TOP_LIST, mTopLinkedList);
    }

    /**
     * 删除会话，会将本地会话数据从imsdk中删除
     *
     * @param index        在数据源中的索引
     * @param conversation 会话信息
     */
    public void deleteConversation(int index, ConversationInfo conversation) {
        SLog.i( "deleteConversation index:" + index + "|conversation:" + conversation);
        V2TIMManager.getConversationManager().deleteConversation(conversation.getConversationId(), new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e( "deleteConversation error:" + code + ", desc:" + desc);
            }

            @Override
            public void onSuccess() {
                SLog.i( "deleteConversation success");
            }
        });
        handleTopData(conversation.getId(), false);
        mProvider.deleteConversation(index);
        updateUnreadTotal(mUnreadTotal - conversation.getUnRead());
    }

    /**
     * 删除会话，只删除数据源中的会话信息
     *
     * @param id C2C：对方的 userID；Group：群 ID
     */
    public void deleteConversation(String id, boolean isGroup) {
        SLog.i( "deleteConversation id:" + id + "|isGroup:" + isGroup);
        handleTopData(id, false);
        if(mProvider==null){
            return;
        }
        List<ConversationInfo> conversationInfos = mProvider.getDataSource();
        for (int i = 0; i < conversationInfos.size(); i++) {
            ConversationInfo info = conversationInfos.get(i);
            if (info.getId().equals(id)) {
                updateUnreadTotal(mUnreadTotal - info.getUnRead());
                break;
            }
        }
        String conversationID = "";
        if (mProvider != null) {
            List<ConversationInfo> conversationInfoList = mProvider.getDataSource();
            for (ConversationInfo conversationInfo : conversationInfoList) {
                if (isGroup == conversationInfo.isGroup() && conversationInfo.getId().equals(id)) {
                    conversationID = conversationInfo.getConversationId();
                    break;
                }
            }
            if (!TextUtils.isEmpty(conversationID)) {
                mProvider.deleteConversation(conversationID);
            }
        }
        if (!TextUtils.isEmpty(conversationID)) {
            V2TIMManager.getConversationManager().deleteConversation(conversationID, new V2TIMCallback() {
                @Override
                public void onError(int code, String desc) {
                    SLog.i( "deleteConversation error:" + code + ", desc:" + desc);
                }

                @Override
                public void onSuccess() {
                    SLog.i( "deleteConversation success");
                }
            });
        }
    }

    /**
     * 添加会话
     *
     * @param conversationInfo
     * @return
     */
    public boolean addConversation(ConversationInfo conversationInfo) {
        List<ConversationInfo> conversationInfos = new ArrayList<>();
        conversationInfos.add(conversationInfo);
        return mProvider.addConversations(conversationInfos);
    }

    /**
     * 会话数据排序，添加了置顶标识的处理
     *
     * @param sources
     * @return
     */
    private List<ConversationInfo> sortConversations(List<ConversationInfo> sources) {
        List<ConversationInfo> normalConversations = new ArrayList<>();
        List<ConversationInfo> topConversations = new ArrayList<>();

        for (int i = 0; i <= sources.size() - 1; i++) {
            ConversationInfo conversation = sources.get(i);
            if (isTop(conversation.getId())) {
                conversation.setTop(true);
                topConversations.add(conversation);
            } else {
                normalConversations.add(conversation);
            }
        }

        mTopLinkedList.clear();
        mTopLinkedList.addAll(topConversations);
        if(topConversations.size() > 1) {
            Collections.sort(topConversations); // 置顶会话列表页也需要按照最后一条时间排序，由新到旧，如果旧会话收到新消息，会排序到前面
        }
        ArrayList<ConversationInfo> conversationInfos = new ArrayList<>(topConversations);
        if(normalConversations.size() > 1) {
            Collections.sort(normalConversations); // 正常会话也是按照最后一条消息时间排序，由新到旧
        }
        conversationInfos.addAll(normalConversations);
        return conversationInfos;
    }

    /**
     * 更新会话未读计数
     *
     * @param unreadTotal
     */
    public void updateUnreadTotal(int unreadTotal) {
        SLog.i( "updateUnreadTotal:" + unreadTotal);
        mUnreadTotal = unreadTotal;
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateUnread(mUnreadTotal);
        }
    }

    /**
     * 获取会话总的未读数
     *
     * @return 未读数量
     */
    public int getUnreadTotal() {
        return mUnreadTotal;
    }

    public boolean isTopConversation(String groupId) {
        SLog.i( "isTopConversation:" + groupId);
        return isTop(groupId);
    }

    /**
     * 消息撤回回调
     *
     * @param msgID
     */
    @Override
    public void handleInvoke(String msgID) {
        SLog.i( "handleInvoke msgID:" + msgID);
        if (mProvider != null) {
            loadConversation(null);
        }
    }


    /**
     * 添加未读计数监听器
     *
     * @param messageUnreadWatcher
     */
    public void addMessageWatcher(YzMessageWatcher messageUnreadWatcher) {
        if (!mUnreadWatchers.contains(messageUnreadWatcher)) {
            mUnreadWatchers.add(messageUnreadWatcher);
            messageUnreadWatcher.updateUnread(mUnreadTotal);
        }
    }

    /**
     * 移除未读计数监听器
     *
     * @param messageUnreadWatcher
     */
    public void removeMessageWatcher(YzMessageWatcher messageUnreadWatcher) {
        if (messageUnreadWatcher == null) {
            mUnreadWatchers.clear();
        } else {
            mUnreadWatchers.remove(messageUnreadWatcher);
        }
    }

    /**
     * 与UI做解绑操作，避免内存泄漏
     */
    public void destroyConversation() {
        SLog.i( "destroyConversation");
        if (mProvider != null) {
            mProvider.attachAdapter(null);
        }
        if (mUnreadWatchers != null) {
            mUnreadWatchers.clear();
        }
    }

    public void destroyConversationSearch(){
        if(mSearchProvide!=null){
            mSearchProvide.attachAdapter(null);
            mSearchProvide = null;
        }
    }

    public String getGroupConversationAvatar(String groupId) {
        SharedPreferences sp = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + SP_IMAGE, Context.MODE_PRIVATE);
        final String savedIcon = sp.getString(groupId, "");
        if (!TextUtils.isEmpty(savedIcon) && new File(savedIcon).isFile() && new File(savedIcon).exists()) {
            return savedIcon;
        }
        return "";
    }

    public void setGroupConversationAvatar(String groupId, String url) {
        SharedPreferences sp = TUIKit.getAppContext().getSharedPreferences(
                TUIKitConfigs.getConfigs().getGeneralConfig().getSDKAppId() + SP_IMAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(groupId, url);
        editor.apply();
    }
    /**
     * 通讯录变化
     */
    public void updateContacts(){
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateContacts();
        }
    }
    /**
     * 刷新全部会话
     */
    public void updateConversion(){
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateConversion();
        }
    }

}