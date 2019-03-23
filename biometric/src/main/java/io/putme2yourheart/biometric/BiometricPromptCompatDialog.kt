package io.putme2yourheart.biometric

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.dialog_biometric_prompt.*


internal class BiometricPromptCompatDialog(context: Context) : Dialog(
    context,
    findThemeResId(context)
) {

    init {
        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.BOTTOM)

        setCanceledOnTouchOutside(true)
        setCancelable(true)

        val rootView = LayoutInflater.from(getContext())
            .inflate(R.layout.dialog_biometric_prompt, null)
        addContentView(
            rootView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fingerprint_dialog_layout.dialog = this
        setBiometricPromptCompatDialogWidth()
    }

    fun onConfigurationChangedCallback() {
        setBiometricPromptCompatDialogWidth()
    }

    private fun setBiometricPromptCompatDialogWidth() {
        val windowParams = window?.attributes
        windowParams?.let {
            it.width = Math.min(
                window?.windowManager?.defaultDisplay?.width ?: 0,
                window?.windowManager?.defaultDisplay?.height ?: 0
            )
            if (it.width == 0) {
                return
            }
            window?.attributes = windowParams
        }
    }

    fun getTitle(): TextView {
        return tv_title
    }

    fun getSubtitle(): TextView {
        return tv_subtitle
    }

    fun getDescription(): TextView {
        return tv_description
    }

    fun getStatus(): TextView {
        return tv_status
    }

    fun getNegativeButton(): TextView {
        return tv_negative
    }

    fun getFingerprintIcon(): FingerprintIconView {
        return iv_fingerprint_icon
    }

    internal fun setState(state: Int, helpString: String = "") {
        when (state) {
            STATE_NORMAL -> {
                tv_status.text = context.getString(R.string.text_biometric_description)
            }
            STATE_FAILED -> {
                if (!TextUtils.isEmpty(helpString)) {
                    tv_status.text = helpString
                }
            }
            STATE_ERROR -> {
                if (!TextUtils.isEmpty(helpString)) {
                    tv_status.text = helpString
                }
            }
            STATE_SUCCEED -> {
                dismiss()
            }
        }
    }

    companion object {
        internal const val STATE_NORMAL = 1
        internal const val STATE_FAILED = 2
        internal const val STATE_ERROR = 3
        internal const val STATE_SUCCEED = 4

        private fun findThemeResId(context: Context): Int {
            val ta = context.obtainStyledAttributes(intArrayOf(R.attr.biometricPromptDialogTheme))
            val resId = ta.getResourceId(0, R.style.Theme_BiometricPromptDialog)
            ta.recycle()
            return resId
        }
    }

}