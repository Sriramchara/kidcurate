package com.kidcurate.app.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Strong Hardware Biometric Authenticator for Parent Gate.
 * Strictly permits Class 3 strong hardware biometrics (BIOMETRIC_STRONG, e.g. fingerprint)
 * and excludes weak 2D camera face unlock and DEVICE_CREDENTIAL so the phone's lockscreen
 * PIN/pattern/password can never be used to bypass KidCurate's independent Parent Lock.
 */
object ParentBiometricAuth {

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        UNAVAILABLE,
    }

    private const val HARDWARE_BIOMETRIC_AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_STRONG

    fun getStatus(context: Context): BiometricStatus {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(HARDWARE_BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun isHardwareBiometricAvailable(context: Context): Boolean {
        return getStatus(context) == BiometricStatus.AVAILABLE
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Parent Fingerprint Verification",
        subtitle: String = "Confirm your fingerprint. Phone screen lock password is not accepted.",
        negativeButtonText: String = "Use Parent Lock",
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: (() -> Unit)? = null,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                ) {
                    onCancel?.invoke() ?: onError("Biometric verification canceled.")
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // BiometricPrompt handles transient failed attempts with native OS haptic and visual cues
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(HARDWARE_BIOMETRIC_AUTHENTICATORS)
            .build()

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo)
    }
}
