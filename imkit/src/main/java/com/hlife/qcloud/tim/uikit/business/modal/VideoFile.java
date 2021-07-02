package com.hlife.qcloud.tim.uikit.business.modal;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class VideoFile implements Parcelable {
    public String imagePath;
    public String filePath;
    public Bitmap firstFrame;
    public long duration;

    public VideoFile(){

    }

    protected VideoFile(Parcel in) {
        imagePath = in.readString();
        filePath = in.readString();
        firstFrame = in.readParcelable(Bitmap.class.getClassLoader());
        duration = in.readLong();
    }

    public static final Creator<VideoFile> CREATOR = new Creator<VideoFile>() {
        @Override
        public VideoFile createFromParcel(Parcel in) {
            return new VideoFile(in);
        }

        @Override
        public VideoFile[] newArray(int size) {
            return new VideoFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(imagePath);
        parcel.writeString(filePath);
        parcel.writeParcelable(firstFrame, i);
        parcel.writeLong(duration);
    }
}
