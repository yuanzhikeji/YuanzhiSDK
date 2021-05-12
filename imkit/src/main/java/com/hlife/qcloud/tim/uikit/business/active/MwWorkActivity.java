package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.hlife.liteav.model.CallModel;
import com.hlife.liteav.model.TRTCAVCallImpl;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseFragment;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.adapter.TabPagerAdapter;
import com.hlife.qcloud.tim.uikit.business.dialog.UpdateAppDialog;
import com.hlife.qcloud.tim.uikit.business.fragment.ContactFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.ConversationFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.ProfileFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.WorkFragment;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.thirdpush.HUAWEIHmsMessageService;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.utils.BrandUtil;
import com.hlife.qcloud.tim.uikit.utils.FileUtil;
import com.tencent.imsdk.v2.V2TIMFriendApplicationResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMSignalingInfo;
import com.tencent.imsdk.v2.V2TIMUserFullInfo;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.util.SharedUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangyx
 * Date 2020/8/16
 * email tangyx@live.com
 */

public class MwWorkActivity extends IMBaseActivity implements
        YzMessageWatcher,
        BottomNavigationBar.OnTabSelectedListener,
        ViewPager.OnPageChangeListener {
    private BottomNavigationBar mNavigationBar;
    private ViewPager mPager;
    private TextBadgeItem mMessageBadge;
    private TextBadgeItem mContactsNewBadge;
    private List<BaseFragment> mFragments;

    private CallModel mCallModel;
    public static Context instance;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        instance = this;
        prepareThirdPushToken();
        mNavigationBar = findViewById(R.id.bottom_navigation_bar);
        mMessageBadge = new TextBadgeItem();
        mMessageBadge.hide();
        mContactsNewBadge = new TextBadgeItem();
        mContactsNewBadge.hide();
        int functionPrem = YzIMKitAgent.instance().getFunctionPrem();
        if((functionPrem & 1)>0){
            BottomNavigationItem messageItem = new BottomNavigationItem(R.drawable.icon_chat_fill, R.string.tab_conversation_tab_text)
                    .setInactiveIconResource(R.drawable.icon_chat_stroke)
                    .setActiveColorResource(R.color.defaultColorAccent);
            messageItem.setBadgeItem(mMessageBadge);
            mNavigationBar.addItem(messageItem);
        }

        if((functionPrem & 2)>0){
            BottomNavigationItem contactItem = new BottomNavigationItem(R.drawable.icon_contact_fill, R.string.tab_contact_tab_text)
                    .setInactiveIconResource(R.drawable.icon_contact_stroke)
                    .setActiveColorResource(R.color.defaultColorAccent);
            contactItem.setBadgeItem(mContactsNewBadge);
            mNavigationBar.addItem(contactItem);
        }

        if((functionPrem & 4)>0){
            BottomNavigationItem workItem = new BottomNavigationItem(R.drawable.icon_tools_fill, R.string.tab_work_tab_text)
                    .setInactiveIconResource(R.drawable.icon_tools_stroke)
                    .setActiveColorResource(R.color.defaultColorAccent);
            mNavigationBar.addItem(workItem);
        }

        if((functionPrem & 8)>0){
            BottomNavigationItem profileItem = new BottomNavigationItem(R.drawable.icon_user_fill, R.string.tab_profile_tab_text)
                    .setInactiveIconResource(R.drawable.icon_user_stroke)
                    .setActiveColorResource(R.color.defaultColorAccent);
            mNavigationBar.addItem(profileItem);
        }

        mNavigationBar.initialise();
        mNavigationBar.setTabSelectedListener(this);
        String yzAppId = SharedUtils.getString("YzAppId");
        if("de241446a50499bb77a8684cf610fd04".equals(yzAppId)){//只有元信才需要去验证是否升级
            UpdateAppDialog.showUpdateDialog(this,false);
            //设置为必须要验证才能加好友
            V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
            v2TIMUserFullInfo.setAllowType(V2TIMUserFullInfo.V2TIM_FRIEND_NEED_CONFIRM);
            V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, null);
        }else{
            //随便加好友
            V2TIMUserFullInfo v2TIMUserFullInfo = new V2TIMUserFullInfo();
            v2TIMUserFullInfo.setAllowType(V2TIMUserFullInfo.V2TIM_FRIEND_ALLOW_ANY);
            V2TIMManager.getInstance().setSelfInfo(v2TIMUserFullInfo, null);
        }
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        mFragments = new ArrayList<>();
        int functionPrem = YzIMKitAgent.instance().getFunctionPrem();
        if((functionPrem & 1)>0){
            mFragments.add(ConversationFragment.newConversation(YzChatType.ALL));
        }
        if((functionPrem & 2)>0){
            mFragments.add(new ContactFragment());
        }
        if((functionPrem & 4)>0){
            mFragments.add(new WorkFragment());
        }
        if((functionPrem & 8)>0){
            mFragments.add(new ProfileFragment());
        }
        if(mFragments.size()<=1){
            mNavigationBar.setVisibility(View.INVISIBLE);
            View line = findViewById(R.id.line);
            mNavigationBar.getLayoutParams().height=0;
            line.getLayoutParams().height=0;
        }
        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(this);
        TabPagerAdapter mAdapter = new TabPagerAdapter(this.getSupportFragmentManager(), mFragments);
        mPager.setAdapter(mAdapter);
        mPager.setOffscreenPageLimit(mFragments.size());
        FileUtil.initPath(); // 从application移入到这里，原因在于首次装上app，需要获取一系列权限，如创建文件夹，图片下载需要指定创建好的文件目录，否则会下载本地失败，聊天页面从而获取不到图片、表情
