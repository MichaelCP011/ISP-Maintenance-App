package com.example.isp_icon

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Menu Checklist -> Masuk ke MainActivity (List Data)
        findViewById<CardView>(R.id.menuChecklist).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Menu Monitoring -> Nanti kita buat halamannya
        findViewById<CardView>(R.id.menuMonitoring).setOnClickListener {
            Toast.makeText(this, "Fitur Monitoring akan segera hadir!", Toast.LENGTH_SHORT).show()
            // Nanti kita arahkan ke MonitoringActivity
        }
    }
}