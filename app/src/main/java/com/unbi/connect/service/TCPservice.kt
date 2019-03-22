package com.unbi.connect.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.unbi.connect.*
import com.unbi.connect.activity.MainActivity
import com.unbi.connect.async.ServerAsync
import com.unbi.connect.uiclasses.OngoingNotificationBuilder
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import com.unbi.connect.messaging.MyMessage


class TCPservice : BaseService(), Listener, Logger {
    private val NOTY_ID_NOTSET=-99
    var serviceisNotStart: Boolean = true
    var mySocket: Socket? = null
    var socServer:ServerSocket?=null
    val binder = LocalBinder()
    var notiId: Int = NOTY_ID_NOTSET

    inner class LocalBinder : Binder() {

        internal// Return this instance of LocalService so clients can call public methods
        val service: BaseService
            get() = this@TCPservice
    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (serviceisNotStart) {
            showforeground()
            var serviceisNotStart: Boolean = false
        }
        if (intent == null) {
            return START_STICKY
        }
        val startedby = intent.getStringExtra(STARTED_FROM)
        if (startedby.equals(MainActivity::class.java.canonicalName)) {
            return START_STICKY//return as it is started from main activity
        }
        //todo parse message and send


        return START_STICKY;
    }



    fun restartServer() {
        socServer?.close()
        mySocket?.close()
        this.stopForeground(false);
        val ns = Context.NOTIFICATION_SERVICE
        val mNotificationManager = getSystemService(ns) as NotificationManager
        if (notiId !=NOTY_ID_NOTSET) {
            mNotificationManager.cancel(notiId)
        }
        showforeground()
    }

    private val toaster: Toaster?=null//todo set thsi value

    private fun showforeground() {

        Thread(Runnable {
            try {
                val Server = ServerSocket(Userdata.instance.ipport.port)
                socServer=Server
                while (true) {
                    mySocket = Server.accept()
                    val serverAsyncTask = ServerAsync(this, this,toaster)
                    serverAsyncTask.execute(mySocket)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
        val iconId = R.drawable.draw_lock_connection
        val title = "Address: " + Userdata.instance.ipport.ip + ":" + Userdata.instance.ipport.port
        val text_body = "Running background....."
        val notification = OngoingNotificationBuilder().buildOngoingNotification(
            applicationContext,
            TCPservice::class,
            iconId,
            title,
            text_body
        )
        this.notiId = getID()
        startForeground(this.notiId, notification)

    }

    override fun ActionComplete(message: MyMessage) {
        //todo trigge the valid mesaage to do taskes
    }

    override fun show(int: Int, msg: String) {
        return
        TODO("Todo to update the Looger view")
    }

}

