package com.hlife.qcloud.tim.uikit.component.picture.imageEngine.impl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.hlife.qcloud.tim.uikit.TUIKit;
import com.hlife.qcloud.tim.uikit.R;
import com.hlife.qcloud.tim.uikit.component.picture.imageEngine.ImageEngine;

import java.io.File;
import java.util.concurrent.ExecutionException;


public class GlideEngine implements ImageEngine {


    public static void loadCornerImage(ImageView imageView, String filePath, RequestListener listener, float radius) {
        CornerTransform transform = new CornerTransform(TUIKit.getAppContext(), radius);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.default_head)
                .transform(transform);
        Glide.with(TUIKit.getAppContext())
                .load(filePath)
                .apply(options)
                .listener(listener)
                .into(imageView);
    }

    public static void loadCornerAvatar(ImageView imageView, String filePath) {
        Glide.with(TUIKit.getAppContext())
                .load(filePath)
                .placeholder(R.drawable.default_head)
                .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                .into(imageView);
    }


    public static void loadImage(ImageView imageView, String filePath, RequestListener listener) {
        Glide.with(TUIKit.getAppContext())
                .load(filePath)
                .listener(listener)
                .into(imageView);
    }


    public static void clear(ImageView imageView) {
        Glide.with(TUIKit.getAppContext()).clear(imageView);
    }

    public static void loadImage(ImageView imageView, Uri uri) {
        if (uri == null) {
            return;
        }
        Glide.with(TUIKit.getAppContext())
                .load(uri)
                .apply(new RequestOptions().error(R.drawable.default_user_icon))
                .into(imageView);
    }

    public static void loadImage(String filePath, String url) {
        try {
            File file = Glide.with(TUIKit.getAppContext()).asFile().load(url).submit().get();
            File destFile = new File(filePath);
            file.renameTo(destFile);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void loadImage(ImageView imageView, Object uri) {
        if (uri == null) {
            return;
        }
        Glide.with(TUIKit.getAppContext())
                .load(uri)
                .apply(new RequestOptions().error(R.drawable.default_head))
                .into(imageView);
    }

    public static void loadImage(ImageView imageView, Object uri,int radius,boolean leftTop, boolean rightTop, boolean leftBottom, boolean rightBottom) {
        if (uri == null) {
            return;
        }
        clear(imageView);
        CornerTransform transform = new CornerTransform(TUIKit.getAppContext(), radius);
        transform.setExceptCorner(leftTop,rightTop,leftBottom,rightBottom);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .transform(transform);
        Glide.with(TUIKit.getAppContext())
                .load(uri)
                .apply(options)
                .into(imageView);
    }

    public static Bitmap loadBitmap(Object imageUrl, int targetImageSize) throws InterruptedException, ExecutionException {
        if (imageUrl == null) {
            return null;
        }
        return Glide.with(TUIKit.getAppContext()).asBitmap()
                .load(imageUrl)
                .apply(new RequestOptions().error(R.drawable.default_head))
                .into(targetImageSize, targetImageSize)
                .get();
    }

    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView,
                                 Uri uri) {
        Glide.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(new RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context)
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        Glide.with(context)
                .asGif()
                .load(uri)
                .apply(new RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView);
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }

}
