/* 👑 ROYAL NUCLEUS LOADER */
(function() {
    const wasmScript = document.createElement('script');
    wasmScript.src = 'file:///android_asset/royal_nucleus.js'; // المسار المطبوع
    
    wasmScript.onload = async () => {
        try {
            const module = await createRoyalNucleusModule();
            window.Nexus = {
                Ignition: new module.RoyalIgnitionCore(),
                Core: new module.RoyalCoreEngine(),
                Network: new module.RoyalNetworkCore()
            };
            
            // تفعيل التحقق من السرعة فوراً
            window.Nexus.Ignition.perform_socket_priming(window.location.origin);
            console.log("🚀 ROYAL NUCLEUS: Engine Fused into V8 Core.");
        } catch (err) {
            console.error("❌ ROYAL NUCLEUS: Fusion failed", err);
        }
    };
    document.head.appendChild(wasmScript);
})();
