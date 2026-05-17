package com.gramakhata.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramakhata.app.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // If already logged in, skip to main
        val prefs = getSharedPreferences("gramakhata_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            goToMain()
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isEmpty()) { binding.etUsername.error = "Enter username"; return@setOnClickListener }
            if (password.isEmpty()) { binding.etPassword.error = "Enter password"; return@setOnClickListener }

            // Get stored credentials
            val savedUser = prefs.getString("username", null)
            val savedPass = prefs.getString("password", null)

            when {
                savedUser == null -> {
                    Toast.makeText(this, "No account found. Please register first.", Toast.LENGTH_SHORT).show()
                }
                username == savedUser && password == savedPass -> {
                    prefs.edit().putBoolean("is_logged_in", true).apply()
                    goToMain()
                }
                else -> {
                    Toast.makeText(this, "Incorrect username or password.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
