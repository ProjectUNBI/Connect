package com.unbi.connect.util_classes

import android.util.Base64
import com.unbi.connect.Userdata
import java.security.SecureRandom
import java.util.Arrays.copyOfRange
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AES_Util {
    /**
     * @param stringExtra :- this i sthe string whch we incrypt
     * here the 'byte' is the byte array which generated from the password given by the userr
     * @return the encrypted string
     */
    fun encrypt(stringExtra: String?): String? {
        val byte=Userdata.instance.byte_global_password
        if (byte == null||stringExtra==null) {
            return null
        }
        return encrypt(byte, stringExtra)
    }

    /**
     * @param string :- the encrypted string which need to be decryptedd
     * kthe 'byte' is the password in byte array wwhich is stored after generated
     * a password
     * @return the decrypted string
     * it will return null if the decryption fail
     */
    fun decrypt(string: String?): String? {
        val byte=Userdata.instance.byte_global_password
        if (byte == null||string==null) {
            return null
        }
        return decrypt(byte, string)
    }


    private fun encrypt(key: ByteArray, value: String): String? {
        try {
            val randomSecureRandom = SecureRandom()
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            var iv_str = ByteArray(cipher.getBlockSize())
            randomSecureRandom.nextBytes(iv_str)
            iv_str = randomSecureRandom.generateSeed(16)
            val iv = IvParameterSpec(iv_str)
            val skeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
            val encrypted = cipher.doFinal(value.toByteArray())
            val combined = ByteArray(iv_str.size + encrypted.size)

            for (i in combined.indices) {

                combined[i] = if (i < iv_str.size) iv_str[i] else encrypted[i - iv_str.size]
            }
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    private fun decrypt(key: ByteArray, encrypted: String): String? {
        try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            val original = Base64.decode(encrypted, Base64.DEFAULT)
            val ivbyte = copyOfRange(original, 0, 16)
            val msgbytes = copyOfRange(original, 16, original.size)
            val iv = IvParameterSpec(ivbyte)
            val skeySpec = SecretKeySpec(key, "AES")
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val decrypted = cipher.doFinal(msgbytes)
            val s = String(decrypted)
            return String(decrypted)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }



}