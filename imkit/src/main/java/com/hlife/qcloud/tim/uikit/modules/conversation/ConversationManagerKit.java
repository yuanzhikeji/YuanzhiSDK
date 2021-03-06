package com.hlife.qcloud.tim.uikit.modules.conversation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzDeleteConversationListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupFaceListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.DraftInfo;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.hlife.qcloud.tim.uikit.utils.DateTimeUtil;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationResult;
import com.tencent.imsdk.v2.V2TIMGroupApplication;
import com.tencent.imsdk.v2.V2TIMGroupApplicationResult;
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
import java.util.List;

public class ConversationManagerKit implements MessageRevokedManager.MessageRevokeHandler {

    public final static String SP_IMAGE = "_conversation_group_face";

    private static final ConversationManagerKit instance = new ConversationManagerKit();

    private ConversationProvider mProvider;
    private final List<YzMessageWatcher> mUnreadWatchers = new ArrayList<>();
    private final List<YzGroupFaceListener> mGroupFaceListener = new ArrayList<>();
    private int mUnreadTotal=0;
    private YzChatType mType = YzChatType.ALL;
    private List<String> mApplyGroupID = new ArrayList<>();

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
                    if((type== YzChatType.GROUP && conversationInfo.isGroup())
                        || (type == YzChatType.C2C && !conversationInfo.isGroup())
                        || (type == YzChatType.ALL)){
                        unRead += conversationInfo.getUnRead();
                        data.add(conversationInfo);
                    }
                }
                callBack.onConversationData(sortConversations(data),unRead,v2TIMConversationResult.isFinished()?-1:v2TIMConversationResult.getNextSeq());
            }
        });
    }
    public void getConversation(final String id, final YzConversationDataListener listener){
        searchConversation(id,0,listener);
    }
    private void searchConversation(final String id,long nextSeq,final YzConversationDataListener listener){
        loadConversation(nextSeq, YzChatType.ALL, new YzConversationDataListener() {
            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                super.onConversationData(data, unRead, nextSeq);
                if(data.size()==0){
                    listener.onConversationData(null);
                }else{
                    ConversationInfo conversationInfo = null;
                    for (int i = 0; i < data.size(); i++) {
                        ConversationInfo item = data.get(i);
                        if(item.getId().equals(id)){
                            conversationInfo = item;
                            break;
                        }
                    }
                    if(conversationInfo==null && nextSeq!=-1){
                        searchConversation(id,nextSeq,listener);
                    }else{
                        listener.onConversationData(conversationInfo);
                    }
                }
            }
        });
    }
    /**
     * ???????????????????????????
     */
    public void deleteConversation(String userIdOrGroupId, YzDeleteConversationListener listener){
        deleteConversation(userIdOrGroupId,0,listener);
    }
    public void deleteConversation(String id,long nextSeq,final YzDeleteConversationListener listener){
        loadConversation(nextSeq, YzChatType.ALL, new YzConversationDataListener() {
            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                super.onConversationData(data, unRead, nextSeq);
                if(data.size()==0){
                    if(listener!=null){
                        listener.success();
                    }
                }else{
                    for (int i = 0; i < data.size(); i++) {
                        ConversationInfo item = data.get(i);
                        if(item.getId().equals(id)){
                            V2TIMManager.getConversationManager().deleteConversation(item.getConversationId(), new V2TIMCallback() {
                                @Override
                                public void onError(int code, String desc) {
                                    if(listener!=null){
                                        listener.error(code,desc);
                                    }
                                }

                                @Override
                                public void onSuccess() {
                                    if(listener!=null){
                                        listener.success();
                                    }
                                }
                            });
                            break;
                        }
                    }
                    if(nextSeq!=-1){
                        deleteConversation(id,nextSeq,listener);
                    }else{
                        if(listener!=null){
                            listener.success();
                        }
                    }
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
                        gUnRead += conversationInfo.getUnRead();
                    }else{
                        sUnRead += conversationInfo.getUnRead();
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
     * ??????????????????
     *
     * @param callBack
     */
    public void loadConversation(final IUIKitCallBack callBack){
        loadConversation(mType,callBack);
    }
    public void loadConversation(YzChatType chatType,final IUIKitCallBack callBack) {
        this.mType = chatType;
        //mProvider???????????????null,???????????????????????????????????????????????????????????????
        if (mProvider == null) {
            mProvider = new ConversationProvider();
        }
        this.groupApplicationList(new YzGroupDataListener() {

            @Override
            public void joinMember(List<GroupApplyInfo> applies) {
                allConversation(0, new ArrayList<>(),callBack);
            }
        });
    }

    private void allConversation(long nextSeq,final ArrayList<ConversationInfo> dataArray,final IUIKitCallBack callBack){
        loadConversation(nextSeq, mType, new YzConversationDataListener() {
            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                super.onConversationData(data, unRead, nextSeq);
                dataArray.addAll(data);
                if(nextSeq!=-1){
                    allConversation(nextSeq,dataArray,callBack);
                }else{
                    //?????????imsdk???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????imsdk????????????????????????
                    mProvider.setDataSource(sortConversations(dataArray));
                    getTotalUnreadMessageCount();
                    if (callBack != null) {
                        callBack.onSuccess(mProvider);
                    }
                }
            }
        });
    }
    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param v2TIMConversationList ???????????????????????????
     */
    public void onRefreshConversation(List<V2TIMConversation> v2TIMConversationList) {
        if (mProvider == null) {
            getTotalUnreadMessageCount();
            return;
        }
        ArrayList<ConversationInfo> infos = new ArrayList<>();
        for (int i = 0; i < v2TIMConversationList.size(); i++) {
            V2TIMConversation v2TIMConversation = v2TIMConversationList.get(i);
            ConversationInfo conversationInfo = TIMConversation2ConversationInfo(v2TIMConversation);
            if((mType== YzChatType.GROUP && conversationInfo.isGroup())
                    || (mType == YzChatType.C2C && !conversationInfo.isGroup())
                    || (mType == YzChatType.ALL)){
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
            for (int i = 0; i < dataSource.size(); i++) {
                ConversationInfo cacheInfo = dataSource.get(i);
                //???????????????????????????????????????????????????????????????????????????????????????????????????????????????id?????????id??????
                if (cacheInfo.getId().equals(update.getId()) && cacheInfo.isGroup() == update.isGroup()) {
                    dataSource.remove(i);
                    dataSource.add(i, update);
                    exists.add(update);
                    //infos.remove(j);
                    break;
                }
            }
        }
        infos.removeAll(exists);
        if (infos.size() > 0) {
            dataSource.addAll(infos);
        }
        mProvider.setDataSource(sortConversations(dataSource));
    }

    /**
     * TIMConversation?????????ConversationInfo
     *
     * @param conversation
     * @return
     */
    public SearchDataMessage TIMConversation2ConversationInfo(final V2TIMConversation conversation) {
        if (conversation == null) {
            return null;
        }
        final SearchDataMessage info = new SearchDataMessage();
        int type = conversation.getType();
        if (type != V2TIMConversation.V2TIM_C2C && type != V2TIMConversation.V2TIM_GROUP) {
            return null;
        }
        boolean isGroup = type == V2TIMConversation.V2TIM_GROUP;
        String draftText = conversation.getDraftText();
        if (!TextUtils.isEmpty(draftText)) {
            DraftInfo draftInfo = new DraftInfo();
            draftInfo.setDraftText(draftText);
            draftInfo.setDraftTime(conversation.getDraftTimestamp());
            info.setDraft(draftInfo);
        }
        V2TIMMessage message = conversation.getLastMessage();
        if (message == null) {
            long time = DateTimeUtil.getStringToDate("0001-01-01 00:00:00", "yyyy-MM-dd HH:mm:ss");
            info.setLastMessageTime(time);
        } else {
            info.setLastMessageTime(message.getTimestamp());
        }
        MessageInfo messageInfo = MessageInfoUtil.TIMMessage2MessageInfo(message);
        if (messageInfo != null) {
            info.setLastMessage(messageInfo);
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
            info.setGroupType(conversation.getGroupType());
        } else {
            info.setId(conversation.getUserID());
        }

        info.setRevOpt(conversation.getRecvOpt() == V2TIMMessage.V2TIM_RECEIVE_NOT_NOTIFY_MESSAGE);
        info.setConversationId(conversation.getConversationID());
        info.setGroup(isGroup);
        // AVChatRoom ?????????????????????
        if (!V2TIMManager.GROUP_TYPE_AVCHATROOM.equals(conversation.getGroupType())) {
            info.setUnRead(conversation.getUnreadCount());
        }
        info.setTop(conversation.isPinned());
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

    public void refreshApply(GroupChatManagerKit.GroupNotifyHandler listener){
        this.groupApplicationList(new YzGroupDataListener() {

            @Override
            public void joinMember(List<GroupApplyInfo> applies) {
                if(mProvider!=null){
                    List<ConversationInfo> data = mProvider.getDataSource();
                    String applyStr = TUIKit.getAppContext().getString(R.string.ui_group_apply);
                    for (ConversationInfo info:data) {
                        String at = info.getAtInfoText();
                        if(!TextUtils.isEmpty(at) && at.contains(applyStr) && !mApplyGroupID.contains(info.getId())){
                            at = at.replace(applyStr,"");
                            info.setAtInfoText(at);
                        }
                    }
                    mProvider.updateAdapter();
                    if(listener!=null){
                        listener.onApplied(mApplyGroupID.size());
                    }
                }
            }
        });
    }

    public void groupApplicationList(YzGroupDataListener listener){
        V2TIMManager.getGroupManager().getGroupApplicationList(new V2TIMValueCallback<V2TIMGroupApplicationResult>() {
            @Override
            public void onError(int code, String desc) {
                if(listener==null || code == 6015){
                    return;
                }
                listener.joinMember(new ArrayList<>());
            }

            @Override
            public void onSuccess(V2TIMGroupApplicationResult v2TIMGroupApplicationResult) {
                List<V2TIMGroupApplication> v2TIMGroupApplications = v2TIMGroupApplicationResult.getGroupApplicationList();
                List<GroupApplyInfo> applies = new ArrayList<>();
                mApplyGroupID = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupApplications.size(); i++) {
                    V2TIMGroupApplication v = v2TIMGroupApplications.get(i);
                    if(v.getHandleStatus() == V2TIMGroupApplication.V2TIM_GROUP_APPLICATION_HANDLE_STATUS_UNHANDLED){
                        GroupApplyInfo info = new GroupApplyInfo(v);
                        info.setStatus(0);
                        applies.add(info);
                        mApplyGroupID.add(info.getGroupApplication().getGroupID());
                    }

                }
                if(listener==null){
                    return;
                }
                listener.joinMember(applies);
            }
        });
    }

    private void fillConversationUrlForGroup(final V2TIMConversation conversation, final ConversationInfo info) {
//        SLog.e(conversation.getGroupID()+">"+conversation.getShowName()+":"+conversation.getFaceUrl());
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
                if(mProvider!=null){
                    mProvider.updateAdapter(info.getConversationId());
                }
                if(mGroupFaceListener.size()>0){
                    for (YzGroupFaceListener listener:mGroupFaceListener) {
                        listener.onFaceUrl(info);
                    }
                }
            }
        });
    }
    /**
     * ??????????????????
     */
    public void setConversationTop(final ConversationInfo conversation, final IUIKitCallBack callBack) {
        final boolean setTop = !conversation.isTop();
        V2TIMManager.getConversationManager().pinConversation(conversation.getConversationId(), setTop, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                conversation.setTop(setTop);
                mProvider.setDataSource(sortConversations(mProvider.getDataSource()));
            }

            @Override
            public void onError(int code, String desc) {
                if (callBack != null) {
                    callBack.onError("setConversationTop", code, desc);
                }
            }
        });
    }

    /**
     * ??????????????????
     *
     * @param id   ??????ID
     * @param isTop ????????????
     */
    public void setConversationTop(String id, final boolean isTop, final IUIKitCallBack callBack) {
        ConversationInfo conversation = null;
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
        final String conversationID = conversation.getConversationId();
        V2TIMManager.getConversationManager().pinConversation(conversation.getConversationId(), isTop, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                List<ConversationInfo> conversationInfoList = mProvider.getDataSource();
                for (int i = 0; i < conversationInfoList.size(); i++) {
                    ConversationInfo info = conversationInfoList.get(i);
                    if (info.getId().equals(conversationID)) {
                        info.setTop(isTop);
                        break;
                    }
                }
                mProvider.setDataSource(sortConversations(mProvider.getDataSource()));
            }

            @Override
            public void onError(int code, String desc) {
                if (callBack != null) {
                    callBack.onError("setConversationTop", code, desc);
                }
            }
        });
    }
    /**
     * ????????????
     *
     * @param index        ????????????????????????
     * @param conversation ????????????
     */
    public void clearConversationMessage(int index, ConversationInfo conversation) {
        if (conversation == null || TextUtils.isEmpty(conversation.getConversationId())) {
            return;
        }

        if (conversation.isGroup()) {
            V2TIMManager.getMessageManager().clearGroupHistoryMessage(conversation.getId(), new V2TIMCallback() {
                @Override
                public void onError(int code, String desc) {
                }

                @Override
                public void onSuccess() {
                }
            });
        } else {
            V2TIMManager.getMessageManager().clearC2CHistoryMessage(conversation.getId(), new V2TIMCallback() {
                @Override
                public void onError(int code, String desc) {
                }

                @Override
                public void onSuccess() {
                }
            });
        }
    }

    /**
     * ??????????????????????????????????????????imsdk?????????
     *
     * @param index        ????????????????????????
     * @param conversation ????????????
     */
    public void deleteConversation(int index, ConversationInfo conversation) {
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
        if(mProvider!=null){
            mProvider.deleteConversation(index);
        }
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param id C2C???????????? userID???Group?????? ID
     */
    public void deleteConversation(String id, boolean isGroup) {
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
                }

                @Override
                public void onSuccess() {
                }
            });
        }
    }

    /**
     * ????????????
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
     * ???????????????????????????????????????????????????
     *
     * @param sources
     * @return
     */
    private List<ConversationInfo> sortConversations(List<ConversationInfo> sources) {
        List<ConversationInfo> normalConversations = new ArrayList<>();
        List<ConversationInfo> topConversations = new ArrayList<>();

        for (int i = 0; i <= sources.size() - 1; i++) {
            ConversationInfo conversation = sources.get(i);
            if (conversation.isTop()) {
                topConversations.add(conversation);
            } else {
                normalConversations.add(conversation);
            }
        }

        if(topConversations.size() > 1) {
            Collections.sort(topConversations); // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        }
        ArrayList<ConversationInfo> conversationInfos = new ArrayList<>(topConversations);
        if(normalConversations.size() > 1) {
            Collections.sort(normalConversations); // ?????????????????????????????????????????????????????????????????????
        }
        conversationInfos.addAll(normalConversations);
        return conversationInfos;
    }

    /**
     * ????????????????????????
     *
     */
    private void getTotalUnreadMessageCount(){
        V2TIMManager.getConversationManager().getTotalUnreadMessageCount(new V2TIMValueCallback<Long>() {
            @Override
            public void onSuccess(Long aLong) {
                updateUnreadTotal(aLong.intValue());
            }

            @Override
            public void onError(int code, String desc) {
                SLog.e(code+">"+desc);
                updateUnreadTotal(mUnreadTotal);
            }
        });
    }
    public void updateTotalUnreadMessageCount(long totalUnreadCount) {
        updateUnreadTotal((int)totalUnreadCount);
    }
    private int mTempUnreadTotal = 0;
    public void updateUnreadTotal(int unreadTotal) {
        if(unreadTotal==mTempUnreadTotal){//???????????????????????????
            SLog.i("unread unchanged!");
            return;
        }
        mUnreadTotal = unreadTotal;
        mTempUnreadTotal = mUnreadTotal;
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateUnread(mUnreadTotal);
        }
    }

    /**
     * ???????????????????????????
     *
     * @return ????????????
     */
    public int getUnreadTotal() {
        return mUnreadTotal;
    }

    public boolean isTopConversation(String conversationID) {
        List<ConversationInfo> conversationInfos = mProvider.getDataSource();
        for (int i = 0; i < conversationInfos.size(); i++) {
            ConversationInfo info = conversationInfos.get(i);
            if (info.getId().equals(conversationID)) {
                return info.isTop();
            }
        }
        return false;
    }
    /**
     * ??????????????????
     *
     * @param msgID
     */
    @Override
    public void handleInvoke(String msgID) {
        if (mProvider != null) {
            loadConversation(mType,null);
        }
    }


    /**
     * ???????????????????????????
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
     * ???????????????????????????
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
     * ???UI????????????????????????????????????
     */
    public void destroyConversation() {
        if (mProvider != null) {
            mProvider.attachAdapter(null);
        }
        mProvider = null;
        mUnreadWatchers.clear();
        mGroupFaceListener.clear();
    }

    public void addGroupFaceListener(YzGroupFaceListener listener){
        if(listener!=null){
            mGroupFaceListener.add(listener);
        }
    }

    public void removeGroupFaceListener(YzGroupFaceListener listener){
        mGroupFaceListener.remove(listener);
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
     * ???????????????
     */
    public void updateContacts(){
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateContacts();
        }
    }
    /**
     * ??????????????????
     */
    public void updateConversion(){
        SLog.e("updateConversion:"+mUnreadWatchers.size());
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            SLog.e(i+">"+mUnreadWatchers.get(i));
            mUnreadWatchers.get(i).updateConversion();
        }
    }
    /**
     * ?????????????????????
     */
    public void updateJoinGroup(){
        for (int i = 0; i < mUnreadWatchers.size(); i++) {
            mUnreadWatchers.get(i).updateJoinGroup();
        }
        loadConversation(null);
    }
}