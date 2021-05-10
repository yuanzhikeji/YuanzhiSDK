package com.hlife.qcloud.tim.uikit.modules.group.apply;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hlife.qcloud.tim.uikit.modules.group.interfaces.IGroupMemberLayout;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;


public class GroupApplyManagerLayout extends LinearLayout implements IGroupMemberLayout {

    private ListView mApplyMemberList;
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
        mApplyMemberList = findViewById(R.id.group_apply_members);
        mAdapter = new GroupApplyAdapter();
        mAdapter.setOnItemClickListener(new GroupApplyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupApplyInfo info) {
                Intent intent = new Intent(getContext(), GroupApplyMemberActivity.class);
                intent.putExtra(IMKitConstants.ProfileType.CONTENT, info);
                ((Activity) getContext()).startActivityForResult(intent, IMKitConstants.ActivityRequest.CODE_1);
            }
        });
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
    }

    public void updateItemData(GroupApplyInfo info) {
        mAdapter.updateItemData(info);
    }
}
