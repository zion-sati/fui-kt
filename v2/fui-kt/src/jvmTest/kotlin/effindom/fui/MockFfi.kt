package effindom.fui

class MockFfi : Ffi {
    val calls = mutableListOf<String>()

    override fun uiReset() { calls.add("reset") }
    override fun uiCreateNode(nodeType: Int): Long {
        calls.add("createNode($nodeType)")
        return (calls.size.toLong() shl 32) or 1L
    }
    override fun uiDeleteNode(handle: Long) { calls.add("deleteNode($handle)") }
    override fun uiNodeAddChild(parent: Long, child: Long) { calls.add("addChild($parent, $child)") }
    override fun uiNodeRemoveChild(parent: Long, child: Long) { calls.add("removeChild($parent, $child)") }
    override fun uiSetRoot(handle: Long) { calls.add("setRoot($handle)") }
    override fun uiSetWidth(handle: Long, value: Float, unit: Int) { calls.add("setWidth($handle, $value, $unit)") }
    override fun uiSetHeight(handle: Long, value: Float, unit: Int) { calls.add("setHeight($handle, $value, $unit)") }
    override fun uiSetBgColor(handle: Long, color: Int) { calls.add("setBgColor($handle, $color)") }
    override fun uiAllocUnmanagedBuffer(len: Int): Int {
        calls.add("allocUnmanaged($len)")
        return 1000 // dummy pointer
    }
    override fun uiFreeUnmanagedBuffer(ptr: Int) { calls.add("freeUnmanaged($ptr)") }
    override fun uiSetText(handle: Long, text: String) { calls.add("setText($handle, $text)") }
    override fun uiSetFont(handle: Long, fontId: Int, size: Float) { calls.add("setFont($handle, $fontId, $size)") }
    override fun uiSetTextColor(handle: Long, color: Int) { calls.add("setTextColor($handle, $color)") }
    override fun uiCommitFrame() { calls.add("commitFrame") }
    override fun uiResizeWindow(width: Float, height: Float) { calls.add("resizeWindow($width, $height)") }
    override fun requestRender() { calls.add("requestRender") }
    override fun getViewportWidth(): Float = 320f
    override fun getViewportHeight(): Float = 220f
}
