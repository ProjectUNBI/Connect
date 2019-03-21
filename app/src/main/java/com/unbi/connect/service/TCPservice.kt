package com.unbi.connect.service

import android.content.Intent
import android.os.Bundle
import android.os.AsyncTask.execute
import android.system.Os.accept
import android.net.Proxy.getPort
import com.unbi.connect.R
import com.unbi.connect.Userdata
import com.unbi.connect.getID
import com.unbi.connect.uiclasses.OngoingNotificationBuilder
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket


class TCPservice :BaseService(){

    var serviceisNotStart:Boolean=true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        if(serviceisNotStart){
            Thread(Runnable {
                try {
                    val socServer = ServerSocket(Userdata.port)
                    var socClient: Socket? = null
                    while (true) {
                        socClient = socServer.accept()
                        val serverAsyncTask = ServerAsyncTask(myTCPinterface)
                        serverAsyncTask.execute(arrayOf<Socket>(socClient))
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }).start()
            val iconId= R.drawable.draw_lock_connection
            val title="Connect"
            val text_body="Running background....."
            val notification=OngoingNotificationBuilder().buildOngoingNotification(
                    applicationContext,
                    TCPservice::class,
                    iconId,
                    title,
                    text_body
            )
            startForeground(getID(),notification)
        }
        return START_STICKY;
    }

}