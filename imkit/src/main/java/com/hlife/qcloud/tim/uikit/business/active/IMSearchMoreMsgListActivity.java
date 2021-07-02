package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.adapter.SearchMoreMsgAdapter;
import com.hlife.qcloud.tim.uikit.business.inter.YzSearchMessageListener;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.business.modal.SearchParam;
import com.hlife.qcloud.tim.uikit.business.view.PageRecycleView;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationIconView;
import com.hlife.qcloud.tim.uikit.utils.SearchDataUtils;
import com.hlife.qcloud.tim.uikit.utils.TUIKitConstants;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.work.util.SLog;
import com.work.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListAdapter.mItemAvatarRadius;

public class IMSearchMoreMsgListActivity extends BaseActivity {

    private EditText mEdtSearch;
    private PageRecycleView mMessageRcSearch;
    private SearchMoreMsgAdapter mMessageRcSearchAdapter;

    private RelativeLayout mMessageLayout;
    private RelativeLayout mConversationLayout;
    private ConversationIconView mConversationIcon;
    private TextView mConversationTitle;


    private String mKeyWords;
    private String mConversationId;
    private SearchDataMessage mSearchDataMessage;
    private int pageIndex = 0;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        mEdtSearch = findViewById(R.id.search);
        mMessageRcSearch = findViewById(R.id.message_rc_search);
        mMessageRcSearch.setLayoutManager(new LinearLayoutManager(this));
        mMessageRcSearch.setNestedScrollingEnabled(true);
        mConversationLayout = findViewById(R.id.conversation_layout);
        mConversationIcon = findViewById(R.id.icon_conversation);
        mConversationTitle = findViewById(R.id.conversation_title);
        mMessageLayout = findViewById(R.id.message_layout);
        findViewById(R.id.cancel).setOnClickListener(view -> finish());
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        if (mMessageRcSearchAdapter == null) {
            mMessageRcSearchAdapter = new SearchMoreMsgAdapter(this);
            mMessageRcSearch.setAdapter(mMessageRcSearchAdapter);
        }
        Intent intent = getIntent();
        if (intent != null) {
            mKeyWords = intent.getStringExtra(TUIKitConstants.SEARCH_KEY_WORDS);
            mSearchDataMessage = (SearchDataMessage) intent.getSerializableExtra(TUIKitConstants.SEARCH_DATA_BEAN);
            pageIndex = 0;

            if (mSearchDataMessage != null) {
                mConversationIcon.setRadius(mItemAvatarRadius);
                mConversationIcon.setIconUrls(mSearchDataMessage.getIconUrlList());
                mConversationTitle.setText(mSearchDataMessage.getTitle());

                boolean mIsGroup = mSearchDataMessage.isGroup();
                if (mIsGroup) {
                    mConversationId = "group_" + mSearchDataMessage.getId();
                } else {
                    mConversationId = "c2c_" + mSearchDataMessage.getId();
                }
            }

            initData(mKeyWords);
            mEdtSearch.setText(mKeyWords);
            doChangeColor(mKeyWords);
        }

