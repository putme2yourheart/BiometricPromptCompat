package io.putme2yourheart.biometric

import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.Key
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@RequiresApi(Build.VERSION_CODES.M)
internal class CryptoObjectHelper @Throws(Exception::class)
constructor() {

    private val keystore: KeyStore

    init {
        keystore = KeyStore.getInstance(KEYSTORE_NAME)
        keystore.load(null)
    }

    @Throws(Exception::class)
    fun buildCryptoObject(): FingerprintManager.CryptoObject {
        val cipher = createCipher(true)
        return FingerprintManager.CryptoObject(cipher)
    }

    @Throws(Exception::class)
    private fun createCipher(retry: Boolean): Cipher {
        val key = getKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        try {
            cipher.init(Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE, key)
        } catch (e: KeyPermanentlyInvalidatedException) {
            keystore.deleteEntry(KEY_NAME)
            if (retry) {
                createCipher(false)
            } else {
                throw Exception("Could not create the cipher for fingerprint authentication.", e)
            }
        }
        return cipher
    }

    @Throws(Exception::class)
    private fun getKey(): Key {
        if (!keystore.isKeyEntry(KEY_NAME)) {
            createKey()
        }
        return keystore.getKey(KEY_NAME, null)
    }

    @Throws(Exception::class)
    private fun createKey() {
        val keyGen = KeyGenerator.getInstance(KEY_ALGORITHM, KEYSTORE_NAME)
        val keyGenSpec = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(ENCRYPTION_PADDING)
            .setUserAuthenticationRequired(true)
            .build()
        keyGen.init(keyGenSpec)
        keyGen.generateKey()
    }

    companion object {
        // This can be key name you want. Should be unique for the app.
        private const val KEY_NAME = "io.putme2yourheart.biometric.CryptoObjectHelper"

        // We always use this keystore on Android.
        private const val KEYSTORE_NAME = "AndroidKeyStore"

        // Should be no need to change these values.
        private const val KEY_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION = "$KEY_ALGORITHM/$BLOCK_MODE/$ENCRYPTION_PADDING"
    }
}