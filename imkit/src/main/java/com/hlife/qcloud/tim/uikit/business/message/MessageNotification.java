package com.hlife.qcloud.tim.uikit.business.message;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Html;
import android.text.TextUtils;

import com.hlife.liteav.model.CallModel;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.business.active.MwWorkActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupInfoListener;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.hlife.qcloud.tim.uikit.utils.TUIKitUtils;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMOfflinePushInfo;
import com.hlife.qcloud.tim.uikit.R;
import com.work.util.SLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageNotification {

    private static final String NOTIFICATION_CHANNEL_COMMON = "tuikit_common_msg";
    private static final int NOTIFICATION_ID_COMMON = 520;

    private static final String NOTIFICATION_CHANNEL_CALL = "tuikit_call_msg";
    private static final int NOTIFICATION_ID_CALL = 521;

    private static final int DIALING_DURATION = 30 * 1000;

    private static MessageNotification sNotification = null;

    private final NotificationManager mManager;
    private final Handler mHandler = new Handler();
    private final Context mContext = TUIKit.getAppContext();
    private final HashMap<String,String> mGroupNameMaps = new HashMap<>();

    private MessageNotification() {
        mManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mManager == null) {
            SLog.e("get NotificationManager failed");
            return;
        }
        createNotificationChannel(false);
        createNotificationChannel(true);
    }

    public static MessageNotification getInstance() {
        if(sNotification==null){
            sNotification = new MessageNotification();
        }
        return sNotification;
    }

    private void createNotificationChannel(boolean isDialing) {
        if (mManager == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel;
            if (isDialing) {
                channel = new NotificationChannel(NOTIFICATION_CHANNEL_CALL, "音视频邀请消息通知", NotificationManager.IMPORTANCE_HIGH);
                channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), null);
                channel.setDescription("当程序处于后台时新的来电消息会通过通知栏提醒用户");
                channel.setVibrationPattern(new long[]{0, 1000, 1000, 1000, 1000});
            } else {
                channel = new NotificationChannel(NOTIFICATION_CHANNEL_COMMON, "新普通消息通知", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("当程序处于后台时新消息会通过通知栏提醒用户");
            }
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            mManager.createNotificationChannel(channel);
        }
    }

    public void notify(V2TIMMessage msg) {
        if (mManager == null) {
            return;
        }
        if (TUIKitUtils.ignoreNotification(msg)) {
            return;
        }
        String id = msg.getGroupID();
        if(TextUtils.isEmpty(id)){
            id = msg.getUserID();
        }
        YzIMKitAgent.instance().getConversation(id, new YzConversationDataListener() {
            @Override
            public void onConversationData(ConversationInfo data) {
                super.onConversationData(data);
                if(data!=null){
                    if(!data.isShowDisturbIcon()){
                        showNotice(msg);
                    }
                }
            }
        });

    }

    void showNotice(V2TIMMessage msg){
        mHandler.removeCallbacksAndMessages(null);
        V2TIMOfflinePushInfo v2TIMOfflinePushInfo = msg.getOfflinePushInfo();
        String title = null;
        String desc = null;
        if (v2TIMOfflinePushInfo != null) {
            title = v2TIMOfflinePushInfo.getTitle();
            desc = v2TIMOfflinePushInfo.getDesc();
        }

        if (TextUtils.isEmpty(title)) {
            if (TextUtils.isEmpty(msg.getGroupID())) {
                if (!TextUtils.isEmpty(msg.getFriendRemark())) {
                    title = msg.getFriendRemark();
                } else if (!TextUtils.isEmpty(msg.getNickName())) {
                    title = msg.getNickName();
                } else {
                    title = msg.getUserID();
                }
                loadNotification(msg,title,desc);
            } else {
                title = msg.getGroupID();
                if(mGroupNameMaps.get(title)!=null){
                    loadNotification(msg,mGroupNameMaps.get(title),desc);
                }else{
                    loadGroupInfo(msg.getGroupID(),msg,desc);
                }
            }
        }else{
            loadNotification(msg,title,desc);
        }
    }

    private void loadGroupInfo(String groupId, final V2TIMMessage msg, final String desc){
        YzIMKitAgent.instance().getGroupInfo(new ArrayList<String>() {{
            add(groupId);
        }}, new YzGroupInfoListener() {
            @Override
            public void success(List<GroupInfo> groupInfoList) {
                if(groupInfoList!=null && groupInfoList.size()>0){
                    GroupInfo groupInfo = groupInfoList.get(0);
                    loadNotification(msg,groupInfo.getGroupName(),desc);
                }
            }
            @Override
            public void error(int code, String desc) {

            }
        });
    }

    private void loadNotification(V2TIMMessage msg,String title,String desc){
        CallModel callModel = CallModel.convert2VideoCallData(msg);
        boolean isDialing = false;
        if (callModel != null && callModel.action == CallModel.VIDEO_CALL_ACTION_DIALING) {
            isDialing = true;
        }
        final String tag;
        final int id;
        if (isDialing) {
            tag = NOTIFICATION_CHANNEL_CALL;
            id = NOTIFICATION_ID_CALL;
        } else {
            tag = NOTIFICATION_CHANNEL_COMMON;
            id = NOTIFICATION_ID_COMMON;
        }

        final Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isDialing) {
                builder = new Notification.Builder(mContext, NOTIFICATION_CHANNEL_CALL);
            } else {
                builder = new Notification.Builder(mContext, NOTIFICATION_CHANNEL_COMMON);
            }
            builder.setTimeoutAfter(DIALING_DURATION);
        } else {
            builder = new Notification.Builder(mContext);
        }
        String tickerStr = "新消息";
        builder.setTicker(tickerStr).setWhen(System.currentTimeMillis());


        builder.setContentTitle(title);
        if (TextUtils.isEmpty(desc)) {
            MessageInfo info = MessageInfoUtil.createMessageInfo(msg);
            if(info==null){
                return;
            }
            builder.setContentText(Html.fromHtml(info.getExtra().toString()));
        } else {
            builder.setContentText(Html.fromHtml(desc));
        }
        builder.setSmallIcon(R.drawable.message_small_icon);
        Intent launch;
        // 小米手机需要在设置里面把【云通信IM】的"后台弹出权限"打开才能点击Notification跳转。
        if (isDialing) {
            launch = new Intent(mContext, MwWorkActivity.class);
            launch.putExtra(Constants.CHAT_INFO, callModel);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } else {
            ChatInfo chatInfo = new ChatInfo();
            if (!TextUtils.isEmpty(msg.getGroupID())) {
                chatInfo.setId(msg.getGroupID());
                chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
            } else {
                chatInfo.setId(msg.getUserID());
                chatInfo.setType(V2TIMConversation.V2TIM_C2C);
            }
            chatInfo.setChatName(title);
            launch = new Intent(mContext, ChatActivity.class);
            launch.putExtra(Constants.CHAT_INFO, chatInfo);
            launch.putExtra(Constants.CHAT_INTO_ID,chatInfo.getId());
            launch.putExtra(Constants.CHAT_INTO_TYPE,chatInfo.getType());
            launch.putExtra(Constants.CHAT_INTO_NAME,title);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        builder.setContentIntent(PendingIntent.getActivity(mContext,
                (int) SystemClock.uptimeMillis(), launch, PendingIntent.FLAG_UPDATE_CURRENT));

        Notification notification = builder.build();
        if (isDialing) {
            notification.flags = Notification.FLAG_INSISTENT;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                notification.vibrate = new long[]{0, 1000, 1000, 1000, 1000};
                // 避免对端应用死掉导致本端一直响铃
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mManager.cancel(NOTIFICATION_CHANNEL_CALL, NOTIFICATION_ID_CALL);
                        builder.setContentText("通话失去响应");
                        Notification lastNotification = builder.build();
                        lastNotification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
                        lastNotification.defaults = Notification.DEFAULT_ALL;
                        mManager.notify(NOTIFICATION_CHANNEL_CALL, NOTIFICATION_ID_CALL, lastNotification);
                    }
                }, DIALING_DURATION);
            }
        } else {
            mManager.cancel(NOTIFICATION_CHANNEL_CALL, NOTIFICATION_ID_CALL);
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                notification.defaults = Notification.DEFAULT_ALL;
            }
        }
        mManager.notify(tag, id, notification);
    }

    public void cancelTimeout() {
        mHandler.removeCallbacksAndMessages(null);
    }
}
