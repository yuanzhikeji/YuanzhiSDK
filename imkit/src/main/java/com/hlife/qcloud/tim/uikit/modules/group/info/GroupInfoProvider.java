package com.hlife.qcloud.tim.uikit.modules.group.info;

import android.text.TextUtils;

import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.hlife.qcloud.tim.uikit.modules.group.member.GroupMemberInfo;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMGroupApplication;
import com.tencent.imsdk.v2.V2TIMGroupApplicationResult;
import com.tencent.imsdk.v2.V2TIMGroupInfo;
import com.tencent.imsdk.v2.V2TIMGroupInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupMemberFullInfo;
import com.tencent.imsdk.v2.V2TIMGroupMemberInfoResult;
import com.tencent.imsdk.v2.V2TIMGroupMemberOperationResult;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.modules.chat.GroupChatManagerKit;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.hlife.qcloud.tim.uikit.utils.ToastUtil;
import com.work.api.open.Yz;
import com.work.api.open.model.DestroyGroupReq;
import com.work.api.open.model.GetGroupMsgReq;
import com.work.api.open.model.GetGroupMsgResp;
import com.work.util.SLog;

import java.util.ArrayList;
import java.util.List;

public class GroupInfoProvider {

    private static final int PAGE = 50;

    private GroupInfo mGroupInfo;
    private GroupMemberInfo mSelfInfo;
    private List<GroupMemberInfo> mGroupMembers = new ArrayList<>();
    private List<GroupApplyInfo> mApplyList = new ArrayList<>();

    private void reset() {
        mGroupInfo = new GroupInfo();
        mGroupMembers = new ArrayList<>();
        mSelfInfo = null;
    }

    public void loadGroupInfo(GroupInfo info) {
        mGroupInfo = info;
        mGroupMembers = info.getMemberDetails();
    }

    public void loadGroupInfo(final String groupId, final IUIKitCallBack callBack) {

        reset();

        // ??????????????????????????????
        loadGroupPublicInfo(groupId, false,new IUIKitCallBack() {
            @Override
            public void onSuccess(Object data) {

                // ???????????????????????????????????????????????????
                mGroupInfo.covertTIMGroupDetailInfo((V2TIMGroupInfoResult) data);

                // ???????????????????????????
                boolean isTop = ConversationManagerKit.getInstance().isTopConversation(groupId);
                mGroupInfo.setTopChat(isTop);

                // ?????????????????????
                loadGroupMembers(0, callBack);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                SLog.e("loadGroupPublicInfo failed, code: " + errCode + "|desc: " + errMsg);
                if (callBack != null) {
                    callBack.onError(module, errCode, errMsg);
                }
            }
        });
    }

    public void deleteGroup(final IUIKitCallBack callBack) {
//        V2TIMManager.getInstance().dismissGroup(mGroupInfo.getId(), new V2TIMCallback() {
//            @Override
//            public void onError(int code, String desc) {
//                callBack.onError("SLog",code, desc);
//                SLog.e("deleteGroup failed, code: " + code + "|desc: " + desc);
//            }
//
//            @Override
//            public void onSuccess() {
//                callBack.onSuccess(null);
//                ConversationManagerKit.getInstance().deleteConversation(mGroupInfo.getId(), true);
//                GroupChatManagerKit.getInstance().onGroupForceExit();
//            }
//        });
        DestroyGroupReq destroyGroupReq = new DestroyGroupReq();
        destroyGroupReq.GroupId = mGroupInfo.getId();
        Yz.getSession().destroyGroup(destroyGroupReq, new OnResultDataListener() {
            @Override
            public void onResult(RequestWork req, ResponseWork resp) {
                if(resp.isSuccess()){
                    callBack.onSuccess(null);
                    ConversationManagerKit.getInstance().deleteConversation(mGroupInfo.getId(), true);
                    GroupChatManagerKit.getInstance().onGroupForceExit();
                }else{
                    callBack.onError("SLog",-1, resp.getMessage());
//                SLog.e("deleteGroup failed, code: " + code + "|desc: " + desc);
                }
            }
        });
    }
    public void loadGroupPublicInfo(String groupId, final IUIKitCallBack callBack) {
        loadGroupPublicInfo(groupId,false,callBack);
    }

