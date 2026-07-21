package com.store.app;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentActivity;

public class SystemUI {

    private static boolean isApplied = false;

    // 1. تفعيل وضع "الملك" مع استجابة فورية لإيماءات الرجوع (Instant Gesture Response)
    public static void applyKingMode(FragmentActivity activity, WebView webView) {

        if (isApplied) return;
        isApplied = true;

        Window window = activity.getWindow();

        // 🚀 الخطوة 1: تمديد المحتوى خلف الأشرطة تماماً (Edge-to-Edge)
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // 🚀 الخطوة 2: كسر قيود الحواف (تجاوز حاجز السحبتين)
        // هذا السطر يخبر النظام ألا يحجز حواف الشاشة للأشرطة، مما يحرر إيماءة الرجوع
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        // إزالة أي حدود وهمية للنظام
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        View content = activity.findViewById(android.R.id.content);

        // 🔥 تصفير الحواف لضمان احتلال الويب فيو لـ 100% من المساحة
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
            view.setPadding(0, 0, 0, 0); 
            return WindowInsetsCompat.CONSUMED;
        });

        // تشغيل محرك الاختفاء الذكي مع الحفاظ على "الرجوع اللحظي"
        enableSmartTransientImmersive(window);
    }

    // 👑 تقنية الإخفاء العابر (Transient Immersive Engine)
    private static void enableSmartTransientImmersive(Window window) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // 🛡️ السر هنا: استخدام BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE 
            // مع التأكد من أننا نطلب الإخفاء "بشكل عابر" فقط
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

            // تأخير زمني مدروس (4.5 ثانية كما طلبت)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // إخفاء الأشرطة
                controller.hide(WindowInsetsCompat.Type.systemBars());
                
                // 💡 تلميح تقني: في وضع Transient، السحبة من الحافة ستطلق "الرجوع" 
                // وفي نفس الوقت ستظهر الأشرطة بشكل مؤقت فوق المحتوى.
            }, 4500);
        }
    }

    // 2. المحرك الذكي للألوان العكسية للأيقونات
    public static void setDynamicIcons(android.view.Window window, boolean isLightBackground) {
        if (window == null) return;
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(isLightBackground);
            controller.setAppearanceLightNavigationBars(isLightBackground);
        }
    }

    // 3. دالة كشف السطوع
    public static boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
            }
