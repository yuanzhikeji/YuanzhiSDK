package com.hlife.qcloud.tim.uikit.modules.group.member;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.hlife.qcloud.tim.uikit.modules.group.interfaces.IGroupMemberLayout;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.IUIKitCallBack;
import com.hlife.qcloud.tim.uikit.component.TitleBarLayout;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfo;
import com.hlife.qcloud.tim.uikit.modules.group.info.GroupInfoProvider;
import com.work.util.ToastUtil;

import java.util.List;

public class GroupMemberDeleteLayout extends LinearLayout implements IGroupMemberLayout {

    private TitleBarLayout mTitleBar;
    private GroupMemberDeleteAdapter mAdapter;
    private List<GroupMemberInfo> mDelMembers;
    private GroupInfo mGroupInfo;

    public GroupMemberDeleteLayout(Context context) {
        super(context);
        init();
    }

    public GroupMemberDeleteLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroupMemberDeleteLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.group_member_del_layout, this);
        mTitleBar = findViewById(R.id.group_member_title_bar);
        mTitleBar.setTitle("移除", TitleBarLayout.POSITION.RIGHT);
        mTitleBar.getRightTitle().setTextColor(ContextCompat.getColor(getContext(),R.color.text_negative));
        mTitleBar.setTitle("移除成员", TitleBarLayout.POSITION.MIDDLE);
        mTitleBar.getRightIcon().setVisibility(View.GONE);
        mTitleBar.setOnRightClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupInfoProvider provider = new GroupInfoProvider();
                provider.loadGroupInfo(mGroupInfo);
                provider.removeGroupMembers(mDelMembers, new IUIKitCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        mAdapter.getGroupMembers().removeAll(mDelMembers);
                        mAdapter.notifyDataSetChanged();
                        ToastUtil.success(getContext(),"删除成员成功");
                        post(new Runnable() {
                            @Override
                            public void run() {
                                mTitleBar.setTitle("移除", TitleBarLayout.POSITION.RIGHT);
                                mAdapter.clear();
                                mAdapter.notifyDataSetChanged();

                            }
                        });
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        ToastUtil.error(getContext(),"删除成员失败:" + errCode + "=" + errMsg);
                    }
                });
            }
        });
        mAdapter = new GroupMemberDeleteAdapter();
        mAdapter.setOnSelectChangedListener(new GroupMemberDeleteAdapter.OnSelectChangedListener() {
            @Override
            public void onSelectChanged(List<GroupMemberInfo> members) {
                mDelMembers = members;
                if (mDelMembers.size() > 0) {
                    mTitleBar.setTitle("移除（" + (mDelMembers.size() + "）"), TitleBarLayout.POSITION.RIGHT);
                } else {
                    mTitleBar.setTitle("移除", TitleBarLayout.POSITION.RIGHT);
                }
            }
        });
        ListView mMembers = findViewById(R.id.group_del_members);
        mMembers.setAdapter(mAdapter);
    }

    public TitleBarLayout getTitleBar() {
        return mTitleBar;
    }

    @Override
    public void setParentLayout(Object parent) {

    }

    @Override
    public void setDataSource(GroupInfo groupInfo) {
        mGroupInfo = groupInfo;
        mAdapter.setDataSource(groupInfo.getMemberDetails());
    }
}
