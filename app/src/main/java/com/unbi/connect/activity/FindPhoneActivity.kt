package com.unbi.connect.activity

import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.unbi.connect.R


class FindPhoneActivity : AppCompatActivity() {

    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_phone)
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }


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
        ringtone.stop()
        finish()

    }

    override fun onPause() {
        super.onPause()
        dismis()
    }


}
