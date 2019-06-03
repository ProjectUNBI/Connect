package com.unbi.connect

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.unbi.connect.service.TCPservice
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import com.unbi.connect.messaging.IpPort


class MyBroadCastReciever : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (!Userdata.instance.isReadedfromSpref) {
            Userdata.instance.readfromSpref(context)
        }

        val actionId = intent?.action;
        var pusintent: Intent? = null
        when (actionId) {
            BOOTCOMPLETE -> pusintent = Intent(context, TCPservice::class.java)
            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                //you need to read User preference after the boot
                Userdata.instance.readfromSpref(context)
//                val info: NetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO)
//                if (context == null) {
//                    return
//                }
//                val isWifiStateChanged: Boolean
//                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                    val network = connectivityManager.activeNetwork
//                    val capabilities = connectivityManager.getNetworkCapabilities(network)
//                    isWifiStateChanged = (capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)))
//                } else {
//                    isWifiStateChanged = connectivityManager.activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI
//                }
//                if (isWifiStateChanged) {
//                     if (!getDeviceIpAddress().equals(Userdata.instance.ipport.ip)) {
//                        Userdata.instance.ipport = IpPort(getDeviceIpAddress(), Userdata.instance.ipport.port)
//                        //todo update all ui of IP address
//                         pusintent = Intent(context, TCPservice::class.java)
//                         pusintent.putExtra(TCP_SERVICE_EXTRA,TCPSERVICE_RESTART)
////                        Toast.makeText(context, "IP:"+getDeviceIpAddress(), LENGTH_LONG).show()
//                    }
//                }
            }
        }
        if (pusintent != null && context != null) {
            context.startService(pusintent)//starting the service when boot start
        }


    }
}