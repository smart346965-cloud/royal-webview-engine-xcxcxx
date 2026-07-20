/* 👑 ROYAL NUCLEUS ELITE LOADER (Streaming Edition) */
(function() {
    const WASM_URL = 'https://royal-engine.local/public/js/royal_nucleus.wasm';
    const JS_URL = 'https://royal-engine.local/public/js/royal_nucleus.js';

    async function ignite() {
        if (window.Nexus) return; // منع تكرار الحقن إذا كان المحرك حياً

        try {
            // 1. تحميل سكربت الـ Glue Code يدوياً لضمان النطاق العالمي
            const script = document.createElement('script');
            script.src = JS_URL;
            const scriptLoaded = new Promise(resolve => script.onload = resolve);
            document.head.appendChild(script);
            await scriptLoaded;

            // 2. تفعيل معمارية Streaming: الترجمة أثناء التدفق
            // الكروميوم سيبدأ في تحويل C++ إلى لغة آلة فور وصول أول بايت
            const response = fetch(WASM_URL);
            const module = await createRoyalNucleusModule({
                instantiateWasm: (imports, successCallback) => {
                    WebAssembly.instantiateStreaming(response, imports)
                        .then(result => successCallback(result.instance))
                        .catch(e => {
                            console.error("Falling back to ArrayBuffer...", e);
                            // Fallback في حال لم يدعم المتصفح Streaming (نادر جداً في كروميوم الحديث)
                            response.then(res => res.arrayBuffer()).then(bytes => 
                                WebAssembly.instantiate(bytes, imports).then(res => successCallback(res.instance))
                            );
                        });
                    return {}; // تخبر Emscripten أننا سنتولى المهمة
                }
            });

            // 👑 3. إيقاظ المايسترو (المركز القيادي الموحد لنواة C++)
            const maestro = new module.RoyalNucleus();

            // 4. ربط النطاق العالمي (Global Fusion)
            window.Nexus = {
                Maestro: maestro, // تسجيل المايسترو في النطاق العالمي
                Ignition: new module.RoyalIgnitionCore(),
                Core: new module.RoyalCoreEngine(),
                Network: new module.RoyalNetworkCore()
            };

            // 5. تشغيل التسخين الفوري
            window.Nexus.Ignition.perform_socket_priming(window.location.origin);

            // 👑 6. بناء الجسر الدماغي (RoyalWasm) لكي تتعرف عليه المستشعرات
            window.RoyalWasm = {
                core: window.Nexus.Core,
                
                // الاستدعاء العبقري: نأخذ المحركات من المايسترو مباشرة عبر دوال الجلب 
                // بدلاً من إنشاء نسخ جديدة تشتت الذاكرة!
                intel: maestro.getPredictor(),  
                guardian: maestro.getGuardian() 
            };

            // 👑 2. إيقاظ المستشعرات بعد ضمان اكتمال عقل الـ C++ بنسبة 100%
            if (window.RoyalInteraction && typeof window.RoyalInteraction.init === 'function') {
                window.RoyalInteraction.init();
            }
            
            // 👑 3. إيقاظ التنبؤ بذكاء لحماية المعالج (في وقت الفراغ)
            if (window.RoyalSpeculator && typeof window.RoyalSpeculator.init === 'function') {
                const requestIdle = window.requestIdleCallback || function (cb) { setTimeout(cb, 100); };
                requestIdle(() => window.RoyalSpeculator.init());
            }

            // 👑 7. إطلاق البروتوكولات السيادية
            // تفعيل التوربو الشبكي فوراً
            window.RoyalWasm.guardian.activate_network_turbo();

            // تفعيل التنبؤ العكسي (الرجوع اللحظي)
            // نأخذ رابط الصفحة السابقة (document.referrer) ونرسله للـ C++ ليرسمه مسبقاً
            if (document.referrer && document.referrer.includes(window.location.origin)) {
                window.RoyalWasm.intel.predict_back_step(document.referrer);
            }

            // 👑 8. تأمين الـ BFCache قبل الانتقال لأي صفحة
            window.addEventListener('click', (e) => {
                const link = e.target.closest('a');
                if (link) window.RoyalWasm.intel.lock_current_dom_state();
            });

            console.log("🚀 NUCLEUS ACTIVE: Zero-Latency Fusion Complete.");
            console.log("🚀 NUCLEUS READY: Network Turbo & Back-Step Oracle Online.");

        } catch (err) {
            console.warn("Nucleus Ignition partial fail, retrying...", err);
        }
    }

    // التنفيذ الفوري
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', ignite);
    } else {
        ignite();
    }
})();
