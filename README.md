# ♻️ Trash Cam (트래쉬 캠)

**외국인 거주자를 위한 AI 기반 다국어 분리수거 안내 시스템**

한국의 복잡한 분리수거 체계와 언어 장벽으로 어려움을 겪는 국내 거주 외국인들을 돕기 위해 개발된 서비스입니다.
카메라로 쓰레기를 비추기만 하면 AI가 즉각적으로 분석하여 올바른 분리배출 방법을 **4개국어**로 안내합니다.

---

## 🚀 Key Features (주요 기능)

### 1. AI 실시간 객체 인식 및 시각화

* **YOLO 모델 분석**
  촬영된 이미지를 Flask 서버로 전송하여 실시간으로 객체(페트병, 비닐, 소주병 등)를 탐지합니다.

* **시각적 피드백**
  분석 결과를 바탕으로 이미지 위에 **Bounding Box(탐지 영역)** 와 라벨을 그려 즉각적인 확인이 가능합니다.

---

### 2. 4개국어 맞춤형 가이드

* **다국어 지원**
  한국어, 영어, 중국어, 일본어 중 사용자가 선택한 언어로 상세 배출 지침을 제공합니다.

* **추가 정보 연계**
  분석된 항목에 맞는 재활용 팁 YouTube 동영상을 바로 확인할 수 있습니다.

---

### 3. 동기부여 및 편의 시스템

* **공병 성과 리포트**
  ‘소주병’ 등 보상이 명확한 항목은 자동 기록되어 스탬프 보드 및 예상 수익을 시각화합니다.

* **분리수거 통계**
  사용자가 배출한 쓰레기 데이터를 DB에 저장하고 막대그래프로 통계를 제공합니다.

* **알람 서비스**
  설정한 요일과 시간에 맞춰 분리수거 날짜를 잊지 않도록 푸시 알림을 전송합니다.

---

## 🛠 Tech Stack (기술 스택)

### 📱 Client (Android)

* **Language**: Kotlin
* **Camera**: CameraX API
* **Network**: Retrofit2 (Server Communication)
* **Database**: SQLite (Local History & Statistics)
* **Chart**: MPAndroidChart (Visualization)

---

### 🖥 Server & AI

* **Framework**: Python Flask
* **AI Model**: YOLOv11n (Nano Version for Speed)
* **Deep Learning**: PyTorch, Ultralytics
* **Deployment**: ngrok (Development Tunnel)

---

## 🏗 Architecture (시스템 구성도)

1. **Request**
   안드로이드 앱에서 촬영 이미지를 `multipart/form-data` 형식으로 서버에 전송합니다.

2. **Inference**
   Flask 서버가 YOLO 모델을 통해 객체를 추론합니다.

3. **Response**
   분석 결과(라벨, 좌표값 등)를 JSON 형태로 반환합니다.

4. **UI Render**
   클라이언트가 좌표를 픽셀 단위로 변환하여 이미지 위에 결과를 렌더링합니다.

---

## 📈 Future Improvements (개선 방향)

* **모델 고도화**
  오인식 방지를 위한 부정 샘플(Negative Samples) 추가 학습

* **클라우드 배포**
  AWS 또는 GCP를 통한 24시간 안정적 서버 운영

* **항목 확장**
  캔, 유리, 스티로폼 등 분리배출 가이드 대상 품목 확대
