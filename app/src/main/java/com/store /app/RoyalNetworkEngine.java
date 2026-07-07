Enterpackage com.store.app;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * =========================================================
 * 🌐 ROYAL NETWORK ENGINE (V4 - The Warmup Assistant)
 * =========================================================
 * Architecture: Passive Observer, TCP/TLS Warmup, Zero I/O Blocking.
 * Philosophy: Open the road for Chromium, NEVER download for it.
 */
public final class RoyalNetworkEngine {

    private static final String TAG = "RoyalNetworkEngine";

    // 🛡️ نظام الميزانية الصارم (Budget Control)
    private static final long PREFETCH_COOLDOWN_MS = 300L;
    private static final int MAX_TRACKED_URLS = 60;
    private static final int MAX_WARMED_HOSTS = 10;

    // 🧠 ذاكرة الاستخبارات
    private static final Set<String> prefetchedUrls = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static final Set<String> warmedHosts = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    // ⚡ محرك خلفي خفيف جداً
    private static final ThreadPoolExecutor prefetchExecutor = new ThreadPoolExecutor(
            1, 2, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>()
    );

    // 🛡️ حارس المهام المتزامنة (Concurrent Task Limiter)
    private static final AtomicInteger activePrefetchTasks = new AtomicInteger(0);

    // 🎯 صائد الأنماط (Pattern Hunter)
    private static final Pattern SEQUENCE_PATTERN = Pattern.compile("^(.*[_-])(\\d+)(\\.[a-zA-Z0-9]+.*)$");
    private static final Pattern PRODUCT_PATTERN = Pattern.compile(".*/(product|item|p|detail)/.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAGE_PATTERN = Pattern.compile(".*(page=|/page/)(\\d+).*", Pattern.CASE_INSENSITIVE);

    private static long lastPrefetchTime = 0;
    private static volatile boolean renderBusy = false;
    private static boolean allowPrefetch = true;
    private static boolean isLowEndDevice = false;

    private static int lastScrollY = 0;
    private static long lastScrollTime = 0;
    private static int scrollVelocity = 0;

    private RoyalNetworkEngine() {}

    public static void install(Context context) {
        // 🚫 منع تدمير الأجهزة الضعيفة
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        isLowEndDevice = am.isLowRamDevice();

        // تهيئة مدير الكاش
        RoyalCacheManager.init(context);

        Log.i(TAG, "🌐 Royal Network Advisor V4 Installed. (TCP Warmup Mode). LowEnd: " + isLowEndDevice);
    }

    /**
     * 👁️ المراقب السلبي (Passive Observer)
     */
    public static WebResourceResponse interceptRequest(WebResourceRequest request) {
        // استدعاء مدير الكاش أولاً
        WebResourceResponse cached = RoyalCacheManager.intercept(request);
        if (cached != null) return cached;

        String url = request.getUrl().toString();

        if (!request.getMethod().equalsIgnoreCase("GET")) return null;

        // 🔮 التنبؤ
        if (isLikelyCacheable(url) && isImageResource(url)) {
            analyzeAndPredict(url);
        }

        analyzePagePrediction(url);

        return null; // Chromium هو الملك
    }

    // ==========================================
    // 🧠 الأنظمة الداخلية (Predictive Logic)
    // ==========================================

    private static void analyzeAndPredict(String currentUrl) {
        Matcher matcher = SEQUENCE_PATTERN.matcher(currentUrl);

        if (matcher.matches()) {
            String prefix = matcher.group(1);
            String numberStr = matcher.group(2);
            String suffix = matcher.group(3);

            try {
                int currentNumber = Integer.parseInt(numberStr);
                String nextNumberStr = String.format("%0" + numberStr.length() + "d", currentNumber + 1);
                String predictedUrl = prefix + nextNumberStr + suffix;

                scheduleWarmup(predictedUrl, false);
            } catch (NumberFormatException ignored) {}
        }
    }

    private static void analyzePagePrediction(String url) {
        try {
            if (PRODUCT_PATTERN.matcher(url).matches()) return;

            Matcher pageMatcher = PAGE_PATTERN.matcher(url);
            if (pageMatcher.matches()) {
                String pageNumberStr = pageMatcher.group(2);
                if (pageNumberStr != null && !pageNumberStr.isEmpty()) {
                    int next = Integer.parseInt(pageNumberStr) + 1;
                    String predicted = url.replace(pageNumberStr, String.valueOf(next));

                    scheduleWarmup(predicted, true);
                }
            }
        } catch (Exception ignored) {}
    }

    private static boolean isLikelyCacheable(String url) {
        return url.contains("cdn") || url.contains("static") || url.contains("assets") ||
                url.contains(".css") || url.contains(".js") || isImageResource(url);
    }

    private static boolean isImageResource(String url) {
        String u = url.toLowerCase();
        return u.endsWith(".png") || u.endsWith(".jpg") || u.endsWith(".jpeg") ||
                u.endsWith(".webp") || u.endsWith(".avif") || u.contains("cdn") || u.contains("img");
    }

    /**
     * 🚀 التحمية المسبقة للشبكة (Network Warmup)
     */
    private static void scheduleWarmup(String urlString, boolean isHighPriority) {
        if (!allowPrefetch || renderBusy || isLowEndDevice) return;

        // 👑 لا نتوقع إلا إذا كان السكرول سريعاً نسبياً
        boolean isFastScroll = scrollVelocity > 5;
        long deltaTime = System.currentTimeMillis() - lastScrollTime;
        boolean isFling = deltaTime < 50;

        if ((!isFastScroll && !isFling && !isHighPriority) && !isLikelyCacheable(urlString)) return;

        long now = System.currentTimeMillis();
        if (now - lastPrefetchTime < PREFETCH_COOLDOWN_MS) return;

        if (prefetchedUrls.contains(urlString)) return;

        // تقليل حجم الذاكرة لمنع Flush Storm
        if (prefetchedUrls.size() > MAX_TRACKED_URLS) {
            prefetchedUrls.clear();
        }

        // حارس المهام: لا نسمح بأكثر من مهمتين في نفس الوقت
        if (activePrefetchTasks.get() >= 2) return;

        prefetchedUrls.add(urlString);
        lastPrefetchTime = now;

        warmupHost(urlString);

        prefetchExecutor.execute(() -> {
            activePrefetchTasks.incrementAndGet();

            // تحسين الأولوية للمهام العالية الأهمية
            if (isHighPriority) {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_FOREGROUND);
            }

            try {
                URL url = new URL(urlString);

                // طلب GET مع الكاش للتخزين الفعلي في Cache حق Chromium
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setUseCaches(true);
                connection.setRequestProperty("Cache-Control", "max-age=60");
                connection.setRequestProperty("Connection", "keep-alive");
                connection.setConnectTimeout(1500);
                connection.setReadTimeout(1500);

                // إكمال Handshake مع السيرفر
                int code = connection.getResponseCode();

                if (code == HttpURLConnection.HTTP_OK) {

                    RoyalCacheManager.store(
                            urlString,
                            new BufferedInputStream(connection.getInputStream()),
                            connection.getHeaderFields()
                    );

                }

                connection.disconnect();

            } catch (Exception ignored) {
            } finally {
                activePrefetchTasks.decrementAndGet();
            }
        });
    }

