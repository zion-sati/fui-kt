package effindom.fui

interface Ffi {
    fun uiReset()
    fun uiCreateNode(nodeType: Int): Long
    fun uiDeleteNode(handle: Long)
    fun uiNodeAddChild(parent: Long, child: Long)
    fun uiNodeRemoveChild(parent: Long, child: Long)
    fun uiSetRoot(handle: Long)
    fun uiSetWidth(handle: Long, value: Float, unit: Int)
    fun uiSetHeight(handle: Long, value: Float, unit: Int)
    fun uiSetBgColor(handle: Long, color: Int)
    fun uiAllocUnmanagedBuffer(len: Int): Int
    fun uiFreeUnmanagedBuffer(ptr: Int)
    fun uiSetText(handle: Long, text: String)
    fun uiSetFont(handle: Long, fontId: Int, size: Float)
    fun uiSetTextColor(handle: Long, color: Int)
    fun uiSetPadding(handle: Long, top: Float, right: Float, bottom: Float, left: Float)
    fun uiSetFlexDirection(handle: Long, direction: Int)
    fun uiCommitFrame()
    fun uiResizeWindow(width: Float, height: Float)
    fun requestRender()
    fun getViewportWidth(): Float
    fun getViewportHeight(): Float
}
