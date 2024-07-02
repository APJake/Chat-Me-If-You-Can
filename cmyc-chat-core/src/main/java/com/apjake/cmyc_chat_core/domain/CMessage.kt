package com.apjake.cmyc_chat_core.domain

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

data class CMessage(
    val id: String,
    val sender: CUser,
    val message: String,
    val type: String,
    val status: String = "",
) {
    constructor(message: String) : this("", CUser("", ""), message, "", "")
}
