package com.biegrzes.shoutbox

import android.app.Application

class App: Application() {
    //przechowuje nazwe użytkownika
    companion object {
        lateinit var user:String
    }
}