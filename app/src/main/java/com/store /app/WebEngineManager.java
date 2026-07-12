package com.store.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;

public class WebEngineManager {

    private final Context context;
    private final android.app.Activity activity;
    private final WebView webView;
    private final View splashOverlay;
    private final android.widget.ProgressBar progressBar;

    private final Runnable markSplashRemoved;
    private final SplashStateChecker splashChecker;

    private String trustedScheme = null;
    private String trustedHost = null;
    private int trustedPort = -1;

    public interface SplashStateChecker {
        boolean isRemoved();
    }

    public WebEngineManager(Context context,
                            WebView webView,
                            View splashOverlay,
                            android.widget.ProgressBar progressBar,
                            Runnable markSplashRemoved,
                            SplashStateChecker splashChecker) {

        this.context = context;
        this.webView = webView;
        this.splashOverlay = splashOverlay;
        this.progressBar = progressBar;
        this.markSplashRemoved = markSplashRemoved;
        this.splashChecker = splashChecker;

        this.activity = (context instanceof android.app.Activity)
                ? (android.app.Activity) context
                : null;
    }

    public void init() {
        // 👑 1. حارس العودة الساخنة (Warm Resume Guard)
        if (RoyalWebViewHost.isReady() && webView.getUrl() != null && !webView.getUrl().equals("about:blank")) {
            android.util.Log.i("RoyalEngine", "🔥 Warm Resume Detected! Skipping Splash.");
            webView.setAlpha(1f);
            removeSplashInstantly();
            attachClients();
            return;
        }

        configureSettings();
        attachClients();
    }

    private void removeSplashInstantly() {
        if (activity == null) return;
        activity.runOnUiThread(() -> {
            if (splashOverlay != null && splashOverlay.getParent() instanceof ViewGroup) {
                ((ViewGroup) splashOverlay.getParent()).removeView(splashOverlay);
            }
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            markSplashRemoved.run();
        });
    }

    public void removeSplashSmoothly() {
        if (activity == null || splashChecker.isRemoved()) return;
        activity.runOnUiThread(() -> {
            if (splashOverlay != null) {
                splashOverlay.animate()
                        .alpha(0f)
                        .setDuration(300) 
                        .withEndAction(this::removeSplashInstantly)
                        .start();
            }
        });
    }

