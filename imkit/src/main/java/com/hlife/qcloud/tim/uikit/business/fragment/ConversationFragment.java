package com.hlife.qcloud.tim.uikit.business.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseFragment;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.component.action.PopDialogAdapter;
import com.hlife.qcloud.tim.uikit.component.action.PopMenuAction;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.utils.PopWindowUtil;
import com.hlife.qcloud.tim.uikit.R;
import com.work.util.SLog;

import java.util.ArrayList;
import java.util.List;


public class ConversationFragment extends BaseFragment implements GroupChatManagerKit.GroupNotifyHandler, YzMessageWatcher {
    private final static String TYPE="type";
    private final static String SEARCH = "search";
    private View mBaseView;
    private ConversationLayout mConversationLayout;
    private PopupWindow mConversationPopWindow;
    private final List<PopMenuAction> mConversationPopActions = new ArrayList<>();
    private ConversationListLayout.OnItemClickListener mOnItemClickListener;
    private YzChatType mType = YzChatType.ALL;
    private boolean isShowSearchLayout = true;

    public ConversationFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_im_conversation, container, false);
        initView(savedInstanceState);
        return mBaseView;
    }

    private void initView(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String type = "";
        if(bundle!=null){
            type = bundle.getString(TYPE);
            this.isShowSearchLayout = bundle.getBoolean(SEARCH);
        }else if(savedInstanceState!=null){
            type = savedInstanceState.getString(TYPE);
            this.isShowSearchLayout = savedInstanceState.getBoolean(SEARCH);
        }
        if(YzChatType.C2C.name().equals(type)){
            this.mType = YzChatType.C2C;
        }else if(YzChatType.GROUP.name().equals(type)){
            this.mType = YzChatType.GROUP;
        }
        ConversationManagerKit.getInstance().addMessageWatcher(this);
        // ??????????????????????????????????????????
        mConversationLayout = mBaseView.findViewById(R.id.conversation_layout);
        // ???????????????????????????UI??????????????????
        refreshData();
        // ??????API??????ConversataonLayout??????????????????????????????????????????????????????????????????
//        ConversationLayoutHelper.customizeConversation(mConversationLayout);
        if(mOnItemClickListener!=null){
            mConversationLayout.getConversationList().setOnItemClickListener(mOnItemClickListener);
        }else{
            mConversationLayout.getConversationList().setOnItemClickListener((view, position, conversationInfo) -> {
                //?????????demo??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setGroup(conversationInfo.isGroup());
                chatInfo.setId(conversationInfo.getId());
                chatInfo.setDraft(conversationInfo.getDraft());
                chatInfo.setChatName(conversationInfo.getTitle());
                YzIMKitAgent.instance().startChat(chatInfo,null);
            });
        }
        mConversationLayout.getConversationList().setOnItemLongClickListener((view, position, conversationInfo) -> startPopShow(view, position, conversationInfo));
        initPopMenuAction();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TYPE,mType.name());
        outState.putBoolean(SEARCH,isShowSearchLayout);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    public void refreshData(){
        if(mConversationLayout!=null){
            mConversationLayout.setShowSearchLayout(isShowSearchLayout);
            mConversationLayout.initDefault(mType, new IUIKitCallBack() {
                @Override
                public void onSuccess(Object data) {
                    ConversationManagerKit.getInstance().refreshApply(ConversationFragment.this);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                }
            });
        }
    }

    private void initPopMenuAction() {

        // ????????????conversation??????PopAction
        List<PopMenuAction> conversationPopActions = new ArrayList<PopMenuAction>();
        PopMenuAction action = new PopMenuAction();
        action.setActionName(getResources().getString(R.string.chat_top));
        action.setActionClickListener((position, data) -> mConversationLayout.setConversationTop((ConversationInfo) data, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                SLog.e(module + ", Error code = " + errCode + ", desc = " + errMsg);
            }
        }));
        conversationPopActions.add(action);
        action = new PopMenuAction();
        action.setActionClickListener((position, data) -> mConversationLayout.deleteConversation(position, (ConversationInfo) data));
        action.setActionName(getResources().getString(R.string.chat_delete));
        conversationPopActions.add(action);
        mConversationPopActions.clear();
        mConversationPopActions.addAll(conversationPopActions);
    }

    /**
     * ????????????item??????
     *
     * @param index            ???????????????
     * @param conversationInfo ??????????????????
     * @param locationX        ?????????X??????
     * @param locationY        ?????????Y??????
     */
    private void showItemPopMenu(final int index, final ConversationInfo conversationInfo, float locationX, float locationY) {
        if (mConversationPopActions.size() == 0)
            return;
        View itemPop = LayoutInflater.from(getActivity()).inflate(R.layout.pop_menu_layout, null);
        ListView mConversationPopList = itemPop.findViewById(R.id.pop_menu_list);
        mConversationPopList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PopMenuAction action = mConversationPopActions.get(position);
                if (action.getActionClickListener() != null) {
                    action.getActionClickListener().onActionClick(index, conversationInfo);
                }
                mConversationPopWindow.dismiss();
            }
        });

        for (int i = 0; i < mConversationPopActions.size(); i++) {
            PopMenuAction action = mConversationPopActions.get(i);
            if (conversationInfo.isTop()) {
                if (action.getActionName().equals(getResources().getString(R.string.chat_top))) {
                    action.setActionName(getResources().getString(R.string.quit_chat_top));
                }
            } else {
                if (action.getActionName().equals(getResources().getString(R.string.quit_chat_top))) {
                    action.setActionName(getResources().getString(R.string.chat_top));
                }

            }
        }
        PopDialogAdapter mConversationPopAdapter = new PopDialogAdapter();
        mConversationPopList.setAdapter(mConversationPopAdapter);
        mConversationPopAdapter.setDataSource(mConversationPopActions);
        mConversationPopWindow = PopWindowUtil.popupWindow(itemPop, mBaseView, (int) locationX, (int) locationY);
        mBaseView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConversationPopWindow.dismiss();
            }
        }, 10000); // 10s????????????????????????
    }

    private void startPopShow(View view, int position, ConversationInfo info) {
        showItemPopMenu(position, info, view.getX(), view.getY() + view.getHeight() / 2);
    }

    public void setOnItemClickListener(ConversationListLayout.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public static ConversationFragment newConversation(YzChatType type){
        return newConversation(type,false);
    }
    public static ConversationFragment newConversation(YzChatType type,boolean showSearch){
        ConversationFragment fragment = new ConversationFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE,type.name());
        bundle.putBoolean(SEARCH,showSearch);
        fragment.setArguments(bundle);
        return fragment;
    }
    @Override
    public void onResume() {
        super.onResume();
        ConversationManagerKit.getInstance().refreshApply(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConversationManagerKit.getInstance().removeMessageWatcher(this);
    }

    @Override
    public void onGroupForceExit() {

    }

    @Override
    public void onGroupNameChanged(String newName) {

    }

    @Override
    public void onApplied(int size) {
        mConversationLayout.updateNotice(size);
    }

    @Override
    public void updateUnread(int count) {

    }

    @Override
    public void updateContacts() {

    }

    @Override
    public void updateConversion() {

    }

    @Override
    public void updateJoinGroup() {
        ConversationManagerKit.getInstance().refreshApply(this);
    }
}
