package com.hlife.qcloud.tim.uikit.modules.group.apply;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;

public class GroupApplyManagerActivity extends Activity {

    private GroupApplyManagerLayout mManagerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_apply_manager_activity);
        if (getIntent().getExtras() == null) {
            finish();
            return;
        }
        mManagerLayout = findViewById(R.id.group_apply_manager_layout);

        GroupInfo mGroupInfo = (GroupInfo) getIntent().getExtras().getSerializable(IMKitConstants.Group.GROUP_INFO);
        mManagerLayout.setDataSource(mGroupInfo);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != IMKitConstants.ActivityRequest.CODE_1) {
            return;
        }
        if (resultCode != RESULT_OK) {
            return;
        }
        GroupApplyInfo info = (GroupApplyInfo) data.getSerializableExtra(IMKitConstants.Group.MEMBER_APPLY);
        if (info == null) {
            return;
        }
        mManagerLayout.updateItemData(info);
    }

}
