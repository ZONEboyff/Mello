package com.example.mello.models

data class ImgbbResponse(
    val data: ImageData,
    val success: Boolean,
    val status_code: Int
)

data class ImageData(
    val url: String,
    val display_url: String
)

