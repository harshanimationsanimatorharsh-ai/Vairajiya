package com.vexo.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.vexo.app.databinding.ActivityPhoneAuthBinding
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneAuthBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSendOtp.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.length < 10) {
                binding.etPhone.error = "Enter valid phone number"
                return@setOnClickListener
            }
            sendOtp("+91$phone")
        }

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()
            if (otp.length != 6) {
                binding.etOtp.error = "Enter 6 digit OTP"
                return@setOnClickListener
            }
            verifyOtp(otp)
        }

        binding.ivBack.setOnClickListener { finish() }
    }

    private fun sendOtp(phoneNumber: String) {
        showLoading(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    showLoading(false)
                    Toast.makeText(
                        this@PhoneAuthActivity,
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                override fun onCodeSent(
                    vId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    showLoading(false)
                    verificationId = vId
                    binding.layoutOtp.visibility = View.VISIBLE
                    binding.btnSendOtp.text = "Resend OTP"
                    Toast.makeText(
                        this@PhoneAuthActivity,
                        "OTP Sent!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(otp: String) {
        showLoading(true)
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                showLoading(false)
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener {
                showLoading(false)
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
