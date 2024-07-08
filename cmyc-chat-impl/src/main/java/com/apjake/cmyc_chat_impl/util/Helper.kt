package com.apjake.cmyc_chat_impl.util

import com.apjake.cmyc_chat_impl.dto.MessageDto

/**
 * Created by AP-Jake
 * on 02/07/2024
 */

object Helper {

    fun generateMsgID(message: MessageDto): MessageDto {
        val ts = System.currentTimeMillis()
        val hash = message.message.hashCode()
        return message.copy(
            id = "${message.sender}-$hash-$ts",
        )
    }

}
