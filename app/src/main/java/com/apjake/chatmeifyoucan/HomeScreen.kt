package com.apjake.chatmeifyoucan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

@Composable
fun HomScreen(
    navigateToChat: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {

    var currentUsername by remember { mutableStateOf("") }
    var currentChannel by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        TextField(
            value = currentChannel,
            onValueChange = { currentChannel = it },
            placeholder = { Text("Enter channel id") }
        )
        TextField(
            value = currentUsername,
            onValueChange = { currentUsername = it },
            placeholder = { Text("Enter message") }
        )
        Button(
            onClick = {
                navigateToChat.invoke(currentChannel, currentUsername)
            },
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("Let's chat")
        }
    }

}