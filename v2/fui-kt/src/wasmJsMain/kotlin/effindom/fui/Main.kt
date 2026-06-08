package effindom.fui

import kotlin.js.JsExport

private const val FONT_REGULAR = 1
private val PANEL_TEXT = 0xE2E8F0FF.toInt()
private const val SPACING = 32f

@JsExport
fun __runApp() {
    Runtime.init(WasmFfi)

    val root = column {
        padding(24f)
        child(
            row {
                child(
                    text("left") {
                        font(FONT_REGULAR, 28f)
                        textColor(PANEL_TEXT)
                    }
                )
                child(
                    flexBox {
                        width(SPACING)
                        height(1f)
                    }
                )
                child(
                    text("right") {
                        font(FONT_REGULAR, 28f)
                        textColor(PANEL_TEXT)
                    }
                )
            }
        )
        child(
            flexBox {
                height(24f)
            }
        )
        child(
            flexBox {
                width(120f)
                height(96f)
                bgColor(0x006CFFFF.toInt())
            }
        )
    }

    Application.mount(root)
}

fun main() {
    __runApp()
}
