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
        BiometricPromptCompat(this).authenticate {
            title("Title")
            subtitle("Subtitle")
            description("Description")
            negativeButton("NEGATIVE",
                DialogInterface.OnClickListener { dialog, which ->
                })
            succeededCallback {
                Toast.makeText(
                    this@MainActivity,
                    "Authentication!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            failedCallback {
                Log.i(TAG, "onAuthenticationFailed")
            }
            errorCallback { errorCode, errString ->
                Log.i(TAG, "onAuthenticationError $errString")
            }
            helpCallback { helpCode, helpString ->
                Log.i(TAG, "onAuthenticationHelp $helpString")
            }
        }
    }
}
