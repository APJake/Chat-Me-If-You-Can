package com.apjake.cmyc_chat_impl.repo

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PubNubLifecycleHandler(
    private val onEnterForeground: () -> Unit,
    private val onEnterBackground: () -> Unit,
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        onEnterForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)

        onEnterBackground()
    }

    companion object {
        private const val TAG = "PubNubLifecycleHandler"
    }
}
