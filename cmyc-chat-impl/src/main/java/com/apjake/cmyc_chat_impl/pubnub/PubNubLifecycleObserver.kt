package com.apjake.cmyc_chat_impl.pubnub

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PubNubLifecycleObserver(
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

}
