package com.les.chat.views

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.les.chat.R
import com.les.chat.models.ChatMessage
import com.les.chat.models.User
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.conversations_row.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>(){
    var chatPartnerUser : User ?= null
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.lastmessage_textview_conversations.text = chatMessage.text

        val chatPartnerId : String
        if (chatMessage.fromId == FirebaseAuth.getInstance().uid){
            chatPartnerId = chatMessage.toId
        }else{
            chatPartnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                chatPartnerUser = p0.getValue(User::class.java)

                viewHolder.itemView.username_textview_conversations.text = chatPartnerUser?.username
                val uri = chatPartnerUser?.displayPictureUrl
                val targetImageView = viewHolder.itemView.dp_imageview_conversations
                Picasso.get().load(uri).into(targetImageView)
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })

    }
    override fun getLayout(): Int {
        return R.layout.conversations_row
    }
}