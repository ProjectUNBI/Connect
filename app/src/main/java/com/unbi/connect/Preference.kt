package com.unbi.connect

import android.content.Context
import com.google.gson.Gson
import com.unbi.connect.messaging.DataList
import com.unbi.connect.messaging.IpPort
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.*
import java.net.SocketException


//never use "object" it is a trap... it will not work with Gson
class Userdata private constructor() {
    companion object {
        val instance = Userdata()
    }

    val MY_PREFS_NAME: String = "com.unbi.connect.preferenece.Userdata"

    //instance variable
    var isReadedfromSpref=false//to check from background service in tasker
    var isToast=true
    var ipport: IpPort = IpPort(getDeviceIpAddress(), 6868)
    var global_password: String = ""
        set(value) {
            field = value
            if (global_password.equals("")) {
                return
            }
            var key = global_password.toByteArray()
            val sha = MessageDigest.getInstance("SHA-256")
            key = sha.digest(key)
            byte_global_password = Arrays.copyOf(key, 32)//todo change it
        }
    var byte_global_password: ByteArray? = null
        private set
    var iscustomactivity = false
    var islogview_enable = false

    init {
        global_password = generatePassword()
    }

    //functions......
    fun save(applicationContext: Context?, id: Int, value: Any) {
        when (id) {

            R.id.edit_port_value -> {
                ipport = IpPort(getDeviceIpAddress(), value as Int)
            }
            R.id.edit_password -> global_password = value as String
            R.id.switch_enable_custom_activity -> iscustomactivity = value as Boolean
            R.id.switch_enable_toast->isToast=value as Boolean
        }
        if (applicationContext == null) return
        val editor = applicationContext.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE).edit()
        val string = Gson().toJson(this)
        editor.putString(Userdata::class.java.name, string)
        editor.apply()
    }

    fun readfromSpref(applicationContext: Context?) {
        if (applicationContext == null) {
            return
        }
        val prefs = applicationContext.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
        val pref = prefs.getString(Userdata::class.java.name, null)
        val userdata = Gson().fromJson(pref, Userdata::class.java)
        if (userdata == null) {
            return
        }
        //copying variable valuer
        isToast=userdata.isToast
        ipport = IpPort(getDeviceIpAddress(), userdata.ipport.port)
        global_password = userdata.global_password
        byte_global_password = userdata.byte_global_password
        iscustomactivity = userdata.iscustomactivity
        islogview_enable = userdata.islogview_enable

    }


}


class ApplicationInstance private constructor() {
    companion object {
        val instance = ApplicationInstance()
    }

    val SaltDataArray = DataList()
    val PendingMessageDataArray = DataList()
    val PendingResultArray=DataList()
    val isCapturingMode: Boolean=false

}

fun getDeviceIpAddress():String{
    try {
        val enumeration = NetworkInterface
            .getNetworkInterfaces()
        while (enumeration.hasMoreElements()) {
            val networkInterface = enumeration.nextElement()
            val enumerationIpAddr = networkInterface
                .getInetAddresses()
            while (enumerationIpAddr
                    .hasMoreElements()
            ) {
                val inetAddress = enumerationIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().size == 4) {
                    return inetAddress.getHostAddress()
                }
            }
        }
    } catch (e: SocketException) {
        e.stackTrace

    }
    return  LOCAL_IP
}