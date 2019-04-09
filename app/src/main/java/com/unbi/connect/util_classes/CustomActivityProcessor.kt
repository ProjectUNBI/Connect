package com.unbi.connect.util_classes

import android.content.Context
import android.content.Intent
import com.unbi.connect.messaging.MyMessage
import com.unbi.connect.*
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.media.RingtoneManager
import android.support.v4.app.NotificationManagerCompat
import com.google.gson.Gson
import com.unbi.connect.async.AssyncViewUpdater
import com.unbi.connect.async.AssyncViewUpdater.Companion.LOGTYPE
import com.unbi.connect.async.AssyncViewUpdater.Companion.TOASTYPE
import com.unbi.connect.messaging.MyAddress
import com.unbi.connect.service.TCPservice
import com.unbi.connect.uiclasses.OngoingNotificationBuilder


class CustomActivityProcessor(val message: MyMessage) {
    companion object {
        val FINDPHONE = 1
        val COPYTEXT = 2
        val SENDCLIP = 3
        val UNKNOWN = -1
    }

    //to check the MyMessage Object in constructor is consume when there is
    // a matched in the custum task, we introduce 'isTriggered'
    var isTriggered: Boolean = false
    //'trigerWhat'-> to identified which task has been triggered
    var trigerWhat = UNKNOWN

    init {
        /**
         * In this init, we find out which custom tasked is triggered
         * if triggered we set the value of the isTriggered to 'true'
         * the 'triggereWhat' is also set here
         */
        if (!message.tag.equals(NULL_WORD)) {
            if (message.tag.equals(Userdata.instance.Trig_sendclip)) {
                isTriggered = true
                trigerWhat = SENDCLIP
            }
            if (message.tag.equals(Userdata.instance.Trig_findphone)) {
                isTriggered = true
                trigerWhat = FINDPHONE
            }
            if (message.tag.equals(Userdata.instance.Trig_copytext)) {
                isTriggered = true
                trigerWhat = COPYTEXT
            }
        }
    }

    /**
     * @param context contect of the TCP service
     * @param async AssyncViewUpdate where this methods is called
     * we use this async task so that the published activity to
     * macke change in the UI threads
     * if we dont use async task we will catch error when changing the UI of the
     * app
     */
    fun performIt(context: Context, async: AssyncViewUpdater?) {
        var msg: String? = null
        when (trigerWhat) {
            FINDPHONE -> {
                msg = STR_FINDPHONE
                dofindphone(context)
                async?.mypublish(LOGTYPE, LOG_TYPE_SUCCESS, "Phone finding activity Launched")
            }
            COPYTEXT -> {
                msg = STR_COPYTOCLIP
                //get message
                val msgcontent = message.message
                // copy clipboard data
                copyToClipboard(context, msgcontent)
                async?.mypublish(LOGTYPE, LOG_TYPE_SUCCESS, "Successfully copied: ${msgcontent}")
                //toast copy if toastable
                async?.mypublish(TOASTYPE, "Copied texts")

            }
            SENDCLIP -> {
                msg = null// actually we will send message
                performSendClip(context, async)
                async?.mypublish(TOASTYPE, "Sent clipboard contents")
            }

        }
        if (msg == null) {
            return
        }

        /**
         * the folowing code send a Resoponse type message to the sender adress so that
         * 'the task is performed' should be notified
         */
        val response = MyMessage(
            null,//we dont want any response so
            message.saltToAdd,//i think we can reuse the salt
            Userdata.instance.ipport,
            null,
            msg,
            null,
            false,
            TYPE_RESPOSNE,// change it if we want the tag to be shown in eventghost
            null,
            message.uuidToadd,
            message.taskName,
            RESULT_SUCCESS
        )

        val adddres = MyAddress(message.sender, response.commuType)
        ApplicationInstance.instance.communicator?.sendMessage(response, adddres)
    }

    /**
     * this method id to ring the phone and show the notification
     */
    private fun dofindphone(context: Context) {
        if (ApplicationInstance.instance.prevNotiFindPhone != INVALID_NOTI) {
            //todo poppe the previouse notification
            ApplicationInstance.instance.rington?.stop()//stop the music
            with(NotificationManagerCompat.from(context)) {
                cancel(ApplicationInstance.instance.prevNotiFindPhone)
            }

        }

        val iconId = R.drawable.ic_my_icon
        val title = "Hello"
        val text_body = "I am here..."


        val myIntent = Intent(context, TCPservice::class.java).apply {
            putExtra(TCP_SERVICE_EXTRA, TCPSERVICE_NOTIDISMISSED)
        }
        val deleteintent = PendingIntent.getService(context, 0, myIntent, 0)

        val notification = OngoingNotificationBuilder().buildOngoingNotification(
            context,
            iconId,
            title,
            text_body,
            isAutoCancel = true,
            deletIntent = deleteintent,
            clickIntent = deleteintent
        ) ?: return
        ApplicationInstance.instance.prevNotiFindPhone = getID()
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(ApplicationInstance.instance.prevNotiFindPhone, notification)
        }
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ApplicationInstance.instance.rington = RingtoneManager.getRingtone(context, uri)
        ApplicationInstance.instance.rington?.play()
    }

    /**
     * @param msgcontent it is the ''message of the MyMessage object
     * it is copied to the android clipboard
     */
    private fun copyToClipboard(context: Context, msgcontent: String?) {
        if (msgcontent == null) {
            return
        }
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText("label", msgcontent)
        clipboard?.setPrimaryClip(clip)
    }


    /**
     * The function send the Clipboard Content to the sender
     */
    private fun performSendClip(context: Context, async: AssyncViewUpdater?) {
        /**
         * The folowing grab the text content in the Clipboard content an store
         * in the variable "pasteData
         */
        val clipboard: ClipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData= ""
        val descrip = clipboard.primaryClipDescription
        if (descrip != null) {
            when {
                !clipboard.hasPrimaryClip() -> {
                    pasteData = NULL_WORD
                }
                !(descrip.hasMimeType(MIMETYPE_TEXT_PLAIN)) -> {
                    // This disables the paste menu item, since the clipboard has data but it is not plain text
                    pasteData = NULL_WORD
                }
                else -> {
                    // This enables the paste menu item, since the clipboard contains plain text.
                    val item = clipboard.primaryClip?.getItemAt(0)
                    if (item != null) {
                        pasteData = item.text.toString()
                    } else {
                        pasteData = NULL_WORD
                    }

                }
            }
        } else {
            pasteData = NULL_WORD
        }

        /**
         * Now we get the ClipBoard content
         * So we are sending baack to the other device
         *
         * The folowing is the sending message process\
         *
         */
        if (pasteData.equals(NULL_WORD)) {
            async?.mypublish(LOGTYPE, LOG_TYPE_WARNING, NULL_WORD)
        } else {
            async?.mypublish(LOGTYPE, LOG_TYPE_SUCCESS, "Clipboard text: $pasteData")
        }

        val messageTosend = MyMessage(
            null,//we dont want any response so
            message.saltToAdd,//i think we can reuse the salt
            Userdata.instance.ipport,
            STR_GETCLIP,
            pasteData,
            null,
            false,
            TYPE_MESSAGE,// change it if we want the tag to be shown in eventghost
            null,
            message.uuidToadd,
            message.taskName,
            RESULT_UNKNOWN
        )
        val address = MyAddress(message.sender, messageTosend.commuType)
        ApplicationInstance.instance.communicator?.sendMessage(messageTosend, address)
        async?.mypublish(
            LOGTYPE,
            LOG_TYPE_SUCCESS,
            "Sent clip data to ${Gson().toJson(address)}"
        )


    }


}