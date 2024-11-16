package com.example.sigi

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.sigi.databinding.FragmentSearchBinding
import com.google.firebase.firestore.FirebaseFirestore

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val questions = listOf("잎 무늬", "잎 색", "꽃 색", "과일 색", "배치 장소", "관리 수준", "생장 속도")
    private val answers = mutableMapOf<String, String>() // 사용자의 답변 저장
    private var currentQuestionIndex = 0
    private var filteredPlants = mutableSetOf<String>() // 최종 필터링된 식물 목록

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 챗봇 초기 메시지
        addMessageToChat("안녕하세요! 무엇을 도와드릴까요?", isUser = false)

        binding.sendButton.setOnClickListener {
            val userMessage = binding.messageInput.text.toString()
            if (userMessage.isNotBlank()) {
                // 사용자 메시지 추가
                addMessageToChat(userMessage, isUser = true)
                binding.messageInput.text?.clear()

                // 사용자 입력 처리
                if (userMessage == "식물 찾기") {
                    startQuestionnaire()
                } else {
                    handleUserResponse(userMessage)
                }
            }
        }
    }

    private fun startQuestionnaire() {
        currentQuestionIndex = 0
        filteredPlants.clear() // 이전 결과 초기화
        askNextQuestion()
    }

    private fun handleUserResponse(response: String) {
        if (response.startsWith("예,")) {
            val feature = response.removePrefix("예,").trim()
            answers[questions[currentQuestionIndex - 1]] = feature
            askNextQuestion()
        } else if (response == "아니요") {
            askNextQuestion()
        } else {
            addMessageToChat("이해하지 못했습니다. '예, 특징' 또는 '아니요'로 대답해 주세요.", isUser = false)
        }
    }

    private fun askNextQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            addMessageToChat("${question}을 아시나요? '예, [특징]' 또는 '아니요'로 대답해 주세요.", isUser = false)
            currentQuestionIndex++
        } else {
            filterPlantsByAllFeatures()
        }
    }

    private fun filterPlantsByAllFeatures() {
        addMessageToChat("입력한 조건에 맞는 식물을 찾고 있습니다...", isUser = false)

        db.collection("plantsData")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val plantName = document.getString("식물명") ?: ""
                    var matchesAllFeatures = true

                    for ((featureName, featureValue) in answers) {
                        val featureData = document.getString(featureName) ?: ""
                        if (!featureData.contains(featureValue, ignoreCase = true)) {
                            matchesAllFeatures = false
                            break
                        }
                    }

                    if (matchesAllFeatures) {
                        filteredPlants.add(plantName)
                    }
                }

                displayFinalResults()
            }
            .addOnFailureListener { exception ->
                addMessageToChat("식물을 검색하는 도중 오류가 발생했습니다: ${exception.message}", isUser = false)
            }
    }

    private fun displayFinalResults() {
        if (filteredPlants.isNotEmpty()) {
            addMessageToChat("최종 조건에 맞는 식물:\n${filteredPlants.joinToString(", ")}", isUser = false)
        } else {
            addMessageToChat("조건에 맞는 식물이 없습니다.", isUser = false)
        }
    }

    private fun addMessageToChat(message: String, isUser: Boolean) {
        val messageView = TextView(requireContext()).apply {
            text = message
            textSize = 16f
            setPadding(24, 16, 24, 16)

            // 사용자와 챗봇 메시지에 따라 스타일 지정
            if (isUser) {
                // 사용자 메시지: 오른쪽 정렬, 녹색 배경
                setBackgroundColor(resources.getColor(android.R.color.holo_green_light, null))
                setTextColor(resources.getColor(android.R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                    gravity = Gravity.END
                }
            } else {
                // 챗봇 메시지: 왼쪽 정렬, 흰색 배경
                setBackgroundColor(resources.getColor(android.R.color.white, null))
                setTextColor(resources.getColor(android.R.color.black, null))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                    gravity = Gravity.START
                }
            }
        }

        // 메시지를 chatContainer에 추가
        binding.chatContainer.addView(messageView)

        // 스크롤뷰를 가장 아래로 이동
        binding.chatScrollView.post {
            binding.chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
