package com.example.sigi

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface ImgurApiService {
    @Multipart
    @POST("3/image")
    fun uploadImage(
        @Header("Authorization") auth: String,
        @Part image: MultipartBody.Part
    ): Call<ImgurResponse>
}

