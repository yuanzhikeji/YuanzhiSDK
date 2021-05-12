package com.hlife.qcloud.tim.uikit.business.active;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.base.BaseActivity;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.modal.UserApi;
import com.hlife.qcloud.tim.uikit.business.modal.VideoFile;
import com.hlife.qcloud.tim.uikit.utils.FileUtil;
import com.hlife.qcloud.tim.uikit.utils.IMKitConstants;
import com.hlife.qcloud.tim.uikit.utils.OSSHelper;
import com.work.util.ToastUtil;

import java.io.File;

public class OSSFileActivity extends BaseActivity {

    private final static String UPLOAD_FILE="UPLOAD_FILE";
    private ImageView fileIconImage;
    private TextView mName;
    private TextView mSize;
    private ProgressBar mProgress;
    private Button mOpenBtn;
    private OSSHelper mOSS;

    @Override
    public void onInitView() throws Exception {
        super.onInitView();
        fileIconImage = findViewById(R.id.file);
        mName = findViewById(R.id.name);
        mSize = findViewById(R.id.size);
        mProgress = findViewById(R.id.progress);
        mOpenBtn = findViewById(R.id.open_file);
    }

    @Override
    public void onInitValue() throws Exception {
        super.onInitValue();
        boolean isUpload = getIntent().getBooleanExtra(UPLOAD_FILE,true);
        setTitleName(isUpload?R.string.oss_file_title_upload:R.string.oss_file_title_download);
        if(isUpload){
            Uri mUri = getIntent().getParcelableExtra(OSSFileActivity.class.getSimpleName());
            if(mUri ==null){
                ToastUtil.error(this,R.string.toast_file_error);
                finish();
                return;
            }
            new FileTask().execute(mUri);
        }else{
            CustomFileMessage customFileMessage = (CustomFileMessage) getIntent().getSerializableExtra(OSSFileActivity.class.getSimpleName());
            if(customFileMessage==null){
                finish();
                return;
            }
            mName.setText(customFileMessage.getFileName());
            mSize.setText(FileUtil.FormetFileSize(customFileMessage.getFileSize()));
            mOSS = new OSSHelper(this);
            mOSS.setOnOSSUploadFileListener(new OSSHelper.OnOSSUploadFileListener() {
                @Override
                public void onSuccess(String fileUrl, String filePath) {
                    mOpenBtn.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.GONE);
                    mOpenBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            X5FileOpenActivity.openX5File(OSSFileActivity.this,filePath,customFileMessage.getFileName());
                        }
                    });
                }

                @Override
                public void onProgress(int progress) {
                    mProgress.post(() -> mProgress.setProgress(progress));
                }

                @Override
                public void onError() {

                }
            });
            mOSS.asynGet(customFileMessage);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mOSS!=null){
            mOSS.cancel();
        }
    }

    private class FileTask extends AsyncTask<Uri,Void,Object> {

        @Override
        protected Object doInBackground(Uri... uris) {
            Uri fileUri = uris[0];
            String filePath = FileUtil.getPathFromUri(fileUri);
            File file = new File(filePath);
            if (file.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(filePath,options);
                if(options.outWidth != -1){//是图片
                    return fileUri;
                }
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
                if(mimeType!=null && mimeType.contains("video")){//是视频
                    Bitmap firstFrame = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
                    String imagePath = FileUtil.saveBitmap("JCamera", firstFrame);
                    long duration = 0;
                    try {
                        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
                        mediaMetadataRetriever.setDataSource(filePath);
                        duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                    }catch (Exception ignore){}
                    VideoFile videoFile = new VideoFile();
                    videoFile.imagePath = imagePath;
                    videoFile.filePath = filePath;
                    videoFile.firstFrame = firstFrame;
                    videoFile.duration = duration;
                    return videoFile;
                }
                CustomFileMessage customFileMessage = new CustomFileMessage();
                customFileMessage.setFileName(file.getName());
                customFileMessage.setFilePath(filePath);
                customFileMessage.setFileSize(file.length());
                customFileMessage.setSendUserId(UserApi.instance().getUserId());
                return customFileMessage;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(o!=null){
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                if(o instanceof Uri){
                    bundle.putParcelable(OSSFileActivity.class.getSimpleName(),(Uri)o);
                    intent.putExtras(bundle);
                    setResult(200,intent);
                    finish();
                }else if(o instanceof VideoFile){
                    bundle.putSerializable(OSSFileActivity.class.getSimpleName(),(VideoFile)o);
                    intent.putExtras(bundle);
                    setResult(200,intent);
                    finish();
                }else if(o instanceof CustomFileMessage){
                    CustomFileMessage customFileMessage = (CustomFileMessage) o;
                    String fileName = customFileMessage.getFileName();
                    mName.setText(fileName);
                    mSize.setText(FileUtil.FormetFileSize(customFileMessage.getFileSize()));
                    if(fileName.endsWith(".doc") || fileName.endsWith(".docx")){
                        fileIconImage.setImageResource(R.drawable.icon_word_fill);
                    }else if(fileName.endsWith(".pdf")){
                        fileIconImage.setImageResource(R.drawable.icon_pdf_fill);
                    }else if(fileName.endsWith(".xls") || fileName.endsWith(".xlsx")){
                        fileIconImage.setImageResource(R.drawable.icon_xsl_fill);
                    }else if(fileName.endsWith("ppt") || fileName.endsWith("pptx")){
                        fileIconImage.setImageResource(R.drawable.icon_ppt_fill);
                    }else if(fileName.endsWith(".zip") || fileName.endsWith(".rar")){
                        fileIconImage.setImageResource(R.drawable.icon_zip_fill);
                    }else if(fileName.endsWith(".txt")){
                        fileIconImage.setImageResource(R.drawable.icon_txt_fill);
                    }else{
                        fileIconImage.setImageResource(R.drawable.icon_default_fill);
                    }
                    mOSS = new OSSHelper(OSSFileActivity.this);
                    mOSS.setOnOSSUploadFileListener(new OSSHelper.OnOSSUploadFileListener() {
                        @Override
                        public void onSuccess(String fileUrl, String filePath) {
                            customFileMessage.setUrl(fileUrl);
                            customFileMessage.setBusinessID(IMKitConstants.BUSINESS_ID_CUSTOM_FILE);
                            bundle.putSerializable(OSSFileActivity.class.getSimpleName(),customFileMessage);
                            intent.putExtras(bundle);
                            setResult(200,intent);
                            finish();
                        }

                        @Override
                        public void onProgress(int progress) {
                            mProgress.post(() -> mProgress.setProgress(progress));
                        }

                        @Override
                        public void onError() {
                            ToastUtil.error(OSSFileActivity.this,R.string.oss_file_upload_error);
                        }
                    });
                    mOSS.asyncPut(customFileMessage.getFilePath());
                }

            }

        }
    }

    public static void uploadFile(Fragment fragment, Uri uri, int requestCode){
        Intent intent = new Intent(fragment.getContext(),OSSFileActivity.class);
        intent.putExtra(UPLOAD_FILE,true);
        intent.putExtra(OSSFileActivity.class.getSimpleName(),uri);
        fragment.startActivityForResult(intent,requestCode);
    }
    public static void downloadFile(Context context,CustomFileMessage customFileMessage){
        Intent intent = new Intent(context,OSSFileActivity.class);
        intent.putExtra(UPLOAD_FILE,false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(OSSFileActivity.class.getSimpleName(),customFileMessage);
        context.startActivity(intent);
    }
}
