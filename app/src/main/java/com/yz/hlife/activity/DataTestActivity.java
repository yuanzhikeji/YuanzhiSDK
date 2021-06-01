package com.yz.hlife.activity;

import android.content.Intent;
import android.view.View;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatHistoryMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
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
    }

    private void groupApplicationList(){
        YzIMKitAgent.instance().groupApplicationList(new YzGroupDataListener() {
            @Override
            public void joinMember(List<GroupApplyInfo> applies) {
                ToastUtil.info(DataTestActivity.this,"群申请："+applies.size());
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
                SLog.e("conversationInfos:"+conversationInfos.size());
                ToastUtil.info(DataTestActivity.this,yzChatType+":"+conversationInfos.toString());
            }

            @Override
            public void onConversationError(int code, String desc) {

            }
        });
    }
    public void getConversation(String id){
        ToastUtil.info(this,"获取指定id会话："+id);
        YzIMKitAgent.instance().getConversation(id, new YzConversationDataListener() {

            @Override
            public void onConversationData(ConversationInfo data) {
                ToastUtil.info(DataTestActivity.this,"获取指定会话："+data);
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
                ToastUtil.info(DataTestActivity.this,"单聊未读："+singleUnRead+"----群聊未读："+groupUnRead);
            }
        });
    }

    @Override
    public void updateUnread(int count) {
        SLog.e("未读总数："+count);
        conversationUnRead();
    }

    @Override
    public void updateContacts() {

    }

    @Override
    public void updateConversion() {

    }

    @Override
    public void updateJoinGroup() {
        this.groupApplicationList();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.get_c2c:
                loadConversation(YzChatType.C2C);
                break;
            case R.id.get_group:
                loadConversation(YzChatType.GROUP);
                break;
            case R.id.get_conversation:
                getConversation("123");
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

                if(conversationInfos!=null && conversationInfos.size()>0){
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(conversationInfos.get(0).getId());
                    chatInfo.setChatName(conversationInfos.get(0).getTitle());
                    chatInfo.setGroup(conversationInfos.get(0).isGroup());
                    YzIMKitAgent.instance().startChat(chatInfo,null);
                }else{
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId("2124128975129831111");
                    chatInfo.setChatName("群");
                    chatInfo.setGroup(true);
                    YzIMKitAgent.instance().startChat(chatInfo,null);
                }
                break;
            case R.id.send_custom:
                if(conversationInfos!=null && conversationInfos.size()>0){
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(conversationInfos.get(0).getId());
                    chatInfo.setChatName(conversationInfos.get(0).getTitle());
                    YzIMKitAgent.instance().sendCustomMessage(chatInfo,"这是自己定义的消息内容",null);
                }
                break;
            case R.id.create_group:
                List<String> s = new ArrayList<>();
//                s.add("4624e6e2fd351a0eeaee47490997258e");
                CreateGroupReq createGroupReq = new CreateGroupReq();
                createGroupReq.Owner_Account = UserApi.instance().getUserId();
                createGroupReq.Name = "测试群";
                createGroupReq.FaceUrl = "https://yzkj-pro.oss-cn-beijing.aliyuncs.com/avatar/lPto9oLiOp.jfif";
                List<OpenGroupMember> members = new ArrayList<>();
                OpenGroupMember openGroupMember = new OpenGroupMember();
                openGroupMember.Member_Account = "4624e6e2fd351a0eeaee47490997258e";
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
//                YzIMKitAgent.instance().updateGroup("@TGS#242ILVEH4", "我改群名字", new YzGroupDataListener() {
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
//
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
            case R.id.add_group:
                List<String> m = new ArrayList<>();
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
                mm.add("2d9de88e9cd754abea89736f29132056");
//                YzIMKitAgent.instance().deleteGroupMember("@TGS#242ILVEH4", mm, new YzGroupDataListener() {
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
//
//                    }
//
//                    @Override
//                    public void deleteMember(int code, String msg) {
//                        ToastUtil.info(DataTestActivity.this,code+">"+msg);
//                    }
//
//                    @Override
//                    public void joinMember(List<GroupApplyInfo> applies) {
//
//                    }
//                });
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
//                openTIMElem.Text = "我是api发的文本消息";
                openTIMElem.Data = "我是api发的自定义消息";
                sendMessageReq.setMsgContent(openTIMElem);
                Yz.getSession().sendMessage(sendMessageReq,null);
                break;
            case R.id.send_group_message:
                SendGroupMessageReq sendGroupMessageReq = new SendGroupMessageReq();
                sendGroupMessageReq.setFromUserId("52ac0e63c55ba493dfb7134cd938fe81");
                sendGroupMessageReq.setGroupId("@TGS#2XM253EHK");
                sendGroupMessageReq.setMsgType("TIMTextElem");
                OpenTIMElem openTIMElem1 = new OpenTIMElem();
                openTIMElem1.Data = "我是api发的自定义群组消息";
                sendGroupMessageReq.setMsgContent(openTIMElem1);
                Yz.getSession().sendCustomGroupTextMsg(sendGroupMessageReq,null);
                break;
            case R.id.chat_history:
                if(conversationInfos == null || conversationInfos.size()==0){
                    ToastUtil.error(this,"点击第一个按钮");
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
                        ToastUtil.success(DataTestActivity.this,"获取到会话内容数量："+messageInfos.size());
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
