package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment // Pastikan import ini benar

class HomeFragment : Fragment(R.layout.fragment_home) {

    // Gunakan onViewCreated untuk logika UI di dalam Fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menu Checklist -> Masuk ke MainActivity (List Data)
        // Perhatikan penggunaan 'view.findViewById'
        view.findViewById<CardView>(R.id.menuChecklist).setOnClickListener {
            // Gunakan 'requireContext()' atau 'requireActivity()' sebagai pengganti 'this'
            val intent = Intent(requireContext(), Checklist::class.java)
            startActivity(intent)
        }

        // Menu Monitoring
        view.findViewById<CardView>(R.id.menuMonitoring).setOnClickListener {
            // Gunakan 'requireContext()' untuk Toast
            // Gunakan 'requireContext()' atau 'requireActivity()' sebagai pengganti 'this'
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }
    }
}