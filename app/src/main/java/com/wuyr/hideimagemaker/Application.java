package com.wuyr.hideimagemaker;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by wuyr on 17-11-15 下午7:53.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Fresco.initialize(this);
    }
}
