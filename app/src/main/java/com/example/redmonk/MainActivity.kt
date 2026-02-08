package com.example.redmonk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.redmonk.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var monitor: NetworkMonitor
    private lateinit var adapter: AppUsageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AppUsageAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        monitor = NetworkMonitor(this) { list ->
            adapter.update(list)
        }

        binding.startBtn.setOnClickListener {
            binding.statusText.text = "Status: Running"
            monitor.start()
        }

        binding.stopBtn.setOnClickListener {
            binding.statusText.text = "Status: Stopped"
            monitor.stop()
        }
    }

    override fun onDestroy() {
        monitor.stop()
        super.onDestroy()
    }
}
