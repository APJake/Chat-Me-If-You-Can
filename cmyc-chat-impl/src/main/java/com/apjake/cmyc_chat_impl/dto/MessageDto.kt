package com.apjake.cmyc_chat_impl.dto

/**
 * Created by AP-Jake
 * on 02/07/2024
 */

data class MessageDto(
    val id: String?,
    val sender: UserDto?,
    val type: String?,
    val message: String?,
    val status: String?,
    val timeToken: Long?,
)
