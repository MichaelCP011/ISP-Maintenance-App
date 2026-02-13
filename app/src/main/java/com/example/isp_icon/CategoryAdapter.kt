package com.example.isp_icon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<String>,
    private val completedList: List<String>, // Parameter Baru: Daftar yg sudah selesai
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvDesc: TextView = view.findViewById(R.id.tvCategoryDesc)
        val ivIcon: ImageView = view.findViewById(R.id.ivCategoryIcon)
        val ivCheck: ImageView = view.findViewById(R.id.ivCheckStatus) // Referensi Ikon Ceklis
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_checklist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoryName = categories[position]

        holder.tvName.text = categoryName
        holder.tvDesc.text = "Formulir pendataan $categoryName"

        // --- LOGIC IKON KATEGORI ---
        val iconRes = when (categoryName.lowercase().trim()) {
            "genset" -> R.drawable.genset
            "ac" -> R.drawable.ac
            "grounding" -> R.drawable.grounding
            "kwh meter" -> R.drawable.kwh
            "checklist" -> R.drawable.ceklis // Pastikan nama file drawable benar
            "rectifier" -> R.drawable.rectifier
            "inverter" -> R.drawable.inverter
            "batere" -> R.drawable.batere
            "room layout" -> R.drawable.roomlayout
            "sld" -> R.drawable.sld
            "acpdb" -> R.drawable.acpdb
            "dcpdb" -> R.drawable.dcpdb
            "uji batere" -> R.drawable.ujibatere
            "environment" -> R.drawable.environment
            "data perangkat" -> R.drawable.dataperangkat
            "dokumentasi pop" -> R.drawable.dokumentasipop
            else -> R.drawable.ceklistt
        }

        try {
            holder.ivIcon.setImageResource(iconRes)
        } catch (e: Exception) {
            holder.ivIcon.setImageResource(R.drawable.ceklistt)
        }

        // --- LOGIC CEKLIS HIJAU (Status Selesai) ---
        // Cek apakah kategori ini ada di dalam database status
        if (completedList.contains(categoryName)) {
            holder.ivCheck.visibility = View.VISIBLE
            // Opsional: Ubah teks deskripsi
            holder.tvDesc.text = "Selesai dikerjakan"
            holder.tvDesc.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
        } else {
            holder.ivCheck.visibility = View.GONE
            holder.tvDesc.text = "Formulir pendataan $categoryName"
            holder.tvDesc.setTextColor(android.graphics.Color.parseColor("#757575"))
        }

        holder.itemView.setOnClickListener {
            onClick(categoryName)
        }
    }

    override fun getItemCount() = categories.size
}