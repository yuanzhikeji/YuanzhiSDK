package com.work.api.open.model;

public class GetTenantGroupListReq extends BaseReq{
    private int pageNo=1;
    private int pageSize = 1000;

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
