Enterpackage com.store.app;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    private boolean splashRemoved = false;
    private WebEngineManager engineManager;

    private WebView immortalWebView;

    // 1️⃣ إزالة @Override وتغيير الاسم لتتوافق مع Capacitor 6
    private WebView setupImmortalWebView() {
        // إنشاء الويبفيو الخالد إذا لم يكن موجوداً
        if (!RoyalWebViewHost.isReady()) {
            RoyalWebViewHost.create(getApplicationContext());
        }

        // حماية الخبير التقني: منع إعادة الربط
        if (immortalWebView == null) {
            immortalWebView = RoyalWebViewHost.attach(this);
        }

        return immortalWebView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoSplash);

        // 🛡️ درع الوميض الثاني: تلوين نافذة الأندرويد بالأبيض بدلاً من null (الذي يعطي سواداً)
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.WHITE));

        // 1️⃣ Capacitor يبني غرفته ويضع فيها الويب فيو الافتراضي (الضعيف)
        super.onCreate(savedInstanceState);

        android.webkit.WebView.setWebContentsDebuggingEnabled(true);

        // 2️⃣ نستدعي محركنا الخالد (الشبح الموجود في الذاكرة والذي يحمل Gymshark)
        WebView immortalWv = setupImmortalWebView();

        // 3️⃣ ⚔️ عملية "التبديل الجزيئي" (Atomic View Swap) ⚔️
        WebView defaultWv = getBridge().getWebView();

        if (defaultWv != null && defaultWv != immortalWv) {

            String currentUrl = defaultWv.getUrl(); // 🔥 أخذ الرابط الحالي

            android.view.ViewGroup parent = (android.view.ViewGroup) defaultWv.getParent();

            if (parent != null) {

                int index = parent.indexOfChild(defaultWv);
                android.view.ViewGroup.LayoutParams params = defaultWv.getLayoutParams();

                parent.removeView(defaultWv);

                parent.addView(immortalWv, index, params);

                // 🔥 نقل الصفحة الحالية للمحرك الجديد
                if (currentUrl != null) {
                    immortalWv.loadUrl(currentUrl);
                }

                defaultWv.destroy();

                android.util.Log.i("RoyalEngine", "⚔️ Atomic Swap Complete");
            }
        }

        // 4️⃣ الآن المحرك النشط والمرئي هو محركنا الخالد
        WebView activeWebView = immortalWv;

        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // حماية زر الرجوع
                        WebView currentWv = RoyalWebViewHost.isReady() ? RoyalWebViewHost.get() : getBridge().getWebView();

                        if (currentWv != null && currentWv.canGoBack()) {
                            // 👑 وضع الرجوع في طابور الـ UI لمنع التقطيع (Micro-blocking)
                            currentWv.post(() -> currentWv.goBack());
                        } else {
                            // إخفاء التطبيق في الخلفية بدلاً من قتله
                            moveTaskToBack(true);
                        }
                    }
                });

        SystemUI.applyKingMode(this, activeWebView);
        SystemUI.setDynamicIcons(this.getWindow(), true);

        // Splash Overlay
        final View splashOverlay = new View(this);
        splashOverlay.setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"));
        splashOverlay.setAlpha(1f);

        addContentView(splashOverlay, new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT));

        // ProgressBar
        final android.widget.ProgressBar progressBar =
                new android.widget.ProgressBar(this, null,
                        android.R.attr.progressBarStyleHorizontal);

        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        progressBar.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                6
        ));

        progressBar.setScaleY(0.6f);
        addContentView(progressBar, progressBar.getLayoutParams());

        // إنشاء محرك الموقع (نمرر له النسخة الحية والمرئية)
        engineManager = new WebEngineManager(
                this,
                activeWebView,
                splashOverlay,
                progressBar,
                () -> splashRemoved = true,
                () -> splashRemoved
        );

        engineManager.init();

        // Fail-safe
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            WebView wv = RoyalWebViewHost.isReady() ? RoyalWebViewHost.get() : getBridge().getWebView();
            if (wv != null) {
                android.util.Log.w("Engine", "Fail-safe: Forced reveal after timeout");
            }
        }, 10000);
    }

    // 4️⃣ تغيير protected إلى public لحل تعارض الصلاحيات
    @Override
    public void onDestroy() {
        // الإنقاذ الجراحي: نفصل الويب فيو ونخبئه *قبل* أن يقوم Capacitor بقتله!
        RoyalWebViewHost.detach();

        super.onDestroy();
    }
}
