package dev.helight.hopper.data

import okhttp3.internal.notifyAll
import okhttp3.internal.wait

class KeyLocks<K> {

    private val list: MutableList<K> = mutableListOf()

    @Synchronized
    @Throws(InterruptedException::class)
    fun lock(key: K) {
        while (list.contains(key)) {
            wait()
        }
        list.add(key)
    }

    @Synchronized
    fun unlock(key: K) {
        list.remove(key)
        notifyAll()
    }

    @Synchronized
    fun isLocked(key: K) = list.contains(key)
}