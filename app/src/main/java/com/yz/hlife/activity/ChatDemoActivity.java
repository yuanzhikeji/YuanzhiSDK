package com.yz.hlife.activity;


import android.content.Intent;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.fragment.ChatFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.ConversationFragment;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageClickListener;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.work.util.SLog;
import com.work.util.ToastUtil;
import com.yz.hlife.R;

public class ChatDemoActivity extends BaseActivity implements ConversationListLayout.OnItemClickListener, YzMessageClickListener {
    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        int flag = getIntent().getIntExtra(ChatDemoActivity.class.getSimpleName(),0);
        SLog.e("flag:"+flag);
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
            fragmentTransaction.replace(R.id.container,chatFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }else{
            ConversationFragment fragment0 = ConversationFragment.newConversation(YzChatType.SINGLE);
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

}
