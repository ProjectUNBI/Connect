package com.unbi.connect.plugin.receiver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginConditionReceiver
import com.unbi.connect.ApplicationInstance
import com.unbi.connect.TaskerPlugin
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
        var bool_condition = ApplicationInstance.instance.pendingtaskertask.isvalid(conditionState)

        val messageID = TaskerPlugin.Event.retrievePassThroughMessageID(intent)

        if (messageID == -1) {
            result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNKNOWN
        } else {
            if(bool_condition==null){
                bool_condition=false
            }
            if (bool_condition) {
                result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_SATISFIED
            } else {
                result = com.twofortyfouram.locale.api.Intent.RESULT_CONDITION_UNSATISFIED
            }

        }
        return result

    }
}