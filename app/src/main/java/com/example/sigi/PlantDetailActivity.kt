package com.example.sigi

import android.content.Intent
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

        // 뒤로가기 버튼 클릭 리스너 설정
        binding.backButton.setOnClickListener {
            finish()
        }

        // 전달받은 plantId
        val plantId = intent.getStringExtra("plant_id")
        if (plantId != null) {
            loadPlantDetails(plantId)
        }

        // "등록하기" 버튼 클릭 시 plantId를 전달하며 PlantRegisterActivity로 이동
        binding.registerPlantButton.setOnClickListener {
            val intent = Intent(this, PlantRegisterActivity::class.java)
            intent.putExtra("plant_id", plantId) // plantId 전달
            startActivity(intent)
        }
    }

    private fun loadPlantDetails(plantId: String) {
        db.collection("plants_search").document(plantId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val plant = document.toObject(Plant::class.java)
                    if (plant != null) {
                        binding.plantImageView.load(plant.imageUrl) {
                            placeholder(R.drawable.placeholder)
                            error(R.drawable.error)
                        }
                        binding.plantScientificName.text = getString(R.string.scientific_name, plant.scientificName)
                        binding.plantEnglishName.text = getString(R.string.english_name, plant.englishName)
                        binding.plantDistributionName.text = getString(R.string.distribution_name, plant.distributionName)
                        binding.plantFamilyName.text = getString(R.string.family_name, plant.familyName)
                        binding.plantOrigin.text = getString(R.string.origin, plant.origin)

                        // 상세 정보
                        binding.plantClassification.text = getString(R.string.classification, plant.classification)
                        binding.plantGrowthForm.text = getString(R.string.growth_form, plant.growthForm)
                        binding.plantIndoorComposition.text = getString(R.string.indoor_composition, plant.indoorComposition)
                        binding.plantEcology.text = getString(R.string.ecology, plant.ecology)
                        binding.plantLeafPattern.text = getString(R.string.leaf_pattern, plant.leafPattern)
                        binding.plantLeafColor.text = getString(R.string.leaf_color, plant.leafColor)
                        binding.plantBloomSeason.text = getString(R.string.bloom_season, plant.bloomSeason)
                        binding.plantFlowerColor.text = getString(R.string.flower_color, plant.flowerColor)
                        binding.plantFruitSeason.text = getString(R.string.fruit_season, plant.fruitSeason)
                        binding.plantFruitColor.text = getString(R.string.fruit_color, plant.fruitColor)
                        binding.plantGrowthHeight.text = getString(R.string.growth_height, plant.growthHeight)
                        binding.plantGrowthWidth.text = getString(R.string.growth_width, plant.growthWidth)
                        binding.plantLeafShape.text = getString(R.string.leaf_shape, plant.leafShape)
                        binding.plantScent.text = getString(R.string.scent, plant.scent)
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
