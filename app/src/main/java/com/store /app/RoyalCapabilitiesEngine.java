package com.store.app;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class RoyalCapabilitiesEngine {

    private final Activity activity;
    
    // متغير حيوي لحفظ مسار الملف عندما يطلب الموقع رفع صورة/ملف
    public static ValueCallback<Uri[]> filePathCallback;
    public final static int FILECHOOSER_RESULTCODE = 101; // كود سري لتعريف العملية

    public RoyalCapabilitiesEngine(Activity activity) {
        this.activity = activity;
    }

    // 1️⃣ تفعيل مدير التحميلات (File Downloading)
    public void attachDownloadManager(WebView webView) {
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimetype);
                    
                    // جلب الكوكيز لتتمكن من تحميل الملفات من المواقع التي تتطلب تسجيل دخول
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);

                    request.setDescription("Downloading file...");
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                    
                    DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    
                    Toast.makeText(activity.getApplicationContext(), "جاري التحميل...", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("RoyalCapabilities", "❌ خطأ في تحميل الملف: " + e.getMessage());
                }
            }
        });
    }

    // 2️⃣ بناء العميل الخارق (WebChromeClient) الذي يدمج شريط التحميل مع قدرات العتاد
    public WebChromeClient buildChromeClient(ProgressBar progressBar) {
        return new WebChromeClient() {

            // [أ] التعامل مع شريط التحميل (تم نقله من الملف القديم)
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                    if (newProgress == 100) {
                        progressBar.animate()
                                .alpha(0f)
                                .setDuration(150)
                                .withEndAction(() -> progressBar.setVisibility(View.GONE))
                                .start();
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setAlpha(1f);
                    }
                }
            }

            // [ب] نظام الملفات والاستوديو: استجابة لزر رفع الصور (File Upload)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (RoyalCapabilitiesEngine.filePathCallback != null) {
                    RoyalCapabilitiesEngine.filePathCallback.onReceiveValue(null);
                }
                RoyalCapabilitiesEngine.filePathCallback = filePathCallback;
                
                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType("*/*"); // يسمح باختيار أي نوع ملف
                
                // السماح برفع ملفات متعددة إذا كان الموقع يطلب ذلك
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, fileChooserParams.getMode() == FileChooserParams.MODE_OPEN_MULTIPLE);
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "اختر ملفاً أو صورة");
                
                try {
                    activity.startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                    return true;
                } catch (Exception e) {
                    RoyalCapabilitiesEngine.filePathCallback = null;
                    return false;
                }
            }

            // [ج] تحديد الموقع الجغرافي (Geolocation) لخرائط التوصيل
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                // التحقق مما إذا كان المستخدم قد منح صلاحية الموقع للأندرويد
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // إذا لم يمنحها، نطلبها من نظام الأندرويد
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
                    // يجب عليك إخطار الموقع لاحقاً بعد موافقة المستخدم
                    callback.invoke(origin, false, false);
                } else {
                    // إذا كانت ممنوحة، نعطي الموقع التصريح فوراً
                    callback.invoke(origin, true, false);
                }
            }

            // [د] صلاحيات الويب الحديثة (WebRTC, Camera, Microphone, Bluetooth)
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // هنا نقوم بمنح الصلاحيات للموقع تلقائياً بناءً على طلبات الـ JavaScript
                    // (يُفترض أنك ستضيف لاحقاً منطقاً يطلب الصلاحية من المستخدم إذا لم تكن ممنوحة في الأندرويد)
                    request.grant(request.getResources());
                }
            }
        };
    }
}
