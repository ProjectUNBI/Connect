package com.unbi.connect.plugin.event

import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioButton
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import com.unbi.connect.R
import com.unbi.connect.bind

class EventEditActivity : AbstractAppCompatPluginActivity(){
    private val radio_message_event: RadioButton by bind(R.id.event_radio_it_is_message)
    private val radio_response_event: RadioButton by bind(R.id.event_radio_it_is_response)
    private val edit_TAG_event: EditText by bind(R.id.event_editplugin_tag)
    private val edit_MSG_event: EditText by bind(R.id.event_editplugin_message)

    val checklistener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        if (!isChecked) {
            return@OnCheckedChangeListener
        }
        when (view.id) {
            R.id.event_radio_it_is_response -> {
                radio_message_event.isChecked = false
            }
            R.id.event_radio_it_is_message -> {
                radio_response_event.isChecked = false
            }
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plugin_edit_event);
        radio_message_event.setOnCheckedChangeListener(checklistener)
        radio_response_event.setOnCheckedChangeListener(checklistener)

    }

    override fun isBundleValid(bundle: Bundle): Boolean {
        return EventBundleValues.isBundleValid(bundle)
    }

    override fun onPostCreateWithPreviousResult(previousBundle: Bundle, previousBlurb: String) {
        val prevalue=EventBundleValues.getEditActivityTaskValues(previousBundle)
        if(prevalue.isResponse){
            radio_response_event.isChecked=true
            radio_message_event.isChecked=false
        }else{
            radio_response_event.isChecked=false
            radio_message_event.isChecked=true
        }
        edit_MSG_event.setText(prevalue.MSG)
        edit_TAG_event.setText(prevalue.TAG)
    }

    override fun getResultBundle(): Bundle? {
        var value:EditActivityEventValues?=null
        if(radio_response_event.isChecked==true){
            value= EditActivityEventValues(
                true,
                null,
                null
            )


        }else{
            value=EditActivityEventValues(
                false,
                edit_TAG_event.text.toString(),
                edit_MSG_event.text.toString()
            )

        }
        return EventBundleValues.generateBundle(applicationContext,value)
    }

    override fun getResultBlurb(bundle: Bundle): String {
        val value=EventBundleValues.getEditActivityTaskValues(bundle)
        val builder = StringBuilder()
        if(value.isResponse){
            builder.append("Type: Response\n")
        }else{
            builder.append("Type: Message\n")
            builder.append("Tag: ${value.TAG}\n")
            builder.append("Message: ${value.MSG}\n")
        }
        return builder.toString()
    }
}