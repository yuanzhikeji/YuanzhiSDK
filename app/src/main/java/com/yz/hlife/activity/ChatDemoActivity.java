package com.yz.hlife.activity;


import android.content.Intent;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.fragment.ChatFragment;
import com.hlife.qcloud.tim.uikit.business.fragment.ConversationFragment;
import com.hlife.qcloud.tim.uikit.business.helper.CustomIMUIController;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatMessageListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzChatType;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageClickListener;
import com.hlife.qcloud.tim.uikit.business.inter.YzMessageSendCallback;
import com.hlife.qcloud.tim.uikit.business.message.CustomMessage;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationListLayout;
import com.hlife.qcloud.tim.uikit.modules.conversation.base.ConversationInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.work.util.SLog;
import com.work.util.ToastUtil;
import com.yz.hlife.R;

import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_CARD;

public class ChatDemoActivity extends BaseActivity implements ConversationListLayout.OnItemClickListener, YzMessageClickListener {

    @Override
    public void onInitView() throws Exception {
        super.onInitView();

        int flag = getIntent().getIntExtra(ChatDemoActivity.class.getSimpleName(),0);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(flag == 1){
            ConversationFragment fragment1 = ConversationFragment.newConversation(YzChatType.GROUP);
            fragment1.setOnItemClickListener(this);
            fragmentTransaction.replace(R.id.container,fragment1);
            fragmentTransaction.commitAllowingStateLoss();
        }else if(flag == 2){
            ChatInfo chatInfo = (ChatInfo) getIntent().getSerializableExtra("chatInfo");
            ChatViewConfig chatViewConfig = new ChatViewConfig();
            chatViewConfig.setDisableVideoCall(false);
            chatViewConfig.setDisableAudioCall(false);
            chatViewConfig.setCustomAtGroupMember(true);
            chatViewConfig.setShowTitle(true);
            View sendCustom = findViewById(R.id.send_custom);
            sendCustom.setVisibility(View.VISIBLE);
            sendCustom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    CustomMessage message = new CustomMessage();
//                    message.setBusinessID(IMKitConstants.BUSINESS_ID_CUSTOM_CARD);
//                    message.setLogo("https://yzkj-im.oss-cn-beijing.aliyuncs.com/user/16037885020911603788500745.png");
//                    message.setDesc("欢迎加入元讯大家庭！欢迎加入元讯大家庭！欢迎加入元讯大家庭！欢迎加入元讯大家庭！");
//                    message.setTitle("元讯IM生态工具元讯IM生态工具元讯IM生态工具元讯IM生态工具元讯IM生态工具");
//                    message.setLink("http://yzmsri.com/");
//                    message.setBusinessID(IMKitConstants.BUSINESS_ID_CUSTOM_CARD);

//                    String message = "{\"createTimeStr\":\"2021\",\"imgUrl\":\"https://gimg2.baidu.com/image_search/src\\u003dhttp%3A%2F%2Fimage.it168.com%2Fn%2F640x480%2F7%2F7480%2F7480107.jpg\\u0026refer\\u003dhttp%3A%2F%2Fimage.it168.com\\u0026app\\u003d2002\\u0026size\\u003df9999,10000\\u0026q\\u003da80\\u0026n\\u003d0\\u0026g\\u003d0n\\u0026fmt\\u003djpeg?sec\\u003d1624446455\\u0026t\\u003d182cb71002d7ad118b03bf47b63d2e16\",\"link\":\"http://wwww.baidu.com\",\"messageType\":\"0\",\"moneyText\":\"¥50\",\"orderNum\":\"订单号\",\"productName\":\"商品名称\"}";
                    ProductInfoMessageForYJ productInfoMessageForYJ = new ProductInfoMessageForYJ();
                    productInfoMessageForYJ.setImgUrl("https://gimg2.baidu.com/image_search/src\\u003dhttp%3A%2F%2Fimage.it168.com%2Fn%2F640x480%2F7%2F7480%2F7480107.jpg\\u0026refer\\u003dhttp%3A%2F%2Fimage.it168.com\\u0026app\\u003d2002\\u0026size\\u003df9999,10000\\u0026q\\u003da80\\u0026n\\u003d0\\u0026g\\u003d0n\\u0026fmt\\u003djpeg?sec\\u003d1624446455\\u0026t\\u003d182cb71002d7ad118b03bf47b63d2e16");
                    productInfoMessageForYJ.setCreateTimeStr("2021");
                    productInfoMessageForYJ.setProductName("商品名称");
                    YzIMKitAgent.instance().sendCustomMessage(new Gson().toJson(productInfoMessageForYJ), new YzMessageSendCallback() {
                        @Override
                        public void success() {

                        }

                        @Override
                        public void error(int code, String desc) {

                        }
                    });
                }
            });
            ChatFragment chatFragment = ChatFragment.newChatFragment(chatInfo,chatViewConfig);
            chatFragment.setYzMessageClickListener(this);
            chatFragment.setYzChatMessageListener(new YzChatMessageListener() {
                @Override
                public void onChatSendMessageError(int code, String desc) {
                    SLog.e("send fail>>"+code+">"+desc);
                }
            });
            chatFragment.setYzCustomMessageDrawListener((parent, info) -> {
//                SLog.e("接收到自定义消息："+new String(info.getTimMessage().getCustomElem().getData()));
//                // 获取到自定义消息的json数据
//                if (info.getTimMessage().getElemType() != V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
//                    return;
//                }
//                V2TIMCustomElem elem = info.getTimMessage().getCustomElem();
//                // 自定义的json数据，需要解析成bean实例
//                CustomMessage data = null;
//                try {
//                    data = new Gson().fromJson(new String(elem.getData()), CustomMessage.class);
//                } catch (Exception e) {
//                    SLog.w("invalid json: " + new String(elem.getData()) + " " + e.getMessage());
//                }
//                if (data == null) {
//                    SLog.e( "No Custom Data: " + new String(elem.getData()));
//                } else if (data.version == IMKitConstants.JSON_VERSION_1
//                        || (data.version == IMKitConstants.JSON_VERSION_4 && data.getBusinessID().equals(BUSINESS_ID_CUSTOM_CARD))) {
//                    CustomIMUIController.onDrawCard(parent, data);
//                } else {
//                    SLog.w("unsupported version: " + data);
//                }
            });
            fragmentTransaction.replace(R.id.container,chatFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }else{
            ConversationFragment fragment0 = ConversationFragment.newConversation(YzChatType.C2C);
            fragment0.setOnItemClickListener(this);
            fragmentTransaction.replace(R.id.container,fragment0);
            fragmentTransaction.commitAllowingStateLoss();
        }

    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
    }

    @Override
    public void onItemClick(View view, int position, ConversationInfo messageInfo) {
        ToastUtil.info(ChatDemoActivity.this,"自定义点击："+messageInfo.toString());
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setChatName(messageInfo.getTitle());
        chatInfo.setId(messageInfo.getId());
        chatInfo.setGroup(messageInfo.isGroup());
        startActivity(new Intent(this,ChatDemoActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra("chatInfo",chatInfo)
                .putExtra(ChatDemoActivity.class.getSimpleName(),2));
    }

    @Override
    public boolean isShowTitleBar() {
        int flag = getIntent().getIntExtra(ChatDemoActivity.class.getSimpleName(),0);
        return flag!=2;
    }

    @Override
    public void onUserIconClick(String userId) {
        ToastUtil.info(this,"点击了头像："+userId);
    }

    @Override
    public void onAtGroupMember(String groupId) {
        ToastUtil.info(this,"监听到触发了@能力："+groupId);
    }
}
