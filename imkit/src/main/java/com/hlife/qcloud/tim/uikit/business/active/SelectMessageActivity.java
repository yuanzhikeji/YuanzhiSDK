package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.business.message.CustomMessage;
import com.hlife.qcloud.tim.uikit.modules.chat.C2CChatManagerKit;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.contact.ContactItemBean;
import com.hlife.qcloud.tim.uikit.modules.contact.ContactLayout;
import com.hlife.qcloud.tim.uikit.modules.contact.ContactListView;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfoUtil;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.work.util.SLog;
import com.work.util.ToastUtil;

/**
 * Created by tangyx
 * Date 2020/11/12
 * email tangyx@live.com
 */

public class SelectMessageActivity extends BaseActivity {

    private ContactLayout mContactLayout;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        mContactLayout = findViewById(R.id.contact_layout);
        findViewById(R.id.im_title_layout).setVisibility(View.GONE);
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        setTitleName("选择");
        final String message =getIntent().getStringExtra(SelectMessageActivity.class.getSimpleName());
        if(TextUtils.isEmpty(message)){
            finish();
            return;
        }
        mContactLayout.initDefault(ContactListView.DataSource.CONTACT_GROUP_SELECT);
        mContactLayout.getContactListView().setOnItemClickListener(new ContactListView.OnItemClickListener() {
            @Override
            public void onItemClick(int position, ContactItemBean contact) {
                if (position == 0) {
                    Intent intent = new Intent(TUIKit.getAppContext(), GroupListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(GroupListActivity.class.getSimpleName(),message);
                    TUIKit.getAppContext().startActivity(intent);
                    finish();
                } else {
                    final ChatInfo chatInfo = new ChatInfo();
                    chatInfo.setType(V2TIMConversation.V2TIM_C2C);
                    chatInfo.setId(contact.getId());
                    String chatName = contact.getId();
                    if (!TextUtils.isEmpty(contact.getRemark())) {
                        chatName = contact.getRemark();
                    } else if (!TextUtils.isEmpty(contact.getNickname())) {
                        chatName = contact.getNickname();
                    }
                    chatInfo.setChatName(chatName);
                    C2CChatManagerKit c2CChatManagerKit = C2CChatManagerKit.getInstance();
                    c2CChatManagerKit.setCurrentChatInfo(chatInfo);
                    MessageInfo info = MessageInfoUtil.buildCustomMessage(message);
                    c2CChatManagerKit.sendMessage(info, false, new IUIKitCallBack() {
                        @Override
                        public void onSuccess(Object data) {
                            SLog.e("custom message send success:"+data);
                            finish();
                        }

                        @Override
                        public void onError(String module, int errCode, String errMsg) {
                            ToastUtil.warning(SelectMessageActivity.this,errCode+">"+errMsg);
                        }
                    });
                }
            }
        });
    }

    @Override
    public int onCustomContentId() {
        return R.layout.fragment_im_contact;
    }
    public static void sendCustomMessage(Context context, String message){
        Intent intent = new Intent(context, SelectMessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SelectMessageActivity.class.getSimpleName(),message);
        context.startActivity(intent);
    }
}
