package com.ammarad.dictionary;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCache {
    private static ImageCache instance;
    private LruCache<String, Bitmap> memoryCache;
    private ExecutorService executorService;
    
    private ImageCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
        
        executorService = Executors.newFixedThreadPool(4);
    }
    
    public static synchronized ImageCache getInstance() {
        if (instance == null) {
            instance = new ImageCache();
        }
        return instance;
    }
    
    public void addBitmapToCache(String key, Bitmap bitmap) {
        if (getBitmapFromCache(key) == null && bitmap != null) {
            memoryCache.put(key, bitmap);
        }
    }
    
    public Bitmap getBitmapFromCache(String key) {
        return memoryCache.get(key);
    }
    
    public void loadBitmap(final String imageUrl, final ImageCallback callback) {
        final String key = imageUrl;
        
        Bitmap cachedBitmap = getBitmapFromCache(key);
        if (cachedBitmap != null) {
            callback.onImageLoaded(cachedBitmap);
            return;
        }
        
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);
                    connection.connect();
                    
                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();
                    connection.disconnect();
                    
                    if (bitmap != null) {
                        addBitmapToCache(key, bitmap);
                    }
                    
                    final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onImageLoaded(bitmap);
                            }
                        }
                    });
                    
                } catch (Exception e) {
                    final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onImageFailed();
                            }
                        }
                    });
                }
            }
        });
    }
    
    public interface ImageCallback {
        void onImageLoaded(Bitmap bitmap);
        void onImageFailed();
    }
}