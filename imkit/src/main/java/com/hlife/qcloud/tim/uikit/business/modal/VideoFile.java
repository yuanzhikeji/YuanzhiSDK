package com.hlife.qcloud.tim.uikit.business.modal;

import android.graphics.Bitmap;

import java.io.Serializable;

public class VideoFile implements Serializable {
    public String imagePath;
    public String filePath;
    public Bitmap firstFrame;
    public long duration;
}
