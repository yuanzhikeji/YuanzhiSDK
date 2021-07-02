package com.hlife.qcloud.tim.uikit.modules.chat.base;

import android.content.Context;

import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.component.NoticeLayout;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.work.util.SLog;

public abstract class ChatLayoutUI extends LinearLayout implements IChatLayout {

    protected NoticeLayout mGroupApplyLayout;
    protected View mRecordingGroup;
    protected ImageView mRecordingIcon;
    protected TextView mRecordingTips;
    private TitleBarLayout mTitleBar;
    private MessageLayout mMessageLayout;
    private InputLayout mInputLayout;
    private NoticeLayout mNoticeLayout;
    protected ChatInfo mChatInfo;
    private TextView mChatAtInfoLayout;

    public ChatLayoutUI(Context context) {
        super(context);
        initViews();
    }

    public ChatLayoutUI(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ChatLayoutUI(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        inflate(getContext(), R.layout.chat_layout, this);

        mTitleBar = findViewById(R.id.chat_title_bar);
        mMessageLayout = findViewById(R.id.chat_message_layout);
        mInputLayout = findViewById(R.id.chat_input_layout);
        mInputLayout.setChatLayout(this);
        mRecordingGroup = findViewById(R.id.voice_recording_view);
        mRecordingIcon = findViewById(R.id.recording_icon);
        mRecordingTips = findViewById(R.id.recording_tips);
        mGroupApplyLayout = findViewById(R.id.chat_group_apply_layout);
        mNoticeLayout = findViewById(R.id.chat_notice_layout);
        mChatAtInfoLayout = findViewById(R.id.chat_at_text_view);
        init();
    }

    protected void init() {

    }

    @Override
    public InputLayout getInputLayout() {
        return mInputLayout;
    }

    @Override
    public MessageLayout getMessageLayout() {
        return mMessageLayout;
    }

    @Override
    public NoticeLayout getNoticeLayout() {
        return mNoticeLayout;
    }

    @Override
    public ChatInfo getChatInfo() {
        return mChatInfo;
    }


    @Override
    public TextView getAtInfoLayout() {
        return mChatAtInfoLayout;
    }

    @Override
    public void setChatInfo(ChatInfo chatInfo) {
        mChatInfo = chatInfo;
        mInputLayout.setChatInfo(chatInfo);
        if (chatInfo == null) {
            return;
        }
        String chatTitle = chatInfo.getChatName();
        if(TextUtils.isEmpty(chatTitle)){
            if(chatInfo.getType() == V2TIMConversation.V2TIM_GROUP){
                GroupChatManagerKit.getInstance().loadGroupInfo(mChatInfo.getId(), new IUIKitCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        SLog.e(data.toString());
                        V2TIMGroupInfoResult result = (V2TIMGroupInfoResult) data;
                        getTitleBar().setTitle(result.getGroupInfo().getGroupName(), TitleBarLayout.POSITION.MIDDLE);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {

                    }
                });
            }
        }else{
            getTitleBar().setTitle(chatTitle, TitleBarLayout.POSITION.MIDDLE);
        }
    }

    @Override
    public void exitChat() {

    }

    @Override
    public void initDefault() {

    }

    @Override
    public void loadMessages(int type) {

    }

    @Override
    public void sendMessage(MessageInfo msg, boolean retry) {

    }

    @Override
    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setParentLayout(Object parent) {

    }
}
