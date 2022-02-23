package dev.helight.hopper.data

class SwapSynchronizer<T>(var a: T? = null) {

    var b: T? = null

    @Volatile
    private var switch = true

    fun read(): T = when(switch) {
        true -> a
        false -> b
    }!!

    @Synchronized
    fun push(value: T) {
        val next = !switch
        when (next) {
            true -> a = value
            false -> b = value
        }
        switch = next
    }

}