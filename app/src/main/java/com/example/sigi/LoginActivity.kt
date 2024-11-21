package com.example.sigi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var registerButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView)
        registerButton = findViewById(R.id.registerButton)

        // 로그인 버튼 클릭 이벤트
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseAuth", "로그인 성공")
                            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()

                            val currentUser = auth.currentUser
                            if (currentUser != null) {
                                // Firestore에서 사용자 이름 가져오기
                                db.collection("names")
                                    .document(currentUser.uid)
                                    .get()
                                    .addOnSuccessListener { document ->
                                        if (!document.exists()) {
                                            // Firestore에 기본 이름 생성
                                            val nameData = hashMapOf("name" to "새 사용자")
                                            db.collection("names").document(currentUser.uid)
                                                .set(nameData)
                                                .addOnSuccessListener {
                                                    Log.d("Firestore", "기본 이름 설정 완료")
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e("Firestore", "기본 이름 설정 실패", e)
                                                }
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Firestore", "Firestore 이름 가져오기 실패", e)
                                    }
                            }

                            // MainActivity로 이동
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Log.e("FirebaseAuth", "로그인 실패", task.exception)
                            Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 비밀번호 찾기 클릭 이벤트
        forgotPasswordTextView.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isNotEmpty()) {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "비밀번호 재설정 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "이메일 전송 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 클릭 이벤트
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
