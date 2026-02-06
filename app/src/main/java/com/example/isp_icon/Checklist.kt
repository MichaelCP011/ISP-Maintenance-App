package com.example.isp_icon

import DashboardAdapter
import DashboardMenu
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity // 1. Ubah import ini
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 2. Warisi AppCompatActivity, bukan Fragment
class Checklist : AppCompatActivity() {

    // 3. Gunakan onCreate, bukan onViewCreated
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 4. Set layout di sini
        setContentView(R.layout.activity_checklist2)

        // 5. Hapus 'view.', langsung cari ID-nya
        val recyclerView = findViewById<RecyclerView>(R.id.rvMenuDashboard)

        // --- DATA (Tidak ada perubahan) ---
        val menuData = listOf(
            DashboardMenu("Checklist", "Form untuk checklist pekerjaan preventive maintenance ISP", R.drawable.ceklis),
            DashboardMenu("Grounding", "Form untuk pendataan grounding", R.drawable.grounding),
            DashboardMenu("KWH Meter", "Form untuk pendataan KWH meter", R.drawable.kwh),
            DashboardMenu("Genset", "Form untuk pendataan genset", R.drawable.genset),
            DashboardMenu("ACPDB", "Form untuk pendataan ACPDB", R.drawable.genset),
            DashboardMenu("DCPDB", "Form untuk pendataan DCPDB", R.drawable.dcpdb),
            DashboardMenu("Rectifier", "Form untuk pendataan rectifier", R.drawable.rectifier),
            DashboardMenu("Inverter", "Form untuk pendataan inverter", R.drawable.inverter),
            DashboardMenu("Batere", "Form untuk pendataan batere", R.drawable.batere),
            DashboardMenu("Uji Batere", "Form untuk pendataan uji kapasitas batere", R.drawable.ujibatere),
            DashboardMenu("Environment", "Form untuk pendataan uji eksternal alarm", R.drawable.environment),
            DashboardMenu("Ac", "Form untuk pendataan Air Conditioner milik ICON+", R.drawable.ac),
            DashboardMenu("Data Perangkat", "Form untuk pendataan perangkat ICON+", R.drawable.dataperangkat),
            DashboardMenu("Dokumentasi POP", "Form untuk pendataan dokumentasi pekerjaan", R.drawable.dokumentasipop),
            DashboardMenu("Room Layout", "Form untuk pendataan room layout", R.drawable.roomlayout),
            DashboardMenu("SLD", "Form untuk pendataan single line diagram", R.drawable.sld),
        )

        // 6. Setup Adapter
        val adapter = DashboardAdapter(menuData) { item ->
            // Gunakan 'this' sebagai context di Activity
            Toast.makeText(this, "Kamu memilih: ${item.title}", Toast.LENGTH_SHORT).show()
        }

        // 7. Pasang ke RecyclerView
        // Gunakan 'this' untuk context layout manager
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}