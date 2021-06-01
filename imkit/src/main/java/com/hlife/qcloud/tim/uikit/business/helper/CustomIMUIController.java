package com.hlife.qcloud.tim.uikit.business.helper;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.business.active.OSSFileActivity;
import com.hlife.qcloud.tim.uikit.business.active.WebActivity;
import com.hlife.qcloud.tim.uikit.business.message.CustomFileMessage;
import com.hlife.qcloud.tim.uikit.business.message.CustomMessage;
import com.hlife.qcloud.tim.uikit.component.picture.imageEngine.impl.GlideEngine;
import com.hlife.qcloud.tim.uikit.modules.chat.layout.message.holder.YzCustomMessageViewGroup;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.utils.FileUtil;

import java.io.File;

public class CustomIMUIController {


    public static void onDrawCard(YzCustomMessageViewGroup parent, final CustomMessage data) {
        // 把自定义消息view添加到TUIKit内部的父容器里
        View view = LayoutInflater.from(TUIKit.getAppContext()).inflate(R.layout.test_custom_message_layout1, null, false);
        parent.addMessageContentView(view);

        // 自定义消息view的实现，这里仅仅展示文本信息，并且实现超链接跳转
        TextView mTitle = view.findViewById(R.id.title);
        TextView mDesc = view.findViewById(R.id.desc);
        ImageView mLogo = view.findViewById(R.id.logo);
        final String text = "不支持的自定义消息";
        if (data == null) {
            mTitle.setText(text);
        } else {
            mTitle.setText(data.getTitle());
            mDesc.setText(data.getDesc());
            GlideEngine.loadImage(mLogo,data.getLogo());
            view.setClickable(true);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WebActivity.startWebView(data.getLink());
                }
            });
        }
    }
    public static void onDrawFile(YzCustomMessageViewGroup parent, final CustomFileMessage data){
        View view = LayoutInflater.from(TUIKit.getAppContext()).inflate(R.layout.message_adapter_content_file, null, false);
        TextView fileNameText = view.findViewById(R.id.file_name_tv);
        TextView fileSizeText = view.findViewById(R.id.file_size_tv);
        TextView fileStatusText = view.findViewById(R.id.file_status_tv);
        ImageView fileIconImage = view.findViewById(R.id.file_icon_iv);
        parent.addMessageContentView(view);
        String fileName = data.getFileName();
        fileNameText.setText(fileName);
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
        String size = FileUtil.FormetFileSize(data.getFileSize());
        fileSizeText.setText(size);
        fileStatusText.setVisibility(View.GONE);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OSSFileActivity.downloadFile(view.getContext(),data);
            }
        });
    }
}
