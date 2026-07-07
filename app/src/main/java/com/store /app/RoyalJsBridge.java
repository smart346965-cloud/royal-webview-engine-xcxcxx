Enterpackage com.store.app;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

public class RoyalJsBridge {

    private static final String TAG = "RoyalJsBridge";

    private final WebView webView;

    // متغير لتخزين دالة إخفاء السبلاش
    private Runnable onHideSplashCallback;

    public RoyalJsBridge(WebView webView) {
        this.webView = webView;
    }

    public void setOnHideSplashCallback(Runnable callback) {
        this.onHideSplashCallback = callback;
    }

    /**
     * 🚀 Network Warmup
     * يتم استدعاؤه من TapEngine قبل النقر
     */
    @JavascriptInterface
    public void warmup(String url) {
        try {
            RoyalNetworkEngine.warmupLink(url);
        } catch (Exception e) {
            Log.e(TAG, "Warmup failed", e);
        }
    }

    /**
     * 🌊 Scroll velocity hint
     */
    @JavascriptInterface
    public void scrollHint(int velocity) {
        try {
            // مستقبلًا يمكن ربطه بمحرك prefetch
            Log.d(TAG, "Scroll velocity: " + velocity);
        } catch (Exception e) {
            Log.e(TAG, "scrollHint error", e);
        }
    }

    /**
     * 🧠 JS diagnostic channel
     */
    @JavascriptInterface
    public void log(String message) {
        Log.d(TAG, "JS: " + message);
    }

    /**
     * 🎭 Visual Completeness Signal
     * يُستدعى من الجافاسكريبت عندما يكتمل رسم الموقع بالكامل
     */
    @JavascriptInterface
    public void hideSplash() {
        if (onHideSplashCallback != null) {
            // يجب تنفيذ الإخفاء على الـ UI Thread
            if (webView != null) {
                webView.post(onHideSplashCallback);
            }
        }
    }

    /**
     * 👁️‍🗨️ Panopticon Telemetry Receiver
     * يستقبل الحالة الصحية للمتصفح من الجافاسكريبت ويرسلها للعقل المدبر
     */
    @JavascriptInterface
    public void reportBrowserState(int domNodes, int fps, long jsMemoryMB, int longTasks) {
        try {
            RoyalPanopticon.syncBrowserState(domNodes, fps, jsMemoryMB, longTasks);
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync browser state", e);
        }
    }

    @JavascriptInterface
    public void inspect() {
        try {
            String report = RoyalPanopticon.buildReport();

            // نهرب النص عشان ما يكسر الجافاسكريبت
            report = report
                    .replace("\\", "\\\\")
                    .replace("`", "\\`")
                    .replace("$", "\\$");

            final String js = "console.log(`" + report + "`);";

            webView.post(() -> webView.evaluateJavascript(js, null));

        } catch (Exception e) {
            Log.e("RoyalJsBridge", "Inspect failed", e);
        }
    }

    /**
     * 🔁 Native → JS callback
     */
    public void dispatchToJS(String script) {
        if (webView == null) return;

        webView.post(() ->
                webView.evaluateJavascript(script, null)
        );
    }
}
