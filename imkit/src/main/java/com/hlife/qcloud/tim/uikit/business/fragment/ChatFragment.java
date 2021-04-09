package com.hlife.qcloud.tim.uikit.business.fragment;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.BaseFragment;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.FriendProfileActivity;
import com.hlife.qcloud.tim.uikit.business.helper.ChatLayoutHelper;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageClickListener;
import com.hlife.qcloud.tim.uikit.component.AudioPlayer;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.ChatLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.modules.group.info.StartGroupMemberSelectActivity;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.hlife.qcloud.tim.uikit.R;
import com.tencent.imsdk.v2.V2TIMGroupAtInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.util.SLog;
import com.workstation.permission.PermissionsManager;

import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;


public class ChatFragment extends BaseFragment {
    public static String[] PERMISSIONS = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA};
    private View mBaseView;
    private ChatLayout mChatLayout;
    private ChatInfo mChatInfo;
    private ChatViewConfig mConfig;
    private YzMessageClickListener mYzMessageClickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.fragment_im_chat, container, false);
        initView();
        return mBaseView;
    }

    private void initView() {
        if(!PermissionsManager.getInstance().hasAllPermissions(getContext(),PERMISSIONS)){
            PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this, PERMISSIONS, null);
        }
        final Bundle bundle = getArguments();
        if(mChatInfo==null){
            mChatInfo = (ChatInfo) bundle.getSerializable(Constants.CHAT_INFO);
        }
        if (mChatInfo == null) {
            if(getActivity()!=null){
                getActivity().finish();
            }
            return;
        }
        //从布局文件中获取聊天面板组件
        mChatLayout = mBaseView.findViewById(R.id.chat_layout);

        //单聊组件的默认UI和交互初始化
        mChatLayout.initDefault();

        /*
         * 需要聊天的基本信息
         */
        mChatLayout.setChatInfo(mChatInfo);

        //获取单聊面板的标题栏
        TitleBarLayout mTitleBar = mChatLayout.getTitleBar();

        //单聊面板标记栏返回按钮点击事件，这里需要开发者自行控制
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(bundle.getBoolean(Constants.CHAT_TO_CONVERSATION,true)){
//                    if(MwWorkActivity.instance==null){
//                        Intent intent = new Intent(getActivity(),MwWorkActivity.class);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    }
//                }
                getActivity().finish();
            }
        });
        if (mChatInfo.getType() == V2TIMConversation.V2TIM_C2C) {
            mTitleBar.setOnRightClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChatInfo.setShowAddGroup(true);
                    Intent intent = new Intent(getContext(), FriendProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(IMKitConstants.ProfileType.CONTENT, mChatInfo);
                    startActivity(intent);
                }
            });
        }
        mChatLayout.getMessageLayout().setOnItemClickListener(new MessageLayout.OnItemClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
                //因为adapter中第一条为加载条目，位置需减1
                mChatLayout.getMessageLayout().showItemPopMenu(position - 1, messageInfo, view);
            }

            @Override
            public void onUserIconClick(View view, int position, MessageInfo messageInfo) {
                if (null == messageInfo) {
                    return;
                }
                if(mYzMessageClickListener!=null){
                    mYzMessageClickListener.onUserIconClick(messageInfo.getFromUser());
                    return;
                }
                ChatInfo info = new ChatInfo();
                info.setShowAddGroup(true);
                info.setId(messageInfo.getFromUser());
                Intent intent = new Intent(TUIKit.getAppContext(), FriendProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(IMKitConstants.ProfileType.CONTENT, info);
                TUIKit.getAppContext().startActivity(intent);
            }
        });
        mChatLayout.getInputLayout().setStartActivityListener(new InputLayout.onStartActivityListener() {
            @Override
            public void onStartGroupMemberSelectActivity() {
                if(mConfig.isCustomAtGroupMember()){
                    if(mYzMessageClickListener!=null){
                        mYzMessageClickListener.onAtGroupMember(mChatInfo.getId());
                    }
                }else{
                    Intent intent = new Intent(TUIKit.getAppContext(), StartGroupMemberSelectActivity.class);
                    GroupInfo groupInfo = new GroupInfo();
                    groupInfo.setId(mChatInfo.getId());
                    groupInfo.setChatName(mChatInfo.getChatName());
                    intent.putExtra(IMKitConstants.Group.GROUP_INFO, groupInfo);
                    startActivityForResult(intent, 1);
                }
            }

        });
        if (mChatInfo.getType() == V2TIMConversation.V2TIM_GROUP) {
            V2TIMManager.getConversationManager().getConversation(mChatInfo.getId(), new V2TIMValueCallback<V2TIMConversation>() {
                @Override
                public void onError(int i, String s) {
                    SLog.e("getConversation error:"+i+",desc:"+s);
                }

                @Override
                public void onSuccess(V2TIMConversation v2TIMConversation) {
                    if (v2TIMConversation == null){
                        SLog.d("getConversation failed");
                        return;
                    }
                    mChatInfo.setAtInfoList(v2TIMConversation.getGroupAtInfoList());

                    final V2TIMMessage lastMessage = v2TIMConversation.getLastMessage();

                    updateAtInfoLayout();
                    mChatLayout.getAtInfoLayout().setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final List<V2TIMGroupAtInfo> atInfoList = mChatInfo.getAtInfoList();
                            if (atInfoList == null || atInfoList.isEmpty()) {
                                mChatLayout.getAtInfoLayout().setVisibility(GONE);
                            } else {
                                mChatLayout.getChatManager().getAtInfoChatMessages(atInfoList.get(atInfoList.size() - 1).getSeq(), lastMessage, new IUIKitCallBack() {
                                    @Override
                                    public void onSuccess(Object data) {
                                        mChatLayout.getMessageLayout().scrollToPosition((int) atInfoList.get(atInfoList.size() - 1).getSeq());
                                        LinearLayoutManager mLayoutManager = (LinearLayoutManager) mChatLayout.getMessageLayout().getLayoutManager();
                                        mLayoutManager.scrollToPositionWithOffset((int) atInfoList.get(atInfoList.size() - 1).getSeq(), 0);

                                        atInfoList.remove(atInfoList.size() - 1);
                                        mChatInfo.setAtInfoList(atInfoList);

                                        updateAtInfoLayout();
                                    }

                                    @Override
                                    public void onError(String module, int errCode, String errMsg) {
                                        SLog.d("getAtInfoChatMessages failed");
                                    }
                                });
                            }
                        }
                    });
                }
            });
        }
    }

    private void updateAtInfoLayout(){
        int atInfoType = getAtInfoType(mChatInfo.getAtInfoList());
        switch (atInfoType){
            case V2TIMGroupAtInfo.TIM_AT_ME:
                mChatLayout.getAtInfoLayout().setVisibility(VISIBLE);
                mChatLayout.getAtInfoLayout().setText(getString(R.string.ui_at_me));
                break;
            case V2TIMGroupAtInfo.TIM_AT_ALL:
                mChatLayout.getAtInfoLayout().setVisibility(VISIBLE);
                mChatLayout.getAtInfoLayout().setText(getString(R.string.ui_at_all));
                break;
            case V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME:
                mChatLayout.getAtInfoLayout().setVisibility(VISIBLE);
                mChatLayout.getAtInfoLayout().setText(getString(R.string.ui_at_all_me));
                break;
            default:
                mChatLayout.getAtInfoLayout().setVisibility(GONE);
                break;

        }
    }

    private int getAtInfoType(List<V2TIMGroupAtInfo> atInfoList) {
        int atInfoType;
        boolean atMe = false;
        boolean atAll = false;

        if (atInfoList == null || atInfoList.isEmpty()){
            return V2TIMGroupAtInfo.TIM_AT_UNKNOWN;
        }

        for (V2TIMGroupAtInfo atInfo : atInfoList) {
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ME) {
                atMe = true;
                continue;
            }
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ALL) {
                atAll = true;
                continue;
            }
            if (atInfo.getAtType() == V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME) {
                atMe = true;
                atAll = true;
            }
        }

        if (atAll && atMe) {
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ALL_AT_ME;
        } else if (atAll) {
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ALL;
        } else if (atMe) {
            atInfoType = V2TIMGroupAtInfo.TIM_AT_ME;
        } else {
            atInfoType = V2TIMGroupAtInfo.TIM_AT_UNKNOWN;
        }

        return atInfoType;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 3) {
            String result_id = data.getStringExtra(IMKitConstants.Selection.USER_ID_SELECT);
            String result_name = data.getStringExtra(IMKitConstants.Selection.USER_NAMECARD_SELECT);
            updateInputText(result_name,result_id);
        }
    }

    public void updateInputText(String names,String ids){
        mChatLayout.getInputLayout().updateInputText(names, ids);
    }

    public void setConfig(ChatViewConfig mConfig) {
        this.mConfig = mConfig;
    }

    public void setChatInfo(ChatInfo mChatInfo) {
        this.mChatInfo = mChatInfo;
    }

    public void setYzMessageClickListener(YzMessageClickListener mYzMessageClickListener) {
        this.mYzMessageClickListener = mYzMessageClickListener;
    }

    public static ChatFragment newChatFragment(ChatInfo chatInfo, ChatViewConfig config){
        ChatFragment chatFragment = new ChatFragment();
        chatFragment.setChatInfo(chatInfo);
        chatFragment.setConfig(config);
        return chatFragment;
    }
    ChatLayoutHelper helper;
    @Override
    public void onResume() {
        super.onResume();
        // TODO 通过api设置ChatLayout各种属性的样例
        if(helper == null){
            helper = new ChatLayoutHelper(getActivity());
        }
        if(mConfig==null){
            mConfig = new ChatViewConfig();
        }
        helper.customizeChatLayout(mChatLayout,mConfig);
    }

    @Override
    public void onPause() {
        super.onPause();
        AudioPlayer.getInstance().stopPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatLayout != null) {
            mChatLayout.exitChat();
        }
    }

}
