package com.hlife.qcloud.tim.uikit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.hlife.qcloud.tim.uikit.base.IMEventListener;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.business.active.MwWorkActivity;
import com.hlife.qcloud.tim.uikit.business.active.OSSFileActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatHistoryMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupChangeListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupJoinListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupMemberListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageSendCallback;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.inter.YzStatusListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzWorkAppItemClickListener;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.message.MessageNotification;
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
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.http.network.task.ObjectMapperFactory;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMGroupApplication;
import com.tencent.imsdk.v2.V2TIMGroupApplicationResult;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
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
        Yz.getSession().sysUser(userReq, new OnResultDataListener() {
            @Override
            public void onResult(RequestWork req, ResponseWork resp) {
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
        if(chatInfo==null){
            return;
        }
        if(chatInfo.isGroup()){
            MessageInfo info = MessageInfoUtil.buildCustomMessage(customMessage);
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }else{
            MessageInfo info = MessageInfoUtil.buildCustomMessage(customMessage);
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
    }
    public void sendCustomMessage(String customMessage, YzMessageSendCallback callback){
        MessageInfo info = MessageInfoUtil.buildCustomMessage(customMessage);
        C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
        if(c2CChatManagerKit.getCurrentChatInfo()!=null){
            c2CChatManagerKit.sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }else{
            GroupChatManagerKit groupChatManagerKit = GroupChatManagerKit.getInstance();
            if(groupChatManagerKit.getCurrentChatInfo()!=null){
                groupChatManagerKit.sendMessage(info,false,new IUIKitCallBack() {
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
                });
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
        if(chatInfo.isGroup()){
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
        else{
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
    }
    /**
     * 图片消息
     */
    public void sendImageMessage(ChatInfo chatInfo,final Uri uri,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildImageMessage(uri,true);
        if(chatInfo.isGroup()){
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
        else{
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
    }
    /**
     * 视频消息
     */
    public void sendVideoMessage(ChatInfo chatInfo,String imgPath, String videoPath, int width, int height, long duration,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildVideoMessage(imgPath,videoPath,width,height,duration);
        if(chatInfo.isGroup()){
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
        else{
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
    }
    /**
     * 语音消息
     */
    public void sendAudioMessage(ChatInfo chatInfo,String recordPath, int duration,YzMessageSendCallback callback){
        if(chatInfo==null){
            return;
        }
        MessageInfo info = MessageInfoUtil.buildAudioMessage(recordPath,duration);
        if(chatInfo.isGroup()){
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
        else{
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
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
        if(chatInfo.isGroup()){
            groupChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
        else{
            c2CChatManagerKit(chatInfo).sendMessage(info, false, new IUIKitCallBack() {
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
            });
        }
    }
    /**
     * 封装发送体
     */
    private GroupChatManagerKit groupChatManagerKit(ChatInfo chatInfo){
        GroupChatManagerKit groupChatManagerKit = GroupChatManagerKit.getInstance();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(chatInfo.getId());
        groupInfo.setGroupName(chatInfo.getChatName());
        groupChatManagerKit.setCurrentChatInfo(groupInfo);
        return groupChatManagerKit;
    }
    private C2CChatManagerKit c2CChatManagerKit(ChatInfo chatInfo){
        C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
        c2CChatManagerKit.setCurrentChatInfo(chatInfo);
        return c2CChatManagerKit;
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
     * 获取群申请信息
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
                if(listener==null){
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
    public void setGroupFaceUrl(String groupId, String url, YzGroupChangeListener listener){
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
    public void setGroupName(String groupId, String name, YzGroupChangeListener listener){
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
    public void mutedGroup(String groupId,boolean muted,YzGroupChangeListener listener){
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
    public void receiveMessageOpt(String groupId,boolean opt,YzGroupChangeListener listener){
        V2TIMManager.getGroupManager().setReceiveMessageOpt(groupId, opt ? V2TIMGroupInfo.V2TIM_GROUP_NOT_RECEIVE_MESSAGE : V2TIMGroupInfo.V2TIM_GROUP_RECEIVE_MESSAGE, new V2TIMCallback() {
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
     * 修改群公告
     */
    public void setGroupNotice(String groupId,String content,YzGroupChangeListener listener){
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
     * 设置管理员
     */
    public void setGroupMemberRole(String groupId,String userid,boolean isAdd,YzGroupChangeListener listener){
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
        if(mIMKitStatusListener!=null){
            mIMKitStatusListener.logout();
        }
    }
}
