package io.putme2yourheart.biometric

import android.content.Context
import android.content.DialogInterface
import android.hardware.fingerprint.FingerprintManager
import android.os.*
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
internal class BiometricPromptApi23Impl private constructor() : IBiometricPromptImpl {

    private lateinit var context: Context
    private var fingerprintManager: FingerprintManager? = null
    private var cancellationSignal: CancellationSignal? = null
    private var authenticationCallback: BiometricPromptCompat.IBiometricAuthenticationCallback? = null
    private lateinit var biometricPromptDialog: BiometricPromptCompatDialog
    private val fingerprintManageCallback = FingerprintManageCallbackImpl()
    private lateinit var animateHandler: Handler

    constructor(
        context: Context,
        title: CharSequence?,
        subtitle: CharSequence?,
        description: CharSequence?,
        negativeButtonText: CharSequence?,
        negativeButtonListener: DialogInterface.OnClickListener
    ) : this() {
        this.context = context
        this.fingerprintManager = getFingerprintManager(context)
        this.animateHandler = AnimateHandler(context.mainLooper)

        biometricPromptDialog = BiometricPromptCompatDialog(context)
        biometricPromptDialog.setTitle("Title")
        biometricPromptDialog.getTitle().text = title ?: context.getString(
            R.string.text_biometric_title
        )
        subtitle?.let {
            biometricPromptDialog.getSubtitle().visibility = View.VISIBLE
            biometricPromptDialog.getSubtitle().text = it
        }
        description?.let {
            biometricPromptDialog.getDescription().visibility = View.VISIBLE
            biometricPromptDialog.getDescription().text = it
        }
        negativeButtonText?.let {
            biometricPromptDialog.getNegativeButton().text = negativeButtonText
            biometricPromptDialog.getNegativeButton().setOnClickListener {
                biometricPromptDialog.dismiss()
                negativeButtonListener.onClick(biometricPromptDialog, DialogInterface.BUTTON_NEGATIVE)
            }
        }
    }

    private fun getFingerprintManager(context: Context): FingerprintManager? {
        if (fingerprintManager == null) {
            fingerprintManager = context.getSystemService(FingerprintManager::class.java)
        }
        return fingerprintManager
    }

    override fun authenticate(
        cancel: CancellationSignal,
        callback: BiometricPromptCompat.IBiometricAuthenticationCallback
    ) {
        authenticationCallback = callback
        cancellationSignal = cancel
        if (cancellationSignal == null) {
            cancellationSignal = CancellationSignal()
        }
        cancellationSignal?.setOnCancelListener {
            biometricPromptDialog.dismiss()
        }

        biometricPromptDialog.setOnCancelListener {
            cancellationSignal?.let {
                if (!it.isCanceled) {
                    it.cancel()
                }
            }
        }

        biometricPromptDialog.setOnDismissListener {
            cancellationSignal?.let {
                if (!it.isCanceled) {
                    it.cancel()
                }
            }
        }

        biometricPromptDialog.setOnShowListener {
            biometricPromptDialog.getFingerprintIcon().setState(
                FingerprintIconView.State.ON, false
            )

            try {
                val cryptoObjectHelper = CryptoObjectHelper()
                fingerprintManager?.authenticate(
                    cryptoObjectHelper.buildCryptoObject(), cancellationSignal,
                    0, fingerprintManageCallback, null
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        biometricPromptDialog.show()
    }

    private inner class AnimateHandler internal constructor(looper: Looper) : Handler(
        looper
    ) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_RESTORE_NORMAL_STATE -> {
                    biometricPromptDialog.getFingerprintIcon().setState(FingerprintIconView.State.ON)
                    biometricPromptDialog.setState(BiometricPromptCompatDialog.STATE_NORMAL)
                }
                WHAT_DISMISS_AUTHENTICATION_DIALOG -> {
                    biometricPromptDialog.dismiss()
                }
            }
        }
    }

    private inner class FingerprintManageCallbackImpl : FingerprintManager.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            Log.d(
                TAG,
                "onAuthenticationError() called with: errorCode = [$errorCode], errString = [$errString]"
            )

            biometricPromptDialog.setState(BiometricPromptCompatDialog.STATE_ERROR, errString.toString())
            biometricPromptDialog.getFingerprintIcon().setState(FingerprintIconView.State.ERROR)
            animateHandler.removeMessages(WHAT_RESTORE_NORMAL_STATE)
            animateHandler.sendEmptyMessageDelayed(WHAT_DISMISS_AUTHENTICATION_DIALOG, 2000)
            authenticationCallback?.onAuthenticationError(errorCode, errString.toString())
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            Log.d(TAG, "onAuthenticationFailed() called")

            biometricPromptDialog.setState(
                BiometricPromptCompatDialog.STATE_FAILED,
                context.getString(R.string.text_biometric_not_recognized_fingerprint)
            )
            biometricPromptDialog.getFingerprintIcon().setState(FingerprintIconView.State.ERROR)
            animateHandler.removeMessages(WHAT_RESTORE_NORMAL_STATE)
            animateHandler.sendEmptyMessageDelayed(WHAT_RESTORE_NORMAL_STATE, 2000)
            authenticationCallback?.onAuthenticationFailed()
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            super.onAuthenticationHelp(helpCode, helpString)
            Log.d(
                TAG,
                "onAuthenticationHelp() called with: helpCode = [$helpCode], helpString = [$helpString]"
            )

            biometricPromptDialog.setState(BiometricPromptCompatDialog.STATE_FAILED, helpString.toString())
            biometricPromptDialog.getFingerprintIcon().setState(FingerprintIconView.State.ON)
            authenticationCallback?.onAuthenticationHelp(helpCode, helpString)
        }

        override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            Log.i(TAG, "onAuthenticationSucceeded: ")

            biometricPromptDialog.setState(BiometricPromptCompatDialog.STATE_SUCCEED)
            authenticationCallback?.onAuthenticationSucceeded()
        }
    }

    companion object {
        internal const val TAG: String = "BiometricPromptImpl23"

        internal const val WHAT_RESTORE_NORMAL_STATE = 0
        internal const val WHAT_DISMISS_AUTHENTICATION_DIALOG = 1
    }

}