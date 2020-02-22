package com.biegrzes.shoutbox

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface JsonShoutboxAPI {

    @GET("messages")
    fun getMessages(): Call<List<SResponse>>

    @POST("message")
    fun newMessage(@Body responsebody: ResponseBody): Call<ResponseBody>

    @PUT("message/{id}")
    fun editMessage(@Path("id") id: String, @Body responsebody: ResponseBody): Call<ResponseBody>

    @DELETE("message/{id}")
    fun deleteMessage(@Path("id") id: String): Call<ResponseBody>


    companion object Factory {

        val BASE_URL = "http://tgryl.pl/shoutbox/"
        //val BASE_URL = "http://192.168.1.34:8080/api/"
        fun create(): JsonShoutboxAPI {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(JsonShoutboxAPI::class.java)
        }
    }
}