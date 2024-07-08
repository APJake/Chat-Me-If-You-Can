package com.apjake.cmyc_chat_impl.di

import com.apjake.cmyc_chat_core.`interface`.ChannelInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatFeature
import com.apjake.cmyc_chat_core.`interface`.ChatInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatStatePublisher
import com.apjake.cmyc_chat_impl.mapper.MessageMapper
import com.apjake.cmyc_chat_impl.pubnub.PubNubErrorHandler
import com.apjake.cmyc_chat_impl.pubnub.PubNubInitializer
import com.google.gson.Gson

/**
 * Created by AP-Jake
 * on 05/07/2024
 */

private val myPubNub = PubNubInitializer(
    errorHandler = PubNubErrorHandler(),
    mapper = MessageMapper(Gson())
)

val chatInitializer: ChatInitializer
    get() = myPubNub

val chatFeature: ChatFeature
    get() = myPubNub

val chatStatePublisher: ChatStatePublisher
    get() = myPubNub

val channelInitializer: ChannelInitializer
    get() = myPubNub
