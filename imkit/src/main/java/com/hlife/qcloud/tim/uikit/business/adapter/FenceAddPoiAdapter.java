package com.hlife.qcloud.tim.uikit.business.adapter;

import androidx.annotation.Nullable;

import com.amap.api.services.help.Tip;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;
import com.yzcm.library.adapter.mm.BaseViewHolder;
import com.hlife.qcloud.tim.uikit.R;

import java.util.List;

/**
 * Created by tangyx
 * Date 2020/8/4
 * email tangyx@live.com
 */

public class FenceAddPoiAdapter extends BaseQuickAdapter<Tip, BaseViewHolder> {

    public FenceAddPoiAdapter(@Nullable List<Tip> data) {
        super(R.layout.adapter_fence_add_poi,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Tip item) {
        helper.setText(R.id.name,item.getName());
        helper.setText(R.id.district,item.getDistrict());
    }
}
