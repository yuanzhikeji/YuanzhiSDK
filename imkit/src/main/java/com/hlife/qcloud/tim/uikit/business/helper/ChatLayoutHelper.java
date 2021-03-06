package com.hlife.qcloud.tim.uikit.business.helper;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.message.CustomMessage;
import com.hlife.qcloud.tim.uikit.config.ChatViewConfig;
import com.hlife.qcloud.tim.uikit.modules.chat.ChatLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.base.BaseInputFragment;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.input.InputLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.MessageLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageViewGroup;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageDrawListener;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.http.network.task.ObjectMapperFactory;
import com.tencent.imsdk.v2.V2TIMCustomElem;
import com.tencent.imsdk.v2.V2TIMMessage;
import com.hlife.qcloud.tim.uikit.R;
import com.work.util.SLog;

import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_CARD;
import static com.hlife.qcloud.tim.uikit.utils.IMKitConstants.BUSINESS_ID_CUSTOM_FILE;

public class ChatLayoutHelper {

    private Context mContext;

    public ChatLayoutHelper(Context context) {
        mContext = context;
    }

    public void customizeChatLayout(final ChatLayout layout, final ChatViewConfig config, YzCustomMessageDrawListener customMessageDrawListener) {
//        //====== NoticeLayout使用范例 ======//
//        NoticeLayout noticeLayout = layout.getNoticeLayout();
//        noticeLayout.alwaysShow(true);
//        noticeLayout.getContent().setText("现在插播一条广告");
//        noticeLayout.getContentExtra().setText("参看有奖");
//        noticeLayout.setOnNoticeClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ToastUtil.toastShortMessage("赏白银五千两");
//            }
//        });
//
        //====== MessageLayout使用范例 ======//
        MessageLayout messageLayout = layout.getMessageLayout();
//        ////// 设置聊天背景 //////
//        messageLayout.setBackground(new ColorDrawable(0xFFEFE5D4));
//        ////// 设置头像 //////
//        // 设置默认头像，默认与朋友与自己的头像相同
//        messageLayout.setAvatar(R.drawable.ic_more_file);
//        // 设置头像圆角
        messageLayout.setAvatarRadius(30);
//        // 设置头像大小
//        messageLayout.setAvatarSize(new int[]{48, 48});
//
//        ////// 设置昵称样式（对方与自己的样式保持一致）//////
//        messageLayout.setNameFontSize(12);
//        messageLayout.setNameFontColor(0xFF8B5A2B);
//
//        ////// 设置气泡 ///////
//        // 设置自己聊天气泡的背景
//        messageLayout.setRightBubble(new ColorDrawable(0xFFCCE4FC));
//        // 设置朋友聊天气泡的背景
//        messageLayout.setLeftBubble(new ColorDrawable(0xFFE4E7EB));
//
//        ////// 设置聊天内容 //////
//        // 设置聊天内容字体字体大小，朋友和自己用一种字体大小
//        messageLayout.setChatContextFontSize(15);
//        // 设置自己聊天内容字体颜色
        messageLayout.setRightChatContentFontColor(Color.WHITE);
//        // 设置朋友聊天内容字体颜色
        messageLayout.setLeftChatContentFontColor(Color.BLACK);
//
//        ////// 设置聊天时间 //////
//        // 设置聊天时间线的背景
//        messageLayout.setChatTimeBubble(new ColorDrawable(0xFFE4E7EB));
//        // 设置聊天时间的字体大小
//        messageLayout.setChatTimeFontSize(12);
//        // 设置聊天时间的字体颜色
//        messageLayout.setChatTimeFontColor(0xFF7E848C);
//
//        ////// 设置聊天的提示信息 //////
//        // 设置提示的背景
//        messageLayout.setTipsMessageBubble(new ColorDrawable(0xFFE4E7EB));
//        // 设置提示的字体大小
//        messageLayout.setTipsMessageFontSize(12);
//        // 设置提示的字体颜色
//        messageLayout.setTipsMessageFontColor(0xFF7E848C);
//
        // 设置自定义的消息渲染时的回调
        messageLayout.setOnCustomMessageDrawListener(new CustomMessageDraw(customMessageDrawListener));
//
//        // 新增一个PopMenuAction
//        PopMenuAction action = new PopMenuAction();
//        action.setActionName("test");
//        action.setActionClickListener(new PopActionClickListener() {
//            @Override
//            public void onActionClick(int position, Object data) {
//                ToastUtil.toastShortMessage("新增一个pop action");
//            }
//        });
//        messageLayout.addPopAction(action);
//
//        final MessageLayout.OnItemClickListener l = messageLayout.getOnItemClickListener();
//        messageLayout.setOnItemClickListener(new MessageLayout.OnItemClickListener() {
//            @Override
//            public void onMessageLongClick(View view, int position, MessageInfo messageInfo) {
//                l.onMessageLongClick(view, position, messageInfo);
//                ToastUtil.toastShortMessage("demo中自定义长按item");
//            }
//
//            @Override
//            public void onUserIconClick(View view, int position, MessageInfo messageInfo) {
//                l.onUserIconClick(view, position, messageInfo);
//                ToastUtil.toastShortMessage("demo中自定义点击头像");
//            }
//        });


        //====== InputLayout使用范例 ======//
        InputLayout inputLayout = layout.getInputLayout();

//        // TODO 隐藏音频输入的入口，可以打开下面代码测试
//        inputLayout.disableAudioInput(true);
//        // TODO 隐藏表情输入的入口，可以打开下面代码测试
//        inputLayout.disableEmojiInput(true);
//        // TODO 隐藏更多功能的入口，可以打开下面代码测试
//        inputLayout.disableMoreInput(true);
//        // TODO 可以用自定义的事件来替换更多功能的入口，可以打开下面代码测试
//        inputLayout.replaceMoreInput(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ToastUtil.toastShortMessage("自定义的更多功能按钮事件");
//                MessageInfo info = MessageInfoUtil.buildTextMessage("自定义的消息");
//                layout.sendMessage(info, false);
//            }
//        });
//        // TODO 可以用自定义的fragment来替换更多功能，可以打开下面代码测试
//        inputLayout.replaceMoreInput(new CustomInputFragment().setChatLayout(layout));
//
//        // TODO 可以disable更多面板上的各个功能，可以打开下面代码测试
        inputLayout.setChatViewConfig(config);
//        inputLayout.disableSendPhotoAction(config.isDisableSendPhotoAction());
//        inputLayout.disableCaptureAction(config.isDisableCaptureAction());
//        inputLayout.disableVideoRecordAction(config.isDisableVideoRecordAction());
//        inputLayout.disableSendFileAction(config.isDisableSendFileAction());
//        inputLayout.disableSendLocationAction(config.isDisableSendLocationAction());
        if(!config.isDisableAudioCall()){
            inputLayout.enableAudioCall();
        }
        if(!config.isDisableVideoCall()){
            inputLayout.enableVideoCall();
        }
        inputLayout.setVisibility(config.isDisableChatInput()?View.GONE:View.VISIBLE);
        // TODO 可以自己增加一些功能，可以打开下面代码测试
        // 增加一个欢迎提示富文本
//        InputMoreActionUnit unit = new InputMoreActionUnit();
//        unit.setIconResId(R.drawable.custom);
//        unit.setTitleId(R.string.test_custom_action);
//        unit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Gson gson = new Gson();
//                CustomMessage customHelloMessage = new CustomMessage();
//                String data = gson.toJson(customHelloMessage);
//                MessageInfo info = MessageInfoUtil.buildCustomMessage(data);
//                layout.sendMessage(info, false);
//            }
//        });
//        inputLayout.addAction(unit);
        layout.getTitleBar().setVisibility(config.isShowTitle()?View.VISIBLE:View.GONE);
    }

