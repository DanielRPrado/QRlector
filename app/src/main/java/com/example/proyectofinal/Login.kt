package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = Firebase.auth
        var btnLogin = findViewById<Button>(R.id.login)
        var email = findViewById<EditText>(R.id.email)
        var password = findViewById<EditText>(R.id.password)

        btnLogin.setOnClickListener{

            if (email.text.toString() != "" && password.text.toString() != ""){
                auth.signInWithEmailAndPassword(email.text.toString(),password.text.toString()).addOnCompleteListener{
                        task ->

                    if(task.isSuccessful){
                        Toast.makeText(this, "Se inicio sesión correctamente", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, MenuPrincipal::class.java))

                    }
                    else{
                        Toast.makeText(this, "ERROR " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()

                    }
                }
            }
            else{
                Toast.makeText(this, "Introduce Email o contraseña ", Toast.LENGTH_LONG).show()
            }

        }
    }

    public override fun onStart() {
        super.onStart()

        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "No hay usuarios autenticados", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Ya estás autenticado", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MenuPrincipal::class.java))
            finish()
        }
    }



    public override fun onDestroy() {
        super.onDestroy()

        auth.signOut()
    }
}