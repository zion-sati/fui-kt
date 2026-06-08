package effindom.fui

import kotlin.test.Test
import kotlin.test.assertTrue

class SmokeTest {
    @Test
    fun `builds flexbox root with bgColor and commits frame`() {
        val mock = MockFfi()
        Runtime.init(mock)

        val root = flexBox {
            width(320f)
            height(200f)
            bgColor(0xFF202020.toInt())
        }

        Application.mount(root)

        val calls = mock.calls
        assertTrue(calls.any { it == "reset" }, "expected reset call")
        assertTrue(calls.any { it.startsWith("resizeWindow") }, "expected resizeWindow call")
        assertTrue(calls.any { it.startsWith("createNode(0)") }, "expected FlexBox createNode")
        assertTrue(calls.any { it.startsWith("setWidth") }, "expected setWidth call")
        assertTrue(calls.any { it.startsWith("setHeight") }, "expected setHeight call")
        assertTrue(calls.any { it.startsWith("setBgColor") }, "expected setBgColor call")
        assertTrue(calls.any { it.startsWith("setRoot") }, "expected setRoot call")
        assertTrue(calls.any { it == "commitFrame" }, "expected commitFrame call")
    }

    @Test
    fun `builds flexbox with text child`() {
        val mock = MockFfi()
        Runtime.init(mock)

        val root = flexBox {
            width(320f)
            height(200f)
            child(text("Hello, World!"))
        }

        Application.mount(root)

        val calls = mock.calls
        assertTrue(calls.any { it.startsWith("createNode(0)") }, "expected FlexBox createNode")
        assertTrue(calls.any { it.startsWith("createNode(1)") }, "expected Text createNode")
        assertTrue(calls.any { it.startsWith("addChild") }, "expected addChild call")
        assertTrue(calls.any { it.startsWith("setText") }, "expected setText call")
    }
}
