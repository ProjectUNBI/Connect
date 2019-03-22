package com.unbi.connect.messaging

import com.google.gson.Gson
import com.unbi.connect.*
import com.unbi.connect.async.ClientAsync
import com.unbi.connect.util_classes.AES_Util
import java.util.*
import kotlin.collections.ArrayList


class MyMessage(
    val saltToAdd: Salt/*This salt is to be add in nxt message if send*/,
    val saltToCheck: Salt?/*This salt is to Check from the previously stored data*/,
    val sender: IpPort,
    val tag: String?,
    val message: String?,
    val extra: Extra?,
    val isIntent: Boolean,
    val type: Int,
    val uuidToadd: MsgUUID?,
    val uuidToCheck: MsgUUID,
    val taskName: String?=null,
    val resultCode: Int = RESULT_UNKNOWN
) {
    fun getEncryptedMsg(): String? {
        return AES_Util().encrypt(Gson().toJson(this))
    }

    fun send(toaster: Toaster?, logger: Logger) {//maybe a time consuming tasek
        val async=ClientAsync(toaster,logger)
        async.execute(this)
    }

}

open class TimeBaseObject(val milli: Long)

class PendingMessage(val message: MyMessage, milli: Long) : TimeBaseObject(milli) {
    fun addToPendings(data: DataList) {
        data.popexpiredObject()
        data.add(this)

    }
}


class MsgUUID(var uuid: String = "", val taskName: String) {

    fun generate(data: DataList) {
        if (uuid.equals("")) {
            uuid = UUID_PREFIX + UUID.randomUUID().toString()
        }
    }

}
//class MsgUUID(var uuid: String = "", milli: Long, val taskName: String) : TimeBaseObject(milli) {
//
//    fun generate(data: DataList) {
//        if (uuid.equals("")) {
//            uuid = UUID_PREFIX+UUID.randomUUID().toString()
//        }
//        data.add(this)
//    }
//
//}

class DataList {

    var lasttimepoped = System.currentTimeMillis()

    var arrayOftbObject: ArrayList<TimeBaseObject> = ArrayList()


    fun <T : TimeBaseObject> getAndPopTimeBaseObject(identifier: String?, kclass: Class<T>): T? {
        val nameKlass = kclass.canonicalName
        if (nameKlass == null) {
            return null
        }
        val i = arrayOftbObject.iterator()
        var objectoreturn: TimeBaseObject? = null
        if (nameKlass.equals(PendingMessage::class.java.canonicalName)) {
            while (i.hasNext()) {
                val dataobject = i.next() // must be called before you can call i.remove()
                // Do something
                if ((dataobject as PendingMessage).message.uuidToCheck.equals(identifier)) {
                    return dataobject as T

                }
            }//end of while
        }
        if (nameKlass.equals(Salt::class.java.canonicalName)) {

            while (i.hasNext()) {
                val dataobject = i.next() // must be called before you can call i.remove()
                // Do something
                if ((dataobject as Salt).saltString.equals(identifier)) {
                    return dataobject as T
                }
            }//end of while

        }
        return null
    }


    fun add(timeBaseObject: TimeBaseObject) {
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
                if (timeBaseObject.message.uuidToCheck.uuid.equals((dataobject as PendingMessage).message.uuidToCheck.uuid)) {
                    existed = true
                }
            }//end of while


        }


        /////////////////////////////////////
        if (!existed) {
            arrayOftbObject.add(timeBaseObject)
        }
    }

    fun isValid(timeBaseObject: TimeBaseObject): Boolean {//Calling this method more than once cause invalidUUid as it is removed from database
        if (lasttimepoped + MAXIMUM_TIME < System.currentTimeMillis()) {
            //this is not to pop again and again if the "isValid" is run within ten second
            popexpiredObject()
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

    fun popexpiredObject() {//todo check if it is removing or not
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


class Salt(var saltString: String = "", milli: Long) : TimeBaseObject(milli) {

    fun generate(data: DataList): Salt {
        if (saltString.equals("")) {
            saltString = SALT_PREFIX + UUID.randomUUID().toString()
        }
        data.add(this)
        return this
    }

}


class Extra(val key: String, val body: String)


class IpPort(val ip: String, val port: Int)


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