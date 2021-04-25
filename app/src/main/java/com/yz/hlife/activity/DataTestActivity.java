package com.yz.hlife.activity;

import android.content.Intent;
import android.view.View;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.business.inter.YzStatusListener;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.work.api.open.model.SysUserReq;
import com.work.util.SLog;
import com.work.util.ToastUtil;
import com.yz.hlife.R;

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
        SysUserReq sysUserReq = new SysUserReq();
        sysUserReq.setNickName("试试2233");
        YzIMKitAgent.instance().register(sysUserReq,null);
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
        }
    }
}
