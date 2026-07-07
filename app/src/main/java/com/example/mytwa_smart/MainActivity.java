package com.example.mytwa_smart;

import android.os.Bundle;
import com.google.androidbrowserhelper.trusted.LauncherActivity;

/**
 * 👑 ROYAL MAIN ACTIVITY (The Pure TWA Shell)
 * لا نكتب أي كود للسبلاش هنا. مكتبة جوجل تتولى نقل السبلاش إلى كروم بسلاسة مطلقة.
 */
public class MainActivity extends LauncherActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // لا تضيف SplashScreen.installSplashScreen(this) هنا!
        // LauncherActivity تقوم بكل السحر البصري بنفسها.
    }
    
    // 👑 ملاحظة بخصوص الأوفلاين: 
    // مكتبة LauncherActivity تمتلك نظام Fallback داخلي. 
    // إذا أردت تفعيل الأوفلاين، سنقوم بذلك عبر كلاس DelegationService لاحقاً، 
    // وليس بتشويه هذه الشاشة.
}
