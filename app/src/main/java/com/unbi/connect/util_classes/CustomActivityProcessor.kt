package com.unbi.connect.util_classes

import android.content.Context
import android.content.Intent
import com.unbi.connect.messaging.MyMessage
import com.unbi.connect.*
import com.unbi.connect.activity.FindPhoneActivity
import android.R.attr.label
import android.content.ClipData
import android.content.ClipData.newPlainText
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.net.Uri
import android.support.v4.content.ContextCompat.getSystemService
import java.net.URI


class CustomActivityProcessor(val message: MyMessage) {
    companion object {
        val FINDPHONE = 1
        val COPYTEXT = 2
        val SENDCLIP = 3
        val UNKNOWN = -1
    }

    var isTriggered: Boolean = false
    var trigerWhat = UNKNOWN

    init {
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

    fun performIt(context: Context, toaster: Toaster?, logger: Logger?) {
        var msg: String? = null
        when (trigerWhat) {
            FINDPHONE -> {
                msg = STR_FINDPHONE
                dofindphone(context)
            }
            COPYTEXT -> {
                msg = STR_COPYTOCLIP
                //get message
                val msgcontent = message.message
                // copy clipboard data
                copyToClipboard(context, msgcontent)
                //toast copy if toastable


            }
            SENDCLIP -> {
                msg = null// actually we will send message
                performSendClip(context, toaster, logger)
            }

        }
        if (msg == null) {
            return
        }
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
        response.secureAndSend(message.sender, toaster, logger)
    }

    private fun dofindphone(context: Context) {
        // Start activity find phone
        val dialogIntent = Intent(context, FindPhoneActivity::class.java)
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(dialogIntent)

    }


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
    private fun performSendClip(context: Context,toaster:Toaster?, logger:Logger?) {
        /**
         * The folowing grab the text content in the Clipboard content an store
         * in the variable "pasteData
         */
        val clipboard: ClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var pasteData: String = ""
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
        messageTosend.secureAndSend(message.sender, toaster, logger)

    }



}