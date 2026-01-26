package com.example.camera // 패키지 이름 확인

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.UIDesign.R // R.string을 사용하기 위해 import
import com.example.UIDesign.databinding.ActivityCameraBinding // 바인딩 경로 확인
import com.example.db.GarbageDatabaseHelper
import com.example.db.ProfitReportActivity // '성과 리포트' Activity import
import com.example.db.StatisticsActivity
import com.example.flask.ApiService
import com.example.flask.Detection // YOLO 결과 클래스
import com.example.flask.PredictionResponse // YOLO 응답 클래스
import com.example.flask.RetrofitClient
import com.example.flask.ResultDrawer // ResultDrawer 경로 확인
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay // ▼▼▼ 'delay' 함수 import 추가 ▼▼▼
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

// UI 상태 정의 (Loading 상태 포함)
private sealed class UiState {
    object CameraPreview : UiState()
    object Loading : UiState()
    object Confirmation : UiState()
}

// 서버 응답 리스너 (YOLO 결과용)
interface ServerAnalysisListener {
    fun onServerAnalyzed(detections: List<Detection>?)
    fun onServerError(message: String)
}

class CameraActivity : AppCompatActivity(), CameraHandlerListener, ServerAnalysisListener {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraHandler: CameraHandler
    private lateinit var uiManager: CameraUiManager
    private lateinit var dbHelper: GarbageDatabaseHelper
    private lateinit var apiService: ApiService

    private var lastPhotoUri: Uri? = null
    private var latestAnalysisResult: String? = null
    private var lastResultBitmap: Bitmap? = null
    private var lastDetections: List<Detection>? = null // [상세] 버튼 클릭 시 사용하기 위해 탐지 결과 저장

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 전문가들을 고용합니다.
        uiManager = CameraUiManager(this, binding)
        dbHelper = GarbageDatabaseHelper(this)
        apiService = RetrofitClient.instance
        cameraHandler = CameraHandler(this, this, binding.viewFinder, this)

        setupButtonListeners()
        uiManager.showInitialState()

