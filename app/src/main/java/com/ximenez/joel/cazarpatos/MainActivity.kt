package com.ximenez.joel.cazarpatos

import android.animation.ValueAnimator
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.util.Log
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var countDownTimer: CountDownTimer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Views
        textViewUser = findViewById(R.id.textViewUser)
        textViewCounter = findViewById(R.id.textViewCounter)
        textViewTime = findViewById(R.id.textViewTime)
        imageViewDuck = findViewById(R.id.imageViewDuck)

        // Usuario (SIN cerrar la Activity)
        val usuario = intent.getStringExtra(EXTRA_LOGIN) ?: "Unknown"
        textViewUser.text = usuario

        initializeScreen()
        initializeSound()
        initializeCountdown()

        // Mover pato cuando la vista ya está lista
        imageViewDuck.post {
            moveDuck()
        }

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

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            isLoaded = true
        }
    }

    private fun initializeCountdown() {
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                textViewTime.text = "${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                gameOver = true
                textViewTime.text = "0s"
                showGameOverDialog()
            }
        }
        countDownTimer.start()
    }

    private fun moveDuck() {
        when (Random.nextInt(3)) {
            0 -> moveDuckRandom()
            1 -> moveDuckAnimated()
            2 -> moveDuckParabolic()
        }
    }

    private fun moveDuckRandom() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height

        if (maxX > 0 && maxY > 0) {
            imageViewDuck.x = Random.nextInt(0, maxX).toFloat()
            imageViewDuck.y = Random.nextInt(0, maxY).toFloat()
        }
    }

    private fun moveDuckAnimated() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height

        if (maxX > 0 && maxY > 0) {
            imageViewDuck.animate()
                .x(Random.nextInt(0, maxX).toFloat())
                .y(Random.nextInt(0, maxY).toFloat())
                .setDuration(300)
                .start()
        }
    }

    private fun moveDuckParabolic() {
        val maxX = screenWidth - imageViewDuck.width
        val maxY = screenHeight - imageViewDuck.height

        if (maxX <= 0 || maxY <= 0) return

        val startX = imageViewDuck.x
        val startY = imageViewDuck.y
        val endX = Random.nextInt(0, maxX).toFloat()
        val endY = Random.nextInt(0, maxY).toFloat()

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 300
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener {
            val t = it.animatedValue as Float
            val arc = 300f * 4 * t * (1 - t)

            imageViewDuck.x = startX + (endX - startX) * t
            imageViewDuck.y = startY + (endY - startY) * t - arc
        }

        animator.start()
    }

    private fun showGameOverDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_title_game_end))
            .setMessage(getString(R.string.dialog_message_congratulations, counter))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.button_restart)) { _, _ ->
                restartGame()
            }
            .setNegativeButton(getString(R.string.button_close)) { _, _ ->
                finish()
            }
            .show()
    }

    private fun restartGame() {
        counter = 0
        gameOver = false
        textViewCounter.text = "0"
        initializeCountdown()
        moveDuck()
    }

    override fun onStop() {
        super.onStop()
        Log.w("MainActivity", "Game stopped")
        countDownTimer.cancel()
        if (streamId != 0) soundPool.stop(streamId)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
