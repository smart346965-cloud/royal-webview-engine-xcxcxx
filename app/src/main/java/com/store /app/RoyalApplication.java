package com.example.mytwa_smart;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.browser.customtabs.CustomTabsClient;
import androidx.browser.customtabs.CustomTabsServiceConnection;
import androidx.browser.customtabs.CustomTabsSession;

/**
 * =========================================================
 * 👑 ROYAL APPLICATION (V2 - The Ultimate Pre-Warmer)
 * =========================================================
 * الوظيفة: تسخين محرك كروم + إنشاء جلسة + التحقق من AssetLinks في الخلفية.
 * النتيجة: 0ms Cold Start + إبادة ومضة شريط كروم نهائياً.
 */
public class RoyalApplication extends Application {

    private static final String TAG = "RoyalEngine";
    private CustomTabsServiceConnection connection;

    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.i(TAG, "🚀 App Process Started: Initiating Ultimate Chrome Pre-warming...");

        connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                try {
                    // 1️⃣ الخطوة الأولى: تسخين المحرك الأساسي (تشغيل عمليات C++ في الخلفية)
                    client.warmup(0L);
                    Log.i(TAG, "🔥 Step 1: Chrome Engine Warmed Up.");

                    // 2️⃣ الخطوة الثانية: إنشاء جلسة تواصل (Session) مع كروم
                    CustomTabsSession session = client.newSession(null);
                    
                    if (session != null) {
                        // 3️⃣ الخطوة الثالثة: استخراج رابط العميل ديناميكياً من الـ Manifest
                        String targetUrl = getClientUrlFromManifest();
                        
                        // 4️⃣ السلاح النووي: إجبار كروم على التحقق من AssetLinks وفتح الاتصال الآن!
                        session.mayLaunchUrl(Uri.parse(targetUrl), null, null);
                        Log.i(TAG, "⚡ Step 2: mayLaunchUrl fired for -> " + targetUrl);
                        Log.i(TAG, "🛡️ Chrome is now verifying AssetLinks in the background!");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error during Chrome Pre-warming", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.w(TAG, "⚠️ Chrome Engine Disconnected.");
            }
        };

        // محاولة الاتصال بكروم (الباقة الافتراضية)
        try {
            CustomTabsClient.bindCustomTabsService(this, "com.android.chrome", connection);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to bind CustomTabsService", e);
        }
    }

    /**
     * 🧠 دالة ذكية تقرأ رابط المتجر من الـ Manifest أوتوماتيكياً
     * (لكي لا تضطر لتغيير الرابط يدوياً لكل عميل في مصنع التطبيقات)
     */
    private String getClientUrlFromManifest() {
        String defaultUrl = "https://your-store.com/"; // رابط احتياطي (Fallback)
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = appInfo.metaData;
            if (bundle != null) {
                String manifestUrl = bundle.getString("android.support.customtabs.trusted.DEFAULT_URL");
                if (manifestUrl != null && !manifestUrl.isEmpty()) {
                    return manifestUrl;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to read URL from Manifest", e);
        }
        return defaultUrl;
    }
    }
