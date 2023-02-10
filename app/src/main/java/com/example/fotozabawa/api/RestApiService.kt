package com.example.fotozabawa.api

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.example.fotozabawa.PdfDialogFragment
import com.example.fotozabawa.Sounds
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RestApiService {
    fun uploadPhoto(photo: File, rotation: Boolean, context: Context) {
        var requestFile: RequestBody =
            RequestBody.create(MediaType.parse("multipart/form-data"), photo)

        var body: MultipartBody.Part =
            MultipartBody.Part.createFormData("image", photo.getName(), requestFile)

        val retrofit = ServiceBuilder.buildService(RestApi::class.java)
        retrofit.upload(body, rotation).enqueue(
            object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    val name = photo.name
                    Toast.makeText(
                        context,
                        "Zdjęcie $name zostało pomyślnie wgrane",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    fun printPdf(theme: Int, context: Context, supportFragmentManager: FragmentManager){
        val sounds: Sounds = Sounds(context)
        val retroFit = ServiceBuilder.buildService(RestApi::class.java)
        retroFit.print(theme).enqueue(
            object: Callback<PrintResponse> {
                override fun onFailure(call: Call<PrintResponse>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<PrintResponse>,
                    response: Response<PrintResponse>
                ) {
                    val newFragment = PdfDialogFragment()
                    newFragment.url = response.body()?.url.toString()
                    newFragment.show(supportFragmentManager, "pdf")

                    sounds.message()
                }
            }
        )
    }
}