    private void configureSettings() {
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setAlpha(0f);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWillNotDraw(false);
        webView.setOverScrollMode(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
    }

    private void attachClients() {
        // 🚀 وداعاً Capacitor! الـ WebViewClient الآن خالص لمحركنا
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                WebEnhancer.apply(view, context);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                if (!NetworkMonitor.isInternetAvailable(context)) {
                    return;
                }

                if (view.getAlpha() == 0f) {
                    view.animate().alpha(1f).setDuration(180).start();
                }

                if (trustedHost == null && url != null) {
                    setTrustedOrigin(url);
                }

                WebEnhancer.apply(view, context);
                syncStatusBarColor(view);

                view.postDelayed(() -> {
                    if (!splashChecker.isRemoved()) {
                        removeSplashSmoothly();
                    }
                }, 2500);
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                android.util.Log.e("RoyalEngine", "☠️ FATAL: Chromium Renderer crashed! Auto-Recovery...");
                RoyalWebViewHost.destroy();
                if (activity != null) {
                    RoyalWebViewHost.create(activity.getApplicationContext());
                    activity.recreate();
                }
                return true;
            }

            // 🛡️ درع الحماية الملكي: يمنع ديناصور كروم نهائياً ويحترم الكاش
            private void triggerOfflineProtection(WebView view, String failingUrl) {
                if (failingUrl != null && !failingUrl.startsWith("file:///android_asset/")) {
                    
                    // 1. الإيقاف القسري: نوقف محرك كروم فوراً في مساره لمنعه من رسم صفحة الخطأ
                    view.stopLoading();
                    
                    // 2. التنظيف العميق (Flush): نقوم بمسح التاريخ
                    view.clearHistory();
                    
                    // 3. التوجيه السلس: نضع أمر التحميل في طابور الـ UI لضمان تنظيف الشاشة أولاً ثم عرض الأوفلاين
                    view.post(() -> {

                        String offline =
                                "file:///android_asset/public/offline.html?origin="
                                + Uri.encode(failingUrl);

                        view.loadUrl(offline);

                    });
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // التأكد من أن الفشل في الصفحة الرئيسية (وليس في صورة أو ملف فرعي)
                if (request != null && request.isForMainFrame()) {
                    triggerOfflineProtection(view, request.getUrl().toString());
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                triggerOfflineProtection(view, failingUrl);
            }

            // 🌐 فلتر الشبكة الملكي (The Royal Interceptor)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                // 🚨 جراحة الخبير: تم إزالة فحص NetworkMonitor من هنا تماماً!
                // لا نوقف محرك كروم عن طلب الصور والملفات. إذا انقطع النت، onReceivedError سيتدخل.
                
                WebResourceResponse royalResponse = RoyalNetworkEngine.interceptRequest(request);
                if (royalResponse != null) {
                    return royalResponse;
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // ⏱️ نظام القياس (Telemetry): لمعرفة زمن معالجة النقرة
                long startTime = System.nanoTime(); 

                if (request == null || request.getUrl() == null) return false;
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if (scheme == null) return false;

                boolean isMainFrame = request.isForMainFrame();

                // 1. مسار الإطارات الفرعية (IFrames) -> عبور فوري
                if (!isMainFrame) return false;

                // 2. 🚀 المسار السريع جداً (VIP Fast-Path) للروابط الداخلية
                if (scheme.equals("http") || scheme.equals("https")) {
                    // 🛡️ صمام الأمان: إذا كان هذا أول تشغيل (trustedHost غير معروف بعد)
                    // نقوم بتسجيل هذا الرابط كـ "النطاق الأم" فوراً قبل أي شيء آخر!
                    if (trustedHost == null) {
                        setTrustedOrigin(uri.toString());
                    }

                    // الآن نفحص: هل هو نفس النطاق الموثوق؟ إذا نعم، ابقَ داخل التطبيق!
                    if (isSameOrigin(uri)) {
                        logPerformance("Internal Click", startTime);
                        return false; // العودة بـ false تعني: "أيها الـ WebView، افتح الرابط بداخلك ولا تخرج"
                    }
                }

                // 3. الروابط الخارجية وتطبيقات النظام (واتساب، اتصال، الخ)
                if (!scheme.equals("http") && !scheme.equals("https")) {
                    boolean result = handleUriLogic(uri, true);
                    logPerformance("External App", startTime);
                    return result;
                }

                // 4. الانتقال لمتصفح خارجي (روابط خارج الدومين)
                // هنا فقط نفحص الإنترنت لأننا سنغادر التطبيق
                if (!NetworkMonitor.isInternetAvailable(context)) {
                    triggerOfflineProtection(view, uri.toString());
                    logPerformance("Offline Block", startTime);
                    return true;
                }

                boolean result = handleUriLogic(uri, true);
                logPerformance("External Web", startTime);
                return result;
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
                    return handleUriLogic(Uri.parse(url), true);
                }
                return false;
            }

            // ⏱️ دالة مساعدة لطباعة القياس الزمني في الـ Logcat
            private void logPerformance(String action, long startTimeNanos) {
                long durationMs = (System.nanoTime() - startTimeNanos) / 1000000;
                android.util.Log.d("RoyalEngine_Perf", "⚡ Navigation [" + action + "] took: " + durationMs + " ms");
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.animate().alpha(0f).setDuration(200).start();
                } else {
                    progressBar.setAlpha(1f);
                }
            }
        });
    }

    private void syncStatusBarColor(WebView view) {
        if (activity == null || activity.isFinishing()) return; 

        if (!NetworkMonitor.isInternetAvailable(context))
            return;

        // 👑 قفل الحماية الملكي: إذا كنا في صفحة الأوفلاين المحلية، نفرض الألوان الشفافة والأيقونات الداكنة فوراً بدون تقييم مؤقت
        String currentUrl = view.getUrl();
        if (currentUrl != null && currentUrl.startsWith("file:///android_asset/")) {
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
            SystemUI.setDynamicIcons(activity.getWindow(), true); // true تعني أيقونات داكنة واضحة جداً فوق الخلفية البيضاء للأوفلاين
            return;
        }

        view.evaluateJavascript(
                "(function(){return window.getComputedStyle(document.body).backgroundColor;})();",
                value -> {
                    try {
                        if (value != null && value.contains("rgb")) {
                            String clean = value.replaceAll("[^0-9,]", "");
                            String[] parts = clean.split(",");
                            int r = Integer.parseInt(parts[0].trim());
                            int g = Integer.parseInt(parts[1].trim());
                            int b = Integer.parseInt(parts[2].trim());
                            int color = Color.rgb(r, g, b);

                            activity.getWindow().setStatusBarColor(color);
                            boolean isLight = SystemUI.isColorLight(color);
                            SystemUI.setDynamicIcons(activity.getWindow(), isLight);
                        }
                    } catch (Exception ignored) {}
                }
        );
    }

    private void setTrustedOrigin(String url) {
        Uri uri = Uri.parse(url);
        trustedScheme = uri.getScheme();
        trustedHost = uri.getHost();
        trustedPort = uri.getPort() == -1 ? (trustedScheme.equals("https") ? 443 : 80) : uri.getPort();
    }

    private boolean isSameOrigin(Uri uri) {
        if (trustedHost == null) return false;
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null) return false;

        int port = uri.getPort() == -1 ? (scheme.equals("https") ? 443 : 80) : uri.getPort();
        return scheme.equals(trustedScheme) && host.equalsIgnoreCase(trustedHost) && port == trustedPort;
    }

    private boolean handleUriLogic(Uri uri, boolean isMainFrame) {
        if (!isMainFrame) return false;
        String scheme = uri.getScheme();
        if (scheme == null) return true;

        if (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("whatsapp") || scheme.equals("intent")) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } catch (Exception ignored) {}
            return true;
        }

        if (scheme.equals("https") || scheme.equals("http")) {
            if (isSameOrigin(uri)) {
                return false;
            } else {
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ignored) {}
                return true;
            }
        }
        return true;
    }
            }
