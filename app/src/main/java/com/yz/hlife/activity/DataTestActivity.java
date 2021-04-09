package com.yz.hlife.activity;

import android.content.Intent;
import android.view.View;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzConversationDataListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageWatcher;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.work.util.SLog;
import com.yz.hlife.R;

import java.util.List;

/**
 * Created by tangyx
 * Date 2021/4/4
 * email tangyx@live.com
 */

public class DataTestActivity extends BaseActivity implements YzMessageWatcher, View.OnClickListener {


    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        loadConversation();
        YzIMKitAgent.instance().addMessageWatcher(this);
        findViewById(R.id.chat_group).setOnClickListener(this);
        findViewById(R.id.chat_single).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        YzIMKitAgent.instance().removeMessageWatcher(this);
    }

    public void loadConversation(){
        YzIMKitAgent.instance().loadConversation(0, YzChatType.ALL, new YzConversationDataListener() {

            @Override
            public void onConversationData(List<ConversationInfo> data, long unRead, long nextSeq) {
                if(data!=null && data.size()>0){
                    getConversation(data.get(0).getConversationId());
                }
            }

            @Override
            public void onConversationError(int code, String desc) {

            }
        });
    }
    public void getConversation(String conversionId){
        YzIMKitAgent.instance().getConversation(conversionId, new YzConversationDataListener() {

            @Override
            public void onConversationData(ConversationInfo data) {
                SLog.e("获取指定会话："+data.toString());
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
                SLog.e("单聊未读："+singleUnRead+"群聊未读："+groupUnRead);
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
            case R.id.chat_single:
                startActivity(new Intent(this,ChatDemoActivity.class).putExtra(ChatDemoActivity.class.getSimpleName(),0));
                break;
            case R.id.chat_group:
                startActivity(new Intent(this,ChatDemoActivity.class).putExtra(ChatDemoActivity.class.getSimpleName(),1));
                break;
        }
    }
}
