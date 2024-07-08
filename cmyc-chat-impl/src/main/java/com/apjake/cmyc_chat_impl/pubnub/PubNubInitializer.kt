package com.apjake.cmyc_chat_impl.pubnub

import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.apjake.cmyc_chat_core.domain.AutoReply
import com.apjake.cmyc_chat_core.domain.ChannelState
import com.apjake.cmyc_chat_core.domain.Message
import com.apjake.cmyc_chat_core.domain.User
import com.apjake.cmyc_chat_core.`interface`.ChannelInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatFeature
import com.apjake.cmyc_chat_core.`interface`.ChatInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatStatePublisher
import com.apjake.cmyc_chat_impl.mapper.MessageMapper
import com.apjake.cmyc_chat_impl.util.createTextMessage
import com.pubnub.api.PubNub
import com.pubnub.api.UserId
import com.pubnub.api.v2.PNConfiguration
import com.pubnub.api.v2.entities.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class PubNubInitializer(
    private val errorHandler: PubNubErrorHandler,
    private val mapper: MessageMapper,
) : ChatInitializer, ChannelInitializer, ChatFeature, ChatStatePublisher {

    private lateinit var sender: User
    private val pubNub: PubNub by lazy {
        val config = PNConfiguration.builder(
            userId = UserId(sender.name),
            subscribeKey = "sub-c-9d8f7930-b1b4-4c60-8600-c9cb03475c0c",
        ).apply { publishKey = "pub-c-37969d64-2fae-4e59-80c8-f84224eb5521" }.build()
        PubNub.create(config)
    }
    private val statusListener = PubNubStatusListener()
    private val messageStreamer = PubNubMessageStreamer()

    private lateinit var lifecycleObserver: PubNubLifecycleObserver
    private lateinit var messageHandler: PubNubMessageHandler
    private lateinit var eventHandler: PubNubEventHandler

    override val channelState: Flow<ChannelState>
        get() = statusListener.channelState
    override val messagesStream: Flow<List<Message>>
        get() = messageStreamer.messageStream
    override val errorState: Flow<Exception>
        get() = errorHandler.errorState

    private lateinit var channel: Channel

    private val isOnForeground = MutableStateFlow(true)

    override fun init(context: Context, user: User) {
        this.sender = user
        lifecycleObserver = PubNubLifecycleObserver(
            onEnterBackground = ::onEnterBackground,
            onEnterForeground = ::onEnterForeground,
        )
        pubNub.addListener(statusListener)

        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
    }

    override fun setAutoReplies(autoReplies: List<AutoReply>) {}

    override fun subscribeToChannel(channelID: String) {
        channel = pubNub.channel(channelID)

        startListening()
    }

    override fun reconnect(channelID: String) {
        if (::channel.isInitialized) {
            pubNub.reconnect()
        } else {
            channel = pubNub.channel(channelID)
        }

        startListening()
    }

    override fun disconnect(channelID: String) {
        pubNub.disconnect()
    }

    override fun sendTextMessage(text: String) {
        val message = Message.createTextMessage(
            text = text,
            sender = sender,
            channelId = channel.name
        )
        messageStreamer.addOrUpdateMessage(message)

        messageHandler.sendTextMessage(mapper.toMessageDataItem(message))
    }

    private fun startListening() {
        messageHandler = PubNubMessageHandler(
            pubNub = pubNub,
            channel = channel,
            sender = sender,
            errorHandler = errorHandler,
        )
        eventHandler = PubNubEventHandler(
            pubNub = pubNub,
            channel = channel,
            onRequestReceiptStatus = ::getMyReceiptStatus,
            mapper = mapper,
            messageHandler = messageHandler,
            messageStreamer = messageStreamer,
            errorHandler = errorHandler,
        )
        eventHandler.listenAndUpdateMessageHistory()
        val subscription = channel.subscription()
        // making sure not to duplicate listeners
        // TODO: might need to test with different use-cases
        subscription.removeAllListeners()

        subscription.addListener(eventHandler)

        subscription.subscribe()
    }

    private fun onEnterBackground() {
        isOnForeground.update { false }
    }

    private fun onEnterForeground() {
        isOnForeground.update { true }

        // need to update other's message status of delivered to seen by me
        messageStreamer.messageStream.value.forEach { message ->
            if (!messageHandler.isFromMyself(message.sender.id) && message.status == Message.Status.Delivered) {
                messageHandler.updateMessageStatus(
                    message = mapper.toMessageDataItem(message),
                    status = getMyReceiptStatus().name
                )
            }
        }
    }

    private fun getMyReceiptStatus(): Message.Status {
        return if (isOnForeground.value) {
            Message.Status.Seen
        } else {
            Message.Status.Delivered
        }
    }

}
