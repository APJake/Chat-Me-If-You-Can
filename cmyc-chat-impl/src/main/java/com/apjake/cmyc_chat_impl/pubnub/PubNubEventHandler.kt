package com.apjake.cmyc_chat_impl.pubnub

import com.apjake.cmyc_chat_impl.mapper.MessageMapper
import com.apjake.cmyc_chat_impl.util.PubNubConstants
import com.apjake.cmyc_chat_core.domain.Message
import com.pubnub.api.PubNub
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.v2.callbacks.EventListener
import com.pubnub.api.v2.entities.Channel
import timber.log.Timber

internal class PubNubEventHandler(
    private val pubNub: PubNub,
    private val channel: Channel,
    private val onRequestReceiptStatus: () -> Message.Status,
    private val mapper: MessageMapper,
    private val errorHandler: PubNubErrorHandler,
    private val messageHandler: PubNubMessageHandler,
    private val messageStreamer: PubNubMessageStreamer,
) : EventListener {

    override fun message(pubnub: PubNub, result: PNMessageResult) {
        if (result.channel != channel.name) return
        Timber.d("Received message (${result.timetoken}): ${result.message}")

        // need to update the TimeToken as published time
        val message = mapper.toMessage(result.message).copy(
            publishedAt = result.timetoken ?: -1
        )

        if (messageHandler.isFromMyself(message.sender.id)) {
            messageStreamer.addOrUpdateMessage(
                message.copy(status = Message.Status.Sent)
            )
        } else {
            // need to send acknowledgement back if new message is from the other
            messageHandler.updateMessageStatus(
                message = mapper.toMessageDataItem(message),
                status = onRequestReceiptStatus().name
            )
            messageStreamer.addOrUpdateMessage(
                message.copy(status = onRequestReceiptStatus())
            )
        }
    }

    override fun messageAction(pubnub: PubNub, result: PNMessageActionResult) {
        Timber.d("Received signal $result")
        if (result.messageAction.type == PubNubConstants.TYPE_RECEIPT) {
            messageStreamer.mapMessage { message ->
                // message status will be updated if there is receipt type action
                if (result.messageAction.messageTimetoken == message.publishedAt) {
                    message.copy(
                        status = mapper.toMessageStatus(result.messageAction.value)
                    )
                } else {
                    message
                }
            }
        }
    }

    fun listenAndUpdateMessageHistory() {
        pubNub.history(
            channel = channel.name,
            reverse = true,
            count = PubNubConstants.MAX_MESSAGE_HISTORY_COUNT
        ).async { result ->
            result
                .onFailure(errorHandler::onFailure)
                .onSuccess { historyResult ->
                    Timber.d("Received message history: " + historyResult.messages)
                    var hasSeen = false
                    val messageHistoryList = historyResult.messages.reversed().map {
                        val message = mapper.toMessage(it.entry)
                        if (messageHandler.isFromMyself(message.sender.id)) {
                            hasSeen = true
                        }
                        message.copy(
                            status = when {
                                hasSeen -> Message.Status.Seen
                                message.status == Message.Status.Sending -> Message.Status.Sent
                                else -> message.status
                            }
                        )
                    }
                    messageStreamer.replaceAll(messageHistoryList)
                    historyResult.messages.lastOrNull()?.let {
                        messageHandler.updateMessageStatus(
                            message = mapper.toMessageDataItem(it.entry),
                            status = onRequestReceiptStatus().name
                        )
                    }
                }
        }
    }

}
