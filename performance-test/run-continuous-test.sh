#!/bin/bash

# FitPass 동시성 테스트 - 연속 실행 (실시간 모니터링용)
echo "🔄 FitPass 연속 동시성 테스트 시작"
echo "======================================"
echo "⚠️  Ctrl+C로 중단할 수 있습니다"
echo ""

# 환경 변수 설정
export BASE_URL=${BASE_URL:-"http://localhost:8080"}
export RESERVATION_TIME="14:00"
export CLASS_ID="1"

# 테스트 카운터
test_count=0

# 연속 테스트 실행
while true; do
    test_count=$((test_count + 1))
    
    # 매번 다른 날짜로 테스트 (중복 방지)
    current_date=$(date -d "+$test_count days" +%Y-%m-%d)
    export RESERVATION_DATE=$current_date
    
    echo "🚀 테스트 #$test_count 실행 중... (날짜: $current_date)"
    
    # K6 테스트 실행
    k6 run \
      --quiet \
      --out influxdb=http://localhost:8086/k6 \
      k6-concurrency-enhanced.js
    
    echo "✅ 테스트 #$test_count 완료"
    echo "📊 Grafana에서 실시간 결과 확인: http://localhost:3000"
    echo ""
    
    # 10초 대기 후 다음 테스트
    echo "⏳ 10초 후 다음 테스트..."
    sleep 10
done
