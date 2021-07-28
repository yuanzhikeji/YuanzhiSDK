package com.yz.hlife.activity;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.work.util.ToastUtil;
import com.yz.hlife.R;
import com.yz.hlife.activity.adapter.ConversationListAdapter;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

public class ConversationListActivity extends BaseActivity {

    private static final String TAG = ConversationListActivity.class.getSimpleName();
    public static final String ConversationType = "ConversationType";

    private RecyclerView recyclerView;
    private ConversationListAdapter adapter;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();

        recyclerView = findViewById(R.id.conversation_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.mContext));
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();

        YzChatType chatType = YzChatType.ALL;
        int type = getIntent().getIntExtra(ConversationType, 0);
        switch (type) {
            case 1:
                chatType = YzChatType.C2C;
                break;
            case 2:
                chatType = YzChatType.GROUP;
                break;
        }
        adapter = new ConversationListAdapter(R.layout.conversation_item, new ArrayList<>());
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                ConversationInfo conversationInfo = (ConversationInfo) adapter.getItem(position);
                startChat(conversationInfo);
            }
        });
        recyclerView.setAdapter(adapter);

        YzIMKitAgent.instance().loadConversation(0, chatType, new YzConversationDataListener() {
            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                Log.i(TAG, "load conversations: " + data.size());
                adapter.setNewData(data);
                setTitle("未读消息:" + unRead);
            }

            @Override
            public void onConversationError(int code, String desc) {
                ToastUtil.info(ConversationListActivity.this,"获取会话列表失败："+ code + ", desc:" + desc);
            }
        });

        //YzIMKitAgent.instance().sendCustomMessage();

        YzIMKitAgent.instance().addMessageWatcher(new YzMessageWatcher() {
            @Override
            public void updateUnread(int count) {
                setTitle("未读消息:" + count);
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
        });
    }

    private void startChat(ConversationInfo conversationInfo) {
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(conversationInfo.isGroup() ? V2TIMConversation.V2TIM_GROUP : V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(conversationInfo.getId());
        chatInfo.setChatName(conversationInfo.getTitle());
        YzIMKitAgent.instance().startChat(chatInfo,null);
    }

    private void setTitle(String title) {
        this.setTitleName(title);
    }
}
