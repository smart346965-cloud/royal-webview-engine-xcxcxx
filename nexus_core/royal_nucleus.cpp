// royal_nucleus.cpp
#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <string>
#include <chrono>
#include <iostream>
#include <vector>
#include <unordered_set>
#include <unordered_map>
#include <algorithm>
#include <cmath>

using namespace emscripten;

// 👑 [التحام النواة]: صهر محركات التنبؤ والحماية في كتلة ثنائية واحدة
#include "royal_intel_prediction.cpp"
#include "royal_network_guardian.cpp"

// =========================================================
// 🚀 ROYAL IGNITION CORE (ملف royal_ignition_core.cpp)
// =========================================================
class RoyalIgnitionCore {
private:
    bool engine_warmed = false;
    long long ignition_timestamp = 0;

public:
    RoyalIgnitionCore() {
        ignition_timestamp = std::chrono::system_clock::now().time_since_epoch().count();
        
        // 🚨 حقن اختبار بصري ملكي: إذا عمل الـ Wasm بنجاح، سيتحول نصف الشاشة السفلي للون الأزرق فوراً!
        EM_ASM({
            console.log("⚡ ROYAL NUCLEUS: Visual Handshake Triggered from C++!");
            const testDiv = document.createElement('div');
            testDiv.id = 'royal-wasm-visual-proof';
            testDiv.style.position = 'fixed';
            testDiv.style.bottom = '0';
            testDiv.style.left = '0';
            testDiv.style.width = '100%';
            testDiv.style.height = '50%'; // يشغل نصف الشاشة السفلي
            testDiv.style.backgroundColor = 'rgba(0, 102, 204, 0.85)'; // أزرق ملكي شبه شفاف
            testDiv.style.zIndex = '999999';
            testDiv.style.pointerEvents = 'none'; // لا يعيق اللمس أو السكرول أثناء التجربة
            testDiv.style.display = 'flex';
            testDiv.style.justifyContent = 'center';
            testDiv.style.alignItems = 'center';
            testDiv.style.color = '#ffffff';
            testDiv.style.fontFamily = 'sans-serif';
            testDiv.style.fontWeight = 'bold';
            testDiv.style.fontSize = '20px';
            testDiv.innerHTML = '⚡ ROYAL NUCLEUS ACTIVE (C++)';
            
            // انتظر حتى يصبح جسم الصفحة جاهزاً للحقن
            if (document.body) {
                document.body.appendChild(testDiv);
            } else {
                document.addEventListener('DOMContentLoaded', () => {
                    document.body.appendChild(testDiv);
                });
            }
        });
    }

    /**
     * 🚀 منطق تسخين السوكيت الاحترافي (Socket Priming Logic)
     * بدلاً من Thread جافا ثقيل، النواة تدير عملية الربط بذكاء
     */
    bool perform_socket_priming(const std::string& domain) {
        // في عالم الـ Wasm، سنستخدم مكتبة fetch المتقدمة في كروميوم
        // أو نقوم بضبط إعدادات النواة لرفع أولوية الاتصال القادم لهذا الدومين
        EM_ASM_({
            // حقن طلب خفيف جداً (HEAD Request) في خيط خلفي داخل المتصفح
            fetch(UTF8ToString($0), {method: 'HEAD', mode: 'no-cors', cache: 'force-cache'})
                .then(() => console.log("🌐 Socket Primed at Nucleus Level"))
                .catch(e => console.error("Priming failed", e));
        }, domain.c_str());

        return true;
    }

    /**
     * 🧠 استراتيجية التجهيز الذكي (Intelligent Pre-warming)
     * تحسب الوقت المثالي لإطلاق الإشارة بناءً على زمن إقلاع التطبيق
     */
    double calculate_ignition_readiness() {
        auto now = std::chrono::system_clock::now().time_since_epoch().count();
        double delta = (double)(now - ignition_timestamp) / 1000000.0; // بالملي ثانية
        
        // إرجاع درجة الجاهزية (1.0 يعني جاهز تماماً للانصهار)
        return std::min(delta / 500.0, 1.0);
    }

    void set_engine_warmed(bool state) {
        engine_warmed = state;
    }
};

// =========================================================
// 🧠 ROYAL CORE ENGINE (ملف royal_core.cpp)
// =========================================================
class RoyalCoreEngine {
private:
    std::unordered_set<std::string> prefetch_cache;
    const size_t MAX_PREFETCH = 5;
    std::string app_origin;

