package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.modules.chat.base.ChatInfo;
import com.hlife.qcloud.tim.uikit.modules.contact.ContactItemBean;
import com.hlife.qcloud.tim.uikit.modules.contact.ContactListView;
import com.hlife.qcloud.tim.uikit.utils.ToastUtil;
import com.tencent.imsdk.v2.V2TIMConversation;
import com.hlife.qcloud.tim.uikit.R;

public class StartC2CChatActivity extends IMBaseActivity {


    private ContactItemBean mSelectedItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup_start_c2c_chat);

        TitleBarLayout mTitleBar = findViewById(R.id.start_c2c_chat_title);
        mTitleBar.setTitle(getResources().getString(R.string.sure), TitleBarLayout.POSITION.RIGHT);
        mTitleBar.getRightTitle().setTextColor(getResources().getColor(R.color.title_bar_font_color));
        mTitleBar.getRightIcon().setVisibility(View.GONE);
        mTitleBar.setOnRightClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConversation();
            }
        });
        mTitleBar.setOnLeftClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ContactListView mContactListView = findViewById(R.id.contact_list_view);
        mContactListView.setSingleSelectMode(true);
        mContactListView.loadDataSource(ContactListView.DataSource.FRIEND_LIST);
        mContactListView.setOnSelectChangeListener(new ContactListView.OnSelectChangedListener() {
            @Override
            public void onSelectChanged(ContactItemBean contact, boolean selected) {
                if (selected) {
                    if (mSelectedItem == contact) {
                        // ?????????Item?????????
                    } else {
                        if (mSelectedItem != null) {
                            mSelectedItem.setSelected(false);
                        }
                        mSelectedItem = contact;
                    }
                } else {
                    if (mSelectedItem == contact) {
                        mSelectedItem.setSelected(false);
                    }
                }
            }
        });
    }

    public void startConversation() {
        if (mSelectedItem == null || !mSelectedItem.isSelected()) {
            ToastUtil.toastLongMessage("?????????????????????");
            return;
        }
        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setType(V2TIMConversation.V2TIM_C2C);
        chatInfo.setId(mSelectedItem.getId());
        String chatName = mSelectedItem.getId();
        if (!TextUtils.isEmpty(mSelectedItem.getRemark())) {
            chatName = mSelectedItem.getRemark();
        } else if (!TextUtils.isEmpty(mSelectedItem.getNickname())) {
            chatName = mSelectedItem.getNickname();
        }
        chatInfo.setChatName(chatName);
        Intent intent = new Intent(TUIKit.getAppContext(), ChatActivity.class);
        intent.putExtra(Constants.CHAT_INFO, chatInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TUIKit.getAppContext().startActivity(intent);

        finish();
    }
}
