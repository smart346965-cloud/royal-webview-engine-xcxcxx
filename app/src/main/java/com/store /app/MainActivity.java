package com.store.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

// 👑 وراثة AppCompatActivity الصافية بدلاً من BridgeActivity
public class MainActivity extends AppCompatActivity {

    private boolean splashRemoved = false;
    private WebEngineManager engineManager;
    private WebView activeWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🛡️ درع الوميض: قبل أي بناء للواجهة
        setTheme(R.style.AppTheme_NoSplash);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        
        super.onCreate(savedInstanceState);

        // تفعيل التصحيح التقني
        WebView.setWebContentsDebuggingEnabled(true);

        // 1️⃣ استدعاء وتهيئة الويب فيو الخالد مباشرة (لا وسيط بعد اليوم)
        if (!RoyalWebViewHost.isReady()) {
            RoyalWebViewHost.create(getApplicationContext());
        }
        activeWebView = RoyalWebViewHost.attach(this);

        // 2️⃣ تعيين المحرك الخالد كواجهة أساسية مباشرة (0ms احتكاك)
        setContentView(activeWebView);

        // 3️⃣ توجيه المحرك للهدف (رابط تجريبي عالي الأداء)
        String targetUrl = "https://row.gymshark.com/"; 
        if (activeWebView.getUrl() == null || !activeWebView.getUrl().startsWith("http")) {
            activeWebView.loadUrl(targetUrl);
        }

        // 4️⃣ نظام التحكم بالرجوع المستقل هندسياً (Native Back Press)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (activeWebView != null && activeWebView.canGoBack()) {
                    activeWebView.post(() -> activeWebView.goBack());
                } else {
                    moveTaskToBack(true);
                }
            }
        });

        // 5️⃣ الحصانة البصرية وتخصيص شريط النظام
        SystemUI.applyKingMode(this, activeWebView);
        SystemUI.setDynamicIcons(this.getWindow(), true);

        // 6️⃣ بناء طبقة الـ Splash Screen برمجياً كما صممتها
        setupSplashScreen();
    }

    private void setupSplashScreen() {
        // شاشة التحميل (Overlay)
        final View splashOverlay = new View(this);
        splashOverlay.setBackgroundColor(Color.parseColor("#F3F4F6"));
        splashOverlay.setAlpha(1f);

        addContentView(splashOverlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // شريط التقدم النحيف (ProgressBar)
        final ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);
        
        ViewGroup.LayoutParams progressParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 6);
        progressBar.setLayoutParams(progressParams);
        progressBar.setScaleY(0.6f);
        
        addContentView(progressBar, progressParams);

        // 7️⃣ تهيئة الـ WebEngineManager
        engineManager = new WebEngineManager(
                this,
                activeWebView,
                splashOverlay,
                progressBar,
                () -> splashRemoved = true,
                () -> splashRemoved
        );
        engineManager.init();

        // Fail-safe: حماية تجربة المستخدم بإخفاء الشاشة إن حدث تأخير استثنائي
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!splashRemoved && activeWebView != null) {
                Log.w("RoyalEngine", "Fail-safe: Forced reveal after timeout");
                splashOverlay.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                splashRemoved = true;
            }
        }, 10000);
    }

    @Override
    protected void onDestroy() {
        // 🛡️ الإنقاذ الجراحي: نفصل الويب فيو ونبقيه حياً في الذاكرة لتسريع الفتح القادم
        RoyalWebViewHost.detach();
        super.onDestroy();
    }
}