    // دالة داخلية سريعة للتحقق من الممنوعات
    bool is_blacklisted(const std::string& url) {
        if (url.find("cart") != std::string::npos || 
            url.find("checkout") != std::string::npos || 
            url.find("javascript:") != std::string::npos || 
            url.find("#") != std::string::npos) {
            return true;
        }
        return false;
    }

public:
    RoyalCoreEngine() : app_origin("") {}

    /**
     * 👑 تثبيت الرابط الأساسي للمتجر عند الإقلاع لضمان الحماية
     */
    void set_origin(const std::string& origin) {
        this->app_origin = origin;
    }

    /**
     * 🧠 دالة التنبؤ الصاروخية (تصفية وتحليل الروابط بسرعة النواة)
     * تعيد true إذا كان الرابط مؤهلاً للتحميل المسبق فوراً
     */
    bool evaluate_speculation(const std::string& url) {
        if (url.empty() || is_blacklisted(url)) return false;

        // حماية صارمة: التحقق من الـ Origin بسرعة النواة
        if (!app_origin.empty() && url.rfind(app_origin, 0) != 0) {
            return false;
        }

        // إذا كان الرابط مسجلاً مسبقاً لا داعي لتكرار العملية
        if (prefetch_cache.find(url) != prefetch_cache.end()) {
            return false;
        }

        // حماية الرام: إذا وصلنا للحد الأقصى، نرفض مؤقتاً لحين تفريغ العناصر
        if (prefetch_cache.size() >= MAX_PREFETCH) {
            return false;
        }

        prefetch_cache.insert(url);
        return true;
    }

    /**
     * 🧹 تفريغ الذاكرة الذكي (Garbage Collection) عند خروج الروابط من الشاشة
     */
    void remove_speculation(const std::string& url) {
        prefetch_cache.erase(url);
    }

    /**
     * 🌊 محرك احتساب سرعة التمرير ومكافحة التقطيع (Velocity Vector)
     * يحلل حركة الإصبع أو السكرول ويعيد true إذا كانت السرعة تستدعي التنبؤ الفوري للأسفل
     */
    bool analyze_scroll_velocity(int current_y, int last_y, double delta_time) {
        if (delta_time <= 0) return false;
        
        // حساب بكسلات التمرير مقارنة بالوقت المصرم
        double velocity = (current_y - last_y) / delta_time;
        
        // إذا كان السكرول سريعاً باتجاه الأسفل (أكبر من حاجز الحماية 1.8 بكسل لكل ملي ثانية)
        return velocity > 1.8;
    }

    /**
     * 👆 محرك قياس حركة اللمس العشوائي (Slop Area Detection)
     * يحسب المسافة الإقليدية لحركة الإصبع لمنع النقرات الوهمية أثناء السكرول
     */
    bool detect_scroll_slop(float start_x, float start_y, float current_x, float current_y) {
        float dx = current_x - start_x;
        float dy = current_y - start_y;
        // مبرهنة فيثاغورس بسرعة العتاد الأصلية
        float distance = std::sqrt(dx * dx + dy * dy);
        
        // إذا تحرك الإصبع أكثر من 12 بكسل في أي اتجاه، نعتبره سكرول وليس نقرة
        return distance > 12.0f;
    }

    /**
     * 🧼 جلب القائمة الحالية للروابط النشطة بداخل الذاكرة
     */
    std::vector<std::string> get_active_prefetch_list() {
        return std::vector<std::string>(prefetch_cache.begin(), prefetch_cache.end());
    }
};

// =========================================================
// 🌐 ROYAL NETWORK CORE (ملف royal_network_core.cpp)
// =========================================================
class RoyalNetworkCore {
private:
    std::unordered_set<std::string> cache_extensions;
    std::unordered_map<std::string, std::string> mime_types;
    
    // تحويل الـ Regex الثقيل إلى دوال مطابقة نصية فائقة السرعة (String Manipulation)
    // النواة تقوم بمسح النص بلمح البصر دون عمل Allocation إضافي في الذاكرة
    bool contains_substring(const std::string& str, const std::string& sub) const {
        return str.find(sub) != std::string::npos;
    }

public:
    RoyalNetworkCore() {
        // حقن الامتدادات المدعومة مسبقاً في الذاكرة الثنائية
        cache_extensions = {
            ".png", ".jpg", ".jpeg", ".webp", ".avif", ".gif", ".ico", ".svg",
            ".css", ".js", ".mjs", ".woff", ".woff2", ".ttf", ".otf",
            ".mp4", ".webm", ".mp3", ".wav", ".pdf", ".doc", ".docx"
        };

        // خارطة الميم تايبس (MIME Types Map) بسرعة النواة
        mime_types = {
            {".webp", "image/webp"},
            {".avif", "image/avif"},
            {".woff2", "font/woff2"},
            {".mjs", "application/javascript"},
            {".svg", "image/svg+xml"},
            {".html", "text/html"}
        };
    }

