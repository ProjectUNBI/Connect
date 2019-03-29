package com.unbi.connect.plugin.task

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.twofortyfouram.assertion.BundleAssertions
import com.twofortyfouram.log.Lumberjack
import com.twofortyfouram.spackle.AppBuildInfo
import com.unbi.connect.messaging.MyMessage
import com.google.gson.reflect.TypeToken
import java.lang.Exception


object TaskBundleValues {
    val BUNDLE_EXTRA_INT_VERSION_CODE = "com.unbi.connect.INT_VERSION_CODE" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_RECEIVER = "com.unbi.connect.STRING_RECEIVER" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_TAG = "com.unbi.connect.STRING_TAG" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_MSG = "com.unbi.connect.STRING_MSG" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_GSON_ARRAY_EXTRAS = "com.unbi.connect.STRING_GSON_ARRAY_EXTRAS" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_MSG_UUID = "com.unbi.connect.STRING_MSG_UUID" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_MSG_SALT = "com.unbi.connect.STRING_MSG_SALT" //$NON-NLS-1$
    val BUNDLE_EXTRA_BOOLEAN_RESPONSE = "com.unbi.connect.BOOLEAN_RESPONSE" //$NON-NLS-1$
    val BUNDLE_EXTRA_BOOLEAN_INTENT = "com.unbi.connect.BOOLEAN_INTENT" //$NON-NLS-1$
    val BUNDLE_EXTRA_BOOLEAN_IS_SUCCESS_RESPONSE = "com.unbi.connect.BOOLEAN_IS_SUCCESS_RESPONSE" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_TASK_NAME = "com.unbi.connect.STRING_TASK_NAME" //$NON-NLS-1$

    fun isBundleValid(bundle: Bundle?): Boolean {
        if (null == bundle) {
            return false
        }

        try {
            BundleAssertions.assertHasInt(bundle, BUNDLE_EXTRA_INT_VERSION_CODE)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_RECEIVER, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_TAG, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_MSG, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_GSON_ARRAY_EXTRAS, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_MSG_UUID, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_MSG_SALT, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_TASK_NAME, true, true)
            BundleAssertions.assertHasBoolean(bundle, BUNDLE_EXTRA_BOOLEAN_RESPONSE)
            BundleAssertions.assertHasBoolean(bundle, BUNDLE_EXTRA_BOOLEAN_INTENT)
            BundleAssertions.assertHasBoolean(bundle, BUNDLE_EXTRA_BOOLEAN_IS_SUCCESS_RESPONSE)
            BundleAssertions.assertKeyCount(bundle, 11)//todo keep eye on it
        } catch (e: AssertionError) {
            Lumberjack.e("Bundle failed verification%s", e) //$NON-NLS-1$
            return false
        }

        return true
    }

    fun getEditActivityTaskValues(bundle: Bundle): EditActivityTaskValues {
        return EditActivityTaskValues(
                bundle.getBoolean(BUNDLE_EXTRA_BOOLEAN_RESPONSE, false),
                bundle.getBoolean(BUNDLE_EXTRA_BOOLEAN_IS_SUCCESS_RESPONSE, true),
                bundle.getBoolean(BUNDLE_EXTRA_BOOLEAN_INTENT, false),
                bundle.getString(BUNDLE_EXTRA_STRING_RECEIVER, null),
                bundle.getString(BUNDLE_EXTRA_STRING_TAG, null),
                bundle.getString(BUNDLE_EXTRA_STRING_MSG, null),
                bundle.getString(BUNDLE_EXTRA_STRING_MSG_UUID, null),
                bundle.getString(BUNDLE_EXTRA_STRING_MSG_SALT, null),
                getHashmap(bundle.getString(BUNDLE_EXTRA_STRING_GSON_ARRAY_EXTRAS, null)),
                bundle.getString(BUNDLE_EXTRA_STRING_TASK_NAME, null)
        )

    }


    fun generateBundle(context: Context, values: EditActivityTaskValues): Bundle? {
        val result = Bundle()
        result.putInt(BUNDLE_EXTRA_INT_VERSION_CODE, AppBuildInfo.getVersionCode(context))
        result.putString(BUNDLE_EXTRA_STRING_RECEIVER, values.receiver)
        result.putString(BUNDLE_EXTRA_STRING_TAG, values.TAG)
        result.putString(BUNDLE_EXTRA_STRING_MSG, values.MSG)
        result.putString(BUNDLE_EXTRA_STRING_MSG_UUID, values.ID_MSG)
        result.putString(BUNDLE_EXTRA_STRING_MSG_SALT, values.SALT_MSG)
        result.putString(BUNDLE_EXTRA_STRING_TASK_NAME, values.TASKNAME)
        result.putString(BUNDLE_EXTRA_STRING_GSON_ARRAY_EXTRAS, Gson().toJson(values.extraarray))
        result.putBoolean(BUNDLE_EXTRA_BOOLEAN_RESPONSE, values.isResponse)
        result.putBoolean(BUNDLE_EXTRA_BOOLEAN_INTENT, values.isIntent)
        result.putBoolean(BUNDLE_EXTRA_BOOLEAN_IS_SUCCESS_RESPONSE, values.isSuccess)
        return result
    }

    private fun getHashmap(string: String?): HashMap<String, String> {
        if (string == null) {
            return HashMap<String, String>()
        }
        try {

            val hash = Gson().fromJson<HashMap<String, String>>(string)
            if (hash != null) {
                return hash
            }

        } catch (e: Exception) {
        }
        return HashMap<String, String>()

    }

    inline fun <reified T> Gson.fromJson(json: String) = this.fromJson<T>(json, object : TypeToken<T>() {}.type)


}
