package com.biegrzes.shoutbox

import com.google.gson.annotations.SerializedName

//to jest przesyłane do serwera
class ResponseBody {

    @SerializedName("content")
    var content: String? = null

    @SerializedName("login")
    var login: String? = null

    constructor(login: String?, content: String?) {
        this.content = content
        this.login = login
    }
}