package com.simats.wellnesshome

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var btnReset: androidx.appcompat.widget.AppCompatButton
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etEmail = findViewById(R.id.etEmail)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        btnReset = findViewById(R.id.btnReset)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)

        btnReset.setOnClickListener {
            updatePassword()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun updatePassword() {
        val email = etEmail.text.toString().trim()
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmNewPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }
        if (newPassword.isEmpty()) {
            etNewPassword.error = "New Password is required"
            return
        }
        if (confirmPassword.isEmpty()) {
            etConfirmNewPassword.error = "Confirm Password is required"
            return
        }
        if (newPassword != confirmPassword) {
            etConfirmNewPassword.error = "Passwords do not match"
            return
        }

        // Check if user exists
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPref.getString("EMAIL", null)

        if (email == savedEmail) {
            // Update password
            with(sharedPref.edit()) {
                putString("PASSWORD", newPassword)
                apply()
            }
            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            etEmail.error = "Email not found"
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        }
    }
}
