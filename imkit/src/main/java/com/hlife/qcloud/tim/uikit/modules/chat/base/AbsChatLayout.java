package com.hlife.qcloud.tim.uikit.modules.chat.base;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.hlife.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.interfaces.IChatProvider;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.MessageListAdapter;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMTextElem;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.component.AudioPlayer;
import com.hlife.qcloud.tim.uikit.utils.BackgroundTasks;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.util.SLog;
import com.work.util.ToastUtil;


public abstract class AbsChatLayout extends ChatLayoutUI implements IChatLayout {

    protected MessageListAdapter mAdapter;

    private V2TIMMessage mConversationLastMessage;

    private AnimationDrawable mVolumeAnim;
    private Runnable mTypingRunnable = null;
    private final ChatProvider.TypingListener mTypingListener = new ChatProvider.TypingListener() {
        @Override
        public void onTyping() {
            final String oldTitle = getTitleBar().getMiddleTitle().getText().toString();
            getTitleBar().getMiddleTitle().setText(R.string.typing);
            if (mTypingRunnable == null) {
                mTypingRunnable = () -> getTitleBar().getMiddleTitle().setText(oldTitle);
            }
            getTitleBar().getMiddleTitle().removeCallbacks(mTypingRunnable);
            getTitleBar().getMiddleTitle().postDelayed(mTypingRunnable, 3000);
        }
    };

    public AbsChatLayout(Context context) {
        super(context);
    }

