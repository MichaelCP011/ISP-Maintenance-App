package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.isp_icon.data.AppDatabase
import com.example.isp_icon.data.LokasiEntity
import com.example.isp_icon.data.PersonilEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InspectionHeaderActivity : AppCompatActivity() {

    // Variabel Penampung Data
    private var listLokasi = listOf<LokasiEntity>()
    private var selectedLokasi: LokasiEntity? = null

    // View
    private lateinit var spLokasi: Spinner
    private lateinit var tvTipePop: TextView
    private lateinit var etNoWo: EditText
    private lateinit var etTanggal: EditText
    private lateinit var spPelaksana: Spinner
    private lateinit var spPemeriksa: Spinner
    private lateinit var spAsman: Spinner
    private lateinit var spManajer: Spinner
    private lateinit var spArea: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspection_header)

        // 1. Inisialisasi View
        spLokasi = findViewById(R.id.spLokasi)
        tvTipePop = findViewById(R.id.tvTipePop)
        etNoWo = findViewById(R.id.etNoWo)
        etTanggal = findViewById(R.id.etTanggal)
        spPelaksana = findViewById(R.id.spPelaksana)
        spPemeriksa = findViewById(R.id.spPemeriksa)
        spAsman = findViewById(R.id.spAsman)
        spManajer = findViewById(R.id.spManajer)
        spArea = findViewById(R.id.spArea)

        // Set Tanggal Hari Ini
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        etTanggal.setText(today)

        // Setup Area (Hardcoded atau dari DB kalau mau)
        val listArea = listOf("Jakarta Barat", "Jakarta Pusat", "Tangerang", "Banten")
        spArea.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listArea)

        // 2. Load Data dari Database Lokal
        loadDataFromDB()

        // 3. Tombol Lanjut
        findViewById<Button>(R.id.btnLanjut).setOnClickListener {
            validasiDanLanjut()
        }
    }

    private fun loadDataFromDB() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)

            // Ambil Data
            listLokasi = db.appDao().getAllLokasi()
            val listPersonil = db.appDao().getAllPersonil()

            // Update UI di Main Thread
            withContext(Dispatchers.Main) {
                setupSpinnerLokasi()
                setupSpinnerPersonil(listPersonil)
            }
        }
    }

    private fun setupSpinnerLokasi() {
        // Kita hanya tampilkan Nama Site di Dropdown
        val namaLokasiList = listLokasi.map { it.namaSite ?: "Tanpa Nama" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, namaLokasiList)
        spLokasi.adapter = adapter

        // Listener saat lokasi dipilih
        spLokasi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLokasi = listLokasi[position]
                // Otomatis isi Tipe POP
                tvTipePop.text = selectedLokasi?.tipe ?: "-"
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSpinnerPersonil(listAll: List<PersonilEntity>) {
        // Helper function buat filter & pasang adapter
        fun setAdapter(spinner: Spinner, jabatanFilter: String) {
            // Filter: Cari yang jabatannya mengandung kata kunci (Case insensitive)
            // Atau tampilkan semua kalau datanya belum ada jabatan spesifik
            val filtered = listAll.filter {
                it.jabatan?.contains(jabatanFilter, ignoreCase = true) == true
            }.map { it.namaLengkap }

            // Fallback: Jika tidak ada yang cocok, tampilkan semua nama
            val finalData = if (filtered.isNotEmpty()) filtered else listAll.map { it.namaLengkap }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, finalData)
            spinner.adapter = adapter
        }

        // Pasang ke masing-masing Spinner
        setAdapter(spPelaksana, "Pelaksana")
        setAdapter(spPemeriksa, "Pemeriksa")
        setAdapter(spAsman, "Asman")
        setAdapter(spManajer, "Manajer")
    }

    private fun validasiDanLanjut() {
        val noWo = etNoWo.text.toString()
        if (noWo.isEmpty()) {
            etNoWo.error = "Wajib Diisi!"
            return
        }
        if (selectedLokasi == null) {
            Toast.makeText(this, "Lokasi belum dimuat", Toast.LENGTH_SHORT).show()
            return
        }

        val session = InspectionSession(
            noWo = noWo,
            tanggal = etTanggal.text.toString(),
            idSite = selectedLokasi!!.idSite,
            namaSite = selectedLokasi!!.namaSite ?: "",
            tipePop = selectedLokasi!!.tipe ?: "",
            pelaksana = spPelaksana.selectedItem.toString(),
            pemeriksa = spPemeriksa.selectedItem.toString(),
            asman = spAsman.selectedItem.toString(),
            manajer = spManajer.selectedItem.toString(),
            area = spArea.selectedItem.toString()
        )

        // PINDAH HALAMAN
        val intent = Intent(this, InspectionMenuActivity::class.java)
        intent.putExtra("SESSION_DATA", session)
        startActivity(intent)
    }
}