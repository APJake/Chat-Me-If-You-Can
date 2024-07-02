package com.apjake.chatmeifyoucan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apjake.cmyc_chat_core.domain.CMessage
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_core.repo.CMYCFeature
import com.apjake.cmyc_chat_core.repo.CMYCInitializer
import com.apjake.cmyc_chat_core.repo.CMYCStatePublisher
import com.apjake.cmyc_chat_core.repo.ChannelInitializer

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

@Composable
fun ChatScreen(
    channel: String,
    name: String,
    initializer: CMYCInitializer,
    channelInitializer: ChannelInitializer,
    statePublisher: CMYCStatePublisher,
    feature: CMYCFeature,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        initializer.init(context, CUser("", name))
        channelInitializer.subscribeToChannel(channel)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                // Perform cleanup or other actions here
                println("ExampleScreen is being destroyed")
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            channelInitializer.disconnect(channel)
        }
    }
    val messages by statePublisher.messageHistory.collectAsState(emptyList())

    var currentMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                Text(
                    text = message.message,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            TextField(
                value = currentMessage,
                onValueChange = { currentMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter message") }
            )
            Button(
                onClick = {
                    if (currentMessage.isNotBlank()) {
                        feature.sendTextMessage(CMessage("", currentMessage))
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    ChatScreen()
}
