package com.unbi.connect.messaging

import android.util.Log
import com.google.gson.Gson
import com.unbi.connect.*
import com.unbi.connect.util_classes.AES_Util

/**
 * This Class should be a Single ton Class otherwaise SaltdataArray and pendingdataArray will not work
 */

/**
 * This class  is the Heart of this secure Communication
 *@param sendDataString it is the interface to send the message. the string should directly send
 *@param triggerTask this will pass messagetasktype which we will trigger
 *@param logger if you want some kind of loging mesage
 * this object has a listener function ... all the TCP,s input string {if bluetooth communicaton(bluetooth)} should pass
 *
 */
class Communicator(val sendDataString: SendDataString, val triggerTask: TriggerTask, val logger: Logger? = null) {
    val LOG_TAG = Communicator::class.java.name
    val SaltDataArray = DataList()
    val PendingMessageDataArray = DataList()

    /**
     * This Function should link directly to the mesge reciver
     */
    fun listenMessage(string: String) {
        //we should decrypt the dtring
        val decrypt = AES_Util().decrypt(string)
        if (decrypt == null) {
            logger?.show(LOG_TYPE_ERROR, "Decrypted Message is null/password missmatch")
            return
        }
        //we shold construct the message Object
        val msg = Gson().fromJson(decrypt, MyMessage::class.java)
        //now we are checking about message
        if (msg == null) {
            logger?.show(LOG_TYPE_ERROR, "Message is null/not valid format")
            return
        }

        val sndr = MyAddress(msg.sender, msg.commuType)//grabing the ender Adress

        //So message is not null
        //so lets check the message type is INIT type or not

        /**
         * first we will check if the salt is also null
         */
        if (msg.mtype == TYPE_INIT && msg.saltToCheck == null) {
            logger?.show(LOG_TYPE_ERROR, "Sender salt is null,sending a valid salt")
            val salttoadd = msg.saltToAdd
            val saltocheck = Salt(milli = System.currentTimeMillis()).generate(SaltDataArray)

            val message = MyMessage(
                    //dont forget to interchange salt from previous message
                    saltocheck,
                    salttoadd,
                    Userdata.instance.ipport,
                    null,
                    null,
                    null,
                    false,
                    TYPE_INIT,
                    null,
                    msg.uuidToadd,//dont forget to change the UUIDs
                    commuType = msg.commuType
            )
            dosendMessageBack(sndr, message)
            return
        }
        if (msg.mtype != TYPE_INIT && msg.saltToCheck == null) {
            logger?.show(LOG_TYPE_ERROR, "Invalid Salt(null salt)")
            return
            // we will not accept any type of message if salttoCheck is null from this
        }
        /**
         * secondly we will check if the salt to check is not null
         */
        if (msg.mtype == TYPE_INIT && msg.saltToCheck != null) {
            val valid = SaltDataArray.isValid(msg.saltToCheck as TimeBaseObject)
            /**
             * if not valid dont do anything
             */
            if (!valid) {
                logger?.show(LOG_TYPE_ERROR, "Invalid Salt")
                return
            }
            /**
             * Ok Salt is valid, so lets check if there is Pending messahges from the PendingMessageDataArray
             */

            logger?.show(LOG_TYPE_ERROR, "Valid salt...sending actual meaasge")
            val pendingmessage = PendingMessageDataArray
                    .getAndPopTimeBaseObject(msg.uuidToCheck?.uuid, PendingMessage::class.java)

            if (pendingmessage != null) {
                val tempmsg = pendingmessage.message
                val tobesend = MyMessage(
                        Salt(milli = System.currentTimeMillis()).generate(SaltDataArray),
                        msg.saltToAdd,
                        Userdata.instance.ipport,
                        tempmsg.tag,
                        tempmsg.message,
                        tempmsg.extra,
                        tempmsg.isIntent,
                        tempmsg.mtype,
                        null,
                        msg.uuidToadd,
                        tempmsg.taskName,
                        tempmsg.resultCode,
                        commuType = msg.commuType

                )
                dosendMessageBack(sndr, tobesend)
                //add to pending wait response

            } else {
                logger?.show(LOG_TYPE_ERROR, "No such pending message")
                Log.e(LOG_TAG, "No such pending message")
            }
            return
        }
        /**
         * Ok We have handlee all the INIT type message.. so dont pass
         * any INIT type message from here
         */
//        if (msg.tag == null) {
//            return//if there is no message tag then just return it
//        }
        if (!(msg.mtype.equals(TYPE_MESSAGE) || msg.mtype.equals(TYPE_RESPOSNE))) {
            //if the message type is not response or messgetype...return
            return
        }
        /**
         * i think the messge valisation is not check yet so lets check
         *
         */
        val valid = SaltDataArray.isValid(msg.saltToCheck as TimeBaseObject)
        if (!valid) {
            logger?.show(LOG_TYPE_ERROR, "Invalid Salt")
            return
        }
        triggerTask.triggered(msg)


    }

    fun sendMessage(message: MyMessage,address: MyAddress){
        PendingMessage(
                message,
                System.currentTimeMillis()
        ).addToPendings(PendingMessageDataArray)
        val salt_toadd = Salt(milli = System.currentTimeMillis()).generate(SaltDataArray)
        val initmesage = MyMessage(
                salt_toadd,
                null,
                Userdata.instance.ipport,
                null,
                null,
                null,
                false,
                TYPE_INIT,//message is init mtype
                message.uuidToadd,
                null,
                null
        )
        dosendMessageBack(address, initmesage)
    }

    /**
     * Private function
     */
    private fun dosendMessageBack(adress: MyAddress, message: MyMessage) {
        val encrypted = message.getEncryptedMsg()
        if (encrypted == null) {
            return
        }
        sendDataString.send(adress, encrypted)
        //this sendDataString sjould link diectly to the message Sender
    }


}

class MyAddress(val adress: Any, val type: Int = COMMUTYPE_WIFI)

interface SendDataString {
    fun send(address: MyAddress, string: String)
}

interface TriggerTask {
    fun triggered(message: MyMessage)
}