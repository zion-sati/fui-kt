# FUI-KT — Kotlin/Wasm bindings for EffinDom v2

> **⚠️ Early stage — instructions are temporary and likely to break.** FUI-KT is a thin Kotlin/Wasm binding over the shared
> C++ ABI.  It currently provides a bare-bones smoke app and a fluent node
> builder.  Controls, theming, signals, and component reconciliation are
> planned for future slices.  Expect breaking changes.

## Quickstart

```bash
# Prerequisites: JDK 17+, Gradle 9.5+
brew install openjdk@17 gradle    # macOS
# or: sudo apt install openjdk-17-jdk && sdk install gradle 9.5.1  # Linux

# Clone and build
git clone https://github.com/zion-sati/fui-kt.git
cd fui-kt
npm ci
npm run build
npm run serve
```

Open `http://127.0.0.1:8080/index.html`.

Open `http://127.0.0.1:8080/v2/fui-kt/index.html`.

Full quickstart: [docs/v2/fui-kt/QUICKSTART.md](docs/v2/fui-kt/QUICKSTART.md)

## What's here (Slice 1)

| Primitive | Status |
|---|---|
| `FlexBox` / `TextNode` builders | ✅ |
| `Application.mount(node)` | ✅ |
| Controls (Button, Slider, etc.) | 🔜 Slice 2 |
| Theming / styles | 🔜 Slice 2 |
| Component reactivity | 🔜 Slice 2 |

## Architecture

```
Kotlin/Wasm (GC heap)               C++ UI Runtime
┌──────────────────────┐            ┌────────────────┐
│  @JsModule external  │──shims───▶│  _ui_set_text() │
│  fun _uiSetText()     │            │  _ui_commit_frame()
│  Node.build()         │            │                 │
└──────────────────────┘            └────────────────┘
         │                                    │
         └──────────── JS bridge ──────────────┘
              (harness.ts + shim modules)

Kotlin/Wasm can't write to foreign linear memory, so string
passing uses a JS interop layer (ui_set_text_from_string).
```

FUI-KT calls the same C ABI as fui-as and fui-rs — the binding layer is the
only difference.

## License

AGPL-3.0-only (or commercial — see [COMMERCIAL.md](COMMERCIAL.md)).