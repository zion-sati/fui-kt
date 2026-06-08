#!/usr/bin/env bash

set -euo pipefail

# Ensure pipefail is active (may not be set by calling shell)
set -o pipefail 2>/dev/null || true

PACKAGE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPO_ROOT="$(cd "${PACKAGE_DIR}/../.." && pwd)"
OUT_DIR="${REPO_ROOT}/public/v2/fui-kt"
BRIDGE_DIR="${REPO_ROOT}/public/v2/browser-bridge"
PUBLIC_DIR="${PACKAGE_DIR}/public"

mkdir -p "${OUT_DIR}" "${PUBLIC_DIR}"

bold()  { printf '\033[1m%s\033[0m\n' "$*"; }
green() { printf '\033[32m%s\033[0m\n' "$*"; }

bold "=== Building fui-kt WASM ==="
cd "${PACKAGE_DIR}"

# Compile Kotlin/Wasm. Default output is just project.wasm + project.wasm.js.
GRADLE_LOG=$(mktemp)
if ! ./gradlew wasmJsProductionExecutableCompileSync --no-daemon --refresh-dependencies > "${GRADLE_LOG}" 2>&1; then
  cat "${GRADLE_LOG}" >&2
  rm -f "${GRADLE_LOG}"
  echo "ERROR: wasmJsProductionExecutableCompileSync failed" >&2
  exit 1
fi
if ! grep -q 'BUILD SUCCESSFUL' "${GRADLE_LOG}"; then
  cat "${GRADLE_LOG}" >&2
  rm -f "${GRADLE_LOG}"
  echo "ERROR: wasmJsProductionExecutableCompileSync did not report BUILD SUCCESSFUL" >&2
  exit 1
fi
rm -f "${GRADLE_LOG}"

bold "=== Generating JS shim modules for Kotlin/Wasm @JsModule imports ==="

# Kotlin/Wasm generates import-object.mjs that does:
#   import * as ns from 'effindom_v2_ui';
#   ns.default(args)  // for ALL external functions
# This can't dispatch same-arity functions (e.g. _getViewportWidth vs _getViewportHeight).
# We post-process to use named exports: ns._uiReset(args).

WASM_DIR="${PACKAGE_DIR}/build/compileSync/wasmJs/main/productionExecutable/kotlin"
IMPORT_OBJ="${WASM_DIR}/fui-kt.import-object.mjs"

if [ ! -f "${IMPORT_OBJ}" ]; then
  echo "ERROR: import-object not found at ${IMPORT_OBJ}" >&2
  exit 1
fi

# Replace _ref_XXXX_.default(args) with _ref_XXXX_.FUNCNAME(args)
# Extracts FUNCNAME from the WASM import key: effindom.fui._uiReset_$external_fun -> _uiReset
sed -i '' -E \
  's/(effindom\.fui\._([a-zA-Z0-9]+)_\$external_fun.*_ref_[a-zA-Z0-9_]+)\.default\(/\1._\2(/g' \
  "${IMPORT_OBJ}"

# Generate effindom_v2_ui.js — exports named functions that delegate to window.effindom_v2_ui
# The harness (harness.ts) sets window.effindom_v2_ui before loading fui-kt.mjs.
# Kotlin function names (UpperCamelCase) map to harness names (snake_case).
cat > "${PUBLIC_DIR}/effindom_v2_ui.js" << 'SHIM_UI'
const m = globalThis.effindom_v2_ui;
if (!m) throw new Error('effindom_v2_ui: window.effindom_v2_ui not set — harness must load first');

