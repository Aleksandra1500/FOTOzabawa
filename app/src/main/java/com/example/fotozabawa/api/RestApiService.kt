package com.example.fotozabawa.api

import android.content.Context
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestApiService {
    fun uploadPhoto(photo: File){
        var requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), photo)

        var body: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", photo.getName(), requestFile)

        val retrofit = ServiceBuilder.buildService(RestApi::class.java)
        retrofit.upload(body).enqueue(
            object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                }
            }
        )
    }

    fun printPdf(theme: Int, context: Context){
        val retroFit = ServiceBuilder.buildService(RestApi::class.java)
        retroFit.print(theme).enqueue(
            object: Callback<PrintResponse> {
                override fun onFailure(call: Call<PrintResponse>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<PrintResponse>,
                    response: Response<PrintResponse>
                ) {
                    Toast.makeText(
                        context,
                        response.body()?.url,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}