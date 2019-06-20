package com.les.chat.messages

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.les.chat.R
import com.les.chat.models.ChatMessage
import com.les.chat.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_recieved_row.view.*
import kotlinx.android.synthetic.main.chat_sent_row.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter= GroupAdapter<ViewHolder>()
    var partnerUser: User ?= null //intialize chat partner, optional, with null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)


        //set the chat partner to the user recieved from NewMessageActivity
        partnerUser = intent.getParcelableExtra(NewMessageActivity.USER_KEY)

        supportActionBar?.title = partnerUser?.username

        recyclerview_chatlog.adapter=adapter

        listenForMessages()

        send_button_chatlog.setOnClickListener {
            performSendMessage()
        }
    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        //when a new child is added to given node
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) //store the chat message as a ChatMessage
                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = Conversations.currentUser ?: return //get current usr from global var in ConversationsActivity
                        adapter.add(ChatSentItem(chatMessage.text, currentUser)) //add sentitem to the chatlog
                    } else {
                        adapter.add(ChatRecievedItem(chatMessage.text, partnerUser!!)) //add recieveditem to the chatlog
                    }
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
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
        //Set the properties of ChatMessage
        val text = message_edittext_chatlog.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push() //push creates a new child in ref

        val toref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push() //push creates a new child in ref for the other user

        val latestMessageref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        val latestMessageTOref = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        if(fromId == null) return
        val chatMessage = ChatMessage(ref.key!!,text, fromId, toId, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage) //add node to the given reference
            .addOnSuccessListener {
                message_edittext_chatlog.text.clear()
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)//automatically scroll to end of chat
            }
        toref.setValue(chatMessage)
        latestMessageref.setValue(chatMessage)
        latestMessageTOref.setValue(chatMessage)

    }

}
class ChatRecievedItem(val text: String, val user: User) : Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_recieved.text = text

        val uri = user.displayPictureUrl
        val targetImageView = viewHolder.itemView.message_imageview_recieved
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
        return R.layout.chat_recieved_row
    }
}


class ChatSentItem(val text: String, val user: User): Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.message_textview_sent.text = text
        val uri = user.displayPictureUrl
        val targetImageView = viewHolder.itemView.message_imageview_sent
        Picasso.get().load(uri).into(targetImageView)
    }

    override fun getLayout(): Int {
            return R.layout.chat_sent_row
    }
}
