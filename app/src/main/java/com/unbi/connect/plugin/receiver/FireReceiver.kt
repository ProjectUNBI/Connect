package com.unbi.connect.plugin.receiver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver
import com.unbi.connect.*
import com.unbi.connect.messaging.*
import com.unbi.connect.plugin.task.TaskBundleValues
import com.unbi.connect.TaskerPlugin


class FireReceiver : AbstractPluginSettingReceiver() {

    override fun isBundleValid(bundle: Bundle): Boolean {
        return TaskBundleValues.isBundleValid(bundle)
    }

    override fun isAsync(): Boolean {
        return false///lets perform Async Task// dont set true if you want to return something
    }


    override fun firePluginSetting(
        context: Context,
        bundle: Bundle,
        intent: Intent
    ) {
        if (!Userdata.instance.isReadedfromSpref) {
            Userdata.instance.readfromSpref(context)
            Userdata.instance.isReadedfromSpref = true
        }
        val taskValue = TaskBundleValues.getEditActivityTaskValues(bundle)
        if (taskValue.receiver == null) {
            return
        }
        //todo the actual floW:
        //we should add the action to pendng and sould initialize the task with init message
        /**
         * Constructing the message
         */

        var tasktype = TYPE_INIT
        if (taskValue.isResponse) {
            tasktype = TYPE_RESPOSNE
        } else {
            tasktype = TYPE_MESSAGE
        }
        var resultval = RESULT_UNKNOWN
        if (!taskValue.isSuccess) {
            resultval = RESULT_FAILURE
        } else {
            resultval = RESULT_SUCCESS
        }

        val uidtoadd = MsgUUID(taskValue.ID_MSG)
        uidtoadd.generate()
        val pendingmsg = MyMessage(
            null,
            null,
            Userdata.instance.ipport,
            taskValue.TAG,
            taskValue.MSG,
            Extra(taskValue.extraarray),
            taskValue.isIntent,
            tasktype,
            uidtoadd,
            null,
            taskValue.TASKNAME,
            resultval

        )
        val adress= MyAddress(IpPort.generate(taskValue.receiver) as Any, COMMUTYPE_WIFI)//todo change it if we add bluetooth feature
        ApplicationInstance.instance.communicator?.sendMessage(pendingmsg,adress)

        val vars = Bundle()

        if (isOrderedBroadcast) {
            resultCode = TaskerPlugin.Setting.RESULT_CODE_OK
            if (TaskerPlugin.Setting.hostSupportsVariableReturn(intent.getExtras())) {
                vars.putString("%msgid",uidtoadd.uuid )
                TaskerPlugin.addVariableBundle(getResultExtras(true), vars)
            }
        }
        TaskerPlugin.Setting.signalFinish( context, intent, TaskerPlugin.Setting.RESULT_CODE_OK, vars )
    }


}