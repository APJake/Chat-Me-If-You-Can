package com.apjake.cmyc_chat_core.`interface`

interface ChannelInitializer {
    fun subscribeToChannel(channelID: String)
    fun reconnect(channelID: String)
    fun disconnect(channelID: String)
}
