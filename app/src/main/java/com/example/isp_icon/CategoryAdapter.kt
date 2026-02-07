package com.example.isp_icon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<String>,
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvDesc: TextView = view.findViewById(R.id.tvCategoryDesc)
        val ivIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Gunakan layout baru: item_checklist
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryName = categories[position]

        holder.tvName.text = categoryName
        holder.tvDesc.text = "Formulir pendataan $categoryName" // Deskripsi otomatis

        // LOGIC PILIH IKON BERDASARKAN NAMA KATEGORI
        // Pastikan nama file drawable di sini SAMA dengan yang ada di folder res/drawable kamu
        val iconRes = when (categoryName.lowercase()) {
            "genset" -> R.drawable.genset
            "ac" -> R.drawable.ac
            "grounding" -> R.drawable.grounding
            "kwh meter" -> R.drawable.kwh
            "checklist" -> R.drawable.ceklis // atau ceklistt
            "rectifier" -> R.drawable.rectifier
            "inverter" -> R.drawable.inverter
            "batere" -> R.drawable.batere
            "room layout" -> R.drawable.roomlayout
            "sld" -> R.drawable.sld
            else -> R.drawable.ceklistt // Ikon default jika tidak ada yg cocok
        }

        // Pasang ikon, tapi cek dulu biar ga crash kalau gambar belum ada
        try {
            holder.ivIcon.setImageResource(iconRes)
        } catch (e: Exception) {
            holder.ivIcon.setImageResource(R.drawable.ceklistt) // Fallback aman
        }

        // Klik
        holder.itemView.setOnClickListener {
            onClick(categoryName)
        }
    }

    override fun getItemCount() = categories.size
}