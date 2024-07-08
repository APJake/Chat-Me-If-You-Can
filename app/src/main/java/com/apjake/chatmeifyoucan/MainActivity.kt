package com.apjake.chatmeifyoucan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.apjake.chatmeifyoucan.ui.theme.ChatMeIfYouCanTheme
import com.apjake.cmyc_chat_impl.di.channelInitializer
import com.apjake.cmyc_chat_impl.di.chatFeature
import com.apjake.cmyc_chat_impl.di.chatInitializer
import com.apjake.cmyc_chat_impl.di.chatStatePublisher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            ChatMeIfYouCanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestNavHost(
                        initializer = chatInitializer,
                        channelInitializer = channelInitializer,
                        statePublisher = chatStatePublisher,
                        feature = chatFeature,
                        navController = rememberNavController(),
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

