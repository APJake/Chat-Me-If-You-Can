package com.apjake.cmyc_chat_impl.repo

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow

class PubNubErrorHandler(
    private val _errorState: MutableSharedFlow<Exception>
) {
    fun handleError(exception: Exception) {
        Log.e(TAG, "exception: $exception")
        _errorState.tryEmit(exception)
    }

    companion object {
        private const val TAG = "PubNubErrorHandler"
    }
}
