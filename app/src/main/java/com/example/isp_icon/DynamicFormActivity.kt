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

        val scale = resources.displayMetrics.density

        questions.forEachIndexed { index, q ->
            val nomorSoal = index + 1

            // ========================================================
            // 1. BUAT CONTAINER (Kotak Card untuk tiap soal)
            // ========================================================
            val cardContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                // Background Putih, Sudut Membulat, Border Abu-abu halus
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                    setColor(Color.WHITE)
                    cornerRadius = 24f // Sudut melengkung
                    setStroke(2, Color.parseColor("#E5E7EB")) // Border abu-abu terang
                }
                // Padding dalam kotak
                setPadding((16 * scale).toInt(), (16 * scale).toInt(), (16 * scale).toInt(), (20 * scale).toInt())

                // Margin antar kotak soal
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (16 * scale).toInt()) // Jarak bawah ke soal berikutnya
                }
            }

            // ========================================================
            // 2. HEADER (Nomor + Teks Pertanyaan)
            // ========================================================
            val headerLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = (16 * scale).toInt() // Jarak antara teks dan tombol/input
                }
            }

            // A. Bulatan Nomor
            val badgeSize = (32 * scale).toInt()
            val badgeMargin = (12 * scale).toInt()
            val badgeNumber = TextView(this).apply {
                text = "$nomorSoal"
                setTextColor(Color.WHITE)
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(Color.parseColor("#3B82F6")) // Biru
                }
                layoutParams = LinearLayout.LayoutParams(badgeSize, badgeSize).apply {
                    marginEnd = badgeMargin
                }
            }

            // B. Teks Pertanyaan
            val labelTeks = TextView(this).apply {
                text = q.pertanyaan
                textSize = 15f
                setTextColor(Color.parseColor("#374151")) // Abu-abu gelap
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (4 * scale).toInt() // Sesuaikan agar sejajar tengah dengan nomor
                }
            }

            headerLayout.addView(badgeNumber)
            headerLayout.addView(labelTeks)

            // ========================================================
            // 3. TENTUKAN TIPE INPUT
            // ========================================================
            val inputType = q.tipeInput?.uppercase() ?: "TEKS"
            val inputView: View = when (inputType) {
                "YA_TIDAK" -> createYesNoLayout()
                "ANGKA" -> createStyledEditText(isNumber = true, hint = "Masukkan Angka")
                "PILIHAN" -> createChipSelection(q.opsiPilihan)
                "FOTO" -> createPhotoContainer()
                else -> createStyledEditText(isNumber = false, hint = "Masukkan Teks")
            }

            // ========================================================
            // 4. MENSEJAJARKAN INPUT DENGAN TEKS (Bukan dengan nomor)
            // ========================================================
            // Hitung jarak margin kiri (Lebar bulatan + marginnya)
            val indentLeft = badgeSize + badgeMargin

            val inputParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            inputParams.leftMargin = indentLeft // Terapkan indentasi
            inputView.layoutParams = inputParams

            // ========================================================
            // 5. RAKIT SEMUA KE DALAM CONTAINER
            // ========================================================
            cardContainer.addView(headerLayout)
            cardContainer.addView(inputView)

            llFormContainer.addView(cardContainer)
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
    // --- 2. YA / TIDAK (Diperbaiki Icon dan Teksnya agar Rapi & Rata Tengah) ---
    private fun createYesNoLayout(): RadioGroup {
        val scale = resources.displayMetrics.density
        val rg = RadioGroup(this)
        rg.orientation = LinearLayout.HORIZONTAL

        // Layout Parameters dihapus marginnya karena sudah diatur oleh Container
        rg.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // Definisi Warna
        val colorGrayText = Color.parseColor("#6B7280")
        val colorWhite = Color.WHITE
        val colorRed = Color.parseColor("#EF4444")
        val colorGreen = Color.parseColor("#10B981")
        val colorBorder = Color.parseColor("#E5E7EB")

        val buttonHeight = (46 * scale).toInt()

        // =========================================================
        // HELPER SAKTI: Untuk me-resize Icon agar tidak raksasa
        // dan bisa diubah warnanya (Putih saat diklik, Abu saat normal)
        // =========================================================
        fun getScaledIcon(iconId: Int, color: Int): android.graphics.drawable.Drawable? {
            val drawable = ContextCompat.getDrawable(this, iconId)?.mutate()
            drawable?.setTint(color) // Beri warna pada icon
            val sizePx = (18 * scale).toInt() // Kunci ukurannya di 18dp agar mungil & rapi
            drawable?.setBounds(0, 0, sizePx, sizePx)
            return drawable
        }

        // ================= TOMBOL TIDAK =================
        val rbNo = RadioButton(this)
        rbNo.text = "Tidak"
        rbNo.id = View.generateViewId()
        rbNo.buttonDrawable = null
        rbNo.gravity = Gravity.CENTER
        rbNo.setTextColor(colorGrayText)
        rbNo.background = createButtonBackground(colorRed, colorBorder)

        // Pasang icon yang sudah di-resize pakai helper di atas
        rbNo.setCompoundDrawables(getScaledIcon(R.drawable.ic_close, colorGrayText), null, null, null)
        rbNo.compoundDrawablePadding = (8 * scale).toInt() // Jarak spasi antara icon dan teks

        val paramsNo = RadioGroup.LayoutParams(0, buttonHeight)
        paramsNo.weight = 1f
        paramsNo.marginEnd = (12 * scale).toInt() // Jarak pemisah di tengah
        rbNo.layoutParams = paramsNo

        // PENTING: Atur padding kiri (32dp) agar icon terdorong ke tengah, tidak nempel di pinggir!
        rbNo.setPadding((32 * scale).toInt(), 0, (16 * scale).toInt(), 0)

        // ================= TOMBOL YA =================
        val rbYes = RadioButton(this)
        rbYes.text = "Ya"
        rbYes.id = View.generateViewId()
        rbYes.buttonDrawable = null
        rbYes.gravity = Gravity.CENTER
        rbYes.setTextColor(colorGrayText)
        rbYes.background = createButtonBackground(colorGreen, colorBorder)

        // Pasang icon yang sudah di-resize
        rbYes.setCompoundDrawables(getScaledIcon(R.drawable.ic_check, colorGrayText), null, null, null)
        rbYes.compoundDrawablePadding = (8 * scale).toInt()

        val paramsYes = RadioGroup.LayoutParams(0, buttonHeight)
        paramsYes.weight = 1f
        rbYes.layoutParams = paramsYes

        // Atur padding kiri (32dp) sama seperti tombol Tidak
        rbYes.setPadding((32 * scale).toInt(), 0, (16 * scale).toInt(), 0)

        // ================= LOGIKA KLIK =================
        rg.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == rbNo.id) {
                // Jika pilih TIDAK: Teks & Icon berubah jadi Putih
                rbNo.setTextColor(colorWhite)
                rbNo.setCompoundDrawables(getScaledIcon(R.drawable.ic_close, colorWhite), null, null, null)

                // Tombol YA kembali ke warna Normal (Abu-abu)
                rbYes.setTextColor(colorGrayText)
                rbYes.setCompoundDrawables(getScaledIcon(R.drawable.ic_check, colorGrayText), null, null, null)

            } else if (checkedId == rbYes.id) {
                // Jika pilih YA: Teks & Icon berubah jadi Putih
                rbYes.setTextColor(colorWhite)
                rbYes.setCompoundDrawables(getScaledIcon(R.drawable.ic_check, colorWhite), null, null, null)

                // Tombol TIDAK kembali ke warna Normal (Abu-abu)
                rbNo.setTextColor(colorGrayText)
                rbNo.setCompoundDrawables(getScaledIcon(R.drawable.ic_close, colorGrayText), null, null, null)
            }
        }

        rg.addView(rbNo)
        rg.addView(rbYes)
        return rg
    }

    // --- FUNGSI BANTUAN BACKGROUND TOMBOL ---
    private fun createButtonBackground(selectedColor: Int, borderColor: Int): android.graphics.drawable.StateListDrawable {
        val stateList = android.graphics.drawable.StateListDrawable()
        val scale = resources.displayMetrics.density

        // Normal (Putih + Garis Abu)
        val normalShape = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 8f * scale // Sudut membulat sedikit (bukan kapsul)
            setColor(Color.WHITE)
            setStroke(2, borderColor)
        }

        // Dipilih (Warna Solid Hijau/Merah)
        val checkedShape = android.graphics.drawable.GradientDrawable().apply {
            shape = android.graphics.drawable.GradientDrawable.RECTANGLE
            cornerRadius = 8f * scale
            setColor(selectedColor)
        }

        stateList.addState(intArrayOf(android.R.attr.state_checked), checkedShape)
        stateList.addState(intArrayOf(-android.R.attr.state_checked), normalShape)

        return stateList
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