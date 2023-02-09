package com.example.fotozabawa.api

import com.google.gson.annotations.SerializedName

data class PrintResponse(
    @SerializedName("url") val url: String?
)
