package com.yz.hlife.activity.adapter;

import android.view.View;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.yz.hlife.R;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;

import java.util.List;

public class ConversationListAdapter extends BaseQuickAdapter<ConversationInfo, ConversationItemViewHolder> {
    public ConversationListAdapter(int layoutResId, @Nullable List<ConversationInfo> data) {
        super(layoutResId, data);
    }

    public ConversationListAdapter(int layoutResId) {
        super(layoutResId);
    }

    @Override
    protected void convert(ConversationItemViewHolder helper, ConversationInfo item) {
        helper.setText(R.id.conversation_title, item.getTitle())
                .setText(R.id.conversation_time, "");
        MessageInfo info = item.getLastMessage();
        if (info != null) {
            helper.setText(R.id.conversation_last_msg, info.getExtra().toString());
        } else {
            helper.setText(R.id.conversation_last_msg, "");
        }
        if (item.getUnRead() <= 0) {
            helper.getView(R.id.conversation_unread).setVisibility(View.GONE);
            helper.setText(R.id.conversation_unread, "");
        } else {
            helper.getView(R.id.conversation_unread).setVisibility(View.VISIBLE);
            helper.setText(R.id.conversation_unread, String.valueOf(item.getUnRead()));
        }
        ConversationIconView iconView = (ConversationIconView) helper.getView(R.id.conversation_icon);
        iconView.setConversation(item);
    }
}
