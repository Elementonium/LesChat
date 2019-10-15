package com.les.chat.messages

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.les.chat.R
import com.les.chat.messages.Conversations.Companion.currentUser
import com.les.chat.models.ChatMessage
import com.les.chat.models.User
import com.les.chat.models.UserState
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_recieved_row.view.*
import kotlinx.android.synthetic.main.chat_sent_row.view.*
import java.util.*
import kotlin.concurrent.schedule

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
        listenForPartnerState()
        send_button_chatlog.setOnClickListener {
            performSendMessage()
        }
        message_edittext_chatlog.addTextChangedListener(object : TextWatcher{
            private var timer = Timer()
            val fromId= currentUser?.uid
            val toId = partnerUser?.uid
            val ref = FirebaseDatabase.getInstance().getReference("/user-state/$toId/$fromId/state")
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val typing = true
                ref.child("typing").setValue(typing)
            }
            override fun afterTextChanged(p0: Editable?) {

                val typing = false
                timer.cancel()
                timer = Timer()

                timer.schedule(1000){
                    ref.child("typing").setValue(typing)
                }
            }
        })
    }
    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = partnerUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        //Executed when a new child is created
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
                val chatMessage = p0.getValue(ChatMessage::class.java) //store the chat message as a ChatMessage
                if (chatMessage != null) {
                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = Conversations.currentUser ?: return //Retrieve current user from global variable in ConversationsActivity
                        adapter.add(ChatSentItem(chatMessage.text, currentUser)) //add sentitem to the chatlog adapter
                    } else {
                        adapter.add(ChatRecievedItem(chatMessage.text, partnerUser!!)) //add recieveditem to the chatlog adapter
                    }
                    recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)
                }

            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onCancelled(p0: DatabaseError) {}
        })
    }
    private fun listenForPartnerState(){
        val fromId = currentUser?.uid
        val toId = partnerUser?.uid
        val stateref = FirebaseDatabase.getInstance().getReference("/user-state/$fromId/$toId")
        stateref.addChildEventListener(object:ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val userState = p0.getValue(UserState::class.java) ?: return

                    if(userState.typing){
                        supportActionBar?.subtitle = "typing..."
                    }else{
                            supportActionBar?.subtitle = ""
                    }

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val userState = p0.getValue(UserState::class.java) ?: return

                    if(userState.typing){
                        supportActionBar?.subtitle = "typing..."
                    }
                    else if(userState.seen){
                        supportActionBar?.subtitle = "seen"
                    }
                    else{
                        supportActionBar?.subtitle = ""
                    }


            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildRemoved(p0: DataSnapshot) {}
        })
    }
    private fun performSendMessage(){
        //Set the properties of ChatMessage
        val text = message_edittext_chatlog.text.toString()
        message_edittext_chatlog.text.clear()
        val fromId = FirebaseAuth.getInstance().uid
        if(fromId == null || text == "") return
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push() //push creates a new child in ref

        val toref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push() //push creates a new child in ref for the other user

        val latestMessageref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        val latestMessageTOref = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        val chatMessage = ChatMessage(ref.key!!,text, fromId, toId, System.currentTimeMillis()/1000)

        ref.setValue(chatMessage) //add node to the given reference
            .addOnSuccessListener {
                recyclerview_chatlog.scrollToPosition(adapter.itemCount -1)//automatically scroll to end of chat
            }
        toref.setValue(chatMessage)
        latestMessageref.setValue(chatMessage)
        latestMessageTOref.setValue(chatMessage)

    }
    //Inflate contents of chat_menu to OptionsMenu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.menu_clear_chat->{
                val fromId = FirebaseAuth.getInstance().uid
                val toId = partnerUser?.uid

                val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

                val toref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")

                val latestMessageref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

                val latestMessageTOref = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

                val chatMessage = ChatMessage(ref.key!!,"Chat Cleared", fromId!!, toId!!, System.currentTimeMillis()/1000)

                ref.removeValue()
                toref.removeValue()
                latestMessageref.setValue(chatMessage)
                latestMessageTOref.setValue(chatMessage)
                finish()
            }
            R.id.menu_sendphoto_chat->{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type="image/*"
                startActivityForResult(intent, 1)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && resultCode == Activity.RESULT_OK && data != null){
            //TODO
        }
    }

    override fun onPause() {
        val fromId = currentUser?.uid
        val toId = partnerUser?.uid
        val userStateRef = FirebaseDatabase.getInstance().getReference("/user-state/$toId/$fromId/state")
        val seen = false
        userStateRef.child("seen").setValue(seen)
        super.onPause()
    }

    override fun onResume() {
        val fromId = currentUser?.uid
        val toId = partnerUser?.uid
        val userStateRef = FirebaseDatabase.getInstance().getReference("/user-state/$toId/$fromId/state")
        val seen = true
        userStateRef.child("seen").setValue(seen)
        super.onResume()
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
