package com.example.marsphotos.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.marsphotos.network.MarsApi
import com.example.marsphotos.network.MarsPhoto
import retrofit2.HttpException
import java.io.IOException

private const val TAG = "MarsViewModel"

class MarsViewModel : ViewModel() {
    var marsUiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    // Pour le rafraîchissement "pull-to-refresh"
    var isRefreshing by mutableStateOf(false)
        private set

    // Pour la sélection d'une photo
    var selectedPhoto: MarsPhoto? by mutableStateOf(null)
        private set

    init {
        getMarsPhotos()
    }

    fun getMarsPhotos() {
        viewModelScope.launch {
            marsUiState = MarsUiState.Loading
            marsUiState = try {
                Log.d(TAG, "Calling API...")
                val photos = MarsApi.retrofitService.getPhotos()
                Log.d(TAG, "Received photos successfully")
                MarsUiState.Success(photos)
            } catch (e: IOException) {
                Log.e(TAG, "IOException: ${e.message}")
                MarsUiState.Error("Erreur réseau: Vérifiez votre connexion internet.")
            } catch (e: HttpException) {
                Log.e(TAG, "HttpException: ${e.code()}, ${e.message()}")
                MarsUiState.Error("Erreur HTTP ${e.code()}: ${e.message()}")
            } catch (e: Exception) {
                Log.e(TAG, "General exception: ${e.message}")
                e.printStackTrace()
                MarsUiState.Error("Erreur inattendue: ${e.message}")
            }
        }
    }

    fun refreshPhotos() {
        viewModelScope.launch {
            isRefreshing = true
            try {
                Log.d(TAG, "Refreshing photos...")
                val photos = MarsApi.retrofitService.getPhotos()
                Log.d(TAG, "Photos refreshed successfully")
                marsUiState = MarsUiState.Success(photos)
            } catch (e: Exception) {
                Log.e(TAG, "Refresh failed: ${e.message}")
                // On ne change pas l'état principal en cas d'erreur de rafraîchissement
                // pour éviter de perdre les données existantes
            } finally {
                isRefreshing = false
            }
        }
    }

    fun selectPhoto(photo: MarsPhoto?) {
        selectedPhoto = photo
    }
}

sealed interface MarsUiState {
    data class Success(val photos: List<MarsPhoto>) : MarsUiState
    data class Error(val message: String) : MarsUiState
    object Loading : MarsUiState
}

