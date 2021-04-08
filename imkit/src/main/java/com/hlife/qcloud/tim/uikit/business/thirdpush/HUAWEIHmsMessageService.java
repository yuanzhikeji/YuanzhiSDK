package com.hlife.qcloud.tim.uikit.business.thirdpush;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.hlife.qcloud.tim.uikit.utils.BrandUtil;
import com.work.util.SLog;


public class HUAWEIHmsMessageService {

    public static void updateBadge(final Context context, final int number) {
        if (!BrandUtil.isBrandHuawei()) {
            return;
        }
        SLog.i( "huawei badge = " + number);
        try {
            Bundle extra = new Bundle();
            extra.putString("package", "com.work.mw");
            extra.putString("class", "com.work.mw.activity.SplashActivity");
            extra.putInt("badgenumber", number);
            context.getContentResolver().call(Uri.parse("content://com.huawei.android.launcher.settings/badge/"), "change_badge", null, extra);
        } catch (Exception e) {
            SLog.w( "huawei badge exception: " + e.getLocalizedMessage());
        }
    }
}
