package io.putme2yourheart.biometric

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

internal class FingerprintDialogLayout : ConstraintLayout {

    private var iconView: FingerprintIconView? = null
    private var statusView: TextView? = null
    var dialog: BiometricPromptCompatDialog? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs,
        defStyleAttr
    )

    override fun onFinishInflate() {
        super.onFinishInflate()
        iconView = findViewById(R.id.iv_fingerprint_icon)
        statusView = findViewById(R.id.tv_status)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        dialog?.onConfigurationChangedCallback()
    }
}