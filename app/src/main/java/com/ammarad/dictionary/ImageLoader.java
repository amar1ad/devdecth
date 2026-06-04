package com.ammarad.dictionary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class ImageLoader {

    public interface ImageLoadListener {
        void onSuccess(Bitmap bitmap);
        void onFailure();
    }

    private static ImageLoader instance;
    private Handler mainHandler;
    private File cacheDir;

    private ImageLoader() {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ImageLoader getInstance() {
        if (instance == null) {
            instance = new ImageLoader();
        }
        return instance;
    }

    public void initCacheDir(Context context) {
        if (cacheDir == null) {
            cacheDir = new File(context.getCacheDir(), "image_cache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
        }
    }

    // الطريقة القديمة (لأيقونات التصنيفات، لغات، إلخ)
    public void loadImage(final String iconStr, final ImageView imageView, final TextView textView) {
        showEmoji(iconStr, imageView, textView);
        if (iconStr == null || iconStr.isEmpty()) return;
        if (!iconStr.startsWith("http") && !iconStr.startsWith("data:image")) return;

        final String cachePath = getCachePath(iconStr);
        if (cachePath != null) {
            File cacheFile = new File(cachePath);
            if (cacheFile.exists()) {
                Bitmap cachedBitmap = BitmapFactory.decodeFile(cachePath);
                if (cachedBitmap != null) {
                    showImage(imageView, textView, cachedBitmap);
                    return;
                }
            }
        }

        if (iconStr.startsWith("http")) {
            loadUrlImageOld(iconStr, imageView, textView, cachePath);
        } else if (iconStr.startsWith("data:image")) {
            loadBase64ImageOld(iconStr, imageView, textView, cachePath);
        }
    }

    // الطريقة الجديدة (للحصول على Bitmap مع Listener)
    public void loadImage(final String iconStr, final ImageLoadListener listener, final TextView dummyText) {
        if (iconStr == null || iconStr.isEmpty()) {
            if (listener != null) listener.onFailure();
            return;
        }
        if (!iconStr.startsWith("http") && !iconStr.startsWith("data:image")) {
            if (listener != null) listener.onFailure();
            return;
        }

        final String cachePath = getCachePath(iconStr);
        if (cachePath != null) {
            File cacheFile = new File(cachePath);
            if (cacheFile.exists()) {
                Bitmap cachedBitmap = BitmapFactory.decodeFile(cachePath);
                if (cachedBitmap != null) {
                    if (listener != null) listener.onSuccess(cachedBitmap);
                    return;
                }
            }
        }

        if (iconStr.startsWith("http")) {
            downloadUrlImage(iconStr, listener, cachePath);
        } else if (iconStr.startsWith("data:image")) {
            decodeBase64Image(iconStr, listener, cachePath);
        } else {
            if (listener != null) listener.onFailure();
        }
    }

    // طرق خاصة بالنسخة القديمة
    private void loadUrlImageOld(final String urlString, final ImageView imageView,
                                 final TextView textView, final String cachePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    if (bitmap != null) {
                        if (cachePath != null) saveBitmapToFile(bitmap, cachePath);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showImage(imageView, textView, bitmap);
                            }
                        });
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showEmoji("📚", imageView, textView);
                            }
                        });
                    }
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showEmoji("📚", imageView, textView);
                        }
                    });
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        }).start();
    }

    private void loadBase64ImageOld(final String base64Str, final ImageView imageView,
                                    final TextView textView, final String cachePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String base64Image = base64Str.split(",")[1];
                    byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bitmap != null) {
                        if (cachePath != null) saveBitmapToFile(bitmap, cachePath);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showImage(imageView, textView, bitmap);
                            }
                        });
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                showEmoji("📚", imageView, textView);
                            }
                        });
                    }
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showEmoji("📚", imageView, textView);
                        }
                    });
                }
            }
        }).start();
    }

    // طرق خاصة بالنسخة الجديدة (مع Listener)
    private void downloadUrlImage(final String urlString, final ImageLoadListener listener, final String cachePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(urlString);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    input.close();

                    if (bitmap != null) {
                        if (cachePath != null) saveBitmapToFile(bitmap, cachePath);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onSuccess(bitmap);
                            }
                        });
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onFailure();
                            }
                        });
                    }
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onFailure();
                        }
                    });
                } finally {
                    if (connection != null) connection.disconnect();
                }
            }
        }).start();
    }

    private void decodeBase64Image(final String base64Str, final ImageLoadListener listener, final String cachePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String base64Image = base64Str.split(",")[1];
                    byte[] decoded = Base64.decode(base64Image, Base64.DEFAULT);
                    final Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    if (bitmap != null) {
                        if (cachePath != null) saveBitmapToFile(bitmap, cachePath);
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onSuccess(bitmap);
                            }
                        });
                    } else {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (listener != null) listener.onFailure();
                            }
                        });
                    }
                } catch (Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null) listener.onFailure();
                        }
                    });
                }
            }
        }).start();
    }

    private void showImage(ImageView imageView, TextView textView, Bitmap bitmap) {
        imageView.setVisibility(android.view.View.VISIBLE);
        textView.setVisibility(android.view.View.GONE);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void showEmoji(String emoji, ImageView imageView, TextView textView) {
        imageView.setVisibility(android.view.View.GONE);
        textView.setVisibility(android.view.View.VISIBLE);
        if (emoji != null && !emoji.isEmpty()) {
            if (emoji.startsWith("http") || emoji.startsWith("data:image")) {
                textView.setText("📚");
            } else {
                textView.setText(emoji);
            }
        } else {
            textView.setText("📚");
        }
    }

    private String getCachePath(String input) {
        if (cacheDir == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            String fileName = hex.toString() + ".png";
            return new File(cacheDir, fileName).getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void saveBitmapToFile(Bitmap bitmap, String filePath) {
        try {
            FileOutputStream out = new FileOutputStream(filePath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}