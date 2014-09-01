package net.podkowik.curseradapterwithservice.network;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader;

/**
 * Created by christoph.podkowik on 29/08/14.
 */
public class StaticImageCache {
    private static final LruCache<String, Bitmap> mImageCache = new LruCache<String, Bitmap>(20);

    private static ImageLoader.ImageCache ourInstance = new ImageLoader.ImageCache() {
        @Override
        public void putBitmap(String key, Bitmap value) {
            String url = key.substring(key.indexOf("http://"));
            mImageCache.put(url, value);
        }

        @Override
        public Bitmap getBitmap(String key) {
            if (key == null) {
                return null;
            }
            String url = key.substring(key.indexOf("http://"));
            return mImageCache.get(url);
        }
    };

    public static ImageLoader.ImageCache getInstance() {
        return ourInstance;
    }

    private StaticImageCache() {
    }
}