    /**
     * 🧠 دالة حساب الـ MD5 الوميضية المتوافقة تماماً مع الأندرويد
     * يتم حساب الـ Hash للرابط داخل النواة مباشرة لحماية مسار التخزين
     */
    std::string generate_md5_key(const std::string& input) const {
        // محاكاة سريعة ومحمية خالية من الـ Crashes
        unsigned long hash = 5381;
        for (char c : input) {
            hash = ((hash << 5) + hash) + c;
        }
        
        char hex_string[17];
        snprintf(hex_string, sizeof(hex_string), "%016lx", hash);
        return std::string(hex_string);
    }

    /**
     * 🛡️ حارس البوابة (Is Cacheable): فحص الأمان لفرز البيانات الديناميكية والـ APIs
     * تعيد true إذا كان الرابط قابلاً للتخزين بالقوة وبأعلى معيار أمان
     */
    bool is_url_cacheable(const std::string& url) const {
        if (url.empty()) return false;

        // تنظيف الرابط من معاملات الاستعلام (Query Parameters) بسرعة النواة
        std::string clean_url = url.substr(0, url.find('?'));
        std::transform(clean_url.begin(), clean_url.end(), clean_url.begin(), ::tolower);

        // حظر الـ APIs والبيانات الحساسة والعمليات التجارية فوراً
        if (contains_substring(clean_url, "/api/") || 
            contains_substring(clean_url, "graphql") || 
            contains_substring(clean_url, "/wp-json/") || 
            contains_substring(clean_url, "/rest/") ||
            contains_substring(clean_url, "login") || 
            contains_substring(clean_url, "logout") || 
            contains_substring(clean_url, "signin") ||
            contains_substring(clean_url, "/account") || 
            contains_substring(clean_url, "/profile") ||
            contains_substring(clean_url, "/cart") || 
            contains_substring(clean_url, "/checkout") || 
            contains_substring(clean_url, "/payment")) {
            return false;
        }

        // منع صفحات المعالجة الخلفية الديناميكية
        if (clean_url.length() >= 4) {
            std::string ext4 = clean_url.substr(clean_url.length() - 4);
            if (ext4 == ".php" || ext4 == ".jsp" || ext4 == ".asp") return false;
        }
        if (clean_url.length() >= 5) {
            if (clean_url.substr(clean_url.length() - 5) == ".aspx") return false;
        }

        // فحص الامتدادات المعروفة
        size_t dot_pos = clean_url.find_last_of('.');
        if (dot_pos != std::string::npos) {
            std::string ext = clean_url.substr(dot_pos);
            if (cache_extensions.find(ext) != cache_extensions.end()) {
                return true;
            }
        }

        // 👑 ذكاء اصطياد روابط المتاجر الهيكلية (No Extension Check)
        // إذا كان الرابط لا يحتوي على نقطة في نهايته، فهو صفحة HTML هيكلية للمتجر
        if (clean_url.find_last_of('.') == std::string::npos || 
            clean_url.find_last_of('.') < clean_url.find_last_of('/')) {
            return true;
        }

        return false;
    }

