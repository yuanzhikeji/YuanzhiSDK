package com.yz.hlife.activity;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hlife.qcloud.tim.uikit.YzIMKitAgent;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.inter.YzGroupJoinListener;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.work.api.open.Yz;
import com.work.api.open.model.GetTenantGroupListReq;
import com.work.api.open.model.GetTenantGroupListResp;
import com.work.api.open.model.client.OpenGroupLife;
import com.work.util.ToastUtil;
import com.yz.hlife.R;
import com.yzcm.library.adapter.mm.BaseQuickAdapter;
import com.yzcm.library.adapter.mm.divider.HorizontalDividerItemDecoration;

public class GroupListDemoActivity extends BaseActivity {

    private GroupListDemoAdapter mAdapter;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).build());
        mAdapter = new GroupListDemoAdapter(null);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                OpenGroupLife openGroupLife = mAdapter.getItem(position);
                if(openGroupLife!=null){
                    YzIMKitAgent.instance().joinGroup(openGroupLife.getGroupId(), UserApi.instance().getNickName() + "申请加群", new YzGroupJoinListener() {
                        @Override
                        public void success() {
                            ToastUtil.success(GroupListDemoActivity.this,"申请成功");
                        }

                        @Override
                        public void error(int code, String desc) {
                            ToastUtil.error(GroupListDemoActivity.this,code+">>>"+desc);
                        }
                    });
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        Yz.getSession().getTenantGroupList(new GetTenantGroupListReq(),this);
    }

    @Override
    public int onCustomContentId() {
        return R.layout.activity_group_list;
    }

    @Override
    public void onResult(RequestWork req, ResponseWork resp) throws Exception {
        super.onResult(req, resp);
        if(resp.isSuccess()){
            if(resp instanceof GetTenantGroupListResp){
                mAdapter.addData(((GetTenantGroupListResp) resp).getData());
            }
        }else{
            ToastUtil.error(this,resp.getMessage());
        }
    }
}
