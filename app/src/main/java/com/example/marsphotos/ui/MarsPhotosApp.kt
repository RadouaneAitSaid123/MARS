/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.DetailScreen
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.MarsViewModel
import com.example.marsphotos.ui.screens.SplashScreen

@Composable
fun MarsPhotosApp(
    navController: NavHostController = rememberNavController()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val marsViewModel: MarsViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash", // Modifié pour commencer avec l'écran de démarrage
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
            fadeOut(animationSpec = tween(300))
        }
    ) {
        // Écran de démarrage (SplashScreen)
        composable(route = "splash") {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable(route = "home") {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = { MarsTopAppBar(scrollBehavior = scrollBehavior) }
            ) { paddingValues ->
                val isRefreshing = remember { mutableStateOf(false) }

                // Observer l'état de rafraîchissement du ViewModel
                LaunchedEffect(marsViewModel.isRefreshing) {
                    isRefreshing.value = marsViewModel.isRefreshing
                }

                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Note: pull-to-refresh simple implémenté via
                        // le gestionnaire de défilement dans HomeScreen
                        HomeScreen(
                            marsUiState = marsViewModel.marsUiState,
                            retryAction = { marsViewModel.getMarsPhotos() },
                            onRefresh = { marsViewModel.refreshPhotos() },
                            isRefreshing = isRefreshing.value,
                            onPhotoClick = { photo ->
                                marsViewModel.selectPhoto(photo)
                                navController.navigate("detail")
                            },
                            contentPadding = paddingValues,
                        )
                    }
                }
            }
        }

        composable(route = "detail") {
            // Afficher l'écran de détail quand une photo est sélectionnée
            marsViewModel.selectedPhoto?.let { photo ->
                DetailScreen(
                    photo = photo,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } ?: run {
                // Fallback au cas où aucune photo n'est sélectionnée
                LaunchedEffect(key1 = Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}

@Composable
fun MarsTopAppBar(scrollBehavior: TopAppBarScrollBehavior, modifier: Modifier = Modifier) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        modifier = modifier
    )
}
