import os
import io
import json
from flask import Flask, request, jsonify
from PIL import Image
from ultralytics import YOLO
import logging
# from flask_sqlalchemy import SQLAlchemy # 챗봇 관련 임포트 제거
# from datetime import datetime # 챗봇 관련 임포트 제거

# --- 1. 로거 및 Flask 앱 초기화 ---
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# --- 2. 기본 설정 (YOLO) ---

# YOLO 모델 경로 (나의 경로로 바꾸기)
MODEL_PATH = "C:/Users/kosfg/runs/train9/weights/best.pt"


# 최대 파일 크기 제한 (YOLO용)
app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024 * 1024 # 10 GB

# (제거) 챗봇용 SQLite DB 설정
# (제거) SQLAlchemy DB 인스턴스 초기화

# --- 3. 모델 로드 ---

# 3.1. YOLOv8 모델 로드
try:
    model_yolo = YOLO(MODEL_PATH)
    logger.info(f"YOLO Model loaded successfully from: {MODEL_PATH}")
except Exception as e:
    logger.error(f"Error loading YOLO model from {MODEL_PATH}: {e}")
    model_yolo = None

# (제거) 챗봇 DB 모델 정의 (Items, ChatLog)

# --- 4. API 엔드포인트 정의 (YOLO) ---

# 4.1. YOLO 추론 API 엔드포인트
@app.route('/detect', methods=['POST'])
def detect_objects():
    if model_yolo is None:
        return jsonify({"error": "YOLO Model is not loaded. Check server startup logs."}), 500

    # ▼▼▼ 1. 이미지 파일이 없는 경우 (1차 확인) ▼▼▼
    if 'image' not in request.files:
        logger.warning("Request received without 'image' file part.") # 로그 추가
        return jsonify({"error": "No image file provided in request (expected 'image' field)."}), 400

    image_file = request.files['image']
    logger.info(f"Received file: {image_file.filename}") # 로그 추가

    try:
        image_bytes = image_file.read()
        image = Image.open(io.BytesIO(image_bytes))

        # ▼▼▼ 2. 이미지를 성공적으로 읽었음 (2차 확인) ▼▼▼
        logger.info(f"Image successfully read and opened (Format: {image.format}, Size: {image.size})")

        # ▼▼▼▼▼▼▼▼▼▼▼ 여기가 수정되었습니다 ▼▼▼▼▼▼▼▼▼▼▼
        # YOLOv8 추론 수행 (imgsz 파라미터 추가)
        # 640 (기본값) 대신 1280 픽셀 크기로 분석하여 작은 물체 탐지율 향상
        results = model_yolo(image, imgsz=1280, conf=0.5, iou=0.45, verbose=False)
        # ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        # 결과 파싱
        detections = []
        for r in results:
            boxes = r.boxes.xyxyn.cpu().tolist()
            classes = r.boxes.cls.cpu().tolist()
            confidences = r.boxes.conf.cpu().tolist()
            names_map = r.names

            for box, cls, conf in zip(boxes, classes, confidences):
                detections.append({
                    "classId": int(cls),
                    "className": names_map[int(cls)],
                    "confidence": round(conf, 4),
                    "boxNormalized": [round(c, 4) for c in box]
                })

        # ▼▼▼ 3. 분석까지 성공적으로 완료 (최종 확인) ▼▼▼
        logger.info(f"Successfully processed image. Detected {len(detections)} objects.")
        return jsonify({"detections": detections}), 200

    except Exception as e:
        logger.error(f"An error occurred during inference: {e}", exc_info=True)
        return jsonify({"error": "Internal server error during image processing or inference."}), 500

# (제거) 챗봇 DB 초기 설정 API (/setup)
# (제거) 챗봇 응답 API (/api/chatbot)

# --- 5. 서버 실행 ---
if __name__ == '__main__':
    # Flask 서버 실행 설정
    # host='0.0.0.0'은 외부(안드로이드 앱, 스프링 부트) 접속 허용
    # port=5000은 앱에서 호출할 포트
    logger.info("Starting Flask server on host 0.0.0.0, port 5000...")
    app.run(host='0.0.0.0', port=5000, debug=False)

