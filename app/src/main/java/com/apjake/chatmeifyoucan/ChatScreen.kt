package com.apjake.chatmeifyoucan

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.apjake.cmyc_chat_core.domain.User
import com.apjake.cmyc_chat_core.`interface`.ChannelInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatFeature
import com.apjake.cmyc_chat_core.`interface`.ChatInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatStatePublisher

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

@Composable
fun ChatScreen(
    channel: String,
    name: String,
    initializer: ChatInitializer,
    channelInitializer: ChannelInitializer,
    statePublisher: ChatStatePublisher,
    feature: ChatFeature,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        initializer.init(context, User(name, name, name, ""))
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
    val messages by statePublisher.messagesStream.collectAsState(emptyList())

    var currentMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 8.dp),
            reverseLayout = true
        ) {
            items(messages) { message ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = message.sender.name,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Thin,
                            ),
                        )
                        Text(
                            text = message.content,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        text = message.status.name,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic,
                        ),
                    )
                }
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
                        feature.sendTextMessage(currentMessage)
                        currentMessage = ""
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
