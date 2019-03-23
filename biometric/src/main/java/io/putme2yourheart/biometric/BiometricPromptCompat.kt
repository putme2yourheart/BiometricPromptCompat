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

class BiometricPromptCompat private constructor(private val impl: IBiometricPromptImpl) {

    fun authenticate(callback: IBiometricAuthenticationCallback) {
        impl.authenticate(CancellationSignal(), callback)
    }

    fun authenticate(cancel: CancellationSignal, callback: IBiometricAuthenticationCallback) {
        impl.authenticate(cancel, callback)
    }

    class Builder(private val context: Context) {
        private var title: CharSequence? = null

        private var subtitle: CharSequence? = null

        private var description: CharSequence? = null

        private var negativeButtonText: CharSequence? = null

        private var negativeButtonListener: DialogInterface.OnClickListener? = null

        private object DefaultListener : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }
        }

        fun setTitle(title: String?): Builder {
            this.title = title
            return this
        }

        fun setTitle(@StringRes resId: Int): Builder {
            this.title = context.getString(resId)
            return this
        }

        fun setSubtitle(subtitle: String?): Builder {
            this.subtitle = subtitle
            return this
        }

        fun setSubtitle(@StringRes resId: Int): Builder {
            this.subtitle = context.getString(resId)
            return this
        }

        fun setDescription(description: String?): Builder {
            this.description = description
            return this
        }

        fun setDescription(@StringRes resId: Int): Builder {
            this.description = context.getString(resId)
            return this
        }

        fun setNegativeButton(
            text: CharSequence?,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.negativeButtonText = text
            this.negativeButtonListener = listener
            return this
        }

        fun setNegativeButton(
            @StringRes resId: Int,
            listener: DialogInterface.OnClickListener
        ): Builder {
            this.negativeButtonText = context.getString(resId)
            this.negativeButtonListener = listener
            return this
        }

        @SuppressLint("NewApi")
        fun build(): BiometricPromptCompat {
            return if (isAboveApi28()) {
                val builder = BiometricPrompt.Builder(context)
                builder.setTitle(title ?: context.getString(R.string.text_biometric_title))
                subtitle?.let {
                    builder.setSubtitle(it)
                }
                description?.let {
                    builder.setDescription(it)
                }
                builder.setNegativeButton(
                    negativeButtonText ?: context.getString(R.string.text_biometric_negative),
                    context.mainExecutor,
                    negativeButtonListener ?: DefaultListener
                )
                BiometricPromptCompat(BiometricPromptApi28Impl(context, builder.build()))
            } else {
                BiometricPromptCompat(
                    BiometricPromptApi23Impl(
                        context, title, subtitle, description,
                        negativeButtonText ?: context.getString(R.string.text_biometric_negative),
                        negativeButtonListener ?: DefaultListener
                    )
                )
            }
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