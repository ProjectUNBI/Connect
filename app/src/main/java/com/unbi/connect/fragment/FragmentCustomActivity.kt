package com.unbi.connect.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.unbi.connect.R
import com.unbi.connect.Userdata
import com.unbi.connect.uiclasses.AlerDialogButonParam
import com.unbi.connect.uiclasses.DialogCreator
import kotlinx.android.synthetic.main.frag_customtask.view.*

class FragmentCustomActivity : BaseFragmentCustomActivity(), View.OnClickListener {


    lateinit var copyText: CardView
    lateinit var SendClipboard: CardView
    lateinit var findPhone: CardView

    lateinit var tv_copytext: TextView
    lateinit var tv_send_clip: TextView
    lateinit var tv_find_phone: TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        copyText = rootView.findViewById(R.id.cardview_copy_text)
        SendClipboard = rootView.findViewById(R.id.cardview_send_clipboard)
        findPhone = rootView.findViewById(R.id.cardview_find_phone)

        copyText.setOnClickListener(this)
        SendClipboard.setOnClickListener(this)
        findPhone.setOnClickListener(this)

        tv_copytext = rootView.findViewById(R.id.tv_trigger_msg_copy_text)
        tv_copytext.setText(Userdata.instance.Trig_copytext)
        tv_send_clip = rootView.findViewById(R.id.tv_trigger_msg_send_clipboard)
        tv_send_clip.setText(Userdata.instance.Trig_sendclip)
        tv_find_phone = rootView.findViewById(R.id.tv_trigger_msg_find_phone)
        tv_find_phone.setText(Userdata.instance.Trig_findphone)

        return rootView
    }

    override fun getLayout(): Int {
        return R.layout.frag_customtask
    }

    override fun onClick(view: View?) {
        val temptcontext = context
        if (temptcontext == null) {
            return
        }

        val okey = AlerDialogButonParam(AlertDialog.BUTTON_POSITIVE, "Ok", this)
        val cancel = AlerDialogButonParam(AlertDialog.BUTTON_NEGATIVE, "Cancel", this)
        val clear = AlerDialogButonParam(AlertDialog.BUTTON_NEUTRAL, "Clear", this)
        showdialog(view,temptcontext,okey,cancel,clear)
    }

    override fun refreshview() {
        tv_copytext.setText(Userdata.instance.Trig_copytext.toString())
        tv_send_clip.setText(Userdata.instance.Trig_sendclip.toString())
        tv_find_phone.setText(Userdata.instance.Trig_findphone.toString())

    }


}