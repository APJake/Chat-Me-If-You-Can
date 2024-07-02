package com.apjake.cmyc_chat_impl.mapper

import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_impl.pubnub.MessageDto
import com.google.gson.JsonObject

/**
 * Created by AP-Jake
 * on 26/06/2024
 */
 
fun JsonObject.toCMessage() = CMessage(
    id = this["id"].asString,
    message = this["message"].asString,
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
