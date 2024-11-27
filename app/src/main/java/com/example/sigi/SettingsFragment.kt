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
        val currentPasswordForEmail = view.findViewById<EditText>(R.id.currentPasswordForEmail)
        val newEmailEditText = view.findViewById<EditText>(R.id.newEmailEditText)
        val newPasswordForEmail = view.findViewById<EditText>(R.id.newPasswordForEmail)
        val changeNameButton = view.findViewById<Button>(R.id.changeNameButton)
        val changeEmailButton = view.findViewById<Button>(R.id.changeEmailButton)
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

        // 이메일 및 비밀번호 변경
        changeEmailButton.setOnClickListener {
            val currentPassword = currentPasswordForEmail.text.toString()
            val newEmail = newEmailEditText.text.toString()
            val newPassword = newPasswordForEmail.text.toString()

            if (currentPassword.isNotEmpty() && newEmail.isNotEmpty() && newPassword.isNotEmpty() && currentUser != null) {
                // 현재 비밀번호를 사용해 재인증
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentPassword)
                currentUser.reauthenticate(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 이메일 변경
                            currentUser.updateEmail(newEmail)
                                .addOnCompleteListener { emailTask ->
                                    if (emailTask.isSuccessful) {
                                        // 이메일 확인 링크 전송
                                        currentUser.sendEmailVerification()
                                            .addOnCompleteListener { verifyTask ->
                                                if (verifyTask.isSuccessful) {
                                                    Toast.makeText(context, "새 이메일로 확인 링크를 보냈습니다.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "이메일 확인 링크 전송 실패: ${verifyTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }

                                        // 비밀번호 변경
                                        currentUser.updatePassword(newPassword)
                                            .addOnCompleteListener { passwordTask ->
                                                if (passwordTask.isSuccessful) {
                                                    Toast.makeText(context, "이메일 및 비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "비밀번호 변경 실패: ${passwordTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    } else {
                                        Toast.makeText(context, "이메일 변경 실패: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(context, "재인증 실패: 현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(context, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
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