/**
 * 🧠 NEXUS NUCLEUS WORKER - ELITE EDITION
 * =========================================================
 * المضيف المستقل لنواة الـ C++ (Off-Main-Thread Architecture)
 */

// 1. استيراد شفرة الربط التي تم طبخها في GitHub Actions
importScripts('royal_nucleus.js');

let Maestro = null;
let Ignition = null;
let Core = null;
let Network = null;

/**
 * 🚀 مرحلة الانصهار (Fusion) داخل الـ Worker
 */
async function initNucleus() {
    try {
        const module = await createRoyalNucleusModule({
            print: (text) => console.log('🛰️ WORKER_WASM:', text),
            printErr: (text) => console.error('⚠️ WORKER_WASM_ERR:', text),
            locateFile: (path) => path // ملف الـ .wasm سيكون في نفس المجلد
        });

        // إيقاظ المايسترو داخل الخيط المنفصل
        Maestro = new module.RoyalNucleus();
        
        // ربط المحركات التخصصية
        window_proxy.Nexus = {
            Predictor: Maestro.getPredictor(),
            Guardian: Maestro.getGuardian(),
            Ignition: new module.RoyalIgnitionCore(),
            Core: new module.RoyalCoreEngine(),
            Network: new module.RoyalNetworkCore()
        };

        console.log("🏆 NUCLEUS WORKER: Maestro is alive in an independent thread.");
        
        // إخطار الخيط الرئيسي بالجاهزية
        self.postMessage({ type: 'NUCLEUS_READY' });

    } catch (e) {
        console.error("❌ WORKER_INIT_FAILED:", e);
    }
}

/**
 * 📥 قناة استقبال النبضات (Input Stream)
 * معالجة البيانات القادمة من الحساسات (Touch/Scroll)
 */
self.onmessage = function(e) {
    const data = e.data;

    if (!Maestro && data.type !== 'INIT') return;

    switch(data.type) {
        case 'INIT':
            initNucleus();
            break;

        case 'TOUCH_START':
            // تحليل النية اللحظية في خيط منفصل (0ms UI lag)
            const willClick = window_proxy.Nexus.Predictor.analyze_pointer_intent(
                data.x, data.y, data.timestamp
            );
            if (willClick) {
                // أمر التحميل الشبحي يخرج من هنا
                window_proxy.Nexus.Predictor.inject_speculation_atomic(data.url);
            }
            break;

        case 'SCROLL_DATA':
            // حساب سرعة السكرول وتفعيل التنبؤ للأسفل
            const isFast = window_proxy.Nexus.Core.analyze_scroll_velocity(
                data.y, data.lastY, data.delta
            );
            if (isFast) {
                self.postMessage({ type: 'THROTTLE_RENDER', state: true });
            }
            break;

        case 'NETWORK_CHECK':
            // قرار الكاش الصارم يخرج من الـ Worker
            const strategy = window_proxy.Nexus.Guardian.evaluate_request_strategy(data.url);
            self.postMessage({ type: 'STRATEGY_DECISION', url: data.url, strategy: strategy });
            break;
    }
};

// محاكاة بسيطة لبيئة window داخل الـ Worker لضمان عمل EM_ASM
const window_proxy = { location: { origin: '' } };

// تشغيل المحرك فوراً
initNucleus();
