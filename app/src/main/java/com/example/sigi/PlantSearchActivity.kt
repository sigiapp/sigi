package com.example.sigi

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sigi.databinding.ActivityPlantSearchBinding
import com.google.firebase.firestore.FirebaseFirestore

class PlantSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantSearchBinding
    private lateinit var plantAdapter: PlantAdapter
    private val plantList = mutableListOf<Plant>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearchListener()
        fetchPlantsFromFirestore()

        // 뒤로가기 버튼 동작 추가
        binding.backButton.setOnClickListener {
            finish() // 현재 액티비티 종료
        }
    }

    private fun setupRecyclerView() {
        // 클릭 리스너 추가
        plantAdapter = PlantAdapter(plantList) { plant ->
            // plant.id가 null인지 체크
            if (plant.id != null) {
                val intent = Intent(this, PlantDetailActivity::class.java)
                intent.putExtra("plant_id", plant.id)
                startActivity(intent)
            } else {
                Log.e("PlantSearchActivity", "식물 ID가 null입니다: ${plant.name}")
            }
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlantSearchActivity)
            adapter = plantAdapter
        }
    }

    private fun setupSearchListener() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterPlants(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchPlantsFromFirestore() {
        db.collection("plants_search").get()
            .addOnSuccessListener { result ->
                plantList.clear()
                for (document in result) {
                    val plant = document.toObject(Plant::class.java)
                    plantList.add(plant)
                }

                // 가나다 순으로 정렬
                plantList.sortBy { it.name }

                plantAdapter.updateData(plantList)
                binding.emptyView.visibility = if (plantList.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                Log.e("PlantSearchActivity", "Firebase 데이터 로드 실패", e)
                binding.emptyView.visibility = View.VISIBLE
            }
    }

    private fun filterPlants(query: String) {
        val filteredList = plantList.filter { it.name.contains(query, ignoreCase = true) }

        // 검색 결과도 가나다 순으로 정렬
        val sortedFilteredList = filteredList.sortedBy { it.name }

        plantAdapter.updateData(sortedFilteredList)
        binding.emptyView.visibility = if (sortedFilteredList.isEmpty()) View.VISIBLE else View.GONE
    }
}
