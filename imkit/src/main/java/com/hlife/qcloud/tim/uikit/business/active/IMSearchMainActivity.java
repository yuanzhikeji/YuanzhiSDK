package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.adapter.SearchResultAdapter;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupFaceListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzSearchMessageListener;
import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;
import com.hlife.qcloud.tim.uikit.business.modal.SearchParam;
import com.hlife.qcloud.tim.uikit.business.view.PageRecycleView;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.utils.TUIKitConstants;
import com.work.util.KeyboardUtils;
import com.work.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.hlife.qcloud.tim.uikit.business.adapter.SearchResultAdapter.CONVERSATION_TYPE;

public class IMSearchMainActivity extends BaseActivity implements YzGroupFaceListener {

    private EditText mEdtSearch;

    private RelativeLayout mContactLayout;
    private RelativeLayout mGroupLayout;
    private RelativeLayout mConversationLayout;

    private RelativeLayout mMoreContactLayout;
    private RelativeLayout mMoreGroupLayout;
    private RelativeLayout mMoreConversationLayout;

    private SearchResultAdapter mContactRcSearchAdapter;
    private SearchResultAdapter mGroupRcSearchAdapter;
    private SearchResultAdapter mConversationRcSearchAdapter;

    private List<SearchDataMessage> mContactSearchData = new ArrayList<>();
    private List<SearchDataMessage> mGroupSearchData = new ArrayList<>();


    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        mEdtSearch = findViewById(R.id.search);
        RecyclerView mFriendRcSearch = findViewById(R.id.friend_rc_search);
        RecyclerView mGroupRcSearch = findViewById(R.id.group_rc_search);
        PageRecycleView mConversationRcSearch = findViewById(R.id.conversation_rc_search);
        mFriendRcSearch.setLayoutManager(new LinearLayoutManager(this));
        mGroupRcSearch.setLayoutManager(new LinearLayoutManager(this));
        mConversationRcSearch.setLayoutManager(new LinearLayoutManager(this));
        mFriendRcSearch.setNestedScrollingEnabled(false);
        mGroupRcSearch.setNestedScrollingEnabled(false);
        mConversationRcSearch.setNestedScrollingEnabled(false);
        mContactLayout = findViewById(R.id.contact_layout);
        mMoreContactLayout = findViewById(R.id.more_contact_layout);
        mGroupLayout = findViewById(R.id.group_layout);
        mMoreGroupLayout = findViewById(R.id.more_group_layout);
        mConversationLayout = findViewById(R.id.conversation_layout);
        mMoreConversationLayout = findViewById(R.id.more_conversation_layout);
        if (mContactRcSearchAdapter == null) {
            mContactRcSearchAdapter = new SearchResultAdapter(this);
            mFriendRcSearch.setAdapter(mContactRcSearchAdapter);
        }

        if (mGroupRcSearchAdapter == null) {
            mGroupRcSearchAdapter = new SearchResultAdapter(this);
            mGroupRcSearch.setAdapter(mGroupRcSearchAdapter);
        }

        if (mConversationRcSearchAdapter == null) {
            mConversationRcSearchAdapter = new SearchResultAdapter(this);
            mConversationRcSearch.setAdapter(mConversationRcSearchAdapter);
        }
        findViewById(R.id.cancel).setOnClickListener(view -> finish());
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        mMoreConversationLayout.setVisibility(View.GONE);
        ConversationManagerKit.getInstance().addGroupFaceListener(this);
        mEdtSearch.post(() -> KeyboardUtils.showSoftInput(mEdtSearch));
        setListener();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConversationManagerKit.getInstance().removeGroupFaceListener(this);
    }

    void setListener(){
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
                initData(editable.toString().trim());
                //匹配文字 变色
                doChangeColor(editable.toString().trim());
            }
        });
        if (mConversationRcSearchAdapter != null) {
            mConversationRcSearchAdapter.setOnItemClickListener((view, pos) -> {
                if (mConversationRcSearchAdapter == null){
                    return;
                }
                List<SearchDataMessage> searchDataMessages = mConversationRcSearchAdapter.getDataSource();
                if (searchDataMessages != null && pos < searchDataMessages.size()) {
                    SearchDataMessage searchDataMessage = searchDataMessages.get(pos);
                    Intent intent = new Intent(getApplicationContext(), IMSearchMoreMsgListActivity.class);
                    intent.putExtra(TUIKitConstants.SEARCH_KEY_WORDS, mEdtSearch.getText().toString().trim());
                    intent.putExtra(TUIKitConstants.SEARCH_DATA_BEAN, searchDataMessage);
                    startActivity(intent);
                }
            });
        }
    }

    private void initData(final String keyWords) {
        if (keyWords == null || TextUtils.isEmpty(keyWords)){
            mContactLayout.setVisibility(View.GONE);
            mGroupLayout.setVisibility(View.GONE);
            mConversationLayout.setVisibility(View.GONE);
            return;
        }

//
        searchConversation(keyWords);
    }
    private void searchConversation(String keyword) {
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword(keyword);
        YzIMKitAgent.instance().searchMessage(searchParam, new YzSearchMessageListener() {
            @Override
            public void success(List<SearchDataMessage> list) {
                if (list.size()==0){
                    mConversationLayout.setVisibility(View.GONE);
                    mConversationRcSearchAdapter.setDataSource(null, CONVERSATION_TYPE);
                    mConversationRcSearchAdapter.setTotalCount(0);
                    return;
                }
                mConversationLayout.setVisibility(View.VISIBLE);
                mConversationRcSearchAdapter.setDataSource(list, CONVERSATION_TYPE);
                mConversationRcSearchAdapter.setIsShowAll(true);
            }

            @Override
            public void error(int code, String desc) {
                ToastUtil.error(IMSearchMainActivity.this,code+">"+desc);
            }
        });

    }

    /**
     * 字体匹配方法
     */
    private void doChangeColor(String text) {
        if (text.equals("")) {
            //防止匹配过文字之后点击删除按钮 字体仍然变色的问题
            mContactRcSearchAdapter.setText(null);
            mGroupRcSearchAdapter.setText(null);
            mConversationRcSearchAdapter.setText(null);
        } else {
            //设置要变色的关键字
            mContactRcSearchAdapter.setText(text);
            mGroupRcSearchAdapter.setText(text);
            mConversationRcSearchAdapter.setText(text);
        }
    }

    @Override
    public boolean isShowTitleBar() {
        return false;
    }

    @Override
    public void onFaceUrl(ConversationInfo info) {
        mContactRcSearchAdapter.notifyDataSetChanged();
        mGroupRcSearchAdapter.notifyDataSetChanged();
        List<SearchDataMessage> dataBeanList = mConversationRcSearchAdapter.getDataSource();
        if(dataBeanList==null){
            return;
        }
        for (SearchDataMessage searchDataMessage :dataBeanList) {
            if(searchDataMessage.getConversationId().equals(info.getConversationId())){
                searchDataMessage.setIconUrlList(info.getIconUrlList());
                break;
            }
        }
        mConversationRcSearchAdapter.notifyDataSetChanged();
    }
}
