package com.hlife.qcloud.tim.uikit.modules.conversation;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.holder.ConversationBaseHolder;
import com.hlife.qcloud.tim.uikit.modules.conversation.holder.ConversationCommonHolder;
import com.hlife.qcloud.tim.uikit.modules.conversation.holder.ConversationCustomHolder;
import com.hlife.qcloud.tim.uikit.modules.conversation.interfaces.IConversationAdapter;
import com.hlife.qcloud.tim.uikit.modules.conversation.interfaces.IConversationProvider;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

public class ConversationListAdapter extends IConversationAdapter {
    public static int mItemAvatarRadius = ScreenUtil.getPxByDp(50);
    private boolean mHasShowUnreadDot = true;
    private int mTopTextSize;
    private int mBottomTextSize;
    private int mDateTextSize;
    private List<ConversationInfo> mDataSource = new ArrayList<>();
    private ConversationListLayout.OnItemClickListener mOnItemClickListener;
    private ConversationListLayout.OnItemLongClickListener mOnItemLongClickListener;

    public ConversationListAdapter() {

    }

    public void setOnItemClickListener(ConversationListLayout.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(ConversationListLayout.OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setDataProvider(IConversationProvider provider) {
        mDataSource = provider.getDataSource();
        if (provider instanceof ConversationProvider) {
            provider.attachAdapter(this);
        }
        notifyDataSetChanged();
    }

    @Override
    public void clearData() {
        if(mDataSource!=null){
            mDataSource.clear();
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(TUIKit.getAppContext());
        ConversationBaseHolder holder;
        // ??????????????? ViewHolder
        View view;
        // ??????ViewType???????????????
        if (viewType == ConversationInfo.TYPE_CUSTOM) {
            view = inflater.inflate(R.layout.conversation_custom_adapter, parent, false);
            holder = new ConversationCustomHolder(view);
        } else {
            view = inflater.inflate(R.layout.conversation_adapter, parent, false);
            holder = new ConversationCommonHolder(view);
        }
        holder.setAdapter(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ConversationInfo conversationInfo = getItem(position);
        ConversationBaseHolder baseHolder = (ConversationBaseHolder) holder;
        if (getItemViewType(position) == ConversationInfo.TYPE_CUSTOM) {

        } else {//???????????????????????????
            if (mOnItemClickListener != null) {
                holder.itemView.setOnClickListener(view -> mOnItemClickListener.onItemClick(view, position, conversationInfo));
            }
            if (mOnItemLongClickListener != null) {
                holder.itemView.setOnLongClickListener(view -> {
                    mOnItemLongClickListener.OnItemLongClick(view, position, conversationInfo);
                    return true;
                });
            }
        }
        baseHolder.layoutViews(conversationInfo, position);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof ConversationCommonHolder) {
            ((ConversationCommonHolder) holder).conversationIconView.setBackground(null);
        }
    }

    public ConversationInfo getItem(int position) {
        if (mDataSource.size() == 0)
            return null;
        return mDataSource.get(position);
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mDataSource != null) {
            ConversationInfo conversation = mDataSource.get(position);
            return conversation.getType();
        }
        return 1;
    }

    public void addItem(int position, ConversationInfo info) {
        mDataSource.add(position, info);
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mDataSource.remove(position);
        notifyItemRemoved(position);
        notifyDataSetChanged();
    }

    public void notifyDataSourceChanged(String info) {
        for (int i = 0; i < mDataSource.size(); i++) {
            if (TextUtils.equals(info, mDataSource.get(i).getConversationId())) {
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void setItemTopTextSize(int size) {
        mTopTextSize = size;
    }

    public int getItemTopTextSize() {
        return mTopTextSize;
    }

    public void setItemBottomTextSize(int size) {
        mBottomTextSize = size;
    }

    public int getItemBottomTextSize() {
        return mBottomTextSize;
    }

    public void setItemDateTextSize(int size) {
        mDateTextSize = size;
    }

    public int getItemDateTextSize() {
        return mDateTextSize;
    }

    public void setItemAvatarRadius(int radius) {
        mItemAvatarRadius = radius;
    }

    public int getItemAvatarRadius() {
        return mItemAvatarRadius;
    }

    public void disableItemUnreadDot(boolean flag) {
        mHasShowUnreadDot = !flag;
    }

    public boolean hasItemUnreadDot() {
        return mHasShowUnreadDot;
    }
}
