package com.store.app;

import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.FragmentActivity;

public class SystemUI {

    private static boolean isApplied = false;

    // 1. تفعيل وضع "التمدد الشفاف" مع الاستجابة اللحظية لإيماءات الرجوع
    public static void applyKingMode(FragmentActivity activity, WebView webView) {

        if (isApplied) return;
        isApplied = true;

        Window window = activity.getWindow();

        // Edge-to-Edge حقيقي: جعل المحتوى يتمدد 100% خلف الأشرطة الشفافة
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // منع الطبقة الرمادية في Android 10+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        View content = activity.findViewById(android.R.id.content);

        // 🔥 تصفير الحواف لكي يحتل الـ WebView الشاشة بالكامل دون اقتطاع
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
            view.setPadding(0, 0, 0, 0); 
            return WindowInsetsCompat.CONSUMED;
        });

        // 👑 [تعديل حاسم]: تفعيل وضع الشفافية المباشرة لضمان عمل السحب للرجوع من المرة الأولى
        enableTransparentEdgeToEdge(window);
    }

    // 👑 تحرير إيماءات الحافة (Gesture Lock Bypass)
    private static void enableTransparentEdgeToEdge(Window window) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            // نضمن إظهار الأشرطة كطبقة شفافة 100% فوق الـ WebView
            // هذا يمنع الأندرويد من حجز السحبة الأولى، وتعمل إيماءة الرجوع فوراً!
            controller.show(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT);
        }
    }

    // 2. المحرك الذكي للألوان العكسية - النسخة المطورة (Window-based)
    public static void setDynamicIcons(android.view.Window window, boolean isLightBackground) {

        if (window == null) return;

        androidx.core.view.WindowInsetsControllerCompat controller =
                androidx.core.view.WindowCompat.getInsetsController(window, window.getDecorView());

        if (controller != null) {
            controller.setAppearanceLightStatusBars(isLightBackground);
            controller.setAppearanceLightNavigationBars(isLightBackground);
        }
    }

    // 3. دالة كشف السطوع (المعالج الرياضي للألوان)
    public static boolean isColorLight(int color) {
        double darkness = 1 - (0.299 * Color.red(color)
                + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
            }
