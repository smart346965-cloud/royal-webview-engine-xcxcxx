/* 👑 ROYAL NUCLEUS DIAGNOSTIC LOADER */
(function() {
    console.log("==================================================");
    console.log("🔍 STARTING ROYAL NUCLEUS ROOT DIAGNOSTIC...");
    console.log("==================================================");

    // 1. فحص دعم البيئة للـ WebAssembly
    if (typeof WebAssembly !== "object") {
        console.error("❌ DIAGNOSTIC CRITICAL: Native WebAssembly is NOT supported or DISABLED in this WebView WebView v8 Core!");
        return;
    } else {
        console.log("✅ DIAGNOSTIC: Native WebAssembly object detected in V8.");
    }

    // 2. إعداد كائن الموديل مع تتبع مسار ملف الـ WASM
    window.Module = {
        locateFile: function(path) {
            if (path.endsWith('.wasm')) {
                const absoluteWasmPath = 'https://royal-engine.local/public/js/royal_nucleus.wasm';
                console.log("📂 DIAGNOSTIC: Emscripten requested .wasm file. Forcing path to:", absoluteWasmPath);
                return absoluteWasmPath;
            }
            return path;
        },
        print: function(text) {
            console.log("🛰️ WASM STDOUT:", text);
        },
        printErr: function(text) {
            console.warn("⚠️ WASM STDERR:", text);
        }
    };

    // 3. محاولة قحص وحجم ملف الـ WASM يدوياً قبل تشغيل السكربت (هل هو فارغ؟ هل الـ Interceptor يعمل؟)
    async function verifyWasmFileIntegrity() {
        const targetUrl = 'https://royal-engine.local/public/js/royal_nucleus.wasm';
        try {
            console.log("📡 DIAGNOSTIC: Fetching WASM binary file directly to check integrity...");
            const response = await fetch(targetUrl);
            
            console.log("📊 DIAGNOSTIC: Response Status =", response.status, response.statusText);
            console.log("📄 DIAGNOSTIC: Response Content-Type =", response.headers.get('Content-Type'));
            
            const buffer = await response.arrayBuffer();
            console.log("📏 DIAGNOSTIC: WASM File Binary Size =", buffer.byteLength, "bytes");
            
            if (buffer.byteLength === 0) {
                console.error("❌ DIAGNOSTIC CRITICAL: The WASM file was loaded but it is completely EMPTY (0 bytes)!");
                return false;
            }

            // الفحص السحري: هل يحتوي الملف على توقيع الـ WASM الحقيقي؟ (أول 4 بايتات يجب أن تكون \0asm)
            const magicNumber = new Uint8Array(buffer.slice(0, 4));
            console.log("🔮 DIAGNOSTIC: WASM Magic Number (Hex) =", Array.from(magicNumber).map(b => b.toString(16).padStart(2, '0')).join(' '));
            if (magicNumber[0] === 0x00 && magicNumber[1] === 0x61 && magicNumber[2] === 0x73 && magicNumber[3] === 0x6d) {
                console.log("🧬 DIAGNOSTIC: Magic Number Verified! This is a valid compiled WebAssembly Binary.");
                return true;
            } else {
                console.error("❌ DIAGNOSTIC CRITICAL: Magic Number Mismatch! The file in assets is corrupted or not a true compiled WASM binary.");
                return false;
            }
        } catch (e) {
            console.error("❌ DIAGNOSTIC CRITICAL: Failed to fetch WASM binary directly. Security Block or File Missing!", e);
            return false;
        }
    }

    // 4. إطلاق عملية التحميل وتتبع الجسر
    async function coreIgnition() {
        const wasmValid = await verifyWasmFileIntegrity();
        if (!wasmValid) return;

        console.log("🚀 DIAGNOSTIC: Injecting royal_nucleus.js script tag...");
        const wasmScript = document.createElement('script');
        // ❌ حذفنا wasmScript.type = 'module' لفك قفل السكربت
        wasmScript.src = 'https://royal-engine.local/public/js/royal_nucleus.js'; 
        
        wasmScript.onload = async () => {
            console.log("✅ DIAGNOSTIC: royal_nucleus.js loaded successfully.");
            
            // 🛡️ فحص ذكي: هل الدالة في النطاق العالمي أم محبوسة؟
            const starter = window.createRoyalNucleusModule || createRoyalNucleusModule;

            if (typeof starter !== "function") {
                console.error("❌ DIAGNOSTIC CRITICAL: 'createRoyalNucleusModule' is still hidden! Check global scope.");
                return;
            }

            try {
                console.log("⚙️ DIAGNOSTIC: Invoking Nucleus Module...");
                // نمرر window.Module الذي يحتوي على locateFile
                const module = await starter(window.Module); 
                
                console.log("📦 DIAGNOSTIC: Module compilation finished. Classes:", Object.keys(module).filter(k => k.startsWith('Royal')));

                window.Nexus = {
                    Ignition: new module.RoyalIgnitionCore(),
                    Core: new module.RoyalCoreEngine(),
                    Network: new module.RoyalNetworkCore()
                };
                
                window.Nexus.Ignition.perform_socket_priming(window.location.origin);
                console.log("🏆 DIAGNOSTIC SUCCESS: ENGINE FULLY FUSED.");
            } catch (err) {
                console.error("❌ DIAGNOSTIC CRITICAL: Execution Crash:", err);
            }
        };
        document.head.appendChild(wasmScript);
    }

    // بدء التشخيص
    coreIgnition();
})();
