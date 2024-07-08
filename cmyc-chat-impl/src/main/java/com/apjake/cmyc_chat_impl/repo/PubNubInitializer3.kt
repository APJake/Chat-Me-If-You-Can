package com.apjake.cmyc_chat_impl.repo

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.apjake.cmyc_chat_core.domain.CChannelState
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_core.repo.CMYCFeature
import com.apjake.cmyc_chat_core.repo.CMYCInitializer
import com.apjake.cmyc_chat_core.repo.CMYCStatePublisher
import com.apjake.cmyc_chat_core.repo.ChannelInitializer
import com.apjake.cmyc_chat_impl.dto.UserDto
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.v2.PNConfiguration
import com.pubnub.api.v2.entities.Channel
import com.pubnub.api.v2.subscriptions.SubscriptionOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update

class PubNubInitializer3 : CMYCInitializer, ChannelInitializer, CMYCStatePublisher, CMYCFeature {

    private var pubNub: PubNub? = null
    private var channel: Channel? = null
    private lateinit var lifecycleObserver: PubNubLifecycleHandler

    private val _channelState = MutableStateFlow(CChannelState.Connecting)
    private val _messageStream = MutableStateFlow<CMessage?>(null)
    private val _messageHistory = MutableStateFlow<List<CMessage>>(emptyList())
    private val _errorState = MutableSharedFlow<Exception>()
    private val isOnForeground = MutableStateFlow(true)
    private val receiptStatus: String
        get() = if (isOnForeground.value) "Read" else "Delivered"

    private val sender: UserDto
        get() = UserDto(
            id = pubNub?.configuration?.userId?.value.orEmpty(),
            name = pubNub?.configuration?.userId?.value.orEmpty(),
        )

    override val channelState: Flow<CChannelState>
        get() = _channelState.asStateFlow()
    override val messageStream: Flow<CMessage>
        get() = _messageStream.asStateFlow().filterNotNull()
    override val messageHistory: Flow<List<CMessage>>
        get() = _messageHistory

    override val errorState: Flow<Exception>
        get() = _errorState.asSharedFlow()

    private lateinit var eventHandler: PubNubEventHandler
    private lateinit var messageHandler: PubNubMessageHandler
    private lateinit var stateHandler: PubNubStateHandler
    private lateinit var errorHandler: PubNubErrorHandler

    override fun init(context: Context, user: CUser) {
        val config = PNConfiguration.builder(
            userId = UserId(user.name),
            subscribeKey = "sub-c-9d8f7930-b1b4-4c60-8600-c9cb03475c0c",
        ).apply { publishKey = "pub-c-37969d64-2fae-4e59-80c8-f84224eb5521" }.build()
        pubNub = PubNub.create(config)

        eventHandler = PubNubEventHandler(
            pubNub!!,
            channel!!,
            sender,
            receiptStatus,
            _messageHistory,
            _messageStream
        )
        messageHandler =
            PubNubMessageHandler(pubNub!!, channel!!, sender, _messageHistory, _errorState)
        stateHandler = PubNubStateHandler(_channelState)
        errorHandler = PubNubErrorHandler(_errorState)

        pubNub?.addListener(eventHandler)
        pubNub?.addListener(stateHandler)

        lifecycleObserver = PubNubLifecycleHandler(::onEnterForeground, ::onEnterBackground)
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    private fun onEnterForeground() {
        isOnForeground.update { true }
    }

    private fun onEnterBackground() {
        isOnForeground.update { false }
    }

    override fun subscribeToChannel(channelID: String) {
        channel = pubNub?.channel(channelID)
//        eventHandler.listenMessageHistory()
        val options = SubscriptionOptions.receivePresenceEvents()
        val subscription = channel!!.subscription(options)
        subscription.removeAllListeners()
        subscription.addListener(eventHandler)
        subscription.subscribe()
    }

    override fun reconnect(channelID: String) {
        pubNub?.reconnect()
//        eventHandler.listenMessageHistory()
        val options = SubscriptionOptions.receivePresenceEvents()
        val subscription = channel!!.subscription(options)
        subscription.removeAllListeners()
        subscription.addListener(eventHandler)
        subscription.subscribe()
    }

    override fun disconnect(channelID: String) {
        pubNub?.disconnect()
    }

    override fun sendTextMessage(text: String) {
        messageHandler.sendTextMessage(text)
    }

    override fun sendImage() {
        messageHandler.sendImage()
    }

    companion object {
        private const val TAG = "PubNubInitializer"
    }
}
