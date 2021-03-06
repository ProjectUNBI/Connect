package com.unbi.connect

import android.content.Context
import com.google.gson.Gson
import com.unbi.connect.messaging.*
import com.unbi.connect.plugin.event.EditActivityEventValues
import java.net.NetworkInterface
import java.security.MessageDigest
import java.util.*
import java.net.SocketException
import kotlin.collections.ArrayList
import android.text.format.Formatter.formatIpAddress
import android.content.Context.WIFI_SERVICE
import android.media.Ringtone
import android.net.wifi.WifiManager
import android.support.v4.content.ContextCompat.getSystemService
import java.lang.Exception
import java.math.BigInteger
import java.net.InetAddress


//never use "object" it is a trap... it will not work with Gson
class Userdata private constructor() {
    companion object {
        //        val instance = Userdata()
        var instance = Userdata()
    }

    val MY_PREFS_NAME: String = "com.unbi.connect.preferenece.Userdata"

    //instance variable
    var Trig_copytext: String = NULL_WORD
    var Trig_sendclip: String = NULL_WORD
    var Trig_findphone: String = NULL_WORD
    var isReadedfromSpref = false//to check from background service in tasker
    var isToast = true
    var ipport: IpPort = IpPort(FirstimeGetDeviceIpAddress(), 6868)
    set(value) {
        if(value.ip.equals(LOCAL_IP)){
            return
        }
        field=value
    }
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

            R.id.edit_ip_adress -> {
                ipport = IpPort(value as String, this.ipport.port)
            }
            R.id.edit_port_value -> {
                ipport = IpPort(this.ipport.ip, value as Int)
            }
            R.id.edit_password -> global_password = value as String
            R.id.switch_enable_custom_activity -> iscustomactivity = value as Boolean
            R.id.switch_enable_toast -> isToast = value as Boolean

            R.id.cardview_copy_text -> Userdata.instance.Trig_copytext = value as String
            R.id.cardview_send_clipboard -> Userdata.instance.Trig_sendclip = value as String
            R.id.cardview_find_phone -> Userdata.instance.Trig_findphone = value as String

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
        val data = Gson().fromJson(pref, Userdata::class.java)
        if (data == null) {
            return
        }
        /**
         * copying variable value
         *
         */
//        isToast = data.isToast
//        ipport = IpPort(getDeviceIpAddress(), data.ipport.port)
//        global_password = data.global_password
//        byte_global_password = data.byte_global_password
//        iscustomactivity = data.iscustomactivity
//        islogview_enable = data.islogview_enable
//        Trig_copytext= data.Trig_copytext
//        Trig_sendclip= data.Trig_sendclip
//        Trig_findphone= data.Trig_findphone
        /**
         * end of copying vaiable
         */

        /**Lets read the Ip adress
         * of the device
         */
        data.isReadedfromSpref = true
        Userdata.instance = data
    }


}


class ApplicationInstance private constructor() {
    companion object {
        val instance = ApplicationInstance()
    }

    var communicator: Communicator? = null
    var isCapturingMode: Boolean = false
    val pendingtaskertask = PendingTaskerTask()
    var isLogging = false

    //for find phone
    var prevNotiFindPhone:Int=INVALID_NOTI
    var rington:Ringtone?=null

}

class PendingTaskerTask {
    private var arrayTaskerPendings: ArrayList<MessageTimeObject> = ArrayList()
    private val lock = Any()
    fun addPending(message: MyMessage) {
        synchronized(lock) {
            arrayTaskerPendings.add(MessageTimeObject(message, System.currentTimeMillis()))
        }
    }

    fun isvalid(editActivity: EditActivityEventValues): Pair<Boolean, MyMessage?> {
        synchronized(lock) {
            popexpiredObject()
            val i = arrayTaskerPendings.iterator()
            while (i.hasNext()) {
                val mymessage = i.next() // must be called before you can call i.remove()
                // Do something
                var type = TYPE_MESSAGE
                if (editActivity.isResponse) {
                    type = TYPE_RESPOSNE
                }
                if (mymessage.message.mtype == type &&
                        (mymessage.message.tag == editActivity.TAG || editActivity.TAG == null || editActivity.TAG.equals("")) &&
                        (mymessage.message.message == editActivity.MSG || editActivity.MSG == null || editActivity.MSG.equals(
                                ""
                        ))
                ) {
                    i.remove()
                    return Pair(true, mymessage.message)
                }
            }//end of while
            return Pair(false, null)
        }
    }

    val EXPIRETIME = 1 * 60 * 1000//1minute

    private fun popexpiredObject() {

        val i = arrayTaskerPendings.iterator()
        while (i.hasNext()) {
            val mymessage = i.next() // must be called before you can call i.remove()
            // Do something
            if ((System.currentTimeMillis() - EXPIRETIME) > mymessage.milli) {
                i.remove()
            }
        }//end of while
    }
}


class MessageTimeObject(val message: MyMessage, val milli: Long)


fun FirstimeGetDeviceIpAddress(): String {
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
                    if (isThisMyIpAddress(inetAddress)) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        }
    } catch (e: SocketException) {
        e.stackTrace
    }
    return LOCAL_IP
}

fun isThisMyIpAddress(addr: InetAddress): Boolean {
    // Check if the address is a valid special local or loop back
    if (addr.isAnyLocalAddress || addr.isLoopbackAddress)
        return true

    // Check if the address is defined on any interface
    try {
        return NetworkInterface.getByInetAddress(addr) != null
    } catch (e: SocketException) {
        return false
    }

}