package com.unbi.connect.plugin.receiver

import android.content.Context
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Bundle
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver
import com.unbi.connect.*
import com.unbi.connect.messaging.*
import com.unbi.connect.plugin.task.TaskBundleValues

class FireReceiver : AbstractPluginSettingReceiver() {

    override fun isBundleValid(bundle: Bundle): Boolean {
        return TaskBundleValues.isBundleValid(bundle)
    }

    override fun isAsync(): Boolean {
        return true///lets perform Async Task
//        return false///lets perform Async Task
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun firePluginSetting(context: Context, bundle: Bundle) {
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
        var tasktype = TYPE_INIT
        if (taskValue.isResponse) {
            tasktype = TYPE_RESPOSNE
        } else {
            tasktype = TYPE_MESSAGE
        }
        var resultval = RESULT_UNKNOWN
        if (taskValue.isSuccess) {
            resultval = RESULT_SUCCESS
        } else {
            resultval = RESULT_FAILURE
        }

        val uidtoadd = MsgUUID()
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
        PendingMessage(pendingmsg,System.currentTimeMillis()).addToPendings(ApplicationInstance.instance.PendingMessageDataArray)
        val salt_toadd = Salt(milli = System.currentTimeMillis()).generate(ApplicationInstance.instance.SaltDataArray)


        val message = MyMessage(
                salt_toadd,
                null,
                Userdata.instance.ipport,
                null,
                null,
                null,
                false,
                TYPE_INIT,//message is init mtype
                uidtoadd,
                null,
                null
        )
        message.sendAsync(IpPort.generate(taskValue.receiver), null, null)
//        message.sendAsync(IpPort.generate(taskValue.receiver),null, null)

    }


}