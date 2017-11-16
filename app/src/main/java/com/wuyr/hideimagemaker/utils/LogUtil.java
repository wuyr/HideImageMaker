package com.wuyr.hideimagemaker.utils;

import android.support.annotation.IntDef;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by wuyr on 6/8/16 6:54 PM.
 */
@SuppressWarnings("unused")
public class LogUtil {

    @IntDef({VERBOSE, DEBUG, INFO, WARN, ERROR})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DEBUG_LEVEL {
    }

    public static final int VERBOSE = 1, DEBUG = 2, INFO = 3, WARN = 4, ERROR = 5;

    private static boolean isDebugOn = false;
    private static int debugLevel = DEBUG;

    public static void setDebugOn(boolean isDebugOn) {
        LogUtil.isDebugOn = isDebugOn;
    }

    public static void setDebugLevel(@DEBUG_LEVEL int l) {
        debugLevel = l;
        setDebugOn(true);
    }

    public static void print(Object s) {
        if (isDebugOn)
            if (s != null) {
                StackTraceElement element = Thread.currentThread().getStackTrace()[3];
                switch (debugLevel) {
                    case VERBOSE:
                        Log.v(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.valueOf(s));
                        break;
                    case DEBUG:
                        Log.d(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.valueOf(s));
                        break;
                    case INFO:
                        Log.i(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.valueOf(s));
                        break;
                    case WARN:
                        Log.w(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.valueOf(s));
                        break;
                    case ERROR:
                        Log.e(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.valueOf(s));
                        break;
                    default:
                        break;
                }
            }
    }

    public static void printf(String format, Object... args) {
        if (isDebugOn)
            if (format != null && args != null) {
                StackTraceElement element = Thread.currentThread().getStackTrace()[3];
                switch (debugLevel) {
                    case VERBOSE:
                        Log.v(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.format(format, args));
                        break;
                    case DEBUG:
                        Log.d(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.format(format, args));
                        break;
                    case INFO:
                        Log.i(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.format(format, args));
                        break;
                    case WARN:
                        Log.w(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.format(format, args));
                        break;
                    case ERROR:
                        Log.e(String.format("%s-->%s", element.getClassName(), element.getMethodName()), String.format(format, args));
                        break;
                    default:
                        break;
                }
            }
    }
}