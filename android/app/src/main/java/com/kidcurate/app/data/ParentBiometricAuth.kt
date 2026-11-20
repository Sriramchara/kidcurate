package com.kidcurate.app.data

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Hardware-backed Biometric and Device Credential Authenticator for Parent Gate.
 * Enforces hardware security (fingerprint, face unlock, or device screen lock PIN/pattern)
 * so that children cannot reset or bypass the Parent PIN.
 */
object ParentBiometricAuth {

    enum class BiometricStatus {
        AVAILABLE,
        NOT_ENROLLED,
        UNAVAILABLE,
    }

    private const val AUTHENTICATORS =
        BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL

    fun getStatus(context: Context): BiometricStatus {
        val manager = BiometricManager.from(context)
        return when (manager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNAVAILABLE
        }
    }

    fun isBiometricOrDeviceLockAvailable(context: Context): Boolean {
        return getStatus(context) == BiometricStatus.AVAILABLE
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Parent Identity Verification",
        subtitle: String = "Confirm your fingerprint, face, or phone screen lock to verify you are the parent.",
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
                    onCancel?.invoke() ?: onError("Verification canceled.")
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
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        val prompt = BiometricPrompt(activity, executor, callback)
        prompt.authenticate(promptInfo)
    }
}
