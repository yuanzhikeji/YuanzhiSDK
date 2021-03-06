package com.yz.hlife.activity;

import android.content.Intent;
import android.view.View;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatHistoryMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzDeleteConversationListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.http.network.task.ObjectMapperFactory;
import com.work.api.open.Yz;
import com.work.api.open.model.CreateGroupReq;
import com.work.api.open.model.CreateGroupResp;
import com.work.api.open.model.GetTenantGroupListReq;
import com.work.api.open.model.SendGroupMessageReq;
import com.work.api.open.model.SendMessageReq;
import com.work.api.open.model.client.OpenGroupMember;
import com.work.api.open.model.client.OpenTIMElem;
import com.work.util.SLog;
import com.work.util.ToastUtil;
import com.yz.hlife.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tangyx
 * Date 2021/4/4
 * email tangyx@live.com
 */

public class DataTestActivity extends BaseActivity implements YzMessageWatcher, View.OnClickListener {
    private List<ConversationInfo> conversationInfos;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        YzIMKitAgent.instance().addMessageWatcher(this);
        findViewById(R.id.get_c2c).setOnClickListener(this);
        findViewById(R.id.get_group).setOnClickListener(this);
        findViewById(R.id.get_conversation).setOnClickListener(this);
        findViewById(R.id.get_unread).setOnClickListener(this);
        findViewById(R.id.chat_group).setOnClickListener(this);
        findViewById(R.id.chat_single).setOnClickListener(this);
        findViewById(R.id.start_chat).setOnClickListener(this);
        findViewById(R.id.send_custom).setOnClickListener(this);
        findViewById(R.id.create_group).setOnClickListener(this);
        findViewById(R.id.update_group_name).setOnClickListener(this);
        findViewById(R.id.start_auto).setOnClickListener(this);
        findViewById(R.id.add_group).setOnClickListener(this);
        findViewById(R.id.delete_group).setOnClickListener(this);
        findViewById(R.id.join_group).setOnClickListener(this);
        findViewById(R.id.send_message).setOnClickListener(this);
        findViewById(R.id.send_group_message).setOnClickListener(this);
        findViewById(R.id.chat_history).setOnClickListener(this);
        findViewById(R.id.del_conversation).setOnClickListener(this);
        findViewById(R.id.c2c_receiver_opt).setOnClickListener(this);
    }

    private void groupApplicationList(){
        YzIMKitAgent.instance().groupApplicationList(new YzGroupDataListener() {
            @Override
            public void joinMember(List<GroupApplyInfo> applies) {
                ToastUtil.info(DataTestActivity.this,"????????????"+applies.size());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YzIMKitAgent.instance().removeMessageWatcher(this);
    }

    public void loadConversation(YzChatType yzChatType){
        YzIMKitAgent.instance().loadConversation(0, yzChatType, new YzConversationDataListener() {

            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                conversationInfos = data;
//                ToastUtil.info(DataTestActivity.this,yzChatType+":"+conversationInfos.toString());
                for (ConversationInfo info:data) {
                    SLog.e(info.getTitle()+">"+info.getLastMessageTime());
                }
            }

            @Override
            public void onConversationError(int code, String desc) {

            }
        });
    }
    public void getConversation(String id){
        ToastUtil.info(this,"????????????id?????????"+id);
        YzIMKitAgent.instance().getConversation(id, new YzConversationDataListener() {

            @Override
            public void onConversationData(ConversationInfo data) {
                ToastUtil.info(DataTestActivity.this,"?????????????????????"+data);
            }

            @Override
            public void onConversationError(int code, String desc) {
                ToastUtil.error(DataTestActivity.this,code+">>"+desc);
            }
        });
    }
    public void conversationUnRead(){
        YzIMKitAgent.instance().conversationUnRead(new YzConversationDataListener() {
            @Override
            public void onUnReadCount(long singleUnRead, long groupUnRead) {
                super.onUnReadCount(singleUnRead, groupUnRead);
                SLog.e("?????????"+singleUnRead+">??????"+groupUnRead);
//                ToastUtil.info(DataTestActivity.this,"???????????????"+singleUnRead+"----???????????????"+groupUnRead);
            }
        });
    }

    @Override
    public void updateUnread(int count) {
        SLog.e("updateUnread>>>>"+count);
        conversationUnRead();
    }

    @Override
    public void updateContacts() {

    }

    @Override
    public void updateConversion() {
        SLog.e("updateConversion");
    }

    @Override
    public void updateJoinGroup() {
        this.groupApplicationList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.get_c2c:
                //loadConversation(YzChatType.C2C);
                startActivity(new Intent(this,ConversationListActivity.class).putExtra(ConversationListActivity.ConversationType,1));
                break;
            case R.id.get_group:
                //loadConversation(YzChatType.GROUP);
                startActivity(new Intent(this,ConversationListActivity.class).putExtra(ConversationListActivity.ConversationType,2));
                break;
            case R.id.get_conversation:
                getConversation("123");
                break;
            case R.id.del_conversation:
                YzIMKitAgent.instance().deleteConversation("22222", new YzDeleteConversationListener() {
                    @Override
                    public void success() {
                        ToastUtil.success(DataTestActivity.this,"????????????");
                    }

                    @Override
                    public void error(int code, String desc) {
                        ToastUtil.error(DataTestActivity.this,"???????????????"+code+">"+desc);
                    }
                });
                break;
            case R.id.get_unread:
                conversationUnRead();
                break;
            case R.id.chat_single:
                startActivity(new Intent(this,ChatDemoActivity.class).putExtra(ChatDemoActivity.class.getSimpleName(),0));
                break;
            case R.id.chat_group:
                startActivity(new Intent(this,ChatDemoActivity.class).putExtra(ChatDemoActivity.class.getSimpleName(),1));
                break;
            case R.id.start_chat:
//                if(conversationInfos!=null && conversationInfos.size()>0){
//                    ChatInfo chatInfo = new ChatInfo();
//                    chatInfo.setId(conversationInfos.get(0).getId());
//                    chatInfo.setChatName(conversationInfos.get(0).getTitle());
//                    chatInfo.setGroup(conversationInfos.get(0).isGroup());
//                    YzIMKitAgent.instance().startChat(chatInfo,null);
//                }else{
//                    ChatInfo chatInfo = new ChatInfo();
//                    chatInfo.setId("@TGS#2536TCHHD");
//                    chatInfo.setChatName("??????");
//                    chatInfo.setGroup(true);
//                    YzIMKitAgent.instance().startChat(chatInfo,null);
//                }
                ChatInfo chatInfo1 = new ChatInfo();
//                chatInfo1.setId("@TGS#2E2CYFIHJ");
                chatInfo1.setId("@TGS#2TMIYFIH2");
                chatInfo1.setChatName("?????????");
                chatInfo1.setGroup(true);
//                chatInfo1.setId("1850000000054892407893890");
//                chatInfo1.setChatName("test1test1");
//                chatInfo1.setGroup(false);
                YzIMKitAgent.instance().startChat(chatInfo1,null);
                break;
            case R.id.send_custom:
                if(conversationInfos!=null && conversationInfos.size()>0){
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(conversationInfos.get(0).getId());
                    chatInfo.setChatName(conversationInfos.get(0).getTitle());
                    YzIMKitAgent.instance().sendCustomMessage(chatInfo,"?????????????????????????????????",null);
                }
                break;
            case R.id.create_group:
                List<String> s = new ArrayList<>();
//                s.add("4624e6e2fd351a0eeaee47490997258e");
                CreateGroupReq createGroupReq = new CreateGroupReq();
//                createGroupReq.Owner_Account = "5398762486586751595398";
                createGroupReq.Owner_Account = "1850000000054892407893891";
                createGroupReq.Name = "?????????"+UserApi.instance().getNickName();
                createGroupReq.FaceUrl = "https://yzkj-pro.oss-cn-beijing.aliyuncs.com/avatar/lPto9oLiOp.jfif";
                List<OpenGroupMember> members = new ArrayList<>();
                OpenGroupMember openGroupMember = new OpenGroupMember();
//                openGroupMember.Member_Account = "4624e6e2fd351a0eeaee47490997258e";
                openGroupMember.Member_Account = "1850000000054892407893890";
                members.add(openGroupMember);
                openGroupMember = new OpenGroupMember();
                openGroupMember.Member_Account = "1850000000054892407893891";
                members.add(openGroupMember);
                createGroupReq.MemberList = members;
                Yz.getSession().createGroup(createGroupReq, new OnResultDataListener() {
                    @Override
                    public void onResult(RequestWork req, ResponseWork resp) throws Exception {
                        if(!resp.isSuccess()){
                            ToastUtil.error(DataTestActivity.this,resp.getMessage());
                        }else if(resp instanceof CreateGroupResp){
                            Yz.getSession().getTenantGroupList(new GetTenantGroupListReq(),this);
                        }
                    }
                });
                break;
            case R.id.update_group_name:
//                CreateGroupReq createGroupReq1 = new CreateGroupReq();
//                createGroupReq1.GroupId = "@TGS#2ZTF2CGHX";
//                createGroupReq1.Name = "??????????????????";
//                createGroupReq1.FaceUrl = "https://cdns4.91helife.com/groupManage/1624867591297.IMG_2950.JPG";
//                Yz.getSession().updateGroup(createGroupReq1, new OnResultDataListener() {
//                    @Override
//                    public void onResult(RequestWork req, ResponseWork resp) throws Exception {
//
//                    }
//                });
                break;
            case R.id.add_group:

//                List<String> m = new ArrayList<>();
//                m.add("2d9de88e9cd754abea89736f29132056");
//                YzIMKitAgent.instance().addGroupMember("@TGS#242ILVEH4", m, new YzGroupDataListener() {
//                    @Override
//                    public void onCreate(int code, String groupId, String msg) {
//
//                    }
//
//                    @Override
//                    public void update(int code, String msg) {
//
//                    }
//
//                    @Override
//                    public void addMember(int code, String msg) {
//                        ToastUtil.info(DataTestActivity.this,code+">"+msg);
//                    }
//
//                    @Override
//                    public void deleteMember(int code, String msg) {
//
//                    }
//
//                    @Override
//                    public void joinMember(List<GroupApplyInfo> applies) {
//
//                    }
//                });
                break;
            case  R.id.delete_group:
                List<String> mm = new ArrayList<>();
                mm.add("1234");
                CreateGroupReq createGroupReq22 = new CreateGroupReq();
                createGroupReq22.GroupId = "@TGS#2N3MJFGHM";
                createGroupReq22.MemberToDel_Account = mm;
                Yz.getSession().deleteGroupUser(createGroupReq22, new OnResultDataListener() {
                    @Override
                    public void onResult(RequestWork req, ResponseWork resp) throws Exception {

                    }
                });
                break;
            case R.id.join_group:
                startActivity(new Intent(this, GroupListDemoActivity.class));
                break;
            case R.id.start_auto:
                YzIMKitAgent.instance().startAuto();
//                startActivity(new Intent(this, GroupListActivity.class));
                break;
            case R.id.send_message:
                SendMessageReq sendMessageReq = new SendMessageReq();
                sendMessageReq.setFromUserId(UserApi.instance().getUserId());
                sendMessageReq.setToUserId("52ac0e63c55ba493dfb7134cd938fe81");
                sendMessageReq.setMsgType("TIMCustomElem");
                OpenTIMElem openTIMElem = new OpenTIMElem();
//                openTIMElem.Text = "??????api??????????????????";
                openTIMElem.Data = "??????api?????????????????????";
                sendMessageReq.setMsgContent(openTIMElem);
                Yz.getSession().sendMessage(sendMessageReq,null);
                break;
            case R.id.send_group_message:
                SendGroupMessageReq sendGroupMessageReq = new SendGroupMessageReq();
//                sendGroupMessageReq.setFromUserId("91429818885382211469142");
                sendGroupMessageReq.setGroupId("@TGS#2536TCHHD");
//                sendGroupMessageReq.setGroupId("@TGS#2Q2K37GHF");
                sendGroupMessageReq.setMsgType("TIMCustomElem");
                OpenTIMElem openTIMElem1 = new OpenTIMElem();
                openTIMElem1.Data = ObjectMapperFactory.getObjectMapper().model2JsonStr(new CustomModel());
                openTIMElem1.Desc = ObjectMapperFactory.getObjectMapper().model2JsonStr(new CustomModel());
//                openTIMElem1.Data = "{'123':'223'}";
                sendGroupMessageReq.setMsgContent(openTIMElem1);
                Yz.getSession().sendCustomGroupTextMsg(sendGroupMessageReq,null);
                break;
            case R.id.c2c_receiver_opt:
//                List<String> ids = new ArrayList<>();
//                ids.add("userId1");
//                ids.add("userId2");
//                ids.add("userId3");
//                YzIMKitAgent.instance().changeC2CReceiveMessageOpt(ids,true,new YzGroupChangeListener(){
//
//                    @Override
//                    public void success() {
//
//                    }
//
//                    @Override
//                    public void error(int code, String desc) {
//
//                    }
//                });
                break;
            case R.id.chat_history:
                if(conversationInfos == null || conversationInfos.size()==0){
                    ToastUtil.error(this,"?????????????????????");
                    return;
                }
                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setId(conversationInfos.get(0).getId());
                chatInfo.setChatName(conversationInfos.get(0).getTitle());
                chatInfo.setGroup(conversationInfos.get(0).isGroup());
                YzIMKitAgent.instance().getHistoryMessage(chatInfo, 20, null, new YzChatHistoryMessageListener() {
                    @Override
                    public void onChatMessageHistory(List<MessageInfo> messageInfos) {
                        SLog.e("messageInfos>>>>>"+messageInfos.size());
                        ToastUtil.success(DataTestActivity.this,"??????????????????????????????"+messageInfos.size());
                    }

                    @Override
                    public void onError(int code, String desc) {
                        ToastUtil.error(DataTestActivity.this,code+">>>"+desc);
                    }
                });
                break;
        }
    }
}
