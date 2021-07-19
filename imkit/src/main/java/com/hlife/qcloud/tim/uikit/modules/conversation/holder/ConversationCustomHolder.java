package com.hlife.qcloud.tim.uikit.modules.conversation.holder;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlife.data.IMFriendManager;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.tencent.imsdk.v2.V2TIMConversation;

/**
 * 自定义会话Holder
 */
public class ConversationCustomHolder extends ConversationBaseHolder {

    protected LinearLayout leftItemLayout;
    protected TextView titleText;
    protected TextView messageText;
    protected TextView timelineText;
    protected TextView unreadText;
    protected ConversationIconView conversationIconView;


    public ConversationCustomHolder(View itemView) {
        super(itemView);
        leftItemLayout = rootView.findViewById(R.id.item_left);
        conversationIconView = rootView.findViewById(R.id.conversation_icon);
        titleText = rootView.findViewById(R.id.conversation_title);
        messageText = rootView.findViewById(R.id.conversation_last_msg);
        timelineText = rootView.findViewById(R.id.conversation_time);
        unreadText = rootView.findViewById(R.id.conversation_unread);
    }

    @Override
    public void layoutViews(ConversationInfo conversation, int position) {
        if (conversation.isTop()) {
            leftItemLayout.setBackgroundColor(rootView.getResources().getColor(R.color.conversation_top_color));
        } else {
            leftItemLayout.setBackgroundColor(Color.WHITE);
        }
        conversationIconView.setDefaultImageResId(R.drawable.default_head);
        conversationIconView.setIconUrls(conversation.getIconUrlList());

        if (conversation.getType() == V2TIMConversation.V2TIM_C2C) {
            String remark = IMFriendManager.getInstance().getFriendRemark(conversation.getId());
            if (!TextUtils.isEmpty(remark)) {
                titleText.setText(remark);
            } else {
                titleText.setText(conversation.getTitle());
            }
        } else {
            titleText.setText(conversation.getTitle());
        }

        messageText.setText("");
        timelineText.setText("");

        if (conversation.getUnRead() > 0) {
            unreadText.setVisibility(View.VISIBLE);
            if (conversation.getUnRead() > 99) {
                unreadText.setText("99+");
            } else {
                unreadText.setText("" + conversation.getUnRead());
            }
        } else {
            unreadText.setVisibility(View.GONE);
        }

        if (mAdapter.getItemDateTextSize() != 0) {
            timelineText.setTextSize(mAdapter.getItemDateTextSize());
        }
        if (mAdapter.getItemBottomTextSize() != 0) {
            messageText.setTextSize(mAdapter.getItemBottomTextSize());
        }
        if (mAdapter.getItemTopTextSize() != 0) {
            titleText.setTextSize(mAdapter.getItemTopTextSize());
        }

    }

}
