/**
 * =========================================================
 * 👑 ROYAL ENGINE CORE (V2.0 - The Lean Orchestrator)
 * File: index.js
 * =========================================================
 * Architecture: Direct Execution, Phased Scheduling, Zero Overhead.
 * Philosophy: Keep it simple, protect the CPU, manage the time.
 */

(function () {
    'use strict';

    // 🛡️ حماية من التكرار
    if (window.__ROYAL_ORCHESTRATOR_ACTIVE__) return;
    window.__ROYAL_ORCHESTRATOR_ACTIVE__ = true;

    console.log("👑 ROYAL ENGINE: Orchestrator Online. Awaiting DOM...");

    // =========================================================
    // 🛡️ 1. SAFETY WRAPPER (صندوق الرمل لحماية المتجر)
    // =========================================================
    function safeExecute(moduleName, fn) {
        if (typeof fn !== 'function') return;
        try {
            // const t0 = performance.now();
            fn();
            // const t1 = performance.now();
            // console.log(`✅ [${moduleName}] executed in ${(t1 - t0).toFixed(2)}ms`);
        } catch (error) {
            console.error(`🚨 [ROYAL CRASH] Module: ${moduleName} failed!`, error);
        }
    }

    // =========================================================
    // ⏱️ 2. PHASED EXECUTION (التوزيع الزمني للعضلات)
    // =========================================================
    const moduleState = {
        interaction: false,
        speculation: false
    };

    function runDomReadyModules() {
        // ⚡ العضلات الحرجة: تعمل فور اكتمال الـ HTML لضمان استجابة النقر
        if (window.RoyalInteraction && !moduleState.interaction) {
            safeExecute('RoyalInteraction', window.RoyalInteraction.init);
            moduleState.interaction = true;
        }
    }

    function runIdleModules() {
        // 🔮 العضلات الثقيلة: تعمل فقط عندما يرتاح معالج الهاتف (لحماية الـ INP)
        if (window.RoyalSpeculator && !moduleState.speculation) {
            safeExecute('RoyalSpeculator', window.RoyalSpeculator.init);
            moduleState.speculation = true;
        }
    }

    // =========================================================
    // 🚀 3. BOOT SEQUENCE (تسلسل الإقلاع)
    // =========================================================
    function boot() {
        // 1. تشغيل عضلات التفاعل فوراً
        runDomReadyModules();

        // 2. تشغيل عضلات التنبؤ في وقت الفراغ
        const requestIdle = window.requestIdleCallback || function (cb) { setTimeout(cb, 200); };
        requestIdle(runIdleModules);
    }

    // ننتظر حتى يكتمل بناء هيكل الصفحة (DOM) قبل الإقلاع
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', boot);
    } else {
        boot();
    }

    // =========================================================
    // 🔄 4. SPA ROUTE MONITOR (مراقب التنقل الداخلي)
    // =========================================================
    // متاجر React لا تقوم بتحديث الصفحة، لذلك يجب أن نراقب التغييرات لإعادة تشغيل العضلات
    let lastUrl = location.href;
    let routeChangeScheduled = false;

    const routeObserver = new MutationObserver(() => {
        if (location.href === lastUrl) return;

        lastUrl = location.href;

        if (routeChangeScheduled) return;
        routeChangeScheduled = true;

        setTimeout(() => {
            routeChangeScheduled = false;

            console.log("🔄 ROYAL ENGINE: SPA Route Changed. Re-igniting modules...");

            window.dispatchEvent(new CustomEvent('royal:route-change', {
                detail: { url: lastUrl }
            }));

            requestAnimationFrame(() => {
                runDomReadyModules();

                const requestIdle = window.requestIdleCallback || function (cb) { setTimeout(cb, 200); };
                requestIdle(runIdleModules);
            });

        }, 150); // debounce time
    });

    // نراقب الـ body لأن React يغير محتواه عند الانتقال بين الصفحات
    routeObserver.observe(document.body, { childList: true, subtree: true });

})();
