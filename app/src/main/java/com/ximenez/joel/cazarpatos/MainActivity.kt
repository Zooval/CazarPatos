package com.ximenez.joel.cazarpatos

import android.animation.ValueAnimator
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var textViewUser: TextView
    private lateinit var textViewCounter: TextView
    private lateinit var textViewTime: TextView
    private lateinit var imageViewDuck: ImageView

    private lateinit var soundPool: SoundPool
    private var soundId = 0
    private var streamId = 0
    private var isLoaded = false

    private val handler = Handler(Looper.getMainLooper())
    private var counter = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var gameOver = false

    private var countDownTimer: CountDownTimer? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Views
        textViewUser = findViewById(R.id.textViewUser)
        textViewCounter = findViewById(R.id.textViewCounter)
        textViewTime = findViewById(R.id.textViewTime)
        imageViewDuck = findViewById(R.id.imageViewDuck)

        // Usuario
        var usuario = intent.getStringExtra(EXTRA_LOGIN) ?: "Unknown"
        usuario = usuario.substringBefore("@")
        textViewUser.text = usuario

        initializeScreen()
        initializeSound()
        startCountdown()

        // Mover pato cuando la vista ya está lista
        imageViewDuck.post { moveDuck() }

        imageViewDuck.setOnClickListener {
            if (gameOver) return@setOnClickListener

            counter++
            textViewCounter.text = counter.toString()

            if (isLoaded) {
                streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }

            imageViewDuck.setImageResource(R.drawable.duck_clicked)
            handler.postDelayed({
                imageViewDuck.setImageResource(R.drawable.duck)
            }, 300)

            moveDuck()
        }
    }

    private fun initializeScreen() {
        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    private fun initializeSound() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = soundPool.load(this, R.raw.gunshot, 1)

        soundPool.setOnLoadCompleteListener { _, _, status ->
            isLoaded = (status == 0)
        }
    }

    private fun startCountdown() {
        countDownTimer?.cancel()

        countDownTimer = object : CountDownTimer(60_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                textViewTime.text = "${secondsRemaining}s"
            }

            override fun onFinish() {
                textViewTime.text = "0s"
                gameOver = true
                showGameOverDialog()

                val nombreJugador = textViewUser.text.toString()
                val patosCazados = counter
                procesarPuntajePatosCazados(nombreJugador, patosCazados)
            }
        }.start()
    }



    private fun moveDuck() {
        when (Random.nextInt(3)) {
            0 -> moveDuckRandom()
            1 -> moveDuckAnimated()
            2 -> moveDuckParabolic()
        }
    }
    fun eliminarPuntajeJugador(idDocumentoSeleccionado:String){
        val db = Firebase.firestore
        db.collection("ranking")
            .document(idDocumentoSeleccionado)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this,"Puntaje de usuario eliminado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error deleting document", exception)
                Toast.makeText(this,"Error al eliminar el puntaje" , Toast.LENGTH_LONG).show()
            }
    }

    private fun moveDuckRandom() {
        val min = imageViewDuck.getWidth()/2
        val maximoX = screenWidth - imageViewDuck.getWidth()
        val maximoY = screenHeight - imageViewDuck.getHeight()
        // Generamos 2 números aleatorios, para la coordenadas x , y
        val randomX = Random.nextInt(0,maximoX - min + 1)
        val randomY = Random.nextInt(92,maximoY - min + 1)

        imageViewDuck.animate()
            .x(randomX.toFloat())
            .y(randomY.toFloat())
            .setDuration(300) // animación suave
            .start()

    }

    private fun moveDuckAnimated() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height
        if (maxX <= 0 || maxY <= 0) return

        imageViewDuck.animate()
            .x(Random.nextInt(0, maxX + 1).toFloat())
            .y(Random.nextInt(0, maxY + 1).toFloat())
            .setDuration(300)
            .start()
    }

    private fun moveDuckParabolic() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height
        if (maxX <= 0 || maxY <= 0) return

        val startX = imageViewDuck.x
        val startY = imageViewDuck.y
        val endX = Random.nextInt(0, maxX + 1).toFloat()
        val endY = Random.nextInt(0, maxY + 1).toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addUpdateListener { va ->
                val t = va.animatedValue as Float
                val arc = 300f * 4 * t * (1 - t) // arco

                imageViewDuck.x = startX + (endX - startX) * t
                imageViewDuck.y = startY + (endY - startY) * t - arc
            }
        }

        animator.start()
    }

    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle("Fin del juego")
            .setMessage("Felicidades!!\nHas conseguido cazar $counter patos")
            .setIcon(R.drawable.duck)
            .setCancelable(false)
            .setPositiveButton("Reiniciar") { _, _ -> restartGame() }
            .setNegativeButton("Cerrar") { _, _ -> finish() }
            .show()
    }

    private fun restartGame() {
        counter = 0
        gameOver = false
        textViewCounter.text = "0"
        startCountdown()
        moveDuck()
    }

    fun jugarOnline(){
        var intentWeb = Intent()
        intentWeb.action = Intent.ACTION_VIEW
        intentWeb.data = Uri.parse("https://duckhuntjs.com/")
        startActivity(intentWeb)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_nuevo_juego -> {
                restartGame()
                true
            }
            R.id.action_jugar_online -> {
                jugarOnline()
                true
            }
            R.id.action_ranking -> {
                val intent = Intent(this, RankingActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_salir -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    fun procesarPuntajePatosCazados(nombreJugador:String, patosCazados:Int){
        val jugador = Player(nombreJugador,patosCazados)
        //Trata de obtener id del documento del ranking específico,
        // si lo obtiene lo actualiza, caso contrario lo crea
        val db = Firebase.firestore
        db.collection("ranking")
            .whereEqualTo("username", jugador.username)
            .get()
            .addOnSuccessListener { documents ->
                if(documents!= null &&
                    documents.documents != null &&
                    documents.documents.count()>0
                ){
                    val idDocumento = documents.documents.get(0).id
                    val jugadorLeido = documents.documents.get(0).toObject(Player::class.java)
                    if(jugadorLeido!!.huntedDucks < patosCazados )
                    {
                        Log.w(EXTRA_LOGIN, "Puntaje actual mayor, por lo tanto actualizado")
                        actualizarPuntajeJugador(idDocumento, jugador)
                    }
                    else{
                        Log.w(EXTRA_LOGIN, "No se actualizo puntaje, por ser menor al actual")
                    }
                }
                else{
                    ingresarPuntajeJugador(jugador)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error getting documents", exception)
                Toast.makeText(this, "Error al obtener datos de jugador", Toast.LENGTH_LONG).show()
            }
    }
    fun ingresarPuntajeJugador(jugador:Player){
        val db = Firebase.firestore
        db.collection("ranking")
            .add(jugador)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this,"Puntaje usuario ingresado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error adding document", exception)
                Toast.makeText(this,"Error al ingresar el puntaje", Toast.LENGTH_LONG).show()
            }
    }
    fun actualizarPuntajeJugador(idDocumento:String, jugador:Player){
        val db = Firebase.firestore
        db.collection("ranking")
            .document(idDocumento)
            //.update(contactoHashMap)
            .set(jugador) //otra forma de actualizar
            .addOnSuccessListener {
                Toast.makeText(this,"Puntaje de usuario actualizado exitosamente", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { exception ->
                Log.w(EXTRA_LOGIN, "Error updating document", exception)
                Toast.makeText(this,"Error al actualizar el puntaje" , Toast.LENGTH_LONG).show()
            }
    }

    override fun onStop() {
        super.onStop()
        Log.w("MainActivity", "Game stopped")
        countDownTimer?.cancel()
        if (streamId != 0) soundPool.stop(streamId)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
        soundPool.release()
    }
}
