package com.ximenez.joel.cazarpatos

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegisterActivity : AppCompatActivity() {
    private lateinit var manejadorArchivo: FileHandler
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextPassword2: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonBackLogin: Button
    private lateinit var mediaPlayer: MediaPlayer

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Inicialización de variables
        manejadorArchivo = SharedPreferencesManager(this)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextPassword2 = findViewById(R.id.editTextPassword2)
        buttonSignUp = findViewById(R.id.buttonRegister)
        buttonBackLogin = findViewById(R.id.buttonNewUser)
        auth = Firebase.auth

        // Eventos clic
        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString()
            val clave = editTextPassword.text.toString()
            
            // Validaciones de datos requeridos y formatos
            if (!validateRequiredData()) {
                return@setOnClickListener
            }
            
            registrarUsuario(email, clave)
        }

        buttonBackLogin.setOnClickListener {
            finish()
        }

        mediaPlayer = MediaPlayer.create(this, R.raw.title_screen)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun registrarUsuario(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterActivity", "createUserWithEmail:success")
                    Toast.makeText(this, "Jugador registrado exitosamente", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Log.w("RegisterActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Error al registrar: ${task.exception?.message}",
                        Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun validateRequiredData(): Boolean {
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()
        val password2 = editTextPassword2.text.toString()

        if (email.isEmpty()) {
            editTextEmail.error = getString(R.string.error_email_required)
            editTextEmail.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = getString(R.string.error_invalid_email)
            editTextEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            editTextPassword.error = getString(R.string.error_password_required)
            editTextPassword.requestFocus()
            return false
        }

        if (password.length < 8) {
            editTextPassword.error = getString(R.string.error_password_min_length)
            editTextPassword.requestFocus()
            return false
        }

        if (password != password2) {
            editTextPassword2.error = getString(R.string.error_password_mismatch)
            editTextPassword2.requestFocus()
            return false
        }

        return true
    }

    override fun onDestroy() {
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        super.onDestroy()
    }
}