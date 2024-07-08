package com.apjake.cmyc_chat_core.domain

data class Message(
    val id: String,
    val channelId: String,
    val sender: User,
    val type: Type,
    val content: String,
    val status: Status,
    val publishedAt: Long,
    val createdAt: Long,
) {
    enum class Status {
        Sending, Sent, Delivered, Seen, Failed;
    }
    enum class Type {
        Text, Image, File, Audio;
    }

    companion object
}
