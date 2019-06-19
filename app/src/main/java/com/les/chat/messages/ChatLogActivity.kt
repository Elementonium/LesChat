package com.les.chat.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.les.chat.R
import com.les.chat.models.ChatMessage
import com.les.chat.models.User
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_recieved_row.view.*
import kotlinx.android.synthetic.main.chat_sent_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter= GroupAdapter<ViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        recyclerview_chatlog.adapter=adapter
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)


        supportActionBar?.title = user.username
        //dummyData()
        listenForMessages()
        send_button_chatlog.setOnClickListener {
            performSendMessage()
        }
    }
    private fun listenForMessages(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        adapter.add(ChatSentItem(chatMessage.text))
                    } else {
                        adapter.add(ChatRecievedItem(chatMessage.text))
                    }
                }
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
    private fun performSendMessage(){
        val ref = FirebaseDatabase.getInstance().getReference("/messages").push() //push creates a new child in ref
        val text = message_edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        if(fromId == null) return
        val chatMessage = ChatMessage(ref.key!!,text, fromId, toId, System.currentTimeMillis()/1000)
        ref.setValue(chatMessage)

    }

//    private fun dummyData(){
//        val adapter = GroupAdapter<ViewHolder>()
//        adapter.add(ChatRecievedItem("from"))
//        adapter.add(ChatSentItem("to"))
//        adapter.add(ChatRecievedItem("from"))
//        adapter.add(ChatRecievedItem("to"))
//        adapter.add(ChatSentItem("from"))
//        recyclerview_chatlog.adapter = adapter
//    }
}
class ChatRecievedItem(val text: String) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_recieved.text = text
    }

    override fun getLayout(): Int {
        return R.layout.chat_recieved_row
    }
}


class ChatSentItem(val text: String): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_sent.text = text
    }

    override fun getLayout(): Int {
            return R.layout.chat_sent_row
    }
}
