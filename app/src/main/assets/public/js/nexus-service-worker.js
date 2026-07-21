/**
 * =========================================================
 * 🧠 Nexus UI Engine - Elite Service Worker (L0 Cache)
 * =========================================================
 * يعمل كوكيل لحظي داخل محرك V8 بالتوازي مع Royal Network
 */

const CACHE_NAME = 'nexus-v8-memory-cache-v1';
const BYTECODE_CACHE_NAME = 'royal-v8-bytecode-v1';

// 👑 التثبيت: فرض التفعيل الفوري دون انتظار إغلاق التبويبات القديمة
self.addEventListener('install', (event) => {
    self.skipWaiting();
    console.log('[Nexus SW] ⚡ Installed and forcing activation.');
});

// 👑 التفعيل: السيطرة الفورية على كل الصفحات المفتوحة وتنظيف القديم
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((cacheNames) => {
            return Promise.all(
                cacheNames.map((cache) => {
                    if (cache !== CACHE_NAME && cache !== BYTECODE_CACHE_NAME) {
                        console.log('[Nexus SW] 🧹 Clearing old V8 cache:', cache);
                        return caches.delete(cache);
                    }
                })
            );
        }).then(() => self.clients.claim())
    );
});

// 👑 اعتراض الطلبات: التنسيق مع حارس الشبكة النيتف والتثبيت الفوري للصفحة الرئيسية
self.addEventListener('fetch', (event) => {
    const request = event.request;
    const url = new URL(request.url);

    // تجاهل طلبات الـ API والتحليلات والطلبات غير الجالبة
    if (url.pathname.includes('/api/') || request.method !== 'GET') {
        return;
    }

    // ⚡ [NEXUS ZERO-SECOND CORE]: تخصيص الصفحة الرئيسية للتسريع اللحظي (0ms)
    const isHomePage = request.mode === 'navigate' && (url.pathname === '/' || url.pathname.endsWith('/index.html') || url.href === self.origin + '/');

    if (isHomePage) {
        event.respondWith(
            caches.open(CACHE_NAME).then(async (cache) => {
                // 1. إرجاع الصفحة المخزنة فوراً بـ 0ms إن وجدت
                const cachedResponse = await cache.match(request);
                
                // 2. إرسال طلب شبكة خلفي لتجديد النسخة للمرة القادمة
                const networkFetch = fetch(request).then((networkResponse) => {
                    if (networkResponse && networkResponse.status === 200) {
                        cache.put(request, networkResponse.clone());
                    }
                    return networkResponse;
                }).catch(() => {/* تجاهل انقطاع الشبكة */});

                // إرجاع النسخة المخزنة فورياً، أو انتظار الشبكة في أول فتح للتطبيق
                return cachedResponse || networkFetch;
            })
        );
        return;
    }

    // 🚀 استراتيجية (Stale-While-Revalidate) لباقي ملفات الهيكل (Scripts, Styles, Images)
    if (request.destination === 'document' || request.destination === 'script' || request.destination === 'style') {
        event.respondWith(
            caches.open(CACHE_NAME).then((cache) => {
                return cache.match(request).then((cachedResponse) => {
                    const fetchedResponse = fetch(request).then((networkResponse) => {
                        cache.put(request, networkResponse.clone());
                        return networkResponse;
                    }).catch(() => cachedResponse);

                    return cachedResponse || fetchedResponse;
                });
            })
        );
    }
});

// 🧠 قناة اتصال سرية: استقبال الأوامر من RoyalJsBridge لتسخين الروابط
self.addEventListener('message', (event) => {
    // ⚡ استقبال أمر الأرشفة الصارمة من الـ C++ Guardian
    if (event.data && event.data.type === 'SAVE_BYTECODE_STRICT') {
        const urlsToCache = event.data.urls || [];
        
        event.waitUntil(
            caches.open(BYTECODE_CACHE_NAME).then((cache) => {
                return Promise.all(urlsToCache.map(url => {
                    return fetch(url).then(response => {
                        // 💉 حقن رؤوس استجابة تخدع V8 بأن الملف لن يتغير أبداً
                        // هذا يحفز المحرك على إنتاج الـ Bytecode فوراً
                        const headers = new Headers(response.headers);
                        headers.set('Cache-Control', 'public, max-age=31536000, immutable');
                        
                        const fakeResponse = new Response(response.body, {
                            status: response.status,
                            statusText: response.statusText,
                            headers: headers
                        });
                        return cache.put(url, fakeResponse);
                    });
                }));
            }).then(() => console.log("⚡ [Nexus SW] Bytecode Persistence Primed."))
        );
    }
});
