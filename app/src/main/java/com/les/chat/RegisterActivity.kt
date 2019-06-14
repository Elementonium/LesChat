package com.les.chat

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        register_button_register.setOnClickListener {
            performRegister()

        }
        already_have_account_text_view.setOnClickListener{
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }
        selectphoto_button_register.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0) //0 is the request code to identify the activity
        }
    }


    var selectedPhotoUri: Uri? = null //create  a varible of type uri,optional, initially null

    //called when selectphoto_button_register activity is completed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            val drawable = BitmapDrawable(bitmap)

            selectphoto_button_register.setBackgroundDrawable(drawable)

        }
    }

    private fun performRegister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Please Enter Email/Password", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener  //if user creation fails don't crash

                //else on success
                uploadImageToFirebaseStorage()

            }.addOnFailureListener {//Onfailure Show Toast with error message
                Toast.makeText(this,"Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri== null) return

        val filename = UUID.randomUUID().toString() //generate random filename
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!) //!! = uri is not null (avoid type mismatch uri?
    }

}
