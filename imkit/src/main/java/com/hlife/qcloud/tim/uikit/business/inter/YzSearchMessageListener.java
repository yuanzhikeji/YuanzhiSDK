package com.hlife.qcloud.tim.uikit.business.inter;

import com.hlife.qcloud.tim.uikit.business.modal.SearchDataMessage;

import java.util.List;

public interface YzSearchMessageListener {
    void success(List<SearchDataMessage> list);
    void error(int code,String desc);
}
