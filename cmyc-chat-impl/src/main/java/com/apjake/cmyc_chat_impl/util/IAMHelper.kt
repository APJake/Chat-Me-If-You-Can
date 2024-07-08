package com.apjake.cmyc_chat_impl.util

internal object IAMHelper {

    val currentTimestamp: Long
        get() = System.currentTimeMillis()

    fun generateUniqueId(vararg params: String): String {
        val ts = System.currentTimeMillis()
        return "${params.joinToString { "-" }}-$ts"
    }

}
