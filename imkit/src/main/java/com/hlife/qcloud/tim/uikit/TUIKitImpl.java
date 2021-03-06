package com.hlife.qcloud.tim.uikit;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.base.GroupListenerConstants;
import com.hlife.qcloud.tim.uikit.base.IMEventListener;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.config.GeneralConfig;
import com.hlife.qcloud.tim.uikit.config.TUIKitConfigs;
import com.hlife.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.message.MessageRevokedManager;
import com.hlife.qcloud.tim.uikit.utils.BackgroundTasks;
import com.hlife.qcloud.tim.uikit.utils.FileUtil;
import com.hlife.qcloud.tim.uikit.utils.NetWorkUtils;
import com.tencent.imsdk.v2.V2TIMAdvancedMsgListener;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMConversationListener;
import com.tencent.imsdk.v2.V2TIMFriendApplication;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMFriendshipListener;
import com.tencent.imsdk.v2.V2TIMGroupChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupListener;
import com.tencent.imsdk.v2.V2TIMGroupMemberChangeInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMMessageReceipt;
import com.tencent.imsdk.v2.V2TIMSDKConfig;
import com.tencent.imsdk.v2.V2TIMSDKListener;
import com.hlife.liteav.AVCallManager;
import com.hlife.liteav.login.ProfileManager;
import com.hlife.liteav.login.UserModel;
import com.hlife.qcloud.tim.uikit.component.face.FaceManager;
import com.work.util.SLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TUIKitImpl {

    private static Context sAppContext;
    private static TUIKitConfigs sConfigs;
    private static List<IMEventListener> sIMEventListeners = new ArrayList<>();
    private static Gson sGson;

    /**
     * TUIKit??????????????????
     *
     * @param context  ?????????????????????????????????????????????ApplicationContext
     * @param sdkAppID ???????????????????????????????????????sdkAppID
     * @param configs  TUIKit?????????????????????????????????????????????????????????????????????API??????
     */
    public static void init(Context context, int sdkAppID, TUIKitConfigs configs) {
        sAppContext = context;
        sConfigs = configs;
        if (sConfigs.getGeneralConfig() == null) {
            GeneralConfig generalConfig = new GeneralConfig();
            sConfigs.setGeneralConfig(generalConfig);
        }
        sConfigs.getGeneralConfig().setSDKAppId(sdkAppID);
        String dir = sConfigs.getGeneralConfig().getAppCacheDir();
        if (TextUtils.isEmpty(dir)) {
            sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
        } else {
            File file = new File(dir);
            if (file.exists()) {
                if (file.isFile()) {
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                } else if (!file.canWrite()) {
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                }
            } else {
                boolean ret = file.mkdirs();
                if (!ret) {
                    sConfigs.getGeneralConfig().setAppCacheDir(context.getFilesDir().getPath());
                }
            }
        }
        initIM(context, sdkAppID);

        BackgroundTasks.initInstance();
        FileUtil.initPath(); // ?????????app???????????????????????????????????????application??????????????????????????????????????????????????????????????????app??????activity??????????????????????????????????????????????????????
        FaceManager.loadFaceFiles();
    }

    public static void login(final String userid, final String usersig, final IUIKitCallBack callback) {
        TUIKitConfigs.getConfigs().getGeneralConfig().setUserId(userid);
        TUIKitConfigs.getConfigs().getGeneralConfig().setUserSig(usersig);
        V2TIMManager.getInstance().login(userid, usersig, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                callback.onError("SLog", code, desc);
            }

            @Override
            public void onSuccess() {
                if (TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
                    UserModel self = new UserModel();
                    self.userId = userid;
                    self.userSig = usersig;
                    ProfileManager.getInstance().setUserModel(self);
                    AVCallManager.getInstance().init(sAppContext);
                }
                callback.onSuccess(null);
            }
        });
    }

    public static void logout(final IUIKitCallBack callback) {
        V2TIMManager.getInstance().logout(new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                callback.onError("SLog", code, desc);
            }

            @Override
            public void onSuccess() {
                callback.onSuccess(null);
                if (!TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
                    return;
                }
                Intent intent = new Intent(sAppContext, AVCallManager.class);
                sAppContext.stopService(intent);
            }
        });
    }

    private static void initIM(final Context context, int sdkAppID) {
        V2TIMSDKConfig sdkConfig = sConfigs.getSdkConfig();
        if (sdkConfig == null) {
            sdkConfig = new V2TIMSDKConfig();
            sConfigs.setSdkConfig(sdkConfig);
        }
        GeneralConfig generalConfig = sConfigs.getGeneralConfig();
        sdkConfig.setLogLevel(generalConfig.getLogLevel());
        sGson = new Gson();
        V2TIMManager.getInstance().initSDK(context, sdkAppID, sdkConfig, new V2TIMSDKListener() {
            @Override
            public void onConnecting() {
            }

            @Override
            public void onConnectSuccess() {
                NetWorkUtils.sIMSDKConnected = true;
                for (IMEventListener l : sIMEventListeners) {
                    l.onConnected();
                }
            }

            @Override
            public void onConnectFailed(int code, String error) {
                NetWorkUtils.sIMSDKConnected = false;
                for (IMEventListener l : sIMEventListeners) {
                    l.onDisconnected(code, error);
                }
            }

            @Override
            public void onKickedOffline() {
                for (IMEventListener l : sIMEventListeners) {
                    l.onForceOffline();
                }
                unInit();
            }

            @Override
            public void onUserSigExpired() {
                for (IMEventListener l : sIMEventListeners) {
                    l.onUserSigExpired();
                }
                unInit();
            }
        });

        V2TIMManager.getConversationManager().setConversationListener(new V2TIMConversationListener() {
            @Override
            public void onSyncServerStart() {
                super.onSyncServerStart();
                SLog.e("iml onSyncServerStart");
            }

            @Override
            public void onSyncServerFinish() {
                super.onSyncServerFinish();
                SLog.e("iml onSyncServerFinish");
            }

            @Override
            public void onSyncServerFailed() {
                super.onSyncServerFailed();
                SLog.e("iml onSyncServerFailed");
            }

            @Override
            public void onNewConversation(List<V2TIMConversation> conversationList) {
                SLog.e("impl conversation new");
                ConversationManagerKit.getInstance().onRefreshConversation(conversationList);
                ConversationManagerKit.getInstance().updateConversion();
                for (IMEventListener listener : sIMEventListeners) {
                    listener.onRefreshConversation(conversationList);
                }
            }

            @Override
            public void onConversationChanged(List<V2TIMConversation> conversationList) {
                SLog.e("im conversation change:"+conversationList.size());
                ConversationManagerKit.getInstance().onRefreshConversation(conversationList);
                ConversationManagerKit.getInstance().updateConversion();
                for (IMEventListener listener : sIMEventListeners) {
                    listener.onRefreshConversation(conversationList);
                }
            }

            @Override
            public void onTotalUnreadMessageCountChanged(long totalUnreadCount) {
                super.onTotalUnreadMessageCountChanged(totalUnreadCount);
                ConversationManagerKit.getInstance().updateTotalUnreadMessageCount(totalUnreadCount);
            }
        });

        V2TIMManager.getInstance().setGroupListener(new V2TIMGroupListener() {
            @Override
            public void onMemberEnter(String groupID, List<V2TIMGroupMemberInfo> memberList) {
                SLog.i("onMemberEnter groupID:" + groupID + ", size:" + memberList.size());
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_MEMBER_ENTER);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_MEMBER, sGson.toJson(memberList));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (v2TIMGroupMemberInfo.getUserID().equals(V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyJoinGroup(groupID, false);
                        return;
                    }
                }
            }

            @Override
            public void onMemberLeave(String groupID, V2TIMGroupMemberInfo member) {
                SLog.i("onMemberLeave groupID:" + groupID + ", memberID:" + member.getUserID());
                ConversationManagerKit.getInstance().setGroupConversationAvatar("group_" + groupID, null);
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_MEMBER_LEAVE);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_MEMBER, sGson.toJson(member));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onMemberInvited(String groupID, V2TIMGroupMemberInfo opUser, List<V2TIMGroupMemberInfo> memberList) {
                ConversationManagerKit.getInstance().setGroupConversationAvatar("group_" + groupID, null);
                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (v2TIMGroupMemberInfo.getUserID().equals(V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyJoinGroup(groupID, true);
                        return;
                    }
                }
            }

            @Override
            public void onMemberKicked(String groupID, V2TIMGroupMemberInfo opUser, List<V2TIMGroupMemberInfo> memberList) {
                ConversationManagerKit.getInstance().setGroupConversationAvatar("group_" + groupID, null);
                for (V2TIMGroupMemberInfo v2TIMGroupMemberInfo : memberList) {
                    if (v2TIMGroupMemberInfo.getUserID().equals(V2TIMManager.getInstance().getLoginUser())) {
                        GroupChatManagerKit.getInstance().notifyKickedFromGroup(groupID);
                        return;
                    }
                }
            }

            @Override
            public void onMemberInfoChanged(String groupID, List<V2TIMGroupMemberChangeInfo> v2TIMGroupMemberChangeInfoList) {

            }

            @Override
            public void onGroupCreated(String groupID) {

            }

            @Override
            public void onGroupDismissed(String groupID, V2TIMGroupMemberInfo opUser) {
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_GROUP_DISMISSED);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_OP_USER, sGson.toJson(opUser));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                GroupChatManagerKit.getInstance().notifyGroupDismissed(groupID);
            }

            @Override
            public void onGroupRecycled(String groupID, V2TIMGroupMemberInfo opUser) {
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_GROUP_RECYCLED);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_OP_USER, sGson.toJson(opUser));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                GroupChatManagerKit.getInstance().notifyGroupDismissed(groupID);
            }

            @Override
            public void onGroupInfoChanged(String groupID, List<V2TIMGroupChangeInfo> changeInfos) {
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_GROUP_INFO_CHANGED);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_INFO, sGson.toJson(changeInfos));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }

            @Override
            public void onReceiveJoinApplication(String groupID, V2TIMGroupMemberInfo member, String opReason) {
                ConversationManagerKit.getInstance().updateJoinGroup();
            }

            @Override
            public void onApplicationProcessed(String groupID, V2TIMGroupMemberInfo opUser, boolean isAgreeJoin, String opReason) {
                if (!isAgreeJoin) {
                    GroupChatManagerKit.getInstance().notifyJoinGroupRefused(groupID);
                }
            }

            @Override
            public void onGrantAdministrator(String groupID, V2TIMGroupMemberInfo opUser, List<V2TIMGroupMemberInfo> memberList) {

            }

            @Override
            public void onRevokeAdministrator(String groupID, V2TIMGroupMemberInfo opUser, List<V2TIMGroupMemberInfo> memberList) {

            }

            @Override
            public void onQuitFromGroup(String groupID) {
                SLog.i("onQuitFromGroup groupID:" + groupID);
            }

            @Override
            public void onReceiveRESTCustomData(String groupID, byte[] customData) {
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_REV_CUSTOM_DATA);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_CUSTOM_DATA, customData);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                GroupChatManagerKit.getInstance().notifyGroupRESTCustomSystemData(groupID, customData);
            }

            @Override
            public void onGroupAttributeChanged(String groupID, Map<String, String> groupAttributeMap) {
                Intent intent = new Intent(GroupListenerConstants.ACTION);
                intent.putExtra(GroupListenerConstants.KEY_METHOD, GroupListenerConstants.METHOD_ON_GROUP_ATTRS_CHANGED);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ID, groupID);
                intent.putExtra(GroupListenerConstants.KEY_GROUP_ATTR, sGson.toJson(groupAttributeMap));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });

        V2TIMManager.getFriendshipManager().setFriendListener(new V2TIMFriendshipListener() {
            @Override
            public void onFriendListAdded(List<V2TIMFriendInfo> users) {
                C2CChatManagerKit.getInstance().notifyNewFriend(users);
                ConversationManagerKit.getInstance().updateContacts();
            }

            @Override
            public void onFriendApplicationListAdded(List<V2TIMFriendApplication> applicationList) {
                super.onFriendApplicationListAdded(applicationList);
                ConversationManagerKit.getInstance().updateContacts();
            }

            @Override
            public void onFriendApplicationListDeleted(List<String> userIDList) {
                super.onFriendApplicationListDeleted(userIDList);
                ConversationManagerKit.getInstance().updateContacts();
            }

            @Override
            public void onBlackListDeleted(List<String> userList) {
                super.onBlackListDeleted(userList);
                ConversationManagerKit.getInstance().updateContacts();
            }

            @Override
            public void onBlackListAdd(List<V2TIMFriendInfo> infoList) {
                super.onBlackListAdd(infoList);
                ConversationManagerKit.getInstance().updateContacts();
            }

        });

        V2TIMManager.getMessageManager().addAdvancedMsgListener(new V2TIMAdvancedMsgListener() {
            @Override
            public void onRecvNewMessage(V2TIMMessage msg) {
                for (IMEventListener l : sIMEventListeners) {
                    l.onNewMessage(msg);
                }
            }

            @Override
            public void onRecvC2CReadReceipt(List<V2TIMMessageReceipt> receiptList) {
                C2CChatManagerKit.getInstance().onReadReport(receiptList);
            }

            @Override
            public void onRecvMessageRevoked(String msgID) {
                super.onRecvMessageRevoked(msgID);
            }
        });

        V2TIMManager.getMessageManager().addAdvancedMsgListener(MessageRevokedManager.getInstance());
    }

    public static void unInit() {
        ConversationManagerKit.getInstance().destroyConversation();
        if (!TUIKitConfigs.getConfigs().getGeneralConfig().isSupportAVCall()) {
            return;
        }
        AVCallManager.getInstance().unInit();
    }

    public static Context getAppContext() {
        return sAppContext;
    }

    public static TUIKitConfigs getConfigs() {
        if (sConfigs == null) {
            sConfigs = TUIKitConfigs.getConfigs();
        }
        return sConfigs;
    }

    public static void addIMEventListener(IMEventListener listener) {
        if (listener != null && !sIMEventListeners.contains(listener)) {
            sIMEventListeners.add(listener);
        }
    }

    public static void removeIMEventListener(IMEventListener listener) {
        if (listener == null) {
            sIMEventListeners.clear();
        } else {
            sIMEventListeners.remove(listener);
        }
    }
}
