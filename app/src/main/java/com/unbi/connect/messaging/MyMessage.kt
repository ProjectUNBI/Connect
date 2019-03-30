package com.unbi.connect.messaging

import android.util.Log
import com.google.gson.Gson
import com.unbi.connect.*
import com.unbi.connect.async.ClientAsync
import com.unbi.connect.util_classes.AES_Util
import java.io.*
import java.lang.Exception
import java.net.InetAddress
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MyMessage(
    val saltToAdd: Salt?/*This salt is to be add in nxt message if sendAsync*/,
    val saltToCheck: Salt?/*This salt is to Check from the previously stored data*/,
    val sender: IpPort,
    val tag: String?,
    val message: String?,
    val extra: Extra?,
    val isIntent: Boolean,
    val mtype: Int,
    val uuidToadd: MsgUUID?,
    val uuidToCheck: MsgUUID?,
    val taskName: String? = null,
    val resultCode: Int = RESULT_UNKNOWN
) {
    fun getEncryptedMsg(): String? {
        return AES_Util().encrypt(Gson().toJson(this))
    }

    fun sendAsync(ipport: IpPort?, toaster: Toaster?, logger: Logger?) {
        val async = ClientAsync(ipport, toaster, logger)
        async.execute(this)
    }

    fun send(ipPort: IpPort?, toaster: Toaster?, logger: Logger?) {

        val stringTosend = this.getEncryptedMsg()

        try {
            if (ipPort == null) {
                logger?.show(LOG_TYPE_ERROR, "Null ip and port")
                toaster?.show("Null ip and port")
                Log.e(MyMessage::class.java.simpleName, "Null ip and port")
                return
            }
            val serverAddr = InetAddress.getByName(ipPort.ip)
            val socket = Socket(serverAddr, ipPort.port)

            //made connection, setup the read (in) and write (out)
            val out = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))

            try {
                //write a message to the server
                out.println(stringTosend)
                //read back a message from the server.
                val str = input.readLine()
                out.flush()
            } catch (e: Exception) {
                logger?.show(LOG_TYPE_ERROR, e.message.toString())
                toaster?.show(e.message)
                Log.e(MyMessage::class.java.simpleName, e.message)

            } finally {
                input.close()
                out.close()
                socket.close()
            }

        } catch (e: Exception) {
            logger?.show(LOG_TYPE_ERROR, e.message.toString())
            toaster?.show(e.message)
            Log.e(MyMessage::class.java.simpleName, e.message)
        }
        logger?.show(LOG_TYPE_ERROR, "Suucessfully sent to: " + this.sender.ip + ":" + this.sender.port)
        toaster?.show("success")
    }

}

open class TimeBaseObject(val milli: Long)

class PendingMessage(val message: MyMessage, milli: Long) : TimeBaseObject(milli) {
    fun addToPendings(data: DataList) {
        data.popexpiredObject()
        data.add(this)

    }
}


class MsgUUID(var uuid: String? = "") {

    fun generate() {
        if (uuid.equals("") || uuid == null) {
            uuid = UUID_PREFIX + UUID.randomUUID().toString()
        }

    }

}


class DataList {

    var lasttimepoped = System.currentTimeMillis()

    var arrayOftbObject: ArrayList<TimeBaseObject> = ArrayList()
    private val lock = Any()

    fun <T : TimeBaseObject> getAndPopTimeBaseObject(identifier: String?, kclass: Class<T>): T? {
        synchronized(lock) {
            val nameKlass = kclass.canonicalName
            if (nameKlass == null) {
                return null
            }
            val i = arrayOftbObject.iterator()
//        var objectoreturn: TimeBaseObject? = null
            if (nameKlass.equals(PendingMessage::class.java.canonicalName)) {
                while (i.hasNext()) {
                    val dataobject = i.next() // must be called before you can call i.remove()
                    // Do something
                    if ((dataobject as PendingMessage).message.uuidToadd?.uuid.equals(identifier)) {
                        i.remove()
                        return dataobject as T

                    }
                }//end of while
            }
            if (nameKlass.equals(Salt::class.java.canonicalName)) {

                while (i.hasNext()) {
                    val dataobject = i.next() // must be called before you can call i.remove()
                    // Do something
                    if ((dataobject as Salt).saltString.equals(identifier)) {
                        i.remove()
                        return dataobject as T
                    }
                }//end of while

            }
            return null
        }
    }


    fun add(timeBaseObject: TimeBaseObject) {
        synchronized(lock) {
            var existed = false
            //i dont know if i should add the following code
            val i = arrayOftbObject.iterator()

            if (timeBaseObject is Salt) {
                while (i.hasNext()) {
                    val dataobject = i.next() // must be called before you can call i.remove()
                    // Do something
                    if (timeBaseObject.saltString.equals((dataobject as Salt).saltString)) {
                        existed = true
                    }
                }//end of while

            }
            if (timeBaseObject is PendingMessage) {
                while (i.hasNext()) {
                    val dataobject = i.next() // must be called before you can call i.remove()
                    // Do something
                    if (timeBaseObject.message.uuidToadd?.uuid.equals((dataobject as PendingMessage).message.uuidToadd?.uuid)) {
                        existed = true
                    }
                }//end of while


            }


            /////////////////////////////////////
            if (!existed) {
                arrayOftbObject.add(timeBaseObject)
            }
        }
    }

