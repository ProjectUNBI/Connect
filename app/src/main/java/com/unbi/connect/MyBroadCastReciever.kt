package com.unbi.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unbi.connect.service.TCPservice

class MyBroadCastReciever:BroadcastReceiver(){

    override fun onReceive(context: Context?, intent: Intent?) {

        val actionId=intent?.action;
        var pusintent:Intent?=null
        when(actionId){
            BOOTCOMPLETE->pusintent= Intent(context,TCPservice::class.java)
        }
        if(pusintent!=null&&context!= null){
            context.startService(pusintent)//starting the service when boot start
        }
    }
}