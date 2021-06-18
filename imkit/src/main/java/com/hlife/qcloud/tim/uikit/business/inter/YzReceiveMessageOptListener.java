package com.hlife.qcloud.tim.uikit.business.inter;


import java.util.HashMap;

public interface YzReceiveMessageOptListener {
    void result(HashMap<String,Boolean> optMap);
    void error(int code,String desc);
}