        setListener();
    }

    /**
     * 设置监听
     */
    private void setListener() {
        //edittext的监听
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            //每次edittext内容改变时执行 控制删除按钮的显示隐藏
            @Override
            public void afterTextChanged(Editable editable) {
                mKeyWords = editable.toString().trim();
                pageIndex = 0;
                mMessageRcSearch.setNestedScrollingEnabled(true);
                initData(mKeyWords);
                //匹配文字 变色
                doChangeColor(mKeyWords);
            }
        });
        //recyclerview的点击监听
        if (mMessageRcSearchAdapter != null) {
            mMessageRcSearchAdapter.setOnItemClickListener((view, pos) -> {
                if (mSearchDataMessage == null) {
                    return;
                }
                List<SearchDataMessage> searchDataBeans = mMessageRcSearchAdapter.getDataSource();
                if (searchDataBeans != null && pos < searchDataBeans.size()) {
                    SearchDataMessage dataMessage = searchDataBeans.get(pos);
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(mSearchDataMessage.getId());
                    chatInfo.setGroup(mSearchDataMessage.isGroup());
                    if (mSearchDataMessage.isGroup()) {
                        chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
                    } else {
                        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
                    }
                    String chatName = mSearchDataMessage.getTitle();
                    chatInfo.setChatName(chatName);
                    chatInfo.setLocateTimMessage(dataMessage.getLocateTimMessage());
                    YzIMKitAgent.instance().startChat(chatInfo,null);
                }
            });
        }

        mConversationLayout.setOnClickListener(v -> {
            if (mSearchDataMessage == null) {
                return;
            }
            ChatInfo chatInfo = new ChatInfo();
            chatInfo.setId(mSearchDataMessage.getId());
            chatInfo.setGroup(mSearchDataMessage.isGroup());
            if (mSearchDataMessage.isGroup()) {
                chatInfo.setType(V2TIMConversation.V2TIM_GROUP);
            } else {
                chatInfo.setType(V2TIMConversation.V2TIM_C2C);
            }
            String chatName = mSearchDataMessage.getTitle();
            chatInfo.setChatName(chatName);
            YzIMKitAgent.instance().startChat(chatInfo,null);
        });

        mMessageRcSearch.setLoadMoreMessageHandler(new PageRecycleView.OnLoadMoreHandler() {
            @Override
            public void loadMore() {
                final List<String> keywordList = new ArrayList<String>() {{
                    add(mKeyWords);
                }};

                searchMessage(keywordList, mConversationId, ++pageIndex);
            }

            @Override
            public boolean isListEnd(int postion) {
                if (mMessageRcSearchAdapter == null || mMessageRcSearchAdapter.getTotalCount() == 0) {
                    return true;
                }

                int totalCount = mMessageRcSearchAdapter.getTotalCount();
                int totalPage = (totalCount % SearchDataUtils.CONVERSATION_MESSAGE_PAGE_SIZE == 0) ? (totalCount / SearchDataUtils.CONVERSATION_MESSAGE_PAGE_SIZE) : (totalCount / SearchDataUtils.CONVERSATION_MESSAGE_PAGE_SIZE + 1);
                if (pageIndex < totalPage) {
                    return false;
                }

                mMessageRcSearch.setNestedScrollingEnabled(false);
                return true;
            }
        });
    }

    private void doChangeColor(String text) {
        if (text.equals("")) {
            mMessageRcSearchAdapter.setText(null);
        } else {
            //设置要变色的关键字
            mMessageRcSearchAdapter.setText(text);
        }
    }


    private void initData(final String keyWords) {
        if (keyWords == null || TextUtils.isEmpty(keyWords)){
            mMessageLayout.setVisibility(View.GONE);
            return;
        }

        final List<String> keywordList = new ArrayList<String>() {{
            add(keyWords);
        }};

        searchMessage(keywordList, mConversationId, pageIndex);
    }

    private void searchMessage(final List<String> keywordList, final String conversationID, int index) {
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword(keywordList.get(0));
        searchParam.setConversationId(conversationID);
        searchParam.setPageIndex(index);
        YzIMKitAgent.instance().searchMessage(searchParam, new YzSearchMessageListener() {
            @Override
            public void success(List<SearchDataMessage> list) {
                SLog.e("list>>"+list.size()+">"+index);
                if(index==0){
                    mMessageRcSearchAdapter.setDataSource(list);
                }else{
                    mMessageRcSearchAdapter.addDataSource(list);
                }
                mMessageLayout.setVisibility(list.size()>0?View.VISIBLE:View.GONE);
            }

            @Override
            public void error(int code, String desc) {
                ToastUtil.error(IMSearchMoreMsgListActivity.this,code+">"+desc);
            }
        });
        //search conversation
//        final V2TIMMessageSearchParam v2TIMMessageSearchParam = new V2TIMMessageSearchParam();
//        v2TIMMessageSearchParam.setKeywordList(keywordList);
//        v2TIMMessageSearchParam.setPageSize(SearchDataUtils.CONVERSATION_MESSAGE_PAGE_SIZE);
//        v2TIMMessageSearchParam.setPageIndex(index);
//        v2TIMMessageSearchParam.setConversationID(conversationID);
//        final boolean isGetByPage = index > 0;
//        V2TIMManager.getMessageManager().searchLocalMessages(v2TIMMessageSearchParam, new V2TIMValueCallback<V2TIMMessageSearchResult>() {
//            @Override
//            public void onSuccess(V2TIMMessageSearchResult v2TIMMessageSearchResult) {
//                if (!isGetByPage) {
//                    mMessageRcSearchAdapter.setDataSource(null);
//                }
//                if (v2TIMMessageSearchResult == null || v2TIMMessageSearchResult.getTotalCount() == 0 ||
//                        v2TIMMessageSearchResult.getMessageSearchResultItems() == null ||
//                        v2TIMMessageSearchResult.getMessageSearchResultItems().size() == 0) {
//                    if (!isGetByPage) {
//                        mMessageRcSearchAdapter.setDataSource(null);
//                        mConversationLayout.setVisibility(View.GONE);
//                        mMessageLayout.setVisibility(View.GONE);
//                        mMessageRcSearchAdapter.setDataSource(null);
//                        mMessageRcSearchAdapter.setTotalCount(0);
//                    }
//                    return;
//                }
//
//                mMessageRcSearchAdapter.setTotalCount(v2TIMMessageSearchResult.getTotalCount());
//                List<V2TIMMessage> v2TIMMessages = v2TIMMessageSearchResult.getMessageSearchResultItems().get(0).getMessageList();
//
//                if (!isGetByPage && (v2TIMMessages == null || v2TIMMessages.isEmpty())){
//                    mMessageRcSearchAdapter.setDataSource(null);
//                    mConversationLayout.setVisibility(View.GONE);
//                    mMessageLayout.setVisibility(View.GONE);
//                    mMessageRcSearchAdapter.setDataSource(null);
//                    mMessageRcSearchAdapter.setTotalCount(0);
//                    return;
//                }
//
//                if (v2TIMMessages != null && !v2TIMMessages.isEmpty()) {
//                    for (V2TIMMessage v2TIMMessage:v2TIMMessages) {
//                        SearchDataMessage searchDataMessage = new SearchDataMessage();
//                        String title;
//                        if (!TextUtils.isEmpty(v2TIMMessage.getFriendRemark())) {
//                            title = v2TIMMessage.getFriendRemark();
//                        } else if (!TextUtils.isEmpty(v2TIMMessage.getNameCard())) {
//                            title = v2TIMMessage.getNameCard();
//                        } else if (!TextUtils.isEmpty(v2TIMMessage.getNickName())) {
//                            title = v2TIMMessage.getNickName();
//                        } else {
//                            title = v2TIMMessage.getUserID()== null ? v2TIMMessage.getGroupID() : v2TIMMessage.getUserID();
//                        }
//                        searchDataMessage.setTitle(title);
//                        String subTitle = SearchDataUtils.getMessageText(v2TIMMessage);
//                        searchDataMessage.setSubTitle(subTitle);
//                        searchDataMessage.setLocateTimMessage(v2TIMMessage);
//                        searchDataMessage.setIconUrlList(new ArrayList(){{
//                            add(v2TIMMessage.getFaceUrl());
//                        }});
//                        mMessageSearchData.add(searchDataMessage);
//
//                    }
//
//                    mConversationLayout.setVisibility(View.VISIBLE);
//                    mMessageLayout.setVisibility(View.VISIBLE);
//                    mMessageRcSearchAdapter.setDataSource(mMessageSearchData);
//                }
//            }
//
//            @Override
//            public void onError(int code, String desc) {
//                if (!isGetByPage) {
//                    mMessageRcSearchAdapter.setDataSource(null);
//                    mConversationLayout.setVisibility(View.GONE);
//                    mMessageLayout.setVisibility(View.GONE);
//                    mMessageRcSearchAdapter.setDataSource(null);
//                    mMessageRcSearchAdapter.setTotalCount(0);
//                }
//            }
//        });
    }


    @Override
    public boolean isShowTitleBar() {
        return false;
    }
}
