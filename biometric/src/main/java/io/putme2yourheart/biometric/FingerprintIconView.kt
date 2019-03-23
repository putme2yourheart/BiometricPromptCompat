package io.putme2yourheart.biometric

import android.content.Context
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

class FingerprintIconView constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ImageView(context, attrs) {

    private var state = State.OFF

    // Keep in sync with attrs.
    enum class State {
        OFF,
        ON,
        ERROR
    }

    init {
        setState(State.OFF, false)
    }

    fun setState(state: State, animate: Boolean = true) {
        if (state == this.state) return

        @DrawableRes val resId = getDrawable(this.state, state, animate)
        if (resId == 0) {
            setImageDrawable(null)
        } else {
            var icon: Drawable? = null
            if (animate) {
                icon = AnimatedVectorDrawableCompat.create(context, resId)
            }
            if (icon == null) {
                icon = VectorDrawableCompat.create(resources, resId, context.theme)
            }
            setImageDrawable(icon)

            if (icon is Animatable) {
                (icon as Animatable).start()
            }
        }

        this.state = state
    }

    @DrawableRes
    private fun getDrawable(currentState: State, newState: State, animate: Boolean): Int {
        when (newState) {
            FingerprintIconView.State.OFF -> {
                if (animate) {
                    if (currentState == State.ON) {
                        return R.drawable.fingerprint_draw_off_animation
                    } else if (currentState == State.ERROR) {
                        return R.drawable.fingerprint_error_off_animation
                    }
                }
                return 0
            }
            FingerprintIconView.State.ON -> {
                if (animate) {
                    if (currentState == State.OFF) {
                        return R.drawable.fingerprint_draw_on_animation
                    } else if (currentState == State.ERROR) {
                        return R.drawable.fingerprint_error_state_to_fp_animation
                    }
                }
                return R.drawable.fingerprint_fingerprint
            }
            FingerprintIconView.State.ERROR -> {
                if (animate) {
                    if (currentState == State.ON) {
                        return R.drawable.fingerprint_fp_to_error_state_animation
                    } else if (currentState == State.OFF) {
                        return R.drawable.fingerprint_error_on_animation
                    }
                }
                return R.drawable.fingerprint_error
            }
            else -> throw IllegalArgumentException("Unknown state: $newState")
        }
    }
}
