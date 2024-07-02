package com.apjake.cmyc_chat_core.repo

import com.apjake.cmyc_chat_core.domain.CChannelState
import com.apjake.cmyc_chat_core.domain.CMessage
import kotlinx.coroutines.flow.Flow


interface CMYCStatePublisher {
    // related to channel connection events only
    val channelState: Flow<CChannelState>

    // related to incoming messages
    val messageStream: Flow<CMessage>

    val messageHistory: Flow<List<CMessage>>

    // related to errors arised from sending messages, sending files, and resolving files etc
    val errorState: Flow<Exception>
}
