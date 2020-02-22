package com.biegrzes.shoutbox
import com.google.gson.annotations.SerializedName

//to jest zwracane z serwera
class SResponse {
    @SerializedName("content")
    var content: String? = null

    @SerializedName("login")
    var login: String? = null

    @SerializedName("date")
    var date: String? = null

    @SerializedName("id")
    var id: String? = null
}