    public void loadGroupPublicInfo(String groupId,boolean selfService, final IUIKitCallBack callBack) {
        List<String> groupList = new ArrayList<>();
        groupList.add(groupId);
        if(selfService){
            GetGroupMsgReq getGroupMsgReq = new GetGroupMsgReq();
            getGroupMsgReq.GroupIdList = groupList;
            Yz.getSession().getGroupMsg(getGroupMsgReq, new OnResultDataListener() {
                @Override
                public void onResult(RequestWork req, ResponseWork resp) {
                    if(resp.isSuccess() && resp instanceof GetGroupMsgResp){
                        if(TextUtils.isEmpty(((GetGroupMsgResp) resp).getData().Name)){
                            callBack.onSuccess(((GetGroupMsgResp) resp).getData().GroupInfo.get(0));
                        }else{
                            callBack.onSuccess(((GetGroupMsgResp) resp).getData().Name);
                        }
                    }
                }
            });
        }else{
            V2TIMManager.getGroupManager().getGroupsInfo(groupList, new V2TIMValueCallback<List<V2TIMGroupInfoResult>>() {
                @Override
                public void onError(int code, String desc) {
                    SLog.e("loadGroupPublicInfo failed, code: " + code + "|desc: " + desc);
                    callBack.onError("SLog",code, desc);
                }

                @Override
                public void onSuccess(List<V2TIMGroupInfoResult> v2TIMGroupInfoResults) {
                    if (v2TIMGroupInfoResults.size() > 0) {
                        V2TIMGroupInfoResult infoResult = v2TIMGroupInfoResults.get(0);
                        // TODO toString??????
                        callBack.onSuccess(infoResult);
                    }
                }
            });
        }
    }

