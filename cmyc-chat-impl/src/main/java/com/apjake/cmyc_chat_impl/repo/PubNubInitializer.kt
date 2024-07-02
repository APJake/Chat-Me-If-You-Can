package com.apjake.cmyc_chat_impl.repo

import android.content.Context
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.apjake.cmyc_chat_core.domain.CChannelState
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_core.repo.CMYCFeature
import com.apjake.cmyc_chat_core.repo.CMYCInitializer
import com.apjake.cmyc_chat_core.repo.CMYCStatePublisher
import com.apjake.cmyc_chat_core.repo.ChannelInitializer
import com.apjake.cmyc_chat_impl.mapper.toCMessage
import com.apjake.cmyc_chat_impl.mapper.toMessage
import com.apjake.cmyc_chat_impl.pubnub.MessageDto
import com.apjake.cmyc_chat_impl.pubnub.UserDto
import com.apjake.cmyc_chat_impl.util.Helper
import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.enums.PNStatusCategory
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
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
    private lateinit var lifecycleObserver: PubNubLifecycleObserver

    private val _channelState = MutableStateFlow(CChannelState.Connecting)
    private val _messageStream = MutableStateFlow<CMessage?>(null)
    private val _messageHistory = MutableStateFlow<List<CMessage>>(emptyList())
    private val _errorState = MutableSharedFlow<Exception>()

    private val sender: UserDto
        get() = UserDto(
            id = config?.userId?.value.orEmpty(),
            name = config?.userId?.value.orEmpty(),
        )

    override val channelState: Flow<CChannelState>
        get() = _channelState.asStateFlow()
    override val messageStream: Flow<CMessage>
        get() = _messageStream.asStateFlow().filterNotNull()
    override val messageHistory: Flow<List<CMessage>>
        get() = _messageHistory

    override val errorState: Flow<Exception>
        get() = _errorState.asSharedFlow()

    private val pubNubEventListener = object : EventListener {

        override fun message(pubnub: PubNub, result: PNMessageResult) {
            if (result.channel == channel!!.name) {
                Log.d(TAG, "Received message ${result.message.asJsonObject}")
                Log.i(TAG, "time token ${result.timetoken}")
                Log.i(TAG, "userMetadata ${result.userMetadata?.asJsonObject}")
                Log.i(TAG, "publisher ${result.publisher}")
                val message = result.message.toCMessage()
                _messageStream.tryEmit(message)
                if (message.sender.id != sender.id) {
                    _messageHistory.update { list ->
                        (list + message).map { message ->
                            message.copy(
                                status = "Read"
                            )
                        }
                    }
                    sendLastMessageAsSignal(message)
                } else {
                    updateMessageFlow(
                        message.copy(
                            status = "Sent"
                        )
                    )
                }
            }
        }

        override fun signal(pubnub: PubNub, result: PNSignalResult) {
            Log.d(TAG, "Received signal ${result}")
            if (result.publisher == sender.id) {
                return
            }
            _messageHistory.update { list ->
                list.map { message ->
                    message.copy(
                        status = result.message.asString
                    )
                }
            }
        }

    }

    fun onEnterForeground() {
        Log.v(TAG, "onEnterForeground")
    }

    fun onEnterBackground() {
        Log.v(TAG, "onEnterBackground")
    }

    private fun updateMessageFlow(message: CMessage) {
        _messageHistory.update { list ->
            val index = list.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                // just update
                list.mapIndexed { i, item ->
                    if (i == index) {
                        message
                    } else {
                        item
                    }
                }
            } else {
                list + message
            }
        }
    }

    override fun init(context: Context, user: CUser) {
        config = PNConfiguration.builder(
            userId = UserId(user.name),
            subscribeKey = "sub-c-9d8f7930-b1b4-4c60-8600-c9cb03475c0c",
        ).apply { publishKey = "pub-c-37969d64-2fae-4e59-80c8-f84224eb5521" }.build()
        config?.let {
            pubNub = PubNub.create(it)
            listenChannelState()
        }
        lifecycleObserver = PubNubLifecycleObserver(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
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

    private fun sendLastMessageAsSignal(message: CMessage) {
        if (channel == null || pubNub == null) {
            return
        }
        if (message.sender.id == sender.id) {
            // No need to send
            return
        }
        pubNub?.signal(
            channel = channel!!.name,
            message = "read",
        )?.async { result ->
            result.onFailure { exception ->
                Log.e(TAG, "exception: $exception")
                _errorState.tryEmit(exception)
            }
            result.onSuccess {
                Log.d(TAG, "Sent signal at: $it")
            }
        }
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
            result.onFailure { exception ->
                Log.e(TAG, "exception: $exception")
                _errorState.tryEmit(exception)
            }
            result.onSuccess { historyResult ->
                Log.d(TAG, "Received message history: ${historyResult.messages}")
                var hasRead = false
                val messageHistoryList = historyResult.messages.reversed().map {
                    val msg = it.entry.toCMessage()
                    if (msg.sender.id != sender.id) {
                        hasRead = true
                    }
                    val status = when {
                        hasRead -> "Read"
                        msg.status == "Sending" -> "Sent"
                        else -> msg.status
                    }
                    msg.copy(
                        status = status
                    )
                }
                _messageHistory.update { messageHistoryList.reversed() }

                historyResult.messages.lastOrNull()?.let {
                    sendLastMessageAsSignal(it.entry.toCMessage())
                }
            }
        }
    }

    private fun listenSubscribers() {
        pubNub?.hereNow()
    }

    override fun subscribeToChannel(channelID: String) {
        channel = pubNub?.channel(channelID)

        startListening()
    }

    override fun reconnect(channelID: String) {
        pubNub?.reconnect()

        startListening()
    }

    private fun startListening() {
        if (channel == null) {
            return
        }
        listenMessageHistory()
        val options = SubscriptionOptions.receivePresenceEvents()
        val subscription = channel!!.subscription(options)
        subscription.removeAllListeners()

        subscription.addListener(pubNubEventListener)

        subscription.subscribe()
    }

    override fun disconnect(channelID: String) {
        pubNub?.disconnect()
    }

    override fun sendTextMessage(text: String) {
        val message = Helper.generateMsgID(
            MessageDto(
                id = "",
                sender = sender,
                message = text,
                status = "Sending",
                type = "Text",
            )
        )
        updateMessageFlow(message = message.toMessage())
        val meta = JsonObject().apply {
            addProperty("id", config?.userId?.value)
        }
        channel?.publish(
            message = message,
            meta = meta,
            shouldStore = true,
            usePost = true,
        )?.async { result ->
            result.onFailure { exception ->
                Log.e(TAG, "exception: $exception")
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
