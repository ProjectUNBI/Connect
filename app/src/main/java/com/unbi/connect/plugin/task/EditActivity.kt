package com.unbi.connect.plugin.task

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractAppCompatPluginActivity
import com.twofortyfouram.log.Lumberjack
import com.unbi.connect.R
import com.unbi.connect.bind
import java.util.regex.Pattern


class EditActivity : AbstractAppCompatPluginActivity() {

    private val radio_message: RadioButton by bind(R.id.radio_it_is_message)
    private val radio_response: RadioButton by bind(R.id.radio_it_is_response)
    private val checkbox_intent: CheckBox by bind(R.id.checkbox_is_it_intent)
    private val checkbox_isSuccess: CheckBox by bind(R.id.checkbox_is_Success)
    private val lay_linear_message: LinearLayout by bind(R.id.layout_meaasge)
    private val edit_ip: EditText by bind(R.id.editplugin_ip)
    private val edit_port: EditText by bind(R.id.editplugin_port)
    private val edit_TAG: EditText by bind(R.id.editplugin_tag)
    private val edit_MSG: EditText by bind(R.id.editplugin_message)
    private val lay_linear_response: LinearLayout by bind(R.id.layout_response)
    private val edit_ID_MSG: EditText by bind(R.id.editplugin_message_id)
    private val edit_SALT_MSG: EditText by bind(R.id.editplugin_message_salt)
    private val edit_TASK_NAME: EditText by bind(R.id.editplugin_taskname)
    private val but_plus: Button by bind(R.id.but_plus_sign)
    private val but_minus: Button by bind(R.id.but_minus_sign)
    private val lay_linear_extra: LinearLayout by bind(R.id.layout_extra_view)
    private val extras_views: ArrayList<View> = ArrayList()

    var key_counter=0
    val clicklistener: View.OnClickListener = View.OnClickListener {
        when (it.id) {
            R.id.but_plus_sign -> {
                key_counter++
                val v: View // Creating an instance for View Object
                val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                v = inflater.inflate(R.layout.viewholder_extra, null)
                val keytexview = v.findViewById<EditText>(R.id.edit_extra_key)
                val valuetexview = v.findViewById<EditText>(R.id.edit_extra_value)
                keytexview.setHint("Key$key_counter")
                valuetexview.setHint("Value$key_counter")
                extras_views.add(v)
                lay_linear_extra.addView(v)
            }
            R.id.but_minus_sign -> {
                if (extras_views.size > 0) {
                    key_counter--
                    val view = extras_views[extras_views.lastIndex]


                    extras_views.remove(view)
                    lay_linear_extra.removeView(view)
                }

            }
        }

    }
    val checklistener = CompoundButton.OnCheckedChangeListener { view, isChecked ->
        if (!isChecked) {
            return@OnCheckedChangeListener
        }
        when (view.id) {
            R.id.radio_it_is_response -> {
                lay_linear_message.visibility = GONE
                lay_linear_response.visibility = VISIBLE
                radio_message.isChecked = false
            }
            R.id.radio_it_is_message -> {
                lay_linear_message.visibility = VISIBLE
                lay_linear_response.visibility = GONE
                radio_response.isChecked = false
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plugin_edit_task)
        but_plus.setOnClickListener(clicklistener)
        but_minus.setOnClickListener (clicklistener)
        radio_message.setOnCheckedChangeListener(checklistener)
        radio_response.setOnCheckedChangeListener(checklistener)
        lay_linear_message.visibility = VISIBLE
        lay_linear_response.visibility = GONE
        /*
         * To help the user keep context, the title shows the host's name and the subtitle
         * shows the plug-in's name.
         */
        var callingApplicationLabel: CharSequence? = null
        try {
            callingApplicationLabel = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(callingPackage,
                            0))
        } catch (e: PackageManager.NameNotFoundException) {
            Lumberjack.e("Calling package couldn't be found%s", e) //$NON-NLS-1$
        }

        if (null != callingApplicationLabel) {
            title = callingApplicationLabel
        }

        supportActionBar?.setSubtitle(R.string.plugin_name)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }


    override fun isBundleValid(bundle: Bundle): Boolean {
        return TaskBundleValues.isBundleValid(bundle)
    }

