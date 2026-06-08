import type { BridgeRuntime, BridgeState, WasmHandleLike } from '../../../browser-bridge/src/index.js';

declare global {
  interface Window {
    __fuiKtReady?: boolean;
    __fuiKtError?: string;
    __fuiKtState?: {
      readonly commandWordCount: number;
      readonly commandWords: readonly number[];
      readonly rootHandle: string | null;
    };
    effindom_v2_ui?: Record<string, (...args: unknown[]) => unknown>;
    fui_host?: Record<string, (...args: unknown[]) => unknown>;
    EffinDomBrowserBridge?: BridgeState;
  }
}

type AppHandleLike = number | bigint;

let latestCommandWords: number[] = [];
let latestRootHandle: string | null = null;

function updateWindowState(): void {
  window.__fuiKtState = {
    commandWordCount: latestCommandWords.length,
    commandWords: latestCommandWords,
    rootHandle: latestRootHandle,
  };
}

function toBigIntHandle(handle: WasmHandleLike | AppHandleLike): bigint {
  if (typeof handle === 'bigint') return handle;
  if (typeof handle === 'number') return BigInt(handle);
  if (typeof handle === 'string') return BigInt(handle);
  const primitive = handle.valueOf();
  if (typeof primitive === 'bigint') return primitive;
  if (typeof primitive === 'number') return BigInt(primitive);
  if (typeof primitive === 'string') return BigInt(primitive);
  return BigInt(handle.toString());
}

function createUiImports(runtime: BridgeRuntime): Record<string, unknown> {
  return {
    ui_reset(): void {
      runtime.ui._ui_reset();
      latestCommandWords = [];
      latestRootHandle = null;
      updateWindowState();
    },
    ui_create_node(type: number): bigint { return toBigIntHandle(runtime.ui._ui_create_node(type)); },
    ui_delete_node(handle: AppHandleLike): void { runtime.ui._ui_delete_node(toBigIntHandle(handle)); },
    ui_node_add_child(parent: AppHandleLike, child: AppHandleLike): void {
      runtime.ui._ui_node_add_child(toBigIntHandle(parent), toBigIntHandle(child));
    },
    ui_node_remove_child(parent: AppHandleLike, child: AppHandleLike): void {
      runtime.ui._ui_node_remove_child(toBigIntHandle(parent), toBigIntHandle(child));
    },
    ui_set_root(handle: AppHandleLike): void {
      const rootHandle = toBigIntHandle(handle);
      latestRootHandle = rootHandle.toString();
      runtime.ui._ui_set_root(rootHandle);
      updateWindowState();
    },
    ui_set_width(handle: AppHandleLike, value: number, unit: number): void {
      runtime.ui._ui_set_width(toBigIntHandle(handle), value, unit);
    },
    ui_set_height(handle: AppHandleLike, value: number, unit: number): void {
      runtime.ui._ui_set_height(toBigIntHandle(handle), value, unit);
    },
    ui_set_bg_color(handle: AppHandleLike, color: number): void {
      runtime.ui._ui_set_bg_color(toBigIntHandle(handle), color);
    },
    ui_set_text(handle: AppHandleLike, ptr: number, len: number): void {
      runtime.ui._ui_set_text(toBigIntHandle(handle), ptr, len);
    },
    ui_set_text_from_string(handle: AppHandleLike, text: string): void {
      const bigHandle = toBigIntHandle(handle);
      if (text.length === 0) {
        runtime.ui._ui_set_text(bigHandle, 0, 0);
        return;
      }
      const bytes = new TextEncoder().encode(text);
      const ptr = runtime.ui._ui_arena_alloc(bytes.length);
      // _ui_arena_alloc returns BigInt on wasm64 — convert to number for HEAPU8 offset.
      const offset = Number(toBigIntHandle(ptr));
      runtime.ui.HEAPU8.set(bytes, offset);
      runtime.ui._ui_set_text(bigHandle, ptr, bytes.length);
    },
    ui_alloc_unmanaged_buffer(len: number) { return runtime.ui._ui_arena_alloc(len); },
    ui_free_unmanaged_buffer(_ptr: number): void { /* frame arena */ },
    ui_set_font(handle: AppHandleLike, fontId: number, size: number): void {
      runtime.ui._ui_set_font(toBigIntHandle(handle), fontId, size);
    },
    ui_set_text_color(handle: AppHandleLike, color: number): void {
      runtime.ui._ui_set_text_color(toBigIntHandle(handle), color);
    },
    ui_set_padding(handle: AppHandleLike, top: number, right: number, bottom: number, left: number): void {
      runtime.ui._ui_set_padding(toBigIntHandle(handle), top, right, bottom, left);
    },
    ui_set_flex_direction(handle: AppHandleLike, direction: number): void {
      runtime.ui._ui_set_flex_direction(toBigIntHandle(handle), direction);
    },
    ui_commit_frame(): void {
      // Only call commitFrame() — let the render loop's flushPendingCommit
      // handle syncCommandBufferToCore + executeCommandBuffer + _ed_render_frame.
      // Calling syncCommandBufferToCore here would extract the buffer before
      // the render loop gets it, leaving nothing to draw.
      runtime.commitFrame();
      latestCommandWords = [1]; // non-empty sentinel — app committed
      updateWindowState();
    },
    ui_resize_window(width: number, height: number): void {
      runtime.ui._ui_resize_window(width, height);
    },
  };
}

function createHostImports(runtime: BridgeRuntime): Record<string, unknown> {
  return {
    request_render(): void {
      runtime.requestFrame();
    },
    get_viewport_width(): number {
      const rect = runtime.canvas.getBoundingClientRect();
      return rect.width > 0 ? rect.width : runtime.canvas.width;
    },
    get_viewport_height(): number {
      const rect = runtime.canvas.getBoundingClientRect();
      return rect.height > 0 ? rect.height : runtime.canvas.height;
    },
  };
}

function waitForFrame(): Promise<void> {
  return new Promise<void>((resolve) => {
    requestAnimationFrame(() => {
      requestAnimationFrame(() => {
        resolve();
      });
    });
  });
}

const bridge = window.EffinDomBrowserBridge;
if (!bridge) throw new Error('EffinDomBrowserBridge not found');

bridge.ready
  .then(async (runtime: BridgeRuntime) => {
    window.effindom_v2_ui = createUiImports(runtime);
    window.fui_host = createHostImports(runtime);

    const script = document.createElement('script');
    script.type = 'module';
    script.src = './fui-kt.mjs';

    await new Promise<void>((resolve, reject) => {
      script.onload = () => resolve();
      script.onerror = () => reject(new Error('Failed to load fui-kt.mjs'));
      document.head.appendChild(script);
    });

    // Wait two animation frames so the first render completes.
    await waitForFrame();

    window.__fuiKtReady = true;
    delete window.__fuiKtError;
  })
  .catch((err: unknown) => {
    console.error('EffinDom bridge failed to initialize:', err);
    window.__fuiKtError = err instanceof Error ? err.message : String(err);
  });
