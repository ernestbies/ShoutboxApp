package com.biegrzes.shoutbox

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadNickname()
        setLogin.setOnClickListener {
            if (login.text.isNotEmpty()) {
                val user = login.text.toString()
                App.user = user
                saveNickname(user)
                val myIntent = Intent(this@MainActivity, Chat::class.java)
                this@MainActivity.startActivity(myIntent)
            } else {
                Toast.makeText(applicationContext, "Username should not be empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveNickname(name: String) {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("USERNAME", name)
        editor.apply()
    }

    private fun loadNickname() {
        val sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("USERNAME", "")
        login.setText(name)
    }

}
