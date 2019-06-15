package com.les.chat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
                    if(!it.isSuccessful) return@addOnCompleteListener  //if user creation fails go back
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
}