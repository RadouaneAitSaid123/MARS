package com.example.marsphotos.network

import com.squareup.moshi.Json

data class MarsPhoto(
    val id: String,

    @Json(name = "img_src")
    val imgSrc: String,

    // Ajout de propriétés supplémentaires pour enrichir l'expérience
    @Json(name = "earth_date")
    val earthDate: String = "Inconnue",

    @Json(name = "camera")
    val camera: Camera = Camera("", "")
)

data class Camera(
    val id: String,
    val name: String
)

