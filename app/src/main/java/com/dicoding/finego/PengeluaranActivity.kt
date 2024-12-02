package com.dicoding.finego

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.finego.api.ApiClient
import com.dicoding.finego.databinding.ActivityPengeluaranBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale


class PengeluaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPengeluaranBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPengeluaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etTanggal.setOnClickListener {
            showMaterialDatePicker()
        }

        binding.etKategoriPengeluaran.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                enableSaveButton()
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                enableSaveButton()
            }
        }

        binding.etNominal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak perlu diubah
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                enableSaveButton()
            }

            override fun afterTextChanged(editable: Editable?) {
                // Tidak perlu diubah
            }
        })

// Set listener untuk perubahan teks di etCatatan
        binding.etCatatan.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak perlu diubah
            }

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                enableSaveButton()
            }

            override fun afterTextChanged(editable: Editable?) {
                // Tidak perlu diubah
            }
        })

        binding.btnSimpan.setOnClickListener {
            val date = binding.etTanggal.text.toString()
            val category = binding.etKategoriPengeluaran.selectedItem.toString()
            val amount = binding.etNominal.text.toString().toInt()
            val note = binding.etCatatan.text.toString()


            if (date.isNotEmpty() && category.isNotEmpty()) {
                val transaction = Transaction(
                    type = "expense",
                    date = date,
                    category = mapCategoryToApi(category),
                    amount = amount,
                    note = note
                )

                submitTransaction(transaction)
            } else {
                Toast.makeText(this, "Mohon lengkapi semua data!", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun mapCategoryToApi(category: String): String {
        return when (category) {
            "Transportasi" -> "transportation_expenses"
            "Makan" -> "food_expenses"
            "Tempat Tinggal" -> "housing_cost"
            "Listrik" -> "electricity_bill"
            "Air" -> "water_bill"
            "Hutang" -> "debt"
            "Internet" -> "internet_bill"
            "Lainnya" -> "other"
            else -> "other" // Default jika tidak ditemukan
        }
    }


    private fun enableSaveButton() {
        val date = binding.etTanggal.text.toString()
        val category = binding.etKategoriPengeluaran.selectedItem.toString()
        val amount = binding.etNominal.text.toString()
        val note = binding.etCatatan.text.toString()

        binding.btnSimpan.isEnabled = date.isNotEmpty() && category.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()
    }


    private fun submitTransaction(transaction: Transaction) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val transactionRequest = TransactionRequest(listOf(transaction))

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.instance.addTransaction(userId, transactionRequest)
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@PengeluaranActivity, "Transaksi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@PengeluaranActivity, "Gagal menambahkan transaksi", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PengeluaranActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun showMaterialDatePicker() {
        val calendar = Calendar.getInstance()

        // Batas maksimal (hari ini)
        val maxDate = calendar.timeInMillis

        // Batas minimal (opsional: 100 tahun ke belakang)
        calendar.add(Calendar.YEAR, -100)
        val minDate = calendar.timeInMillis

        // Atur Constraint
        val constraintsBuilder = CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(maxDate)) // Tidak bisa pilih masa depan
            .setStart(minDate) // Mulai dari 100 tahun lalu
            .setEnd(maxDate) // Hingga hari ini

        // Atur builder DatePicker
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Pilih Tanggal Lahir")
            .setSelection(maxDate) // Default tanggal hari ini
            .setCalendarConstraints(constraintsBuilder.build()) // Tambahkan batas
            .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR) // Tampilkan langsung kalender

        val datePicker = builder.build()

        // Handle tanggal yang dipilih
        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedDate = Calendar.getInstance()
            selectedDate.timeInMillis = selection

            // Format tanggal
            val formattedDate = String.format(
                Locale.getDefault(),
                "%02d/%02d/%d",
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedDate.get(Calendar.MONTH) + 1,
                selectedDate.get(Calendar.YEAR)
            )

            // Set tanggal ke EditText
            binding.etTanggal.setText(formattedDate)
        }

        // Tampilkan dialog DatePicker
        if (!datePicker.isAdded) {
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }

}