package com.kymjs.core.bitmap.diskloader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kymjs.core.bitmap.DiskImageDisplayer;
import com.kymjs.core.bitmap.client.BitmapCore;
import com.kymjs.core.bitmap.client.BitmapRequestConfig;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.http.Response;
import com.kymjs.rxvolley.http.VolleyError;
import com.kymjs.rxvolley.toolbox.FileUtils;

import java.io.FileInputStream;
import java.util.HashMap;

/**
 * @author kymjs (http://www.kymjs.com/) on 12/21/15.
 */
public class AsyncPoster implements Runnable {
    protected final PendingPostQueue queue;
    protected final DiskImageDisplayer displayer;

    public AsyncPoster(DiskImageDisplayer displayer) {
        queue = new PendingPostQueue();
        this.displayer = displayer;
    }

    public void enqueue(BitmapRequestConfig config, HttpCallback callback) {
        PendingPost pendingPost = PendingPost.obtainPendingPost(config, callback);
        queue.enqueue(pendingPost);
        BitmapCore.getExecutorService().execute(this);
    }

    @Override
    public void run() {
        PendingPost pendingPost = queue.poll();
        if (pendingPost == null) {
            throw new IllegalStateException("No pending post available");
        }
        loadFromFile(pendingPost.config.mUrl, pendingPost.config.maxWidth,
                pendingPost.config.maxHeight, pendingPost.callback);
        PendingPost.releasePendingPost(pendingPost);
    }

    /**
     * 从本地载入一张图片
     *
     * @param path 图片的地址
     */
    protected byte[] loadFromFile(String path, int maxWidth, int maxHeight, HttpCallback callback) {
        byte[] data = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
            data = FileUtils.input2byte(fis);
            handleBitmap(path, data, maxWidth, maxHeight, callback);
        } catch (Exception e) {
            displayer.post(path, callback, Response.<Bitmap>error(new VolleyError(e)));
        } finally {
            FileUtils.closeIO(fis);
        }
        return data;
    }

    private Bitmap handleBitmap(String path, byte[] data, int maxWidth, int maxHeight,
                                HttpCallback callback) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        Bitmap bitmap;
        if (maxWidth <= 0 && maxHeight <= 0) {
            bitmap = BitmapFactory
                    .decodeByteArray(data, 0, data.length, option);
        } else {
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length, option);
            int actualWidth = option.outWidth;
            int actualHeight = option.outHeight;

            // 计算出图片应该显示的宽高
            int desiredWidth = getResizedDimension(maxWidth, maxHeight,
                    actualWidth, actualHeight);
            int desiredHeight = getResizedDimension(maxHeight, maxWidth,
                    actualHeight, actualWidth);

            option.inJustDecodeBounds = false;
            option.inSampleSize = findBestSampleSize(actualWidth, actualHeight,
                    desiredWidth, desiredHeight);
            Bitmap tempBitmap = BitmapFactory.decodeByteArray(data, 0,
                    data.length, option);

            // 做缩放
            if (tempBitmap != null
                    && (tempBitmap.getWidth() > desiredWidth || tempBitmap
                    .getHeight() > desiredHeight)) {
                bitmap = Bitmap.createScaledBitmap(tempBitmap, desiredWidth,
                        desiredHeight, true);
                tempBitmap.recycle();
            } else {
                bitmap = tempBitmap;
            }
        }

        if (bitmap == null) {
            displayer.post(path, callback,
                    Response.<Bitmap>error(new VolleyError("bitmap create error")));
        } else {
            displayer.post(path, callback,
                    Response.success(bitmap, new HashMap<String, String>(), null));
        }
        return bitmap;
    }

    /**
     * 框架会自动将大于设定值的bitmap转换成设定值，所以需要这个方法来判断应该显示默认大小或者是设定值大小。<br>
     * 本方法会根据maxPrimary与actualPrimary比较来判断，如果无法判断则会根据辅助值判断，辅助值一般是主要值对应的。
     * 比如宽为主值则高为辅值
     *
     * @param maxPrimary      需要判断的值，用作主要判断
     * @param maxSecondary    需要判断的值，用作辅助判断
     * @param actualPrimary   真实宽度
     * @param actualSecondary 真实高度
     * @return 获取图片需要显示的大小
     */
    private int getResizedDimension(int maxPrimary, int maxSecondary,
                                    int actualPrimary, int actualSecondary) {
        if (maxPrimary == 0 && maxSecondary == 0) {
            return actualPrimary;
        }
        if (maxPrimary == 0) {
            double ratio = (double) maxSecondary / (double) actualSecondary;
            return (int) (actualPrimary * ratio);
        }

        if (maxSecondary == 0) {
            return maxPrimary;
        }

        double ratio = (double) actualSecondary / (double) actualPrimary;
        int resized = maxPrimary;
        if (resized * ratio > maxSecondary) {
            resized = (int) (maxSecondary / ratio);
        }
        return resized;
    }

    /**
     * 关于本方法的判断，可以查看我的博客：http://blog.kymjs.com/code/2014/12/05/02/
     */
    static int findBestSampleSize(int actualWidth, int actualHeight,
                                  int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }
        return (int) n;
    }
}
