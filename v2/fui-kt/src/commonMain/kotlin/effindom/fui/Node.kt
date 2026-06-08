package effindom.fui

class BuiltNode(val handle: Long) {
    private val _children = mutableListOf<BuiltNode>()

    val children: List<BuiltNode> get() = _children
    var destroyed = false
        private set

    fun addChild(child: BuiltNode) {
        Runtime.ffi.uiNodeAddChild(handle, child.handle)
        _children.add(child)
    }

    fun destroy() {
        if (destroyed) return
        for (child in _children.asReversed()) {
            child.destroy()
        }
        _children.clear()
        if (handle != 0L) {
            Runtime.ffi.uiDeleteNode(handle)
        }
        destroyed = true
    }
}

abstract class Node {
    abstract fun build(): BuiltNode
}

enum class NodeType(val value: Int) {
    FlexBox(0),
    Text(1)
}

enum class SizeUnit(val value: Int) {
    Pixel(0),
    Auto(1),
    Star(2)
}

enum class FlexDirection(val value: Int) {
    Column(0),
    Row(1)
}

class FlexBox : Node() {
    private var width: Pair<Float, SizeUnit>? = null
    private var height: Pair<Float, SizeUnit>? = null
    private var bgColor: Int? = null
    private var padding: Quad<Float>? = null
    private var flexDirection: FlexDirection? = null
    private val children = mutableListOf<Node>()

    fun width(value: Float, unit: SizeUnit = SizeUnit.Pixel): FlexBox {
        width = Pair(value, unit)
        return this
    }

    fun height(value: Float, unit: SizeUnit = SizeUnit.Pixel): FlexBox {
        height = Pair(value, unit)
        return this
    }

    fun bgColor(color: Int): FlexBox {
        bgColor = color
        return this
    }

    fun padding(top: Float, right: Float, bottom: Float, left: Float): FlexBox {
        padding = Quad(top, right, bottom, left)
        return this
    }

    fun padding(all: Float): FlexBox = padding(all, all, all, all)

    fun flexDirection(direction: FlexDirection): FlexBox {
        flexDirection = direction
        return this
    }

    fun child(node: Node): FlexBox {
        children.add(node)
        return this
    }

    override fun build(): BuiltNode {
        val node = BuiltNode(Runtime.ffi.uiCreateNode(NodeType.FlexBox.value))
        width?.let { (v, u) -> Runtime.ffi.uiSetWidth(node.handle, v, u.value) }
        height?.let { (v, u) -> Runtime.ffi.uiSetHeight(node.handle, v, u.value) }
        bgColor?.let { Runtime.ffi.uiSetBgColor(node.handle, it) }
        padding?.let { Runtime.ffi.uiSetPadding(node.handle, it.a, it.b, it.c, it.d) }
        flexDirection?.let { Runtime.ffi.uiSetFlexDirection(node.handle, it.value) }
        for (child in children) {
            node.addChild(child.build())
        }
        return node
    }
}

class TextNode(private val content: String) : Node() {
    private var font: Pair<Int, Float>? = null
    private var textColor: Int? = null

    fun font(fontId: Int, size: Float): TextNode {
        font = Pair(fontId, size)
        return this
    }

    fun textColor(color: Int): TextNode {
        textColor = color
        return this
    }

    override fun build(): BuiltNode {
        val node = BuiltNode(Runtime.ffi.uiCreateNode(NodeType.Text.value))
        Runtime.ffi.uiSetText(node.handle, content)
        font?.let { (id, size) -> Runtime.ffi.uiSetFont(node.handle, id, size) }
        textColor?.let { Runtime.ffi.uiSetTextColor(node.handle, it) }
        return node
    }
}

// Internal value type for padding quad
private data class Quad<T>(val a: T, val b: T, val c: T, val d: T)

// Factory functions

fun flexBox(init: FlexBox.() -> Unit = {}): FlexBox {
    return FlexBox().apply(init)
}

fun text(content: String, init: TextNode.() -> Unit = {}): TextNode {
    return TextNode(content).apply(init)
}

fun row(init: FlexBox.() -> Unit = {}): FlexBox {
    return FlexBox().flexDirection(FlexDirection.Row).apply(init)
}

fun column(init: FlexBox.() -> Unit = {}): FlexBox {
    return FlexBox().flexDirection(FlexDirection.Column).apply(init)
}
