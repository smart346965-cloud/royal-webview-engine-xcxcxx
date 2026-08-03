package com.store.app;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 👑 MainActivity - النواة الأساسية لإدارة محرك الويب المخصص
 * تم تطهيرها بالكامل من مخلفات الـ TWA لتعمل بأقصى سرعة استجابة (Zero-friction)
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoyalMainActivity";
    private static final long FIXED_SPLASH_TIME = 5000; // قيمة ثابتة 5 ثوانٍ بالتمام والكمال

    private boolean splashRemoved = false;
    private boolean isPageReady = false; // flag للرندرة

    private WebEngineManager engineManager;
    private WebView activeWebView;
    private ProgressBar progressBar;
    private TextView offlineBar;

    // [تعديل في MainActivity.java - منطقة التعريفات]
    private FrameLayout pureOfflineUI; // الحاوية الكبرى لواجهة أوفلاين
    private boolean isOfflineUIVisible = false;

    private long splashStartTime = 0;

    // =========================================================
    // 🚀 دورة الحياة الأساسية
    // =========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 👑 [تعديل جراحي ملكي 1]: استلام التحكم بأنيميشن خروج سبلاش النظام لجعل خروجه ناعماً للغاية
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                // تنفيذ أنيميشن شفافية ناعم (Fade-Out) لسبلاش النظام لمنع الاختفاء المفاجئ
                splashScreenView.animate()
                        .alpha(0f)
                        .setDuration(500) // 500 ملي ثانية لأنيميشن اختفاء سينمائي
                        .withEndAction(splashScreenView::remove)
                        .start();
            });
        }

        // 🛡️ درع الوميض: مطابقة الخلفية مع لون السبلاش لمنع الوميض الأبيض الصارخ
        setTheme(R.style.AppTheme_NoSplash);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F3F4F6")));

        super.onCreate(savedInstanceState);

        // 🔍 تفعيل محرك الفحص والتشخيص الذكي
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

        // 🚀 السطر الذهبي: حاول الإحياء الثنائي أولاً
        boolean sessionRestored = RoyalSessionSentinel.resurrect(activeWebView, this);

        if (!sessionRestored) {
            // إذا لم توجد جلسة، حمّل الرابط الافتراضي
            activeWebView.loadUrl(BuildConfig.CLIENT_URL);
        }

        // 4️⃣ نظام التحكم بالرجوع المستقل نيتف
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (activeWebView != null && activeWebView.canGoBack()) {
                    activeWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    activeWebView.goBack();
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (activeWebView != null) {
                            activeWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                        }
                    }, 1000);
                } else {
                    moveTaskToBack(true);
                }
            }
        });

        // 5️⃣ الحصانة البصرية وتخصيص شريط النظام بالكامل
        SystemUI.applyKingMode(this, activeWebView);
        SystemUI.setDynamicIcons(this.getWindow(), true);

        // [بناء واجهة الأوفلاين الناتيف فوراً]
        createPureOfflineUI();

        // 6️⃣ بناء وتجهيز طبقة شاشة التحميل (Splash Screen Overlay)
        setupSplashScreen();

        // 7️⃣ إنشاء شريط الأوفلاين السينمائي
        createOfflineBar();

        // 🚀 فحص الإنترنت الأولي (عند الإقلاع)
        if (!NetworkMonitor.isInternetAvailable(this)) {
            toggleOfflineUI(true);
        }

        // ربط الشريط بمراقب الشبكة
        NetworkMonitor.setListener(connected -> {
            if (connected) {
                if (isOfflineUIVisible) toggleOfflineUI(false);
                // إخفاء الشريط النحيف أيضاً إذا كان ظاهراً
                if (offlineBar != null) {
                    offlineBar.animate().translationY(100).setDuration(400).withEndAction(() -> offlineBar.setVisibility(View.GONE)).start();
                }

                // إعادة تحميل الموقع تلقائياً إذا كنا في صفحة بيضاء
                if (activeWebView.getUrl() == null || activeWebView.getUrl().equals("about:blank")) {
                    runOnUiThread(() -> activeWebView.loadUrl(BuildConfig.CLIENT_URL));
                }
            } else {
                // إذا كنا في بداية التشغيل، اظهر الواجهة الكبيرة، وإلا اظهر الشريط النحيف فقط
                if (activeWebView.getUrl() == null || activeWebView.getUrl().equals("about:blank")) {
                    toggleOfflineUI(true);
                } else if (offlineBar != null) {
                    offlineBar.setVisibility(View.VISIBLE);
                    offlineBar.animate().translationY(0).setDuration(400).start();
                }
            }
        });
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

    // =========================================================
    // ⚙️ إعدادات واجهة السبلاش
    // =========================================================

    private void setupSplashScreen() {
        splashStartTime = System.currentTimeMillis();

        // 👑 [تعديل جراحي ملكي 2]: تجميد الشاشة حتى اكتمال الـ 5 ثوانٍ، ثم إطلاق أنيميشن الـ Fade-out
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById(android.R.id.content).getViewTreeObserver().addOnPreDrawListener(
                    new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            if (splashRemoved) {
                                // انقضت الـ 5 ثوانٍ.. نسمح للنظام بالرسم ليبدأ أنيميشن الـ Fade-Out
                                findViewById(android.R.id.content).getViewTreeObserver().removeOnPreDrawListener(this);
                                return true;
                            } else {
                                // الـ 5 ثوانٍ لم تنتهِ بعد.. جمّد الشاشة بصلابة!
                                return false;
                            }
                        }
                    }
            );
        }

        final FrameLayout splashContainer = new FrameLayout(this);
        splashContainer.setBackgroundColor(Color.parseColor("#F3F4F6"));

        ImageView splashIcon = new ImageView(this);
        splashIcon.setImageResource(R.mipmap.ic_launcher);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(280, 280, android.view.Gravity.CENTER);
        splashIcon.setLayoutParams(iconParams);
        splashContainer.addView(splashIcon);

        addContentView(splashContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        addContentView(progressBar, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 8));

        engineManager = new WebEngineManager(
                this, activeWebView, splashContainer, progressBar,
                () -> splashRemoved = true, () -> splashRemoved
        );
        engineManager.setSplashStartTime(splashStartTime);
        engineManager.init();

        // 🚀 الـ Handler المعتمد للـ 5 ثوانٍ
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!splashRemoved) {
                engineManager.triggerFinalReveal();
            }
        }, FIXED_SPLASH_TIME);

        // 🛡️ تعطيل الاستجابة التلقائية للجسور
        if (RoyalWebViewHost.getBridge() != null) {
            RoyalWebViewHost.getBridge().setOnHideSplashCallback(() -> {
                Log.i(TAG, "⚡ Page ready, but Splash is LOCKED by engineer's timer.");
            });
        }
    }

    // =========================================================
    // 📡 شريط الأوفلاين
    // =========================================================

    private void createOfflineBar() {
        offlineBar = new TextView(this);
        offlineBar.setText("لا يتوفر اتصال بالإنترنت");
        offlineBar.setTextColor(Color.WHITE);
        offlineBar.setBackgroundColor(Color.parseColor("#323232")); // أسود يوتيوب الأنيق
        offlineBar.setGravity(android.view.Gravity.CENTER);
        offlineBar.setPadding(0, 12, 0, 12);
        offlineBar.setTextSize(14f);
        offlineBar.setVisibility(View.GONE); // مخفي في البداية

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 80, android.view.Gravity.BOTTOM);
        // وضعه فوق أزرار التنقل قليلاً
        params.bottomMargin = 0;
        addContentView(offlineBar, params);
    }

    // =========================================================
    // 🍏 واجهة الأوفلاين الناتيف (Apple Style)
    // =========================================================

    // [إضافة جراحية في MainActivity.java - بناء الواجهة الناتيف]
    private void createPureOfflineUI() {
        // 1. الحاوية الرئيسية
        pureOfflineUI = new FrameLayout(this);
        pureOfflineUI.setBackgroundColor(Color.parseColor("#F3F4F6"));
        pureOfflineUI.setVisibility(View.GONE); // مخفية افتراضياً

        // 2. شعار المتجر في المنتصف (R.mipmap.ic_launcher)
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        FrameLayout.LayoutParams logoParams = new FrameLayout.LayoutParams(320, 320, android.view.Gravity.CENTER);
        pureOfflineUI.addView(logo, logoParams);

        // 3. الكرت السفلي الفاخر (Apple Dark Card)
        LinearLayout bottomCard = new LinearLayout(this);
        bottomCard.setOrientation(LinearLayout.VERTICAL);
        bottomCard.setBackground(createCardDrawable()); // رسم الخلفية المنحنية
        bottomCard.setPadding(60, 80, 60, 80);
        bottomCard.setGravity(android.view.Gravity.CENTER);

        // أ- نقطة الحالة النابضة (Pulsing Red Dot)
        View statusDot = new View(this);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(Color.parseColor("#FF3B30")); // أحمر أبل
        statusDot.setBackground(dot);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(25, 25);
        dotParams.bottomMargin = 20;
        bottomCard.addView(statusDot, dotParams);

        // ب- نص "لا يتوفر اتصال"
        TextView msg = new TextView(this);
        msg.setText("لا يتوفر اتصال بالإنترنت");
        msg.setTextColor(Color.WHITE);
        msg.setTextSize(18f);
        msg.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        bottomCard.addView(msg);

        // ج- زر إعادة المحاولة (Retry Button)
        TextView retryBtn = new TextView(this);
        retryBtn.setText("إعادة المحاولة");
        retryBtn.setTextColor(Color.parseColor("#007AFF")); // أزرق أبل
        retryBtn.setPadding(0, 40, 0, 0);
        retryBtn.setOnClickListener(v -> {
            if (NetworkMonitor.isInternetAvailable(this)) {
                toggleOfflineUI(false);
                activeWebView.reload();
            } else {
                // هزاز خفيف للإشارة إلى فشل المحاولة
                v.animate().translationX(10).setDuration(50)
                        .withEndAction(() -> v.animate().translationX(-10).setDuration(50)
                                .withEndAction(() -> v.setTranslationX(0)).start()).start();
            }
        });
        bottomCard.addView(retryBtn);

        // 4. وضع الكرت في أسفل الشاشة
        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, android.view.Gravity.BOTTOM);
        pureOfflineUI.addView(bottomCard, cardParams);

        addContentView(pureOfflineUI, new ViewGroup.LayoutParams(-1, -1));
    }

    // دالة لرسم خلفية الكرت المنحنية بامتياز
    private Drawable createCardDrawable() {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor("#1C1C1E")); // رمادي داكن فاخر
        gd.setCornerRadii(new float[]{80, 80, 80, 80, 0, 0, 0, 0}); // زوايا علوية فقط
        return gd;
    }

    // محرك التبديل بين الـ WebView والواجهة الناتيف
    private void toggleOfflineUI(boolean show) {
        isOfflineUIVisible = show;
        runOnUiThread(() -> {
            if (show) {
                pureOfflineUI.setVisibility(View.VISIBLE);
                pureOfflineUI.setAlpha(0f);
                pureOfflineUI.animate().alpha(1f).setDuration(500).start();
                activeWebView.setVisibility(View.GONE);
            } else {
                pureOfflineUI.animate().alpha(0f).setDuration(500)
                        .withEndAction(() -> pureOfflineUI.setVisibility(View.GONE)).start();
                activeWebView.setVisibility(View.VISIBLE);
            }
        });
    }

    // =========================================================
    // 🔄 نتائج النشاطات والصلاحيات
    // =========================================================

    // 👑 [تعديل جراحي]: الجسر المفقود لاستقبال نتائج الاستوديو ومدير الملفات
    // هذه الدالة تلتقط الملف/الصورة التي اختارها المستخدم وتعيدها مباشرة إلى محرك الويب
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RoyalCapabilitiesEngine.FILECHOOSER_RESULTCODE) {
            if (RoyalCapabilitiesEngine.filePathCallback == null) return;

            Uri[] results = null;

            // التحقق من أن المستخدم اختار ملفاً بالفعل ولم يتراجع
            if (resultCode == android.app.Activity.RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    android.content.ClipData clipData = data.getClipData();

                    // دعم رفع ملفات متعددة (Multiple Files Upload)
                    if (clipData != null) {
                        results = new Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    }
                    // دعم رفع ملف واحد
                    else if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }
            }

            // إرسال النتيجة إلى الويب فيو (سواء كانت ملفات أو null إذا ألغى المستخدم)
            RoyalCapabilitiesEngine.filePathCallback.onReceiveValue(results);
            RoyalCapabilitiesEngine.filePathCallback = null;
        }
    }

    // [تعديل جراحي في MainActivity.java - جسر الصلاحيات]
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // 🛡️ تمرير نتيجة موافقة المستخدم إلى محرك القدرات
        if (engineManager != null && engineManager.getCapabilitiesHandler() != null) {
            // إذا كنت تستخدم اسم الكلاس من المهندس (RoyalCapabilitiesEngine)
            // تأكد من إضافة دالة getCapabilitiesHandler() في WebEngineManager
            engineManager.getCapabilitiesHandler().handlePermissionResult(requestCode, grantResults);
        }
    }
                }
