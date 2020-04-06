# UserAuthentication With Firebase (Kotlin)

# Steps
### 1. Create Firebase Project
  use this(https://firebase.google.com/docs/android/setup) linnk to begin with firebase setup. 
  Note: You can create SHA in android studio which is more easy. Refer image
  

### 2. Add Firebase to Project
In your project-level ```build.gradle``` file, make sure to include Google's Maven repository in both your ```buildscript``` and ```allprojects``` sections.

### 3. Add the dependency for the Firebase Authentication Android library to your module (app-level) Gradle file (usually ```app/build.gradle```):  

```implementation 'com.google.firebase:firebase-auth:19.3.0'```

### 4. Enable Phone Number sign-in for your Firebase project
Go to Project --> Authentivation --> Sign-in method and enable Phone provider

## Code
#### Send a verification code to the user's phone   
```
PhoneAuthProvider.getInstance().verifyPhoneNumber(
        phoneNumber, // Phone number to verify
        60, // Timeout duration
        TimeUnit.SECONDS, // Unit of timeout
        this, // Activity (for callback binding)
        callbacks) // OnVerificationStateChangedCallbacks    
```
if you call it multiple times, such as in an activity's ```onStart``` method, the ```verifyPhoneNumber``` method will not send a second SMS unless the original request has timed out.


SMS message sent by Firebase can also be localized, if needed
```
  auth.setLanguageCode("fr")  
```


 callback functions that handle the results of the request
 ```
object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:$credential")

                    verificationComplete()
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // This callback is invoked for invalid request
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        verificationError("Invalid credentials")
                    } else if (e is FirebaseTooManyRequestsException) {
                        verificationError("UnExpected error")
                    }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
                    Log.d(TAG, "onCodeSent:$verificationId")

                    // Save verification ID and resending token so we can use them later
                    storedVerificationId = verificationId
                    resendToken = token


                }
            }) 
```

As we see ```onVerificationCompleted``` already authenticated your OTP if same device mobile number is provided. But If user provide mobile number of another device, then ```onCodeSent``` will receive ```VerificationId``` and ```resendToken``` which will used for verificationand resend procedure.   
```
private fun verifyOTP(enteredOTP: String) {
        var credential = PhoneAuthProvider.getCredential(storedVerificationId!!, enteredOTP)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    verificationComplete()
                    val user = task.result?.user
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        verificationError("Invalid credentials")
                    } else {
                        verificationError("Unexpected error")
                    }
                }
            }
    }
```








