package com.hlife.qcloud.tim.uikit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.hlife.data.IMFriendManager;
import com.hlife.data.YzUpdateFriendCallback;
import com.hlife.qcloud.tim.uikit.base.IMEventListener;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.base.TUIKitListenerManager;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.business.active.MwWorkActivity;
import com.hlife.qcloud.tim.uikit.business.active.OSSFileActivity;
import com.hlife.qcloud.tim.uikit.business.helper.CustomChatController;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatHistoryMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzDeleteConversationListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupChangeListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupInfoListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupJoinListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupMemberListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageSendCallback;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.inter.YzReceiveMessageOptListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzSearchMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzStatusListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzWorkAppItemClickListener;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.message.MessageNotification;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.business.modal.SearchParam;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.business.modal.VideoFile;
import com.hlife.qcloud.tim.uikit.business.thirdpush.HUAWEIHmsMessageService;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.config.GeneralConfig;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.hlife.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.utils.BrandUtil;
import com.hlife.qcloud.tim.uikit.utils.SearchDataUtils;
import com.http.network.task.ObjectMapperFactory;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMGroupApplication;
import com.tencent.imsdk.v2.V2TIMGroupApplicationResult;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageSearchParam;
import com.tencent.imsdk.v2.V2TIMMessageSearchResult;
import com.tencent.imsdk.v2.V2TIMMessageSearchResultItem;
import com.tencent.imsdk.v2.V2TIMReceiveMessageOptInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.work.api.open.ApiClient;
import com.work.api.open.Yz;
import com.work.api.open.model.LoginResp;
import com.work.api.open.model.SysUserReq;
import com.work.api.open.model.client.OpenData;
import com.work.util.AppUtils;
import com.work.util.SLog;
import com.work.util.SharedUtils;
import com.work.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tangyx
 * Date 2020/8/15
 * email tangyx@live.com
 */

public final class YzIMKitAgent {

    private final IMEventListener IMEventPushListener = new IMEventListener() {
        @Override
        public void onNewMessage(V2TIMMessage msg) {
            MessageNotification notification = MessageNotification.getInstance();
            notification.notify(msg);
        }
    };

    private final YzMessageWatcher UnreadWatcher = new YzMessageWatcher() {
        @Override
        public void updateUnread(int count) {
            // 华为离线推送角标
            HUAWEIHmsMessageService.updateBadge(mContext, count);
        }

        @Override
        public void updateContacts() {

        }

        @Override
        public void updateConversion() {

        }

        @Override
        public void updateJoinGroup() {

        }
    };
    private static YzIMKitAgent singleton;
    private final Context mContext;
    private YzStatusListener mIMKitStatusListener;
    private YzWorkAppItemClickListener mWorkAppItemClickListener;
    private int functionPrem;
    private static boolean isDev;

    private YzIMKitAgent(Context context, String mYzAppId) {
        this.mContext = context;
        loadConfig();
        SharedUtils.putData("YzAppId",mYzAppId);
        UserApi userApi = UserApi.instance();
        userApi.setStore("im sdk");
        //配置网络相关
        ApiClient.setApiConfig(new ApiClient.ApiConfig().setHostName(isDev?"https://dev1-imapi.yzmetax.com/":"https://imapi.yzmetax.com/").setParamObj(userApi));
    }

    public static void init(Context context,String appId){
        init(context,appId,false);
    }

    public static void init(Context context,String appId,boolean dev){
        isDev = dev;
        if(singleton==null){
            singleton = new YzIMKitAgent(context,appId);
        }
    }

    public static YzIMKitAgent instance(){
        return singleton;
    }

    public void addStatusListener(YzStatusListener listener){
        this.mIMKitStatusListener = listener;
    }

    public void addWorkAppItemClickListener(YzWorkAppItemClickListener listener){
        this.mWorkAppItemClickListener = listener;
    }

