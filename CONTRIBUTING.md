# Contributing to FUI-KT

> **⚠️ Early stage.** FUI-KT is a thin Kotlin/Wasm binding over the shared
> C++ ABI. It's under active development — controls, theming, component
> reactivity, and signal-based reactivity are planned for future slices.

This guide is for people working **on the SDK itself** — fixing bugs, adding
bindings, or improving the runtime integration.

---

## Prerequisites

- **JDK 17+** (required by Kotlin 2.4+)
- **Gradle 9.5+**
- **Node.js 24+** and npm
- **`@effindomv2/runtime@0.1.15+`** — fetched via the monorepo or npm

If developing against a **local runtime checkout**, install from the
[EffinDOM repo](https://github.com/zion-sati/EffinDOM) first.

---

## Clone and build

```bash
git clone https://github.com/zion-sati/EffinDOM.git
cd EffinDOM
npm ci
npm run build:v2:browser-bridge
npm run build:v2:fui-kt
```

## Run tests

```bash
npm run test:v2:fui-kt:unit
```

Unit tests run on the JVM (no browser needed) via a recording mock FFI.

## Run the smoke app

```bash
npm run serve
```

Open `http://127.0.0.1:8080/v2/fui-kt/index.html`.

## Repo structure

```
src/
  commonMain/kotlin/effindom/fui/
    Ffi.kt         — FFI interface
    Runtime.kt     — Composition root
    Node.kt        — Fluent node builders (FlexBox, TextNode)
    Application.kt — mount()
  wasmJsMain/kotlin/effindom/fui/
    WasmExternals.kt — @WasmImport declarations
    WasmFfi.kt       — Ffi impl wrapping externals
    Main.kt          — @WasmExport entry point
  jvmTest/kotlin/effindom/fui/
    MockFfi.kt       — Recording mock for unit tests
    SmokeTest.kt     — Tests
```

## Docs

- **[FUI-KT Quickstart](https://github.com/zion-sati/EffinDOM/blob/main/docs/v2/fui-kt/QUICKSTART.md)**
- **[FUI-AS Docs Index](https://github.com/zion-sati/EffinDOM/blob/main/docs/v2/fui-as/SDK_INDEX.md)** (C ABI docs apply to all SDKs)

## Getting in touch

This is a solo project. If you're thinking about contributing, please open an
issue or start a discussion before writing code.

For anything else: **zionsatidev@gmail.com**
