package com.apjake.cmyc_chat_impl.repo

import android.util.Log
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_impl.mapper.toCMessage
import com.apjake.cmyc_chat_impl.dto.UserDto
import com.pubnub.api.PubNub
import com.pubnub.api.models.consumer.message_actions.PNMessageAction
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.api.v2.callbacks.EventListener
import com.pubnub.api.v2.entities.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PubNubEventHandler(
    private val pubNub: PubNub,
    private val channel: Channel,
    private val sender: UserDto,
    private val receiptStatus: String,
    private val _messageHistory: MutableStateFlow<List<CMessage>>,
    private val _messageStream: MutableStateFlow<CMessage?>
) : EventListener {

    override fun messageAction(pubnub: PubNub, result: PNMessageActionResult) {
        Log.d(TAG, "Received signal $result")
        if (result.messageAction.type == "receipt") {
            _messageHistory.update { list ->
                list.map { message ->
                    if (result.messageAction.messageTimetoken == message.timeToken) {
                        message.copy(status = result.messageAction.value)
                    } else {
                        message
                    }
                }
            }
        }
    }

    override fun message(pubnub: PubNub, result: PNMessageResult) {
        if (result.channel == channel.name) {
            Log.d(TAG, "Received message ${result.message.asJsonObject}")
            Log.i(TAG, "time token ${result.timetoken}")
            Log.i(TAG, "userMetadata ${result.userMetadata?.asJsonObject}")
            Log.i(TAG, "publisher ${result.publisher}")
            val message = result.message.toCMessage().copy(
                timeToken = result.timetoken ?: -1
            )
            _messageStream.tryEmit(message)
            if (message.sender.id != sender.id) {
                _messageHistory.update { list ->
                    list + message.copy(status = receiptStatus)
                }
                sendLastMessageAsSignal(message)
            } else {
                updateMessageFlow(message.copy(status = "Sent"))
            }
        }
    }

    private fun sendLastMessageAsSignal(message: CMessage) {
        if (message.sender.id == sender.id) return
        pubNub.addMessageAction(
            channel = channel.name,
            messageAction = PNMessageAction(
                type = "receipt",
                value = receiptStatus,
                messageTimetoken = message.timeToken
            )
        ).async { result ->
            result.onFailure { exception ->
                Log.e(TAG, "Receipt exception: $exception")
            }
            result.onSuccess {
                Log.d(TAG, "Sent receipt at: $it")
            }
        }
    }

    private fun updateMessageFlow(message: CMessage) {
        _messageHistory.update { list ->
            val index = list.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                list.mapIndexed { i, item ->
                    if (i == index) message else item
                }
            } else {
                list + message
            }
        }
    }

    companion object {
        private const val TAG = "PubNubEventHandler"
    }
}
