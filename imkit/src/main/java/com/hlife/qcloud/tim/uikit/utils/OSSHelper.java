package com.hlife.qcloud.tim.uikit.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.work.api.open.model.GetImageConfigResp;
import com.work.api.open.model.client.OpenImageConfig;
import com.work.util.FileUtils;
import com.work.util.SLog;
import com.work.util.ToastUtil;

import java.io.File;

/**
 * Created by Administrator on 2019/4/24
 * Description
 */

public class OSSHelper implements OnResultDataListener {
    private OpenImageConfig mConfig;
    private OSS mOss;
    private BaseActivity mActivity;
    private Handler mHandler;
    private OnOSSUploadFileListener onOSSUploadFileListener;
    private FileUtils mFile;

    public OSSHelper(final BaseActivity mActivity) {
        this.mActivity = mActivity;
        mFile = new FileUtils(mActivity);
        mHandler = new Handler(mActivity.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                mActivity.dismissProgress();
                switch (msg.what){
                    case -1://上传失败
                        if(onOSSUploadFileListener!=null){
                            onOSSUploadFileListener.onError();
                        }
                        break;
                    case 0:
                        OSSUpload ossUpload = (OSSUpload) msg.obj;
                        if(onOSSUploadFileListener!=null){
                            onOSSUploadFileListener.onSuccess(ossUpload.fileUrl,ossUpload.filePath);
                        }
                        break;
                }
            }
        };
    }

    private boolean createOssClient(String filePath){
        if(mOss == null){
//            Yz.getSession().getImageConfig(this,filePath);
            return false;
        }
        return true;
    }

    @Override
    public void onResult(RequestWork req, ResponseWork resp) throws Exception {
        mActivity.onResult(req,resp);
        if(resp instanceof GetImageConfigResp){
            if(resp.isSuccess()){
                ClientConfiguration conf = new ClientConfiguration();
                conf.setConnectionTimeout(15 * 1000); // 连接超时，默认15秒
                conf.setSocketTimeout(15 * 1000); // socket超时，默认15秒
                conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
                conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
                mConfig = ((GetImageConfigResp) resp).getData();
                mOss = new OSSClient(mActivity, mConfig.getEndPoint(), new OSSFederationCredentialProvider() {
                    @Override
                    public OSSFederationToken getFederationToken() {
                        return new OSSFederationToken(mConfig.getKeyId(),mConfig.getKeySecret(),mConfig.getToken(),mConfig.getExpiration());
                    }
                }, conf);
                String filePath = resp.getPositionParams(0);
                asyncPut(filePath);
            }
        }
    }
    /**
     * 上传
     */
    public void asyncPut(final String filePath){
        if(TextUtils.isEmpty(filePath)){
            ToastUtil.error(mActivity, R.string.toast_file_error);
            return;
        }
        File file = new File(filePath);
        if(!file.exists()){
            ToastUtil.error(mActivity, R.string.toast_file_error);
            return;
        }
        putOss(filePath,file.getName());
    }
    private void putOss(final String filePath,String name){
        if(createOssClient(filePath)){
            PutObjectRequest put = new PutObjectRequest(mConfig.getBucket(), name, filePath);
            // 异步上传时可以设置进度回调
            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                @Override
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    int progress = (int) (100 * currentSize / totalSize);
                    if(SLog.debug) SLog.d("progress: " + String.valueOf(progress) + "%");
                }
            });
            mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                @Override
                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                    if(SLog.debug)SLog.v("Bucket: " + mConfig.getBucket()
                            + "\nObject: " + request.getObjectKey()
                            + "\nETag: " + result.getETag()
                            + "\nRequestId: " + result.getRequestId()
                            + "\nCallback: " + result.getServerCallbackReturnBody());
                    String fileUrl = mConfig.getEndPoint()+request.getObjectKey();
                    Message message = mHandler.obtainMessage();
                    message.what = 0;
                    message.obj = new OSSUpload(fileUrl,filePath);
                    mHandler.sendMessage(message);
                    File file = new File(filePath);
                    if(file.exists()) file.delete();
                }

                @Override
                public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                    // 请求异常
                    if (clientException != null) {
                        // 本地异常如网络异常等
                        clientException.printStackTrace();
                    }
                    if (serviceException != null) {
                        serviceException.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(-1);
                }
            });
        }
    }

    static class OSSUpload{
        String fileUrl;
        String filePath;

        OSSUpload(String fileUrl, String filePath) {
            this.fileUrl = fileUrl;
            this.filePath = filePath;
        }
    }

    public void setOnOSSUploadFileListener(OnOSSUploadFileListener onOSSUploadFileListener) {
        this.onOSSUploadFileListener = onOSSUploadFileListener;
    }

    public interface OnOSSUploadFileListener{
        void onSuccess(String fileUrl,String filePath);
        void onError();
    }
}
