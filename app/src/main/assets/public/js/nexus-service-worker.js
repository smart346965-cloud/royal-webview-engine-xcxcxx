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

// 👑 اعتراض الطلبات: التنسيق مع حارس الشبكة النيتف
self.addEventListener('fetch', (event) => {
    const request = event.request;
    const url = new URL(request.url);

    // تجاهل طلبات الـ API والتحليلات (نتركها تعبر للإنترنت مباشرة)
    if (url.pathname.includes('/api/') || request.method !== 'GET') {
        return;
    }

    // 🚀 استراتيجية (Stale-While-Revalidate) لملفات الهيكل الثابتة
    // تعطي المستخدم النسخة المخزنة فوراً، وتقوم بتحديثها في الخلفية من النيتف
    if (request.destination === 'document' || request.destination === 'script' || request.destination === 'style') {
        event.respondWith(
            caches.open(CACHE_NAME).then((cache) => {
                return cache.match(request).then((cachedResponse) => {
                    const fetchedResponse = fetch(request).then((networkResponse) => {
                        // حفظ النسخة الجديدة للصدمة القادمة
                        cache.put(request, networkResponse.clone());
                        return networkResponse;
                    }).catch(() => {
                        // إذا فشل الاتصال، نعتمد كلياً على النيتف أو الكاش
                        return cachedResponse;
                    });

                    // إرجاع الكاش فوراً إذا وجد، وإلا انتظار الشبكة/النيتف
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
