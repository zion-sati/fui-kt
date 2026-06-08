package effindom.fui

object WasmFfi : Ffi {
    override fun uiReset() = _uiReset()
    override fun uiCreateNode(nodeType: Int): Long = _uiCreateNode(nodeType)
    override fun uiDeleteNode(handle: Long) = _uiDeleteNode(handle)
    override fun uiNodeAddChild(parent: Long, child: Long) = _uiNodeAddChild(parent, child)
    override fun uiNodeRemoveChild(parent: Long, child: Long) = _uiNodeRemoveChild(parent, child)
    override fun uiSetRoot(handle: Long) = _uiSetRoot(handle)
    override fun uiSetWidth(handle: Long, value: Float, unit: Int) = _uiSetWidth(handle, value, unit)
    override fun uiSetHeight(handle: Long, value: Float, unit: Int) = _uiSetHeight(handle, value, unit)
    override fun uiSetBgColor(handle: Long, color: Int) = _uiSetBgColor(handle, color)
    override fun uiAllocUnmanagedBuffer(len: Int): Int = _uiAllocUnmanagedBuffer(len)
    override fun uiFreeUnmanagedBuffer(ptr: Int) = _uiFreeUnmanagedBuffer(ptr)
    override fun uiSetFont(handle: Long, fontId: Int, size: Float) = _uiSetFont(handle, fontId, size)
    override fun uiSetTextColor(handle: Long, color: Int) = _uiSetTextColor(handle, color)
    override fun uiSetPadding(handle: Long, top: Float, right: Float, bottom: Float, left: Float) =
        _uiSetPadding(handle, top, right, bottom, left)
    override fun uiSetFlexDirection(handle: Long, direction: Int) =
        _uiSetFlexDirection(handle, direction)
    override fun uiCommitFrame() = _uiCommitFrame()
    override fun uiResizeWindow(width: Float, height: Float) = _uiResizeWindow(width, height)
    override fun requestRender() = _requestRender()
    override fun getViewportWidth(): Float = _getViewportWidth()
    override fun getViewportHeight(): Float = _getViewportHeight()

    override fun uiSetText(handle: Long, text: String) {
        // Uses js() bridge → harness allocates in C++ heap and copies bytes.
        jsUiSetTextFromString(handle, text)
    }
}
