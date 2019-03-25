# BiometricPromptCompat

[中文文档](https://github.com/putme2yourheart/BiometricPromptCompat/edit/master/README-cn.md)

> In Android P, Google provides a easier way for developers to use biometric sensors to authenticate user identity: **BiometricPrompt**. However, it is only supported in latest Android. We create a compat library to take it to All Android 6.0+ devices.

> BiometricPromptCompat is designed to be compatible with lower versions of Android (6.0~8.1), and its interface is very close to the original BiometricPrompt to ensure a consistent UI on different Android.

> Support Android X and it's written by Kotlin

## usage:
- Add the library to your module `build.gradle`
```groovy
dependencies {
     implementation 'io.putme2yourheart.biometricprompt:library:1.0.0'
}
```

- If you support landscape, please add 
```xml
  <activity android:configChanges="orientation|keyboardHidden|screenSize">
  </activity>
```

- Hardware detection & Enrolled Fingerprints
```kotlin
BiometricPromptCompat.isHardwareDetected(Context)

BiometricPromptCompat.hasEnrolledFingerprints(Context)
```

- You only need a few lines of code to use BiometricPromptCompat
```kotlin
BiometricPromptCompat.Builder(this)
            .build()
            .authenticate(object : BiometricPromptCompat.IBiometricAuthenticationCallback {
                override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence) {
                    
                }

                override fun onAuthenticationSucceeded() {
                    
                }

                override fun onAuthenticationFailed() {
                   
                }

                override fun onAuthenticationError(errorCode: Int, errString: String) {
                    
                }
            })
```

- Also, you can
```kotlin
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
                    
                }

                override fun onAuthenticationSucceeded() {
                    
                }

                override fun onAuthenticationFailed() {
                   
                }

                override fun onAuthenticationError(errorCode: Int, errString: String) {
                    
                }
            })
```

## Test & Screenshots:
Tested successfully in Android N & P.

- Android N

![alarum](https://github.com/putme2yourheart/BiometricPromptCompat/blob/master/screenshots/24.gif)

- Android P

![alarum](https://github.com/putme2yourheart/BiometricPromptCompat/blob/master/screenshots/28.gif)

License
-------

    Copyright 2019 putme2yourheart

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
