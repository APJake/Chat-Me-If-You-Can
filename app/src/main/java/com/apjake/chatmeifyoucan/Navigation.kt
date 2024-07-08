package com.apjake.chatmeifyoucan

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.apjake.cmyc_chat_core.`interface`.ChannelInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatFeature
import com.apjake.cmyc_chat_core.`interface`.ChatInitializer
import com.apjake.cmyc_chat_core.`interface`.ChatStatePublisher

/**
 * Created by AP-Jake
 * on 26/06/2024
 */

@Composable
fun TestNavHost(
    initializer: ChatInitializer,
    channelInitializer: ChannelInitializer,
    statePublisher: ChatStatePublisher,
    feature: ChatFeature,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400),
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400),
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400),
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400),
            )
        }
    ) {
        composable("home") {
            HomScreen(navigateToChat = { channel, username ->
                navController.navigate("chat/$channel/$username")
            })
        }

        composable(
            "chat/{channel}/{username}",
            arguments = listOf(
                navArgument("channel") { type = NavType.StringType },
                navArgument("username") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            val channel = backStackEntry.arguments?.getString("channel")
            val username = backStackEntry.arguments?.getString("username")
            if (username != null && channel != null) {
                ChatScreen(
                    channel = channel,
                    name = username,
                    initializer = initializer,
                    channelInitializer = channelInitializer,
                    statePublisher = statePublisher,
                    feature = feature
                )
            }
        }
    }
}
