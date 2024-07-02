package com.apjake.cmyc_chat_core.repo

import com.apjake.cmyc_chat_core.domain.CMessage

interface CMYCFeature {
    fun sendTextMessage(text: CMessage)
    fun sendImage()
}
