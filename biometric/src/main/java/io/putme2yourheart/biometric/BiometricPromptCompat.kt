package io.putme2yourheart.biometric

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import java.util.*

typealias SucceededCallback = () -> Unit
typealias FailedCallback = () -> Unit
typealias ErrorCallback = (errorCode: Int, errString: String) -> Unit
typealias HelpCallback = (helpCode: Int, helpString: CharSequence) -> Unit

class BiometricPromptCompat constructor(val windowContext: Context) {

    var title: CharSequence? = null
        internal set
    var subtitle: CharSequence? = null
        internal set
    var description: CharSequence? = null
        internal set

    private var negativeButtonText: CharSequence? = null
    private var negativeButtonListener: DialogInterface.OnClickListener? = null

    private var succeededCallback: SucceededCallback? = null
    private var failedCallback: FailedCallback? = null
    private var errorCallback: ErrorCallback? = null
    private var helpCallback: HelpCallback? = null

    private var cancel: CancellationSignal = CancellationSignal()
    private val callback: IBiometricAuthenticationCallback = object : IBiometricAuthenticationCallback {
        override fun onAuthenticationFailed() {
            failedCallback?.invoke()
        }

        override fun onAuthenticationError(errorCode: Int, errString: String) {
            errorCallback?.invoke(errorCode, errString)
        }

        override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
            helpCallback?.invoke(helpCode, helpString)
        }

        override fun onAuthenticationSucceeded() {
            succeededCallback?.invoke()
        }
    }

    fun title(text: String?): BiometricPromptCompat {
        this.title = text
        return this
    }

    fun title(@StringRes resId: Int): BiometricPromptCompat {
        this.title = windowContext.getString(resId)
        return this
    }

    fun subtitle(text: String?): BiometricPromptCompat {
        this.subtitle = text
        return this
    }

    fun subtitle(@StringRes resId: Int): BiometricPromptCompat {
        this.subtitle = windowContext.getString(resId)
        return this
    }

    fun description(text: String?): BiometricPromptCompat {
        this.description = text
        return this
    }

    fun description(@StringRes resId: Int): BiometricPromptCompat {
        this.description = windowContext.getString(resId)
        return this
    }

    fun negativeButton(
        text: CharSequence?,
        listener: DialogInterface.OnClickListener
    ): BiometricPromptCompat {
        this.negativeButtonText = text
        this.negativeButtonListener = listener
        return this
    }

    fun negativeButton(
        @StringRes resId: Int,
        listener: DialogInterface.OnClickListener
    ): BiometricPromptCompat {
        this.negativeButtonText = windowContext.getString(resId)
        this.negativeButtonListener = listener
        return this
    }

    fun succeededCallback(callback: SucceededCallback): BiometricPromptCompat {
        succeededCallback = callback
        return this
    }

    fun failedCallback(callback: FailedCallback): BiometricPromptCompat {
        failedCallback = callback
        return this
    }

    fun errorCallback(callback: ErrorCallback): BiometricPromptCompat {
        errorCallback = callback
        return this
    }

    fun helpCallback(callback: HelpCallback): BiometricPromptCompat {
        helpCallback = callback
        return this
    }

    fun cancellationSignal(cancel: CancellationSignal): BiometricPromptCompat {
        this.cancel = cancel
        return this
    }

    inline fun authenticate(
        func: BiometricPromptCompat.() -> Unit
    ): BiometricPromptCompat {
        this.func()
        authenticate()
        return this
    }

    fun authenticate() {
        build().authenticate(cancel, callback)
    }

    @SuppressLint("NewApi")
    private fun build(): IBiometricPromptImpl {
        return if (isAboveApi28()) {
            val builder = BiometricPrompt.Builder(windowContext)
            builder.setTitle(title ?: windowContext.getString(R.string.text_biometric_title))
            subtitle?.let {
                builder.setSubtitle(it)
            }
            description?.let {
                builder.setDescription(it)
            }
            builder.setNegativeButton(
                negativeButtonText ?: windowContext.getString(R.string.text_biometric_negative),
                windowContext.mainExecutor,
                negativeButtonListener ?: DefaultListener
            )
            BiometricPromptApi28Impl(windowContext, builder.build())
        } else {
            BiometricPromptApi23Impl(
                windowContext, title, subtitle, description,
                negativeButtonText ?: windowContext.getString(R.string.text_biometric_negative),
                negativeButtonListener ?: DefaultListener
            )
        }
    }

    private object DefaultListener : DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface?, which: Int) {
            dialog?.dismiss()
        }
    }

    interface IBiometricAuthenticationCallback {

        fun onAuthenticationSucceeded()

        fun onAuthenticationFailed()

        fun onAuthenticationError(errorCode: Int, errString: String)

        fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence)
    }

    companion object {
        private const val TAG = "BiometricPromptCompat"

        @RequiresApi(api = Build.VERSION_CODES.M)
        private val SUPPORTED_BIOMETRIC_FEATURES = arrayOf(PackageManager.FEATURE_FINGERPRINT)

        private fun isAboveApi28(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
        }

        private fun isAboveApi23(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        }

        fun hasEnrolledFingerprints(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val manager = context.getSystemService(FingerprintManager::class.java)
                manager != null && manager.hasEnrolledFingerprints()
            } else {
                Log.e(
                    TAG, "Device software version is too low so we return " +
                            "hasEnrolledFingerprints=false instead. Recommend to check software version " +
                            "by yourself before using BiometricPromptCompat."
                )
                false
            }
        }

        fun isHardwareDetected(context: Context): Boolean {
            return when {
                isAboveApi28() -> {
                    val pm = context.packageManager
                    return Arrays.stream(SUPPORTED_BIOMETRIC_FEATURES).anyMatch { pm.hasSystemFeature(it) }
                }
                isAboveApi23() -> {
                    val fm = context.getSystemService(FingerprintManager::class.java)
                    return fm != null && fm.isHardwareDetected
                }
                else -> false
            }
        }
    }

}