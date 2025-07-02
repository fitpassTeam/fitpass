#!/bin/bash

echo "🔧 K6 + Grafana + InfluxDB 연결 테스트"
echo "======================================="

# 1. InfluxDB 연결 테스트
echo "1️⃣ InfluxDB 연결 테스트..."
INFLUX_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8086/ping)
if [ "$INFLUX_RESPONSE" = "204" ]; then
    echo "✅ InfluxDB 연결 성공 (응답코드: $INFLUX_RESPONSE)"
else
    echo "❌ InfluxDB 연결 실패 (응답코드: $INFLUX_RESPONSE)"
    echo "   docker-compose up -d influxdb를 실행하세요"
    exit 1
fi

# 2. Grafana 연결 테스트
echo "2️⃣ Grafana 연결 테스트..."
GRAFANA_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:3000/api/health)
if [ "$GRAFANA_RESPONSE" = "200" ]; then
    echo "✅ Grafana 연결 성공 (응답코드: $GRAFANA_RESPONSE)"
else
    echo "❌ Grafana 연결 실패 (응답코드: $GRAFANA_RESPONSE)"
    echo "   docker-compose up -d grafana를 실행하세요"
    exit 1
fi

# 3. InfluxDB에 k6 데이터베이스가 있는지 확인
echo "3️⃣ InfluxDB k6 데이터베이스 확인..."
DATABASES=$(curl -s "http://localhost:8086/query?q=SHOW%20DATABASES" | grep -o '"k6"')
if [ "$DATABASES" = '"k6"' ]; then
    echo "✅ k6 데이터베이스 존재"
else
    echo "⚠️ k6 데이터베이스 없음 - 첫 k6 테스트 실행 시 자동 생성됩니다"
fi

# 4. 간단한 k6 테스트로 데이터 전송 테스트
echo "4️⃣ K6 테스트 데이터 전송 테스트..."
cat << 'EOF' > /tmp/test-k6.js
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1,
  duration: '5s',
};

export default function () {
  const res = http.get('http://httpbin.org/get');
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
EOF

echo "   간단한 테스트 실행 중..."
k6 run --out influxdb=http://localhost:8086/k6 /tmp/test-k6.js > /dev/null 2>&1

# 5. InfluxDB에 데이터가 들어왔는지 확인
echo "5️⃣ InfluxDB 데이터 확인..."
sleep 2
HTTP_REQS=$(curl -s "http://localhost:8086/query?db=k6&q=SELECT%20count(*)%20FROM%20http_reqs" | grep -o '"count_value":[0-9]*' | cut -d':' -f2)
if [ ! -z "$HTTP_REQS" ] && [ "$HTTP_REQS" -gt 0 ]; then
    echo "✅ InfluxDB에 k6 데이터 정상 저장됨 (http_reqs: $HTTP_REQS개)"
else
    echo "❌ InfluxDB에 k6 데이터 없음"
    echo "   k6 설치 상태: $(which k6 >/dev/null && echo '✅ 설치됨' || echo '❌ 미설치')"
fi

# 6. Grafana 데이터소스 확인
echo "6️⃣ Grafana 데이터소스 확인..."
DATASOURCE_TEST=$(curl -s -u admin:admin "http://localhost:3000/api/datasources/uid/InfluxDB-K6" | grep -o '"name":"InfluxDB-K6"')
if [ "$DATASOURCE_TEST" = '"name":"InfluxDB-K6"' ]; then
    echo "✅ Grafana InfluxDB-K6 데이터소스 설정됨"
else
    echo "❌ Grafana InfluxDB-K6 데이터소스 없음"
    echo "   Grafana 재시작이 필요할 수 있습니다: docker-compose restart grafana"
fi

# 임시 파일 정리
rm -f /tmp/test-k6.js

echo ""
echo "🎯 다음 단계:"
echo "   1. 모든 테스트가 성공했다면 'bash run-concurrency-test.sh' 실행"
echo "   2. Grafana 대시보드 확인: http://localhost:3000 (admin/admin)"
echo "   3. 대시보드: 'FitPass PT 예약 시스템 - 고급 동시성 분석'"
echo ""
