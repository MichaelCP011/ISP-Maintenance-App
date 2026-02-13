package com.example.isp_icon

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import com.example.isp_icon.data.InspectionStatusEntity
import com.example.isp_icon.data.PertanyaanEntity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.res.ColorStateList

class DynamicFormActivity : AppCompatActivity() {

    private lateinit var session: InspectionSession
    private lateinit var categoryName: String
    private lateinit var llFormContainer: LinearLayout

    // Menyimpan referensi input user (Key: ID Soal, Value: View)
    private val inputViewsMap = mutableMapOf<String, View>()

    // Launcher Kamera Sederhana (Untuk menangkap hasil foto)
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Foto berhasil diambil! (Logika upload file menyusul)", Toast.LENGTH_SHORT).show()
            // Catatan: Di tahap selanjutnya kita bisa tambahkan logika menampilkan thumbnail foto di sini
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_form)

        // 1. Ambil Data Session dari Halaman Sebelumnya
        @Suppress("DEPRECATION")
        session = intent.getParcelableExtra("SESSION_DATA") ?: return
        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: "Inspeksi"

        // 2. Setup Header
        findViewById<TextView>(R.id.tvKategoriTitle).text = "Formulir: $categoryName"
        llFormContainer = findViewById(R.id.llFormContainer)

        // 3. Load Soal dari Database
        loadQuestions()

        // 4. Tombol Simpan
        findViewById<Button>(R.id.btnSimpan).setOnClickListener {
            simpanJawaban()
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

    // --- GENERATE TAMPILAN FORM ---
    private fun generateFormUI(questions: List<PertanyaanEntity>) {
        llFormContainer.removeAllViews()
        inputViewsMap.clear()

        for (q in questions) {
            // A. Buat Label Pertanyaan
            val label = TextView(this)
            label.text = q.pertanyaan
            label.textSize = 14f
            label.setTextColor(Color.DKGRAY)
            label.setTypeface(null, Typeface.BOLD)
            label.setPadding(0, 32, 0, 8) // Jarak atas bawah
            llFormContainer.addView(label)

            // B. Tentukan Tipe Input
            val inputType = q.tipeInput?.uppercase() ?: "TEKS"

            val inputView: View = when (inputType) {
                "YA_TIDAK" -> createYesNoLayout()
                "ANGKA" -> createStyledEditText(isNumber = true, hint = "Masukkan Angka")
                "PILIHAN" -> createChipSelection(q.opsiPilihan)
                "FOTO" -> createPhotoContainer()
                else -> createStyledEditText(isNumber = false, hint = "Masukkan Teks")
            }

            // C. Masukkan ke Layar & Map
            llFormContainer.addView(inputView)
            inputViewsMap[q.idPertanyaan] = inputView
        }
    }

    // --- WIDGET HELPER FUNCTIONS ---

    // 1. EDIT TEXT (Border Biru/Hijau)
    private fun createStyledEditText(isNumber: Boolean, hint: String): EditText {
        val et = EditText(this)

        // 1. Konversi DP ke Pixel (Agar ukuran konsisten di semua HP)
        val scale = resources.displayMetrics.density
        val minHeightPx = (50 * scale + 0.5f).toInt() // Tinggi minimal 50dp (Standar UI Android)
        val paddingPx = (16 * scale + 0.5f).toInt()   // Padding 16dp (Lega)
        val marginPx = (12 * scale + 0.5f).toInt()    // Margin 12dp

        // 2. Atur Layout Params (Wajib WRAP_CONTENT agar bisa membesar, tapi punya minHeight)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 0, 0, marginPx) // Jarak bawah antar elemen
        et.layoutParams = params

        // 3. SET BACKGROUND DULUAN (PENTING! Sebelum set padding/height)
        if (isNumber) {
            et.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            et.background = ContextCompat.getDrawable(this, R.drawable.bg_input_green)
        } else {
            et.inputType = InputType.TYPE_CLASS_TEXT
            et.background = ContextCompat.getDrawable(this, R.drawable.bg_input_blue)
        }

        // 4. BARU SET PADDING & HEIGHT (Setelah set background)
        et.hint = hint
        et.textSize = 16f
        et.setPadding(paddingPx, paddingPx, paddingPx, paddingPx) // Padding dalam
        et.minimumHeight = minHeightPx // Pakai property minimumHeight

        // Opsional: Paksa tinggi jika minimumHeight masih bandel (Hapus komen di bawah jika perlu)
        // et.height = minHeightPx

        return et
    }
    // 2. YA / TIDAK (Chip Merah & Hijau Kustom)
    private fun createYesNoLayout(): RadioGroup {
        val rg = RadioGroup(this)
        rg.orientation = LinearLayout.HORIZONTAL
        rg.weightSum = 2f // Agar terbagi rata 50:50

        // Tombol TIDAK (Kiri - Merah)
        val rbNo = RadioButton(this)
        rbNo.text = "Tidak / Rusak"
        rbNo.id = View.generateViewId()
        rbNo.buttonDrawable = null // Hapus bulatan radio default
        rbNo.gravity = Gravity.CENTER
        rbNo.setTextColor(Color.BLACK)
        // Pastikan Anda sudah membuat file drawable/selector_chip_no.xml
        rbNo.background = ContextCompat.getDrawable(this, R.drawable.selector_chip_no)

        val paramsNo = RadioGroup.LayoutParams(0, 120) // Tinggi fix
        paramsNo.weight = 1f
        paramsNo.setMargins(0, 0, 16, 0) // Jarak kanan
        rbNo.layoutParams = paramsNo

        // Tombol YA (Kanan - Hijau)
        val rbYes = RadioButton(this)
        rbYes.text = "Ya / Baik"
        rbYes.id = View.generateViewId()
        rbYes.buttonDrawable = null
        rbYes.gravity = Gravity.CENTER
        rbYes.setTextColor(Color.BLACK)
        // Pastikan Anda sudah membuat file drawable/selector_chip_yes.xml
        rbYes.background = ContextCompat.getDrawable(this, R.drawable.selector_chip_yes)

        val paramsYes = RadioGroup.LayoutParams(0, 120)
        paramsYes.weight = 1f
        rbYes.layoutParams = paramsYes

        // Logic Ganti Warna Teks saat diklik (Agar kontras dengan background)
        rg.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == rbNo.id) {
                // Jika pilih Tidak (Merah)
                rbNo.setTextColor(Color.WHITE)
                rbYes.setTextColor(Color.BLACK)
            } else if (checkedId == rbYes.id) {
                // Jika pilih Ya (Hijau)
                rbYes.setTextColor(Color.WHITE)
                rbNo.setTextColor(Color.BLACK)
            }
        }

        rg.addView(rbNo)
        rg.addView(rbYes)
        return rg
    }

    // 3. PILIHAN (Chip Vertical Biru)
    private fun createChipSelection(options: String?): ChipGroup {
        val cg = ChipGroup(this)
        cg.isSingleSelection = true
        cg.isSelectionRequired = true // Wajib pilih salah satu

        val listOptions = options?.split(",") ?: listOf()

        // --- ATURAN WARNA (State List) ---
        // Checked = Biru, Unchecked = Putih
        val backgroundColorState = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(
                Color.parseColor("#2196F3"), // Warna Biru saat dipilih
                Color.WHITE                  // Warna Putih saat tidak dipilih
            )
        )

        // Teks: Checked = Putih, Unchecked = Hitam
        val textColorState = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.WHITE, Color.BLACK)
        )

        for (opt in listOptions) {
            val chip = Chip(this)
            chip.text = opt.trim()

            // --- PERBAIKAN KLIK DI SINI ---
            chip.id = View.generateViewId() // WAJIB: ID Unik agar ChipGroup tahu siapa yang diklik
            chip.isCheckable = true
            chip.isClickable = true

            // Terapkan Warna
            chip.chipBackgroundColor = backgroundColorState
            chip.setTextColor(textColorState)

            // Style Border
            chip.chipStrokeWidth = 2f
            chip.chipStrokeColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))

            // Agar lebar chip mengisi layar (opsional, hapus jika ingin ukuran menyesuaikan teks)
            chip.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            chip.textAlignment = View.TEXT_ALIGNMENT_CENTER

            cg.addView(chip)
        }
        return cg
    }
    // 4. FOTO (Kotak Garis Putus-putus)
    private fun createPhotoContainer(): LinearLayout {
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.gravity = Gravity.CENTER
        // Pastikan Anda sudah membuat file drawable/bg_photo_dashed.xml
        container.background = ContextCompat.getDrawable(this, R.drawable.bg_photo_dashed)
        container.setPadding(0, 60, 0, 60)

        // Ikon Kamera (Menggunakan Text Emoji agar simpel, bisa diganti ImageView)
        val icon = TextView(this)
        icon.text = "📷"
        icon.textSize = 32f
        icon.gravity = Gravity.CENTER

        val text = TextView(this)
        text.text = "Buka Kamera / Galeri"
        text.textSize = 14f
        text.setTextColor(Color.GRAY)
        text.gravity = Gravity.CENTER
        text.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = 8 }

        container.addView(icon)
        container.addView(text)

        // Klik untuk buka kamera
        container.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                cameraLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Tidak dapat membuka kamera", Toast.LENGTH_SHORT).show()
            }
        }
        return container
    }

    // --- LOGIKA SIMPAN JAWABAN ---
    private fun simpanJawaban() {
        val btnSimpan = findViewById<Button>(R.id.btnSimpan)
        btnSimpan.isEnabled = false
        btnSimpan.text = "Mengirim..."

        val mapJawaban = mutableMapOf<String, String>()

        // 1. Panen Jawaban dari UI
        for ((idSoal, view) in inputViewsMap) {
            var jawabanUser = "-"

            when (view) {
                is EditText -> {
                    jawabanUser = view.text.toString()
                }
                is ChipGroup -> { // Untuk Tipe PILIHAN
                    val selectedId = view.checkedChipId
                    if (selectedId != -1) {
                        val chip = view.findViewById<Chip>(selectedId)
                        jawabanUser = chip.text.toString()
                    }
                }
                is RadioGroup -> { // Untuk Tipe YA_TIDAK
                    val selectedId = view.checkedRadioButtonId
                    if (selectedId != -1) {
                        val radioButton = view.findViewById<RadioButton>(selectedId)
                        jawabanUser = radioButton.text.toString()
                    }
                }
                // Case Foto bisa ditambahkan nanti
            }
            mapJawaban[idSoal] = jawabanUser
        }

        // 2. Siapkan Paket Data
        val request = InspectionRequest(
            noWo = session.noWo,
            idSite = session.idSite,
            namaPelaksana = session.pelaksana ?: "-",
            kategori = categoryName,
            jawaban = mapJawaban
        )

        // 3. Kirim ke Server
        ApiClient.instance.submitInspection(request = request).enqueue(object : Callback<MaintenanceResponse> {
            override fun onResponse(call: Call<MaintenanceResponse>, response: Response<MaintenanceResponse>) {
                btnSimpan.isEnabled = true
                btnSimpan.text = "SIMPAN JAWABAN"

                if (response.isSuccessful) {
                    Toast.makeText(applicationContext, "Data Berhasil Disimpan!", Toast.LENGTH_LONG).show()

                    // --- UPDATE STATUS DI DATABASE LOKAL (Agar muncul Ceklis Hijau) ---
                    simpanStatusLokal()
                } else {
                    Toast.makeText(applicationContext, "Gagal Server: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<MaintenanceResponse>, t: Throwable) {
                btnSimpan.isEnabled = true
                btnSimpan.text = "SIMPAN JAWABAN"
                Toast.makeText(applicationContext, "Error Koneksi: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun simpanStatusLokal() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // Simpan status bahwa kategori ini untuk No WO ini sudah selesai
            db.appDao().insertStatus(
                InspectionStatusEntity(
                    noWo = session.noWo,
                    kategori = categoryName,
                    isCompleted = true
                )
            )

            withContext(Dispatchers.Main) {
                // Tutup halaman ini, kembali ke Menu Inspeksi
                finish()
            }
        }
    }
}