    public void loadGroupMembers(final long nextSeq, final IUIKitCallBack callBack) {
        V2TIMManager.getGroupManager().getGroupMemberList(mGroupInfo.getId(), V2TIMGroupMemberFullInfo.V2TIM_GROUP_MEMBER_FILTER_ALL, nextSeq, new V2TIMValueCallback<V2TIMGroupMemberInfoResult>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("loadGroupMembers failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess(V2TIMGroupMemberInfoResult v2TIMGroupMemberInfoResult) {
                List<GroupMemberInfo> members = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupMemberInfoResult.getMemberInfoList().size(); i++) {
                    GroupMemberInfo member = new GroupMemberInfo();
                    members.add(member.covertTIMGroupMemberInfo(v2TIMGroupMemberInfoResult.getMemberInfoList().get(i)));
                }
                if(nextSeq==0){
                    mGroupMembers.clear();
                }
                mGroupMembers.addAll(members);
                mGroupInfo.setMemberDetails(mGroupMembers);
                if (v2TIMGroupMemberInfoResult.getNextSeq() != 0){
                    loadGroupMembers(v2TIMGroupMemberInfoResult.getNextSeq(), callBack);
                } else {
                    callBack.onSuccess(mGroupInfo);
                }
            }
        });
    }

    public List<GroupMemberInfo> getGroupMembers() {
        return mGroupMembers;
    }

    public void modifyGroupInfo(final Object value, final int type, final IUIKitCallBack callBack) {
        V2TIMGroupInfo v2TIMGroupInfo = new V2TIMGroupInfo();
        v2TIMGroupInfo.setGroupID(mGroupInfo.getId());
        if (type == IMKitConstants.Group.MODIFY_GROUP_NAME) {
            v2TIMGroupInfo.setGroupName(value.toString());
        } else if (type == IMKitConstants.Group.MODIFY_GROUP_NOTICE) {
            v2TIMGroupInfo.setNotification(value.toString());
        } else if (type == IMKitConstants.Group.MODIFY_GROUP_JOIN_TYPE) {
            v2TIMGroupInfo.setGroupAddOpt((Integer)value);
        }
        V2TIMManager.getGroupManager().setGroupInfo(v2TIMGroupInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.i("modifyGroupInfo faild tyep| value| code| desc " + value + ":" + type + ":" + code + ":" + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess() {
                if (type == IMKitConstants.Group.MODIFY_GROUP_NAME) {
                    mGroupInfo.setGroupName(value.toString());
                } else if (type == IMKitConstants.Group.MODIFY_GROUP_NOTICE) {
                    mGroupInfo.setNotice(value.toString());
                } else if (type == IMKitConstants.Group.MODIFY_GROUP_JOIN_TYPE) {
                    mGroupInfo.setJoinType((Integer) value);
                }
                callBack.onSuccess(value);
            }
        });
    }

    public void modifyMyGroupNickname(final String nickname, final IUIKitCallBack callBack) {
        if (mGroupInfo == null) {
            ToastUtil.toastLongMessage("modifyMyGroupNickname fail: NO GROUP");
        }

        V2TIMGroupMemberFullInfo v2TIMGroupMemberFullInfo = new V2TIMGroupMemberFullInfo();
        v2TIMGroupMemberFullInfo.setUserID(V2TIMManager.getInstance().getLoginUser());
        v2TIMGroupMemberFullInfo.setNameCard(nickname);
        V2TIMManager.getGroupManager().setGroupMemberInfo(mGroupInfo.getId(), v2TIMGroupMemberFullInfo, new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                callBack.onError("SLog",code, desc);
                ToastUtil.toastLongMessage("modifyMyGroupNickname fail: " + code + "=" + desc);
            }

            @Override
            public void onSuccess() {
                callBack.onSuccess(null);
            }
        });
    }

    public GroupMemberInfo getSelfGroupInfo() {
        if (mSelfInfo != null) {
            return mSelfInfo;
        }
        for (int i = 0; i < mGroupMembers.size(); i++) {
            GroupMemberInfo memberInfo = mGroupMembers.get(i);
            if (TextUtils.equals(memberInfo.getAccount(), V2TIMManager.getInstance().getLoginUser())) {
                mSelfInfo = memberInfo;
                return memberInfo;
            }

        }
        return null;
    }

    public void setTopConversation(boolean flag, IUIKitCallBack callBack) {
        ConversationManagerKit.getInstance().setConversationTop(mGroupInfo.getId(), flag, callBack);
    }

    public void quitGroup(final IUIKitCallBack callBack) {
        V2TIMManager.getInstance().quitGroup(mGroupInfo.getId(), new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("quitGroup failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess() {
                ConversationManagerKit.getInstance().deleteConversation(mGroupInfo.getId(), true);
                GroupChatManagerKit.getInstance().onGroupForceExit();
                callBack.onSuccess(null);
                reset();
            }
        });
    }

    public void inviteGroupMembers(List<String> addMembers, final IUIKitCallBack callBack) {
        if (addMembers == null || addMembers.size() == 0) {
            return;
        }

        V2TIMManager.getGroupManager().inviteUserToGroup(mGroupInfo.getId(), addMembers, new V2TIMValueCallback<List<V2TIMGroupMemberOperationResult>>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("addGroupMembers failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess(List<V2TIMGroupMemberOperationResult> v2TIMGroupMemberOperationResults) {
                final List<String> adds = new ArrayList<>();
                if (v2TIMGroupMemberOperationResults.size() > 0) {
                    for (int i = 0; i < v2TIMGroupMemberOperationResults.size(); i++) {
                        V2TIMGroupMemberOperationResult res = v2TIMGroupMemberOperationResults.get(i);
                        if (res.getResult() == V2TIMGroupMemberOperationResult.OPERATION_RESULT_PENDING) {
                            callBack.onSuccess("?????????????????????????????????");
                            return;
                        }
                        if (res.getResult() > 0) {
                            adds.add(res.getMemberID());
                        }
                    }
                }
                if (adds.size() > 0) {
                    loadGroupMembers(0, callBack);
                }
            }
        });
    }

    public void removeGroupMembers(List<GroupMemberInfo> delMembers, final IUIKitCallBack callBack) {
        if (delMembers == null || delMembers.size() == 0) {
            return;
        }
        List<String> members = new ArrayList<>();
        for (int i = 0; i < delMembers.size(); i++) {
            members.add(delMembers.get(i).getAccount());
        }

        V2TIMManager.getGroupManager().kickGroupMember(mGroupInfo.getId(), members, "", new V2TIMValueCallback<List<V2TIMGroupMemberOperationResult>>() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("removeGroupMembers failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess(List<V2TIMGroupMemberOperationResult> v2TIMGroupMemberOperationResults) {
                List<String> dels = new ArrayList<>();
                for (int i = 0; i < v2TIMGroupMemberOperationResults.size(); i++) {
                    V2TIMGroupMemberOperationResult res = v2TIMGroupMemberOperationResults.get(i);
                    if (res.getResult() == V2TIMGroupMemberOperationResult.OPERATION_RESULT_SUCC) {
                        dels.add(res.getMemberID());
                    }
                }

                for (int i = 0; i < dels.size(); i++) {
                    for (int j = mGroupMembers.size() - 1; j >= 0; j--) {
                        if (mGroupMembers.get(j).getAccount().equals(dels.get(i))) {
                            mGroupMembers.remove(j);
                            break;
                        }
                    }
                }
                mGroupInfo.setMemberDetails(mGroupMembers);
                callBack.onSuccess(dels);
            }
        });
    }

    public List<GroupApplyInfo> getApplyList() {
        return mApplyList;
    }

    public void loadGroupApplies(final IUIKitCallBack callBack) {
        loadApplyInfo(new IUIKitCallBack() {

            @Override
            public void onSuccess(Object data) {
                if (mGroupInfo == null) {
                    callBack.onError("SLog",0, "no groupInfo");
                    return;
                }
                String groupId = mGroupInfo.getId();
                List<GroupApplyInfo> allApplies = (List<GroupApplyInfo>) data;
                List<GroupApplyInfo> applyInfos = new ArrayList<>();
                for (int i = 0; i < allApplies.size(); i++) {
                    GroupApplyInfo applyInfo = allApplies.get(i);
                    if (groupId.equals(applyInfo.getGroupApplication().getGroupID())
                            && applyInfo.getGroupApplication().getHandleStatus() == V2TIMGroupApplication.V2TIM_GROUP_APPLICATION_HANDLE_STATUS_UNHANDLED) {
                        applyInfos.add(applyInfo);
                    }
                }
                mApplyList = applyInfos;
                callBack.onSuccess(applyInfos);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                SLog.e("loadApplyInfo failed, code: " + errCode + "|desc: " + errMsg);
                callBack.onError(module, errCode, errMsg);
            }
        });
    }

    private void loadApplyInfo(final IUIKitCallBack callBack) {
        final List<GroupApplyInfo> applies = new ArrayList<>();
        V2TIMManager.getGroupManager().getGroupApplicationList(new V2TIMValueCallback<V2TIMGroupApplicationResult>() {
            @Override
            public void onError(int code, String desc) {
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess(V2TIMGroupApplicationResult v2TIMGroupApplicationResult) {
                List<V2TIMGroupApplication> v2TIMGroupApplicationList = v2TIMGroupApplicationResult.getGroupApplicationList();
                for (int i = 0; i < v2TIMGroupApplicationList.size(); i++) {
                    GroupApplyInfo info = new GroupApplyInfo(v2TIMGroupApplicationList.get(i));
                    info.setStatus(0);
                    applies.add(info);
                }
                callBack.onSuccess(applies);
            }
        });
    }

    public void acceptApply(final GroupApplyInfo item, final IUIKitCallBack callBack) {
        V2TIMManager.getGroupManager().acceptGroupApplication(item.getGroupApplication(), "", new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("acceptApply failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess() {
                item.setStatus(GroupApplyInfo.APPLIED);
                callBack.onSuccess(null);
                ConversationManagerKit.getInstance().updateJoinGroup();
            }
        });
    }

    public void refuseApply(final GroupApplyInfo item, final IUIKitCallBack callBack) {
        V2TIMManager.getGroupManager().refuseGroupApplication(item.getGroupApplication(), "", new V2TIMCallback() {
            @Override
            public void onError(int code, String desc) {
                SLog.e("refuseApply failed, code: " + code + "|desc: " + desc);
                callBack.onError("SLog",code, desc);
            }

            @Override
            public void onSuccess() {
                item.setStatus(GroupApplyInfo.REFUSED);
                callBack.onSuccess(null);
                ConversationManagerKit.getInstance().updateJoinGroup();
            }
        });
    }
}
