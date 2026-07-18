package com.store.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

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

    private final Runnable scrollFinishedRunnable =
            RoyalNetworkEngine::notifyScrollFinished;

    private long splashStartTime = 0;

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

    public void setSplashStartTime(long startTime) {
        this.splashStartTime = startTime;
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

        // 👑 إرسال سرعة السكرول لمحرك الشبكة حتى تصبح قرارات الـ Prefetch أكثر ذكاءً
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            webView.setOnScrollChangeListener(
                    (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                        RoyalNetworkEngine.notifyScroll(scrollY);

                        v.removeCallbacks(scrollFinishedRunnable);

                        v.postDelayed(scrollFinishedRunnable, 90);

                    });
        }
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
            RoyalNetworkEngine.notifyRenderIdle();
        });
    }

    public void removeSplashSmoothly() {
        if (activity == null || splashChecker.isRemoved()) return;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - splashStartTime;
        long remainingTime = Math.max(0, 1800 - elapsedTime); // نضمن بقاءه 1.8 ثانية على الأقل

        activity.runOnUiThread(() -> {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (splashOverlay != null) {
                    splashOverlay.animate()
                            .alpha(0f)
                            .setDuration(400) // خروج ناعم جداً
                            .withEndAction(this::removeSplashInstantly)
                            .start();
                }
            }, remainingTime);
        });
    }

    // [تعديل جراحي في WebEngineManager.java]
    private void configureSettings() {
        // 1. بدلاً من TRANSPARENT، نستخدم لون السبلاش لمنع التباين البصري
        webView.setBackgroundColor(Color.parseColor("#F3F4F6")); 
        
        // 2. اجعل الـ Alpha دائماً 1 لضمان بقاء السطح الرسومي نشطاً
        webView.setAlpha(1f); 
        
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setWillNotDraw(false);
        webView.setOverScrollMode(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        // 👑 فتح قواعد البيانات التخزينية العميقة (ضروري للـ Service Worker والـ IndexedDB)
        settings.setDatabaseEnabled(true);
        
        // 👑 السماح بالوصول للملفات لتتمكن نواة كروم من كتابة الـ Bytecode محلياً
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        // 👑 السماح للموارد المحلية بالاتصال ببعضها (مهم جداً لتخطي قيود الـ CORS داخل الكاش)
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        if (WebViewFeature.isFeatureSupported(
                WebViewFeature.ALGORITHMIC_DARKENING)) {

            WebSettingsCompat.setAlgorithmicDarkeningAllowed(
                    settings,
                    true
            );
        }

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
    }

    private void attachClients() {
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                // سجل الطلب فقط، لا تلمس الـ Alpha أبداً لكي يظل آخر إطار معروضاً
                RoyalPanopticon.recordRequestSent();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                RoyalPanopticon.recordNavigationComplete();
                RoyalNetworkEngine.notifyRenderIdle();
                // ❌ تم نقل WebEnhancer.apply من هنا للسرعة
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                // 🚀 حقن المحرك الملكي في أول أجزاء من الثانية (Atomic Injection)
                WebEnhancer.apply(view, context);

                removeSplashSmoothly();
                RoyalNetworkEngine.notifyRenderStart();
                syncStatusBarColor(view);
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                android.util.Log.e("RoyalEngine", "☠️ FATAL: Chromium Renderer crashed! Auto-Recovery...");
                RoyalNetworkEngine.notifyRenderIdle();
                RoyalWebViewHost.destroy();
                if (activity != null) {
                    RoyalWebViewHost.create(activity.getApplicationContext());
                    activity.recreate();
                }
                return true;
            }

            private void triggerOfflineProtection(WebView view, String failingUrl) {
                if (failingUrl != null && !failingUrl.startsWith("file:///android_asset/")) {
                    RoyalNetworkEngine.notifyRenderIdle();
                    view.stopLoading();
                    view.clearHistory();
                    view.post(() -> {
                        String offline = "file:///android_asset/public/offline.html?origin=" + Uri.encode(failingUrl);
                        view.loadUrl(offline);
                    });
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    RoyalNetworkEngine.notifyRenderIdle();
                    triggerOfflineProtection(view, request.getUrl().toString());
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                RoyalNetworkEngine.notifyRenderIdle();
                triggerOfflineProtection(view, failingUrl);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (request == null || request.getUrl() == null) return null;
                String url = request.getUrl().toString();

                // 👑 [محاكي النطاق الافتراضي] اعتراض ملف الـ JS الوهمي وإعطائه تصريح العبور الآمن (CORS)
                if (url.endsWith("/royal_nucleus.js")) {
                    try {
                        java.io.InputStream jsStream = context.getAssets().open("public/js/royal_nucleus.js");
                        java.util.Map<String, String> headers = new java.util.HashMap<>();
                        headers.put("Content-Type", "application/javascript");
                        headers.put("Access-Control-Allow-Origin", "*"); // كسر قيود CORS للسماح بالتشغيل داخل صفحة المتجر
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            return new WebResourceResponse("application/javascript", "UTF-8", 200, "OK", headers, jsStream);
                        } else {
                            return new WebResourceResponse("application/javascript", "UTF-8", jsStream);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("RoyalEngine", "❌ FATAL: Failed to serve local JS Core!", e);
                    }
                }

                // 👑 [تصحيح أمني حاسم] حسم ملف الـ WASM المحلي وإرجاعه بـ MIME Type المعتمد عالمياً لقهر الحظر الصامت
                if (url.endsWith("/royal_nucleus.wasm")) {
                    try {
                        java.io.InputStream wasmStream = context.getAssets().open("public/js/royal_nucleus.wasm");
                        java.util.Map<String, String> headers = new java.util.HashMap<>();
                        headers.put("Content-Type", "application/wasm"); // فرض هوية الملف الثنائي
                        headers.put("Access-Control-Allow-Origin", "*"); // كسر قيود الـ CORS محلياً
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            return new WebResourceResponse("application/wasm", null, 200, "OK", headers, wasmStream);
                        } else {
                            return new WebResourceResponse("application/wasm", null, wasmStream);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("RoyalEngine", "❌ FATAL: Failed to serve local WASM Core!", e);
                    }
                }

                if (url.endsWith("/nexus-service-worker.js")) {
                    try {
                        java.io.InputStream swStream = context.getAssets().open("public/js/nexus-service-worker.js");
                        java.util.Map<String, String> headers = new java.util.HashMap<>();
                        headers.put("Content-Type", "application/javascript");
                        headers.put("Service-Worker-Allowed", "/");
                        headers.put("Cache-Control", "no-cache"); 

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            return new WebResourceResponse("application/javascript", "UTF-8", 200, "OK", headers, swStream);
                        } else {
                            return new WebResourceResponse("application/javascript", "UTF-8", swStream);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("RoyalEngine", "Failed to serve local Service Worker", e);
                    }
                }

                if (!NetworkMonitor.isInternetAvailable(context) && request.isForMainFrame()) {
                    return new WebResourceResponse("text/html", "UTF-8", null);
                }

                // 👑 [تعديل الأولوية القصوى] إدراج الـ .wasm كعنصر نواة فوري لرفع أولوية المعالجة في العتاد
                boolean isCoreResource = request.isForMainFrame() || url.contains(".js") || url.contains(".css") || url.contains(".wasm");
                if (isCoreResource) {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
                }

                // 👁️ تتبع كفاءة الـ Network Interceptor الفرعي وتسجيل أدائه
                long startIntercept = System.currentTimeMillis();
                WebResourceResponse royalResponse = RoyalNetworkEngine.interceptRequest(request);
                long duration = System.currentTimeMillis() - startIntercept;
                
                // نرسل السجلات فوراً لمحرك الفحص لمعرفة سرعة استرجاع الكاش الملكي
                RoyalPanopticon.recordExecution("NetworkInterceptor", duration, true, 0);

                if (royalResponse != null) {
                    return royalResponse;
                }
                
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request == null || request.getUrl() == null) return false;
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                String url = uri.toString();

                // 1. السماح بالتطبيقات الخارجية المحددة فقط (واتساب، اتصال، الخ)
                if (scheme != null && (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("whatsapp") || scheme.equals("intent") || scheme.equals("sms"))) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        android.util.Log.e("RoyalEngine", "External App not found for: " + scheme);
                    }
                    return true; // تم التعامل معه بنجاح، لا تفتح كروم
                }

                // 2. قفل الحصن: إذا كان الرابط http أو https
                if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                    // التحقق من الأصل (Origin)
                    if (isSameOrigin(uri)) {
                        // نفس الموقع -> افتحه داخل الويب فيو فوراً
                        return false; 
                    } else {
                        // موقع خارجي -> افتحه داخل الويب فيو أيضاً (لمنع الهروب لكروم) 
                        // إلا إذا كنت تريد منع المستخدم من الخروج من متجرك نهائياً
                        view.loadUrl(url);
                        return true;
                    }
                }
                
                // منع أي محاولة هروب لأي بروتوكول آخر غير معروف
                return true; 
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
                    RoyalPanopticon.recordUserClick(url);
                    return handleUriLogic(Uri.parse(url), true);
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress == 100) {
                    progressBar.animate()
                            .alpha(0f)
                            .setDuration(150)
                            .withEndAction(() -> progressBar.setVisibility(View.GONE))
                            .start();
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

        if (!view.isAttachedToWindow()) {
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
