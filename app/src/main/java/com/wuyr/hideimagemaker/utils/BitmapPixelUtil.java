package com.wuyr.hideimagemaker.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;

/**
 * Created by wuyr on 17-11-14 下午4:22.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class BitmapPixelUtil {
    /**
     * 亮, 暗色阶映射表
     */
    private static int[] mLightColorTable, mDarkColorTable;

    /**
     * 生成隐藏图片
     *
     * @param outerBitmap 要在外面才显示的bitmap
     * @param interBitmap 要在里面才显示的bitmap
     * @return 隐藏图片
     */
    public static Bitmap makeHideImage(Bitmap outerBitmap, Bitmap interBitmap) {
        return makeHideImage(outerBitmap, interBitmap, null);
    }

    /**
     * 生成隐藏图片
     *
     * @param outerBitmap 要在外面才显示的bitmap
     * @param interBitmap 要在里面才显示的bitmap
     * @param listener    进度监听
     * @return 隐藏图片
     */
    public static Bitmap makeHideImage(Bitmap outerBitmap, Bitmap interBitmap, OnProgressUpdateListener listener) {
        if (checkBitmapCanUse(outerBitmap) && checkBitmapCanUse(interBitmap)) {
            updateListener(listener, 0.1F);

            去色(outerBitmap);
            updateListener(listener, 0.2F);

            调亮色阶(outerBitmap);
            updateListener(listener, 0.3F);

            反相(outerBitmap);
            updateListener(listener, 0.4F);

            去色(interBitmap);
            updateListener(listener, 0.5F);

            调暗色阶(interBitmap);
            updateListener(listener, 0.6F);

            outerBitmap = 线性减淡(outerBitmap, interBitmap);
            updateListener(listener, 0.7F);

            Bitmap temp = 红色通道(outerBitmap);
            updateListener(listener, 0.8F);

            outerBitmap = 划分(outerBitmap, interBitmap);
            updateListener(listener, 0.9F);

            Bitmap result = 蒙版(outerBitmap, temp);
            updateListener(listener, 1);
            return result;
        }
        return null;
    }

    private static void updateListener(OnProgressUpdateListener listener, float progress) {
        if (listener != null)
            listener.onUpdate(progress);
    }

    /**
     * 将目标bitmap去色 （变成黑白图片）
     *
     * @param target 目标bitmap
     */
    public static void 去色(Bitmap target) {
        final int width = target.getWidth(), height = target.getHeight();
        int[] targetPixels = new int[width * height];
        getBitmapPixelColor(target, (x, y, a, r, g, b) -> {
            int gray = (r + g + b) / 3;
//            bitmap.setPixel(x, y, Color.argb(a, gray, gray, gray));
            targetPixels[x + y * width] = Color.argb(a, gray, gray, gray);
        });
        target.setPixels(targetPixels, 0, width, 0, 0, width, height);
    }

    public static void 调亮色阶(Bitmap target) {
        changeColorLevel(target, true);
    }

    public static void 调暗色阶(Bitmap target) {
        changeColorLevel(target, false);
    }

    public static void 反相(Bitmap target) {
        final int width = target.getWidth(), height = target.getHeight();
        int[] targetPixels = new int[width * height];
        getBitmapPixelColor(target, (x, y, a, r, g, b) -> {
            int max = 255;
            targetPixels[x + y * width] = Color.argb(a, max - r, max - g, max - b);
        });
        target.setPixels(targetPixels, 0, width, 0, 0, width, height);
    }

    /**
     * 图层特效 效果等于PhotoShop中图层特效的线性减淡(增加)
     *
     * @param src    作用特效的bitmap
     * @param target 目标bitmap
     * @return 作用特效后的bitmap
     */
    public static Bitmap 线性减淡(Bitmap src, Bitmap target) {
        final int width = src.getWidth(), height = src.getHeight();
        int[] srcPixels = new int[width * height];
        final Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        getBitmapPixelColor(src, (x, y, a, r, g, b) -> {
            int dstR, dstG, dstB, dstPixelColor, resultA, resultR, resultG, resultB;
            dstPixelColor = target.getPixel(x, y);
            dstR = Color.red(dstPixelColor);
            dstG = Color.green(dstPixelColor);
            dstB = Color.blue(dstPixelColor);
            resultA = 255;
            resultR = r + dstR;
            resultG = g + dstG;
            resultB = b + dstB;
            if (resultR > 255)
                resultR = 255;
            if (resultG > 255)
                resultG = 255;
            if (resultB > 255)
                resultB = 255;
//                result.setPixel(x, y, Color.argb(resultA, resultR, resultG, resultB));
            srcPixels[x + y * width] = Color.argb(resultA, resultR, resultG, resultB);
        });
        result.setPixels(srcPixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 效果类似PhotoShop中的取图层红色通道中的选区
     * (因为我们等下的蒙板只是取目标bitmap的透明度, 而反相和调整色阶都只是更改rgb色值,
     * 并没有影响到透明度, 所以我们为了执行效率, 直接把red色值作为透明度后返回,
     * 当然, 如需和PhotoShop上的 红色通道 的选区效果一样, 可以把下面的3行注释取消, 其实不会影响到最终生成图片的效果的)
     *
     * @param target 目标bitmap
     * @return 目标bitmap的红色通道
     */
    public static Bitmap 红色通道(Bitmap target) {
        final int width = target.getWidth(), height = target.getHeight();
        int[] targetPixels = new int[width * height];
        final Bitmap result = Bitmap.createBitmap(target.getWidth(), target.getHeight(), Bitmap.Config.ARGB_8888);
        getBitmapPixelColor(target, (x, y, a, r, g, b) -> targetPixels[x + y * width] = Color.argb(r, r, g, b));
        result.setPixels(targetPixels, 0, width, 0, 0, width, height);
//        反相(result);
//        for (int i = 0; i < 5; i++)
//            调暗色阶(result);
        return result;
    }

    /**
     * 图层特效 效果等于PhotoShop中图层特效的划分
     *
     * @param src    作用特效的bitmap
     * @param target 目标bitmap
     * @return 作用特效后的bitmap
     */
    public static Bitmap 划分(Bitmap src, Bitmap target) {
        final int width = src.getWidth(), height = src.getHeight();
        int[] srcPixels = new int[width * height];
        final Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        getBitmapPixelColor(src, (x, y, a, r, g, b) -> {
            int dstR, dstG, dstB, dstPixelColor, resultA, resultR, resultG, resultB;
            dstPixelColor = target.getPixel(x, y);
            dstR = Color.red(dstPixelColor);
            dstG = Color.green(dstPixelColor);
            dstB = Color.blue(dstPixelColor);
            resultA = 255;
            resultR = (int) (255 / (((float) r / (float) dstR)));
            resultG = (int) (255 / (((float) g / (float) dstG)));
            resultB = (int) (255 / (((float) b / (float) dstB)));
//                result.setPixel(x, y, Color.argb(resultA, resultR, resultG, resultB));
            srcPixels[x + y * width] = Color.argb(resultA, resultR, resultG, resultB);
        });
        result.setPixels(srcPixels, 0, width, 0, 0, width, height);
        return result;
    }

    /**
     * 将bitmap的透明度换成目标bitmap的透明度
     *
     * @param src    作用蒙板的bitmap
     * @param target 目标bitmap
     * @return 添加蒙板后的bitmap
     */
    public static Bitmap 蒙版(Bitmap src, Bitmap target) {
        final int width = src.getWidth(), height = src.getHeight();
        int[] srcPixels = new int[width * height];
        final Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        getBitmapPixelColor(src, (x, y, a, r, g, b) -> {
            int dstA, dstPixelColor;
            dstPixelColor = target.getPixel(x, y);
            dstA = Color.alpha(dstPixelColor);
//                result.setPixel(x, y, Color.argb(dstA, r, g, b));
            srcPixels[x + y * width] = Color.argb(dstA, r, g, b);
        });
        result.setPixels(srcPixels, 0, width, 0, 0, width, height);
        return result;
    }

    public static Bitmap scaleBitmap(Bitmap target, int w, int h) {
        if (target == null || target.isRecycled()) return target;
        int width = target.getWidth(), height = target.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(((float) w / width), ((float) h / height));
        return Bitmap.createBitmap(target, 0, 0, width, height, matrix, true);
    }

    private static void changeColorLevel(Bitmap target, boolean isToLight) {
        final int width = target.getWidth(), height = target.getHeight();
        int[] targetPixels = new int[width * height];
        int[] table = isToLight ? getLightColorTable() : getDarkColorTable();
        getBitmapPixelColor(target, (x, y, a, r, g, b) -> targetPixels[x + y * width] = Color.argb(a, table[r], table[g], table[b]));
        target.setPixels(targetPixels, 0, width, 0, 0, width, height);
    }

    private static boolean checkBitmapCanUse(Bitmap target) {
        return target != null && !target.isRecycled() && target.isMutable();
    }

    private static void getBitmapPixelColor(Bitmap target, PixelColorHandler handler) {
        if (checkBitmapCanUse(target) && handler != null) {
            int a, r, g, b, pixelColor;
            for (int y = 0; y < target.getHeight(); y++) {
                for (int x = 0; x < target.getWidth(); x++) {
                    pixelColor = target.getPixel(x, y);
                    a = Color.alpha(pixelColor);
                    r = Color.red(pixelColor);
                    g = Color.green(pixelColor);
                    b = Color.blue(pixelColor);
                    handler.onHandle(x, y, a, r, g, b);
                }
            }
        }
    }

    private static int[] getLightColorTable() {
        if (mLightColorTable == null)
            initLightColorTable();
        return mLightColorTable;
    }

    private static int[] getDarkColorTable() {
        if (mDarkColorTable == null)
            initDarkColorTable();
        return mDarkColorTable;
    }

    private static void initLightColorTable() {
//        输出色阶 120 ～ 255 的映射表        
//        由 getColorLevelTable(120, 255); 得来
        mLightColorTable = new int[]{
                120, 120, 121, 121, 122, 122, 123, 123, 124, 124, 125, 125, 126, 126, 127, 127, 128, 128,
                129, 129, 130, 130, 131, 132, 132, 133, 133, 134, 134, 135, 135, 136, 136, 137, 137, 138,
                138, 139, 139, 140, 140, 141, 142, 142, 143, 143, 144, 144, 145, 145, 146, 146, 147, 147,
                148, 148, 149, 149, 150, 150, 151, 152, 152, 153, 153, 154, 154, 155, 155, 156, 156, 157,
                157, 158, 158, 159, 159, 160, 161, 161, 162, 162, 163, 163, 164, 164, 165, 165, 166, 166,
                167, 167, 168, 168, 169, 170, 170, 171, 171, 172, 172, 173, 173, 174, 174, 175, 175, 176,
                176, 177, 177, 178, 179, 179, 180, 180, 181, 181, 182, 182, 183, 183, 184, 184, 185, 185,
                186, 186, 187, 188, 188, 189, 189, 190, 190, 191, 191, 192, 192, 193, 193, 194, 194, 195,
                195, 196, 197, 197, 198, 198, 199, 199, 200, 200, 201, 201, 202, 202, 203, 203, 204, 205,
                205, 206, 206, 207, 207, 208, 208, 209, 209, 210, 210, 211, 211, 212, 212, 213, 214, 214,
                215, 215, 216, 216, 217, 217, 218, 218, 219, 219, 220, 220, 221, 222, 222, 223, 223, 224,
                224, 225, 225, 226, 226, 227, 227, 228, 228, 229, 229, 230, 231, 231, 232, 232, 233, 233,
                234, 234, 235, 235, 236, 236, 237, 237, 238, 239, 239, 240, 240, 241, 241, 242, 242, 243,
                243, 244, 244, 245, 245, 246, 247, 247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252,
                253, 253, 254, 255,
        };
    }

    private static void initDarkColorTable() {
//        输出色阶 0 ～ 135 的映射表
//        由 getColorLevelTable(0, 135); 得来
        mDarkColorTable = new int[]{0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10,
                10, 11, 12, 12, 13, 13, 14, 14, 15, 15, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20, 21,
                22, 22, 23, 23, 24, 24, 25, 25, 26, 26, 27, 27, 28, 28, 29, 29, 30, 30, 31, 32, 32,
                33, 33, 34, 34, 35, 35, 36, 36, 37, 37, 38, 38, 39, 39, 40, 41, 41, 42, 42, 43, 43,
                44, 44, 45, 45, 46, 46, 47, 47, 48, 48, 49, 50, 50, 51, 51, 52, 52, 53, 53, 54, 54,
                55, 55, 56, 56, 57, 57, 58, 59, 59, 60, 60, 61, 61, 62, 62, 63, 63, 64, 64, 65, 65,
                66, 66, 67, 68, 68, 69, 69, 70, 70, 71, 71, 72, 72, 73, 73, 74, 74, 75, 75, 76, 77,
                77, 78, 78, 79, 79, 80, 80, 81, 81, 82, 82, 83, 83, 84, 85, 85, 86, 86, 87, 87, 88,
                88, 89, 89, 90, 90, 91, 91, 92, 92, 93, 94, 94, 95, 95, 96, 96, 97, 97, 98, 98, 99,
                99, 100, 100, 101, 102, 102, 103, 103, 104, 104, 105, 105, 106, 106, 107, 107, 108,
                108, 109, 109, 110, 111, 111, 112, 112, 113, 113, 114, 114, 115, 115, 116, 116, 117,
                117, 118, 119, 119, 120, 120, 121, 121, 122, 122, 123, 123, 124, 124, 125, 125, 126,
                127, 127, 128, 128, 129, 129, 130, 130, 131, 131, 132, 132, 133, 133, 134, 135,
        };
    }

    /**
     * @param outputMin 输出色阶
     * @param outputMax 输出色阶
     * @return 色值映射表
     */
    private static int[] getColorLevelTable(int outputMin, int outputMax) {
        int[] data = new int[256];
        int inputMin = 0, inputMiddle = 128, inputMax = 255;
        if (outputMin < 0) outputMin = 0;
        if (outputMin > 255) outputMin = 255;
        if (outputMax < 0) outputMax = 0;
        if (outputMax > 255) outputMax = 255;
        for (int index = 0; index <= 255; index++) {
            double temp = index - inputMin;
            if (temp < 0) {
                temp = outputMin;
            } else if (temp + inputMin > inputMax) {
                temp = outputMax;
            } else {
                double gamma = Math.log(0.5) / Math.log((double) (inputMiddle - inputMin) / (inputMax - inputMin));
                temp = outputMin + (outputMax - outputMin) * Math.pow((temp / (inputMax - inputMin)), gamma);
            }
            if (temp > 255)
                temp = 255;
            else if (temp < 0)
                temp = 0;
            data[index] = (int) temp;
        }
        return data;
    }

    private interface PixelColorHandler {
        void onHandle(int x, int y, int a, int r, int g, int b);
    }

    public interface OnProgressUpdateListener {
        void onUpdate(float progress);
    }
}
