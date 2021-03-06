package com.hlife.qcloud.tim.uikit.modules.group.info;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.dialog.ConfirmDialog;
import com.hlife.qcloud.tim.uikit.business.dialog.GroupJoinTypeDialog;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupChangeListener;
import com.hlife.qcloud.tim.uikit.modules.group.interfaces.IGroupMemberLayout;
import com.hlife.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;
import com.hlife.qcloud.tim.uikit.modules.group.member.GroupMemberRemindActivity;
import com.hlife.qcloud.tim.uikit.modules.group.member.IGroupMemberRouter;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.component.LineControllerView;
import com.hlife.qcloud.tim.uikit.component.SelectionActivity;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.util.SLog;
import com.work.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class GroupInfoLayout extends LinearLayout implements IGroupMemberLayout, View.OnClickListener {

    private TitleBarLayout mTitleBar;
    private LineControllerView mMemberView;
    private GroupInfoAdapter mMemberAdapter;
    private GroupInfoAdminAdapter mMemberAdminAdapter;
    private IGroupMemberRouter mMemberPreviewListener;
    private LineControllerView mGroupTypeView;
    private LineControllerView mGroupIDView;
    private LineControllerView mGroupNameView;
    private LineControllerView mGroupNotice;
    private LineControllerView mNickView;
    private LineControllerView mJoinTypeView;
    private LineControllerView mMutedSwitchView;
    private LineControllerView mRevOptView;
    private LineControllerView mTopSwitchView;
    private View mOwnerLayout;
    private LineControllerView mTransferGroupView;
    private LineControllerView mMemberAdminView;
    private GridView memberAdminList;
    private Button mDissolveBtn;

    private GroupInfo mGroupInfo;
    private GroupInfoPresenter mPresenter;
    private final ArrayList<String> mJoinTypes = new ArrayList<>();

    public GroupInfoLayout(Context context) {
        super(context);
        init();
    }

    public GroupInfoLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupInfoLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.group_info_layout, this);
        // ??????
        mTitleBar = findViewById(R.id.group_info_title_bar);
        mTitleBar.getRightGroup().setVisibility(GONE);
        mTitleBar.setTitle(getResources().getString(R.string.group_detail), TitleBarLayout.POSITION.MIDDLE);
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((Activity) getContext()).finish();
            }
        });
        // ????????????
        mMemberView = findViewById(R.id.group_member_bar);
        mMemberView.setOnClickListener(this);
        mMemberView.setCanNav(true);
        // ????????????
        GridView memberList = findViewById(R.id.group_members);
        mMemberAdapter = new GroupInfoAdapter();
        memberList.setAdapter(mMemberAdapter);
        // ??????????????????
        mGroupTypeView = findViewById(R.id.group_type_bar);
        // ???ID?????????
        mGroupIDView = findViewById(R.id.group_account);
        // ????????????
        mGroupNameView = findViewById(R.id.group_name);
        mGroupNameView.setOnClickListener(this);
        mGroupNameView.setCanNav(true);
        // ?????????
        LineControllerView mGroupIcon = findViewById(R.id.group_icon);
        mGroupIcon.setOnClickListener(this);
        mGroupIcon.setCanNav(false);
        // ?????????
        mGroupNotice = findViewById(R.id.group_notice);
        mGroupNotice.setOnClickListener(this);
        mGroupNotice.setCanNav(true);
        // ????????????
        mJoinTypeView = findViewById(R.id.join_type_bar);
        mJoinTypeView.setOnClickListener(this);
        mJoinTypeView.setCanNav(true);
        mJoinTypes.addAll(Arrays.asList(getResources().getStringArray(R.array.group_join_type)));
        // ?????????
        mNickView = findViewById(R.id.self_nickname_bar);
        mNickView.setOnClickListener(this);
        mNickView.setCanNav(true);
        //????????????
        mOwnerLayout = findViewById(R.id.group_owner_layout);
        mTransferGroupView = findViewById(R.id.transfer_group_owner);
        mTransferGroupView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                GroupMemberRemindActivity.startGroupMemberRemind(getContext(), mGroupInfo.getId(),getContext().getString(R.string.transfer_group_owner), new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object res) {
                        GroupMemberInfo memberInfo = (GroupMemberInfo) res;
                        V2TIMManager.getGroupManager().transferGroupOwner(mGroupInfo.getId(), memberInfo.getAccount(), new V2TIMCallback() {
                            @Override
                            public void onError(int i, String s) {
                                ToastUtil.error(getContext(),s);
                            }

                            @Override
                            public void onSuccess() {
                                ToastUtil.success(getContext(),"????????????");
                                ((Activity)getContext()).finish();
                            }
                        });
                    }
                });
            }
        });
        // ???????????????
        mMemberAdminView = findViewById(R.id.group_member_admin);
        memberAdminList = findViewById(R.id.group_members_admin);
        mMemberAdminAdapter = new GroupInfoAdminAdapter();
        memberAdminList.setAdapter(mMemberAdminAdapter);
        //???????????????
        mRevOptView = findViewById(R.id.rev_opt);
        mRevOptView.setCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                if(mGroupInfo==null){
                    return;
                }
                YzIMKitAgent.instance().changeReceiveMessageOpt(mGroupInfo.getId(), b, new YzGroupChangeListener() {
                    @Override
                    public void success() {
                        if(b){
                            mGroupInfo.setRevOpt(V2TIMMessage.V2TIM_RECEIVE_NOT_NOTIFY_MESSAGE);
                        }else{
                            mGroupInfo.setRevOpt(V2TIMMessage.V2TIM_RECEIVE_MESSAGE);
                        }
                    }

                    @Override
                    public void error(int code, String desc) {
                        SLog.e("changeReceiveMessageOpt:" + code + "|desc:" + desc);
                    }
                });
            }
        });
        //????????????
        mMutedSwitchView = findViewById(R.id.chat_muted);
        mMutedSwitchView.setCheckListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(mGroupInfo==null){
                    return;
                }
                V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
                v2TIMGroupInfo.setGroupID(mGroupInfo.getId());
                v2TIMGroupInfo.setAllMuted(b);
                V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        SLog.e("????????????:" + code + "|desc:" + desc);
                        ToastUtil.error(getContext(),desc);
                    }

                    @Override
                    public void onSuccess() {
//                    ToastUtil.toastLongMessage("?????????????????????");
                        SLog.e("??????????????????");
                    }
                });
            }
        });
        // ????????????
        mTopSwitchView = findViewById(R.id.chat_to_top_switch);
        mTopSwitchView.setCheckListener((buttonView, isChecked) -> mPresenter.setTopConversation(isChecked, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                buttonView.setChecked(false);
            }
        }));
        // ??????
        mDissolveBtn = findViewById(R.id.group_dissolve_button);
        mDissolveBtn.setOnClickListener(this);

        mPresenter = new GroupInfoPresenter(this);
    }

    @Override
    public void onClick(View v) {
        if (mGroupInfo == null) {
            SLog.e("mGroupInfo is NULL");
            return;
        }
        BaseActivity activity = (BaseActivity) getContext();
        if (v.getId() == R.id.group_member_bar) {
            if (mMemberPreviewListener != null) {
                mMemberPreviewListener.forwardListMember(mGroupInfo);
            }
        } else if (v.getId() == R.id.group_name) {
            if(!mGroupInfo.isOwner() && !mGroupInfo.isRole()){
                ToastUtil.info(getContext(),"?????????????????????");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(IMKitConstants.Selection.TITLE, getResources().getString(R.string.modify_group_name));
            bundle.putString(IMKitConstants.Selection.INIT_CONTENT, mGroupNameView.getContent());
            bundle.putInt(IMKitConstants.Selection.LIMIT, 10);
            SelectionActivity.startTextSelection(getContext(), bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(final Object text) {
                    mPresenter.modifyGroupName(text.toString());
                    mGroupNameView.setContent(text.toString());
                }
            });
        } else if (v.getId() == R.id.group_icon) {
            String groupUrl = String.format("https://picsum.photos/id/%d/200/200", new Random().nextInt(1000));

            V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
            v2TIMGroupInfo.setGroupID(mGroupInfo.getId());
            v2TIMGroupInfo.setFaceUrl(groupUrl);
            V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
                @Override
                public void onError(int code, String desc) {
                    SLog.e("modify group icon failed, code:" + code + "|desc:" + desc);
                }

                @Override
                public void onSuccess() {
//                    ToastUtil.toastLongMessage("?????????????????????");
                }
            });
        } else if (v.getId() == R.id.group_notice) {
            if(!mGroupInfo.isOwner() && !mGroupInfo.isRole()){
                if(!TextUtils.isEmpty(mGroupNotice.getContent())){
                    new ConfirmDialog().setContent(mGroupNotice.getContent())
                            .setHiddenCancel(true)
                            .setConfirmTextResId(R.string.label_know)
                            .show(((BaseActivity) getContext()).getSupportFragmentManager(),"notice");
                }
                ToastUtil.info(getContext(),"?????????????????????");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(IMKitConstants.Selection.TITLE, getResources().getString(R.string.modify_group_notice));
            bundle.putString(IMKitConstants.Selection.INIT_CONTENT, mGroupNotice.getContent());
            bundle.putInt(IMKitConstants.Selection.LIMIT, 200);
            SelectionActivity.startTextSelection(getContext(), bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(final Object text) {
                    mPresenter.modifyGroupNotice(text.toString());
                    mGroupNotice.setContent(text.toString());
                }
            });
        } else if (v.getId() == R.id.self_nickname_bar) {
            Bundle bundle = new Bundle();
            bundle.putString(IMKitConstants.Selection.TITLE, getResources().getString(R.string.modify_nick_name_in_goup));
            bundle.putString(IMKitConstants.Selection.INIT_CONTENT, mNickView.getContent());
            bundle.putInt(IMKitConstants.Selection.LIMIT, 6);
            SelectionActivity.startTextSelection(getContext(), bundle, new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(final Object text) {
                    mPresenter.modifyMyGroupNickname(text.toString());
                    mNickView.setContent(text.toString());
                }
            });
        } else if (v.getId() == R.id.join_type_bar) {
            if (mGroupTypeView.getContent().equals("?????????")) {
                ToastUtil.info(getContext(),"???????????????????????????????????????????????????");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(IMKitConstants.Selection.TITLE, getResources().getString(R.string.group_join_type));
            bundle.putStringArrayList(IMKitConstants.Selection.LIST, mJoinTypes);
            bundle.putInt(IMKitConstants.Selection.DEFAULT_SELECT_ITEM_INDEX, mGroupInfo.getJoinType());
//            SelectionActivity.startListSelection((Activity) getContext(), bundle, new SelectionActivity.OnResultReturnListener() {
//                @Override
//                public void onReturn(final Object text) {
//                    mPresenter.modifyGroupInfo((Integer) text, TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE);
//                    mJoinTypeView.setContent(mJoinTypes.get((Integer) text));
//
//                }
//            });
            GroupJoinTypeDialog groupJoinTypeDialog = new GroupJoinTypeDialog();
            groupJoinTypeDialog.setJoinTypeIndex(mGroupInfo.getJoinType());
            groupJoinTypeDialog.setJoinTypes(mJoinTypes);
            groupJoinTypeDialog.setOnResultReturnListener(new SelectionActivity.OnResultReturnListener() {
                @Override
                public void onReturn(Object text) {
                    mPresenter.modifyGroupInfo((Integer) text, IMKitConstants.Group.MODIFY_GROUP_JOIN_TYPE);
                    mJoinTypeView.setContent(mJoinTypes.get((Integer) text));
                }
            });
            groupJoinTypeDialog.show(activity.getSupportFragmentManager(),"join_type");
        } else if (v.getId() == R.id.group_dissolve_button) {
            if (mGroupInfo.isOwner() &&
                    (!mGroupInfo.getGroupType().equals(IMKitConstants.GroupType.TYPE_WORK)
                            || !mGroupInfo.getGroupType().equals(IMKitConstants.GroupType.TYPE_PRIVATE))) {
                new ConfirmDialog().setContent("??????????????????????").setOnConfirmListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.deleteGroup();
                    }
                }).show(activity.getSupportFragmentManager(),"del_group");
            } else {
                new ConfirmDialog().setContent("??????????????????????").setOnConfirmListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPresenter.quitGroup();
                    }
                }).show(activity.getSupportFragmentManager(),"quit_group");
            }
        }
    }

    public void setGroupId(String groupId) {
        mPresenter.loadGroupInfo(groupId, new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {
                setGroupInfo((GroupInfo) data);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {

            }
        });
    }

    private void setGroupInfo(GroupInfo info) {
        if (info == null) {
            return;
        }
        this.mGroupInfo = info;
        mGroupNameView.setContent(info.getGroupName());
        mGroupIDView.setContent(info.getId());
        mGroupNotice.setContent(info.getNotice());
        mMemberView.setContent(info.getMemberCount() + "???");
        mMemberAdapter.setDataSource(info);
        mGroupTypeView.setContent(convertGroupText(info.getGroupType()));
        mJoinTypeView.setContent(mJoinTypes.get(info.getJoinType()));
        mNickView.setContent(mPresenter.getNickName());
        mMutedSwitchView.setChecked(mGroupInfo.isMuted());
        mTopSwitchView.setChecked(mGroupInfo.isTopChat());
        mRevOptView.setChecked(mGroupInfo.isRevOpt());
        mDissolveBtn.setText(R.string.dissolve);
//            mJoinTypeView.setVisibility(VISIBLE);
        if (mGroupInfo.getGroupType().equals(IMKitConstants.GroupType.TYPE_WORK)
                || mGroupInfo.getGroupType().equals(IMKitConstants.GroupType.TYPE_PRIVATE)) {
            mDissolveBtn.setText(R.string.dissolve);
            mMutedSwitchView.setVisibility(GONE);
            if(!mGroupInfo.isOwner()){
                mOwnerLayout.setVisibility(GONE);
                mDissolveBtn.setText(R.string.exit_group);
            }
        }else if(mGroupInfo.getGroupType().equals(IMKitConstants.GroupType.TYPE_PUBLIC)){
            mMemberAdminView.setVisibility(VISIBLE);
            memberAdminList.setVisibility(VISIBLE);
            if(!mGroupInfo.isRole() && !mGroupInfo.isOwner()){
                mMutedSwitchView.setVisibility(GONE);
            }
            if(!mGroupInfo.isOwner()){
                mOwnerLayout.setVisibility(GONE);
                mDissolveBtn.setText(R.string.exit_group);
            }
            loadAdmin();
        }

//            mJoinTypeView.setVisibility(GONE);

    }

    public void loadAdmin(){
        V2TIMManager.getGroupManager().getGroupMemberList(mGroupInfo.getId(), V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ADMIN, 0, new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("loadGroupMembers failed, code: " + code + "|desc: " + desc);
            }

            @Override
            public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                List<GroupMemberInfo> members = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupMemberInfoResult.getMemberInfoList().size(); i++) {
                    GroupMemberInfo member = new GroupMemberInfo();
                    members.add(member.covertTIMGroupMemberInfo(v2TIMGroupMemberInfoResult.getMemberInfoList().get(i)));
                }
                mGroupInfo.setMemberAdminDetails(members);
                mMemberAdminView.setContent(members.size()+"???");
                mMemberAdminAdapter.setDataSource(mGroupInfo);

            }
        });
    }

    private String convertGroupText(String groupType) {
        String groupText = "";
        if (TextUtils.isEmpty(groupType)) {
            return groupText;
        }
        if (TextUtils.equals(groupType, IMKitConstants.GroupType.TYPE_PRIVATE)
                || TextUtils.equals(groupType, IMKitConstants.GroupType.TYPE_WORK)) {
            groupText = "?????????";
        } else if (TextUtils.equals(groupType, IMKitConstants.GroupType.TYPE_PUBLIC)) {
            groupText = "?????????";
        } else if (TextUtils.equals(groupType, IMKitConstants.GroupType.TYPE_CHAT_ROOM)
                || TextUtils.equals(groupType, IMKitConstants.GroupType.TYPE_MEETING)) {
            groupText = "?????????";
        }
        return groupText;
    }

    public void onGroupInfoModified(Object value, int type) {
        switch (type) {
            case IMKitConstants.Group.MODIFY_GROUP_NAME:
                ToastUtil.success(getContext(),getResources().getString(R.string.modify_group_name_success));
                mGroupNameView.setContent(value.toString());
                break;
            case IMKitConstants.Group.MODIFY_GROUP_NOTICE:
                mGroupNotice.setContent(value.toString());
                ToastUtil.success(getContext(),getResources().getString(R.string.modify_group_notice_success));
                break;
            case IMKitConstants.Group.MODIFY_GROUP_JOIN_TYPE:
                mJoinTypeView.setContent(mJoinTypes.get((Integer) value));
                break;
            case IMKitConstants.Group.MODIFY_MEMBER_NAME:
                ToastUtil.success(getContext(),getResources().getString(R.string.modify_nickname_success));
                mNickView.setContent(value.toString());
                break;
        }
    }

    public void setRouter(IGroupMemberRouter listener) {
        mMemberPreviewListener = listener;
        mMemberAdapter.setManagerCallBack(listener);
        mMemberAdminAdapter.setManagerCallBack(new IGroupMemberRouter() {
            @Override
            public void forwardListMember(GroupInfo info) {

            }

            @Override
            public void forwardItemMember(GroupMemberInfo info) {

            }

            @Override
            public void forwardAddMember(GroupInfo info) {
                GroupMemberRemindActivity.startGroupMemberRemind(getContext(), mGroupInfo.getId(), "???????????????", new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object res) {
                        GroupMemberInfo memberInfo = (GroupMemberInfo) res;
                        V2TIMManager.getGroupManager().setGroupMemberRole(mGroupInfo.getId(), memberInfo.getAccount(), V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_ADMIN, new V2TIMCallback() {
                            @Override
                            public void onError(int i, String s) {
                                SLog.e(i+">>"+s);
                                ToastUtil.error(getContext(),"?????????????????????????????????????????????");
                            }

                            @Override
                            public void onSuccess() {
                                ToastUtil.success(getContext(),"?????????????????????");
                                loadAdmin();
                            }
                        });
                    }
                });
            }

            @Override
            public void forwardDeleteMember(GroupInfo info) {
                GroupMemberRemindActivity.startGroupMemberRemind(getContext(), mGroupInfo.getId(), "???????????????",V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER, new SelectionActivity.OnResultReturnListener() {
                    @Override
                    public void onReturn(Object res) {
                        GroupMemberInfo memberInfo = (GroupMemberInfo) res;
                        V2TIMManager.getGroupManager().setGroupMemberRole(mGroupInfo.getId(), memberInfo.getAccount(), V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_ROLE_MEMBER, new V2TIMCallback() {
                            @Override
                            public void onError(int i, String s) {
                                SLog.e(i+">>"+s);
                            }

                            @Override
                            public void onSuccess() {
                                ToastUtil.success(getContext(),"?????????????????????");
                                loadAdmin();
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void setDataSource(GroupInfo dataSource) {

    }

    @Override
    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

}
