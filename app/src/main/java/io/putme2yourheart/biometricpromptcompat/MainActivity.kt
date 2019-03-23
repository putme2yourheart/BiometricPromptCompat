package io.putme2yourheart.biometricpromptcompat

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.putme2yourheart.biometric.BiometricPromptCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_authenticate.setOnClickListener {
            authenticate()
        }
    }

    private fun authenticate() {
        if (!BiometricPromptCompat.isHardwareDetected(this)) {
            Toast.makeText(
                this, "No hardware found for fingerprints",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (!BiometricPromptCompat.hasEnrolledFingerprints(this)) {
            Toast.makeText(
                this, "no fingerprints",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        BiometricPromptCompat.Builder(this)
            .setTitle("Title")
            .setSubtitle("Subtitle")
            .setDescription("Description")
            .setNegativeButton("NEGATIVE",
                DialogInterface.OnClickListener { dialog, which ->
                })
            .build()
            .authenticate(object : BiometricPromptCompat.IBiometricAuthenticationCallback {
                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    Log.i(TAG, "onAuthenticationHelp $helpString")
                }

                override fun onAuthenticationSucceeded() {
                    Toast.makeText(this@MainActivity, "Authentication!", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    Log.i(TAG, "onAuthenticationFailed")
                }

                override fun onAuthenticationError(errorCode: Int, errString: String) {
                    Log.i(TAG, "onAuthenticationError $errString")
                }
            })
    }
}
