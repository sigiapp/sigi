package com.example.sigi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class DiaryWriteActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var etDiaryContent: EditText
    private lateinit var imagePreview: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var captureImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>
    private val imgurClientId = "2f672bc07a0d026" // Replace with your actual Imgur Client ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_diary)

        db = FirebaseFirestore.getInstance()

        val selectedDate = intent.getStringExtra("selectedDate")
        val tvDate = findViewById<TextView>(R.id.tv_date)
        tvDate.text = selectedDate

        etDiaryContent = findViewById(R.id.et_diary_content)
        imagePreview = findViewById(R.id.image_preview)

        loadExistingDiaryContent(selectedDate ?: "")

        galleryImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                imagePreview.load(selectedImageUri)
            }
        }

        findViewById<Button>(R.id.btn_upload_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryImageLauncher.launch(intent)
        }

        captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                if (imageBitmap != null) {
                    imagePreview.setImageBitmap(imageBitmap)
                    uploadImageToImgur(imageBitmap, selectedDate ?: "")
                }
            }
        }

        findViewById<Button>(R.id.btn_camera).setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            captureImageLauncher.launch(takePictureIntent)
        }

        findViewById<Button>(R.id.btn_save_diary).setOnClickListener {
            val diaryContent = etDiaryContent.text.toString().trim()
            if (diaryContent.isNotEmpty()) {
                saveDiaryToFirestore(selectedDate ?: "", diaryContent)
            }
        }

        findViewById<Button>(R.id.btn_back_to_calendar).setOnClickListener {
            finish()
        }
    }

    private fun loadExistingDiaryContent(date: String) {
        db.collection("plantDiary").document(date).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val existingContent = document.getString("content") ?: ""
                    etDiaryContent.setText(existingContent)
                    val imageUrl = document.getString("imageUri")
                    if (imageUrl != null) {
                        imagePreview.load(imageUrl)
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun uploadImageToImgur(bitmap: Bitmap, date: String) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val imageBytes = byteArrayOutputStream.toByteArray()
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaType())
        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)

        val client = OkHttpClient.Builder().apply {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            addInterceptor(logging)
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgur.com/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val imgurService = retrofit.create(ImgurApiService::class.java)
        val call = imgurService.uploadImage("Client-ID $imgurClientId", body)

        call.enqueue(object : retrofit2.Callback<ImgurResponse> {
            override fun onResponse(call: retrofit2.Call<ImgurResponse>, response: retrofit2.Response<ImgurResponse>) {
                if (response.isSuccessful) {
                    val imageUrl = response.body()?.data?.link
                    Log.d("ImgurUpload", "Image URL: $imageUrl") // 로그 추가
                    if (imageUrl != null) {
                        saveImageUriToFirestore(imageUrl, date)
                    }
                } else {
                    Log.e("ImgurUpload", "Upload failed: ${response.message()}")
                }
            }

            override fun onFailure(call: retrofit2.Call<ImgurResponse>, t: Throwable) {
                Log.e("ImgurUpload", "Error: ${t.message}")
            }
        })
    }

    private fun saveImageUriToFirestore(imageUrl: String, date: String) {
        val diaryData = mapOf(
            "imageUri" to imageUrl
        )

        db.collection("diary").document(date).set(diaryData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Firestore", "Image URL saved successfully")
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    private fun saveDiaryToFirestore(date: String, newContent: String) {
        db.collection("diary").document(date).get()
            .addOnSuccessListener { document ->
                val existingContent = document.getString("content") ?: ""
                val updatedContent = "$existingContent\n$newContent"
                val diaryData = mapOf(
                    "content" to updatedContent
                )

                db.collection("diary").document(date)
                    .set(diaryData, SetOptions.merge())
                    .addOnSuccessListener {
                        finish()
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
