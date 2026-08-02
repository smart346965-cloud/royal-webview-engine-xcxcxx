package com.store.app;

import android.content.Context;
import android.util.LruCache;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class RoyalCacheManager {

    private static class CacheMeta {
        long expiry;
        String etag;
        String lastModified;
        long created;
    }

    private static final String TAG = "RoyalCacheManager";

    // [تعديل جراحي 1: تعريف هيكلية المستودع الصلب]
    private static File vaultDir;
    private static Context context;
    private static final String[] VAULT_SUBFOLDERS = {"html", "assets", "media", "fonts", "json", "api"};

    // 👑 تحويل السقف إلى متغير ديناميكي يلتهم المساحة المتاحة بذكاء
    private static long MAX_DISK_CACHE;

    // ترقية الـ RAM لخدمة المستودع الضخم
    private static final int RAM_LIMIT = 64 * 1024 * 1024; // 64MB لسرعة العرض
    private static final int RAM_THRESHOLD = 5 * 1024 * 1024;

    // [حقن في بداية RoyalCacheManager]
    private static final long BLIND_TRUST_WINDOW = 100;

    private static final LruCache<String, byte[]> memoryCache =
            new LruCache<String, byte[]>(RAM_LIMIT) {
                @Override
                protected int sizeOf(String key, byte[] value) {
                    return value.length;
                }
            };

    private static final Set<String> writingNow =
            Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    private static long lastEviction = 0;

    private static final Set<String> EXT = new HashSet<>(Arrays.asList(
            ".png",".jpg",".jpeg",".webp",".avif",".gif",".ico",".svg",
            ".css",".js",".mjs",
            ".woff",".woff2",".ttf",".otf",
            ".mp4",".webm",".mp3",".wav",
            ".pdf",".doc",".docx"
    ));

    private static final Map<String, String> MIME = new HashMap<>();
    static {
        MIME.put(".webp","image/webp");
        MIME.put(".avif","image/avif");
        MIME.put(".woff2","font/woff2");
        MIME.put(".mjs","application/javascript");
        MIME.put(".svg","image/svg+xml");
        MIME.put(".html","text/html");
    }

    private RoyalCacheManager() {}

    // [تعديل جراحي 3: بناء المستودع في الذاكرة الصلبة]
    public static void init(Context context) {
        RoyalCacheManager.context = context.getApplicationContext();
        if (vaultDir != null) return;

        // 🚩 استخدام getFilesDir لضمان السيادة (النظام لا يمسح هذا المجلد أبداً)
        vaultDir = new File(context.getFilesDir(), "royal_vault_v1");
        if (!vaultDir.exists()) vaultDir.mkdirs();

        // إنشاء المجلدات الفرعية التخصصية
        for (String sub : VAULT_SUBFOLDERS) {
            File folder = new File(vaultDir, sub);
            if (!folder.exists()) folder.mkdirs();
        }

        // تحديد سقف الـ 4 جيجابايت أو المساحة المتاحة
        long usableSpace = vaultDir.getUsableSpace();
        MAX_DISK_CACHE = Math.min(4096L * 1024 * 1024, (long)(usableSpace * 0.40));
                         
        Log.i(TAG, "🏗️ Royal Vault Active: 4GB Capacity Primed at FilesDir.");
    }

    // ==========================================
    // 🔥 INTERCEPT (L1 → L2)
    // ==========================================

    public static WebResourceResponse intercept(WebResourceRequest request) {

        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        boolean success = true;

        try {
            if (vaultDir == null) return null;

            String url = request.getUrl().toString();
            if (!"GET".equalsIgnoreCase(request.getMethod())) return null;

            // 👑 حماية النخبة: منع تخزين أي طلب API خفي يتنكر كرابط عادي
            Map<String, String> requestHeaders = request.getRequestHeaders();
            if (requestHeaders != null) {
                String accept = requestHeaders.get("Accept");
                if (accept != null && (accept.contains("application/json") || accept.contains("text/event-stream"))) {
                    return null; // دعه يمر للإنترنت لأنه بيانات ديناميكية
                }
            }

            if (!isCacheable(url)) return null;

            maybeEvict();

            String key = generateAtomicKey(url);
            String subFolder = getVaultFolder(url);
            File file = new File(new File(vaultDir, subFolder), key);

            // 🛡️ حماية الأوفلاين الصارمة: إذا انقطع الإنترنت
            if (!NetworkMonitor.isInternetAvailable(context)) {
                if (file.exists()) {
                    return new WebResourceResponse(getMime(url), null, new BufferedInputStream(new FileInputStream(file)));
                }
                // ⚓ إذا كان الملف المطلوب صفحة HTML وغير موجودة كاش، ارجع أحدث صفحة رئيسية مخزنة لمنع الشاشة البيضاء
                if ("html".equals(subFolder) || getMime(url).equals("text/html")) {
                    InputStream anchorStream = getOfflineHtmlFallback();
                    if (anchorStream != null) {
                        Log.i(TAG, "⚓ Served Offline HTML Anchor for: " + url);
                        return new WebResourceResponse("text/html", "UTF-8", anchorStream);
                    }
                }
            }

            // ⚡ L1 RAM
            byte[] mem = memoryCache.get(key);
            if (mem != null) {
                return new WebResourceResponse(getMime(url), null,
                        new ByteArrayInputStream(mem));
            }

            // 💾 L2 Disk
            if (!file.exists()) return null;

            CacheMeta meta = loadMeta(subFolder, key);

            if (meta == null) {
                file.delete();
                return null;
            }

            // 👑 تطبيق معمارية Stale-While-Revalidate (العرض الفوري والتحديث بالخلفية)
            String strategy = "DEFAULT"; 
            if (url.endsWith(".js") || url.endsWith(".css")) {
                strategy = "BINARY_TRUST_CACHE"; 
            }

            long now = System.currentTimeMillis();

            if (meta != null) {
                if ("BINARY_TRUST_CACHE".equals(strategy)) {
                    if (now - meta.created > 7L * 24 * 60 * 60 * 1000) {
                        RoyalNetworkEngine.revalidateInBackground(url, getValidationHeaders(url));
                    }
                    Log.d(TAG, "🛡️ Stubborn Cache Access: " + url);
                } else if (now > meta.expiry) {
                    RoyalNetworkEngine.revalidateInBackground(url, getValidationHeaders(url));
                }
            }

            try {
                // 🔥 SMALL → RAM
                if (file.length() < RAM_THRESHOLD) {

                    FileInputStream fis = new FileInputStream(file);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[8192];
                    int r;

                    while ((r = fis.read(buffer)) != -1) {
                        bos.write(buffer, 0, r);
                    }

                    fis.close();

                    byte[] data = bos.toByteArray();
                    memoryCache.put(key, data);

                    file.setLastModified(System.currentTimeMillis());

                    return new WebResourceResponse(getMime(url), null,
                            new ByteArrayInputStream(data));
                }

                // 🔥 LARGE → STREAM (بدون RAM)
                return new WebResourceResponse(
                        getMime(url),
                        null,
                        new BufferedInputStream(new FileInputStream(file))
                );

            } catch (Exception e) {
                return null;
            }

        } catch (Exception e) {
            success = false;
            return null;
        } finally {
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = Math.max(0, memoryAfter - memoryBefore);

            RoyalPanopticon.recordExecution(
                    "RoyalCacheManager",
                    latency,
                    success,
                    memoryUsed
            );
        }
    }

    // ==========================================
    // 💾 STORE
    // ==========================================

    public static void store(String url, InputStream inputStream, Map<String, List<String>> headers) {

        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        boolean success = true;

        try {
            if (vaultDir == null) return;
            if (!isCacheable(url)) return;

            maybeEvict();

            String key = generateAtomicKey(url);
            String subFolder = getVaultFolder(url);

            // 🔒 atomic lock
            if (!writingNow.add(key)) return;

            try {
                CacheMeta meta = parseHeaders(url, headers);
                if (meta == null) return;

                // [تعديل جراحي 6: داخل دالة store]
                // 🚩 التمرد على الرؤوس: إذا كان الملف (JS, CSS, HTML, JSON) 
                // سنقوم بتخزينه حتى لو قال الخادم no-store
                File finalFile = new File(new File(vaultDir, subFolder), key);

                // 👑 فك قفل التحديث: منع تكرار الكتابة للملفات الثابتة فقط، والسماح بتحديث صفحات HTML و root_anchor باستمرار
                boolean isHtmlResource = "html".equals(subFolder) || "text/html".equals(getMime(url));
                if (!isHtmlResource && finalFile.exists() && finalFile.length() > 0) return;

                // 🛡️ الكتابة في ملف مؤقت أولاً (Atomic Write)
                File tmpFile = new File(new File(vaultDir, subFolder), key + ".tmp");
                FileOutputStream fos = new FileOutputStream(tmpFile);

                BufferedInputStream bis = new BufferedInputStream(inputStream);

                byte[] memBuffer = null;
                byte[] buffer = new byte[16384];
                int total = 0;
                int read;

                while ((read = bis.read(buffer)) != -1) {

                    fos.write(buffer, 0, read);
                    total += read;

                    if (total <= RAM_THRESHOLD) {
                        if (memBuffer == null) {
                            memBuffer = new byte[RAM_THRESHOLD];
                        }
                        System.arraycopy(buffer, 0, memBuffer, total - read, read);
                    }
                }

                fos.flush();
                fos.close();
                bis.close();

                // 🛡️ إنهاء عملية الكتابة الذرية بأمان
                if (tmpFile.length() == 0) {
                    tmpFile.delete();
                    return;
                } else {
                    // استبدال الملف القديم بالجديد في جزء من الثانية
                    tmpFile.renameTo(finalFile); 
                }

                // ⚡ RAM promotion
                if (memBuffer != null) {
                    byte[] exact = Arrays.copyOf(memBuffer, total);
                    memoryCache.put(key, exact);
                }

                saveMeta(subFolder, key, meta);

                // ⚓ تحديث مرساة الصفحات الأوفلاين لضمان وجود نسخة احتياطية رئيسية دائماً
                if ("html".equals(subFolder)) {
                    saveOfflineHtmlAnchor(finalFile);
                }

                // 🔥 runtime eviction (خفيف)
                if (new Random().nextInt(20) == 0) {
                    performLRUEviction();
                }

            } catch (Exception ignored) {
            } finally {
                writingNow.remove(key);
            }

        } catch (Exception e) {
            success = false;
        } finally {
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = Math.max(0, memoryAfter - memoryBefore);

            RoyalPanopticon.recordExecution(
                    "RoyalCacheManager",
                    latency,
                    success,
                    memoryUsed
            );
        }
    }

    public static Map<String, String> getValidationHeaders(String url) {
        String key = generateAtomicKey(url);
        String subFolder = getVaultFolder(url);
        CacheMeta meta = loadMeta(subFolder, key);

        if (meta == null) return null;

        Map<String, String> headers = new HashMap<>();

        if (meta.etag != null)
            headers.put("If-None-Match", meta.etag);

        if (meta.lastModified != null)
            headers.put("If-Modified-Since", meta.lastModified);

        return headers;
    }

    // ==========================================
    // 🧠 RULES
    // ==========================================

    // 2. تعديل منطق الـ isCacheable ليكون "شرهًا" في التخزين
    private static boolean isCacheable(String url) {
        String clean = url.split("\\?")[0].toLowerCase();
        // استثناءات أمنية فقط (لا نخزن العمليات المالية)
        if (clean.contains("/checkout") || clean.contains("/payment") || clean.contains("/auth")) return false;
        
        // 👑 القاعدة الذهبية: خزن كل شيء آخر! 
        // المتاجر تحتاج لصور عالية الدقة وخطوط وجافا سكريبت ضخم، سنلتهمها جميعاً.
        return true; 
    }

    // [تعديل جراحي 4: محرك تصنيف الموارد]
    private static String getVaultFolder(String url) {
        String u = url.toLowerCase();
        if (u.contains(".js") || u.contains(".mjs")) return "assets";
        if (u.contains(".css")) return "assets";
        if (u.contains(".json")) return "json";
        if (u.contains("/api/")) return "api";
        if (u.contains(".woff") || u.contains(".ttf") || u.contains(".otf")) return "fonts";
        if (u.contains(".png") || u.contains(".jpg") || u.contains(".webp") || u.contains(".avif") || u.contains(".gif")) return "media";
        return "html";
    }

    private static long resolveTTL(String url) {

        String u = url.toLowerCase(Locale.US);

        if (u.endsWith(".js"))
            return 6L * 60 * 60 * 1000;

        if (u.endsWith(".css"))
            return 6L * 60 * 60 * 1000;

        if (u.endsWith(".woff"))
            return 30L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".woff2"))
            return 30L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".ttf"))
            return 30L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".otf"))
            return 30L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".png"))
            return 7L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".jpg"))
            return 7L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".jpeg"))
            return 7L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".webp"))
            return 7L * 24 * 60 * 60 * 1000;

        if (u.endsWith(".avif"))
            return 7L * 24 * 60 * 60 * 1000;

        // 👑 إذا كان الرابط هو صفحة HTML للمتجر، نعطيه TTL قصير جداً (5 دقائق)
        // هذا يضمن أن يفتح المتجر فوراً، ولكنه سيجبر المحرك على جلب النسخة الأحدث إذا تغيرت الأسعار.
        if (u.endsWith(".html") || !u.matches(".*\\.[a-z0-9]{2,5}$")) {
            return 5L * 60 * 1000; // 5 دقائق فقط
        }

        return 60L * 60 * 1000; // ساعة لباقي الملفات المجهولة
    }

    // ==========================================
    // 🧹 EVICTION
    // ==========================================

    private static void maybeEvict() {
        long now = System.currentTimeMillis();

        if (now - lastEviction > 5 * 60 * 1000) { // كل 5 دقائق
            lastEviction = now;
            performLRUEviction();
        }
    }

    private static void performLRUEviction() {
        long startTime = System.nanoTime();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        boolean success = true;

        try {
            if (vaultDir == null) return;

            List<File> allFiles = new ArrayList<>();
            long totalSize = 0;

            // مسح جميع المجلدات الفرعية الستة واستخراج الملفات الحقيقية
            for (String sub : VAULT_SUBFOLDERS) {
                File subDir = new File(vaultDir, sub);
                File[] files = subDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && !f.getName().endsWith(".meta") && !f.getName().endsWith(".tmp")) {
                            allFiles.add(f);
                            totalSize += f.length();
                        }
                    }
                }
            }

            // إذا لم يتجاوز السقف المحدد، لا داعي للحذف
            if (totalSize < MAX_DISK_CACHE) return;

            // ترتيب الملفات من الأقدم إلى الأحدث حسب تاريخ آخر تعديل
            Collections.sort(allFiles, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            });

            // حذف الملفات الأقدم حتى ينخفض الحجم إلى 80% من السقف
            long targetSize = (long) (MAX_DISK_CACHE * 0.80);
            for (File f : allFiles) {
                long fileSize = f.length();
                File metaFile = new File(f.getAbsolutePath() + ".meta");

                if (metaFile.exists()) metaFile.delete();
                if (f.delete()) {
                    totalSize -= fileSize;
                }

                if (totalSize <= targetSize) break;
            }

            Log.i(TAG, "🧹 Deep LRU Eviction Completed. New Total Size: " + (totalSize / (1024 * 1024)) + " MB");

        } catch (Exception e) {
            success = false;
            Log.e(TAG, "Error during deep LRU eviction", e);
        } finally {
            long latency = (System.nanoTime() - startTime) / 1_000_000;
            long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            long memoryUsed = Math.max(0, memoryAfter - memoryBefore);

            RoyalPanopticon.recordExecution(
                    "RoyalCacheManager",
                    latency,
                    success,
                    memoryUsed
            );
        }
    }

    // ==========================================
    // 🔧 UTILS
    // ==========================================

    // [تعديل جراحي 2: خوارزمية FNV-1a 64-bit الصارمة]
    private static String generateAtomicKey(String input) {
        if (input == null) return "0";
        String normalized = input.trim();
        
        // 👑 إزالة الشرطة المائلة الأخيرة لضمان أن https://site.com و https://site.com/ يولدان نفس المفتاح تماماً
        if (normalized.endsWith("/") && normalized.length() > 8) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        long hash = 0xcbf29ce484222325L; // FNV_offset_basis 64-bit
        for (int i = 0; i < normalized.length(); i++) {
            hash ^= normalized.charAt(i);
            hash *= 0x100000001b3L; // FNV_prime 64-bit
        }
        return Long.toHexString(hash);
    }

    private static String getMime(String url) {
        String clean = url.toLowerCase().split("\\?")[0];

        for (Map.Entry<String, String> e : MIME.entrySet()) {
            if (clean.endsWith(e.getKey())) return e.getValue();
        }

        String sys = URLConnection.guessContentTypeFromName(clean);
        if (sys != null) return sys;

        // 👑 تصحيح جراحي: أي رابط لا ينتهي بـ extension نقطي (مثل/ أو المسارات) يتم اعتباره text/html فوراً
        String lastSegment = clean.contains("/") ? clean.substring(clean.lastIndexOf('/') + 1) : clean;
        if (!lastSegment.contains(".") || lastSegment.isEmpty()) {
            return "text/html";
        }

        return "application/octet-stream";
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();

        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }

    // [تعديل جراحي 1: دوال meta مع subfolder]
    private static File metaFile(String subFolder, String key) {
        return new File(new File(vaultDir, subFolder), key + ".meta");
    }

    private static void saveMeta(String subFolder, String key, CacheMeta meta) {
        try (FileOutputStream fos = new FileOutputStream(metaFile(subFolder, key))) {
            Properties p = new Properties();
            p.put("expiry", String.valueOf(meta.expiry));
            if (meta.etag != null) p.put("etag", meta.etag);
            if (meta.lastModified != null) p.put("lm", meta.lastModified);
            p.put("created", String.valueOf(System.currentTimeMillis()));
            p.store(fos, null);
        } catch (Exception ignored) {}
    }

    private static CacheMeta loadMeta(String subFolder, String key) {
        File f = metaFile(subFolder, key);
        if (!f.exists()) return null;

        try (FileInputStream fis = new FileInputStream(f)) {
            Properties p = new Properties();
            p.load(fis);

            CacheMeta m = new CacheMeta();
            m.expiry = Long.parseLong(p.getProperty("expiry", "0"));
            m.etag = p.getProperty("etag");
            m.lastModified = p.getProperty("lm");
            m.created = Long.parseLong(p.getProperty("created", "0"));
            return m;

        } catch (Exception e) {
            return null;
        }
    }

    // 👑 تحديث وقت انتهاء الصلاحية فقط عند استلام 304 Not Modified
    public static void updateValidationMeta(String url, Map<String, List<String>> newHeaders) {
        String key = generateAtomicKey(url);
        String subFolder = getVaultFolder(url);
        CacheMeta oldMeta = loadMeta(subFolder, key);
        
        if (oldMeta != null) {
            CacheMeta updatedMeta = parseHeaders(url, newHeaders);
            if (updatedMeta != null) {
                // دمج البيانات الجديدة مع القديمة
                oldMeta.expiry = updatedMeta.expiry;
                if (updatedMeta.etag != null) oldMeta.etag = updatedMeta.etag;
                if (updatedMeta.lastModified != null) oldMeta.lastModified = updatedMeta.lastModified;
                
                saveMeta(subFolder, key, oldMeta);
            }
        }
    }

    // [تعديل جراحي 7: داخل دالة parseHeaders]
    private static CacheMeta parseHeaders(
            String url,
            Map<String, List<String>> headers) {

        CacheMeta meta = new CacheMeta();
        long now = System.currentTimeMillis();

        // 🔥 القبضة الحديدية: التجاهل التام لأوامر الخوادم التي تمنع الكاش
        List<String> cc = headers.get("Cache-Control");
        if (cc != null) {
            String val = cc.get(0);
            String lower = val.toLowerCase(Locale.US);

            // استخلاص الـ max-age إن وجد، وتجاهل أوامر no-cache و no-store تماماً
            if (lower.contains("max-age")) {
                try {
                    String s = lower.split("max-age=")[1].split(",")[0];
                    long seconds = Long.parseLong(s);
                    meta.expiry = now + (seconds * 1000);
                } catch (Exception ignored) {}
            }
        }

        // 👑 إذا لم يرسل الخادم وقت انتهاء، أو أرسل وقتاً منتهياً (لإجبارنا على التحديث)، 
        // سنرفض ذلك ونفرض المدة الزمنية الخاصة بمحركنا بالقوة!
        if (meta.expiry == 0 || meta.expiry <= now) {
            meta.expiry = now + resolveTTL(url);
        }

        // 🔥 ETag
        List<String> et = headers.get("ETag");
        if (et != null) meta.etag = et.get(0);

        // 🔥 Last-Modified
        List<String> lm = headers.get("Last-Modified");
        if (lm != null) meta.lastModified = lm.get(0);

        return meta;
    }

    // ==========================================
    // 📥 DOWNLOAD MANAGER DIRECTORY
    // ==========================================

    /**
     * 🔥 يتولى معالجة تحميل الملفات الكبيرة لتخفيف الضغط عن المحرك الأساسي
     */
    public static void downloadLargeFile(Context context, String url, String userAgent, String contentDisposition, String mimeType) {
        try {
            android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(android.net.Uri.parse(url));
            request.setMimeType(mimeType);
            
            // حقن الكوكيز لضمان صلاحية التحميل من المواقع التي تتطلب تسجيل دخول
            String cookies = android.webkit.CookieManager.getInstance().getCookie(url);
            if (cookies != null) {
                request.addRequestHeader("cookie", cookies);
            }
            
            request.addRequestHeader("User-Agent", userAgent);
            request.setDescription("Downloading file...");
            
            String fileName = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType);
            request.setTitle(fileName);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, fileName);
            
            android.app.DownloadManager dm = (android.app.DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            if (dm != null) dm.enqueue(request);
            
        } catch (Exception e) {
            Log.e(TAG, "Royal Download Manager failed", e);
        }
    }

    // ==========================================
    // ⚓ OFFLINE ANCHOR HELPERS
    // ==========================================

    private static void saveOfflineHtmlAnchor(File htmlFile) {
        try {
            File htmlDir = new File(vaultDir, "html");
            File anchorFile = new File(htmlDir, "root_anchor.html");
            
            // نسخ أحدث صفحة HTML بنجاح لتكون المرساة الرئيسية
            FileInputStream fis = new FileInputStream(htmlFile);
            FileOutputStream fos = new FileOutputStream(anchorFile);
            byte[] buf = new byte[8192];
            int len;
            while ((len = fis.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (Exception ignored) {}
    }

    private static InputStream getOfflineHtmlFallback() {
        try {
            File htmlDir = new File(vaultDir, "html");
            File anchorFile = new File(htmlDir, "root_anchor.html");

            if (anchorFile.exists() && anchorFile.length() > 0) {
                return new BufferedInputStream(new FileInputStream(anchorFile));
            }

            // 👑 ترتيب الملفات تنازلياً حسب أحدث تاريخ تعديل واختيار أحدث صفحة HTML حقيقية
            File[] files = htmlDir.listFiles();
            if (files != null && files.length > 0) {
                List<File> htmlFiles = new ArrayList<>();
                for (File f : files) {
                    if (f.isFile() && !f.getName().endsWith(".meta") && !f.getName().endsWith(".tmp") && f.length() > 0) {
                        htmlFiles.add(f);
                    }
                }
                if (!htmlFiles.isEmpty()) {
                    Collections.sort(htmlFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    return new BufferedInputStream(new FileInputStream(htmlFiles.get(0)));
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
    }
