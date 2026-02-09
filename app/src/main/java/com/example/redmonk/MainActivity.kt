package com.example.redmonk

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redmonk.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var monitor: NetworkMonitor
    private lateinit var adapter: AppUsageAdapter

    // Stores latest captured data for report
    private var latestUsageList: List<AppUsage> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView setup
        adapter = AppUsageAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Network Monitor
        monitor = NetworkMonitor(this) { list ->

            // 🔥 Analyze each app for anomaly / suspicious behaviour
            val enriched = list.map { app ->
                val result = RiskAnalyzer.analyze(this, app)
                app.copy(
                    riskLevel = result.level,
                    riskReason = result.reason
                )
            }

            // keep for report
            latestUsageList = enriched

            // save into behavioural history
            UsageHistory.save(this, enriched)

            // update UI
            adapter.update(enriched)
        }

        // Start monitoring
        binding.startBtn.setOnClickListener {
            binding.statusText.text = "Status: Running"
            monitor.start()
        }

        // Stop monitoring
        binding.stopBtn.setOnClickListener {
            binding.statusText.text = "Status: Stopped"
            monitor.stop()
        }

        // Generate forensic report
        binding.reportBtn.setOnClickListener {
            if (latestUsageList.isEmpty()) {
                Toast.makeText(this, "No data to generate report", Toast.LENGTH_SHORT).show()
            } else {
                PdfReportGenerator.generate(this, latestUsageList)
                Toast.makeText(this, "Forensic report saved", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        monitor.stop()
        super.onDestroy()
    }
}