//        // 未读消息监视器
        ConversationManagerKit.getInstance().addMessageWatcher(this);
        GroupChatManagerKit.getInstance();
        getFriendApplicationList();
    }

    @Override
    protected void onDestroy() {
        ConversationManagerKit.getInstance().destroyConversation();
        instance = null;
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mCallModel = (CallModel) intent.getSerializableExtra(Constants.CHAT_INFO);
    }


    private void prepareThirdPushToken() {
//        ThirdPushTokenMgr.getInstance().setPushTokenToTIM();

        if (BrandUtil.isBrandHuawei()) {
            // 华为离线推送
//            new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        // read from agconnect-services.json
//                        String appId = AGConnectServicesConfig.fromContext(MwWorkActivity.this).getString("client/app_id");
//                        String token = HmsInstanceId.getInstance(MwWorkActivity.this).getToken(appId, "HCM");
//                        if (!TextUtils.isEmpty(token)) {
//                            ThirdPushTokenMgr.getInstance().setThirdPushToken(token);
//                            ThirdPushTokenMgr.getInstance().setPushTokenToTIM();
//                        }
//                    } catch (ApiException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }.start();
        } else if (BrandUtil.isBrandVivo()) {
            // vivo离线推送
//            DemoLog.i(TAG, "vivo support push: " + PushClient.getInstance(getApplicationContext()).isSupport());
//            PushClient.getInstance(getApplicationContext()).turnOnPush(new IPushActionListener() {
//                @Override
//                public void onStateChanged(int state) {
//                    if (state == 0) {
//                        String regId = PushClient.getInstance(getApplicationContext()).getRegId();
//                        ThirdPushTokenMgr.getInstance().setThirdPushToken(regId);
//                        ThirdPushTokenMgr.getInstance().setPushTokenToTIM();
//                    } else {
//                        // 根据vivo推送文档说明，state = 101 表示该vivo机型或者版本不支持vivo推送，链接：https://dev.vivo.com.cn/documentCenter/doc/156
//                        SLog.i("vivopush open vivo push fail state = " + state);
//                    }
//                }
//            });
        }
//        else if (HeytapPushManager.isSupportPush()) {
//            // oppo离线推送
//            OPPOPushImpl oppo = new OPPOPushImpl();
//            oppo.createNotificationChannel(this);
//            HeytapPushManager.register(this, PrivateConstants.OPPO_PUSH_APPKEY, PrivateConstants.OPPO_PUSH_APPSECRET, oppo);
//        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mCallModel != null) {
            TRTCAVCallImpl impl = (TRTCAVCallImpl) TRTCAVCallImpl.sharedInstance(TUIKit.getAppContext());
            impl.stopCall();
            final V2TIMSignalingInfo info = new V2TIMSignalingInfo();
            info.setInviteID(mCallModel.callId);
            info.setInviteeList(mCallModel.invitedList);
            info.setGroupID(mCallModel.groupId);
            info.setInviter(mCallModel.sender);
            info.setData(mCallModel.data);
            ((TRTCAVCallImpl) (TRTCAVCallImpl.sharedInstance(TUIKit.getAppContext()))).processInvite(
                    info.getInviteID(), info.getInviter(), info.getGroupID(), info.getInviteeList(), info.getData());
            mCallModel = null;
        }
    }

    @Override
    public void updateUnread(int count) {
        if (count > 0) {
            mMessageBadge.show(true);
        } else {
            mMessageBadge.hide();
        }
        String unreadStr = "" + count;
        if (count > 100) {
            unreadStr = "99+";
        }
        mMessageBadge.setText(unreadStr);
//        // 华为离线推送角标
        HUAWEIHmsMessageService.updateBadge(this, count);
    }

    @Override
    public void updateContacts() {
        for (int i = 0; i < mFragments.size(); i++) {
            if(mFragments.get(i) instanceof ContactFragment){
                updateFragment(i);
            }
        }
        getFriendApplicationList();
    }

    private void getFriendApplicationList(){
        V2TIMManager.getFriendshipManager().getFriendApplicationList(new V2TIMValueCallback<V2TIMFriendApplicationResult>() {
            @Override
            public void onError(int code, String desc) {
            }

            @Override
            public void onSuccess(V2TIMFriendApplicationResult v2TIMFriendApplicationResult) {
                int count = 0;
                if (v2TIMFriendApplicationResult.getFriendApplicationList() != null) {
                    count = v2TIMFriendApplicationResult.getUnreadCount();
                    if (count > 0) {
                        mContactsNewBadge.show(true);
                    } else {
                        mContactsNewBadge.hide();
                    }
                    String unreadStr = "" + count;
                    if (count > 100) {
                        unreadStr = "99+";
                    }
                    mContactsNewBadge.setText(unreadStr);
                }else{
                    mContactsNewBadge.hide();
                }
                for (BaseFragment fragment:mFragments) {
                    if(fragment instanceof ContactFragment){
                        ((ContactFragment) fragment).newFriendCount(count);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void updateConversion() {
        BaseFragment f = mFragments.get(0);
        if(f instanceof ConversationFragment){
            ((ConversationFragment) f).refreshData();
        }
    }

    @Override
    public void updateJoinGroup() {

    }

    @Override
    public boolean isShowTitleBar() {
        return false;
    }

    @Override
    public void onBackPressed() {
        String yzAppId = SharedUtils.getString("YzAppId");
        if("de241446a50499bb77a8684cf610fd04".equals(yzAppId)){//只有元信才需要去验证
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onTabSelected(int position) {
        mPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(int position) {

    }

    @Override
    public void onTabReselected(int position) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mNavigationBar.selectTab(position,false);
        updateFragment(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
    private void updateFragment(int position){
        BaseFragment fragment = mFragments.get(position);
        if(fragment instanceof ContactFragment){
            ((ContactFragment) fragment).refreshData();
        }else if(fragment instanceof WorkFragment){
            ((WorkFragment) fragment).refreshTool();
        }
        if(fragment instanceof WorkFragment || fragment instanceof ProfileFragment){
            setStatusBar(ContextCompat.getColor(this, R.color.background_color));
        }else{
            setStatusBar(ContextCompat.getColor(this, R.color.white));
        }
    }
}
