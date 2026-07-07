Enterpackage com.store.app;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;

import androidx.browser.customtabs.CustomTabsIntent;

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
            android.util.Log.i("RoyalEngine", "🔥 Warm Resume Detected! Skipping Splash, but Re-binding Clients.");

            // 🛡️ درع الوميض الثالث: إجبار الويب فيو على الظهور فوراً بكامل طاقته
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
                        .setDuration(300) // تلاشي سينمائي ناعم جداً
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

        // 👑 إعادة إحساس الـ Native (الارتداد عند نهاية الصفحة)
        webView.setOverScrollMode(WebView.OVER_SCROLL_IF_CONTENT_SCROLLS);

        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);

        // الإعدادات الأساسية (باقي الإعدادات القوية تمت في RoyalHybridEngine)
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        // 👑 تم حذف setDatabaseEnabled لأنه Deprecated ويستهلك I/O بلا فائدة
        settings.setAllowFileAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSupportMultipleWindows(false);
        settings.setSupportZoom(false);
    }

    private void attachClients() {
        // جلب عميل Capacitor الأصلي لغلافه (Wrapper Pattern)
        WebViewClient capacitorClient =
                ((com.getcapacitor.BridgeActivity) context)
                        .getBridge().getWebViewClient();

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                // نترك Capacitor يقوم بعمله
                if (capacitorClient != null) {
                    capacitorClient.onPageFinished(view, url);
                }

                // 🚀 حقن احتياطي آمن (Safe Fallback Injection)
                // يضمن عمل العضلات فوراً حتى لو تأخر رسم الإطار الأول
                WebEnhancer.apply(view, context);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                // 1. إظهار الويب فيو بنعومة
                if (view.getAlpha() == 0f) {
                    view.animate().alpha(1f).setDuration(180).start();
                }

                // 2. السماح لـ Capacitor بالعمل
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && capacitorClient != null) {
                    capacitorClient.onPageCommitVisible(view, url);
                }

                if (trustedHost == null && url != null) {
                    setTrustedOrigin(url);
                }

                // 3. حقن العضلات
                WebEnhancer.apply(view, context);
                syncStatusBarColor(view);

                // 👑 4. بروتوكول الاكتمال البصري (Visual Completeness Protocol):
                // نحن لن نخفي السبلاش هنا! سننتظر إشارة `hideSplash` من الجافاسكريبت.
                // لكن، كإجراء أمان (Fail-safe)، إذا تعطل الجافاسكريبت، سنخفيه بعد 2.5 ثانية كحد أقصى.
                view.postDelayed(() -> {
                    if (!splashChecker.isRemoved()) {
                        android.util.Log.w("RoyalEngine", "⏳ Fail-safe: Hiding splash due to JS timeout.");
                        removeSplashSmoothly();
                    }
                }, 2500);
            }

            // 🚑 2. الإنعاش التلقائي عند انهيار محرك كروم (Renderer Crash Recovery)
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                android.util.Log.e("RoyalEngine", "☠️ FATAL: Chromium Renderer crashed! Initiating Auto-Recovery...");

                // إخبار Capacitor بالكارثة
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && capacitorClient != null) {
                    capacitorClient.onRenderProcessGone(view, detail);
                }

                // تدمير الويب فيو الميت وإعادة بناء واحد جديد
                RoyalWebViewHost.destroy();
                if (activity != null) {
                    RoyalWebViewHost.create(activity.getApplicationContext());
                    activity.recreate(); // إعادة تشغيل الشاشة بأمان
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && capacitorClient != null) {
                    capacitorClient.onReceivedError(view, request, error);
                }
                if (request != null && request.isForMainFrame()) {
                    view.loadUrl("file:///android_asset/public/offline.html");
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (capacitorClient != null) {
                    capacitorClient.onReceivedError(view, errorCode, description, failingUrl);
                }
                view.loadUrl("file:///android_asset/public/offline.html");
            }

            // 🌐 Royal Network Engine Interceptor
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {

                // 1️⃣ إعطاء محرك الشبكة الملكي فرصة أولاً
                WebResourceResponse royalResponse = RoyalNetworkEngine.interceptRequest(request);
                if (royalResponse != null) {
                    return royalResponse;
                }

                // 2️⃣ إعطاء Capacitor فرصة للتعامل مع طلباته الداخلية
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && capacitorClient != null) {
                    WebResourceResponse capacitorResponse =
                            capacitorClient.shouldInterceptRequest(view, request);

                    if (capacitorResponse != null) {
                        return capacitorResponse;
                    }
                }

                // 3️⃣ ترك Chromium يتعامل مع الطلب
                return super.shouldInterceptRequest(view, request);
            }

            // 🤝 3. معالجة الروابط الاحترافية (Zero-Friction Routing)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    Uri uri = request.getUrl();
                    String scheme = uri.getScheme();

                    if (scheme == null) return false;

                    // 1. إذا كان الرابط تطبيقاً خارجياً (واتساب، اتصال)، نرسله لمنطقنا فوراً
                    if (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("whatsapp") || scheme.equals("intent")) {
                        return handleUriLogic(uri, request.isForMainFrame());
                    }

                    // 2. إذا كان الرابط ويب (http/https)
                    if (scheme.equals("http") || scheme.equals("https")) {
                        // إذا كان رابطاً خارجياً (ليس Gymshark)، نرسله لمنطقنا (Custom Tabs)
                        if (!isSameOrigin(uri)) {
                            return handleUriLogic(uri, request.isForMainFrame());
                        }
                        // 👑 السحر هنا: إذا كان رابطاً داخلياً (Gymshark)، نعود بـ false فوراً!
                        // هذا يمنع الجافا من التدخل، ويترك المتصفح يطير بالرابط في 0 ملي ثانية.
                        return false;
                    }
                }
                return false;
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null) {
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
                    progressBar.animate().alpha(0f).setDuration(200).start();
                } else {
                    progressBar.setAlpha(1f);
                }
            }
        });
    }

    private void syncStatusBarColor(WebView view) {
        if (activity == null || activity.isFinishing()) return; // حماية من الانهيار

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
                context.startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, uri));
            } catch (Exception ignored) {}
            return true;
        }

        if (scheme.equals("https") || scheme.equals("http")) {
            if (isSameOrigin(uri)) {
                return false;
            } else {
                new CustomTabsIntent.Builder().build().launchUrl(context, uri);
                return true;
            }
        }
        return true;
    }
}
