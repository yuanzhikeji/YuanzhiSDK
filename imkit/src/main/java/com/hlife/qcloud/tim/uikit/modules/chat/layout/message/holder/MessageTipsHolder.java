package com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder;

import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.hlife.data.IMFriendManager;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;

public class MessageTipsHolder extends MessageEmptyHolder {

    private TextView mChatTipsTv;

    public MessageTipsHolder(View itemView) {
        super(itemView);
    }

    @Override
    public int getVariableLayout() {
        return R.layout.message_adapter_content_tips;
    }

    @Override
    public void initVariableViews() {
        mChatTipsTv = rootView.findViewById(R.id.chat_tips_tv);
    }

    @Override
    public void layoutViews(MessageInfo msg, int position) {
        super.layoutViews(msg, position);

        if (properties.getTipsMessageBubble() != null) {
            mChatTipsTv.setBackground(properties.getTipsMessageBubble());
        }
        if (properties.getTipsMessageFontColor() != 0) {
            mChatTipsTv.setTextColor(properties.getTipsMessageFontColor());
        }
        if (properties.getTipsMessageFontSize() != 0) {
            mChatTipsTv.setTextSize(properties.getTipsMessageFontSize());
        }

        if (msg.getStatus() == MessageInfo.MSG_STATUS_REVOKE) {
            if (msg.isSelf()) {
                msg.setExtra("您撤回了一条消息");
            } else if (msg.isGroup()) {
                String name = IMFriendManager.getInstance().getFriendRemark(msg.getFromUser());
                if (TextUtils.isEmpty(name)) {
                    name = msg.getGroupNameCard();
                }
                if (msg.getTimMessage() != null) {
                    if(TextUtils.isEmpty(name)){
                        name = msg.getTimMessage().getNameCard();
                    }
                    if(TextUtils.isEmpty(name)){
                        name = msg.getTimMessage().getNickName();
                    }
                }
                if(TextUtils.isEmpty(name)){
                    name = msg.getFromUser();
                }
                String message = IMKitConstants.covert2HTMLString(name);
                msg.setExtra(message + "撤回了一条消息");
            } else {
                msg.setExtra("对方撤回了一条消息");
            }
        }

        if (msg.getStatus() == MessageInfo.MSG_STATUS_REVOKE
                || (msg.getMsgType() >= MessageInfo.MSG_TYPE_GROUP_CREATE
                && msg.getMsgType() <= MessageInfo.MSG_TYPE_GROUP_AV_CALL_NOTICE)) {
            if (msg.getExtra() != null) {
                mChatTipsTv.setText(Html.fromHtml(msg.getExtra().toString()));
            }
        }
    }

}
