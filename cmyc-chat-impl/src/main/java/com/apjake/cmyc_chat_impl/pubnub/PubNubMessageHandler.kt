package com.apjake.cmyc_chat_impl.pubnub

import com.apjake.cmyc_chat_core.domain.User
import com.apjake.cmyc_chat_impl.model.MessageDataItem
import com.apjake.cmyc_chat_impl.util.PubNubConstants
import com.pubnub.api.PubNub
import com.pubnub.api.models.consumer.message_actions.PNMessageAction
import com.pubnub.api.v2.entities.Channel
import timber.log.Timber

internal class PubNubMessageHandler(
    private val pubNub: PubNub,
    private val channel: Channel,
    private val sender: User,
    private val errorHandler: PubNubErrorHandler,
) {

    fun sendTextMessage(message: MessageDataItem) {
        channel.publish(
            message = message,
            shouldStore = true,
            usePost = true,
        ).async { result ->
            result
                .onFailure(errorHandler::onFailure)
                .onSuccess {
                    // no need to update the state here
                    Timber.d("Sent message at: $it")
                }
        }
    }

    fun sendImage() {
        // Implementation for sending image
    }

    fun updateMessageStatus(message: MessageDataItem, status: String) {
        // won't be sent if the message is from myself
        if (isFromMyself(message.sender.id)) return
        pubNub.addMessageAction(
            channel = channel.name,
            messageAction = PNMessageAction(
                type = PubNubConstants.TYPE_RECEIPT,
                value = status,
                messageTimetoken = message.publishedAt
            )
        ).async { result ->
            result
                .onFailure(errorHandler::onFailure)
                .onSuccess {
                    Timber.d("Sent receipt status at: $it")
                }
        }
    }

    fun isFromMyself(messageSenderId: String?): Boolean {
        return messageSenderId == sender.id
    }

}