        // 권한 확인 없이 바로 카메라 시작 (MainActivity가 권한을 처리했다고 가정)
        cameraHandler.startCamera()
        setupOnBackPressed()
    }

    override fun onResume() {
        super.onResume()
        cameraHandler.startCamera() // onStop에서 껐을 경우 다시 켜줍니다.
    }

    override fun onStop() {
        super.onStop()
        cameraHandler.shutdownCamera()
    }

    private fun setupButtonListeners() {
        binding.btnCapture.setOnClickListener {
            // '단어장'에서 로딩 메시지 가져오기
            uiManager.showLoadingState(getString(R.string.loading_analyzing))
            cameraHandler.takePhoto()
        }
        binding.btnReshoot.setOnClickListener { uiManager.showInitialState() }
        binding.btnDelete.setOnClickListener {
            lastPhotoUri?.let { uri ->
                GalleryHandler.deletePhoto(this, uri)
                Toast.makeText(this, getString(R.string.toast_photo_deleted), Toast.LENGTH_SHORT).show()
            }
            uiManager.showInitialState()
        }

        binding.btnSave.setOnClickListener {
            latestAnalysisResult?.let { result ->
                dbHelper.addEntry(result)
                Toast.makeText(this, getString(R.string.toast_db_saved, result), Toast.LENGTH_SHORT).show()
            } ?: run {
                Toast.makeText(this, getString(R.string.toast_no_result_to_save), Toast.LENGTH_SHORT).show()
            }
            uiManager.showInitialState()
        }

        // ▼▼▼ [상세 보기] 버튼 로직 수정 (효율화) ▼▼▼
        binding.btnDetails.setOnClickListener {
            // 1순위 결과와 '그려진 Bitmap'이 모두 준비되었는지 확인
            if (lastResultBitmap != null && latestAnalysisResult != null) {

                lifecycleScope.launch {
                    uiManager.showLoadingState(getString(R.string.loadingTextView)) // 로딩 메시지

                    // 2. 비트맵을 다시 그리지 않고, '저장된 비트맵'을 바로 디스크에 저장 (IO 스레드)
                    val savedUri = withContext(Dispatchers.IO) {
                        saveBitmapToDiskAndGetUri(lastResultBitmap!!)
                    }

                    // 3. Main 스레드에서 ResultActivity 시작
                    runOnUiThread {
                        if (savedUri != null) {
                            val intent = Intent(this@CameraActivity, RecyclingResultActivity::class.java).apply {
                                putExtra(RecyclingResultActivity.EXTRA_URI, savedUri)
                                putExtra(RecyclingResultActivity.EXTRA_CLASS_NAME, latestAnalysisResult)
                            }
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@CameraActivity, getString(R.string.error_file_conversion_failed), Toast.LENGTH_SHORT).show()
                        }
                        // 확인 화면으로 다시 복귀 (로딩 해제)
                        uiManager.showConfirmationState(lastPhotoUri, binding.tvDetectionResult.text.toString(), lastResultBitmap)
                    }
                }
            } else {
                Toast.makeText(this, getString(R.string.error_no_analysis_data), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPhotoSaved(uri: Uri) {
        lastPhotoUri = uri
        uploadImageToServer(uri)
    }

    private fun uploadImageToServer(imageUri: Uri) {
        lifecycleScope.launch {
            var tempFile: File? = null
            try {
                tempFile = withContext(Dispatchers.IO) { uriToFile(imageUri) }
                if (tempFile == null) {
                    runOnUiThread { onServerError(getString(R.string.error_file_conversion_failed)) }
                    return@launch
                }

                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                val response = withContext(Dispatchers.IO) { apiService.uploadImage(body) }

                runOnUiThread { // 결과 처리 및 UI 업데이트
                    if (response.isSuccessful) {
                        val prediction = response.body()
                        if (prediction?.detections != null) {
                            if (!isFinishing && !isDestroyed) {
                                onServerAnalyzed(prediction.detections)
                            }
                        } else if (prediction?.error != null) {
                            onServerError(getString(R.string.error_server_analysis, prediction.error))
                        } else {
                            onServerError(getString(R.string.error_unknown_response))
                        }
                    } else {
                        onServerError(getString(R.string.error_server_communication, response.code(), response.message()))
                    }
                }

            } catch (e: Exception) {
                Log.e("CameraActivity", "Upload Error", e)
                runOnUiThread { onServerError(getString(R.string.error_upload_failed, e.message ?: "Unknown error")) }
            } finally {
                withContext(Dispatchers.IO) { tempFile?.delete() }
            }
        }
    }

    // ▼▼▼ "Soju" 3초 지연 로직 + "1순위 텍스트" 로직으로 수정됨 ▼▼▼
    override fun onServerAnalyzed(detections: List<Detection>?) {
        if (isFinishing || isDestroyed) return

        // 1. 1순위 결과 확인
        val topResult = detections?.firstOrNull()?.className ?: "Unknown"
        latestAnalysisResult = topResult // (저장/상세보기 버튼 클릭 시 사용하기 위해 저장)
        lastDetections = detections // [상세] 버튼 클릭 시 사용하기 위해 탐지 결과 저장

        // ▼▼▼ 2. 텍스트 가공 (1순위 결과만, 퍼센트 없이) ▼▼▼
        val resultText = if (!detections.isNullOrEmpty()) {
            getString(R.string.analysis_result_title) + "\n- " + topResult
        } else {
            getString(R.string.analysis_no_result_found)
        }


        // 3. 이미지에 사각형 그리기 (IO 스레드) & UI 처리 (Main 스레드)
        lifecycleScope.launch {
            val resultBitmap = withContext(Dispatchers.IO) {
                uriToBitmap(lastPhotoUri)?.let { originalBitmap ->
                    ResultDrawer.drawDetections(originalBitmap, detections ?: emptyList())
                }
            }

            // 4. '그려진 Bitmap'을 클래스 변수에 저장
            lastResultBitmap = resultBitmap

            // 5. [중요] 'Soju'든 아니든, '확인 화면'을 먼저 띄웁니다!
            //    (이때, 방금 만든 '1순위 텍스트'를 사용)
            uiManager.showConfirmationState(lastPhotoUri, resultText, resultBitmap)

            // 6. "Soju"인 경우 3초 후 자동 이동
            if (topResult.equals("Soju", ignoreCase = true)) { // "soju", "Soju" 등 모두 처리

                // 6a. 3초간 대기 (이 시간 동안 사용자는 라벨링된 'Soju' 사진을 봅니다)
                delay(3000) // 3000 milliseconds = 3 seconds
                if (isFinishing || isDestroyed) return@launch

                // 6c. 자동 저장
                dbHelper.addEntry(topResult)
                Toast.makeText(this@CameraActivity, getString(R.string.toast_db_saved_auto, topResult), Toast.LENGTH_SHORT).show()

                // 6d. '성과 리포트' 화면으로 바로 이동
                val intent = Intent(this@CameraActivity, ProfitReportActivity::class.java)
                startActivity(intent)

                // 6e. 카메라 화면은 다시 촬영할 수 있도록 초기화
                uiManager.showInitialState()

            } else {
                // 7. "Soju"가 아니면(Paper, Poly bag 등),
                //    그냥 '확인 화면'에 머무르며 사용자의 버튼 입력을 기다립니다.
            }
        }
    }
    // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

    override fun onServerError(message: String) {
        if (isFinishing || isDestroyed) return
        Toast.makeText(this, message, Toast.LENGTH_LONG).show() // 오류 메시지는 동적이므로 그대로 표시
        uiManager.showInitialState() // 오류 시 초기화
    }

    override fun onError(message: String) {
        if (!isFinishing || isDestroyed) return
        Toast.makeText(this, message, Toast.LENGTH_LONG).show() // 오류 메시지는 동적이므로 그대로 표시
        uiManager.showInitialState()
    }

    private fun setupOnBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // XML에 loadingTextView가 있으므로 loadingTextView로 확인
                if (binding.confirmationLayout.visibility == View.VISIBLE || binding.loadingTextView.visibility == View.VISIBLE) {
                    uiManager.showInitialState() // 확인 또는 로딩 중이면 초기 상태로
                } else {
                    finish() // 카메라 미리보기 상태면 액티비티 종료
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    // ▼▼▼ 3. Bitmap을 디스크에 저장하고 Uri를 반환하는 함수 추가 ▼▼▼
    private suspend fun saveBitmapToDiskAndGetUri(bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val filename = "labeled_image_${System.currentTimeMillis()}.jpg"
        val file = File(cacheDir, filename)

        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            // Bitmap을 JPEG 형식으로 디스크에 기록 (품질 95)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            fos.flush()

            // 파일 경로를 Uri로 변환하여 반환
            return@withContext Uri.fromFile(file)
        } catch (e: Exception) {
            Log.e("CameraActivity", "Error saving Bitmap to disk", e)
            return@withContext null
        } finally {
            fos?.close()
        }
    }

    private suspend fun uriToFile(uri: Uri): File? = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = contentResolver.openInputStream(uri) ?: return@withContext null
            val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
            outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            return@withContext file
        } catch (e: Exception) {
            Log.e("CameraActivity", "Uri to File Error", e)
            return@withContext null
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    private fun uriToBitmap(uri: Uri?): Bitmap? {
        if (uri == null) return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                android.graphics.ImageDecoder.decodeBitmap(android.graphics.ImageDecoder.createSource(contentResolver, uri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            Log.e("CameraActivity", "Bitmap conversion failed", e)
            null
        }
    }
}

