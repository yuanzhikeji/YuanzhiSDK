package com.yz.hlife.activity;


import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.fragment.ChatFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.ConversationFragment;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageClickListener;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageViewGroup;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageDrawListener;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.work.util.ToastUtil;
import com.yz.hlife.R;

public class ChatDemoActivity extends BaseActivity implements ConversationListLayout.OnItemClickListener, YzMessageClickListener {
    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        int flag = getIntent().getIntExtra(ChatDemoActivity.class.getSimpleName(),0);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(flag == 1){
            ConversationFragment fragment1 = ConversationFragment.newConversation(YzChatType.GROUP);
            fragment1.setOnItemClickListener(this);
            fragmentTransaction.replace(R.id.container,fragment1);
            fragmentTransaction.commitAllowingStateLoss();
        }else if(flag == 2){
            ChatInfo chatInfo = (ChatInfo) getIntent().getSerializableExtra("chatInfo");

            ChatViewConfig chatViewConfig = new ChatViewConfig();
            chatViewConfig.setDisableVideoCall(false);
            chatViewConfig.setDisableAudioCall(false);

            ChatFragment chatFragment = ChatFragment.newChatFragment(chatInfo,chatViewConfig);
            chatFragment.setYzMessageClickListener(this);
            chatFragment.setYzCustomMessageDrawListener((parent, info) -> {
                View view = LayoutInflater.from(TUIKit.getAppContext()).inflate(com.hlife.qcloud.tim.uikit.R.layout.test_custom_message_layout1, null, false);
                parent.addMessageContentView(view);
            });
            fragmentTransaction.replace(R.id.container,chatFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }else{
            ConversationFragment fragment0 = ConversationFragment.newConversation(YzChatType.C2C);
            fragment0.setOnItemClickListener(this);
            fragmentTransaction.replace(R.id.container,fragment0);
            fragmentTransaction.commitAllowingStateLoss();
        }

    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
    }

    @Override
    public void onItemClick(View view, int position, ConversationInfo messageInfo) {
        ToastUtil.info(ChatDemoActivity.this,"自定义点击："+messageInfo.toString());
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setChatName(messageInfo.getTitle());
        chatInfo.setId(messageInfo.getId());
        chatInfo.setGroup(messageInfo.isGroup());
        startActivity(new Intent(this,ChatDemoActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("chatInfo",chatInfo)
                .putExtra(ChatDemoActivity.class.getSimpleName(),2));
    }

    @Override
    public boolean isShowTitleBar() {
        int flag = getIntent().getIntExtra(ChatDemoActivity.class.getSimpleName(),0);
        return flag!=2;
    }

    @Override
    public void onUserIconClick(String userId) {
        ToastUtil.info(this,"点击了头像："+userId);
    }

    @Override
    public void onAtGroupMember(String groupId) {

    }
    public static class customMessageLayout implements YzCustomMessageDrawListener {

        @Override
        public void onDraw(YzCustomMessageViewGroup parent, MessageInfo info) {

        }
    }
}
