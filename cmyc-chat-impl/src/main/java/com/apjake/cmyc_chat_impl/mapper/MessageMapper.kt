package com.apjake.cmyc_chat_impl.mapper

import com.apjake.cmyc_chat_impl.model.MessageDataItem
import com.apjake.cmyc_chat_impl.model.UserDataItem
import com.apjake.cmyc_chat_core.domain.Message
import com.apjake.cmyc_chat_core.domain.User
import com.google.gson.Gson
import com.google.gson.JsonElement

internal class MessageMapper(
    private val gson: Gson,
) {

    fun toMessageStatus(value: String): Message.Status {
        return Message.Status.values().find {
            it.name.equals(value, true)
        } ?: Message.Status.Sending
    }

    fun toMessageType(value: String): Message.Type {
        return Message.Type.values().find {
            it.name.equals(value, true)
        } ?: Message.Type.Text
    }

    fun toMessageDataItem(value: JsonElement): MessageDataItem {
        val item = Gson().fromJson(value, MessageDataItem::class.java)
        return item
    }

    fun toMessage(value: JsonElement): Message {
        return toMessageDataItem(value).toDomain()
    }

    fun toUser(value: JsonElement): Message {
        val item = Gson().fromJson(value, MessageDataItem::class.java)
        return item.toDomain()
    }

    fun toMessageDataItem(value: Message) = with(value) {
        MessageDataItem(
            id = id,
            channelId = channelId,
            sender = toUserDataItem(sender),
            type = type.name,
            content = content,
            status = status.name,
            publishedAt = publishedAt,
            createdAt = createdAt,
        )
    }

    private fun toUserDataItem(value: User) = with(value) {
        UserDataItem(
            id = id,
            name = name,
            description = description,
            profileImage = profileImage,
        )
    }

    private fun MessageDataItem.toDomain() = Message(
        id = id,
        channelId = channelId,
        sender = sender.toDomain(),
        type = toMessageType(type),
        content = content,
        status = toMessageStatus(status),
        publishedAt = publishedAt,
        createdAt = createdAt,
    )

    private fun UserDataItem.toDomain() = User(
        id = id.orEmpty(),
        name = name.orEmpty(),
        description = description.orEmpty(),
        profileImage = profileImage.orEmpty(),
    )

}
