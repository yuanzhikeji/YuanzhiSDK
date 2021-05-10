package com.hlife.qcloud.tim.uikit.modules.group.apply;

import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.modules.contact.FriendProfileLayout;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;

public class GroupApplyMemberActivity extends BaseActivity {

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        FriendProfileLayout layout = findViewById(R.id.friend_profile);
        layout.initData(getIntent().getSerializableExtra(IMKitConstants.ProfileType.CONTENT));
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        setTitleName("申请加群");
    }

    @Override
    public int onCustomContentId() {
        return R.layout.group_apply_member_activity;
    }
}
