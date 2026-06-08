package effindom.fui

import kotlin.js.JsModule

// ---- UI tier imports (effindom_v2_ui) ----

@JsModule("effindom_v2_ui")
internal external fun _uiReset()

@JsModule("effindom_v2_ui")
internal external fun _uiCreateNode(nodeType: Int): Long

@JsModule("effindom_v2_ui")
internal external fun _uiDeleteNode(handle: Long)

@JsModule("effindom_v2_ui")
internal external fun _uiNodeAddChild(parent: Long, child: Long)

@JsModule("effindom_v2_ui")
internal external fun _uiNodeRemoveChild(parent: Long, child: Long)

@JsModule("effindom_v2_ui")
internal external fun _uiSetRoot(handle: Long)

@JsModule("effindom_v2_ui")
internal external fun _uiSetWidth(handle: Long, value: Float, unit: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiSetHeight(handle: Long, value: Float, unit: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiSetBgColor(handle: Long, color: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiAllocUnmanagedBuffer(len: Int): Int

@JsModule("effindom_v2_ui")
internal external fun _uiFreeUnmanagedBuffer(ptr: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiSetText(handle: Long, ptr: Int, len: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiSetFont(handle: Long, fontId: Int, size: Float)

@JsModule("effindom_v2_ui")
internal external fun _uiSetTextColor(handle: Long, color: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiSetPadding(handle: Long, top: Float, right: Float, bottom: Float, left: Float)

@JsModule("effindom_v2_ui")
internal external fun _uiSetFlexDirection(handle: Long, direction: Int)

@JsModule("effindom_v2_ui")
internal external fun _uiCommitFrame()

@JsModule("effindom_v2_ui")
internal external fun _uiResizeWindow(width: Float, height: Float)

// ---- Host imports (fui_host) ----

@JsModule("fui_host")
internal external fun _requestRender()

@JsModule("fui_host")
internal external fun _getViewportWidth(): Float

@JsModule("fui_host")
internal external fun _getViewportHeight(): Float

// ---- JS bridge helpers (bypass @JsModule for string management) ----

// Kotlin/Wasm cannot write to another module's linear memory directly.
// Use js() to call the harness function that handles heap allocation + copy.
internal fun jsUiSetTextFromString(handle: Long, text: String): Unit =
    js("window.effindom_v2_ui.ui_set_text_from_string(handle, text)")
