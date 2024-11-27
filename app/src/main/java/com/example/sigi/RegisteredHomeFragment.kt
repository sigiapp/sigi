package com.example.sigi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import coil.load
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisteredHomeFragment : Fragment() {

    private lateinit var tempValue: TextView
    private lateinit var humidityValue: TextView
    private lateinit var lightValue: TextView
    private lateinit var soilMessage: TextView
    private lateinit var nicknameText: TextView
    private lateinit var plantImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registered_home, container, false)

        // View 초기화
        tempValue = view.findViewById(R.id.tempValue)
        humidityValue = view.findViewById(R.id.humidityValue)
        lightValue = view.findViewById(R.id.lightValue)
        soilMessage = view.findViewById(R.id.soilMessage)
        nicknameText = view.findViewById(R.id.nicknameText)
        plantImage = view.findViewById(R.id.plantImage)

        // Firebase 초기화
        val database = Firebase.database
        val firestore = FirebaseFirestore.getInstance()

        // 현재 계절 계산
        val currentSeason = getCurrentSeason()

        // Firestore에서 등록된 식물 데이터 가져오기
        val plantDocumentId = "plant1"
        val registerPlantRef = firestore.collection("registerplant").document(plantDocumentId)

        registerPlantRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val nickname = document.getString("nickname") ?: "애칭 없음"
                val imageUrl = document.getString("imageUrl") ?: ""

                // UI 업데이트: 애칭 및 이미지
                nicknameText.text = nickname
                plantImage.load(imageUrl)

                // 리얼타임 데이터 가져오기
                val sensorRef = database.getReference("plant_data/latest")
                sensorRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val temp = snapshot.child("temperature").getValue(Double::class.java) ?: 0.0
                        val humidity = snapshot.child("humidity").getValue(Double::class.java) ?: 0.0
                        val light = snapshot.child("lightLevel").getValue(Double::class.java) ?: 0.0
                        val soilMoisture = snapshot.child("soilMoisture").getValue(Double::class.java) ?: 0.0

                        // UI 업데이트: 실시간 데이터
                        tempValue.text = "${temp}°C"
                        humidityValue.text = "${humidity}%"
                        lightValue.text = "${light}lx"

                        // 권장 물주기 정보 가져오기
                        val seasonKey = when (currentSeason) {
                            "봄" -> "spring"
                            "여름" -> "summer"
                            "가을" -> "fall"
                            "겨울" -> "winter"
                            else -> "spring"
                        }
                        val recommendedWatering = document.getString("권장 물주기 $seasonKey") ?: ""

                        // 권장 범위 설정
                        val range = getMoistureRange(recommendedWatering)

                        // 토양 습도 상태 판별
                        val status = when {
                            soilMoisture < range.first -> "흙이 건조합니다. 물을 주세요!"
                            soilMoisture > range.second -> "물이 과도합니다. 주의하세요!"
                            else -> "토양 상태가 적당합니다."
                        }

                        // UI 업데이트: 토양 상태 메시지
                        soilMessage.text = "현재 상태: $status"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        soilMessage.text = "Firebase Realtime Database에서 데이터를 가져오지 못했습니다."
                    }
                })
            } else {
                soilMessage.text = "Firestore에서 식물 데이터를 가져오지 못했습니다."
            }
        }.addOnFailureListener {
            soilMessage.text = "Firestore 요청 실패: 데이터를 가져올 수 없습니다."
        }

        return view
    }

    // 현재 계절 판별
    private fun getCurrentSeason(): String {
        val currentMonth = System.currentTimeMillis() / (1000 * 60 * 60 * 24 * 30) % 12
        return when (currentMonth.toInt()) {
            2, 3, 4 -> "봄" // 3월, 4월, 5월
            5, 6, 7 -> "여름" // 6월, 7월, 8월
            8, 9, 10 -> "가을" // 9월, 10월, 11월
            11, 0, 1 -> "겨울" // 12월, 1월, 2월
            else -> "봄" // 기본값
        }
    }

    // 권장 물주기 텍스트를 퍼센트 범위로 변환
    private fun getMoistureRange(wateringGuide: String): Pair<Double, Double> {
        return when (wateringGuide) {
            "흙을 촉촉하게 유지함(물에 잠기지 않도록 주의)" -> Pair(40.0, 60.0)
            "토양 표면이 말랐을 때 충분히 관수함" -> Pair(20.0, 40.0)
            "화분 흙 대부분 말랐을 때 충분히 관수함" -> Pair(0.0, 20.0)
            else -> Pair(0.0, 100.0) // 기본값: 전체 범위
        }
    }
}
