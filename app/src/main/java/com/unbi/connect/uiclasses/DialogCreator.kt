package com.unbi.connect.uiclasses

import android.content.Context
import android.content.DialogInterface
import android.widget.Button
import android.support.v7.app.AlertDialog
import com.unbi.connect.R


class DialogCreator(context: Context, val title_text: String, val dialog_text: String, vararg buttonparamarg: AlerDialogButonParam) {
    var PositiveButton: Button? = null
    var NeutralButton: Button? = null
    var NeagtiveButton: Button? = null
    var alertDialog: AlertDialog

    init {

        alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle(title_text)
        alertDialog.setMessage(dialog_text)
        for (butonparam in buttonparamarg) {
            alertDialog.setButton(butonparam.identifier, butonparam.text, butonparam.clicklistener)
        }
        alertDialog.show()
        PositiveButton=alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        NeutralButton=alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)
        NeagtiveButton=alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
    }

    fun setText(string: String){
        alertDialog.setMessage(string)
    }
}

class AlerDialogButonParam(val identifier: Int, val text: String, val clicklistener: DialogInterface.OnClickListener)