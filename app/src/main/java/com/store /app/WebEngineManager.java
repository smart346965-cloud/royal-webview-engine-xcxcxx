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

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request != null && request.isForMainFrame()) {
                    view.loadUrl("file:///android_asset/public/offline.html");
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                view.loadUrl("file:///android_asset/public/offline.html");
            }

            // 🌐 فلتر الشبكة الملكي (The Royal Interceptor)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                WebResourceResponse royalResponse = RoyalNetworkEngine.interceptRequest(request);
                if (royalResponse != null) {
                    return royalResponse;
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request != null && request.getUrl() != null) {
                    Uri uri = request.getUrl();
                    String scheme = uri.getScheme();

                    if (scheme == null) return false;

                    if (scheme.equals("tel") || scheme.equals("mailto") || scheme.equals("whatsapp") || scheme.equals("intent")) {
                        return handleUriLogic(uri, request.isForMainFrame());
                    }

                    if (scheme.equals("http") || scheme.equals("https")) {
                        if (!isSameOrigin(uri)) {
                            return handleUriLogic(uri, request.isForMainFrame());
                        }
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
        if (activity == null || activity.isFinishing()) return; 
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
