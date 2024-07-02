package com.apjake.cmyc_chat_impl.mapper

import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_impl.pubnub.MessageDto
import com.apjake.cmyc_chat_impl.pubnub.UserDto
import com.google.gson.Gson
import com.google.gson.JsonElement

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

fun JsonElement.toCMessage(): CMessage {
    val dto = Gson().fromJson(this, MessageDto::class.java)
    return dto.toMessage()
}

fun MessageDto.toMessage() = CMessage(
    id = id.orEmpty(),
    sender = sender?.toUser() ?: CUser("", ""),
    message = message.orEmpty(),
    type = type.orEmpty(),
    status = status.orEmpty(),
)

fun UserDto.toUser() = CUser(
    id = id.orEmpty(),
    name = name.orEmpty(),
)

fun CMessage.toDto(sender: CUser) = MessageDto(
    id = id,
    sender = sender.toDto(),
    message = message,
    type = type,
    status = status,
)

fun CUser.toDto() = UserDto(
    id = id,
    name = name,
)
