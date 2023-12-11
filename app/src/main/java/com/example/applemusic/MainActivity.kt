package com.example.applemusic
import android.app.Application
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import eightbitlab.com.blurview.BlurView

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}

data class User(
    val name: String? = "",
    val songs: Map<String, Boolean>? = null,
    val images: Map<String, Boolean>? = null
)

class MainActivity : AppCompatActivity() {

    private lateinit var blurViewAll: BlurView
    private lateinit var blurViewBottom: BlurView
    private lateinit var playButton: ImageView
    private lateinit var seekBar: SeekBar
    private lateinit var endTime: TextView

    private lateinit var music: MediaPlayer
    private val mHandler = Handler()

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        blurViewAll = findViewById(R.id.blurLayout)
        blurViewBottom = findViewById(R.id.blurBottom)
        playButton = findViewById(R.id.playButton)
        seekBar = findViewById(R.id.seekBar)
        endTime = findViewById(R.id.EndTime)

        blurBackground()

        music = MediaPlayer.create(this, R.raw.notyouoo)

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!

        // Fetch user data from Firebase
        fetchUserData()

        playButton.setOnClickListener {
            musicControl(it)
        }

        seekBar.max = music.duration
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    music.seekTo(progress)
                    updateSeekBarAndTime()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Do nothing when tracking starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Do nothing when tracking stops
            }
        })

        updateSeekBarAndTime()
    }

    private fun blurBackground() {
        val radiusAll = 5f
        val radiusBottom = 10f

        val decorView = window.decorView
        val rootView: ViewGroup = decorView.findViewById(android.R.id.content)
        val windowBackground: Drawable? = decorView.background

        blurViewAll.setupWith(rootView)
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radiusAll)
            .setBlurEnabled(true)

        val decorViewBottom = window.decorView
        val rootViewBottom: ViewGroup = decorViewBottom.findViewById(android.R.id.content)
        val windowBackgroundBottom: Drawable? = decorViewBottom.background

        blurViewBottom.setupWith(rootViewBottom)
            .setFrameClearDrawable(windowBackgroundBottom)
            .setBlurRadius(radiusBottom)
            .setBlurEnabled(true)
        blurViewBottom.outlineProvider = ViewOutlineProvider.BACKGROUND
        blurViewBottom.clipToOutline = true
    }

    // Playing, pausing, and stopping the music
    fun musicControl(v: View) {
        if (music.isPlaying) {
            music.pause()
            animateButton(R.drawable.avd_pause_to_play)
        } else {
            music.start()
            animateButton(R.drawable.avd_play_to_pause)
        }
    }

    // Stopping the music
    fun stopMusic(v: View) {
        if (music.isPlaying) {
            music.stop()
            music = MediaPlayer.create(this, R.raw.notyouoo)
            animateButton(R.drawable.avd_play_to_pause)
        }
    }

    private fun animateButton(animationResId: Int) {
        val avd = AnimatedVectorDrawableCompat.create(this, animationResId)
        playButton.setImageDrawable(avd)
        avd?.start()
    }

    private fun updateSeekBarAndTime() {
        seekBar.progress = music.currentPosition
        endTime.text = formatTime(music.duration)
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private val mUpdateTimeTask = Runnable {
        updateSeekBarAndTime()
    }

    private fun formatTime(millis: Int): String {
        var seconds = millis / 1000
        val minutes = seconds / 60
        seconds %= 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun fetchUserData() {
        val userReference = database.child("Users").child(currentUser.uid)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userData = snapshot.getValue(User::class.java)
                    userData?.let {
                        // Now userData contains the user's data (songs, images, etc.)
                        // Update your UI with the fetched data...
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error
            }
        })
    }
}
