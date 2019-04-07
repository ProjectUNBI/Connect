package com.unbi.connect.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.unbi.connect.*
import com.unbi.connect.activity.MainActivity
import com.unbi.connect.async.ServerAsync
import com.unbi.connect.uiclasses.OngoingNotificationBuilder
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import com.unbi.connect.plugin.event.EventEditActivity
import com.unbi.connect.util_classes.CustomActivityProcessor
import com.unbi.connect.TaskerPlugin
import com.unbi.connect.activity.ServiceToActivity
import com.unbi.connect.async.AssyncViewUpdater
import com.unbi.connect.async.ClientAsync
import com.unbi.connect.messaging.*


class TCPservice : BaseService(), TriggerTask, Logger, SendDataString, Toaster {

    val INTENT_REQUEST_REQUERY = Intent(
        com.twofortyfouram.locale.api.Intent.ACTION_REQUEST_QUERY
    ).putExtra(
        com.twofortyfouram.locale.api.Intent.EXTRA_STRING_ACTIVITY_CLASS_NAME,
        EventEditActivity::class.java.getName()
    )
    ////////////////
    private val NOTY_ID_NOTSET = -99
    var serviceisNotStart: Boolean = true
    var mySocket: Socket? = null
    var socServer: ServerSocket? = null
    val binder = LocalBinder()
    var notiId: Int = NOTY_ID_NOTSET
    var servicToActivity: ServiceToActivity? = null

    inner class LocalBinder : Binder() {

        internal// Return this instance of LocalService so clients can call public methods
        val service: BaseService
            get() = this@TCPservice
    }


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ApplicationInstance.instance.communicator == null) {
            ApplicationInstance.instance.communicator = Communicator(this)
        }
        /**
         * Lets check if the bundle has some exxtra and if this has no extra just leave
         * if extra then we have to restart the service
         * or stop the playing find phone sound if started
         */
        val extra_string = intent?.getStringExtra(TCP_SERVICE_EXTRA)
        when (extra_string) {
            TCPSERVICE_RESTART -> {
                //restart the service
                restartServer()
                return START_STICKY
            }
            TCPSERVICE_NOTIDISMISSED -> {
                ApplicationInstance.instance.rington?.stop()
                return START_STICKY
            }

        }

        //////////////
        if (serviceisNotStart) {
            showforeground()
            serviceisNotStart = false
        }
        if (intent == null) {
            return START_STICKY
        }
        val startedby = intent.getStringExtra(STARTED_FROM)
        if (startedby != null && startedby.equals(MainActivity::class.java.canonicalName)) {
            return START_STICKY//return as it is started from main activity
        }
        return START_STICKY
    }


    fun restartServer() {
        socServer?.close()
        mySocket?.close()
        this.stopForeground(false);
        val ns = Context.NOTIFICATION_SERVICE
        val mNotificationManager = getSystemService(ns) as NotificationManager
        if (notiId != NOTY_ID_NOTSET) {
            mNotificationManager.cancel(notiId)
        }
        showforeground()
    }


    private fun showforeground() {

        Thread(Runnable {
            try {
                val Server = ServerSocket(Userdata.instance.ipport.port)
                socServer = Server
                while (true) {
                    mySocket = Server.accept()
                    val serverAsyncTask = ServerAsync(this, this, this)
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
            iconId,
            title,
            text_body
        )
        this.notiId = getID()
        startForeground(this.notiId, notification)

    }


    override fun triggered(message: MyMessage, asyncUpdater: AssyncViewUpdater?) {
        if (ApplicationInstance.instance.isCapturingMode) {
            servicToActivity?.sendtoActivity(ServiceToActivity.DEFAULT, message)
            return
        }
        if (Userdata.instance.iscustomactivity) {
            val customActivityProcessor = CustomActivityProcessor(message)
            if (customActivityProcessor.isTriggered) {
                customActivityProcessor.performIt(applicationContext, asyncUpdater)
                return
            }
        }
        //todo trigge the valid mesaage to do taskes
        if (message.isIntent) {
//            TODO("SENT AS INTENT")
            if (message.tag == null) {
                return
            }
            val intent = Intent()
            intent.setAction(message.tag)
            prepairemessageforIntent(intent, message)
            sendBroadcast(intent)
            return
        }

        ApplicationInstance.instance.pendingtaskertask.addPending(message)
        TaskerPlugin.Event.addPassThroughMessageID(INTENT_REQUEST_REQUERY)
        applicationContext.sendBroadcast(INTENT_REQUEST_REQUERY)
        return
    }


    override fun show(int: Int, msg: String) {

        if (!ApplicationInstance.instance.isLogging) {
            return
        }
        servicToActivity?.sendtoActivity(ServiceToActivity.LOG_DATA, int, msg)
    }

    override fun show(msg: String?) {
        if (!Userdata.instance.isToast) {
            return
        }
        Toast.makeText(applicationContext, msg, LENGTH_SHORT).show()
    }

    private fun prepairemessageforIntent(intent: Intent, myMessage: MyMessage) {
        if (myMessage.mtype == TYPE_INIT) {
            intent.putExtra("type", "init")
        }
        if (myMessage.mtype == TYPE_RESPOSNE) {
            intent.putExtra("type", "response")
            if (myMessage.resultCode == RESULT_SUCCESS) {
                intent.putExtra("result", "success")
            }
            if (myMessage.resultCode == RESULT_FAILURE) {
                intent.putExtra("result", "fail")
            }
            if (myMessage.resultCode == RESULT_UNKNOWN) {
                intent.putExtra("result", "unknown")
            }
        }
        if (myMessage.mtype == TYPE_MESSAGE) {
            intent.putExtra("type", "message")
        }
        intent.putExtra("tag", myMessage.tag)
        intent.putExtra("message", myMessage.message)
        intent.putExtra("taskname", myMessage.taskName)
        intent.putExtra("senderip", myMessage.sender.ip)
        intent.putExtra("senderport", myMessage.sender.port.toString())
        intent.putExtra("msgid", myMessage.uuidToCheck?.uuid)
        intent.putExtra("msgidtoadd", myMessage.uuidToadd?.uuid)

        val extra = myMessage.extra?.hash ?: return
        for ((key, value) in extra) {
            var temp = key.replace("%", "")
            temp = key.replace(" ", "")
            if (temp.isNotEmpty()) {
                intent.putExtra(temp, value)
            }
        }

    }

    override fun send(address: MyAddress, string: String) {
        when (address.type) {
            COMMUTYPE_WIFI -> {
                //todo send via wifi
                val client = ClientAsync(address.adress as IpPort, this, this)
                client.execute(string)
            }
        }
    }

}

