package com.apjake.cmyc_chat_impl.repo

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PubNubLifecycleObserver(
    private val pubNubInitializer2: PubNubInitializer2
) : DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        pubNubInitializer2.onEnterForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        pubNubInitializer2.onEnterBackground()
    }

}
