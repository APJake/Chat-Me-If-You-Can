package com.apjake.cmyc_chat_impl.repo

import android.util.Log
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_impl.mapper.toMessage
import com.apjake.cmyc_chat_impl.dto.MessageDto
import com.apjake.cmyc_chat_impl.dto.UserDto
import com.apjake.cmyc_chat_impl.util.Helper
import com.google.gson.JsonObject
import com.pubnub.api.PubNub
import com.pubnub.api.v2.entities.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PubNubMessageHandler(
    private val pubNub: PubNub,
    private val channel: Channel,
    private val sender: UserDto,
    private val _messageHistory: MutableStateFlow<List<CMessage>>,
    private val _errorState: MutableSharedFlow<Exception>
) {

    fun sendTextMessage(text: String) {
        val message = Helper.generateMsgID(
            MessageDto(
                id = "",
                sender = sender,
                message = text,
                status = "Sending",
                type = "Text",
                timeToken = -1,
            )
        )
        updateMessageFlow(message = message.toMessage())
        val meta = JsonObject().apply {
            addProperty("id", pubNub.configuration.userId.value)
        }
        channel.publish(
            message = message,
            meta = meta,
            shouldStore = true,
            usePost = true,
        ).async { result ->
            result.onFailure { exception ->
                Log.e(TAG, "exception: $exception")
                _errorState.tryEmit(exception)
            }
            result.onSuccess {
                Log.d(TAG, "Sent message at: $it")
            }
        }
    }

    fun sendImage() {
        // Implementation for sending image
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
        private const val TAG = "PubNubMessageHandler"
    }
}
