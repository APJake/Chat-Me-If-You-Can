package com.apjake.cmyc_chat_impl.model

data class MessageDataItem(
    val id: String,
    val channelId: String,
    val sender: UserDataItem,
    val type: String,
    val content: String,
    val status: String,
    val publishedAt: Long,
    val createdAt: Long,
)
