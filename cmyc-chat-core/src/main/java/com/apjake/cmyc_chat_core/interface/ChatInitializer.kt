package com.apjake.cmyc_chat_core.`interface`

import android.content.Context
import com.apjake.cmyc_chat_core.domain.AutoReply
import com.apjake.cmyc_chat_core.domain.User

interface ChatInitializer {

    // context for platform related actions
    // user for pax || driver profile
    fun init(context: Context, user: User)

    fun setAutoReplies(autoReplies: List<AutoReply>)
}
