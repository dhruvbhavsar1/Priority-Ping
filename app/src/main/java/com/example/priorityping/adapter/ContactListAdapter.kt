package com.example.priorityping.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.priorityping.R
import com.example.priorityping.databinding.ItemContactBinding
import com.example.priorityping.model.PriorityContactEntity
import com.example.priorityping.model.PriorityLevel
import com.example.priorityping.model.SupportedApps

class ContactListAdapter(
    private val onToggleActive: (PriorityContactEntity) -> Unit,
    private val onDelete: (PriorityContactEntity) -> Unit,
    private val onClick: (PriorityContactEntity) -> Unit
) : ListAdapter<PriorityContactEntity, ContactListAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: PriorityContactEntity) {
            binding.txtIdentifier.text = contact.identifier
            binding.txtLabel.text = contact.label
            binding.txtLabel.visibility = if (contact.label.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            
            binding.txtAvatar.text = contact.identifier.take(1).uppercase()
            val app = SupportedApps.fromAppName(contact.appName)
            
            // Part 2: Contact List Screen Enhancement - Brand color avatars
            if (contact.appName == "instagram") {
                val gd = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(Color.parseColor("#833AB4"), Color.parseColor("#FD1D1D"), Color.parseColor("#FCB045"))
                )
                gd.cornerRadius = 100f // circle
                binding.avatarContainer.background = gd
            } else {
                val appColor = app?.let { ContextCompat.getColor(binding.root.context, it.colorRes) } 
                    ?: ContextCompat.getColor(binding.root.context, R.color.colorPrimary)
                binding.avatarContainer.setCardBackgroundColor(appColor)
            }
            
            binding.txtAvatar.setTextColor(if (contact.appName == "snapchat") Color.BLACK else Color.WHITE)

            binding.chipApp.text = app?.displayName ?: contact.appName
            binding.chipPriority.text = contact.priorityLevel.displayName
            
            // Part 2: Priority level chip colors
            val priorityConfig = when (contact.priorityLevel) {
                PriorityLevel.HIGH -> Triple("#3B0A0D", "#FF6B6B", "#CF6679")
                PriorityLevel.MEDIUM -> Triple("#2D1F00", "#FFB74D", "#FB8C00")
                PriorityLevel.NORMAL -> Triple("#1A2B1A", "#81C784", "#4CAF50")
            }
            
            binding.chipPriority.chipBackgroundColor = ColorStateList.valueOf(Color.parseColor(priorityConfig.first))
            binding.chipPriority.setTextColor(Color.parseColor(priorityConfig.second))
            binding.chipPriority.chipStrokeColor = ColorStateList.valueOf(Color.parseColor(priorityConfig.third))
            binding.chipPriority.chipStrokeWidth = 1f

            binding.switchActive.setOnCheckedChangeListener(null)
            binding.switchActive.isChecked = contact.isActive
            binding.switchActive.setOnCheckedChangeListener { _, _ ->
                onToggleActive(contact)
            }

            binding.btnDelete.setOnClickListener {
                onDelete(contact)
            }

            binding.root.setOnClickListener {
                onClick(contact)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PriorityContactEntity>() {
        override fun areItemsTheSame(oldItem: PriorityContactEntity, newItem: PriorityContactEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PriorityContactEntity, newItem: PriorityContactEntity) = oldItem == newItem
    }
}
