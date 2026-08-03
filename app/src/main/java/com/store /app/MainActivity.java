package com.store.app;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.ProgressBar;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import java.io.File;

/**
 * 👑 MainActivity - النواة الأساسية لإدارة محرك الويب المخصص
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "RoyalMainActivity";
    private boolean splashRemoved = false;
    private WebEngineManager engineManager;
    private WebView activeWebView;
    private ProgressBar progressBar;
    private long splashStartTime = 0;
    private static final long FIXED_SPLASH_TIME = 5000;
    private boolean isPageReady = false;
    private TextView offlineBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSplashScreen().setOnExitAnimationListener(splashScreenView -> {
                splashScreenView.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(splashScreenView::remove)
                        .start();
            });
        }

        setTheme(R.style.AppTheme_NoSplash);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F3F4F6")));
        
        super.onCreate(savedInstanceState);

        // 👑 تثبيت وتهيئة محرك الشبكة والتنبؤ والتأكد من إطلاق primeRootUrl()
        RoyalNetworkEngine.install(getApplicationContext());

        try {
            RoyalPanopticon.startAwareness();
            Log.i(TAG, "RoyalPanopticon Engine: Active and running in background.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RoyalPanopticon: " + e.getMessage());
        }

        WebView.setWebContentsDebuggingEnabled(true);

        if (!RoyalWebViewHost.isReady()) {
            RoyalWebViewHost.create(getApplicationContext());
        }
        activeWebView = RoyalWebViewHost.attach(this);

        setContentView(activeWebView);

        boolean sessionRestored = RoyalSessionSentinel.resurrect(activeWebView, this);

        if (!sessionRestored) {
            // 🛡️ معالجة الفتح بدون إنترنت (Cold Start Offline Handshake)
            if (!NetworkMonitor.isInternetAvailable(this)) {
                activeWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                
                // ⚓ التحقق الفوري من وجود مرساة الصفحة الرئيسية الأوفلاين وحقنها فوراً
                File anchorFile = new File(getFilesDir(), "royal_vault_v1/html/root_anchor.html");
                if (anchorFile.exists() && anchorFile.length() > 0) {
                    Log.i(TAG, "⚓ Cold Start Offline: Instantly loading saved Root Anchor!");
                    activeWebView.loadUrl("file://" + anchorFile.getAbsolutePath());
                } else {
                    activeWebView.loadUrl(BuildConfig.CLIENT_URL);
                }
            } else {
                activeWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                activeWebView.loadUrl(BuildConfig.CLIENT_URL);
            }
        }

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

        SystemUI.applyKingMode(this, activeWebView);
        SystemUI.setDynamicIcons(this.getWindow(), true);

        setupSplashScreen();

        createOfflineBar();
        
        NetworkMonitor.setListener(connected -> {
            if (activeWebView != null) {
                activeWebView.getSettings().setCacheMode(
                    connected ? WebSettings.LOAD_DEFAULT : WebSettings.LOAD_CACHE_ELSE_NETWORK
                );
            }
            if (connected) {
                offlineBar.animate().translationY(100).setDuration(400).withEndAction(() -> offlineBar.setVisibility(View.GONE)).start();
            } else {
                offlineBar.setVisibility(View.VISIBLE);
                offlineBar.setTranslationY(100);
                offlineBar.animate().translationY(0).setDuration(400).start();
            }
        });
    }

    private void createOfflineBar() {
        offlineBar = new TextView(this);
        offlineBar.setText("لا يتوفر اتصال بالإنترنت");
        offlineBar.setTextColor(Color.WHITE);
        offlineBar.setBackgroundColor(Color.parseColor("#323232"));
        offlineBar.setGravity(android.view.Gravity.CENTER);
        offlineBar.setPadding(0, 12, 0, 12);
        offlineBar.setTextSize(14f);
        offlineBar.setVisibility(View.GONE);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 80, android.view.Gravity.BOTTOM);
        params.bottomMargin = 0; 
        addContentView(offlineBar, params);
    }

    private void setupSplashScreen() {
        splashStartTime = System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            findViewById(android.R.id.content).getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if (splashRemoved) {
                            findViewById(android.R.id.content).getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
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

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!splashRemoved) {
                engineManager.triggerFinalReveal();
            }
        }, FIXED_SPLASH_TIME);

        if (RoyalWebViewHost.getBridge() != null) {
            RoyalWebViewHost.getBridge().setOnHideSplashCallback(() -> {
                Log.i(TAG, "⚡ Page ready, but Splash is LOCKED by engineer's timer.");
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (activeWebView != null) {
            activeWebView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (activeWebView != null) {
            activeWebView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (activeWebView != null) {
            activeWebView.stopLoading();
        }
        RoyalWebViewHost.detach();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RoyalCapabilitiesEngine.FILECHOOSER_RESULTCODE) {
            if (RoyalCapabilitiesEngine.filePathCallback == null) return;

            android.net.Uri[] results = null;

            if (resultCode == android.app.Activity.RESULT_OK) {
                if (data != null) {
                    String dataString = data.getDataString();
                    android.content.ClipData clipData = data.getClipData();

                    if (clipData != null) {
                        results = new android.net.Uri[clipData.getItemCount()];
                        for (int i = 0; i < clipData.getItemCount(); i++) {
                            results[i] = clipData.getItemAt(i).getUri();
                        }
                    } 
                    else if (dataString != null) {
                        results = new android.net.Uri[]{android.net.Uri.parse(dataString)};
                    }
                }
            }

            RoyalCapabilitiesEngine.filePathCallback.onReceiveValue(results);
            RoyalCapabilitiesEngine.filePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (engineManager != null && engineManager.getCapabilitiesHandler() != null) {
            engineManager.getCapabilitiesHandler().handlePermissionResult(requestCode, grantResults);
        }
    }
}
