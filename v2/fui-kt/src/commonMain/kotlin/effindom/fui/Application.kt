package effindom.fui

object Application {
    private var mountedRoot: BuiltNode? = null

    fun mount(node: Node) {
        Runtime.ffi.uiReset()
        Runtime.ffi.uiResizeWindow(
            Runtime.ffi.getViewportWidth(),
            Runtime.ffi.getViewportHeight()
        )
        val root = node.build()
        Runtime.ffi.uiSetRoot(root.handle)
        Runtime.ffi.uiCommitFrame()
        mountedRoot = root
    }
}
