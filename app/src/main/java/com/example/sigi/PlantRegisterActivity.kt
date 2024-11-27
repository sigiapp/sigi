package com.example.sigi

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

class PlantRegisterActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var userPlantImageView: ImageView
    private lateinit var nicknameEditText: EditText
    private lateinit var registerPlantButton: Button

    // Firestore 및 데이터 변수
    private val db = FirebaseFirestore.getInstance()
    private var selectedPotType: String = ""
    private var selectedPotSize: String = ""
    private var selectedSunlight: String = ""
    private var selectedImageUrl: String = ""
    private var selectedPotTypeButton: MaterialButton? = null
    private var selectedPotSizeButton: MaterialButton? = null
    private var selectedSunlightButton: MaterialButton? = null
    private var plantId: String? = null // 전달받은 plantId를 저장할 변수

    companion object {
        private const val REQUEST_CAMERA = 101
        private const val REQUEST_GALLERY = 102
        private const val IMGUR_CLIENT_ID = "09cb24db6cccbf7"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_register)

        // XML 뷰 초기화
        backButton = findViewById(R.id.backButton)
        userPlantImageView = findViewById(R.id.userPlantImageView)
        nicknameEditText = findViewById(R.id.nicknameEditText)
        registerPlantButton = findViewById(R.id.registerPlantButton)

        // 뒤로가기 버튼
        backButton.setOnClickListener { finish() }

        // 전달받은 plantId
        plantId = intent.getStringExtra("plant_id")

        // 이미지 클릭 시 업로드 처리
        userPlantImageView.setOnClickListener {
            showImageUploadDialog()
        }

        // 등록하기 버튼 클릭 리스너 설정
        registerPlantButton.setOnClickListener { registerPlant() }

        // 선택 옵션 버튼 초기화 및 클릭 리스너 설정
        setupOptionButtons()
    }

    private fun setupOptionButtons() {
        val potTypeGood = findViewById<MaterialButton>(R.id.potTypeGood)
        val potTypeNormal = findViewById<MaterialButton>(R.id.potTypeNormal)
        val potTypeBad = findViewById<MaterialButton>(R.id.potTypeBad)

        val potSizeLarge = findViewById<MaterialButton>(R.id.potSizeLarge)
        val potSizeNormal = findViewById<MaterialButton>(R.id.potSizeNormal)
        val potSizeSmall = findViewById<MaterialButton>(R.id.potSizeSmall)

        val sunlightHigh = findViewById<MaterialButton>(R.id.sunlightHigh)
        val sunlightMedium = findViewById<MaterialButton>(R.id.sunlightMedium)
        val sunlightLow = findViewById<MaterialButton>(R.id.sunlightLow)

        // 화분 종류 버튼 클릭 리스너
        potTypeGood.setOnClickListener { selectButton(potTypeGood, "좋음", ButtonType.POT_TYPE) }
        potTypeNormal.setOnClickListener { selectButton(potTypeNormal, "보통", ButtonType.POT_TYPE) }
        potTypeBad.setOnClickListener { selectButton(potTypeBad, "나쁨", ButtonType.POT_TYPE) }

        // 화분 크기 버튼 클릭 리스너
        potSizeLarge.setOnClickListener { selectButton(potSizeLarge, "큼", ButtonType.POT_SIZE) }
        potSizeNormal.setOnClickListener { selectButton(potSizeNormal, "보통", ButtonType.POT_SIZE) }
        potSizeSmall.setOnClickListener { selectButton(potSizeSmall, "작음", ButtonType.POT_SIZE) }

        // 햇빛 조건 버튼 클릭 리스너
        sunlightHigh.setOnClickListener { selectButton(sunlightHigh, "많음", ButtonType.SUNLIGHT) }
        sunlightMedium.setOnClickListener { selectButton(sunlightMedium, "적당", ButtonType.SUNLIGHT) }
        sunlightLow.setOnClickListener { selectButton(sunlightLow, "적음", ButtonType.SUNLIGHT) }
    }

    private fun selectButton(button: MaterialButton, value: String, type: ButtonType) {
        val previousButton = when (type) {
            ButtonType.POT_TYPE -> selectedPotTypeButton
            ButtonType.POT_SIZE -> selectedPotSizeButton
            ButtonType.SUNLIGHT -> selectedSunlightButton
        }

        // 이전 버튼 스타일 초기화
        previousButton?.let {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.default_button_color))
            it.setTextColor(ContextCompat.getColor(this, R.color.default_text_color))
        }

        // 선택된 버튼 스타일 변경
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.selected_button_color))
        button.setTextColor(ContextCompat.getColor(this, R.color.selected_text_color))

        // 선택 상태 업데이트
        when (type) {
            ButtonType.POT_TYPE -> {
                selectedPotType = value
                selectedPotTypeButton = button
            }
            ButtonType.POT_SIZE -> {
                selectedPotSize = value
                selectedPotSizeButton = button
            }
            ButtonType.SUNLIGHT -> {
                selectedSunlight = value
                selectedSunlightButton = button
            }
        }
    }


    private fun showImageUploadDialog() {
        val options = arrayOf("카메라로 찍기", "갤러리에서 선택")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("이미지 업로드")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA)
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_GALLERY)
    }

    private fun uploadImageToImgur(image: Any, onSuccess: (String) -> Unit) {
        val url = "https://api.imgur.com/3/image"
        val mediaType = "image/*".toMediaTypeOrNull()
        val requestBody: RequestBody

        when (image) {
            is Bitmap -> {
                val baos = ByteArrayOutputStream()
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()
                requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "image.jpg", RequestBody.create(mediaType, data))
                    .build()
            }
            is Uri -> {
                val inputStream = contentResolver.openInputStream(image) ?: return
                val byteArray = inputStream.readBytes()
                requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "image.jpg", RequestBody.create(mediaType, byteArray))
                    .build()
            }
            else -> return
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Client-ID $IMGUR_CLIENT_ID")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Snackbar.make(registerPlantButton, "이미지 업로드 실패", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonResponse = JSONObject(responseBody ?: "")
                    val imageUrl = jsonResponse.getJSONObject("data").getString("link")

                    runOnUiThread {
                        onSuccess(imageUrl)
                    }
                } else {
                    runOnUiThread {
                        Snackbar.make(registerPlantButton, "Imgur 업로드 실패", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAMERA -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    if (photo != null) {
                        userPlantImageView.setImageBitmap(photo)

                        // Imgur에 업로드
                        uploadImageToImgur(photo) { imageUrl ->
                            selectedImageUrl = imageUrl
                        }
                    }
                }
                REQUEST_GALLERY -> {
                    val selectedImage = data?.data
                    if (selectedImage != null) {
                        userPlantImageView.load(selectedImage)

                        // Imgur에 업로드
                        uploadImageToImgur(selectedImage) { imageUrl ->
                            selectedImageUrl = imageUrl
                        }
                    }
                }
            }
        }
    }

    private fun registerPlant() {
        val nickname = nicknameEditText.text.toString()

        // 애칭만 입력되었는지 확인
        if (nickname.isBlank()) {
            Snackbar.make(registerPlantButton, "애칭을 입력해주세요.", Snackbar.LENGTH_SHORT).show()
            return
        }

        // Firestore 컬렉션 참조
        val collectionRef = db.collection("registerplant")

        // Firestore에 저장될 데이터
        val plantData = hashMapOf(
            "nickname" to nickname,
            "potType" to selectedPotType,
            "potSize" to selectedPotSize,
            "sunlight" to selectedSunlight,
            "imageUrl" to selectedImageUrl,
            "plantId" to plantId
        )

        // 마지막으로 저장된 ID를 조회
        collectionRef.get().addOnSuccessListener { querySnapshot ->
            val newId = if (querySnapshot.isEmpty) {
                "plant1"
            } else {
                val lastId = querySnapshot.documents.maxOfOrNull {
                    it.id.removePrefix("plant").toIntOrNull() ?: 0
                } ?: 0
                "plant${lastId + 1}"
            }

            // ID를 이용해 문서를 저장
            collectionRef.document(newId).set(plantData)
                .addOnSuccessListener {
                    // 성공 메시지와 완료 화면 이동
                    Snackbar.make(registerPlantButton, "식물 등록 완료!", Snackbar.LENGTH_SHORT).show()
                    navigateToRegisterComplete() // 완료 화면으로 이동
                }
                .addOnFailureListener {
                    Snackbar.make(registerPlantButton, "식물 등록 실패.", Snackbar.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Snackbar.make(registerPlantButton, "ID 생성 실패.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun navigateToRegisterComplete() {
        val intent = Intent(this, RegisterCompleteActivity::class.java)
        startActivity(intent)
        finish() // 현재 Activity 종료
    }
    private enum class ButtonType {
        POT_TYPE, POT_SIZE, SUNLIGHT
    }
}
