package com.unbi.connect.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.unbi.connect.ApplicationInstance
import com.unbi.connect.NULL_WORD
import com.unbi.connect.R
import com.unbi.connect.Userdata
import com.unbi.connect.uiclasses.AlerDialogButonParam
import com.unbi.connect.uiclasses.DialogCreator

abstract class BaseFragmentCustomActivity : BaseFragMent(), DialogInterface.OnClickListener, caturedMessag {

    var dialogCreator: DialogCreator? = null

    protected fun showdialog(
            v: View?,
            temptcontext: Context,
            okey: AlerDialogButonParam,
            cancel: AlerDialogButonParam,
            clear: AlerDialogButonParam
    ) {
        ApplicationInstance.instance.isCapturingMode = true
        when (v?.id) {
            R.id.cardview_copy_text -> {
                last_click = R.id.cardview_copy_text
                dialogCreator = DialogCreator(
                        temptcontext,
                        Userdata.instance.ipport.toString(),
                        "Waiting for message...",
                        okey,
                        clear,
                        cancel
                )
            }
            R.id.cardview_send_clipboard -> {
                last_click = R.id.cardview_send_clipboard
                dialogCreator = DialogCreator(
                        temptcontext,
                        Userdata.instance.ipport.toString(),
                        "Waiting for message...",
                        okey,
                        clear,
                        cancel
                )
            }
            R.id.cardview_find_phone -> {
                last_click = R.id.cardview_find_phone
                dialogCreator = DialogCreator(
                        temptcontext,
                        Userdata.instance.ipport.toString(),
                        "Waiting for message...",
                        okey,
                        clear,
                        cancel
                )
            }
        }
        dialogCreator?.PositiveButton?.isEnabled = false

    }

    var last_click = 0;//it is to determine which Cardview is clicked
    var temporary_message: String = NULL_WORD
    override fun onClick(dialog: DialogInterface?, which: Int) {
        Log.d("DIALOG", which.toString())
        when (which) {

            AlertDialog.BUTTON_NEUTRAL -> {
                Userdata.instance.save(context, last_click, NULL_WORD)//saving the appl

            }
            AlertDialog.BUTTON_POSITIVE -> {
                if (temporary_message == NULL_WORD) {
                    return
                }
                if ((temporary_message.equals(Userdata.instance.Trig_copytext)
                                && last_click != R.id.cardview_copy_text) ||
                        (temporary_message.equals(Userdata.instance.Trig_findphone)
                                && last_click != R.id.cardview_find_phone) ||
                        (temporary_message.equals(Userdata.instance.Trig_sendclip)
                                && last_click != R.id.cardview_send_clipboard)
                ) {
                    Toast.makeText(context, "Already assigned to another one...", LENGTH_SHORT).show()
                } else {
                    Userdata.instance.save(context, last_click, temporary_message)//saving the appl
                }
                temporary_message = NULL_WORD//reset the temporary message
            }
            AlertDialog.BUTTON_NEGATIVE -> {/*do noptjing*/
            }
        }
        last_click = 0//reset the last click

        if (!ApplicationInstance.instance.isCapturingMode) {
            return
        }
        ApplicationInstance.instance.isCapturingMode = false
        refreshview()
    }

    override fun captured(string: String) {
        if (!ApplicationInstance.instance.isCapturingMode) {
            return
        }
        temporary_message = string
        dialogCreator?.setText(string)
        dialogCreator?.PositiveButton?.isEnabled = true

    }

    abstract fun refreshview()//refresh the view
}

interface caturedMessag {
    fun captured(string: String)
}