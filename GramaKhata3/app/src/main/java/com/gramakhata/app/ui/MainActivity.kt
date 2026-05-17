package com.gramakhata.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gramakhata.app.R
import com.gramakhata.app.databinding.ActivityMainBinding
import com.gramakhata.app.ui.customers.AddCustomerActivity
import com.gramakhata.app.ui.customers.CustomerAdapter
import com.gramakhata.app.ui.customers.CustomerDetailActivity
import com.gramakhata.app.viewmodel.KhataViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: KhataViewModel by viewModels()
    private lateinit var customerAdapter: CustomerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show shop name in header if available
        val prefs = getSharedPreferences("gramakhata_prefs", Context.MODE_PRIVATE)
        val shopName = prefs.getString("shop_name", "Due Dashboard") ?: "Due Dashboard"
        binding.tvShopName.text = shopName

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout") { _, _ ->
                        val prefs = getSharedPreferences("gramakhata_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("is_logged_in", false).apply()
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        customerAdapter = CustomerAdapter { customer ->
            val intent = Intent(this, CustomerDetailActivity::class.java)
            intent.putExtra("customer_id", customer.id)
            startActivity(intent)
        }
        binding.rvCustomers.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = customerAdapter
        }
    }

    private fun setupObservers() {
        viewModel.displayedCustomers.observe(this) { customers ->
            customerAdapter.submitList(customers)
            binding.tvEmptyState.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
            binding.rvCustomers.visibility = if (customers.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.totalDues.observe(this) { total ->
            binding.tvTotalDue.text = "₹${String.format("%,.2f", total ?: 0.0)}"
        }

        viewModel.activeDebtorCount.observe(this) { count ->
            binding.tvDebtorCount.text = "$count customers"
        }
    }

    private fun setupListeners() {
        binding.fabAddCustomer.setOnClickListener {
            startActivity(Intent(this, AddCustomerActivity::class.java))
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnShareReport.setOnClickListener { shareReport() }
    }

    private fun shareReport() {
        val customers = viewModel.displayedCustomers.value ?: return
        val total = viewModel.totalDues.value ?: 0.0
        val prefs = getSharedPreferences("gramakhata_prefs", Context.MODE_PRIVATE)
        val shopName = prefs.getString("shop_name", "My Shop") ?: "My Shop"
        val sb = StringBuilder()
        sb.appendLine("📒 *$shopName — Daily Report*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        customers.filter { it.netDue > 0 }.forEach { c ->
            sb.appendLine("👤 ${c.name}  →  ₹${String.format("%.2f", c.netDue)}")
        }
        sb.appendLine()
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 *Total Due: ₹${String.format("%.2f", total)}*")
        sb.appendLine("_Sent from Grama-Khata App_")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, sb.toString())
        }
        startActivity(Intent.createChooser(intent, "Share Report via"))
    }
}