    override fun onPostCreateWithPreviousResult(previousBundle: Bundle, previousBlurb: String) {
        val prevalue = TaskBundleValues.getEditActivityTaskValues(previousBundle)
        if (prevalue.isResponse) {
            radio_response.isChecked = true
            radio_message.isChecked = false//fixme repeatedly change
        } else {
            radio_response.isChecked = false
            radio_message.isChecked = true
        }
        checkbox_intent.isChecked = prevalue.isIntent
        checkbox_isSuccess.isChecked = prevalue.isSuccess
        edit_ip.setText(getip(prevalue.receiver))
        edit_port.setText(getport(prevalue.receiver))
        edit_TAG.setText(prevalue.TAG)
        edit_MSG.setText(prevalue.MSG)
        edit_ID_MSG.setText(prevalue.ID_MSG)
        edit_SALT_MSG.setText(prevalue.SALT_MSG)
        edit_TASK_NAME.setText(prevalue.TASKNAME)

        val hashextra = prevalue.extraarray
        for ((key, value) in hashextra) {
            val v: View // Creating an instance for View Object
            val inflater = baseContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            v = inflater.inflate(R.layout.viewholder_extra, null)
            val keytexview = v.findViewById<EditText>(R.id.edit_extra_key)
            val valuetexview = v.findViewById<EditText>(R.id.edit_extra_value)
            keytexview.setText(key)
            valuetexview.setText(value)
            extras_views.add(v)
            lay_linear_extra.addView(v)
        }
    }


    override fun getResultBundle(): Bundle? {
        var result: Bundle? = null

        if (!TextUtils.isEmpty(edit_ip.text.toString()) &&
                !TextUtils.isEmpty(edit_port.text.toString())
        ) {
            val values = EditActivityTaskValues(
                    radio_response.isChecked,
                    checkbox_isSuccess.isChecked,
                    checkbox_intent.isChecked,
                    getipadress(),
                    edit_TAG.text.toString(),
                    edit_MSG.text.toString(),
                    edit_ID_MSG.text.toString(),
                    edit_SALT_MSG.text.toString(),
                    getextras(),
                    edit_TASK_NAME.text.toString()

            )
            result = TaskBundleValues.generateBundle(applicationContext, values)
        }

        return result


    }

    /*
    It is the message that show in the tasker task of this plugin
     */
    override fun getResultBlurb(bundle: Bundle): String {
        val data = TaskBundleValues.getEditActivityTaskValues(bundle)
        val builder = StringBuilder()
        builder.append("Receiver: ${data.receiver}\n")
        if (data.isResponse) {
            builder.append("Type: Response\n")
            builder.append("Intent: ${data.isIntent}\n")
            builder.append("Success: ${data.isSuccess}\n")
            builder.append("Id: ${data.ID_MSG}\n")
            builder.append("Salt: ${data.SALT_MSG}\n")
        } else {
            builder.append("Type: Message\n")
            builder.append("Tag: ${data.TAG}\n")
            builder.append("Message: ${data.MSG}\n")
        }
        var i=0
        for((key,value) in data.extraarray){
            i++
            builder.append("Key${i}: $key\n")
            builder.append("Value${i}: $value\n")
        }
        return builder.toString()
    }

    ///private function
    private fun getextras(): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        for (view in extras_views) {
            val keytext = view.findViewById<EditText>(R.id.edit_extra_key)
            val valueText = view.findViewById<EditText>(R.id.edit_extra_value)
            if (!TextUtils.isEmpty(keytext.text.toString())) {
                hashMap.put(
                        keytext.text.toString(),
                        valueText.text.toString()
                )
            }
        }
        return hashMap
    }

    private fun getipadress(): String? {
        val ipport = edit_ip.text.toString() + ":" + edit_port.text.toString()
        val p = Pattern.compile("^("
//                + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
//                + "|"
                + "localhost" // localhost

                + "|"
                + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip

                + ":"
                + "[0-9]{1,5}$")
        if (p.matcher(ipport).matches()) {
            return ipport
        }
        return null
    }


    private fun getport(receiver: String?): String {
        if (receiver != null) {
            return receiver.split(":")[1]
        }
        return "";
    }

    private fun getip(receiver: String?): String {
        if (receiver != null) {
            return receiver.split(":")[0]
        }
        return "";
    }
}


class EditActivityTaskValues(
        val isResponse: Boolean = false,
        val isSuccess: Boolean = true,
        val isIntent: Boolean = false,
        val receiver: String? = null,
        val TAG: String? = null,
        val MSG: String? = null,
        val ID_MSG: String? = null,
        val SALT_MSG: String? = null,
        val extraarray: HashMap<String, String>,
        val TASKNAME:String?
)
