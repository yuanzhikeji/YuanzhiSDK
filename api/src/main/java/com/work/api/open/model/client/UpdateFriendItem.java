package com.work.api.open.model.client;

import java.util.List;

public class UpdateFriendItem {
    private String toAccount;
    private List<SNSItem> SnsItem;

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public List<SNSItem> getSnsItem() {
        return SnsItem;
    }

    public void setSnsItem(List<SNSItem> snsItem) {
        SnsItem = snsItem;
    }

    public static class SNSItem {
        private String tag;
        private String value;

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