    public YzWorkAppItemClickListener getWorkAppItemClickListener(){
        return this.mWorkAppItemClickListener;
    }

    private void loadConfig(){
        SharedUtils.init(mContext);
        //初始化im
        TUIKit.init(mContext,isDev?1400433756:1400432221,getConfigs());
        //加载腾讯x5的浏览器引擎
        HashMap<String,Object> map = new HashMap<>();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setDownloadWithoutWifi(true);
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {

            }

            @Override
            public void onViewInitFinished(boolean b) {
                if(SLog.debug) SLog.e("x5 web init:"+b);
            }
        };
        QbSdk.initX5Environment(mContext,cb);
        //注册消息通知离线登录
        registerPush();
        TUIKitListenerManager.getInstance().addChatListener(new CustomChatController());
    };

    public int getFunctionPrem() {
        return functionPrem;
    }

    private TUIKitConfigs getConfigs() {
        GeneralConfig config = new GeneralConfig();
        // 显示对方是否已读的view将会展示
        config.setShowRead(true);
        config.setAppCacheDir(mContext.getFilesDir().getPath());
        if (new File(Environment.getExternalStorageDirectory() + "/111222333").exists()) {
            config.setTestEnv(true);
        }
        TUIKit.getConfigs().setGeneralConfig(config);
//        TUIKit.getConfigs().setCustomFaceConfig(initCustomFaceConfig());
        return TUIKit.getConfigs();
    }
    public void addIMEventListener(IMEventListener eventListener){
        TUIKit.addIMEventListener(eventListener);
    }

    public void register(final SysUserReq userReq, final YzStatusListener listener) {
        Yz.getSession().sysUser(userReq, (req, resp) -> {
            if(resp instanceof LoginResp){
                if(resp.isSuccess()){
                    OpenData data = ((LoginResp) resp).getData();
                    String userId = data.getUserId();
                    String userSign = data.getUserSign();
                    String token = ((LoginResp) resp).getToken();
                    UserApi userApi = UserApi.instance();
                    userApi.setUserId(userId);
                    userApi.setUserSign(userSign);
                    userApi.setNickName(userReq.getNickName());
                    userApi.setUserIcon(userReq.getUserIcon());
                    userApi.setMobile(userReq.getMobile());
                    userApi.setPosition(userReq.getPosition());
                    userApi.setDepartmentId(userReq.getDepartmentId());
                    userApi.setDepartName(userReq.getDepartName());
                    userApi.setCard(userReq.getCard());
                    userApi.setEmail(userReq.getEmail());
                    userApi.setToken(token);
                    userApi.setCity(userReq.getCity());
                    userApi.setGender(userReq.getGender());
                    userApi.setUserSignature(userReq.getUserSignature());
                    loginIM(listener);
                    functionPrem = data.getFunctionPerm();
                }else{
                    if(listener!=null){
                        listener.loginFail("sysUser",((LoginResp) resp).getCode(),resp.getMessage());
                    }
                }
            }
        });
    }
    private void loginIM(final YzStatusListener listener){
        TUIKitImpl.login(UserApi.instance().getUserId(), UserApi.instance().getUserSign(), new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if(listener!=null){
                    listener.loginSuccess(data);
                }
                IMFriendManager.getInstance().setup();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if(listener!=null){
                    listener.loginFail(module,errCode,errMsg);
                }
            }
        });
    }
    /**
     * 启动im
     */
    public void startAuto(){
        Intent intent = new Intent(mContext, MwWorkActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
    public void startChat(ChatInfo chatInfo, ChatViewConfig config){
        if((getFunctionPrem() & 1)<=0){
            ToastUtil.error(mContext, R.string.toast_conversation_permission);
            return;
        }
        Intent intent = new Intent(TUIKit.getAppContext(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_INFO, chatInfo);
        intent.putExtra(Constants.CHAT_CONFIG,config);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TUIKit.getAppContext().startActivity(intent);
    }
    public void sendCustomMessage(final ChatInfo chatInfo, String customMessage,YzMessageSendCallback callback){
        this.sendCustomMessage(chatInfo,customMessage,customMessage,callback);
    }
    public void sendCustomMessage(final ChatInfo chatInfo, String customMessage,String desc,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildCustomMessage(customMessage,desc);
        sendMessage(chatInfo,info,callback);
    }
    public void sendCustomMessage(String customMessage, YzMessageSendCallback callback){
        C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
        if(c2CChatManagerKit.getCurrentChatInfo()!=null){
            this.sendCustomMessage(c2CChatManagerKit.getCurrentChatInfo(),customMessage,callback);
        }else{
            GroupChatManagerKit groupChatManagerKit = GroupChatManagerKit.getInstance();
            if(groupChatManagerKit.getCurrentChatInfo()!=null){
                this.sendCustomMessage(groupChatManagerKit.getCurrentChatInfo(),customMessage,callback);
            }
        }
    }
    /**
     * 文本消息
     */
    public void sendTextMessage(ChatInfo chatInfo,String message,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildTextMessage(message);
        sendMessage(chatInfo,info,callback);
    }
    /**
     * 图片消息
     */
    public void sendImageMessage(ChatInfo chatInfo,final Uri uri,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildImageMessage(uri,true);
        sendMessage(chatInfo,info,callback);
    }
    /**
     * 视频消息
     */
    public void sendVideoMessage(ChatInfo chatInfo,String imgPath, String videoPath, int width, int height, long duration,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildVideoMessage(imgPath,videoPath,width,height,duration);
        sendMessage(chatInfo,info,callback);
    }
    /**
     * 语音消息
     */
    public void sendAudioMessage(ChatInfo chatInfo,String recordPath, int duration,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildAudioMessage(recordPath,duration);
        sendMessage(chatInfo,info,callback);
    }
    /**
     * 发送文件
     */
    public void sendFile(ChatInfo chatInfo,Uri uri,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        OSSFileActivity.uploadFileSDK(uri, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if(data instanceof Uri){
                    sendImageMessage(chatInfo,(Uri) data,callback);
                }else if(data instanceof VideoFile){
                    VideoFile videoFile = (VideoFile) data;
                    sendVideoMessage(chatInfo,videoFile.imagePath,videoFile.filePath,videoFile.firstFrame.getWidth(),videoFile.firstFrame.getHeight(),videoFile.duration,callback);
                }else if(data instanceof CustomFileMessage){
                    String custom = ObjectMapperFactory.getObjectMapper().model2JsonStr(data);
                    sendCustomMessage(chatInfo,custom,callback);
                }
            }
            @Override
            public void onError(String module, int errCode, String errMsg) {
                if(callback!=null){
                    callback.error(errCode,errMsg);
                }
            }
        });
    }
    /**
     * 定位
     */
    public void sendLocationMessage(ChatInfo chatInfo,String data,double longitude,double latitude,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildLocationMessage(data,longitude,latitude);
        sendMessage(chatInfo,info,callback);
    }
    private void sendMessage(ChatInfo chatInfo,MessageInfo info,YzMessageSendCallback callback){
        IUIKitCallBack iuiKitCallBack = new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if(callback!=null){
                    callback.success();
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                if(callback!=null){
                    callback.error(errCode,errMsg);
                }
            }
        };

        if(chatInfo.isGroup()){
            GroupChatManagerKit groupChatManagerKit = GroupChatManagerKit.getInstance();
            if(groupChatManagerKit.getCurrentChatInfo()!=null && groupChatManagerKit.getCurrentChatInfo().getId().equals(chatInfo.getId())){
                groupChatManagerKit.sendMessage(info, false, iuiKitCallBack);
            }else{
                GroupInfo groupInfo = new GroupInfo();
                groupInfo.setId(chatInfo.getId());
                groupInfo.setGroupName(chatInfo.getChatName());
                groupChatManagerKit.setCurrentChatInfo(groupInfo);
                GroupChatManagerKit.getInstance().sendMessageId(info,false,iuiKitCallBack);
            }
        }else{
            C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
            if(c2CChatManagerKit.getCurrentChatInfo()!=null && c2CChatManagerKit.getCurrentChatInfo().getId().equals(chatInfo.getId())){
                c2CChatManagerKit.sendMessage(info, false,iuiKitCallBack);
            }else{
                c2CChatManagerKit.setCurrentChatInfo(chatInfo);
                C2CChatManagerKit.getInstance().sendMessageId(info,false,iuiKitCallBack);
            }
        }
    }
    /**
     * 搜索本地消息
     */
    public void searchMessage(SearchParam searchParam, YzSearchMessageListener listener){
        final V2TIMMessageSearchParam param = new V2TIMMessageSearchParam();
        List<String> keywords = new ArrayList<>();
        keywords.add(searchParam.getKeyword());
        param.setPageSize(searchParam.getPageSize());
        param.setPageIndex(searchParam.getPageIndex());
        param.setKeywordList(keywords);
        if(!TextUtils.isEmpty(searchParam.getConversationId())){
            param.setConversationID(searchParam.getConversationId());
        }
        V2TIMManager.getMessageManager().searchLocalMessages(param, new V2TIMValueCallback<V2TIMMessageSearchResult>() {
            @Override
            public void onSuccess(V2TIMMessageSearchResult v2TIMMessageSearchResult) {
                List<SearchDataMessage> dataMessages = new ArrayList<>();
                if (v2TIMMessageSearchResult == null || v2TIMMessageSearchResult.getTotalCount() == 0 ||
                        v2TIMMessageSearchResult.getMessageSearchResultItems() == null ||
                        v2TIMMessageSearchResult.getMessageSearchResultItems().size() == 0) {
                    if(listener!=null){
                        listener.success(dataMessages);
                    }
                    return;
                }
                if(TextUtils.isEmpty(searchParam.getConversationId())){
                    final Map<String, V2TIMMessageSearchResultItem> mMsgsCountInConversationMap = new HashMap<>();
                    List<V2TIMMessageSearchResultItem> v2TIMMessageSearchResultItems = v2TIMMessageSearchResult.getMessageSearchResultItems();
                    List<String> conversationIDList = new ArrayList<>();
                    for(V2TIMMessageSearchResultItem v2TIMMessageSearchResultItem : v2TIMMessageSearchResultItems) {
                        conversationIDList.add(v2TIMMessageSearchResultItem.getConversationID());
                        mMsgsCountInConversationMap.put(v2TIMMessageSearchResultItem.getConversationID(), v2TIMMessageSearchResultItem);
                    }
                    V2TIMManager.getConversationManager().getConversationList(conversationIDList, new V2TIMValueCallback<List<V2TIMConversation>>() {
                        @Override
                        public void onSuccess(List<V2TIMConversation> v2TIMConversationList) {
                            if (v2TIMConversationList == null || v2TIMConversationList.size() == 0){
                                if(listener!=null){
                                    listener.success(dataMessages);
                                }
                                return;
                            }

                            for (V2TIMConversation v2TIMConversation : v2TIMConversationList) {
                                SearchDataMessage searchDataMessage = ConversationManagerKit.getInstance().TIMConversation2ConversationInfo(v2TIMConversation);
                                dataMessages.add(searchDataMessage);
                            }
                            if (dataMessages.size() > 0) {
                                for (int i = 0; i < dataMessages.size(); i++) {
                                    V2TIMMessageSearchResultItem v2TIMMessageSearchResultItem = mMsgsCountInConversationMap.get(dataMessages.get(i).getConversationId());
                                    if (v2TIMMessageSearchResultItem != null) {
                                        int count = v2TIMMessageSearchResultItem.getMessageCount();
                                        SearchDataMessage searchDataMessage = dataMessages.get(i);
                                        searchDataMessage.setCount(count);
                                        if (count == 1) {
                                            searchDataMessage.setSubTitle(SearchDataUtils.getMessageText(v2TIMMessageSearchResultItem.getMessageList().get(0)));
                                            searchDataMessage.setSubTextMatch(1);
                                            searchDataMessage.setLocateTimMessage(v2TIMMessageSearchResultItem.getMessageList().get(0));
                                        } else if (count > 1) {
                                            searchDataMessage.setSubTitle(count + TUIKit.getAppContext().getString(R.string.chat_records));
                                            searchDataMessage.setSubTextMatch(0);
                                        }
                                    }
                                }
                            }
                            if(listener!=null){
                                listener.success(dataMessages);
                            }
                        }

                        @Override
                        public void onError(int code, String desc) {
                            if(listener!=null){
                                listener.success(dataMessages);
                            }
                        }
                    });
                }
                else{
                    List<V2TIMMessage> v2TIMMessages = v2TIMMessageSearchResult.getMessageSearchResultItems().get(0).getMessageList();
                    if (v2TIMMessages != null && !v2TIMMessages.isEmpty()) {
                        for (V2TIMMessage v2TIMMessage:v2TIMMessages) {
                            SearchDataMessage searchDataMessage = new SearchDataMessage();
                            searchDataMessage.setConversationId(searchParam.getConversationId());
                            String title;
                            if (!TextUtils.isEmpty(v2TIMMessage.getFriendRemark())) {
                                title = v2TIMMessage.getFriendRemark();
                            } else if (!TextUtils.isEmpty(v2TIMMessage.getNameCard())) {
                                title = v2TIMMessage.getNameCard();
                            } else if (!TextUtils.isEmpty(v2TIMMessage.getNickName())) {
                                title = v2TIMMessage.getNickName();
                            } else {
                                title = v2TIMMessage.getUserID()== null ? v2TIMMessage.getGroupID() : v2TIMMessage.getUserID();
                            }
                            if(v2TIMMessage.getSender().equals(UserApi.instance().getUserId())){//是自己
                                searchDataMessage.setTitle(UserApi.instance().getNickName());
                                searchDataMessage.setIconUrlList(new ArrayList<Object>(){{
                                    add(UserApi.instance().getUserIcon());
                                }});
                            }else{
                                searchDataMessage.setTitle(title);
                                searchDataMessage.setIconUrlList(new ArrayList<Object>(){{
                                    add(v2TIMMessage.getFaceUrl());
                                }});
                            }
                            searchDataMessage.setId(v2TIMMessage.getSender());
                            String subTitle = SearchDataUtils.getMessageText(v2TIMMessage);
                            searchDataMessage.setSubTitle(subTitle);
                            searchDataMessage.setLocateTimMessage(v2TIMMessage);
                            dataMessages.add(searchDataMessage);
                        }
                    }
                    if(listener!=null){
                        listener.success(dataMessages);
                    }
                }
            }

            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }
        });
    }
    /**
     * 获取所有的聊天会话
     */
    public void loadConversation(int nextSeq, YzChatType type, final YzConversationDataListener callBack){
        ConversationManagerKit.getInstance().loadConversation(nextSeq,type,callBack);
    }
    public void getConversation(String id, YzConversationDataListener listener){
        ConversationManagerKit.getInstance().getConversation(id,listener);
    }
    public void conversationUnRead(YzConversationDataListener listener){
        ConversationManagerKit.getInstance().conversationUnRead(listener);
    }
    public void addMessageWatcher(YzMessageWatcher watcher){
        ConversationManagerKit.getInstance().addMessageWatcher(watcher);
    }
    public void removeMessageWatcher(YzMessageWatcher watcher){
        ConversationManagerKit.getInstance().removeMessageWatcher(watcher);
    }
    public void deleteConversation(String id, YzDeleteConversationListener listener){
        if(TextUtils.isEmpty(id)){
            if(listener!=null){
                listener.error(-1,"ID cannot be empty");
            }
            return;
        }
        ConversationManagerKit.getInstance().deleteConversation(id,listener);
    }
    /**
     * 获取历史会话记录
     */
    public void getHistoryMessage(ChatInfo chatInfo,int pageSize,MessageInfo lastMessageInfo, YzChatHistoryMessageListener listener){
        V2TIMMessage v2TIMMessage = null;
        if(lastMessageInfo!=null){
            v2TIMMessage = lastMessageInfo.getTimMessage();
        }
        if(chatInfo.isGroup()){
            V2TIMManager.getMessageManager().getGroupHistoryMessageList(chatInfo.getId(), pageSize, v2TIMMessage, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int code, String desc) {
                    if(listener!=null){
                        listener.onError(code,desc);
                    }
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    processHistoryMsgs(v2TIMMessages, chatInfo, listener);
                }
            });
        }
        else{
            V2TIMManager.getMessageManager().getC2CHistoryMessageList(chatInfo.getId(), pageSize, v2TIMMessage, new V2TIMValueCallback<List<V2TIMMessage>>() {
                @Override
                public void onError(int code, String desc) {
                    if(listener!=null){
                        listener.onError(code,desc);
                    }
                }

                @Override
                public void onSuccess(List<V2TIMMessage> v2TIMMessages) {
                    processHistoryMsgs(v2TIMMessages, chatInfo, listener);
                }
            });
        }
    }
    private void processHistoryMsgs(List<V2TIMMessage> v2TIMMessages, ChatInfo chatInfo, YzChatHistoryMessageListener listener) {
        ArrayList<V2TIMMessage> messages = new ArrayList<>(v2TIMMessages);
        Collections.reverse(messages);
        List<MessageInfo> msgInfos = MessageInfoUtil.TIMMessages2MessageInfos(v2TIMMessages, chatInfo.isGroup());
        if(listener!=null){
            listener.onChatMessageHistory(msgInfos);
        }
    }
    /**
     * 申请加群
     */
    public void joinGroup(String groupId, String message, YzGroupJoinListener listener){
        V2TIMManager.getInstance().joinGroup(groupId, message, new V2TIMCallback() {
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
    }
    public void groupApplicationList(YzGroupDataListener listener){
        V2TIMManager.getGroupManager().getGroupApplicationList(new V2TIMValueCallback<V2TIMGroupApplicationResult>() {
            @Override
            public void onError(int code, String desc) {
                if(listener==null || code==6015){//频繁调用
                    return;
                }
                listener.joinMember(new ArrayList<>());
            }

            @Override
            public void onSuccess(V2TIMGroupApplicationResult v2TIMGroupApplicationResult) {
                if(listener==null){
                    return;
                }
                List<V2TIMGroupApplication> v2TIMGroupApplications = v2TIMGroupApplicationResult.getGroupApplicationList();
                List<GroupApplyInfo> applies = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupApplications.size(); i++) {
                    GroupApplyInfo info = new GroupApplyInfo(v2TIMGroupApplications.get(i));
                    info.setStatus(0);
                    applies.add(info);
                }
                listener.joinMember(applies);
            }
        });
    }
    /**
     * 修改群头像
     */
    public void changeGroupFaceUrl(String groupId, String url, YzGroupChangeListener listener){
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(groupId);
        v2TIMGroupInfo.setFaceUrl(url);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
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
    }
    /**
     * 修改群名字
     */
    public void changeGroupName(String groupId, String name, YzGroupChangeListener listener){
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(groupId);
        v2TIMGroupInfo.setGroupName(name);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
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
    }
    /**
     * 全体是否禁言
     */
    public void changeGroupMuted(String groupId,boolean muted,YzGroupChangeListener listener){
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(groupId);
        v2TIMGroupInfo.setAllMuted(muted);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
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
    }
    /**
     * 是否消息免打扰
     */
    public void changeReceiveMessageOpt(String groupId,boolean opt,YzGroupChangeListener listener){
        V2TIMManager.getMessageManager().setGroupReceiveMessageOpt(groupId, opt ? V2TIMMessage.V2TIM_RECEIVE_NOT_NOTIFY_MESSAGE : V2TIMMessage.V2TIM_RECEIVE_MESSAGE, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                if(listener!=null){
                    listener.success();
                }
            }

            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }
        });
    }
    public void changeC2CReceiveMessageOpt(List<String> ids,boolean opt,YzGroupChangeListener listener){
        V2TIMManager.getMessageManager().setC2CReceiveMessageOpt(ids, opt ? V2TIMMessage.V2TIM_RECEIVE_NOT_NOTIFY_MESSAGE : V2TIMMessage.V2TIM_RECEIVE_MESSAGE, new V2TIMCallback() {
            @Override
            public void onSuccess() {
                if(listener!=null){
                    listener.success();
                }
            }

            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }
        });
    }
    public void getC2CReceiveMessageOpt(List<String> ids, YzReceiveMessageOptListener listener){
        V2TIMManager.getMessageManager().getC2CReceiveMessageOpt(ids, new V2TIMValueCallback<List<V2TIMReceiveMessageOptInfo>>() {
            @Override
            public void onSuccess(List<V2TIMReceiveMessageOptInfo> v2TIMReceiveMessageOptInfos) {
                HashMap<String,Boolean> result = new HashMap<>();
                for (V2TIMReceiveMessageOptInfo v2TIMReceiveMessageOptInfo:v2TIMReceiveMessageOptInfos) {
                    result.put(v2TIMReceiveMessageOptInfo.getUserID(),v2TIMReceiveMessageOptInfo.getC2CReceiveMessageOpt() == V2TIMMessage.V2TIM_RECEIVE_NOT_NOTIFY_MESSAGE);
                }
                if(listener!=null){
                    listener.result(result);
                }
            }

            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }
        });
    }
    /**
     * 修改群公告
     */
    public void changeGroupNotice(String groupId,String content,YzGroupChangeListener listener){
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(groupId);
        v2TIMGroupInfo.setNotification(content);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
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
    }
    /**
     * 修改群简介
     */
    public void changeGroupIntroduction(String groupId,String content,YzGroupChangeListener listener){
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(groupId);
        v2TIMGroupInfo.setIntroduction(content);
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
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
    }
    /**
     * 设置管理员
     */
    public void changeGroupMemberRole(String groupId,String userid,boolean isAdd,YzGroupChangeListener listener){
        V2TIMManager.getGroupManager().setGroupMemberRole(groupId, userid,isAdd?V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN : V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER, new V2TIMCallback() {
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
    }
    /**
     * 获取成员
     * V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ADMIN
     * V2TIM_GROUP_MEMBER_FILTER_COMMON
     * V2TIM_GROUP_MEMBER_FILTER_ALL
     */
    public void groupMember(String groupId, int filter, long nextSeq, YzGroupMemberListener listener){
        V2TIMManager.getGroupManager().getGroupMemberList(groupId, filter, nextSeq, new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }

            @Override
            public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                List<GroupMemberInfo> members = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupMemberInfoResult.getMemberInfoList().size(); i++) {
                    GroupMemberInfo member = new GroupMemberInfo();
                    members.add(member.covertTIMGroupMemberInfo(v2TIMGroupMemberInfoResult.getMemberInfoList().get(i)));
                }
                if(listener!=null){
                    listener.groupMember(members);
                }
            }
        });
    }
    /**
     * 获取群资料
     */
    public void getGroupInfo(List<String> groupList, YzGroupInfoListener listener){
        V2TIMManager.getGroupManager().getGroupsInfo(groupList, new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
            @Override
            public void onError(int code, String desc) {
                if(listener!=null){
                    listener.error(code,desc);
                }
            }

            @Override
            public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                List<GroupInfo> groupInfos = new ArrayList<>();
                for (V2TIMGroupInfoResult v2TIMGroupInfoResult:v2TIMGroupInfoResults) {
                    GroupInfo groupInfo = new GroupInfo();
                    groupInfo.covertTIMGroupDetailInfo(v2TIMGroupInfoResult);
                    groupInfos.add(groupInfo);
                }
                if(listener!=null){
                    listener.success(groupInfos);
                }
            }
        });
    }
    /**
     * 注册推送
     */
    private void registerPush(){
        if (BrandUtil.isBrandXiaoMi()) {
            // 小米离线推送
//            MiPushClient.registerPush(mContext, PrivateConstants.XM_PUSH_APPID, PrivateConstants.XM_PUSH_APPKEY);
        } else if (BrandUtil.isBrandHuawei()) {
            // 华为离线推送，设置是否接收Push通知栏消息调用示例
//            HmsMessaging.getInstance(mContext).turnOnPush().addOnCompleteListener(new com.huawei.hmf.tasks.OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(Task<Void> task) {
//                    if (task.isSuccessful()) {
//                        SLog.i( "huawei turnOnPush Complete");
//                    } else {
//                        SLog.e("huawei turnOnPush failed: ret=" + task.getException().getMessage());
//                    }
//                }
//            });
        } else if (BrandUtil.isBrandVivo()) {
            // vivo离线推送
//            PushClient.getInstance(mContext.getApplicationContext()).initialize();
        }
    }

    public void onActivityStarted(){
        V2TIMManager.getOfflinePushManager().doForeground(new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("doForeground err = " + code + ", desc = " + desc);
            }

            @Override
            public void onSuccess() {
                SLog.i( "doForeground success");
            }
        });
        removeIMEventListener(IMEventPushListener);
        ConversationManagerKit.getInstance().removeMessageWatcher(UnreadWatcher);
        MessageNotification.getInstance().cancelTimeout();
    }

    public boolean parseOfflineMessage(Intent intent){
//        OfflineMessageBean bean = OfflineMessageDispatcher.parseOfflineMessage(intent);
//        if (bean != null) {
//            OfflineMessageDispatcher.redirect(bean);
//            return true;
//        }
        return false;
    }

    public void onActivityStopped(){
        if(AppUtils.isAppBackground(mContext)){
            int unReadCount = ConversationManagerKit.getInstance().getUnreadTotal();
            V2TIMManager.getOfflinePushManager().doBackground(unReadCount, new V2TIMCallback() {
                @Override
                public void onError(int code, String desc) {
                    SLog.e("doBackground err = " + code + ", desc = " + desc);
                }

                @Override
                public void onSuccess() {
                    SLog.i( "doBackground success");
                }
            });
            // 应用退到后台，消息转化为系统通知
            addIMEventListener(IMEventPushListener);
            ConversationManagerKit.getInstance().addMessageWatcher(UnreadWatcher);
        }
    }

    private void removeIMEventListener(IMEventListener listener) {
        TUIKitImpl.removeIMEventListener(listener);
    }

    public void logout() {
        TUIKitImpl.logout(new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
        UserApi.instance().clear();
        unInit();
    }

    /**
     * 释放一些资源等，一般可以在退出登录时调用
     */
    public void unInit() {
        TUIKitImpl.unInit();
        IMFriendManager.getInstance().clear();
        if(mIMKitStatusListener!=null){
            mIMKitStatusListener.logout();
        }
    }

    public void updateUserRemark(String userId, String remark, YzUpdateFriendCallback callback) {
        IMFriendManager.getInstance().updateFriendRemark(userId, remark, callback);
    }
}
