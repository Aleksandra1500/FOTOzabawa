package com.example.fotozabawa.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RestApi {

    @Multipart
    @POST("upload")
    fun upload(@Part imageFile: MultipartBody.Part): Call<ResponseBody>

    @GET("print")
    fun print(@Query("theme") theme: Int): Call<PrintResponse>
}