package com.example.isp_icon

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import com.example.isp_icon.data.PertanyaanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DynamicFormActivity : AppCompatActivity() {

    private lateinit var session: InspectionSession
    private lateinit var categoryName: String
    private lateinit var llFormContainer: LinearLayout

    // Untuk menyimpan referensi input user agar bisa diambil nanti (Key: ID Soal, Value: View)
    private val inputViewsMap = mutableMapOf<String, View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_form)

        // 1. Ambil Data dari Intent
        @Suppress("DEPRECATION")
        session = intent.getParcelableExtra("SESSION_DATA") ?: return
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Inspeksi"

        // 2. Setup Header
        findViewById<TextView>(R.id.tvKategoriTitle).text = "Formulir: $categoryName"
        llFormContainer = findViewById(R.id.llFormContainer)

        // 3. Load Soal dari Database & Generate UI
        loadQuestions()

        // 4. Tombol Simpan
        findViewById<Button>(R.id.btnSimpan).setOnClickListener {
            simpanJawaban() // Nanti kita isi logicnya
        }
    }

    private fun loadQuestions() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            // Ambil pertanyaan HANYA untuk kategori yang dipilih
            val questions = db.appDao().getPertanyaanByKategori(categoryName)

            withContext(Dispatchers.Main) {
                if (questions.isEmpty()) {
                    Toast.makeText(this@DynamicFormActivity, "Belum ada soal untuk kategori ini", Toast.LENGTH_SHORT).show()
                } else {
                    generateFormUI(questions)
                }
            }
        }
    }

    // --- MAGIC HAPPENS HERE: MENGUBAH DATA MENJADI TAMPILAN ---
    private fun generateFormUI(questions: List<PertanyaanEntity>) {
        llFormContainer.removeAllViews()
        inputViewsMap.clear()

        for (q in questions) {
            // 1. Buat Label Pertanyaan (TextView)
            val label = TextView(this)
            label.text = q.pertanyaan
            label.textSize = 16f
            label.setTextColor(Color.BLACK)
            label.setTypeface(null, Typeface.BOLD)
            label.setPadding(0, 32, 0, 8) // Jarak atas bawah
            llFormContainer.addView(label)

            // 2. Buat Input Sesuai Tipe
            val inputType = q.tipeInput?.uppercase() ?: "TEKS"
            val inputView: View = when (inputType) {
                "YA_TIDAK" -> createRadioYesNo()
                "ANGKA" -> createEditText(isNumber = true)
                "PILIHAN" -> createSpinner(q.opsiPilihan)
                "FOTO" -> createButtonPhoto()
                else -> createEditText(isNumber = false) // Default TEKS
            }

            // 3. Tambahkan ke Layar & Simpan ke Map
            llFormContainer.addView(inputView)

            // Simpan referensi view dengan kunci ID Pertanyaan (agar nanti bisa diambil isinya)
            inputViewsMap[q.idPertanyaan] = inputView
        }
    }

    // --- HELPER CREATE WIDGETS ---

    private fun createEditText(isNumber: Boolean): EditText {
        val et = EditText(this)
        et.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        et.background = getDrawable(android.R.drawable.edit_text) // Style standar
        if (isNumber) {
            et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        return et
    }

    private fun createRadioYesNo(): RadioGroup {
        val rg = RadioGroup(this)
        rg.orientation = RadioGroup.HORIZONTAL

        val rbYes = RadioButton(this)
        rbYes.text = "Ya"
        rbYes.id = View.generateViewId() // Penting untuk identifikasi

        val rbNo = RadioButton(this)
        rbNo.text = "Tidak"
        rbNo.id = View.generateViewId()

        rg.addView(rbYes)
        rg.addView(rbNo)
        return rg
    }

    private fun createSpinner(optionsString: String?): Spinner {
        val spinner = Spinner(this)
        // Parse opsi "A,B,C" menjadi List ["A", "B", "C"]
        val options = optionsString?.split(",")?.map { it.trim() } ?: listOf("-")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinner.adapter = adapter
        return spinner
    }

    private fun createButtonPhoto(): Button {
        val btn = Button(this)
        btn.text = "Ambil Foto (Kamera)"
        btn.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_camera, 0, 0, 0)
        btn.setOnClickListener {
            Toast.makeText(this, "Fitur Kamera akan segera hadir!", Toast.LENGTH_SHORT).show()
        }
        return btn
    }

    private fun simpanJawaban() {
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        btnSimpan.isEnabled = false
        btnSimpan.text = "Mengumpulkan Data..."

        val mapJawaban = mutableMapOf<String, String>()
        var adaYangKosong = false

        // 1. LOOPING SEMUA INPUT (PANEN JAWABAN)
        for ((idSoal, view) in inputViewsMap) {
            var jawabanUser = ""

            when (view) {
                is EditText -> {
                    jawabanUser = view.text.toString()
                }
                is Spinner -> {
                    jawabanUser = view.selectedItem.toString()
                }
                is RadioGroup -> {
                    val selectedId = view.checkedRadioButtonId
                    if (selectedId != -1) {
                        val radioButton = view.findViewById<RadioButton>(selectedId)
                        jawabanUser = radioButton.text.toString()
                    }
                }
                // Tambahkan case Button (Foto) nanti jika sudah ada fiturnya
            }

            // Validasi sederhana (Optional)
            if (jawabanUser.isEmpty() || jawabanUser == "-") {
                // adaYangKosong = true // Aktifkan jika ingin wajib diisi semua
            }

            mapJawaban[idSoal] = jawabanUser
        }

        // 2. KIRIM KE SERVER
        btnSimpan.text = "Mengirim ke Server..."

        val request = InspectionRequest(
            noWo = session.noWo,
            idSite = session.idSite,
            namaPelaksana = session.pelaksana,
            kategori = categoryName,
            jawaban = mapJawaban
        )

        ApiClient.instance.submitInspection(request = request).enqueue(object : Callback<MaintenanceResponse> {
            override fun onResponse(call: Call<MaintenanceResponse>, response: Response<MaintenanceResponse>) {
                btnSimpan.isEnabled = true
                btnSimpan.text = "SIMPAN JAWABAN"

                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Berhasil Disimpan!", Toast.LENGTH_LONG).show()
                    finish() // Tutup halaman form, kembali ke menu
                } else {
                    Toast.makeText(applicationContext, "Gagal: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MaintenanceResponse>, t: Throwable) {
                btnSimpan.isEnabled = true
                btnSimpan.text = "SIMPAN JAWABAN"
                Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}