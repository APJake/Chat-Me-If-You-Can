package com.apjake.cmyc_chat_core.`interface`

import com.apjake.cmyc_chat_core.domain.ChannelState
import com.apjake.cmyc_chat_core.domain.Message
import kotlinx.coroutines.flow.Flow

interface ChatStatePublisher {
    // related to channel connection events only
    val channelState: Flow<ChannelState>

    // related to incoming messages
    val messagesStream: Flow<List<Message>>

    // related to errors arisen from sending messages, sending files, and resolving files etc
    val errorState: Flow<Exception>
}
