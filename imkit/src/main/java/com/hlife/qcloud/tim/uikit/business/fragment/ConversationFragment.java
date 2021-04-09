package com.hlife.qcloud.tim.uikit.business.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseFragment;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.component.action.PopActionClickListener;
import com.hlife.qcloud.tim.uikit.component.action.PopDialogAdapter;
import com.hlife.qcloud.tim.uikit.component.action.PopMenuAction;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.utils.PopWindowUtil;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.hlife.qcloud.tim.uikit.R;

import java.util.ArrayList;
import java.util.List;


public class ConversationFragment extends BaseFragment {

    private View mBaseView;
    private ConversationLayout mConversationLayout;
    private PopupWindow mConversationPopWindow;
    private final List<PopMenuAction> mConversationPopActions = new ArrayList<>();
    private ConversationListLayout.OnItemClickListener mOnItemClickListener;
    private YzChatType mType = YzChatType.ALL;

    private ConversationFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_im_conversation, container, false);
        initView();
        return mBaseView;
    }

    private void initView() {
        // 从布局文件中获取会话列表面板
        mConversationLayout = mBaseView.findViewById(R.id.conversation_layout);
        // 会话列表面板的默认UI和交互初始化
        mConversationLayout.initDefault(mType);
        // 通过API设置ConversataonLayout各种属性的样例，开发者可以打开注释，体验效果
//        ConversationLayoutHelper.customizeConversation(mConversationLayout);
        if(mOnItemClickListener!=null){
            mConversationLayout.getConversationList().setOnItemClickListener(mOnItemClickListener);
        }else{
            mConversationLayout.getConversationList().setOnItemClickListener(new ConversationListLayout.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position, ConversationInfo conversationInfo) {
                    //此处为demo的实现逻辑，更根据会话类型跳转到相关界面，开发者可根据自己的应用场景灵活实现
//                    startChatActivity(conversationInfo,null);
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setType(conversationInfo.isGroup() ? V2TIMConversation.V2TIM_GROUP : V2TIMConversation.V2TIM_C2C);
                    chatInfo.setId(conversationInfo.getId());
                    chatInfo.setChatName(conversationInfo.getTitle());
                    YzIMKitAgent.instance().startChat(chatInfo,null);
                }
            });
        }
        mConversationLayout.getConversationList().setOnItemLongClickListener(new ConversationListLayout.OnItemLongClickListener() {
            @Override
            public void OnItemLongClick(View view, int position, ConversationInfo conversationInfo) {
                startPopShow(view, position, conversationInfo);
            }
        });
        initPopMenuAction();
    }

    public void refreshData(){
        if(mConversationLayout!=null){
            mConversationLayout.initDefault(mType);
        }
    }

    private void initPopMenuAction() {

        // 设置长按conversation显示PopAction
        List<PopMenuAction> conversationPopActions = new ArrayList<PopMenuAction>();
        PopMenuAction action = new PopMenuAction();
        action.setActionName(getResources().getString(R.string.chat_top));
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                mConversationLayout.setConversationTop(position, (ConversationInfo) data);
            }
        });
        conversationPopActions.add(action);
        action = new PopMenuAction();
        action.setActionClickListener(new PopActionClickListener() {
            @Override
            public void onActionClick(int position, Object data) {
                mConversationLayout.deleteConversation(position, (ConversationInfo) data);
            }
        });
        action.setActionName(getResources().getString(R.string.chat_delete));
        conversationPopActions.add(action);
        mConversationPopActions.clear();
        mConversationPopActions.addAll(conversationPopActions);
    }

    /**
     * 长按会话item弹框
     *
     * @param index            会话序列号
     * @param conversationInfo 会话数据对象
     * @param locationX        长按时X坐标
     * @param locationY        长按时Y坐标
     */
    private void showItemPopMenu(final int index, final ConversationInfo conversationInfo, float locationX, float locationY) {
        if (mConversationPopActions == null || mConversationPopActions.size() == 0)
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
        }, 10000); // 10s后无操作自动消失
    }

    private void startPopShow(View view, int position, ConversationInfo info) {
        showItemPopMenu(position, info, view.getX(), view.getY() + view.getHeight() / 2);
    }

//    public void startChatActivity(ConversationInfo conversationInfo, ChatViewConfig config) {
//        ChatInfo chatInfo = new ChatInfo();
//        chatInfo.setType(conversationInfo.isGroup() ? V2TIMConversation.V2TIM_GROUP : V2TIMConversation.V2TIM_C2C);
//        chatInfo.setId(conversationInfo.getId());
//        chatInfo.setChatName(conversationInfo.getTitle());
//        Intent intent = new Intent(TUIKit.getAppContext(), ChatActivity.class);
//        intent.putExtra(Constants.CHAT_INFO, chatInfo);
//        intent.putExtra(Constants.CHAT_CONFIG,config);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        TUIKit.getAppContext().startActivity(intent);
//    }

    public void setType(YzChatType mType) {
        this.mType = mType;
    }

    public void setOnItemClickListener(ConversationListLayout.OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }

    public static ConversationFragment newConversation(YzChatType type){
        ConversationFragment fragment = new ConversationFragment();
        fragment.setType(type);
        return fragment;
    }
}
