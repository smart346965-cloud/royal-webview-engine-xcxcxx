/* 👑 ROYAL NUCLEUS LOADER */
(function() {
    // 1. تعريف الكائن وتحديد مسار الـ WASM الحقيقي في أصول الأندرويد إجبارياً
    window.Module = {
        locateFile: function(path) {
            if (path.endsWith('.wasm')) {
                return 'file:///android_asset/public/js/royal_nucleus.wasm';
            }
            return path;
        }
    };

    const wasmScript = document.createElement('script');
    wasmScript.src = 'file:///android_asset/public/js/royal_nucleus.js'; 
    
    wasmScript.onload = async () => {
        try {
            // 2. استدعاء وحدة الدمج مع تمرير الإعدادات المجهزة بالمسار المطلق
            const module = await createRoyalNucleusModule(window.Module);
            window.Nexus = {
                Ignition: new module.RoyalIgnitionCore(),
                Core: new module.RoyalCoreEngine(),
                Network: new module.RoyalNetworkCore()
            };
            
            window.Nexus.Ignition.perform_socket_priming(window.location.origin);
            console.log("🚀 ROYAL NUCLEUS: Engine Fused into V8 Core.");
        } catch (err) {
            console.error("❌ ROYAL NUCLEUS: Fusion failed", err);
        }
    };
    document.head.appendChild(wasmScript);
})();
