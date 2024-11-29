package com.dicoding.finego

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.dicoding.finego.api.ApiClient
import com.dicoding.finego.databinding.ActivityFormBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import java.util.Calendar
import java.util.Locale

class FormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etTglLahir.setOnClickListener {
            if (binding.etTglLahir.isEnabled) {
                showMaterialDatePicker()
            }
        }

        binding.btnSubmit.setOnClickListener {
            submitUserProfile()
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
            binding.etTglLahir.setText(formattedDate)
        }

        // Tampilkan dialog DatePicker
        if (!datePicker.isAdded) {
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }
    }


    private fun submitUserProfile(){
        val nama = binding.etNama.text.toString()
        val tglLahir = binding.etTglLahir.text.toString()
        val provinsi = binding.etProvinsi.selectedItem.toString()
        val penghasilan = binding.etPenghasilan.text.toString()
        val transportasi = binding.etTransportasi.text.toString()
        val sewa = binding.etSewa.text.toString()
        val listrik = binding.etListrik.text.toString()
        val air = binding.etAir.text.toString()
        val internet = binding.etInternet.text.toString()
        val utang = binding.etUtang.text.toString()
        val makan = binding.etMakan.text.toString()
        val tabungan = binding.etTabungan.text.toString()
        val email = binding.etEmail.text.toString()


        val profile = Profile(
            name = nama,
            email = email,
            birthDate = tglLahir,
            province = provinsi,
            income = penghasilan.toInt(),
            savings = tabungan.toInt()
        )

        val expense = Expense(
            foodExpenses = makan.toInt(),
            transportationExpenses = transportasi.toInt(),
            housingCost = sewa.toInt(),
            electricityBill = listrik.toInt(),
            waterBill = air.toInt(),
            internetCost = internet.toInt(),
            debt = utang.toInt()
        )

        val userInputRequest = UserInputRequest(profile, expense)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        ApiClient.instance.inputUserProfile("$userId", userInputRequest)
            .enqueue(object : retrofit2.Callback<Void> {
                override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        println("Profil berhasil disimpan")
                        Toast.makeText(this@FormActivity, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                        // Arahkan ke MainActivity setelah sukses
                        startActivity(Intent(this@FormActivity, MainActivity::class.java))
                        finish()
                    } else {
                        println("Gagal menyimpan profil: ${response.errorBody()?.string()}")
                        Toast.makeText(
                            this@FormActivity, "Gagal menyimpan profil: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    println("Error: ${t.localizedMessage}")
                }
            })


    }
}