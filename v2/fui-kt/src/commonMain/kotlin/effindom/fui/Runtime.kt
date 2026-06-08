package effindom.fui

object Runtime {
    lateinit var ffi: Ffi

    fun init(ffi: Ffi) {
        this.ffi = ffi
    }
}
