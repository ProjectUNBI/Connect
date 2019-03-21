package com.unbi.connect

import java.util.*
import kotlin.collections.ArrayList

class MyMessage(val saltToadd: Salt/*This salt is to be add in nxt message if send*/,
                val saltToCheck: Salt/*This salt is to Check from the previously stored data*/,
                val sender: IpPort,
                val tag: String,
                val message: String,
                val extra: Extra,
                val isIntent: Boolean,
                val type: String,
                val uuidToadd: MsgUUID,
                val uuidToCheck: MsgUUID,
                val resultCode: Int = 0) {

}

class MsgUUID(var uuid: String = "", val milli: Long, val taskName: String) {

    fun generate(data: DataListUUID) {
        if (uuid.equals("")) {
            uuid = UUID.randomUUID().toString()
        }
        data.add(this)
    }

}

class DataListUUID {

    var lasttimepoped = System.currentTimeMillis()

    var arrayOfuuid: ArrayList<MsgUUID> = ArrayList()

    fun add(msgUUID: MsgUUID) {
        arrayOfuuid.add(msgUUID)
    }

    fun isValidUUid(msgUUID: MsgUUID): Boolean {//Calling this method more than once cause invalidUUid as it is removed from database
        if (lasttimepoped + MAXIMUM_TIME < System.currentTimeMillis()) {
            //this is not to pop again and again if the "isValid" is run within ten second
            popexpiredUUID()
        }
        var isvalid = false;
        val i = arrayOfuuid.iterator()
        while (i.hasNext()) {
            val msguuid = i.next() // must be called before you can call i.remove()
            // Do something
            if (msguuid.uuid.equals(msgUUID.uuid)) {
                isvalid = true
                i.remove()
            }
        }//end of while

        return isvalid

    }


    private fun popexpiredUUID() {//todo check if it is removing or not
        lasttimepoped = System.currentTimeMillis()
        val time = lasttimepoped - MAXIMUM_TIME
        val i = arrayOfuuid.iterator()
        while (i.hasNext()) {
            val msguuid = i.next() // must be called before you can call i.remove()
            // Do something
            if (msguuid.milli < time) {
                i.remove()
            }
        }
    }
}

class Extra(val key: String, val body: String)

class Salt(var saltString: String, var milli: Long) {

}

class IpPort(val ip: String, val port: Int)
