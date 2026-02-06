package com.example.isp_icon

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MaintenanceAdapter(
    private var listData: List<MaintenanceItem>,
    private val onClick: (MaintenanceItem) -> Unit // Agar bisa diklik nanti
) : RecyclerView.Adapter<MaintenanceAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvArea: TextView = view.findViewById(R.id.tvArea)      // Judul Utama
        val tvNamaSite: TextView = view.findViewById(R.id.tvNamaSite)
        val tvTanggal: TextView = view.findViewById(R.id.tvTanggal)
        val tvPelaksana: TextView = view.findViewById(R.id.tvPelaksana)
        val tvAsman: TextView = view.findViewById(R.id.tvAsman)    // Tambahan baru
        val tvTipePop: TextView = view.findViewById(R.id.tvTipePop)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_maintenance, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listData[position]

        // Set Judul (Area)
        holder.tvArea.text = item.area ?: "Area Unknown"

        val rawSite = item.namaSite ?: "-"
        val shortSite = rawSite.split(" / ")[0]
        holder.tvNamaSite.text = shortSite

        // Set Detail Lainnya
        holder.tvTanggal.text = ": ${item.tanggal ?: "-"}"
        holder.tvPelaksana.text = ": ${item.pelaksana ?: "-"}"
        holder.tvAsman.text = ": ${item.asman ?: "-"}"

        // Set Badge
        holder.tvTipePop.text = item.tipePop ?: "POP"

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount() = listData.size

    // Fungsi untuk update data baru dari API
    fun updateData(newData: List<MaintenanceItem>) {
        listData = newData
        notifyDataSetChanged()
    }
}