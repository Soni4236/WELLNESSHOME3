package com.simats.wellnesshome

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.simats.wellnesshome.api.ApiClient
import com.simats.wellnesshome.api.AuthResponse
import com.simats.wellnesshome.api.LoginRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: androidx.appcompat.widget.AppCompatButton
    private lateinit var tvForgotPassword: TextView
    private lateinit var tvSignUpLink: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
        tvSignUpLink = findViewById(R.id.tvSignUpLink)

        btnLogin.setOnClickListener {
            if (validateInput()) {
                loginUserAPI()
            }
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }
    }

    private fun validateInput(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }
        return true
    }

    private fun loginUserAPI() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        val request = LoginRequest(email, password)

        ApiClient.instance.login(request)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    val authResponse = response.body()
                    if (response.isSuccessful && authResponse != null && authResponse.success) {
                        Toast.makeText(this@LoginActivity, "Login Success!", Toast.LENGTH_SHORT).show()
                        
                        // Save user session (token and userId)
                        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("AUTH_TOKEN", authResponse.token)
                            putString("USER_ID", authResponse.userId)
                            apply()
                        }
                        
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        val errorMessage = authResponse?.message ?: response.message()
                        Toast.makeText(this@LoginActivity, "Login Failed: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "API Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
