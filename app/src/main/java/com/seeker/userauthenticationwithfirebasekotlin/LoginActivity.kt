package com.seeker.userauthenticationwithfirebasekotlin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var storedVerificationId: String
    lateinit var auth: FirebaseAuth
    var TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        verify.setOnClickListener {
            sendOtp(mobile.text.toString())
        }

        vlidateOtp.setOnClickListener {
            verifyOTP(otp.text.toString())
        }
    }

    private fun sendOtp(phoneNumber: String) {
        rel_Bottom.visibility = View.VISIBLE
        val phoneAuthProvider = PhoneAuthProvider.getInstance()

        phoneAuthProvider.verifyPhoneNumber(
            "+91" + phoneNumber, // Phone number to verify
            60L, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout duartion
            this, // Activity (for callback binding)
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Invoked in two situations:
                    // 1 - Instant verification. In some cases the phone number can be instantly
                    //     verified without needing to send or enter a verification code.
                    // 2 - Auto-retrieval. On some devices Google Play services can automatically
                    //     detect the incoming verification SMS and perform verification without
                    //     user action.
                    Log.d(TAG, "onVerificationCompleted:$credential")

                    rel_Bottom.visibility = View.VISIBLE
                    otp.setText(credential.smsCode)
                    Handler().postDelayed(Runnable {
                        verificationComplete()

                    }, 2000)

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
            }) // OnVerificationStateChangedCallbacks
    }


    private fun verifyOTP(enteredOTP: String) {
        var credential = PhoneAuthProvider.getCredential(storedVerificationId!!, enteredOTP)

        auth=FirebaseAuth.getInstance()
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

    private fun verificationComplete() {
        Toast.makeText(this, "Login Successfully.", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun verificationError(error: String) {
        Toast.makeText(this, "Verification failed due to " + error, Toast.LENGTH_SHORT).show()
    }

}
