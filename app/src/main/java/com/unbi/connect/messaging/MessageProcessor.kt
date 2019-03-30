package com.unbi.connect.messaging

import android.util.Log
import com.google.gson.Gson
import com.unbi.connect.*
import com.unbi.connect.util_classes.AES_Util
import java.util.zip.InflaterInputStream


class MessageProcessor(string: String, logger: Logger) {
    val LOG_TAG = MessageProcessor::class.java.simpleName
    var messageTaskType = MessageTaskType(TO_NOT_TODO, null, null)
        private set

    init {
        performinit(string, logger)
    }

    private fun performinit(string: String, logger: Logger) {
        val decrypt = AES_Util().decrypt(string)
        if (decrypt == null) {
            logger.show(LOG_TYPE_ERROR, "Decrypted Message is null/password missmatch")
            return
        }
        val msg = Gson().fromJson(decrypt, MyMessage::class.java)
        //now we are checking about message
        if (msg == null) {
            logger.show(LOG_TYPE_ERROR, "Message is null/not valid format")
            return
        }
        val sndr = msg.sender
        if (msg.mtype == TYPE_INIT && msg.saltToCheck == null) {
            logger.show(LOG_TYPE_ERROR, "Sender salt is null,sending a valid salt")
            val salttoadd = msg.saltToAdd
            val saltocheck = Salt(milli = System.currentTimeMillis()).generate(ApplicationInstance.instance.SaltDataArray)

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
                    msg.uuidToadd//dont forget to change the UUIDs
            )
            messageTaskType = MessageTaskType(TO_REDIRECT, message, sndr)
            return
        }
        if(msg.mtype != TYPE_INIT && msg.saltToCheck==null){
            logger.show(LOG_TYPE_ERROR, "Invalid Salt(null salt)")
            return
        }

        if (msg.mtype == TYPE_INIT && msg.saltToCheck != null) {
            val valid = ApplicationInstance.instance.SaltDataArray.isValid(msg.saltToCheck as TimeBaseObject)
            if (!valid) {
                logger.show(LOG_TYPE_ERROR, "Invalid Salt")
                return
            }

            logger.show(LOG_TYPE_ERROR, "Valid salt...sending actual meaasge")
            val pendingmessage = ApplicationInstance.instance
                    .PendingMessageDataArray
                    .getAndPopTimeBaseObject(msg.uuidToCheck?.uuid, PendingMessage::class.java)

            if (pendingmessage != null) {
                val tempmsg = pendingmessage.message
                val tobesend = MyMessage(
                        Salt(milli = System.currentTimeMillis()).generate(ApplicationInstance.instance.SaltDataArray),
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
                        tempmsg.resultCode
                        )

                messageTaskType = MessageTaskType(TO_REDIRECT, tobesend, sndr)
                //add to pending wait response

            } else {
                logger.show(LOG_TYPE_ERROR, "No such pending message")
                Log.e(LOG_TAG, "No such pending message")
            }
            return
        }
        if (msg.mtype == TYPE_RESPOSNE || msg.mtype == TYPE_MESSAGE) {
            // we will check the salt is valid in the response
            messageTaskType = MessageTaskType(TO_TRIGGER, msg, sndr)
        }
    }
}

class MessageTaskType(val type: Int, val message: MyMessage?, val thesender: IpPort?)