package com.apjake.cmyc_chat_impl.pubnub

import com.apjake.cmyc_chat_core.domain.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PubNubMessageStreamer {

    private val _messageStream = MutableStateFlow(emptyList<Message>())
    val messageStream = _messageStream.asStateFlow()

    fun addOrUpdateMessage(message: Message) {
        _messageStream.update { list ->
            val index = list.indexOfFirst { it.id == message.id }
            if (index >= 0) {
                // just update the item if already exists
                list.mapIndexed { i, item ->
                    if (i == index) message else item
                }
            } else {
                // add as new item
                listOf(message) + list
            }
        }
    }

    fun replaceAll(list: List<Message>) {
        _messageStream.update { list }
    }

    fun mapMessage(onMap: (Message) -> Message) {
        _messageStream.update { list ->
            list.map(onMap)
        }
    }

}
