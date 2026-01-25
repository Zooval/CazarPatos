package com.ximenez.joel.cazarpatos

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    private lateinit var manejadorArchivo: FileHandler
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonNewUser: Button
    private lateinit var checkBoxRecordarme: CheckBox
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Inicialización de variables
        manejadorArchivo = SharedPreferencesManager(this)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        buttonNewUser = findViewById(R.id.buttonNewUser)
        checkBoxRecordarme = findViewById(R.id.checkBoxRecordarme)
        auth = Firebase.auth
        LeerDatosDePreferencias()

        // Eventos clic
        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val clave = editTextPassword.text.toString()

            if (!validateRequiredData())
                return@setOnClickListener

            GuardarDatosEnPreferencias()
            AutenticarUsuario(email, clave)
        }

        buttonNewUser.setOnClickListener {
            // Abrir la pantalla de registro
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.title_screen)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("EXTRA_LOGIN", user.email)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }

    private fun AutenticarUsuario(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("LoginActivity", "signInWithEmail:success")
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("EXTRA_LOGIN", auth.currentUser!!.email)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("LoginActivity", "signInWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Error: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateRequiredData(): Boolean {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        if (email.isEmpty()) {
            editTextEmail.error = getString(R.string.error_email_required)
            editTextEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.error_password_required)
            editTextPassword.requestFocus()
            return false
        }
        return true
    }

    private fun LeerDatosDePreferencias() {
        val listadoLeido = manejadorArchivo.ReadInformation()
        if (listadoLeido.first != null && listadoLeido.first.isNotEmpty()) {
            checkBoxRecordarme.isChecked = true
            editTextEmail.setText(listadoLeido.first)
            editTextPassword.setText(listadoLeido.second)
        }
    }

    private fun GuardarDatosEnPreferencias() {
        val email = editTextEmail.text.toString()
        val clave = editTextPassword.text.toString()
        val listadoAGrabar = if (checkBoxRecordarme.isChecked) email to clave else "" to ""

        manejadorArchivo.SaveInformation(listadoAGrabar)
        EncryptedSharedPreferencesManager(this).SaveInformation(listadoAGrabar)
        FileInternalManager(this).SaveInformation(listadoAGrabar)
        FileExternalManager(this).SaveInformation(listadoAGrabar)
    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}