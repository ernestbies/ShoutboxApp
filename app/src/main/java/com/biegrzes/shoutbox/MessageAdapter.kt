package com.biegrzes.shoutbox

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


//obsługa RecyclerView
class MessageAdapter(val userList: ArrayList<SResponse>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val v = LayoutInflater.from(p0.context).inflate(R.layout.messages, p0, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val user: SResponse = userList[p1]

        p0.textContent.text = user.content
        p0.textDate.text = user.date
        p0.textNick.text = user.login

    }

    fun removeItem(position: Int) {
        userList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, userList.size)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textContent = itemView.findViewById(R.id.content) as TextView
        val textDate = itemView.findViewById(R.id.date) as TextView
        val textNick = itemView.findViewById(R.id.nickname) as TextView

    }
}
