package com.apjake.cmyc_chat_impl.repo

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class PubNubLifecycleObserver(private val pubNubInitializer: PubNubInitializer) :
    DefaultLifecycleObserver {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        pubNubInitializer.onEnterForeground()
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        pubNubInitializer.onEnterBackground()
    }

}
