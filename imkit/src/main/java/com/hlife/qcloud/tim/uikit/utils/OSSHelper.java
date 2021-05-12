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
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.http.network.listener.OnResultDataListener;
import com.http.network.model.RequestWork;
import com.http.network.model.ResponseWork;
import com.work.api.open.Yz;
import com.work.api.open.model.GetImageConfigResp;
import com.work.api.open.model.client.OpenImageConfig;
import com.work.util.FileUtils;
import com.work.util.SLog;
import com.work.util.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by Administrator on 2019/4/24
 * Description
 */

public class OSSHelper implements OnResultDataListener {
    private final static String Dir="userdir/";
    private OpenImageConfig mConfig;
    private OSS mOss;
    private final BaseActivity mActivity;
    private final Handler mHandler;
    private OnOSSUploadFileListener onOSSUploadFileListener;
    private FileUtils mFile;
    private CustomFileMessage mCustomFile;
    private OSSAsyncTask mTask;

    public OSSHelper(final BaseActivity mActivity) {
        this.mActivity = mActivity;
        mHandler = new Handler(mActivity.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
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
            Yz.getSession().getImageConfig(this,filePath);
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
                mConfig.setEndPoint("https://oss-cn-beijing.aliyuncs.com/");
//                mConfig.setEndPoint("https://yzkj-im.oss-cn-beijing.aliyuncs.com/");
                mOss = new OSSClient(mActivity, mConfig.getEndPoint(), new OSSFederationCredentialProvider() {
                    @Override
                    public OSSFederationToken getFederationToken() {
                        return new OSSFederationToken(mConfig.getAccessKeyId(),mConfig.getAccessKeySecret(),mConfig.getSecurityToken(),mConfig.getExpiration());
                    }
                }, conf);
                String filePath = resp.getPositionParams(0);
                if(mCustomFile==null){
                    asyncPut(filePath);
                }else{
                    getOss();
                }
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
        String name = Dir +UserApi.instance().getUserId()+"/"+file.getName();
        putOss(filePath,name);
    }
    private void putOss(final String filePath,String name){
        if(createOssClient(filePath)){
            PutObjectRequest put = new PutObjectRequest(mConfig.getBucket(), name, filePath);
            // 异步上传时可以设置进度回调
            put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                @Override
                public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                    int progress = (int) (100 * currentSize / totalSize);
                    if(onOSSUploadFileListener!=null){
                        onOSSUploadFileListener.onProgress(progress);
                    }
                    if(SLog.debug) SLog.d("upload: " + progress + "%");
                }
            });
            mTask = mOss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                @Override
                public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                    if(SLog.debug)SLog.v("Bucket: " + mConfig.getBucket()
                            + "\nObject: " + request.getObjectKey()
                            + "\nETag: " + result.getETag()
                            + "\nRequestId: " + result.getRequestId()
                            + "\nCallback: " + result.getServerCallbackReturnBody());
                    String fileUrl = "https://yzkj-im.oss-cn-beijing.aliyuncs.com/"+request.getObjectKey();
                    Message message = mHandler.obtainMessage();
                    message.what = 0;
                    message.obj = new OSSUpload(fileUrl,filePath);
                    mHandler.sendMessage(message);
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

    /**
     * 下载
     */
    public void asynGet(CustomFileMessage customFileMessage){
        if(TextUtils.isEmpty(customFileMessage.getUrl())){
            ToastUtil.error(mActivity, R.string.oss_file_download_error);
            return;
        }
        //检查文件是否存在
        if(mFile==null){
            mFile = new FileUtils(mActivity);
        }
        String filePath = customFileMessage.getFilePath();
        String localFilePath = mFile.getStorageDirectory()+"/"+Dir+customFileMessage.getSendUserId()+"/"+customFileMessage.getFileName();
        boolean isExists = false;
        if(new File(localFilePath).exists()){
            isExists = true;
        }else if(!TextUtils.isEmpty(filePath) && (new File(filePath).exists())){
            localFilePath = filePath;
            isExists = true;
        }
        if(isExists){
            if(onOSSUploadFileListener!=null){
                onOSSUploadFileListener.onSuccess(customFileMessage.getUrl(),localFilePath);
            }
        }else{
            this.mCustomFile = customFileMessage;
            getOss();
        }
    }

    private void getOss(){
        if(createOssClient(mCustomFile.getUrl())){
            String objectKey = Dir+mCustomFile.getSendUserId()+"/"+mCustomFile.getFileName();
            GetObjectRequest getObjectRequest = new GetObjectRequest(mConfig.getBucket(),objectKey);
            getObjectRequest.setProgressListener(new OSSProgressCallback<GetObjectRequest>() {
                @Override
                public void onProgress(GetObjectRequest request, long currentSize, long totalSize) {
                    int progress = (int) (100 * currentSize / totalSize);
                    if(onOSSUploadFileListener!=null){
                        onOSSUploadFileListener.onProgress(progress);
                    }
                    if(SLog.debug) SLog.d("download: " + progress + "%");
                }
            });
            mTask = mOss.asyncGetObject(getObjectRequest, new OSSCompletedCallback<GetObjectRequest, GetObjectResult>() {
                @Override
                public void onSuccess(GetObjectRequest request, GetObjectResult result) {
                    long length = result.getContentLength();
                    byte[] buffer = new byte[(int) length];
                    int readCount = 0;
                    while (readCount < length) {
                        if(mTask == null){
                            return;
                        }
                        try{
                            readCount += result.getObjectContent().read(buffer, readCount, (int) length - readCount);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    try {
                        if(mFile==null){
                            mFile = new FileUtils(mActivity);
                        }
                        String filePath = mFile.getStorageDirectory()+"/"+ Dir+mCustomFile.getSendUserId();
                        File file = new File(filePath);
                        if(!file.exists()){
                            file.mkdirs();
                        }
                        FileOutputStream fileOutputStream = new FileOutputStream(filePath+"/"+mCustomFile.getFileName());
                        fileOutputStream.write(buffer);
                        fileOutputStream.close();
                        Message message = mHandler.obtainMessage();
                        message.what = 0;
                        message.obj = new OSSUpload(mCustomFile.getUrl(),filePath);
                        mHandler.sendMessage(message);
                        if(SLog.debug)SLog.e(mCustomFile.getUrl()+">"+filePath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(GetObjectRequest request, ClientException clientException, ServiceException serviceException) {
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

    public void cancel(){
        if(mTask!=null){
            mTask.cancel();
            mTask=null;
        }
    }

    public void setOnOSSUploadFileListener(OnOSSUploadFileListener onOSSUploadFileListener) {
        this.onOSSUploadFileListener = onOSSUploadFileListener;
    }

    public interface OnOSSUploadFileListener{
        void onSuccess(String fileUrl,String filePath);
        void onProgress(int progress);
        void onError();
    }
}