    public AbsChatLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AbsChatLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initListener() {
        getMessageLayout().setPopActionClickListener(new MessageLayout.OnPopActionClickListener() {
            @Override
            public void onCopyClick(int position, MessageInfo msg) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard == null || msg == null) {
                    return;
                }
                if (msg.getMsgType() == MessageInfo.MSG_TYPE_TEXT) {
                    V2TIMTextElem textElem = msg.getTimMessage().getTextElem();
                    String copyContent;
                    if (textElem == null) {
                        copyContent = (String) msg.getExtra();
                    } else {
                        copyContent = textElem.getText();
                    }
                    ClipData clip = ClipData.newPlainText("message", copyContent);
                    clipboard.setPrimaryClip(clip);
                }
            }

            @Override
            public void onSendMessageClick(MessageInfo msg, boolean retry) {
                sendMessage(msg, retry);
            }

            @Override
            public void onDeleteMessageClick(int position, MessageInfo msg) {
                deleteMessage(position, msg);
            }

            @Override
            public void onRevokeMessageClick(int position, MessageInfo msg) {
                revokeMessage(position, msg);
            }
        });
        getMessageLayout().setLoadMoreMessageHandler(new MessageLayout.OnLoadMoreHandler() {
            @Override
            public void loadMore(int type) {
                loadMessages(type);
            }

            @Override
            public boolean isListEnd(int postion) {
                if (mAdapter == null || mConversationLastMessage == null || mAdapter.getItem(postion) == null) {
                    return true;
                }
                if (postion < 0 || postion >= mAdapter.getItemCount()) {
                    return true;
                }

                return mAdapter.getItem(postion).getTimMessage().getSeq() >= mConversationLastMessage.getSeq();
            }
        });
        getMessageLayout().setEmptySpaceClickListener(() -> getInputLayout().hideSoftInput());

        /**
         * ???????????????????????????????????????
         */
        getMessageLayout().addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    View child = rv.findChildViewUnder(e.getX(), e.getY());
                    if (child == null) {
                        getInputLayout().hideSoftInput();
                    } else if (child instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) child;
                        final int count = group.getChildCount();
                        float x = e.getRawX();
                        float y = e.getRawY();
                        View touchChild = null;
                        for (int i = count - 1; i >= 0; i--) {
                            final View innerChild = group.getChildAt(i);
                            int position[] = new int[2];
                            innerChild.getLocationOnScreen(position);
                            if (x >= position[0]
                                    && x <= position[0] + innerChild.getMeasuredWidth()
                                    && y >= position[1]
                                    && y <= position[1] + innerChild.getMeasuredHeight()) {
                                touchChild = innerChild;
                                break;
                            }
                        }
                        if (touchChild == null) {
                            getInputLayout().hideSoftInput();
                        }
                    }
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        getInputLayout().setChatInputHandler(new InputLayout.ChatInputHandler() {
            @Override
            public void onInputAreaClick() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        scrollToEnd();
                    }
                });
            }

            @Override
            public void onRecordStatusChanged(int status) {
                switch (status) {
                    case RECORD_START:
                        startRecording();
                        break;
                    case RECORD_STOP:
                        stopRecording();
                        break;
                    case RECORD_CANCEL:
                        cancelRecording();
                        break;
                    case RECORD_TOO_SHORT:
                    case RECORD_FAILED:
                        stopAbnormally(status);
                        break;
                    default:
                        break;
                }
            }

            private void startRecording() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        AudioPlayer.getInstance().stopPlay();
                        mRecordingGroup.setVisibility(VISIBLE);
                        mRecordingIcon.setImageResource(R.drawable.recording_volume);
                        mVolumeAnim = (AnimationDrawable) mRecordingIcon.getDrawable();
                        mVolumeAnim.start();
                        mRecordingTips.setTextColor(Color.WHITE);
                        mRecordingTips.setText("???????????????????????????");
                    }
                });
            }

            private void stopRecording() {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVolumeAnim.stop();
                        mRecordingGroup.setVisibility(GONE);
                    }
                }, 500);
            }

            private void stopAbnormally(final int status) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mVolumeAnim.stop();
                        mRecordingIcon.setImageResource(R.drawable.ic_volume_dialog_length_short);
                        mRecordingTips.setTextColor(Color.WHITE);
                        if (status == RECORD_TOO_SHORT) {
                            mRecordingTips.setText("??????????????????");
                        } else {
                            mRecordingTips.setText("????????????");
                        }
                    }
                });
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRecordingGroup.setVisibility(GONE);
                    }
                }, 1000);
            }

            private void cancelRecording() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mRecordingIcon.setImageResource(R.drawable.ic_volume_dialog_cancel);
                        mRecordingTips.setText("???????????????????????????");
                    }
                });
            }
        });
    }

    @Override
    public void initDefault() {
        getTitleBar().getLeftGroup().setVisibility(VISIBLE);
        getTitleBar().setOnLeftClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getContext() instanceof Activity) {
                    ((Activity) getContext()).finish();
                }
            }
        });
        getInputLayout().setMessageHandler(new InputLayout.MessageHandler() {
            @Override
            public void sendMessage(MessageInfo msg) {
                AbsChatLayout.this.sendMessage(msg, false);
            }
        });
        getInputLayout().clearCustomActionList();
        if (getMessageLayout().getAdapter() == null) {
            mAdapter = new MessageListAdapter();
            getMessageLayout().setAdapter(mAdapter);
        }
        initListener();
    }

    @Override
    public void setParentLayout(Object parentContainer) {

    }

    public void scrollToEnd() {
        getMessageLayout().scrollToEnd();
    }

    public void setDataProvider(IChatProvider provider) {
        if (provider != null) {
            ((ChatProvider) provider).setTypingListener(mTypingListener);
        }
        if (mAdapter != null) {
            mAdapter.setDataSource(provider);
            getChatManager().setLastMessageInfo(mAdapter.getItemCount() > 0 ? mAdapter.getItem(1) : null);
        }
    }

    public abstract ChatManagerKit getChatManager();

    @Override
    public void loadMessages(int type) {
        if (type == TUIKitConstants.GET_MESSAGE_FORWARD) {
            loadChatMessages(mAdapter.getItemCount() > 0 ? mAdapter.getItem(1).getTimMessage() : null, type);
        } else if (type == TUIKitConstants.GET_MESSAGE_BACKWARD){
            loadChatMessages(mAdapter.getItemCount() > 0 ? mAdapter.getItem(mAdapter.getItemCount() -1).getTimMessage() : null, type);
        }
    }

    public void loadChatMessages(final V2TIMMessage lastMessage, final int getMessageType) {
        getChatManager().loadChatMessages(getMessageType, lastMessage, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                if (getMessageType == TUIKitConstants.GET_MESSAGE_TWO_WAY || (lastMessage == null && data != null)) {
                    setDataProvider((ChatProvider) data);
                }

                if (getMessageType == TUIKitConstants.GET_MESSAGE_TWO_WAY) {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSourceChanged(MessageLayout.DATA_CHANGE_SCROLL_TO_POSITION, mAdapter.getLastMessagePosition(lastMessage));
                    }
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                SLog.e(errCode+">"+errMsg);
                if (lastMessage == null) {
                    setDataProvider(null);
                }
            }
        });
    }

    public void getConversationLastMessage(String id) {
        V2TIMManager.getConversationManager().getConversation(id, new V2TIMValueCallback<V2TIMConversation>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("getConversationLastMessage error:"+code+">>"+desc);
            }

            @Override
            public void onSuccess(V2TIMConversation v2TIMConversation) {
                if (v2TIMConversation == null){
                    mConversationLastMessage = null;
                    return;
                }
                mConversationLastMessage = v2TIMConversation.getLastMessage();
            }
        });
    }

    protected void deleteMessage(int position, MessageInfo msg) {
        getChatManager().deleteMessage(position, msg);
    }

    protected void revokeMessage(int position, MessageInfo msg) {
        getChatManager().revokeMessage(position, msg);
    }

    @Override
    public void sendMessage(MessageInfo msg, boolean retry) {
        getChatManager().sendMessage(msg, retry, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                BackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scrollToEnd();
                    }
                });
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
//                ToastUtil.toastLongMessage(errMsg);
                if(errCode == 10017){
                    ToastUtil.info(getContext(),"??????????????????");
                }
            }
        });
    }


    @Override
    public void exitChat() {
        getTitleBar().getMiddleTitle().removeCallbacks(mTypingRunnable);
        AudioPlayer.getInstance().stopRecord();
        AudioPlayer.getInstance().stopPlay();
        getChatManager().markMessageAsRead(mChatInfo);
        if (getChatManager() != null) {
            // ????????????????????? ChatManagerKit ?????? ChatInfo ????????????????????????????????????????????????????????? ChatInfo
            if (getChatInfo() != getChatManager().getCurrentChatInfo()) {
                return;
            }
            getChatManager().destroyChat();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        exitChat();
    }


}
