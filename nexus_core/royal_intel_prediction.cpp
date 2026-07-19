#include <emscripten/emscripten.h>
#include <emscripten/bind.h>
#include <cmath>
#include <string>

using namespace emscripten;

class RoyalIntelPrediction {
private:
    float last_touch_x = 0;
    float last_touch_y = 0;
    long long last_touch_time = 0;
    bool is_preloading_active = false;

public:
    RoyalIntelPrediction() {}

    /**
     * 👆 تحليل نية النقر (Pointer Intent Analysis)
     * تحسب ما إذا كان المستخدم يضغط "ليفتح" أم يضغط "ليمرر"
     */
    bool analyze_pointer_intent(float x, float y, long long timestamp) {
        float dx = std::abs(x - last_touch_x);
        float dy = std::abs(y - last_touch_y);
        long long dt = timestamp - last_touch_time;

        // إذا كانت الحركة أقل من 5 بكسل واستغرقت أكثر من 100ms
        // هذا يعني أن المستخدم "يستعد" للنقر.. نطلق شرارة التحميل المسبق فوراً!
        if (dx < 5.0f && dy < 5.0f && dt > 100) {
            return true; 
        }

        last_touch_x = x;
        last_touch_y = y;
        last_touch_time = timestamp;
        return false;
    }

    /**
     * 🔮 محرك حقن قواعد التكهن (Speculation Rules Injector)
     * يولد كود JS فائق القوة ليخبر الكروميوم بالصفحات التي يجب رسمها مسبقاً
     */
    // [تعديل جراحي: ترقية النواة للرندرة المسبقة الشاملة]
    void inject_speculation_atomic(const std::string& url) {
        EM_ASM_({
            const targetUrl = UTF8ToString($0);
            // 🚀 تقنية Speculation Rules API: تفعيل الرندرة الكاملة (DOM + JS + Paint) في الخلفية
            const specScript = document.createElement('script');
            specScript.type = 'speculationrules';
            specScript.textContent = JSON.stringify({
                "prerender": [{
                    "source": "list",
                    "urls": [targetUrl],
                    "score": 1.0,
                    "eagerness": "immediate" // تنفيذ فوري بأمر من النواة
                }]
            });
            document.head.appendChild(specScript);
            console.log("🔮 NUCLEUS: Full Prerender Sequence Initiated for: " + targetUrl);
        }, url.c_str());
    }
};

EMSCRIPTEN_BINDINGS(royal_intel_module) {
    class_<RoyalIntelPrediction>("RoyalIntelPrediction")
        .constructor()
        .function("analyze_pointer_intent", &RoyalIntelPrediction::analyze_pointer_intent)
        .function("inject_speculation_atomic", &RoyalIntelPrediction::inject_speculation_atomic);
    }