// Kotlin @JsModule external fun names -> harness snake_case keys
export function _uiReset(...a)                { return m.ui_reset(...a); }
export function _uiCreateNode(...a)           { return m.ui_create_node(...a); }
export function _uiDeleteNode(...a)           { return m.ui_delete_node(...a); }
export function _uiNodeAddChild(...a)         { return m.ui_node_add_child(...a); }
export function _uiNodeRemoveChild(...a)      { return m.ui_node_remove_child(...a); }
export function _uiSetRoot(...a)              { return m.ui_set_root(...a); }
export function _uiSetWidth(...a)             { return m.ui_set_width(...a); }
export function _uiSetHeight(...a)            { return m.ui_set_height(...a); }
export function _uiSetBgColor(...a)           { return m.ui_set_bg_color(...a); }
export function _uiAllocUnmanagedBuffer(...a)  { return m.ui_alloc_unmanaged_buffer(...a); }
export function _uiFreeUnmanagedBuffer(...a)   { return m.ui_free_unmanaged_buffer(...a); }
export function _uiSetText(...a)              { return m.ui_set_text(...a); }
export function _uiSetFont(...a)              { return m.ui_set_font(...a); }
export function _uiSetTextColor(...a)         { return m.ui_set_text_color(...a); }
export function _uiSetPadding(...a)           { return m.ui_set_padding(...a); }
export function _uiSetFlexDirection(...a)     { return m.ui_set_flex_direction(...a); }
export function _uiCommitFrame(...a)          { return m.ui_commit_frame(...a); }
export function _uiResizeWindow(...a)         { return m.ui_resize_window(...a); }
SHIM_UI

# Generate fui_host.js — exports named functions that delegate to window.fui_host
cat > "${PUBLIC_DIR}/fui_host.js" << 'SHIM_HOST'
const m = globalThis.fui_host;
if (!m) throw new Error('fui_host: window.fui_host not set — harness must load first');

export function _requestRender(...a)       { return m.request_render(...a); }
export function _getViewportWidth(...a)    { return m.get_viewport_width(...a); }
export function _getViewportHeight(...a)   { return m.get_viewport_height(...a); }
SHIM_HOST

bold "=== Copying build artifacts ==="

if [ ! -d "${WASM_DIR}" ]; then
  echo "ERROR: Kotlin/Wasm output not found at ${WASM_DIR}" >&2
  exit 1
fi

# Main WASM file
cp "${WASM_DIR}/fui-kt.wasm" "${OUT_DIR}/fui-kt.wasm"
# JS glue files (skip classes.*.wasm split modules)
cp "${WASM_DIR}/"*.mjs "${OUT_DIR}/" 2>/dev/null || true

bold "=== Bundling fui-kt browser harness ==="
npx esbuild "${PACKAGE_DIR}/browser/harness.ts" \
  --bundle \
  --format=esm \
  --platform=browser \
  --target=es2020 \
  --minify \
  --outfile="${OUT_DIR}/harness.js" \
  --sourcemap

cp "${PACKAGE_DIR}/browser/index.html" "${OUT_DIR}/index.html"
cp "${PUBLIC_DIR}/effindom_v2_ui.js" "${OUT_DIR}/effindom_v2_ui.js"
cp "${PUBLIC_DIR}/fui_host.js" "${OUT_DIR}/fui_host.js"
cp "${BRIDGE_DIR}/bridge.js" "${OUT_DIR}/bridge.js"
cp "${BRIDGE_DIR}/bridge.js.map" "${OUT_DIR}/bridge.js.map"

cp "${BRIDGE_DIR}/effindom.v2.manifest.json" "${OUT_DIR}/effindom.v2.manifest.json"
if [ -f "${BRIDGE_DIR}/icu-asset.json" ]; then
  cp "${BRIDGE_DIR}/icu-asset.json" "${OUT_DIR}/icu-asset.json"
fi
rm -rf "${OUT_DIR}/runtime"
cp -R "${BRIDGE_DIR}/runtime" "${OUT_DIR}/runtime"

# Generate runtime config — tells the bridge where to find the manifest.
# Must be loaded BEFORE bridge.js (see index.html script order).
cat > "${OUT_DIR}/effindom-runtime-config.js" << RUNTIME_CONFIG
window.__effindomRuntime = Object.assign({}, window.__effindomRuntime, {
  manifestUrl: './effindom.v2.manifest.json',
});
RUNTIME_CONFIG

green "fui-kt build complete — artifacts in ${OUT_DIR}"
