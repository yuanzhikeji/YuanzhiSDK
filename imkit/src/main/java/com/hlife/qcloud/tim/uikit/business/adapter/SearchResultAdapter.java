package com.hlife.qcloud.tim.uikit.business.adapter;

import android.animation.Animator;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.hlife.qcloud.tim.uikit.utils.DateTimeUtil;
import com.work.util.SLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListAdapter.mItemAvatarRadius;


public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    /**
     * adapter传递过来的数据集合
     */
    private final List<String> list = new ArrayList<>();
    /**
     * 需要改变颜色的text
     */
    private String text;
    /**
     * 属性动画
     */
    private Animator animator;

    public static final int CONVERSATION_TYPE = 1;
    public static final int CONTACT_TYPE = 2;
    public static final int GROUP_TYPE = 3;

    private int mViewType = -1;
    private int mShowCount = 0;
    private boolean mIsShowAll = false;
    private int mTotalCount = 0;

    //data list
    private List<SearchDataMessage> mDataList;

    /**
     * 在MainActivity中设置text
     */
    public void setText(String text) {
        this.text = text;
    }

    public SearchResultAdapter(Context context) {
        this.context = context;
    }

    public int getShowCount() {
        if (mIsShowAll) {
            return mShowCount;
        } else {
            if (mShowCount <= 3) {
                return mShowCount;
            } else {
                return 3;
            }
        }
    }

    public void setShowCount(int mShowCount) {
        this.mShowCount = mShowCount;
    }

    public void setIsShowAll(boolean mIsShowAll) {
        this.mIsShowAll = mIsShowAll;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    public void setTotalCount(int mTotalCount) {
        this.mTotalCount = mTotalCount;
    }

    public void setDataSource(List<SearchDataMessage> provider, int viewType) {
        if (provider == null) {
            if (mDataList != null) {
                mDataList.clear();
                mDataList = null;
            }
            setShowCount(0);
        } else {
            mDataList = provider;
            setShowCount(mDataList.size());
        }

        mViewType = viewType;
        notifyDataSetChanged();
    }

    public List<SearchDataMessage> getDataSource() {
        return mDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ContactViewHolder holder = new ContactViewHolder(LayoutInflater.from(context).inflate(R.layout.item_im_contact_search, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
        if (contactViewHolder != null && mDataList != null && mDataList.size() > 0 && position < mDataList.size()) {
            SearchDataMessage data = mDataList.get(position);
            String title = data.getTitle();
            String subTitle = data.getSubTitle();
            // subTitle 内容为空时，title 可居中显示
            if (TextUtils.isEmpty(subTitle)) {
                RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                contactViewHolder.mTvText.setLayoutParams(layoutParams);
            } else {
                RelativeLayout.LayoutParams layoutParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                contactViewHolder.mTvText.setLayoutParams(layoutParams);
            }
            contactViewHolder.mUserIconView.setRadius(mItemAvatarRadius);
            if (data.getIconUrlList() != null) {
                contactViewHolder.mUserIconView.setIconUrls(data.getIconUrlList());
            }
            if (text != null) {
                if (mViewType == CONVERSATION_TYPE) {
                    contactViewHolder.mTvText.setText(title);

                    int isSubTextMatch = mDataList.get(position).getSubTextMatch();
                    if (isSubTextMatch == 1) {
                        SpannableString subString = matcherSearchText(Color.parseColor("#2da0f0"), subTitle, text);
                        contactViewHolder.mSubTvText.setText(subString);
                    } else {
                        contactViewHolder.mSubTvText.setText(subTitle);
                    }
                } else {
                    SpannableString string = matcherSearchText(Color.parseColor("#2da0f0"), title, text);
                    contactViewHolder.mTvText.setText(string);

                    SpannableString subString = matcherSearchText(Color.parseColor("#2da0f0"), subTitle, text);
                    contactViewHolder.mSubTvText.setText(subString);
                }
            } else {
                contactViewHolder.mTvText.setText(title);
                contactViewHolder.mSubTvText.setText(subTitle);
            }
            contactViewHolder.mLlItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(view, position);
                }
            });
            if(data.getLocateTimMessage()==null){
                contactViewHolder.mTime.setText(null);
            }else{
                contactViewHolder.mTime.setText(DateTimeUtil.getTimeFormatText(new Date(data.getMessageTime() * 1000)));
            }
        }
    }

    @Override
    public int getItemCount() {
        return getShowCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }
    /**
     * Recyclerview的点击监听接口
     */
    public interface onItemClickListener {
        void onClick(View view, int pos);
    }

    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(SearchResultAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mLlItem;
        private final ConversationIconView mUserIconView;
        private final TextView mTvText;
        private final TextView mSubTvText;
        private final TextView mTime;

        public ContactViewHolder(View itemView) {
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
