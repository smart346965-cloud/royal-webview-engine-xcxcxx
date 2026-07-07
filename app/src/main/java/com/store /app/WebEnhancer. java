Enterpackage com.store.app;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * =========================================================
 * 🧠 Royal Web Enhancer (Enterprise Atomic Injector)
 * =========================================================
 * - Single atomic injection
 * - Deterministic execution order
 * - WebView / SPA safe
 * - Radar-compatible (HUD profiler)
 */
public final class WebEnhancer {

    private static final String TAG = "RoyalWebEnhancer";

    /**
     * 🔑 Entry point
     */
    public static synchronized void apply(WebView webView, Context context) {

        if (webView == null || context == null) return;

        final String[] scripts = {

                // Core Engine
                "public/js/royalInteraction.js",

                // Predictive Scroll
                "public/js/viewportPredictor.js"
        };

        injectBatch(webView, context, scripts);
    }

    /**
     * 🧬 Atomic Batch Injector
     */
    private static void injectBatch(WebView webView, Context context, String[] assetPaths) {
        StringBuilder payload = new StringBuilder(48_000);

        payload.append("(function(){\n");
        payload.append("try {\n");

        for (String path : assetPaths) {
            appendScript(payload, context, path);
        }

        payload.append("\n} catch(e) { console.error('❌ Royal Engine bootstrap failed', e); }\n");
        payload.append("})();");

        webView.evaluateJavascript(payload.toString(), null);
        Log.i(TAG, "✅ Royal Engine injected as atomic batch");
    }

    /**
     * 📦 Append asset with DevTools visibility
     */
    private static void appendScript(StringBuilder out, Context context, String assetPath) {
        try (InputStream is = context.getAssets().open(assetPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String fileName = assetPath.substring(assetPath.lastIndexOf('/') + 1);

            out.append("\n/* ===== ").append(fileName).append(" ===== */\n");

            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append('\n');
            }

            out.append("\n//# sourceURL=royal://").append(fileName).append("\n");

        } catch (Exception ex) {
            Log.e(TAG, "❌ Failed to load asset: " + assetPath, ex);
        }
    }

    private WebEnhancer() {}
}
