package com.example.sigi

// RegisterActivity.kt
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        registerButton = findViewById(R.id.registerButton)
        backButton = findViewById(R.id.backButton) // 뒤로 가기 버튼 ImageView 추가

        // 뒤로 가기 버튼 클릭 이벤트
        backButton.setOnClickListener {
            // LoginActivity로 이동
            finish() // 현재 액티비티를 종료하여 이전 화면(LoginActivity)으로 돌아감
        }

        // 회원가입 버튼 클릭 이벤트
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Firebase Authentication을 통해 계정 생성
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseAuth", "회원가입 성공")
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                            finish() // 회원가입 후 로그인 화면으로 돌아가기
                        } else {
                            Log.e("FirebaseAuth", "회원가입 실패", task.exception)
                            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
