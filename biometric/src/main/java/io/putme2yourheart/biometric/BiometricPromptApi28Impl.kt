package io.putme2yourheart.biometric

import android.content.Context
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.*
import java.security.spec.ECGenParameterSpec

@RequiresApi(Build.VERSION_CODES.P)
internal class BiometricPromptApi28Impl constructor(
    private val context: Context,
    private val biometricPrompt: BiometricPrompt
) : IBiometricPromptImpl {

    private var authenticationCallback: BiometricPromptCompat.IBiometricAuthenticationCallback? = null
    private lateinit var cancellationSignal: CancellationSignal
    private var signature: Signature

    init {
        try {
            generateKeyPair(TAG, true)
            signature = initSignature(TAG)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Generate NIST P-256 EC Key pair for signing and verification
     * @param keyName
     * @param invalidatedByBiometricEnrollment
     * @return KeyPair
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun generateKeyPair(keyName: String, invalidatedByBiometricEnrollment: Boolean): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val builder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_SIGN
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            // Require the user to authenticate with a biometric to authorize every use of the key
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)

        keyPairGenerator.initialize(builder.build())

        return keyPairGenerator.generateKeyPair()
    }

    @Throws(Exception::class)
    private fun getKeyPair(keyName: String): KeyPair {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        // Get public key
        val publicKey = keyStore.getCertificate(keyName).publicKey
        // Get private key
        val privateKey = keyStore.getKey(keyName, null) as PrivateKey
        // Return a key pair
        return KeyPair(publicKey, privateKey)
    }

    @Throws(Exception::class)
    private fun initSignature(keyName: String): Signature {
        val keyPair = getKeyPair(keyName)

        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(keyPair.private)
        return signature
    }

    override fun authenticate(
        cancel: CancellationSignal,
        callback: BiometricPromptCompat.IBiometricAuthenticationCallback
    ) {
        authenticationCallback = callback
        cancellationSignal = cancel

        biometricPrompt.authenticate(
            BiometricPrompt.CryptoObject(signature),
            cancellationSignal, context.mainExecutor, BiometricPromptCallbackImpl()
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private inner class BiometricPromptCallbackImpl : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            cancellationSignal.cancel()
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            super.onAuthenticationHelp(helpCode, helpString)
            authenticationCallback?.onAuthenticationHelp(helpCode, helpString)
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            authenticationCallback?.onAuthenticationSucceeded()
            cancellationSignal.cancel()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            authenticationCallback?.onAuthenticationFailed()
        }
    }

    companion object {
        internal const val TAG = "BiometricPromptImpl28"
    }

}