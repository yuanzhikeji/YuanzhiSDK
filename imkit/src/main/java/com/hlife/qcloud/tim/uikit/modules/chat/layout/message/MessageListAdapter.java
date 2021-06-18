package com.hlife.qcloud.tim.uikit.modules.chat.layout.message;

import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IBaseViewHolder;
import com.hlife.qcloud.tim.uikit.base.TUIChatControllerListener;
import com.hlife.qcloud.tim.uikit.base.TUIKitListenerManager;
import com.hlife.qcloud.tim.uikit.business.helper.CustomChatController;
import com.hlife.qcloud.tim.uikit.modules.chat.interfaces.IChatProvider;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageDrawListener;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageContentHolder;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageCustomHolder;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageEmptyHolder;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageHeaderHolder;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageBaseHolder;
import com.hlife.qcloud.tim.uikit.utils.BackgroundTasks;
import com.tencent.imsdk.v2.V2TIMMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MessageListAdapter extends RecyclerView.Adapter {

    public static final int MSG_TYPE_HEADER_VIEW = -99;
    private boolean mLoading = true;
    private MessageLayout mRecycleView;
    private List<MessageInfo> mDataSource = new ArrayList<>();
    private MessageLayout.OnItemLongClickListener mOnItemLongClickListener;

    private YzCustomMessageDrawListener mOnCustomMessageDrawListener;

    //消息转发
    private HashMap<String, Boolean> mSelectedPositions = new HashMap<String, Boolean>();
    private boolean isShowMutiSelectCheckBox = false;
    private int mHighShowPosition;

    public MessageLayout.OnItemLongClickListener getOnItemClickListener() {
        return this.mOnItemLongClickListener;
    }

    public void setOnCustomMessageDrawListener(YzCustomMessageDrawListener listener) {
        mOnCustomMessageDrawListener = listener;
    }

    public void setOnItemClickListener(MessageLayout.OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setHighShowPosition(int mHighShowPosition) {
        this.mHighShowPosition = mHighShowPosition;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return MessageEmptyHolder.Factory.getInstance(parent, this, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        final MessageInfo msg = getItem(position);
        if (holder instanceof MessageBaseHolder) {
            final MessageBaseHolder baseHolder = (MessageBaseHolder) holder;
            baseHolder.setOnItemClickListener(mOnItemLongClickListener);
            String msgId = "";
            if (msg != null) {
                msgId = msg.getId();
            }
            switch (getItemViewType(position)) {
                case MSG_TYPE_HEADER_VIEW:
                    ((MessageHeaderHolder) baseHolder).setLoadingStatus(mLoading);
                    break;
                case MessageInfo.MSG_TYPE_TEXT:
                case MessageInfo.MSG_TYPE_IMAGE:
                case MessageInfo.MSG_TYPE_VIDEO:
                case MessageInfo.MSG_TYPE_CUSTOM_FACE:
                case MessageInfo.MSG_TYPE_AUDIO:
                case MessageInfo.MSG_TYPE_FILE:
                case MessageInfo.MSG_TYPE_MERGE: {
                    if (position == mHighShowPosition) {
//                        ValueAnimator animator = ValueAnimator.ofInt(TUIKit.getAppContext().getResources().getColor(R.color.chat_background_color),TUIKit.getAppContext().getResources().getColor(R.color.green));
//                        animator.addUpdateListener(animation -> {
//                            int curValue = (int)animation.getAnimatedValue();
//                            ((MessageEmptyHolder) baseHolder).mContentLayout.setBackgroundColor(curValue);
//                        });
//                        animator.setRepeatMode(ValueAnimator.REVERSE);
//                        animator.setRepeatCount(ValueAnimator.INFINITE);
//                        animator.setDuration(3000);
//                        animator.start();

                        final Handler mHandlerData = new Handler();
                        Runnable runnable = () -> {
                            ((MessageEmptyHolder) baseHolder).mContentLayout.setBackgroundColor(TUIKit.getAppContext().getResources().getColor(R.color.line));
                            new Handler(Looper.getMainLooper()).postDelayed(() -> ((MessageEmptyHolder) baseHolder).mContentLayout.setBackgroundColor(TUIKit.getAppContext().getResources().getColor(R.color.chat_background_color)), 600);
                        };

                        mHandlerData.postDelayed(runnable, 200);
                        mHighShowPosition = -1;
                    }
                }
                break;
            }
            baseHolder.layoutViews(msg, position);
        }
        // 对于自定义消息，需要在正常布局之后，交给外部调用者重新加载渲染
        if (holder instanceof IBaseViewHolder) {
            if (holder instanceof MessageCustomHolder) {
                ((MessageCustomHolder) holder).setShowMutiSelect(isShowMutiSelectCheckBox);
            }
            bindCustomHolder(position, msg, (IBaseViewHolder) holder);
        }
    }

    private void bindCustomHolder(int position, MessageInfo msg, IBaseViewHolder customHolder) {
        for(TUIChatControllerListener chatListener : TUIKitListenerManager.getInstance().getTUIChatListeners()) {
            if(chatListener instanceof CustomChatController){
                ((CustomChatController) chatListener).setOnCustomMessageDrawListener(mOnCustomMessageDrawListener);
            }
            if (chatListener.bindCommonViewHolder(customHolder, msg, position)) {
                return;
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecycleView = (MessageLayout) recyclerView;
        mRecycleView.setItemViewCacheSize(5);
    }

    public void showLoading() {
        if (mLoading) {
            return;
        }
        mLoading = true;
        notifyItemChanged(0);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof MessageContentHolder) {
            ((MessageContentHolder) holder).msgContentFrame.setBackground(null);
        }
    }

    public void notifyDataSourceChanged(final int type, final int value) {
        BackgroundTasks.getInstance().postDelayed(() -> {
            mLoading = false;
            if (type == MessageLayout.DATA_CHANGE_TYPE_REFRESH) {
                notifyDataSetChanged();
                mRecycleView.scrollToEnd();
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_ADD_BACK) {
                notifyItemRangeInserted(mDataSource.size() + 1, value);
                notifyDataSetChanged();
                mRecycleView.onMsgAddBack();
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_UPDATE) {
                notifyItemChanged(value + 1);
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_LOAD || type == MessageLayout.DATA_CHANGE_TYPE_ADD_FRONT) {
                //加载条目为数0，只更新动画
                if (value == 0) {
                    notifyItemChanged(0);
                } else {
                    //加载过程中有可能之前第一条与新加载的最后一条的时间间隔不超过5分钟，时间条目需去掉，所以这里的刷新要多一个条目
                    if (getItemCount() > value) {
                        notifyItemRangeInserted(0, value);
                    } else {
                        notifyItemRangeInserted(0, value);
                    }
                }
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_DELETE) {
                notifyItemRemoved(value + 1);
                notifyDataSetChanged();
            } else if (type == MessageLayout.DATA_CHANGE_SCROLL_TO_POSITION) {
                notifyDataSetChanged();
                mRecycleView.scrollToPositon(value);
                mRecycleView.setHighShowPosition(value);
            }
        }, 100);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return MSG_TYPE_HEADER_VIEW;
        }
        MessageInfo msg = getItem(position);
        return msg.getMsgType();
    }

    public void setDataSource(IChatProvider provider) {
        if (provider == null) {
            mDataSource.clear();
        } else {
            mDataSource = provider.getDataSource();
            provider.setAdapter(this);
        }
        notifyDataSourceChanged(MessageLayout.DATA_CHANGE_TYPE_REFRESH, getItemCount());
    }

    public int getLastMessagePosition(V2TIMMessage lastlastMessage) {
        int positon = 0;
        if (mDataSource == null || mDataSource.isEmpty()) {
            return positon;
        }

        for (int i =0; i < mDataSource.size(); i++) {
            if (mDataSource.get(i).getTimMessage().getMsgID() == lastlastMessage.getMsgID()) {
                positon = i;
            }
        }
        return positon + 1;
    }


    public MessageInfo getItem(int position) {
        if (position == 0 || mDataSource.size() == 0) {
            return null;
        }
        return mDataSource.get(position - 1);
    }
}