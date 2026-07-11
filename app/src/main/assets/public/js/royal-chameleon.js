/**
 * =========================================================
 * 🎨 ROYAL CHAMELEON ENGINE (V2.0 - The Flawless Illusion)
 * File: royal-chameleon.js
 * =========================================================
 * Architecture: Dynamic Theme-Color, Luminance Detection, Scroll Sync.
 * Fixes: Null Pointer Protection, CPU-Safe SPA Observer, Scroll Throttling.
 */

(function () {
    'use strict';

    const MODULE_NAME = 'RoyalChameleon';

    // 🛡️ حماية من التكرار
    if (window.__ROYAL_CHAMELEON__) return;
    window.__ROYAL_CHAMELEON__ = true;

    const Chameleon = {
        metaTheme: null,
        lastColor: '',
        scrollTimer: null,
        lastScrollTime: 0, // 👑 حارس السكرول (Throttle)

        init: function (context) {
            const { Logger } = context;

            // 🛠️ الإصلاح 1: البحث عن وسم theme-color أو إنشاؤه (يجب أن يكون أول شيء!)
            this.metaTheme = document.querySelector('meta[name="theme-color"]');
            if (!this.metaTheme) {
                this.metaTheme = document.createElement('meta');
                this.metaTheme.name = "theme-color";
                document.head.appendChild(this.metaTheme);
            }

            // 👑 تشغيل فوري بعد ضمان وجود الـ metaTheme
            this.syncColors();
            requestAnimationFrame(() => this.syncColors());

            // 👑 حفظ لون الهوية (مرة واحدة فقط - لا يتغير مع السكرول)
            const requestIdle = window.requestIdleCallback || function (cb) { setTimeout(cb, 800); };
            requestIdle(() => this.captureBrandColor());

            // 🛠️ الإصلاح 3: مراقبة السكرول مع خنق زمني (Throttle 150ms) لمنع احتراق المعالج
            document.addEventListener('scroll', () => {
                const now = performance.now();
                if (now - this.lastScrollTime < 150) return; // لا تعمل أكثر من مرة كل 150ms
                this.lastScrollTime = now;

                requestAnimationFrame(() => this.syncColors());
            }, { passive: true });

            // 4. دعم SPA (تغير الصفحات بدون reload)
            this.observeSPA();

            // 5. تحديث عند عودة التبويب للواجهة
            document.addEventListener('visibilitychange', () => {
                if (!document.hidden) {
                    requestAnimationFrame(() => this.syncColors());
                }
            });

            Logger.info(`[${MODULE_NAME}] Chameleon Illusion Online.`);
        },

        /**
         * 🔍 قراءة الألوان وتحديث شريط كروم
         */
        syncColors: function () {
            // أ. محاولة قراءة لون الهيدر
            let topColor = this.getTopElementColor();
           
            // ب. إذا فشل، نأخذ لون خلفية الـ Body
            if (!topColor || topColor === 'rgba(0, 0, 0, 0)' || topColor === 'transparent') {
                topColor = window.getComputedStyle(document.body).backgroundColor;
            }

            // ج. تحويل اللون إلى HEX
            const hexColor = this.rgbToHex(topColor);

            // د. التحديث فقط إذا تغير اللون
            if (hexColor && hexColor !== this.lastColor) {
                this.metaTheme.setAttribute("content", hexColor);
                this.lastColor = hexColor;
               
                // تعديل لون الأيقونات (فاتح/داكن)
                this.adjustIconContrast(topColor);
            }
        },

        /**
         * 🎯 قناص الألوان: يقرأ لون أول عنصر مرئي في أعلى الشاشة
         */
        getTopElementColor: function() {
            const points =[10, 30, 60];

            for (let y of points) {
                const el = document.elementFromPoint(window.innerWidth / 2, y);
                if (!el) continue;

                const style = window.getComputedStyle(el);
                const bg = style.backgroundColor;

                if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent') {
                    return bg;
                }
            }
            return null;
        },

        /**
         * 🌓 حارس التباين: يحسب سطوع اللون ليقرر لون أيقونات البطارية
         */
        adjustIconContrast: function(rgbString) {
            const rgb = rgbString.match(/\d+/g);
            if (!rgb || rgb.length < 3) return;

            const r = parseInt(rgb[0]);
            const g = parseInt(rgb[1]);
            const b = parseInt(rgb[2]);

            const luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255;

            if (luminance > 0.6) {
                document.documentElement.style.setProperty('color-scheme', 'light');
            } else {
                document.documentElement.style.setProperty('color-scheme', 'dark');
            }
        },

        /**
         * 🔧 أداة مساعدة: تحويل RGB إلى HEX
         */
        rgbToHex: function(rgb) {
            const match = rgb.match(/^rgba?\((\d+),\s*(\d+),\s*(\d+)/);
            if (!match) return null;
            function hex(x) {
                return ("0" + parseInt(x).toString(16)).slice(-2);
            }
            return "#" + hex(match[1]) + hex(match[2]) + hex(match[3]);
        },

        /**
         * 👑 دعم SPA: مراقبة تغييرات الـ URL (CPU-Safe)
         */
        observeSPA: function () {
            let lastUrl = location.href;

            // 🛠️ الإصلاح 2: استخدام setInterval خفيف جداً بدلاً من MutationObserver المدمر
            setInterval(() => {
                if (location.href !== lastUrl) {
                    lastUrl = location.href;
                    requestAnimationFrame(() => this.syncColors());
                }
            }, 500); // فحص الرابط مرتين في الثانية فقط (استهلاك 0% من المعالج)
        },

        /**
         * 🎨 حفظ لون الهوية (مرة واحدة فقط - لا يتغير مع السكرول)
         */
        captureBrandColor: function () {
            try {
                let color = null;

                function isValidColor(rgb) {
                    const values = rgb.match(/\d+/g);
                    if (!values) return false;

                    const [r, g, b] = values.map(Number);

                    // تجاهل الألوان القريبة من الأبيض أو الرمادي
                    if (r > 240 && g > 240 && b > 240) return false;

                    return true;
                }

                // 🥇 1. محاولة التقاط لون الهيدر (أقوى مؤشر هوية)
                const headerSelectors = [
                    'header',
                    '.header',
                    '.navbar',
                    '.top-bar',
                    '[role="banner"]'
                ];

                for (let selector of headerSelectors) {
                    const el = document.querySelector(selector);
                    if (!el) continue;

                    const bg = getComputedStyle(el).backgroundColor;

                    if (bg && bg !== 'rgba(0, 0, 0, 0)' && bg !== 'transparent' && isValidColor(bg)) {
                        color = bg;
                        break;
                    }
                }

                // 🥈 2. محاولة أخذ لون زر رئيسي
                if (!color) {
                    const btn = document.querySelector('button, .btn, .btn-primary, a[role="button"]');
                    if (btn) {
                        const bg = getComputedStyle(btn).backgroundColor;
                        if (bg && bg !== 'rgba(0, 0, 0, 0)' && isValidColor(bg)) {
                            color = bg;
                        }
                    }
                }

                // 🥉 3. meta fallback (إذا موجود)
                if (!color) {
                    color =
                        document.querySelector('meta[name="theme-color"]')?.content ||
                        document.querySelector('meta[name="msapplication-TileColor"]')?.content;
                }

                // 🏁 4. fallback أخير
                if (!color || color === 'rgba(0, 0, 0, 0)' || !isValidColor(color)) {
                    color = getComputedStyle(document.body).backgroundColor || '#0f172a';
                }

                // 🎯 تحويل إلى HEX
                if (color && color.startsWith('rgb')) {
                    const rgb = color.match(/\d+/g);
                    if (rgb) {
                        color =
                            "#" +
                            rgb.slice(0, 3)
                                .map(x => {
                                    const hex = parseInt(x).toString(16);
                                    return hex.length === 1 ? "0" + hex : hex;
                                })
                                .join('');
                    }
                }

                // 🧠 حفظ مرة واحدة فقط
                if (!localStorage.getItem('royal_theme_color')) {
                    localStorage.setItem('royal_theme_color', color);
                }

            } catch (e) {
                // صامت
            }
        }
    };

    // =========================================================
    // 📦 REGISTER MODULE (تسجيل العضلة في المايسترو)
    // =========================================================
    window.__ROYAL_MODULES__ = window.__ROYAL_MODULES__ ||[];

    window.__ROYAL_MODULES__.push({
        name: MODULE_NAME,
        stage: 'IDLE',
        priority: 25,

        init: function (context) {
            Chameleon.init(context);
        },

        hooks: {
            afterInit: () => {
                // console.log(`[${MODULE_NAME}] is active.`);
            }
        }
    });

    // 🔗 كشف العضلة للمايسترو (index.js)
    window.RoyalChameleon = { init: function() { Chameleon.init({ Logger: console }); } };

})();
