package com.example.flask

// 패키지 이름 확인

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.example.flask.ApiService

object RetrofitClient {
    // ▼▼▼ 중요! Flask 서버 IP 주소와 포트로 변경하세요 ▼▼▼
    // 예: 에뮬레이터 -> PC 로컬: "http://10.0.2.2:5000/"
    // 예: 실제 폰 -> PC 로컬: "http://192.168.0.10:5000/" (PC의 실제 IP)
    private const val BASE_URL ="https://jurisprudentially-postvarioloid-linn.ngrok-free.dev" // 끝에 '/' 필수!

    // 통신 시간 제한 설정
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // 연결 시도 시간 (30초)
        .readTimeout(30, TimeUnit.SECONDS)    // 응답 대기 시간 (30초)
        .writeTimeout(30, TimeUnit.SECONDS)   // 전송 시간 (30초)
        .build()

    // Retrofit 인스턴스를 만드는 부분 (지연 초기화 사용)
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)                 // 서버 기본 주소 설정
            .client(okHttpClient)              // 시간 제한 등 설정 적용
            .addConverterFactory(GsonConverterFactory.create()) // JSON 변환기 설정
            .build()
        retrofit.create(ApiService::class.java) // ApiService 인터페이스 구현체 생성
    }
}

