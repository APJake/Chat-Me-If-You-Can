package com.apjake.cmyc_chat_impl.pubnub

import com.pubnub.api.PubNubException
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber

class PubNubErrorHandler {

    val errorState = MutableSharedFlow<Exception>()

    fun onFailure(exception: PubNubException) {
        Timber.e("onFailure: $exception")
        // TODO: need to handle some errors and refactor later
        val error = Exception(exception.errorMessage)
        errorState.tryEmit(error)
    }

}
