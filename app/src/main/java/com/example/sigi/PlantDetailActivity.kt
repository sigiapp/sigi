package com.example.sigi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.sigi.databinding.ActivityPlantDetailBinding
import com.google.firebase.firestore.FirebaseFirestore

class PlantDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlantDetailBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlantDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 인텐트에서 plant_name 받아오기
        val plantName = intent.getStringExtra("plant_name")
        if (plantName != null) {
            loadPlantDetailsByName(plantName)
        }

        // 뒤로가기 버튼 클릭 리스너 설정 (필요 시 추가)
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadPlantDetailsByName(plantName: String) {
        db.collection("plants_search")
            .whereEqualTo("name", plantName)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val plant = documents.documents[0].toObject(Plant::class.java)
                    if (plant != null) {
                        binding.plantName.text = plant.name
                        binding.plantEnglishName.text = plant.englishName
                        binding.plantImageView.load(plant.imageUrl) {
                            placeholder(R.drawable.placeholder)
                            error(R.drawable.error)
                        }
                    }
                } else {
                    // 문서를 찾을 수 없는 경우에 대한 처리 (예: "데이터가 없습니다" 메시지 표시)
                    binding.plantName.text = "데이터가 없습니다"
                    binding.plantEnglishName.text = ""
                    binding.plantImageView.setImageResource(R.drawable.error)
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}

