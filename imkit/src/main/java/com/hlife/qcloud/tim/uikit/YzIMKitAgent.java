package com.hlife.qcloud.tim.uikit;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.base.IMEventListener;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.business.active.MwWorkActivity;
import com.hlife.qcloud.tim.uikit.business.active.SelectMessageActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.inter.YzStatusListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzWorkAppItemClickListener;
import com.hlife.qcloud.tim.uikit.business.message.CustomMessage;
import com.hlife.qcloud.tim.uikit.business.message.MessageNotification;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.business.thirdpush.HUAWEIHmsMessageService;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.config.GeneralConfig;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.hlife.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.utils.BrandUtil;
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
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
import java.util.HashMap;

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
    };
    private static YzIMKitAgent singleton;
    private final Context mContext;
    private YzStatusListener mIMKitStatusListener;
    private YzWorkAppItemClickListener mWorkAppItemClickListener;
    private int functionPrem;

    private YzIMKitAgent(Context context, String mYzAppId) {
        this.mContext = context;
        loadConfig();
        SharedUtils.putData("YzAppId",mYzAppId);
        UserApi userApi = UserApi.instance();
        userApi.setStore("im sdk");
        //配置网络相关
        ApiClient.setApiConfig(new ApiClient.ApiConfig().setHostName("https://dev-imapi.yzmetax.com/").setParamObj(userApi));
    }

    public static void init(Context context,String appId){
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
        //umeng统计
        UMConfigure.init(mContext, "5f8d583980455950e4af10d9", "Yz", UMConfigure.DEVICE_TYPE_PHONE, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        //初始化im
        TUIKit.init(mContext,1400432221,getConfigs());
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
    /**
     * 分享卡片消息
     */
    public void startCustomMessage(String customMessage){
        startCustomMessage(null,customMessage);
    }
    public void startCustomMessage(final ChatInfo chatInfo, String customMessage){
        if(chatInfo==null){
            SelectMessageActivity.sendCustomMessage(mContext,customMessage);
        }else{
            C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
            c2CChatManagerKit.setCurrentChatInfo(chatInfo);
            MessageInfo info = MessageInfoUtil.buildCustomMessage(customMessage);
            c2CChatManagerKit.sendMessage(info, false, new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    SLog.e("custom message send success:"+data);
                    YzIMKitAgent.instance().startChat(chatInfo,null);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    ToastUtil.warning(mContext,errCode+">"+errMsg);
                }
            });
        }
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
