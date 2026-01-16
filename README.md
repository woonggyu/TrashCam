🗑️ TrashCam: AI 기반 쓰레기 자동 분류 시스템
TrashCam은 객체 인식 기술을 활용하여 사용자에게 올바른 분리배출 방법을 안내하는 안드로이드 애플리케이션 및 백엔드 시스템입니다.

📝 프로젝트 개요
목적: 딥러닝 모델을 활용한 쓰레기 이미지 인식 및 분리배출 정보 제공.

주요 기능:

카메라를 이용한 쓰레기 실시간 인식.

AI 모델을 통한 객체 분류 및 결과 전송.

안드로이드와 스프링 부트 서버 간의 REST API 양방향 통신.

🛠️ 기술 스택 (Tech Stack)
Frontend (Android)
Language: Kotlin

Library: CameraX (카메라 기능 구현)

Communication: Retrofit2 (Server API 통신)

Backend (Server)
Framework: flask

Language: Kotlin

AI & Recognition
Model: YOLO (You Only Look Once)


🌐 서버 배포 및 연결 (ngrok)
본 프로젝트는 개발 환경에서 안드로이드 앱과 로컬 서버를 연결하기 위해 ngrok을 사용합니다.

ngrok 활용: 로컬 호스트(localhost:8080)를 외부 공인 URL로 터널링하여 안드로이드 에뮬레이터 또는 실기기에서 서버에 접근할 수 있도록 설정했습니다.

연결 방식: 안드로이드의 Retrofit 설정 주소에 ngrok에서 생성된 임시 URL을 적용하여 통신을 수행합니다.
