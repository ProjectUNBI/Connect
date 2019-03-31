package com.unbi.connect.plugin.receiver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginConditionReceiver
import com.unbi.connect.*
import com.unbi.connect.messaging.MyMessage
import com.unbi.connect.plugin.event.EditActivityEventValues
import com.unbi.connect.plugin.event.EventBundleValues
import com.unbi.connect.plugin.event.EventEditActivity

class QueryReceiver : AbstractPluginConditionReceiver() {
    override fun isBundleValid(bundle: Bundle): Boolean {
        return EventBundleValues.isBundleValid(bundle)
    }

    override fun isAsync(): Boolean {
        return false
    }

    override fun getPluginConditionResult(context: Context, bundle: Bundle, intent: Intent): Int {

        val result: Int
        val conditionState = EventBundleValues.getEditActivityTaskValues(bundle)
        val pair_result = ApplicationInstance.instance.pendingtaskertask.isvalid(conditionState)
        val messageID = TaskerPlugin.Event.retrievePassThroughMessageID(intent)

        if (messageID == -1) {
            result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNKNOWN
        } else {
            val bool_condition = pair_result.first

            if (bool_condition) {
                if (TaskerPlugin.Condition.hostSupportsVariableReturn(intent.extras)) {
                    val varsBundle = Bundle()
                    val myMessage = pair_result.second
                    putVariabletoBundle(varsBundle, myMessage)
                    TaskerPlugin.addVariableBundle(getResultExtras(true), varsBundle)
                }

                result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_SATISFIED
            } else {
                result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNSATISFIED
            }

        }
        return result

    }

    private fun putVariabletoBundle(varsBundle: Bundle, myMessage: MyMessage?) {
        if (myMessage == null) {
            return
        }
        if (myMessage.mtype == TYPE_INIT) {
            varsBundle.putString("%type", "init")
        }
        if (myMessage.mtype == TYPE_RESPOSNE) {
            varsBundle.putString("%type", "response")
            if (myMessage.resultCode == RESULT_SUCCESS) {
                varsBundle.putString("%result", "success")
            }
            if (myMessage.resultCode == RESULT_FAILURE) {
                varsBundle.putString("%result", "fail")
            }
            if (myMessage.resultCode == RESULT_UNKNOWN) {
                varsBundle.putString("%result", "unknown")
            }
        }
        if (myMessage.mtype == TYPE_MESSAGE) {
            varsBundle.putString("%type", "message")
        }
        varsBundle.putString("%tag", myMessage.tag)
        varsBundle.putString("%message", myMessage.message)
        varsBundle.putString("%taskname", myMessage.taskName)
        varsBundle.putString("%senderip", myMessage.sender.ip)
        varsBundle.putString("%senderport", myMessage.sender.port.toString())
        varsBundle.putString("%msgid", myMessage.uuidToCheck?.uuid)
        varsBundle.putString("%msgidtoadd", myMessage.uuidToadd?.uuid)

        val extra = myMessage.extra?.hash ?: return
        for ((key, value) in extra) {
            if (key.isNotEmpty()) {
                val tempkey = validate(key)
                if (tempkey != null) {
                    if (TaskerPlugin.variableNameValid(tempkey)) {
                        varsBundle.putString(tempkey, value)
                    }
                }
            }
        }


    }

    private fun validate(key: String): String? {
        var temp = key.replace("%", "")
        temp = key.replace(" ", "")
        if (temp.isEmpty()) {
            return null
        }
        if (temp.length < 3) {
            while (temp.length < 3) {
                temp = "_" + temp
            }
        }
        return "%" + temp
    }
}