    fun isValid(timeBaseObject: TimeBaseObject): Boolean {//Calling this method more than once cause invalidUUid as it is removed from database
        synchronized(lock) {
            if (lasttimepoped + MAXIMUM_TIME < System.currentTimeMillis()) {
                //this is not to pop again and again if the "isValid" is run within ten second
                popexpiredObjectNonSync()
            }
            var isvalid = false;
            val i = arrayOftbObject.iterator()

            if (timeBaseObject is Salt) {
                while (i.hasNext()) {
                    val dataobject = i.next() // must be called before you can call i.remove()
                    // Do something
                    if (timeBaseObject.saltString.equals((dataobject as Salt).saltString)) {
                        isvalid = true
                        i.remove()
                    }
                }//end of while

            }
            return isvalid
        }
    }

    /*
    I am afraid of death lock so i am making another faunction to be called from a Synchronise method
     */
    fun popexpiredObjectNonSync() {//todo check if it is removing or not
            lasttimepoped = System.currentTimeMillis()
            val time = lasttimepoped - MAXIMUM_TIME
            val i = arrayOftbObject.iterator()
            while (i.hasNext()) {
                val timeBaseObject = i.next() // must be called before you can call i.remove()
                // Do something
                if (timeBaseObject.milli < time) {
                    i.remove()
                }
            }

    }
    fun popexpiredObject() {//todo check if it is removing or not
        synchronized(lock) {
            lasttimepoped = System.currentTimeMillis()
            val time = lasttimepoped - MAXIMUM_TIME
            val i = arrayOftbObject.iterator()
            while (i.hasNext()) {
                val timeBaseObject = i.next() // must be called before you can call i.remove()
                // Do something
                if (timeBaseObject.milli < time) {
                    i.remove()
                }
            }
        }
    }
}


class Salt(var saltString: String? = "", milli: Long) : TimeBaseObject(milli) {

    fun generate(data: DataList): Salt {
        if (saltString.equals("")) {
            saltString = SALT_PREFIX + UUID.randomUUID().toString()
        }
        data.add(this)
        return this
    }

}


class Extra(val hash: HashMap<String, String>)


class IpPort(val ip: String, val port: Int) {
    companion object {
        fun generate(string: String): IpPort? {
            try {
                val array = string.split(":")
                return IpPort(array[0], array[1].toInt())
            } catch (e: Exception) {
                return null
            }

        }
    }
}


/*
//class DataListUUID {
//
//    var lasttimepoped = System.currentTimeMillis()
//
//    var arrayOfuuid: ArrayList<MsgUUID> = ArrayList()
//
//    fun add(msgUUID: MsgUUID) {
//        arrayOfuuid.add(msgUUID)
//    }
//
//    fun isValidUUid(msgUUID: MsgUUID): Boolean {//Calling this method more than once cause invalidUUid as it is removed from database
//        if (lasttimepoped + MAXIMUM_TIME < System.currentTimeMillis()) {
//            //this is not to pop again and again if the "isValid" is run within ten second
//            popexpiredObject()
//        }
//        var isvalid = false;
//        val i = arrayOfuuid.iterator()
//        while (i.hasNext()) {
//            val msguuid = i.next() // must be called before you can call i.remove()
//            // Do something
//            if (msguuid.uuid.equals(msgUUID.uuid)) {
//                isvalid = true
//                i.remove()
//            }
//        }//end of while
//
//        return isvalid
//
//    }
//
//    private fun popexpiredObject() {//todo check if it is removing or not
//        lasttimepoped = System.currentTimeMillis()
//        val time = lasttimepoped - MAXIMUM_TIME
//        val i = arrayOfuuid.iterator()
//        while (i.hasNext()) {
//            val msguuid = i.next() // must be called before you can call i.remove()
//            // Do something
//            if (msguuid.milli < time) {
//                i.remove()
//            }
//        }
//    }
//}

//class DataListSalt {
//
//    var lasttimepoped = System.currentTimeMillis()
//
//    var arrayOfSalt: ArrayList<Salt> = ArrayList()
//    fun add(salt: Salt){
//        arrayOfSalt.add(salt)
//    }
//
//    fun isValidSalt(salt: Salt){
//        if (lasttimepoped + MAXIMUM_TIME < System.currentTimeMillis()) {
//            //this is not to pop again and again if the "isValid" is run within ten second
//            popexpiredSalt()
//        }
//
//    }
//
//    private fun popexpiredSalt() {
//        lasttimepoped=System.currentTimeMillis()
//        val time =lasttimepoped- MAXIMUM_TIME
//        val i=arrayOfSalt
//
//    }
//}
*/