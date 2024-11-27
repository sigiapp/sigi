package com.example.sigi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentEmailEditText = view.findViewById<EditText>(R.id.currentEmailEditText)
        val currentPasswordEditText = view.findViewById<EditText>(R.id.currentPasswordEditText)
        val nameEditText = view.findViewById<EditText>(R.id.nameEditText)
        val changeNameButton = view.findViewById<Button>(R.id.changeNameButton)
        val logoutButton = view.findViewById<Button>(R.id.logoutButton) // 로그아웃 버튼 추가

        val currentUser = auth.currentUser

        if (currentUser != null) {
            // 현재 이메일 표시
            currentEmailEditText.setText(currentUser.email)

            // 현재 비밀번호는 보안상 표시 불가능. 대신 "*"으로 고정
            currentPasswordEditText.setText("********")

            // Firestore에서 사용자 이름 가져오기
            db.collection("names").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name")
                        nameEditText.setText(name)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SettingsFragment", "Firestore 이름 가져오기 실패", e)
                }
        }

        // 이름 변경
        changeNameButton.setOnClickListener {
            val name = nameEditText.text.toString()
            if (name.isNotEmpty() && currentUser != null) {
                val nameData = hashMapOf("name" to name)
                db.collection("names").document(currentUser.uid)
                    .set(nameData)
                    .addOnSuccessListener {
                        Toast.makeText(context, "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SettingsFragment", "이름 변경 실패", e)
                        Toast.makeText(context, "이름 변경 실패", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }



        // 로그아웃 버튼 클릭 이벤트 처리
        logoutButton.setOnClickListener {
            auth.signOut() // Firebase 로그아웃 처리
            Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

            // 로그인 화면으로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}