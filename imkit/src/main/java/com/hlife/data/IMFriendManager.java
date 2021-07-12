package com.hlife.data;

import android.text.TextUtils;
import android.util.Log;

import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.api.open.Yz;
import com.work.api.open.model.BaseResp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IMFriendManager {
    private static final String TAG = IMFriendManager.class.getSimpleName();

    private static IMFriendManager singleton;
    public static IMFriendManager getInstance() {
        if (singleton == null) {
            synchronized (IMFriendManager.class) {
                if (singleton == null) {
                    singleton = new IMFriendManager();
                }
            }
        }
        return singleton;
    }

    private int retryTimes = 0;
    private final ConcurrentMap<String, String> friendRemarks;
    private IMFriendManager() {
        friendRemarks = new ConcurrentHashMap<>();
        loadAllFriends();
    }

    public String getFriendRemark(String userId) {
        return friendRemarks.get(userId);
    }

    public void clear() {
        friendRemarks.clear();
        retryTimes = 0;
    }

    public void updateFriendRemark(String userId, String friendRemark, YzUpdateFriendListener listener) {
        Yz.getSession().updateFriend(userId, friendRemark, new OnResultDataListener() {
            @Override
            public void onResult(RequestWork req, ResponseWork resp) throws Exception {
                if (resp.isSuccess()) {
                    friendRemarks.put(userId, friendRemark);
                    if (listener != null) {
                        listener.success();
                    }
                } else {
                    Log.e(TAG, "failed to update friend remark: " + resp);
                    int code;
                    String desc;
                    if (resp instanceof BaseResp) {
                        BaseResp baseResp = (BaseResp)resp;
                        code = baseResp.getCode();
                        desc = baseResp.getMessage();
                    } else {
                        code = resp.getHttpCode();
                        desc = resp.getMessage();
                    }
                    if (listener != null) {
                        listener.error(code, desc);
                    }
                }
            }
        });
    }

    private void loadAllFriends() {
        V2TIMManager.getFriendshipManager().getFriendList(new V2TIMValueCallback<List<V2TIMFriendInfo>>() {
            @Override
            public void onSuccess(List<V2TIMFriendInfo> v2TIMFriendInfos) {
                retryTimes = 0;
                for (V2TIMFriendInfo info: v2TIMFriendInfos) {
                    if (!TextUtils.isEmpty(info.getFriendRemark())) {
                        friendRemarks.put(info.getUserID(), info.getFriendRemark());
                    }
                }
            }

            @Override
            public void onError(int code, String desc) {
                Log.e(TAG, "Failed to load tim friends: " + code + ", desc: " + desc);
                retry();
            }
        });
    }

    private void retry() {
        if (retryTimes < 3) {
            retryTimes++;
            loadAllFriends();
        }
    }
}
