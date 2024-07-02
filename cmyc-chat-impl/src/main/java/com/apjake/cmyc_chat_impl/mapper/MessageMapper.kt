package com.apjake.cmyc_chat_impl.mapper

import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_impl.pubnub.MessageDto
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * Created by AP-Jake
 * on 26/06/2024
 */
 
fun JsonElement.toCMessage(): CMessage {
    val dto = Gson().fromJson(this, MessageDto::class.java)
    return dto.toMessage()
}

fun MessageDto.toMessage() = CMessage(
    id = id,
    message = message
)

fun CMessage.toJsonObject() = JsonObject().apply {
    addProperty("id", id)
    addProperty("message", message)
}

fun CMessage.toDto(sender: String) = MessageDto(
    id = id,
    sender = sender,
    message = message
)
