package com.hlife.qcloud.tim.uikit.modules.conversation;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.hlife.qcloud.tim.uikit.business.active.IMSearchMainActivity;
import com.work.util.SLog;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.business.MorePopWindow;
import com.hlife.qcloud.tim.uikit.business.active.ScanIMQRCodeActivity;
import com.hlife.qcloud.tim.uikit.business.active.SearchAddMoreActivity;
import com.hlife.qcloud.tim.uikit.business.active.StartGroupChatActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.component.NoticeLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.interfaces.IConversationAdapter;
import com.hlife.qcloud.tim.uikit.modules.conversation.interfaces.IConversationLayout;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyManagerActivity;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.work.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class ConversationLayout extends RelativeLayout implements IConversationLayout {

    private ConversationListLayout mConversationList;
    private NoticeLayout mNoticeLayout;
    private MorePopWindow mMenu;
    private View mSearchLayout;

    public ConversationLayout(Context context) {
        super(context);
        init();
    }

    public ConversationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConversationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化相关UI元素
     */
    private void init() {
        inflate(getContext(), R.layout.conversation_layout, this);
        EditText mSearch = findViewById(R.id.search);
        mSearch.setOnClickListener(view -> getContext().startActivity(new Intent(getContext(), IMSearchMainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        mSearchLayout = findViewById(R.id.search_layout);
        mConversationList = findViewById(R.id.conversation_list);
        mNoticeLayout = findViewById(R.id.chat_group_apply_layout);
        mNoticeLayout.setOnNoticeClickListener(view -> getContext().startActivity(new Intent(getContext(), GroupApplyManagerActivity.class)));
    }
    private IConversationAdapter adapter;

    public void setShowSearchLayout(boolean showSearchLayout) {
        if(mSearchLayout!=null){
            mSearchLayout.setVisibility(showSearchLayout?VISIBLE:GONE);
        }
    }

    public void initDefault(YzChatType type, IUIKitCallBack callBack) {
        final View mAddMore = findViewById(R.id.add_more);
        int functionPrem = YzIMKitAgent.instance().getFunctionPrem();
        if((functionPrem & 2)>0){
            mAddMore.setVisibility(VISIBLE);
            mAddMore.setOnClickListener(view -> {
                if(mMenu==null){
                    List<String> item = new ArrayList<>();
                    item.add(getContext().getResources().getString(R.string.add_friend));
                    item.add(getContext().getResources().getString(R.string.add_group));
                    item.add(getContext().getResources().getString(R.string.scan_qr_code));
                    mMenu = new MorePopWindow(getContext(),item , new BaseQuickAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                            mMenu.dismiss();
                            switch (position){
                                case 0:
                                    getContext().startActivity(new Intent(getContext(), SearchAddMoreActivity.class));
                                    break;
                                case 1:
                                    Intent intent = new Intent(getContext(), StartGroupChatActivity.class);
                                    intent.putExtra(IMKitConstants.GroupType.TYPE, IMKitConstants.GroupType.PUBLIC);
                                    getContext().startActivity(intent);
                                    break;
                                case 2:
                                    getContext().startActivity(new Intent(getContext(), ScanIMQRCodeActivity.class));
                                    break;
                            }
                        }
                    });
                }
                mMenu.showPopupWindow(mAddMore);

            });
        }else{
            mAddMore.setVisibility(GONE);
        }
        if(adapter==null){
            adapter = new ConversationListAdapter();
            mConversationList.setAdapter(adapter);
        }
        ConversationManagerKit.getInstance().loadConversation(type,new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                SLog.e("loadConversation type:"+type+">"+((ConversationProvider) data).getDataSource().size());
                adapter.setDataProvider((ConversationProvider) data);
                if(callBack!=null){
                    callBack.onSuccess(null);
                }
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                ToastUtil.error(TUIKit.getAppContext(),"加载消息失败");
            }
        });
    }

    public void updateNotice(int count){
        if(mNoticeLayout==null){
            return;
        }
        if(count==0){
            mNoticeLayout.setVisibility(GONE);
        }else{
            mNoticeLayout.setVisibility(VISIBLE);
            mNoticeLayout.getContent().setText(R.string.group_apply_un_handler);
        }
    }

    public TitleBarLayout getTitleBar() {
        return null;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

    @Override
    public ConversationListLayout getConversationList() {
        return mConversationList;
    }

    public void addConversationInfo(int position, ConversationInfo info) {
        mConversationList.getAdapter().addItem(position, info);
    }

    public void removeConversationInfo(int position) {
        mConversationList.getAdapter().removeItem(position);
    }

    @Override
    public void setConversationTop(ConversationInfo conversation, IUIKitCallBack callBack) {
        ConversationManagerKit.getInstance().setConversationTop(conversation, callBack);
    }

    @Override
    public void deleteConversation(int position, ConversationInfo conversation) {
        ConversationManagerKit.getInstance().deleteConversation(position, conversation);
    }
    @Override
    public void clearConversationMessage(int position, ConversationInfo conversation) {
        ConversationManagerKit.getInstance().clearConversationMessage(position, conversation);
    }
}
