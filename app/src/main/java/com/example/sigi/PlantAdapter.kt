package com.example.sigi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.sigi.R

class PlantAdapter(private var plantList: List<Plant>) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position]
        holder.nameTextView.text = plant.name
        holder.englishNameTextView.text = plant.englishName
        holder.plantImageView.load(plant.imageUrl) {
            placeholder(R.drawable.placeholder)
            error(R.drawable.error)
        }
    }

    override fun getItemCount(): Int = plantList.size

    // 필터링된 데이터로 업데이트
    fun updateData(newList: List<Plant>) {
        plantList = newList
        notifyDataSetChanged()
    }

    inner class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val englishNameTextView: TextView = view.findViewById(R.id.englishNameTextView)
        val plantImageView: ImageView = view.findViewById(R.id.plantImageView)
    }
}
