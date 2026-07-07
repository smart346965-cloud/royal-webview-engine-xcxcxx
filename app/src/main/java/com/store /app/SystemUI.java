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

    // 1. تفعيل وضع "التمدد الشفاف" مع حماية المحتوى من التداخل
    public static void applyKingMode(FragmentActivity activity, WebView webView) {

        if (isApplied) return;
        isApplied = true;

        Window window = activity.getWindow();

        // Edge-to-Edge حقيقي
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        // منع الطبقة الرمادية في Android 10+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.setNavigationBarContrastEnforced(false);
        }

        View content = activity.findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {

            androidx.core.graphics.Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            view.setPadding(
                    0,
                    systemBars.top,
                    0,
                    systemBars.bottom
            );

            return WindowInsetsCompat.CONSUMED;
        });

        ViewCompat.requestApplyInsets(content);
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