    public static void notifyRenderStart() { renderBusy = true; }
    public static void notifyRenderIdle() { renderBusy = false; }
    public static void setNetworkPrefetchAllowed(boolean allowed) { allowPrefetch = allowed; }

    public static void notifyScroll(int scrollY) {
        long now = System.currentTimeMillis();
        int deltaY = scrollY - lastScrollY;
        long deltaTime = now - lastScrollTime;

        if (deltaTime > 0) {
            scrollVelocity = (int) (Math.abs(deltaY) / deltaTime);
        }

        lastScrollY = scrollY;
        lastScrollTime = now;
    }

    /**
     * 🚀 دالة التحمية المباشرة (تُستدعى من الجافاسكريبت)
     */
    public static void warmupLink(String urlString) {
        scheduleWarmup(urlString, true);
    }

    private static void warmupHost(String url) {
        try {
            String host = new URL(url).getHost();
            if (warmedHosts.contains(host)) return;

            // حماية الـ DNS من التضخم
            if (warmedHosts.size() > MAX_WARMED_HOSTS) {
                warmedHosts.clear();
            }
            warmedHosts.add(host);

            prefetchExecutor.execute(() -> {
                try {
                    // تسخين الـ DNS بصمت
                    String ignoredIp = java.net.InetAddress.getByName(host).getHostAddress();
                } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }
}
