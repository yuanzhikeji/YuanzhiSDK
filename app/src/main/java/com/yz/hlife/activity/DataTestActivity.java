package com.yz.hlife.activity;

import android.content.Intent;
import android.view.View;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.modules.group.apply.GroupApplyInfo;
import com.tencent.imsdk.v2.V2TIMCallback;
import com.tencent.imsdk.v2.V2TIMManager;
import com.work.api.open.Yz;
import com.work.api.open.model.GetTenantGroupListReq;
import com.work.api.open.model.SendMessageReq;
import com.work.api.open.model.SysUserReq;
import com.work.api.open.model.client.OpenGroupInfo;
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
        SysUserReq sysUserReq = new SysUserReq();
        sysUserReq.setNickName("试试2233");
        YzIMKitAgent.instance().register(sysUserReq,null);
        this.groupApplicationList();
        Yz.getSession().getTenantGroupList(new GetTenantGroupListReq(),this);
    }

    private void groupApplicationList(){
        YzIMKitAgent.instance().groupApplicationList(new YzGroupDataListener() {
            @Override
            public void onCreate(int code, String groupId, String msg) {

            }

            @Override
            public void update(int code, String msg) {

            }

            @Override
            public void addMember(int code, String msg) {

            }

            @Override
            public void deleteMember(int code, String msg) {

            }

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
                ToastUtil.info(DataTestActivity.this,"获取指定会话："+data.toString());
            }

            @Override
            public void onConversationError(int code, String desc) {

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
                if(conversationInfos!=null && conversationInfos.size()>0){
                    getConversation(conversationInfos.get(0).getId());
                }
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
                }
                break;
            case R.id.send_custom:
                if(conversationInfos!=null && conversationInfos.size()>0){
                    ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setId(conversationInfos.get(0).getId());
                    chatInfo.setChatName(conversationInfos.get(0).getTitle());
                    YzIMKitAgent.instance().startCustomMessage(chatInfo,"这是自己定义的消息内容");
                }
                break;
            case R.id.create_group:
                List<String> s = new ArrayList<>();
                s.add("4624e6e2fd351a0eeaee47490997258e");
                YzIMKitAgent.instance().createPublicGroup(UserApi.instance().getUserId(), "这是一个公开群",s, new YzGroupDataListener() {
                    @Override
                    public void onCreate(int code, String groupId, String msg) {
                        if(code==200){
                            SLog.e("发送一个消息过去。。。");
                            ChatInfo chatInfo = new ChatInfo();
                            chatInfo.setId(groupId);
                            chatInfo.setChatName("这是一个公开群");
                            chatInfo.setGroup(true);
                            YzIMKitAgent.instance().startCustomMessage(chatInfo,"这是自己定义的消息内容");
                        }
                    }

                    @Override
                    public void update(int code, String msg) {

                    }

                    @Override
                    public void addMember(int code, String msg) {

                    }

                    @Override
                    public void deleteMember(int code, String msg) {

                    }

                    @Override
                    public void joinMember(List<GroupApplyInfo> applies) {

                    }
                });
                break;
            case R.id.update_group_name:
                YzIMKitAgent.instance().updateGroup("@TGS#242ILVEH4", "我改群名字", new YzGroupDataListener() {
                    @Override
                    public void onCreate(int code, String groupId, String msg) {

                    }

                    @Override
                    public void update(int code, String msg) {

                    }

                    @Override
                    public void addMember(int code, String msg) {

                    }

                    @Override
                    public void deleteMember(int code, String msg) {

                    }

                    @Override
                    public void joinMember(List<GroupApplyInfo> applies) {

                    }
                });
                break;
            case R.id.add_group:
                List<String> m = new ArrayList<>();
                m.add("2d9de88e9cd754abea89736f29132056");
                YzIMKitAgent.instance().addGroupMember("@TGS#242ILVEH4", m, new YzGroupDataListener() {
                    @Override
                    public void onCreate(int code, String groupId, String msg) {

                    }

                    @Override
                    public void update(int code, String msg) {

                    }

                    @Override
                    public void addMember(int code, String msg) {
                        ToastUtil.info(DataTestActivity.this,code+">"+msg);
                    }

                    @Override
                    public void deleteMember(int code, String msg) {

                    }

                    @Override
                    public void joinMember(List<GroupApplyInfo> applies) {

                    }
                });
                break;
            case  R.id.delete_group:
                List<String> mm = new ArrayList<>();
                mm.add("2d9de88e9cd754abea89736f29132056");
                YzIMKitAgent.instance().deleteGroupMember("@TGS#242ILVEH4", mm, new YzGroupDataListener() {
                    @Override
                    public void onCreate(int code, String groupId, String msg) {

                    }

                    @Override
                    public void update(int code, String msg) {

                    }

                    @Override
                    public void addMember(int code, String msg) {

                    }

                    @Override
                    public void deleteMember(int code, String msg) {
                        ToastUtil.info(DataTestActivity.this,code+">"+msg);
                    }

                    @Override
                    public void joinMember(List<GroupApplyInfo> applies) {

                    }
                });
                break;
            case R.id.join_group:
                V2TIMManager.getInstance().joinGroup("@TGS#242ILVEH4", UserApi.instance().getNickName()+"申请加群", new V2TIMCallback() {
                    @Override
                    public void onError(int code, String desc) {
                        SLog.e("addGroup err code = " + code + ", desc = " + desc);
                        ToastUtil.error(DataTestActivity.this,"Error code = " + code + ", desc = " + desc);
                    }

                    @Override
                    public void onSuccess() {
                        SLog.i("addGroup success");
                        ToastUtil.success(DataTestActivity.this,"加群请求已发送");
                    }
                });
                break;
            case R.id.start_auto:
                YzIMKitAgent.instance().startAuto();
//                startActivity(new Intent(this, GroupListActivity.class));
                break;
            case R.id.send_message:
                SendMessageReq sendMessageReq = new SendMessageReq();
                sendMessageReq.setFromUserId(UserApi.instance().getUserId());
                sendMessageReq.setToUserId("2d9de88e9cd754abea89736f29132056");
                sendMessageReq.setTextContent("这是一个指定发的消息");
                Yz.getSession().sendMessage(sendMessageReq,null);
                break;
        }
    }
}
