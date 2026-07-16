/**
 * =========================================================
 * ⚡ ROYAL INTERACTION ENGINE (V4 - The Instant Silk Touch)
 * =========================================================
 * Architecture: GPU Offloading, Passive Listeners, rAF Batching.
 * Updates: Instant Click (Slop Detection), Visual Feedback, BFCache Sanitizer.
 */

(function () {
    'use strict';

    // =========================================================
    // 🎨 1. CSS ACCELERATION & VISUAL FEEDBACK
    // =========================================================
    function injectHardwareAcceleration() {
        if (document.getElementById('royal-interaction-styles')) return;

        const style = document.createElement("style");
        style.id = 'royal-interaction-styles';
        style.textContent = `
            * { -webkit-tap-highlight-color: transparent !important; }
            a, button,[role="button"], input, select, textarea { touch-action: manipulation !important; }
            
            /* فقط العناصر غير الحساسة */
            img[loading="lazy"] {
                content-visibility: auto;
            }

            /* تسريع GPU بدون كسر التحميل */
            img {
                transform: translateZ(0);
                backface-visibility: hidden;
            }
            
            video, canvas, svg { transform: translateZ(0); backface-visibility: hidden; }
            
            /* فقط العناصر الثقيلة جداً */
            body.royal-is-scrolling iframe {
                pointer-events: none !important;
            }
            body.royal-is-scrolling { will-change: scroll-position; }

            /* 👑 الومضة البصرية الناعمة عند اللمس (Visual Feedback) */
            .royal-tap-active {
                opacity: 0.6 !important;
                transition: none !important;
            }
            .royal-tap-release {
                transition: opacity 0.3s ease-out !important;
            }
        `;
        document.head.appendChild(style);
    }

    // =========================================================
    // 👆 2. TAP ENGINE (محرك النقر اللحظي الذكي)
    // =========================================================
    const TapEngine = {
        init: function () {
            let startX = 0;
            let startY = 0;
            let isScrolling = false;
            let activeLink = null;

            // [تعديل جراحي في royallnteraction.js - TapEngine]

            document.addEventListener("touchstart", (e) => {
                if (e.touches.length === 0) return;

                startX = e.touches[0].clientX;
                startY = e.touches[0].clientY;
                isScrolling = false;

                const link = e.target.closest("a[href]");
                if (link && link.href) {
                    activeLink = link;
                    
                    // 🚀 استجابة لمسية بصرية فورية (تسبق قرار المتصفح)
                    requestAnimationFrame(() => {
                        link.classList.add('royal-tap-active');
                    });

                    // 🧠 تسخين الرابط في النيتف فور اللمس (توقع بنسبة 90%)
                    if (window.RoyalBridge) {
                        window.RoyalBridge.warmup(link.href);
                    }
                }
            }, { passive: true });

            // 2. عند التحرك: اكتشاف السكرول (Slop Detection) وإلغاء النقر
            document.addEventListener("touchmove", (e) => {
                if (isScrolling || e.touches.length === 0 || !activeLink) return;
                
                const moveX = Math.abs(e.touches[0].clientX - startX);
                const moveY = Math.abs(e.touches[0].clientY - startY);
                
                // إذا تحرك الإصبع أكثر من 12 بكسل، نعتبره سكرول
                if (moveX > 12 || moveY > 12) {
                    isScrolling = true;
                    activeLink.classList.remove('royal-tap-active');
                    activeLink = null;
                }
            }, { passive: true });

            // تحديث قسم touchend لضمان الانتقال اللحظي
            document.addEventListener("touchend", (e) => {
                if (isScrolling || !activeLink) {
                    if (activeLink) activeLink.classList.remove('royal-tap-active');
                    activeLink = null;
                    return;
                }

                const link = activeLink;
                activeLink = null;

                // إزالة الومضة بنعومة
                link.classList.replace('royal-tap-active', 'royal-tap-release');
                
                // إذا كان الرابط داخلياً، اطلب الانتقال فوراً لكسر حاجز الـ 300ms
                if (link.origin === location.origin) {
                    window.location.href = link.href;
                }
            }, { passive: false }); // passive: false ضرورية هنا لكي يعمل preventDefault
        }
    };

    // =========================================================
    // 🛡️ 3. BFCACHE SANITIZER (حارس الرجوع الفوري)
    // =========================================================
    const BFCacheSanitizer = {
        init: function () {
            window.addEventListener('pageshow', function (event) {
                // إذا تم استعادة الصفحة من الرام (BFCache)
                if (event.persisted || (window.performance && window.performance.navigation.type === 2)) {
                    console.log("⏪ ROYAL ENGINE: BFCache Zero-Latency Restore.");
                    
                    // إجبار الجسم على الظهور فوراً
                    document.body.style.opacity = "1";
                    document.body.style.visibility = "visible";

                    // إبلاغ الجانب النيتف بإخفاء أي سبلاش فوراً
                    if (window.RoyalBridge && typeof window.RoyalBridge.hideSplash === 'function') {
                        window.RoyalBridge.hideSplash();
                    }
                }
            });
            
            // منع الوميض الأبيض عند بداية النقر على رابط (Pre-hiding)
            window.addEventListener('beforeunload', function() {
                // لا نخفي الصفحة، بل نجعلها ثابتة لتقليل التباين البصري
                document.body.style.transition = "opacity 0.1s";
            });
        }
    };

    // =========================================================
    // 🌊 4. SCROLL ENGINE & GESTURE OPTIMIZER
    // =========================================================
    const ScrollEngine = {
        init: function () {
            let lastY = window.scrollY;
            let ticking = false;
            let scrollEndTimer = null;
            let lastTime = 0; 

            function onScroll() {
                if (!document.body.classList.contains("royal-is-scrolling")) {
                    document.body.classList.add("royal-is-scrolling");
                }

                if (scrollEndTimer) clearTimeout(scrollEndTimer);

                scrollEndTimer = setTimeout(() => {
                    document.body.classList.remove("royal-is-scrolling");
                }, 150);

                const now = performance.now();
                if (now - lastTime < 50) return;
                lastTime = now;

                if (!ticking) {
                    requestAnimationFrame(() => {
                        const currentY = window.scrollY;
                        const velocity = currentY - lastY;
                        lastY = currentY;
                        ticking = false;
                    });
                    ticking = true;
                }
            }
            document.addEventListener("scroll", onScroll, { passive: true });
        }
    };

    // =========================================================
    // 🧱 5. RENDER STABILIZER
    // =========================================================
    const RenderStabilizer = {
        init: function () {
            let scheduled = false;
            function stabilize() {
                if (scheduled) return;
                scheduled = true;
                requestAnimationFrame(() => { scheduled = false; });
            }
            const observer = new MutationObserver(stabilize);
            observer.observe(document.body, { childList: true, subtree: true });
        }
    };

    // =========================================================
    // 🚀 IGNITION (كشف العضلة للمايسترو)
    // =========================================================
    function startRoyalInteraction() {
        injectHardwareAcceleration();
        TapEngine.init();
        ScrollEngine.init();
        RenderStabilizer.init();
        BFCacheSanitizer.init(); 
        console.log("⚡ ROYAL INTERACTION ENGINE V4: Instant Click & BFCache Online.");
    }

    // 👑 نكشف الدالة للـ window لكي يستدعيها index.js متى شاء
    window.RoyalInteraction = {
        init: startRoyalInteraction
    };

})();
