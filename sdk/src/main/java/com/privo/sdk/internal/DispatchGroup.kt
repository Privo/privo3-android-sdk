package com.privo.sdk.internal

// Use coroutines instead
class DispatchGroup (private var groupSize: Int, private val onFinish: () -> Unit) {

    fun leave() {
        groupSize -= 1
        if (groupSize == 0) {
            this.onFinish()
        }
    }
}