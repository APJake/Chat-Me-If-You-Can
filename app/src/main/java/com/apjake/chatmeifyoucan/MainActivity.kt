package com.apjake.chatmeifyoucan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.apjake.chatmeifyoucan.ui.theme.ChatMeIfYouCanTheme
import com.apjake.cmyc_chat_core.domain.CUser
import com.apjake.cmyc_chat_core.repo.CMYCFeature
import com.apjake.cmyc_chat_core.repo.CMYCInitializer
import com.apjake.cmyc_chat_core.repo.CMYCStatePublisher
import com.apjake.cmyc_chat_core.repo.ChannelInitializer
import com.apjake.cmyc_chat_impl.repo.PubNubInitializer

private val myPubNub = PubNubInitializer()
private val initializer: CMYCInitializer = myPubNub
private val channelInitializer: ChannelInitializer = myPubNub
private val statePublisher: CMYCStatePublisher = myPubNub
private val feature: CMYCFeature = myPubNub

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ChatMeIfYouCanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestNavHost(
                        initializer = myPubNub,
                        channelInitializer = myPubNub,
                        statePublisher = myPubNub,
                        feature = myPubNub,
                        navController = rememberNavController(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

