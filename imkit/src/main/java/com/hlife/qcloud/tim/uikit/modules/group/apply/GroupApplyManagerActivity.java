package com.hlife.qcloud.tim.uikit.modules.group.apply;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupDataListener;
import com.hlife.qcloud.tim.uikit.modules.conversation.ConversationManagerKit;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.work.api.open.model.client.OpenGroupInfo;

import java.util.List;

public class GroupApplyManagerActivity extends BaseActivity {

    private GroupApplyManagerLayout mManagerLayout;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        mManagerLayout = findViewById(R.id.group_apply_manager_layout);
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        if (getIntent().getExtras() == null) {
            setTitleName(R.string.group_apply_members_all);
            ConversationManagerKit.getInstance().groupApplicationList(new YzGroupDataListener() {
                @Override
                public void onCreate(int code, String groupId, String msg) {

                }

                @Override
                public void update(int code, String msg) {

                }

                @Override
                public void addMember(int code, String msg) {

                }

                @Override
                public void deleteMember(int code, String msg) {

                }

                @Override
                public void joinMember(List<GroupApplyInfo> applies) {
                    mManagerLayout.setDataSource(applies);
                }
            });
        }else{
            GroupInfo mGroupInfo = (GroupInfo) getIntent().getExtras().getSerializable(IMKitConstants.Group.GROUP_INFO);
            mManagerLayout.setDataSource(mGroupInfo);
            setTitleName(R.string.group_apply_members);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public int onCustomContentId() {
        return R.layout.group_apply_manager_activity;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
