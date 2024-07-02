package com.apjake.cmyc_chat_impl.pubnub

/**
 * Created by AP-Jake
 * on 02/07/2024
 */

data class MessageDto(
    val id: String,
    val sender: String,
    val message: String,
)
