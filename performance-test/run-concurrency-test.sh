#!/bin/bash

# FitPass 동시성 테스트 실행 스크립트
echo "🚀 FitPass PT 예약 시스템 - 동시성 테스트 시작"
echo "================================================"

# 환경 변수 설정
export BASE_URL=${BASE_URL:-"http://localhost:8080"}
export RESERVATION_TIME=${RESERVATION_TIME:-"14:00"}
export RESERVATION_DATE=${RESERVATION_DATE:-"2025-06-30"}
export CLASS_ID=${CLASS_ID:-"1"}

# InfluxDB 연결 확인
echo "📊 InfluxDB 연결 확인 중..."
curl -s http://localhost:8086/ping > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ InfluxDB 연결 성공"
else
    echo "❌ InfluxDB 연결 실패 - docker-compose up -d 실행 후 다시 시도하세요"
    exit 1
fi

# 결과 디렉토리 생성
mkdir -p ./results

echo ""
echo "🎯 테스트 설정:"
echo "   - 서버 URL: $BASE_URL"
echo "   - 예약 날짜: $RESERVATION_DATE"
echo "   - 예약 시간: $RESERVATION_TIME"
echo "   - 클래스 ID: $CLASS_ID"
echo "   - 동시 사용자: 10명"
echo ""

# K6 테스트 실행 (InfluxDB 출력 포함)
echo "⚡ 동시성 테스트 실행 중..."
k6 run \
  --out influxdb=http://localhost:8086/k6 \
  --summary-trend-stats="avg,min,med,max,p(90),p(95),p(99)" \
  --summary-time-unit=ms \
  --tag environment=dev \
  --tag testname=concurrency-test \
  k6-concurrency-enhanced.js

echo ""
echo "🎉 테스트 완료!"
echo "📈 Grafana 대시보드에서 결과 확인: http://localhost:3000"
echo "📊 대시보드: 'FitPass PT 예약 시스템 - 고급 동시성 분석'"
echo ""

# 결과 파일 위치 안내
if [ -f "./results/test-summary.json" ]; then
    echo "📁 상세 결과 파일: ./results/test-summary.json"
fi
