package com.apjake.cmyc_chat_core.repo

interface CMYCFeature {
    fun sendTextMessage(text: String)
    fun sendImage()
}
