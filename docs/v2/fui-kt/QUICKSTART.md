# FUI Kotlin Quickstart

> **⚠️ Early stage.** FUI-KT is under active development. The current slice
> provides a working smoke app and fluent node builders. Controls, theming,
> component reactivity, and signal-based reactivity are planned for future
> slices. Expect breaking changes between slices.

## Prerequisites

Install the shared v2 toolchain first:

- [docs/QUICKSTART.md](../../QUICKSTART.md)

Then install the Kotlin/Wasm toolchain:

### macOS

```bash
# JDK 17+ (required by Kotlin 2.4+)
brew install openjdk@17
# Add to your shell profile (~/.zshrc or ~/.bashrc):
export JAVA_HOME="$(brew --prefix openjdk@17)"
export PATH="$JAVA_HOME/bin:$PATH"

# Gradle (build system)
brew install gradle
```

### Linux (Debian / Ubuntu)

```bash
# JDK 17+
sudo apt-get update && sudo apt-get install -y openjdk-17-jdk

# Gradle via SDKMAN (recommended)
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 9.5.1
```

### Verify

```bash
java -version   # should show 17+
gradle --version  # should show 9.5+
```

### Generate the Gradle wrapper (first-time only)

```bash
cd v2/fui-kt
gradle wrapper --gradle-version 9.5.1
```

This creates `gradlew`, `gradlew.bat`, and `gradle/wrapper/` — commit these so future developers don't need gradle installed globally.

## Build and test

From the repository root:

```bash
npm run build:v2:fui-kt
npm run test:v2:fui-kt:unit
```

The unit tests run on the JVM (no browser needed) via a recording mock FFI.

## Manually test the smoke app in a browser

From the repository root:

```bash
npm run build:v2:browser-bridge
npm run build:v2:fui-kt
npm run serve
```

Then open:

```text
http://127.0.0.1:8080/v2/fui-kt/index.html
```

If port `8080` is busy, `npm run serve` prints the fallback port it chose. The page should render the blue-box smoke scene and the console should log readiness without errors.

## Write a node tree

```kotlin
import effindom.fui.*

fun main() {
    Runtime.init(WasmFfi)

    val root = column {
        padding(24f)
        child(row {
            child(text("left") { textColor(0xE2E8F0FF.toInt()) })
            child(flexBox { width(32f); height(1f) })
            child(text("right") { textColor(0xE2E8F0FF.toInt()) })
        })
        child(flexBox { height(24f) })
        child(flexBox {
            width(120f)
            height(96f)
            bgColor(0x006CFFFF.toInt())
        })
    }

    Application.mount(root)
}
```

## Available Slice 1 primitives

- `flexBox { ... }` — FlexBox node with fluent builder
- `text("content") { ... }` — Text node
- `column { ... }` / `row { ... }` — FlexBox presets with direction
- `Application.mount(node)` — Mount and commit a node tree
- `Ffi` interface — FFI to the C++ UI runtime
- `Runtime.init(ffi)` — Wire the FFI implementation
- `NodeType.FlexBox` / `NodeType.Text` — Node type enum
- `SizeSizeUnit.Pixel` / `SizeSizeUnit.Auto` / `SizeSizeUnit.Star` — Size unit enum

## Architecture

```
Kotlin/Wasm (GC)          C++ UI Runtime (non-GC)
┌──────────────┐          ┌─────────────────────┐
│  Node.kt     │          │  UiRuntime.cpp       │
│  FlexBox     │──FFI────▶│  CreateNode()        │
│  TextNode    │          │  SetWidth/Height()   │
│  Application │          │  CommitFrame()       │
└──────────────┘          └─────────────────────┘
        │                           │
   @WasmImport            EXPORT (exports.txt)
   "effindom_v2_ui"       "effindom_v2_ui"
        │                           │
        └─────────┬─────────────────┘
                  │
          JavaScript bridge
          (harness.ts + shim modules)
```

The Kotlin/Wasm module imports C++ functions via `@WasmImport`. The browser harness loads both modules and wires the C++ exports into the import namespace Kotlin expects. Because Kotlin/Wasm uses a GC-managed heap, string passing uses a JS interop layer to copy bytes between heaps.

## Source layout

```
v2/fui-kt/
├── build.gradle.kts              # KMP config (wasmJs + jvm)
├── settings.gradle.kts
├── package.json                  # npm build/test scripts
├── scripts/build.sh              # gradle → esbuild → copy
├── browser/
│   ├── harness.ts                # Loads C++ runtime + Kotlin WASM
│   └── index.html
└── src/
    ├── commonMain/kotlin/effindom/fui/
    │   ├── Ffi.kt                # FFI interface
    │   ├── Runtime.kt            # Composition root
    │   ├── Node.kt               # FlexBox, TextNode, builders
    │   └── Application.kt        # mount()
    ├── wasmJsMain/kotlin/effindom/fui/
    │   ├── WasmExternals.kt      # @WasmImport declarations
    │   ├── WasmFfi.kt            # Ffi impl wrapping externals
    │   └── Main.kt               # @WasmExport entry point
    ├── jvmMain/kotlin/effindom/fui/
    │   └── JvmPlaceholder.kt
    └── jvmTest/kotlin/effindom/fui/
        ├── MockFfi.kt            # Recording mock for unit tests
        └── SmokeTest.kt         # Tests
```
