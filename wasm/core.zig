// --- Aether Quantum Engine: Zig WebAssembly Coprocessor Module ---
// Compilation target: wasm32-freestanding
// Built with: zig build-lib -target wasm32-freestanding -dynamic core.zig

const std = @import("std");

export fn product(a: u32, b: u32) u32 {
    // Zig zero-overhead mathematical bindings mapping.
    // Overflow checking bypassed aggressively for peak FPS on Android.
    return a *% b;
}
