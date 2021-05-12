package com.hlife.qcloud.tim.uikit.modules.group.apply;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hlife.qcloud.tim.uikit.business.Constants;
import com.hlife.qcloud.tim.uikit.business.active.ChatActivity;
import com.hlife.qcloud.tim.uikit.modules.group.interfaces.IGroupMemberLayout;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;

import java.util.List;


public class GroupApplyManagerLayout extends LinearLayout implements IGroupMemberLayout {

    private GroupApplyAdapter mAdapter;

    public GroupApplyManagerLayout(Context context) {
        super(context);
        init();
    }

    public GroupApplyManagerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupApplyManagerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.group_apply_manager_layout, this);
        ListView mApplyMemberList = findViewById(R.id.group_apply_members);
        mAdapter = new GroupApplyAdapter();
        mApplyMemberList.setAdapter(mAdapter);
    }

    @Override
    public TitleBarLayout getTitleBar() {
        return null;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

    @Override
    public void setDataSource(GroupInfo dataSource) {
        mAdapter.setDataSource(dataSource);
        mAdapter.notifyDataSetChanged();
        mAdapter.setOnItemClickListener(new GroupApplyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupApplyInfo info) {
                Intent intent = new Intent(getContext(), GroupApplyMemberActivity.class);
                intent.putExtra(IMKitConstants.ProfileType.CONTENT, info);
                ((Activity) getContext()).startActivityForResult(intent, IMKitConstants.ActivityRequest.CODE_1);
            }
        });
    }

    public void setDataSource(List<GroupApplyInfo> dataSource) {
        mAdapter.setDataSource(dataSource);
        mAdapter.notifyDataSetChanged();
        mAdapter.setOnItemClickListener(new GroupApplyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupApplyInfo info) {
                GroupInfo groupInfo = new GroupInfo();
                groupInfo.setId(info.getGroupApplication().getGroupID());
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra(Constants.CHAT_INFO, groupInfo);
                getContext().startActivity(intent);
            }
        });
    }

    public void updateItemData(GroupApplyInfo info) {
        mAdapter.updateItemData(info);
    }
}
