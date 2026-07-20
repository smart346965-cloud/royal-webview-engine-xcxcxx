/* 👑 ROYAL NUCLEUS ELITE LOADER (Streaming Edition + Telemetry Engine) */
(function() {
    // =========================================================================
    // 🔬 NEXUS TELEMETRY ENGINE: نظام الرادار التشخيصي الدقيق
    // =========================================================================
    window.NexusTelemetry = {
        metrics: { wasm_start: 0, wasm_end: 0, total_blocking_time: 0, bfcache_hit: false },
        longTasks: [],
        
        startMark: function(name) { performance.mark(name + '_start'); },
        endMark: function(name) { 
            performance.mark(name + '_end'); 
            try { performance.measure(name, name + '_start', name + '_end'); } catch(e){}
        },
        
        initObservers: function() {
            // مراقبة المهام الثقيلة التي تسبب التقطيع (Long Tasks > 50ms)
            if ('PerformanceObserver' in window) {
                try {
                    new PerformanceObserver((list) => {
                        list.getEntries().forEach(entry => {
                            window.NexusTelemetry.total_blocking_time += (entry.duration - 50);
                            window.NexusTelemetry.longTasks.push({ name: entry.name, duration: entry.duration.toFixed(2) });
                        });
                    }).observe({ type: 'longtask', buffered: true });
                } catch(e) {}
            }

            // مراقبة دقيقة لرجوع الصفحة (BFCache Monitor)
            window.addEventListener('pageshow', (event) => {
                window.NexusTelemetry.metrics.bfcache_hit = event.persisted;
                if (event.persisted) {
                    console.log("%c⚡ [NEXUS] BFCache HIT: تم الرجوع بـ 0ms (استرجاع لحظي من الذاكرة)", "color:#00ff00; font-weight:bold; background:#003300; padding:2px 5px;");
                } else {
                    const navType = performance.getEntriesByType("navigation")[0]?.type;
                    if (navType === 'back_forward') {
                        console.log("%c❌ [NEXUS] BFCache MISS: فشل الاسترجاع اللحظي! الموقع أعاد تحميل نفسه.", "color:#ff3333; font-weight:bold; background:#330000; padding:2px 5px;");
                    }
                }
            });
        },

        generateReport: function() {
            console.groupCollapsed("%c📊 NEXUS DIAGNOSTIC REPORT (اضغط لفتح التقرير الشامل)", "color: #00ffff; font-size: 14px; font-weight: bold; background: #111; padding: 6px; border-radius: 4px;");
            
            // 1. تقرير نواة الانصهار (WASM Core) - تم تعديل الخط للأسود الداكن
            const wasmMeasure = performance.getEntriesByName('WASM_IGNITION')[0];
            const wasmTime = wasmMeasure ? wasmMeasure.duration.toFixed(2) : 'N/A';
            console.log(`%c🧠 زمن بناء واستيقاث النواة (C++): %c${wasmTime} ms`, "color: #d97706; font-weight:bold;", "color: #000000; font-weight: bold; font-size: 12px;");
            
            // 2. تقرير سرعة الرسم والتنقل (Render & Navigation)
            const nav = performance.getEntriesByType("navigation")[0];
            const paint = performance.getEntriesByType("paint");
            const fcp = paint.find(p => p.name === 'first-contentful-paint');
            
            if (nav) {
                console.log(`%c🚀 نوع الدخول للصفحة: %c${nav.type.toUpperCase()}`, "color: #d97706; font-weight:bold;", "color: #000000; font-weight: bold;");
                console.log(`%c⏱️ زمن الاستجابة للهيكل (DOM Interactive): %c${nav.domInteractive.toFixed(2)} ms`, "color: #d97706; font-weight:bold;", "color: #000000; font-weight: bold;");
                console.log(`%c🎨 زمن اكتمال الموقع بالكامل (Load Complete): %c${nav.loadEventEnd.toFixed(2)} ms`, "color: #d97706; font-weight:bold;", "color: #000000; font-weight: bold;");
            }
            if (fcp) console.log(`%c👁️ أول بيكسلة ظهرت للشاشة (FCP): %c${fcp.startTime.toFixed(2)} ms`, "color: #d97706; font-weight:bold;", "color: #000000; font-weight: bold;");

            // 3. تقرير دعم تقنية الانصهار السريع (Speculation Rules)
            const specSupported = HTMLScriptElement.supports && HTMLScriptElement.supports('speculationrules');
            console.log(`%c🔮 تقنية التنبؤ والرندرة المسبقة: %c${specSupported ? 'تعمل بكفاءة 100%' : 'غير مدعومة في هذا الويبفيو!'}`, "color: #d97706; font-weight:bold;", specSupported ? "color: #059669; font-weight: bold;" : "color: #dc2626; font-weight: bold;");

            // 4. تحليل الاختناق (Bottleneck Diagnosis)
            console.log("%c🔍 --- التشخيص الآلي لسبب التأخير ---", "color: #0284c7; font-weight:bold;");
            
            if (this.longTasks.length > 0) {
                console.log(`%c⚠️ تم اكتشاف مهام ثقيلة جمدت الشاشة! (Total Blocking: ${this.metrics.total_blocking_time.toFixed(2)}ms)`, "color: #dc2626; font-weight:bold;");
                console.table(this.longTasks);
                console.log("%c💡 التشخيص: التأخير سببه سكربتات جافاسكريبت داخل الموقع الأصلي تعيق عمل المحرك.", "color: #dc2626; font-weight:bold;");
            } else if (wasmMeasure && wasmMeasure.duration > 300) {
                console.log("%c💡 التشخيص: تأخير بسبب بطء معالج الهاتف في فك تشفير ملف الـ WASM.", "color: #d97706; font-weight:bold;");
            } else if (nav && nav.domInteractive > 800) {
                console.log("%c💡 التشخيص: تأخير من الشبكة أو سيرفر الموقع الأصلي يرسل الـ HTML ببطء.", "color: #d97706; font-weight:bold;");
            } else {
                console.log("%c✅ التشخيص: لا يوجد أي بلوك! الأداء مثالي والنواة تعمل كالزبدة.", "color: #059669; font-weight:bold;");
            }
            
            console.groupEnd();
        }
    };

    // تشغيل الرادار
    window.NexusTelemetry.initObservers();
    // إتاحة أمر استخراج التقرير من الكونسول يدوياً
    window.NEXUS_REPORT = function() { window.NexusTelemetry.generateReport(); };

    // =========================================================================
    // 👑 ROYAL NUCLEUS IGNITION (كودك الأصلي مغلف بمجسات القياس)
    // =========================================================================
    const WASM_URL = 'https://royal-engine.local/public/js/royal_nucleus.wasm';
    const JS_URL = 'https://royal-engine.local/public/js/royal_nucleus.js';

    async function ignite() {
        if (window.Nexus) return; 

        try {
            window.NexusTelemetry.startMark('WASM_IGNITION'); // ⏱️ بدء قياس النواة

            const script = document.createElement('script');
            script.src = JS_URL;
            const scriptLoaded = new Promise(resolve => script.onload = resolve);
            document.head.appendChild(script);
            await scriptLoaded;

            const response = fetch(WASM_URL);
            const module = await createRoyalNucleusModule({
                instantiateWasm: (imports, successCallback) => {
                    WebAssembly.instantiateStreaming(response, imports)
                        .then(result => successCallback(result.instance))
                        .catch(e => {
                            console.error("Falling back to ArrayBuffer...", e);
                            response.then(res => res.arrayBuffer()).then(bytes => 
                                WebAssembly.instantiate(bytes, imports).then(res => successCallback(res.instance))
                            );
                        });
                    return {}; 
                }
            });

            const maestro = new module.RoyalNucleus();

            window.Nexus = {
                Maestro: maestro, 
                Ignition: new module.RoyalIgnitionCore(),
                Core: new module.RoyalCoreEngine(),
                Network: new module.RoyalNetworkCore()
            };

            window.Nexus.Ignition.perform_socket_priming(window.location.origin);

            window.RoyalWasm = {
                core: window.Nexus.Core,
                intel: maestro.getPredictor(),  
                guardian: maestro.getGuardian() 
            };

            if (window.RoyalInteraction && typeof window.RoyalInteraction.init === 'function') {
                window.RoyalInteraction.init();
            }
            
            if (window.RoyalSpeculator && typeof window.RoyalSpeculator.init === 'function') {
                const requestIdle = window.requestIdleCallback || function (cb) { setTimeout(cb, 100); };
                requestIdle(() => window.RoyalSpeculator.init());
            }

            window.RoyalWasm.guardian.activate_network_turbo();

            if (document.referrer && document.referrer.includes(window.location.origin)) {
                window.RoyalWasm.intel.predict_back_step(document.referrer);
            }

            window.addEventListener('click', (e) => {
                const link = e.target.closest('a');
                if (link) window.RoyalWasm.intel.lock_current_dom_state();
            });

            // [تعديل جراحي: مستشعر الاستقرار لفتح صنابير السكربتات]
            // =========================================================================
            // 🛡️ NEXUS STABILITY WATCHER: مراقب الخمول لفك خناق السكربتات
            // =========================================================================
            const triggerMaestroStabilization = () => {
                if (window.Nexus && window.Nexus.Maestro) {
                    // إرسال النبضة لنواة C++ لإعلان حالة الاستقرار المطلق
                    window.Nexus.Maestro.getGuardian().mark_stabilized();
                    console.log("%c🛡️ [NEXUS] SHIELD: Main Thread is now COLD. Scripts released to Idle Queue.", "color:#3b82f6; font-weight:bold; background:#e0f2fe; padding:2px 5px;");
                    
                    // إشعار الجافا عبر الجسر (اختياري إذا أردت فتح الفلترة في shouldInterceptRequest)
                    if (window.RoyalBridge && window.RoyalBridge.log) {
                        window.RoyalBridge.log("NUCLEUS_STABILIZED");
                    }
                }
            };

            // خوارزمية الانتظار الذكي: ننتظر خمول المتصفح (Idle) + أمان زمني
            if ('requestIdleCallback' in window) {
                // الخيار الاحترافي: ننتظر حتى يخبرنا المتصفح أنه "فاضي" تماماً
                requestIdleCallback(() => {
                    setTimeout(triggerMaestroStabilization, 1500); // نمهله 1.5 ثانية إضافية بعد أول خمول
                }, { timeout: 4000 }); // حد أقصى 4 ثوانٍ إذا ظل الموقع ثقيلاً
            } else {
                // Fallback للأجهزة القديمة
                setTimeout(triggerMaestroStabilization, 4000);
            }

            window.NexusTelemetry.endMark('WASM_IGNITION'); // ⏱️ إنهاء قياس النواة

            console.log("🚀 NUCLEUS ACTIVE: Zero-Latency Fusion Complete.");
            
            // 📊 طباعة التقرير التلقائي بعد 2 ثانية لضمان استقرار الشاشة
            setTimeout(() => { window.NexusTelemetry.generateReport(); }, 2000);

        } catch (err) {
            console.warn("Nucleus Ignition partial fail, retrying...", err);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', ignite);
    } else {
        ignite();
    }
})();
