package com.hlife.data;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.tencent.imsdk.v2.V2TIMFriendInfo;
import com.tencent.imsdk.v2.V2TIMManager;
import com.tencent.imsdk.v2.V2TIMValueCallback;
import com.work.api.open.Yz;
import com.work.api.open.model.BaseResp;
import com.work.api.open.model.RemarkListGetResponse;
import com.work.api.open.model.client.UserRemark;
import com.work.util.FileUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Collection;
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
    private final ConcurrentMap<String, UserRemark> friendRemarks;
    private final Gson gson = new Gson();

    private IMFriendManager() {
        friendRemarks = new ConcurrentHashMap<>();
        retryTimes = 0;
    }

    public String getFriendRemark(String userId) {
        UserRemark userRemark = friendRemarks.get(userId);
        if (userRemark != null) {
//            Log.i(TAG, "SDKLOG get remark " + userId + ", got" + userRemark.getRemark());
            return userRemark.getRemark();
        }
//        Log.i(TAG, "SDKLOG get remark " + userId + ", nothing");
        return null;
    }

    public void setup() {
//        Log.i(TAG, "SDKLOG setup friend manager");
        retryTimes = 0;
        friendRemarks.clear();
        loadFromLocalFile();
        loadBackendRemarks();
    }

    public void clear() {
//        Log.i(TAG, "SDKLOG clear friend manager");
        friendRemarks.clear();
        retryTimes = 0;
    }

    public void updateFriendRemark(String userId, String friendRemark, YzUpdateFriendCallback callback) {
        String currentId = UserApi.instance().getUserId();
        OnResultDataListener listener = new OnResultDataListener() {
            @Override
            public void onResult(RequestWork req, ResponseWork resp) throws Exception {
                if (resp.isSuccess()) {
                    if (TextUtils.isEmpty(friendRemark)) {
                        friendRemarks.remove(userId);
                    } else {
                        friendRemarks.put(userId, new UserRemark(currentId, userId, friendRemark));
                    }
                    saveToLocalFile();
                    if (callback != null) {
                        callback.success();
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
                    if (callback != null) {
                        callback.error(code, desc);
                    }
                }
            }
        };
        if (TextUtils.isEmpty(friendRemark)) {
//            Log.i(TAG, "SDKLOG delete remark for " + userId);
            Yz.getSession().deleteRemark(userId, listener);
        } else {
//            Log.i(TAG, "SDKLOG set remark for " + userId + " with " + friendRemark);
            Yz.getSession().updateRemark(userId, friendRemark, listener);
        }
    }

    private void loadBackendRemarks() {
        Yz.getSession().getRemarkList(new OnResultDataListener() {
            @Override
            public void onResult(RequestWork req, ResponseWork resp) throws Exception {
                if (resp.isSuccess()) {
                    if (resp instanceof RemarkListGetResponse) {
                        RemarkListGetResponse response = (RemarkListGetResponse)resp;
                        if (response.getData() != null) {
                            String currentId = UserApi.instance().getUserId();
                            Map<String, UserRemark> remarks = new HashMap<>();
                            boolean illegal = false;
                            for (UserRemark userRemark: response.getData()) {
                                if (currentId.equals(userRemark.getFromUserId())) {
                                    remarks.put(userRemark.getToUserId(), userRemark);
                                } else {
                                    illegal = true;
                                    break;
                                }
                            }
                            if (illegal) {
                                Log.e(TAG, "illegal user remarks, not current login user's");
                            } else {
                                friendRemarks.clear();
                                friendRemarks.putAll(remarks);
                            }
                        }
                    } else {
                        Log.e(TAG, "unknown response class: " + resp);
                        retry();
                    }
                } else {
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
                    Log.e(TAG, "Failed to load backend remarks: " + code + ", desc: " + desc);
                    retry();
                }
            }
        });
    }

    private void retry() {
        if (retryTimes < 3) {
            retryTimes++;
            loadBackendRemarks();
        }
    }

    private String getFilePath() {
        String currentId = UserApi.instance().getUserId();
        return String.format("%s/%s_remarks.json", IMKitConstants.APP_DIR, currentId);
    }

    private void loadFromLocalFile() {
        String path = getFilePath();
        Type typeOf = new TypeToken<Collection<UserRemark>>(){}.getType();
        try {
            Collection<UserRemark> userRemarks = gson.fromJson(new FileReader(path), typeOf);
            Map<String, UserRemark> remarkMap = new HashMap<>();
            for (UserRemark remark: userRemarks) {
                remarkMap.put(remark.getToUserId(), remark);
            }
            friendRemarks.clear();
            friendRemarks.putAll(remarkMap);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "failed to read file: " + path, e);
        }
    }

    private void saveToLocalFile() {
        String path = getFilePath();
        Type typeOf = new TypeToken<Collection<UserRemark>>(){}.getType();
        Collection<UserRemark> remarks = friendRemarks.values();
        String content = gson.toJson(remarks, typeOf);
        FileUtils.writeString(path, content);
    }
}
