package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.fragment.ChatFragment;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.hlife.qcloud.tim.uikit.R;

import static com.tencent.imsdk.v2.V2TIMManager.V2TIM_STATUS_LOGINED;

public class ChatActivity extends IMBaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_im_chat);
        chat(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        chat(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void chat(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        ChatInfo mChatInfo;
        mChatInfo = (ChatInfo) bundle.getSerializable(Constants.CHAT_INFO);
        if(mChatInfo==null){
            String chatId = bundle.getString(Constants.CHAT_INTO_ID);
            int chatType = bundle.getInt(Constants.CHAT_INTO_TYPE,-1);
            if(!TextUtils.isEmpty(chatId) && chatType!=-1){
                mChatInfo = new ChatInfo();
                mChatInfo.setId(chatId);
                mChatInfo.setType(chatType);
                mChatInfo.setChatName(bundle.getString(Constants.CHAT_INTO_NAME));
                bundle.putSerializable(Constants.CHAT_INFO, mChatInfo);
            }
        }
        if (mChatInfo == null) {
            finish();
            return;
        }
        if (V2TIMManager.getInstance().getLoginStatus() == V2TIM_STATUS_LOGINED) {
            ChatFragment mChatFragment = new ChatFragment();
            bundle.putBoolean(Constants.CHAT_TO_CONVERSATION,getIntent().getBooleanExtra(Constants.CHAT_TO_CONVERSATION,true));
            mChatFragment.setArguments(bundle);
            mChatFragment.setConfig((ChatViewConfig) getIntent().getSerializableExtra(Constants.CHAT_CONFIG));
            getSupportFragmentManager().beginTransaction().replace(R.id.empty_view, mChatFragment).commitAllowingStateLoss();
        } else {
            finish();
        }
    }

    @Override
    public boolean isShowTitleBar() {
        return false;
    }
}
