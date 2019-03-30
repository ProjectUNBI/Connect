package com.unbi.connect.plugin.event

import android.content.Context
import android.os.Bundle
import com.google.gson.Gson
import com.twofortyfouram.assertion.BundleAssertions
import com.twofortyfouram.log.Lumberjack
import com.twofortyfouram.spackle.AppBuildInfo
import com.unbi.connect.messaging.MyMessage
import com.google.gson.reflect.TypeToken
import java.lang.Exception


object EventBundleValues {
    val BUNDLE_EXTRA_INT_VERSION_CODE = "com.unbi.connect.INT_VERSION_CODE" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_TAG = "com.unbi.connect.STRING_TAG" //$NON-NLS-1$
    val BUNDLE_EXTRA_STRING_MSG = "com.unbi.connect.STRING_MSG" //$NON-NLS-1$
    val BUNDLE_EXTRA_BOOLEAN_RESPONSE = "com.unbi.connect.BOOLEAN_RESPONSE" //$NON-NLS-1$

    fun isBundleValid(bundle: Bundle?): Boolean {
        if (null == bundle) {
            return false
        }

        try {
            BundleAssertions.assertHasInt(bundle, BUNDLE_EXTRA_INT_VERSION_CODE)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_TAG, true, true)
            BundleAssertions.assertHasString(bundle, BUNDLE_EXTRA_STRING_MSG, true, true)
            BundleAssertions.assertHasBoolean(bundle, BUNDLE_EXTRA_BOOLEAN_RESPONSE)
            BundleAssertions.assertKeyCount(bundle, 4)//todo keep eye on it
        } catch (e: AssertionError) {
            Lumberjack.e("Bundle failed verification%s", e) //$NON-NLS-1$
            return false
        }

        return true
    }

    fun getEditActivityTaskValues(bundle: Bundle): EditActivityEventValues {
        return EditActivityEventValues(
            bundle.getBoolean(BUNDLE_EXTRA_BOOLEAN_RESPONSE, false),
            bundle.getString(BUNDLE_EXTRA_STRING_TAG, null),
            bundle.getString(BUNDLE_EXTRA_STRING_MSG, null)
        )


    }


    fun generateBundle(context: Context, values: EditActivityEventValues): Bundle? {
        val result = Bundle()
        result.putInt(BUNDLE_EXTRA_INT_VERSION_CODE, AppBuildInfo.getVersionCode(context))
        result.putString(BUNDLE_EXTRA_STRING_TAG, values.TAG)
        result.putString(BUNDLE_EXTRA_STRING_MSG, values.MSG)
        result.putBoolean(BUNDLE_EXTRA_BOOLEAN_RESPONSE, values.isResponse)
        return result
    }



}

class EditActivityEventValues(val isResponse: Boolean, val TAG: String?, val MSG: String?) {

}
