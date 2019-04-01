package com.unbi.connect.activity

import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.unbi.connect.R


class FindPhoneActivity : AppCompatActivity() {
    private lateinit var player:MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_phone)
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        player = MediaPlayer.create(this, notification)
        player.setLooping(true)
        player.start()

    }
    fun okClick(view:android.view.View){
        dismis()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        dismis()
    }

    private fun dismis() {
        // stop music
        player.stop()
        finish()

    }

    override fun onPause() {
        super.onPause()
        dismis()
    }


}
