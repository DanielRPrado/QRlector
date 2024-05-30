package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator

class MenuPrincipal : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var tvClaveLeida: TextView
    private lateinit var tvMensaje: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_principal)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference.child("clave")

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        tvClaveLeida = findViewById(R.id.tv_clave_leida)
        tvMensaje = findViewById(R.id.tv_mensaje)

        val scanQrButton = findViewById<Button>(R.id.scan_qr_button)
        scanQrButton.setOnClickListener {
            val integrator = IntentIntegrator(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Escanear QR")
            integrator.setCameraId(0)  // Usa la cámara trasera
            integrator.setBeepEnabled(true)
            integrator.setCaptureActivity(CaptureActivityPortrait::class.java)  // Usa una actividad de captura personalizada
            integrator.setOrientationLocked(true)  // Bloquea la orientación en vertical
            integrator.initiateScan()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.salir -> {
                auth.signOut()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_LONG).show()
            } else {
                handleQRCode(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQRCode(qrCode: String) {
        val docRef = database.child(qrCode)
        docRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val status = snapshot.child("status").value.toString()
                    tvClaveLeida.text = "Clave leída: $qrCode"
                    if (status == "QR generado") {
                        docRef.child("status").setValue("QR utilizado")
                            .addOnSuccessListener {
                                tvMensaje.text = "Mensaje: buen viaje"
                            }
                            .addOnFailureListener { e ->
                                tvMensaje.text = "Mensaje: Error al actualizar el estado"
                            }
                    } else if (status == "QR utilizado") {
                        tvMensaje.text = "Mensaje: QR utilizado, Genere un nuevo QR para ingresar"
                    }
                } else {
                    tvMensaje.text = "Mensaje: QR no encontrado"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MenuPrincipal", "Error al obtener el documento.", error.toException())
                tvMensaje.text = "Mensaje: Error al obtener el documento"
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            auth.signOut()
        }
        return super.onKeyDown(keyCode, event)
    }
}
