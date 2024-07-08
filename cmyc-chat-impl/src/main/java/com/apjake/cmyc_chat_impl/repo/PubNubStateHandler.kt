package com.apjake.cmyc_chat_impl.repo

import android.util.Log
import com.apjake.cmyc_chat_core.domain.CChannelState
import com.pubnub.api.PubNub
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.v2.callbacks.StatusListener
import kotlinx.coroutines.flow.MutableStateFlow

class PubNubStateHandler(
    private val _channelState: MutableStateFlow<CChannelState>,
) : StatusListener {

    override fun status(pubnub: PubNub, status: PNStatus) {
        when (status.category) {
            PNStatusCategory.PNConnectedCategory -> {
                Log.d(TAG, "Connected/Reconnected")
                _channelState.tryEmit(CChannelState.Connecting)
            }

            PNStatusCategory.PNDisconnectedCategory,
            PNStatusCategory.PNUnexpectedDisconnectCategory -> {
                Log.d(TAG, "Disconnected/Unexpectedly Disconnected")
                _channelState.tryEmit(CChannelState.Disconnected)
            }

            else -> {}
        }
    }

    companion object {
        private const val TAG = "PubNubStateHandler"
    }
}
