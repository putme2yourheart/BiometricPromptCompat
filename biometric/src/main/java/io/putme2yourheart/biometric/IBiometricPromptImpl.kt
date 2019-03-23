package io.putme2yourheart.biometric


import android.os.CancellationSignal
import androidx.annotation.NonNull

internal interface IBiometricPromptImpl {

    fun authenticate(
        @NonNull cancel: CancellationSignal,
        @NonNull callback: BiometricPromptCompat.IBiometricAuthenticationCallback
    )

}