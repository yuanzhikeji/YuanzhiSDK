package com.work.api.open.model;

import com.work.api.open.model.client.OpenGroupLife;

import java.util.List;

public class GetTenantGroupListResp extends BaseResp{
    private List<OpenGroupLife> data;

    public List<OpenGroupLife> getData() {
        return data;
    }
}
