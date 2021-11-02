package com.work.api.open.model;

import com.work.api.open.model.client.UpdateFriendItem;

import java.util.List;

public class UpdateFriendReq extends BaseReq {
    private String fromAccount;
    private List<UpdateFriendItem> updateItem;

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public List<UpdateFriendItem> getUpdateItem() {
        return updateItem;
    }

    public void setUpdateItem(List<UpdateFriendItem> updateItem) {
        this.updateItem = updateItem;
    }
}