    public static class CustomInputFragment extends BaseInputFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View baseView = inflater.inflate(R.layout.test_chat_input_custom_fragment, container, false);
            Button btn1 = baseView.findViewById(R.id.test_send_message_btn1);
            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (getChatLayout() != null) {
//                        Gson gson = new Gson();
//                        CustomMessage customHelloMessage = new CustomMessage();
//                        String data = gson.toJson(customHelloMessage);
//                        MessageInfo info = MessageInfoUtil.buildCustomMessage(data);
//                        getChatLayout().sendMessage(info, false);
//                    }
                }
            });
            Button btn2 = baseView.findViewById(R.id.test_send_message_btn2);
            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (getChatLayout() != null) {
//                        Gson gson = new Gson();
//                        CustomMessage customHelloMessage = new CustomMessage();
//                        String data = gson.toJson(customHelloMessage);
//                        MessageInfo info = MessageInfoUtil.buildCustomMessage(data);
//                        getChatLayout().sendMessage(info, false);
//                    }
                }
            });
            return baseView;
        }

    }

    public static class CustomMessageDraw implements YzCustomMessageDrawListener {

        private YzCustomMessageDrawListener customMessageDrawListener;

        public CustomMessageDraw(YzCustomMessageDrawListener customMessageDrawListener){
            this.customMessageDrawListener = customMessageDrawListener;
        }

        /**
         * 自定义消息渲染时，会调用该方法，本方法实现了自定义消息的创建，以及交互逻辑
         *
         * @param parent 自定义消息显示的父View，需要把创建的自定义消息view添加到parent里
         * @param info   消息的具体信息
         */
        @Override
        public void onDraw(YzCustomMessageViewGroup parent, MessageInfo info) {
            // 获取到自定义消息的json数据
            if (info.getTimMessage().getElemType() != V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
                return;
            }
            V2TIMCustomElem elem = info.getTimMessage().getCustomElem();
            String elemStr = "";
            if(elem.getData()!=null){
                elemStr = new String(elem.getData());
            }
            // 自定义的json数据，需要解析成bean实例
            CustomMessage data = null;
            try {
                data = ObjectMapperFactory.getObjectMapper().json2Model(elemStr,CustomMessage.class);
            } catch (Exception e) {
                SLog.w("invalid json: " + elemStr + " " + e.getMessage());
            }
            if (data!=null) {
                if(data.getBusinessID().equals(BUSINESS_ID_CUSTOM_CARD)){
                    CustomIMUIController.onDrawCard(parent, data);
                }else if(data.getBusinessID().equals(BUSINESS_ID_CUSTOM_FILE)){
                    try{
                        CustomFileMessage customFileMessage = ObjectMapperFactory.getObjectMapper().json2Model(elemStr,CustomFileMessage.class);
                        CustomIMUIController.onDrawFile(parent,customFileMessage);
                    }catch (Exception ignore){}
                }else{
                    if(customMessageDrawListener!=null){
                        customMessageDrawListener.onDraw(parent,info);
                    }
                }
            } else {
                if(customMessageDrawListener!=null){
                    customMessageDrawListener.onDraw(parent,info);
                }
            }
        }
    }

}
