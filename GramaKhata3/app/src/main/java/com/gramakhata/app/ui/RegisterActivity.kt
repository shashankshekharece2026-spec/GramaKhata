package com.gramakhata.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gramakhata.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val shopName = binding.etShopName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm  = binding.etConfirmPassword.text.toString().trim()

            if (username.isEmpty()) { binding.etUsername.error = "Enter username"; return@setOnClickListener }
            if (shopName.isEmpty()) { binding.etShopName.error = "Enter shop name"; return@setOnClickListener }
            if (password.isEmpty()) { binding.etPassword.error = "Enter password"; return@setOnClickListener }
            if (password.length < 4) { binding.etPassword.error = "Min 4 characters"; return@setOnClickListener }
            if (password != confirm) { binding.etConfirmPassword.error = "Passwords do not match"; return@setOnClickListener }

            val prefs = getSharedPreferences("gramakhata_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("username", username)
                .putString("password", password)
                .putString("shop_name", shopName)
                .putBoolean("is_logged_in", true)
                .apply()

            Toast.makeText(this, "Account created! Welcome, $shopName 🎉", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
        }

        binding.tvGoToLogin.setOnClickListener { finish() }
    }
}
