package com.example.mello.networks

import com.example.mello.models.ImgbbResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgbbApi {
    @Multipart
    @POST("https://api.imgbb.com/1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Query("expiration") expiration: Int?,
        @Part image: MultipartBody.Part
    ): Response<ImgbbResponse>
}