package com.example.redmonk

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.redmonk.databinding.ItemAppUsageBinding
import java.text.SimpleDateFormat
import java.util.*

class AppUsageAdapter(private val context: Context) :
    RecyclerView.Adapter<AppUsageAdapter.ViewHolder>() {

    private val list = mutableListOf<AppUsage>()

    fun update(newList: List<AppUsage>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemAppUsageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppUsageBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        holder.binding.appIcon.setImageDrawable(item.icon)

        // ===== APP NAME + RUNNING =====
        if (item.isRunningNow) {
            holder.binding.appName.text = "🟢 ${item.appName} (RUNNING)"
        } else {
            holder.binding.appName.text = item.appName
        }

        // ===== DATA =====
        holder.binding.dataText.text =
            "↑ ${item.sent / 1024} KB  ↓ ${item.received / 1024} KB"

        // ===== PROTOCOL =====
        holder.binding.protocolText.text = item.protocol

        // ===== TIME + REASON =====
        holder.binding.timeText.text =
            "${sdf.format(Date(item.time))}  |  ${item.riskReason}"

        // ===== RISK COLOR =====
        when (item.riskLevel) {
            "HIGH" -> holder.binding.appName.setTextColor(Color.RED)
            "MEDIUM" -> holder.binding.appName.setTextColor(Color.parseColor("#FFA500"))
            else -> holder.binding.appName.setTextColor(Color.parseColor("#4CAF50"))
        }
    }

    override fun getItemCount() = list.size
}
