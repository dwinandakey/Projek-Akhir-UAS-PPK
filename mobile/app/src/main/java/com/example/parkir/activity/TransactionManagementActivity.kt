package com.example.parkir.activity

import android.app.DatePickerDialog  // Traditional Android DatePickerDialog
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.example.parkir.R
import com.example.parkir.adapter.TransactionAdapter
import com.example.parkir.databinding.ActivityTransactionManagementBinding
import com.example.parkir.model.TransactionViewModel
import com.example.parkir.model.TransactionViewModelFactory
import com.example.parkir.model.TransaksiParkirResponse
import com.example.parkir.utils.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionManagementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionManagementBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: TransactionAdapter
    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(SessionManager(this))
    }

    private var startDate: LocalDate = LocalDate.now().minusMonths(1)
    private var endDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupRecyclerView()
        setupDatePickers()
        setupObservers()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter(
            onDelete = { transaction -> showDeleteConfirmation(transaction) }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TransactionManagementActivity)
            adapter = this@TransactionManagementActivity.adapter
        }
    }

    private fun setupDatePickers() {
        binding.btnStartDate.text = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        binding.btnEndDate.text = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        binding.btnStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnEndDate.setOnClickListener { showDatePicker(false) }

        binding.btnApplyFilter.setOnClickListener {
            viewModel.setDateRange(startDate, endDate)
        }
    }

    private fun setupObservers() {
        viewModel.transactions.observe(this) { transactions ->
            adapter.submitList(transactions)
        }

        viewModel.error.observe(this) { error ->
            // Show error dialog
            MaterialDialog(this).show {
                title(text = "Error")
                message(text = error)
                positiveButton(text = "OK")
            }
        }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val currentDate = if (isStartDate) startDate else endDate
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                try {
                    val selectedDate = LocalDate.of(year, month + 1, day)
                    if (isStartDate) {
                        if (selectedDate.isAfter(endDate)) {
                            showError("Start date cannot be after end date")
                            return@DatePickerDialog
                        }
                        startDate = selectedDate
                        binding.btnStartDate.text = formatDate(selectedDate)
                    } else {
                        if (selectedDate.isBefore(startDate)) {
                            showError("End date cannot be before start date")
                            return@DatePickerDialog
                        }
                        endDate = selectedDate
                        binding.btnEndDate.text = formatDate(selectedDate)
                    }
                    // Trigger data refresh with new date range
                    viewModel.setDateRange(startDate, endDate)
                } catch (e: Exception) {
                    showError("Invalid date selected")
                }
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        // Set date limits
        if (isStartDate) {
            dialog.datePicker.maxDate = endDate.toEpochDay() * 24 * 60 * 60 * 1000
        } else {
            dialog.datePicker.minDate = startDate.toEpochDay() * 24 * 60 * 60 * 1000
        }

        dialog.show()
    }

    private fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun showDeleteConfirmation(transaction: TransaksiParkirResponse) {
        MaterialDialog(this).show {
            title(text = "Delete Transaction")
            message(text = "Are you sure you want to delete this transaction?")
            positiveButton(text = "Delete") {
                viewModel.deleteTransaction(transaction.id)
            }
            negativeButton(text = "Cancel")
        }
    }

    private fun showError(message: String) {
        MaterialDialog(this).show {
            title(text = "Error")
            message(text = message)
            positiveButton(text = "OK")
        }
    }
}