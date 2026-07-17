package com.store.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.FrameLayout;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

/**
 * 👑 MainActivity - النواة الأساسية لإدارة محرك الويب المخصص
 * تم تطهيرها بالكامل من مخلفات الـ TWA لتعمل بأقصى سرعة استجابة (Zero-friction)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoyalMainActivity";
    private boolean splashRemoved = false;
    private WebEngineManager engineManager;
    private WebView activeWebView;
    private ProgressBar progressBar;
    private long splashStartTime = 0;
    private static final long MIN_SPLASH_TIME = 4500; // الحد الأدنى لبقاء السبلاش (1.8 ثانية) لضمان الاستقرار البصري

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🛡️ درع الوميض: مطابقة الخلفية مع لون السبلاش لمنع الوميض الأبيض الصارخ
        setTheme(R.style.AppTheme_NoSplash);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F3F4F6")));
        
        super.onCreate(savedInstanceState);

        // 🔍 تفعيل محرك الفحص والتشخيص الذكي (الآن سيغذي LogFox فوراً!)
        try {
            RoyalPanopticon.startAwareness();
            Log.i(TAG, "RoyalPanopticon Engine: Active and running in background.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RoyalPanopticon: " + e.getMessage());
        }

        // تفعيل أدوات تصحيح الويب التقنية عبر المتصفح
        WebView.setWebContentsDebuggingEnabled(true);

        // 1️⃣ استدعاء وتهيئة الويب فيو الخالد مباشرة بدون وسطاء
        if (!RoyalWebViewHost.isReady()) {
            RoyalWebViewHost.create(getApplicationContext());
        }
        activeWebView = RoyalWebViewHost.attach(this);

        // 2️⃣ تعيين المحرك الخالد كواجهة أساسية مباشرة (استجابة 0ms)
        setContentView(activeWebView);

        // 3️⃣ توجيه المحرك للهدف
        String targetUrl = "https://m.shein.com/"; 
        if (activeWebView.getUrl() == null || !activeWebView.getUrl().startsWith("http")) {
            activeWebView.loadUrl(targetUrl);
        }

        // 4️⃣ نظام التحكم بالرجوع المستقل نيتف (Native Back Press Handling)
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (activeWebView != null && activeWebView.canGoBack()) {
                    // قفل المحرك على الكاش لضمان سرعة الاستجابة
                    activeWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    
                    // إخفاء البروجرس بار لأنه لن يكون له داعٍ في الرجوع اللحظي
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    activeWebView.goBack();
                    
                    // إعادة الضبط للوضع الافتراضي
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (activeWebView != null) {
                            activeWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                        }
                    }, 1000);
                } else {
                    // إرسال التطبيق للخلفية بدلاً من إغلاقه بالكامل للحفاظ على الويب فيو ساخناً بالذاكرة
                    moveTaskToBack(true);
                }
            }
        });

        // 5️⃣ الحصانة البصرية وتخصيص شريط النظام بالكامل
        SystemUI.applyKingMode(this, activeWebView);
        SystemUI.setDynamicIcons(this.getWindow(), true);

        // 6️⃣ بناء وتجهيز طبقة شاشة التحميل (Splash Screen Overlay)
        setupSplashScreen();
    }

    private void setupSplashScreen() {
        splashStartTime = System.currentTimeMillis();

        // 1. الحاوية الرئيسية (خلفية السبلاش)
        final FrameLayout splashContainer = new FrameLayout(this);
        splashContainer.setBackgroundColor(Color.parseColor("#F3F4F6"));
        splashContainer.setAlpha(1f);

        // 2. إضافة الأيقونة في المنتصف (Unified Icon)
        ImageView splashIcon = new ImageView(this);
        // ملاحظة: استبدل R.mipmap.ic_launcher بأيقونة السبلاش الخاصة بك
        splashIcon.setImageResource(R.mipmap.ic_launcher); 
        
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(
                280, 280, android.view.Gravity.CENTER); // حجم الأيقونة 280px
        splashIcon.setLayoutParams(iconParams);
        
        splashContainer.addView(splashIcon);

        addContentView(splashContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // 3. شريط التقدم النحيف (وضعناه فوق الحاوية)
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setScaleY(0.6f);
        addContentView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 8));

        // 4. ربط المدير
        engineManager = new WebEngineManager(
                this, activeWebView, splashContainer, progressBar,
                () -> splashRemoved = true, () -> splashRemoved
        );
        engineManager.setSplashStartTime(splashStartTime); // تمرير وقت البدء للمدير
        engineManager.init();

        // 5. المزامنة مع الجسر
        if (RoyalWebViewHost.getBridge() != null) {
            RoyalWebViewHost.getBridge().setOnHideSplashCallback(() -> {
                if (!splashRemoved) engineManager.removeSplashSmoothly();
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // إيقاف مؤقت للعمليات الرسومية غير النشطة في الخلفية للحفاظ على طاقة الجهاز
        if (activeWebView != null) {
            activeWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // استئناف العمليات الرسومية والـ JavaScript فور عودة المستخدم للتطبيق
        if (activeWebView != null) {
            activeWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        // 🛡️ التعديل: لا تحمل about:blank، فقط افصل الويب فيو بأمان
        if (activeWebView != null) {
            // نكتفي بإيقاف العمليات دون مسح السطح الرسومي
            activeWebView.stopLoading();
        }
        RoyalWebViewHost.detach();
        super.onDestroy();
    }
    }
