package com.apjake.cmyc_chat_impl.repo

import android.content.Context
import android.util.Log
import com.apjake.cmyc_chat_core.domain.CChannelState
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_core.repo.CMYCFeature
import com.apjake.cmyc_chat_core.repo.CMYCInitializer
import com.apjake.cmyc_chat_core.repo.CMYCStatePublisher
import com.apjake.cmyc_chat_core.repo.ChannelInitializer
import com.apjake.cmyc_chat_impl.mapper.toCMessage
import com.apjake.cmyc_chat_impl.mapper.toDto
import com.apjake.cmyc_chat_impl.mapper.toJsonObject
import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.v2.PNConfiguration
import com.pubnub.api.v2.callbacks.EventListener
import com.pubnub.api.v2.callbacks.StatusListener
import com.pubnub.api.v2.entities.Channel
import com.pubnub.api.v2.subscriptions.SubscriptionOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

class PubNubInitializer : CMYCInitializer, ChannelInitializer, CMYCStatePublisher, CMYCFeature {

    private var config: PNConfiguration? = null
    private var pubNub: PubNub? = null
    private var channel: Channel? = null

    private val _channelState = MutableStateFlow(CChannelState.Connecting)
    private val _messageStream = MutableStateFlow<CMessage?>(null)
    private val _messageHistory = MutableStateFlow<List<CMessage>>(emptyList())
    private val _errorState = MutableSharedFlow<Exception>()

    override val channelState: Flow<CChannelState>
        get() = _channelState.asStateFlow()
    override val messageStream: Flow<CMessage>
        get() = _messageStream.asStateFlow().filterNotNull()
    override val messageHistory: Flow<List<CMessage>>
        get() = _messageHistory

    override val errorState: Flow<Exception>
        get() = _errorState.asSharedFlow()

    override fun init(context: Context, user: CUser) {
        config = PNConfiguration.builder(
            userId = UserId(user.name),
            subscribeKey = "sub-c-9d8f7930-b1b4-4c60-8600-c9cb03475c0c",
        ).apply { publishKey = "pub-c-37969d64-2fae-4e59-80c8-f84224eb5521" }.build()
        config?.let {
            pubNub = PubNub.create(it)
            listenChannelState()
        }
    }

    private fun listenChannelState() {
        if (pubNub == null) return
        pubNub!!.addListener(object : StatusListener {
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
        })
    }

    private fun listenMessage() {
        if (channel == null) {
            return
        }
        val options = SubscriptionOptions.receivePresenceEvents()
        val subscription = channel!!.subscription(options)
        subscription.removeAllListeners()
        subscription.addListener(object : EventListener {
            override fun message(pubnub: PubNub, result: PNMessageResult) {
                if (result.channel == channel!!.name) {
                    Log.d(TAG, "Received message ${result.message.asJsonObject}")
                    Log.i(TAG, "time token ${result.timetoken}")
                    Log.i(TAG, "userMetadata ${result.userMetadata?.asJsonObject}")
                    Log.i(TAG, "publisher ${result.publisher}")
                    _messageStream.tryEmit(
                        result.message.toCMessage()
                    )
                    _messageHistory.update {
                        it + result.message.asJsonObject.toCMessage()
                    }
                }
            }
        })
        subscription.subscribe()
    }

    private fun listenMessageHistory() {
        if (channel == null || pubNub == null) {
            return
        }
        pubNub?.history(
            channel = channel!!.name,
            reverse = true,
            count = 100
        )?.async { result ->
            result.onFailure {
                _errorState.tryEmit(it)
            }
            result.onSuccess { historyResult ->
                Log.d(TAG, "Received message history: ${historyResult.messages}")
                _messageHistory.tryEmit(historyResult.messages.map {
                    it.entry.toCMessage()
                })
            }
        }
    }

    private fun listenSubscribers() {
        pubNub?.hereNow()
    }

    override fun subscribeToChannel(channelID: String) {
        channel = pubNub?.channel(channelID)
        listenMessage()
        listenMessageHistory()
        listenSubscribers()
    }

    override fun reconnect(channelID: String) {
        pubNub?.reconnect()
        listenMessage()
        listenMessageHistory()
    }

    override fun disconnect(channelID: String) {
        pubNub?.disconnect()
    }

    override fun sendTextMessage(text: CMessage) {
        val messageDto = text.toDto(config?.userId?.value.orEmpty())
        val meta = JsonObject().apply {
            addProperty("id", config?.userId?.value)
        }
        channel?.publish(
            message = messageDto,
            meta = meta,
            shouldStore = true,
            usePost = true,
        )?.async { result ->
            result.onFailure { exception ->
                _errorState.tryEmit(exception)
            }
            result.onSuccess {
                Log.d(TAG, "Sent message at: $it")
            }
        }
    }

    override fun sendImage() {
    }

    companion object {
        private const val TAG = "MyPubNub"
    }
}
