package com.hlife.qcloud.tim.uikit.business.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.hlife.qcloud.tim.uikit.utils.DateTimeUtil;
import com.hlife.qcloud.tim.uikit.utils.SearchDataUtils;
import com.tencent.imsdk.v2.V2TIMMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListAdapter.mItemAvatarRadius;

public class SearchMoreMsgAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final Context context;
    /**
     * 需要改变颜色的text
     */
    private String text;

    //data list
    private List<SearchDataMessage> mDataList;

    private int mTotalCount = 0;

    /**
     * 在MainActivity中设置text
     */
    public void setText(String text) {
        this.text = text;
    }

    public SearchMoreMsgAdapter(Context context) {
        this.context = context;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void setTotalCount(int mTotalCount) {
        this.mTotalCount = mTotalCount;
    }

    public void setDataSource(List<SearchDataMessage> provider) {
        if (provider == null) {
            if (mDataList != null) {
                mDataList.clear();
                mDataList = null;
            }
        } else {
            mDataList = provider;
        }

        notifyDataSetChanged();
    }

    public void addDataSource(List<SearchDataMessage> provider){
        if(provider!=null && provider.size()>0){
            mDataList.addAll(provider);
            notifyDataSetChanged();
        }
    }

    public List<SearchDataMessage> getDataSource() {
        return mDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MessageViewHolder holder = new MessageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_im_contact_search, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        MessageViewHolder contactViewHolder = (MessageViewHolder) holder;
        if (contactViewHolder != null && mDataList != null && mDataList.size() > 0 && position < mDataList.size()) {
            SearchDataMessage data = mDataList.get(position);
            String title = data.getTitle();
//            if (!TextUtils.isEmpty(v2TIMMessage.getFriendRemark())) {
//                title = v2TIMMessage.getFriendRemark();
//            } else if (!TextUtils.isEmpty(v2TIMMessage.getNameCard())) {
//                title = v2TIMMessage.getNameCard();
//            } else if (!TextUtils.isEmpty(v2TIMMessage.getNickName())) {
//                title = v2TIMMessage.getNickName();
//            } else {
//                title = v2TIMMessage.getUserID()== null ? v2TIMMessage.getGroupID() : v2TIMMessage.getUserID();
//            }
            String subTitle = data.getSubTitle();
            contactViewHolder.mUserIconView.setRadius(mItemAvatarRadius);
            if (data.getIconUrlList() != null) {
                contactViewHolder.mUserIconView.setIconUrls(data.getIconUrlList());
            }
//            if (!TextUtils.isEmpty(path)) {
//                GlideEngine.loadImage(contactViewHolder.mUserIconView, path, null);
//            } else {
//                contactViewHolder.mUserIconView.setImageResource(R.drawable.default_head);
//            }
            if (text != null) {
                //设置span
                SpannableString string = matcherSearchText(Color.parseColor("#2da0f0"), title, text);
                contactViewHolder.mTvText.setText(string);

                SpannableString subString = matcherSearchText(Color.parseColor("#2da0f0"), subTitle, text);
                contactViewHolder.mSubTvText.setText(subString);
            } else {
                contactViewHolder.mTvText.setText(title);
                contactViewHolder.mSubTvText.setText(subTitle);
            }
            contactViewHolder.mLlItem.setOnClickListener(view -> onItemClickListener.onClick(view, position));
            if(data.getLocateTimMessage()==null){
                contactViewHolder.mTime.setText(null);
            }else{
                contactViewHolder.mTime.setText(DateTimeUtil.getTimeFormatText(new Date(data.getMessageTime() * 1000)));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mDataList == null) {
            return 0;
        }
        return mDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        int mViewType = -1;
        return mViewType;
    }
    /**
     * Recyclerview的点击监听接口
     */
    public interface onItemClickListener {
        void onClick(View view, int pos);
    }

    private SearchMoreMsgAdapter.onItemClickListener onItemClickListener;

    public void setOnItemClickListener(SearchMoreMsgAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mLlItem;
        private final ConversationIconView mUserIconView;
        private final TextView mTvText;
        private final TextView mSubTvText;
        private final TextView mTime;

        public MessageViewHolder(View itemView) {
            super(itemView);
            mLlItem = itemView.findViewById(R.id.ll_item);
            mUserIconView = itemView.findViewById(R.id.ivAvatar);
            mTvText = itemView.findViewById(R.id.conversation_title);
            mSubTvText = itemView.findViewById(R.id.conversation_sub_title);
            mTime = itemView.findViewById(R.id.time);
        }
    }

    /**
     * 正则匹配 返回值是一个SpannableString 即经过变色处理的数据
     */
    private SpannableString matcherSearchText(int color, String text, String keyword) {
        if (text == null || TextUtils.isEmpty(text)) {
            return SpannableString.valueOf("");
        }
        SpannableString spannableString = new SpannableString(text);
        //条件 keyword
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        //匹配
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            //ForegroundColorSpan 需要new 不然也只能是部分变色
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //返回变色处理的结果
        return spannableString;
    }
}
