package com.biegrzes.shoutbox

import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.app_bar_chat.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

//chat - obsługa
class Chat : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var list = ArrayList<SResponse>()
    private var view: View? = null
    private val p = Paint()
    private var adapter: MessageAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var alertDialog: AlertDialog.Builder? = null
    private var et_name: EditText? = null
    private var edit_position: Int = 0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        setSupportActionBar(toolbar)


        val btnSend = findViewById(R.id.btnSend) as Button
        val txtMessage = findViewById(R.id.txtMessage) as EditText

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        //wysłanie wiadomości - SEND
        btnSend.setOnClickListener{
            val message = ResponseBody(App.user,txtMessage.text.toString())
            newMessage(message)
            txtMessage.setText("")

        }

        //zczytanie wiadomość
        getMessages()
        //inicjacja swipe'a
        initSwipe()
        //okno dialogowe do edycji posta
        initDialog()

    }



    private fun initSwipe() {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                //sprawdzenie czy jesteśmy użytkownikiem posta
                //zapobiega usunięciu postów innych użytkowników
                if (list[position].login == App.user) {
                    if (direction == ItemTouchHelper.LEFT) {
                        //usunięcie wiadomości
                        deleteMessage(list[position].id!!)
                        adapter!!.removeItem(position)

                    } else {
                        //edycja wiadomości
                        removeView()
                        edit_position = position
                        alertDialog!!.setTitle("Edit post")
                        et_name!!.setText(list[position].content)
                        alertDialog!!.show()
                        adapter!!.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this@Chat, "It's not your post!!!", Toast.LENGTH_LONG).show()
                    adapter!!.notifyDataSetChanged()
                }

            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {

                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    val itemView = viewHolder.itemView
                    val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                    val width = height / 3

                    if (dX > 0) {
                        //pasek edycji wiadomości + ikona
                        p.color = Color.parseColor("#388E3C")
                        val background = RectF(itemView.left.toFloat(), itemView.top.toFloat(), dX, itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_edit_white)
                        val icon_dest = RectF(itemView.left.toFloat() + width, itemView.top.toFloat() + width, itemView.left.toFloat() + 2 * width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    } else {
                        //pasek usunięcia wiadomości + ikona
                        p.color = Color.parseColor("#D32F2F")
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, p)
                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete_white)
                        val icon_dest = RectF(itemView.right.toFloat() - 2 * width, itemView.top.toFloat() + width, itemView.right.toFloat() - width, itemView.bottom.toFloat() - width)
                        c.drawBitmap(icon, null, icon_dest, p)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun removeView() {
        if (view!!.parent != null) {
            (view!!.parent as ViewGroup).removeView(view)
        }
    }

    //Inicjacja dialogu - EditPost
    private fun initDialog() {
        alertDialog = AlertDialog.Builder(this)
        view = layoutInflater.inflate(R.layout.dialog_layout, null)

        alertDialog!!.setView(view)
        alertDialog!!.setPositiveButton("Save") { dialog, which ->

            list[edit_position].content = et_name!!.text.toString()
            adapter!!.notifyDataSetChanged()
            dialog.dismiss()
            val message = ResponseBody(App.user,list[edit_position].content)
            //edycja wiadomości i wysłanie jej na serwer
            editMessage(list[edit_position].id!!, message)

        }
        //pole w EditPost
        et_name = view!!.findViewById(R.id.et_name) as EditText

    }


    //pobieranie wiadomości z serwera
    fun getMessages() {
        val apiService = JsonShoutboxAPI.create()
        val call = apiService.getMessages()
        val layoutManager = LinearLayoutManager(this@Chat, LinearLayout.VERTICAL,false)

        recyclerView = findViewById(R.id.messageList) as RecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = layoutManager


        call.enqueue(object : Callback<List<SResponse>> {
            override fun onResponse(call: Call<List<SResponse>?>?, response: retrofit2.Response<List<SResponse>?>?) {
                if (response != null) {

                    list = response.body()!! as ArrayList<SResponse>
                    for (i in list.indices){
                        list[i].date = convertDate(list[i].date!!)
                    }

                    adapter = MessageAdapter(list)
                    recyclerView!!.adapter = adapter
                    adapter!!.notifyDataSetChanged()


                } else {
                    Toast.makeText(this@Chat, "Response is NULL!! ", Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(call: Call<List<SResponse>>, t: Throwable) {
                Toast.makeText(this@Chat, "Failure " + t.toString(), Toast.LENGTH_LONG).show()
            }

        })
    }

    //wysłanie nowej wiadomości - przesłanie do serwera
    fun newMessage(message: ResponseBody) {
        val apiService = JsonShoutboxAPI.create()
        val call = apiService.newMessage(message)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@Chat, "Failure " + t.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                getMessages()
            }

        })

    }

    //edycja wiadomości - przesłanie do serwera
    fun editMessage(id: String, message: ResponseBody) {
        val apiService = JsonShoutboxAPI.create()
        val call = apiService.editMessage(id, message)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@Chat, "Failure " + t.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                //getMessages()
            }

        })

    }

    //usunięcie wiadomości - przesłanie do serwera (przyjmuje ID)
    fun deleteMessage(id: String) {
        val apiService = JsonShoutboxAPI.create()
        val call = apiService.deleteMessage(id)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@Chat, "Failure " + t.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(this@Chat, "Deleted successful", Toast.LENGTH_LONG).show()
            }

        })
    }


    fun convertDate(isoTime: String): String? {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        var convertedDate: Date? = null
        var formattedDate: String? = null
        try {
            convertedDate = sdf.parse(isoTime)
            formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(convertedDate.time.plus(7200000))
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return formattedDate
    }


    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.shoutbox -> {
            }
            R.id.settings -> {
                val myIntent = Intent(this@Chat, MainActivity::class.java)
                this@Chat.startActivity(myIntent)
            }

        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

}
