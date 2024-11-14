package com.example.sigi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CalendarFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var calendarView: CalendarView
    private lateinit var selectedNoteTextView: TextView
    private var selectedDate: String = ""  // 선택된 날짜 저장

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_calendar, container, false)
        calendarView = view.findViewById(R.id.calendar_view)
        selectedNoteTextView = view.findViewById(R.id.tv_selected_note)

        db = FirebaseFirestore.getInstance()

        // 날짜 선택 리스너 설정
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = "$year-${month + 1}-$dayOfMonth"
            loadDiaryData(selectedDate)
        }

        // TextView 클릭 시 DiaryWriteActivity로 이동하여 일기 작성
        selectedNoteTextView.setOnClickListener {
            if (selectedDate.isNotEmpty()) {  // 선택된 날짜가 있는 경우만 이동
                val intent = Intent(activity, DiaryWriteActivity::class.java)
                intent.putExtra("selectedDate", selectedDate)
                startActivity(intent)
            }
        }

        return view
    }

    private fun loadDiaryData(date: String) {
        db.collection("diary").document(date).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val note = document.getString("content")
                    selectedNoteTextView.text = note ?: "일기 내용이 없습니다."
                } else {
                    selectedNoteTextView.text = "일기 내용이 없습니다."
                }
            }
            .addOnFailureListener {
                selectedNoteTextView.text = "데이터를 불러오는 데 실패했습니다."
            }
    }
}
