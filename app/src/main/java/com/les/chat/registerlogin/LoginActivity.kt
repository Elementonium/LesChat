package com.les.chat.registerlogin

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.les.chat.R
import com.les.chat.messages.Conversations
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {
            performLogin()
        }
        new_account_text_view.setOnClickListener {
            finish()
        }
    }
        private fun performLogin(){
            val email = email_edittext_login.text.toString()
            val password = password_edittext_login.text.toString()

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(this,"Please Enter Email/Password", Toast.LENGTH_SHORT).show()
                return
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if(!it.isSuccessful) return@addOnCompleteListener  //if user login fails dont crash

                    //else if successful
                    val intent = Intent(this, Conversations::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Toast.makeText(this,"Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
}