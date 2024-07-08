package com.apjake.cmyc_chat_impl.pubnub

import com.apjake.cmyc_chat_core.domain.ChannelState
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.v2.callbacks.StatusListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class PubNubStatusListener : StatusListener {

    private val _channelState = MutableStateFlow(ChannelState.Connecting)
    val channelState = _channelState.asStateFlow()

    override fun status(pubnub: PubNub, status: PNStatus) {
        when (status.category) {
            PNStatusCategory.PNConnectedCategory -> {
                Timber.d("Connected/Reconnected")
                _channelState.tryEmit(ChannelState.Connecting)
            }

            PNStatusCategory.PNDisconnectedCategory,
            PNStatusCategory.PNUnexpectedDisconnectCategory -> {
                Timber.d("Disconnected/Unexpectedly Disconnected")
                _channelState.tryEmit(ChannelState.Disconnected)
            }

            else -> {}
        }
    }

}
