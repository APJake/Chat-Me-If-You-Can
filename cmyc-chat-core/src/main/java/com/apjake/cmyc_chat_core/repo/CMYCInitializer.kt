package com.apjake.cmyc_chat_core.repo

import android.content.Context
import com.apjake.cmyc_chat_core.domain.CUser

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

interface CMYCInitializer {
    // context for platform related actions
    // user for pax || driver profile
    fun init(context: Context, user: CUser)
}
