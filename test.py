from flask import Flask, request, jsonify
import io
from PIL import Image # Pillow 라이브러리 추가 (이미지 확인용)

app = Flask(__name__)

@app.route('/predict', methods=['POST'])
def predict():
    # 1. 요청에 'image'라는 이름의 파일이 있는지 확인
    if 'image' not in request.files:
        print(" * 오류: 이미지 파일 없음")
        # 실패 JSON 응답 (HTTP 상태 코드 400: Bad Request)
        return jsonify({'status': 'error', 'message': '이미지 파일이 요청에 포함되지 않았습니다.'}), 400

    file = request.files['image']

    try:
        # 2. 이미지 파일 열기 시도 (간단한 유효성 검사)
        img_bytes = file.read()
        img = Image.open(io.BytesIO(img_bytes))
        print(f" * 이미지 수신 성공: {file.filename} ({img.format}, {img.size})")

        # 3. 성공 JSON 응답 (HTTP 상태 코드 200: OK)
        return jsonify({'status': 'success', 'message': f'{file.filename} 이미지를 성공적으로 받았습니다.'})

    except Exception as e:
        # 이미지 파일 처리 중 오류 발생 시
        print(f" * 오류: 이미지 처리 실패 - {e}")
        # 실패 JSON 응답 (HTTP 상태 코드 500: Internal Server Error)
        return jsonify({'status': 'error', 'message': f'이미지 처리 중 오류 발생: {e}'}), 500

if __name__ == '__main__':
    # 개발 중에는 debug=True 로 설정하면 코드 변경 시 서버 자동 재시작
    app.run(host='0.0.0.0', port=5000, debug=True)

