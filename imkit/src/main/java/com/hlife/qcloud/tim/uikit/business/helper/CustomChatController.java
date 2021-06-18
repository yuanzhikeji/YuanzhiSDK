package com.hlife.qcloud.tim.uikit.business.helper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.base.IBaseAction;
import com.hlife.qcloud.tim.uikit.base.IBaseInfo;
import com.hlife.qcloud.tim.uikit.base.IBaseViewHolder;
import com.hlife.qcloud.tim.uikit.base.TUIChatControllerListener;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.MessageCustomHolder;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageDrawListener;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageViewGroup;
import com.hlife.qcloud.tim.uikit.modules.message.MessageInfo;
import com.tencent.imsdk.v2.V2TIMMessage;

import java.util.List;

public class CustomChatController implements TUIChatControllerListener {

    private YzCustomMessageDrawListener mOnCustomMessageDrawListener;

    public void setOnCustomMessageDrawListener(YzCustomMessageDrawListener mOnCustomMessageDrawListener) {
        this.mOnCustomMessageDrawListener = mOnCustomMessageDrawListener;
    }

    @Override
    public List<IBaseAction> onRegisterMoreActions() {
        return null;
    }

    @Override
    public IBaseInfo createCommonInfoFromTimMessage(V2TIMMessage timMessage) {
//        if (timMessage.getElemType() == V2TIMMessage.V2TIM_ELEM_TYPE_CUSTOM) {
//            MessageInfo messageInfo = new MessageInfo();
//            MessageInfoUtil.setMessageInfoCommonAttributes(messageInfo, timMessage);
//            Context context = TUIKit.getAppContext();
//            if (context != null) {
//                messageInfo.setExtra(context.getString(R.string.custom_msg));
//            }
//            return messageInfo;
//        }
        return null;
    }

    @Override
    public IBaseViewHolder createCommonViewHolder(ViewGroup parent, int viewType) {
        if (parent == null) {
            return null;
        }
        LayoutInflater inflater = LayoutInflater.from(TUIKit.getAppContext());
        View contentView = inflater.inflate(R.layout.message_adapter_item_content, parent, false);
        return new MessageCustomHolder(contentView);
    }

    @Override
    public boolean bindCommonViewHolder(IBaseViewHolder baseViewHolder, IBaseInfo baseInfo, int position) {
        if (baseViewHolder instanceof YzCustomMessageViewGroup && baseInfo instanceof MessageInfo) {
            YzCustomMessageViewGroup customHolder = (YzCustomMessageViewGroup) baseViewHolder;
            MessageInfo msg = (MessageInfo) baseInfo;
            new ChatLayoutHelper.CustomMessageDraw(mOnCustomMessageDrawListener).onDraw(customHolder, msg);
            return true;
        }
        return false;
    }
}

