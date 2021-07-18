package com.work.api.open.model;

import com.work.api.open.model.client.UserRemark;

import java.util.List;

public class RemarkListGetResponse extends BaseResp {
    private List<UserRemark> data;

    public List<UserRemark> getData() {
        return data;
    }

    public void setData(List<UserRemark> data) {
        this.data = data;
    }
}