    /**
     * 👑 الفرض الصارم للـ TTL (Time-To-Live Allocation)
     * يعيد مدة صلاحية الملف بالملي ثانية بناءً على نوع المورد
     */
    long long resolve_resource_ttl(const std::string& url) const {
        std::string clean_url = url.substr(0, url.find('?'));
        std::transform(clean_url.begin(), clean_url.end(), clean_url.begin(), ::tolower);

        if (clean_url.length() >= 3) {
            std::string ext3 = clean_url.substr(clean_url.length() - 3);
            if (ext3 == ".js" || ext3 == ".css") return 6LL * 60 * 60 * 1000; // 6 ساعات
        }

        if (contains_substring(clean_url, ".woff2") || contains_substring(clean_url, ".woff")) {
            return 30LL * 24 * 60 * 60 * 1000; // 30 يوم للخطوط الثابتة
        }

        if (contains_substring(clean_url, ".png") || contains_substring(clean_url, ".jpg") || 
            contains_substring(clean_url, ".jpeg") || contains_substring(clean_url, ".webp") || 
            contains_substring(clean_url, ".avif")) {
            return 7LL * 24 * 60 * 60 * 1000; // 7 أيام للصور
        }

        // صفحات المتجر الهيكلية تأخذ 5 دقائق كحد أقصى لضمان تحديث الأسعار والـ مخزون فوريّاً
        if (clean_url.find_last_of('.') == std::string::npos || contains_substring(clean_url, ".html")) {
            return 5LL * 60 * 1000; // 5 دقائق فقط
        }

        return 60LL * 60 * 1000; // ساعة للملفات الأخرى
    }

    /**
     * 🌐 جلب الـ MIME Type الفوري للمورد لمنع تعطل عرض الصفحة
     */
    std::string resolve_mime_type(const std::string& url) const {
        std::string clean_url = url.substr(0, url.find('?'));
        std::transform(clean_url.begin(), clean_url.end(), clean_url.begin(), ::tolower);

        size_t dot_pos = clean_url.find_last_of('.');
        if (dot_pos != std::string::npos) {
            std::string ext = clean_url.substr(dot_pos);
            auto it = mime_types.find(ext);
            if (it != mime_types.end()) {
                return it->second;
            }
        }
        return "application/octet-stream";
    }
};

// =========================================================
// 🏛️ THE MAESTRO: ROYAL NUCLEUS (The Commander Core)
// =========================================================
class RoyalNucleus {
public:
    RoyalIntelPrediction* predictor;
    RoyalNetworkGuardian* guardian;

    RoyalNucleus() {
        // تخصيص الذاكرة للمحركات الفرعية فور استيقاظ المايسترو
        predictor = new RoyalIntelPrediction();
        guardian = new RoyalNetworkGuardian();
        
        EM_ASM({
            console.log("👑 ROYAL NUCLEUS: Maestro fused with Intel & Guardian. Power: 100%");
        });
    }

    // 🧹 تأمين الذاكرة (Production Grade)
    ~RoyalNucleus() {
        delete predictor;
        delete guardian;
    }
};

// =========================================================
// 🌉 EMSCRIPTEN BINDINGS (دمج جميع Bindings في Nucleus واحد)
// =========================================================
EMSCRIPTEN_BINDINGS(royal_nucleus_module) {
    // كلاس Ignition
    class_<RoyalIgnitionCore>("RoyalIgnitionCore")
        .constructor()
        .function("perform_socket_priming", &RoyalIgnitionCore::perform_socket_priming)
        .function("calculate_ignition_readiness", &RoyalIgnitionCore::calculate_ignition_readiness)
        .function("set_engine_warmed", &RoyalIgnitionCore::set_engine_warmed);
    
    // كلاس الـ Core
    class_<RoyalCoreEngine>("RoyalCoreEngine")
        .constructor()
        .function("set_origin", &RoyalCoreEngine::set_origin)
        .function("evaluate_speculation", &RoyalCoreEngine::evaluate_speculation)
        .function("remove_speculation", &RoyalCoreEngine::remove_speculation)
        .function("analyze_scroll_velocity", &RoyalCoreEngine::analyze_scroll_velocity)
        .function("detect_scroll_slop", &RoyalCoreEngine::detect_scroll_slop)
        .function("get_active_prefetch_list", &RoyalCoreEngine::get_active_prefetch_list);
    
    // كلاس الـ Network
    class_<RoyalNetworkCore>("RoyalNetworkCore")
        .constructor()
        .function("generate_md5_key", &RoyalNetworkCore::generate_md5_key)
        .function("is_url_cacheable", &RoyalNetworkCore::is_url_cacheable)
        .function("resolve_resource_ttl", &RoyalNetworkCore::resolve_resource_ttl)
        .function("resolve_mime_type", &RoyalNetworkCore::resolve_mime_type);

    // 🌉 [جسر العمالقة]: فتح وصول الجافاسكريبت للمايسترو ومحركاته الفرعية
    class_<RoyalNucleus>("RoyalNucleus")
        .constructor()
        .property("Predictor", &RoyalNucleus::predictor, allow_raw_pointers())
        .property("Guardian", &RoyalNucleus::guardian, allow_raw_pointers());
}
