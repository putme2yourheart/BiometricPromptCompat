# BiometricPromptCompat

[English document](https://github.com/putme2yourheart/BiometricPromptCompat/blob/master/README.md)

> 在Android P中，Google为开发人员提供了一种更简单的方法来使用生物识别传感器来验证用户身份：BiometricPrompt。
可惜的是，它仅在最新的Android P中受支持。 我们创建了一个兼容库，可以将它带到所有Android 6.0+设备。

> BiometricPromptCompat旨在与较低版本的Android（6.0~8.1）兼容，其界面非常接近原始BiometricPrompt，以确保在不同Android上的一致UI。

> 支持Android X，由Kotlin编写

## usage:
- 将库添加到模块`build.gradle`
```groovy
dependencies {
     implementation 'io.putme2yourheart.biometricprompt:library:1.0.0'
}
```

- 如果需要支持横屏，请添加
```xml
  <activity android:configChanges="orientation|keyboardHidden|screenSize">
  </activity>
```

- 指纹识别硬件检测与指纹录入检测
```kotlin
BiometricPromptCompat.isHardwareDetected(Context)

BiometricPromptCompat.hasEnrolledFingerprints(Context)
```

- 你可以很简单地使用BiometricPromptCompat
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

- 或者
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

## 测试与示例
在Android N＆P中成功测试

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
