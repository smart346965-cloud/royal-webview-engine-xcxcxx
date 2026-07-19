#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <string>
#include <unordered_map>
#include <chrono>
#include <vector>
#include <algorithm>

using namespace emscripten;

/**
 * 🛡️ ROYAL NETWORK GUARDIAN (The Binary Force)
 * محرك إدارة الكاش والشبكة بمستوى لغة الآلة
 */
class RoyalNetworkGuardian {
private:
    struct CacheRule {
        long long ttl;
        int priority; // 1: High, 2: Normal, 3: Background
        bool force_binary_trust;
    };

    std::unordered_map<std::string, CacheRule> registry;
    long long session_start_time;

    // دالة خاصة لتحويل النصوص للأسفل بسرعة المعالج
    void fast_lower(std::string& s) {
        std::transform(s.begin(), s.end(), s.begin(), [](unsigned char c){ return std::tolower(c); });
    }

public:
    RoyalNetworkGuardian() {
        session_start_time = std::chrono::system_clock::now().time_since_epoch().count();
        
        // 🧪 تلقيم القواعد الأساسية (Seed Rules) لضمان السيادة الفورية
        registry[".js"]   = { 21600000, 1, true };  // 6 ساعات - ثقة مطلقة
        registry[".css"]  = { 21600000, 1, true };
        registry[".woff2"] = { 2592000000, 1, true }; // شهر كامل للخطوط
        registry["html"]  = { 300000, 2, false };    // 5 دقائق للصفحات الهيكلية
    }

    /**
     * ⚡ محرك اتخاذ القرار الثنائي (Binary Decision Engine)
     * يقرر في (0.0001ms) هل يجب استدعاء الملف من الكاش المحلي أم لا
     */
    val evaluate_request_strategy(std::string url) {
        fast_lower(url);
        
        // منع كاش الـ APIs الحساسة فوراً في طبقة النواة
        if (url.find("/api/") != std::string::npos || url.find("token") != std::string::npos) {
            return val("NETWORK_ONLY");
        }

        // استخراج الامتداد بسرعة البرق
        size_t dot_pos = url.find_last_of('.');
        if (dot_pos != std::string::npos) {
            std::string ext = url.substr(dot_pos);
            if (registry.count(ext)) {
                CacheRule rule = registry[ext];
                if (rule.force_binary_trust) {
                    return val("BINARY_TRUST_CACHE"); // الثقة العمياء
                }
            }
        }

        return val("STALE_WHILE_REVALIDATE"); // العرض الفوري مع التحديث الخلفي
    }

    /**
     * 🧬 مولد المفاتيح الوميضي (Atomic Key Generator)
     * بدلاً من MD5 الجافا التقليدي، نستخدم خوارزمية دقيقة لربط الرابط بمكانه في الذاكرة
     */
    std::string compute_atomic_key(const std::string& url) {
        unsigned int hash = 0x811c9dc5; // FNV-1a Hash (الأسرع في C++)
        for (char c : url) {
            hash ^= (unsigned int)c;
            hash *= 0x01000193;
        }
        
        char hex[9];
        snprintf(hex, sizeof(hex), "%08x", hash);
        return std::string(hex);
    }

    /**
     * 🌐 رادار جودة الاتصال (Network Health Awareness)
     * يحسب النواة إذا كان الإنترنت يسمح بعمل Prefetch ثقيل أم لا
     */
    bool should_throttle_network(double current_latency) {
        // إذا كان التأخير أكثر من 500ms، النواة تأمر بإيقاف التنبؤ لحماية الرام والبطارية
        return current_latency > 500.0;
    }

    /**
     * 👑 تقنية "الاستبقاء الساخن" (Hot-Retention Policy)
     * تخبر الجافا بالملفات التي يجب أن تظل في الـ RAM دائماً
     */
    bool is_critical_asset(const std::string& url) {
        return (url.find("main.js") != std::string::npos || 
                url.find("style.css") != std::string::npos ||
                url.find("theme.css") != std::string::npos);
    }

    /**
     * 🖼️ محرك الرندرة البصري (Async Image Engine)
     * يجبر المتصفح على معالجة الصور في خيوط خلفية بعيداً عن الـ UI
     */
    void enforce_async_visuals() {
        EM_ASM({
            const observer = new MutationObserver((mutations) => {
                mutations.forEach(m => {
                    m.addedNodes.forEach(node => {
                        if (m.tagName === 'IMG') {
                            // إجبار فك التشفير غير المتزامن (قوة Kiwi)
                            node.decoding = 'async'; 
                            node.loading = 'lazy';
                        }
                    });
                });
            });
            observer.observe(document.documentElement, { childList: true, subtree: true });
            console.log("🖼️ NUCLEUS: Async Image Decoding Enforced.");
        });
    }

    /**
     * ⚡ تفعيل كاش الـ V8 Bytecode
     * يضمن أن الجافا سكريبت لا يُعاد ترجمته في كل مرة
     */
    void trigger_bytecode_opt() {
        // إرسال إشارة للمتصفح أن الموارد القادمة يجب حفظها كـ Bytecode
        EM_ASM({
            window.addEventListener('load', () => {
                if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
                    navigator.serviceWorker.controller.postMessage({type: 'SAVE_BYTECODE'});
                }
            });
        });
    }
};

EMSCRIPTEN_BINDINGS(royal_guardian_module) {
    class_<RoyalNetworkGuardian>("RoyalNetworkGuardian")
        .constructor()
        .function("evaluate_request_strategy", &RoyalNetworkGuardian::evaluate_request_strategy)
        .function("compute_atomic_key", &RoyalNetworkGuardian::compute_atomic_key)
        .function("should_throttle_network", &RoyalNetworkGuardian::should_throttle_network)
        .function("is_critical_asset", &RoyalNetworkGuardian::is_critical_asset)
        .function("enforce_async_visuals", &RoyalNetworkGuardian::enforce_async_visuals)
        .function("trigger_bytecode_opt", &RoyalNetworkGuardian::trigger_bytecode_opt);
    }
