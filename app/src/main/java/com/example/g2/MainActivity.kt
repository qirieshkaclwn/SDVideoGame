package com.example.g2

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: BallGameView
    private var mediaPlayer: MediaPlayer? = null
    private var isMusicPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.gameView) // Updated to reference correct view
        val restartButton: Button = findViewById(R.id.restartButton)
        val musicButton: Button = findViewById(R.id.musicButton)

        mediaPlayer = MediaPlayer.create(this, R.raw.music)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        restartButton.setOnClickListener {
            gameView.resetGame() // Call resetGame on the correct game view
        }

        musicButton.setOnClickListener {
            toggleMusic(musicButton)
        }
    }

    private fun toggleMusic(musicButton: Button) {
        if (isMusicPlaying) {
            mediaPlayer?.pause()
            musicButton.text = "Play Music"
        } else {
            mediaPlayer?.start()
            musicButton.text = "Stop Music"
        }
        isMusicPlaying = !isMusicPlaying
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause() // Pause music if the activity is paused
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Release the media player when the activity is destroyed
    }
}
