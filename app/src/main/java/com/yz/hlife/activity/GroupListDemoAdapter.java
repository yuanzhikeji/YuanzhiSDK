package com.yz.hlife.activity;

import androidx.annotation.Nullable;

import com.work.api.open.model.client.OpenGroupLife;
import com.yz.hlife.R;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;
import com.yzcm.library.adapter.mm.BaseViewHolder;

import java.util.List;

public class GroupListDemoAdapter extends BaseQuickAdapter<OpenGroupLife, BaseViewHolder> {

    public GroupListDemoAdapter(@Nullable List<OpenGroupLife> data) {
        super(R.layout.adapter_group_demo_item,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, OpenGroupLife item) {
        helper.setText(R.id.name,item.getName());
        helper.setText(R.id.group_id,item.getGroupId());
    }
}
