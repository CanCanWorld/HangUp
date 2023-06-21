package com.zrq.hangup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zrq.hangup.databinding.ItemLogBinding

class LogsAdapter(
    private val context: Context,
    private val list: MutableList<String>,
) : RecyclerView.Adapter<LogsAdapter.InnerHolder>() {

    class InnerHolder(var mBinding: ItemLogBinding) : RecyclerView.ViewHolder(mBinding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerHolder {
        val binding = ItemLogBinding.inflate(LayoutInflater.from(context), parent, false)
        return InnerHolder(binding)
    }

    override fun onBindViewHolder(holder: InnerHolder, position: Int) {
        holder.mBinding.apply {
            tvLog.text = list[position]
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}