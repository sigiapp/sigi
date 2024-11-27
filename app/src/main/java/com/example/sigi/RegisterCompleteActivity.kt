package com.example.sigi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.sigi.databinding.ActivityRegisterCompleteBinding
import com.example.sigi.RegisteredHomeFragment

class RegisterCompleteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterCompleteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterCompleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 홈 화면으로 이동
        binding.goHomeButton.setOnClickListener {
            // 등록 완료 상태를 SharedPreferences에 저장
            val sharedPreferences = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("isRegistered", true) // 등록 상태 저장
            editor.apply()

            // FragmentTransaction으로 RegisteredHomeFragment 표시
            val fragment = RegisteredHomeFragment()
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, fragment) // ID 추가된 LinearLayout 사용
            transaction.commit()
        }
    }
}
