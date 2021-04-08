package com.hlife.qcloud.tim.uikit.modules.chat.base;

import com.hlife.qcloud.tim.uikit.modules.chat.interfaces.IChatLayout;
import com.hlife.qcloud.tim.uikit.base.BaseFragment;

public class BaseInputFragment extends BaseFragment {

    private IChatLayout mChatLayout;

    public IChatLayout getChatLayout() {
        return mChatLayout;
    }

    public BaseInputFragment setChatLayout(IChatLayout layout) {
        mChatLayout = layout;
        return this;
    }
}
