package com.example.parkir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.parkir.databinding.ItemTransactionBinding
import com.example.parkir.model.TransaksiParkirResponse

class TransactionAdapter(
    private val onDelete: (TransaksiParkirResponse) -> Unit
) : ListAdapter<TransaksiParkirResponse, TransactionAdapter.ViewHolder>(TransactionDiffCallback()) {

    inner class ViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: TransaksiParkirResponse) {
            binding.apply {
                // Ubah format tampilan untuk lebih rapi
                val displayText = buildString {
                    appendLine("Nomor Plat: ${transaction.kendaraan.nomorPlat}")
                    appendLine("Jenis: ${transaction.kendaraan.jenisKendaraan}")
                    appendLine("Lokasi: ${transaction.lokasiParkir.namaLokasi}")
                    appendLine("Status: ${transaction.status}")
                    append("Biaya: Rp ${transaction.biaya}")
                }

                // Gunakan satu TextView untuk menampilkan semua info
                tvTransactionInfo.text = displayText

                btnDelete.setOnClickListener { onDelete(transaction) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TransactionDiffCallback : DiffUtil.ItemCallback<TransaksiParkirResponse>() {
    override fun areItemsTheSame(oldItem: TransaksiParkirResponse, newItem: TransaksiParkirResponse) =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: TransaksiParkirResponse, newItem: TransaksiParkirResponse) =
        oldItem == newItem
}