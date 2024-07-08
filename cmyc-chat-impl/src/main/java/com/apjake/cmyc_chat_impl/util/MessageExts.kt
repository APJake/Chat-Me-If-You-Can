package com.apjake.cmyc_chat_impl.util

import com.apjake.cmyc_chat_core.domain.Message
import com.apjake.cmyc_chat_core.domain.User

fun Message.Companion.createTextMessage(
    text: String,
    sender: User,
    channelId: String,
) = Message(
    id = IAMHelper.generateUniqueId(
        sender.id.hashCode().toString(),
        text.hashCode().toString()
    ),
    channelId = channelId,
    sender = sender,
    type = Message.Type.Text,
    content = text,
    status = Message.Status.Sending,
    publishedAt = -1,
    createdAt = IAMHelper.currentTimestamp,
)
