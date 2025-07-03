import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';

// 커스텀 메트릭 정의
const reservationSuccessRate = new Rate('reservation_success_rate');
const reservationDuration = new Trend('reservation_duration');
const concurrentUsers = new Counter('concurrent_users');
const conflictErrors = new Counter('conflict_errors');
const authSuccessRate = new Rate('auth_success_rate');

export const options = {
  scenarios: {
    concurrent_reservations: {
      executor: 'shared-iterations',
      vus: 10,
      iterations: 10,
      maxDuration: '30s',
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.1'], // 실패율 10% 미만
    checks: ['rate>0.95'],
    reservation_success_rate: ['rate>=0.05'],
    auth_success_rate: ['rate>0.95'],
  },
  // InfluxDB 출력 설정
  ext: {
    loadimpact: {
      apm: []
    }
  }
};

// 테스트 사용자 계정 (10명)
const testUsers = [
  'user1@test.com',
  'user2@test.com',
  'user3@test.com',
  'user4@test.com',
  'user5@test.com',
  'user6@test.com',
  'user7@test.com',
  'user8@test.com',
  'user9@test.com',
  'user10@test.com',
];

// 예약 날짜: 오늘 기준 +3일, 시간 고정
const reservationDate = new Date(Date.now() + 3 * 24 * 60 * 60 * 1000)
.toISOString()
.split('T')[0];
const reservationTime = '14:00';

// 서버 URL
const BASE_URL = 'http://localhost:8080';

export function setup() {
  console.log('🎬 === FitPass 동시성 테스트 시작 ===');
  console.log(`🌐 서버 URL: ${BASE_URL}`);
  console.log(`📅 예약 날짜: ${reservationDate}`);
  console.log(`⏰ 예약 시간: ${reservationTime}`);
  console.log(`👥 동시 사용자 수: 10명`);
  console.log('⚡ 테스트 시작!\n');
}

export default function () {
  http.get('http://localhost:8080/reservations');  // 요청 이름 = "GET /reservations"
  concurrentUsers.add(1);

  const userIndex = (__VU - 1) % testUsers.length;
  const email = testUsers[userIndex];
  const password = 'password';

  // 로그인
  const loginPayload = JSON.stringify({ email, password });
  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '10s',
  });

  const loginBody = loginRes.json();

  const loggedIn = check(loginRes, {
    '로그인 성공 (200)': (r) => r.status === 200,
    '토큰 있음': () => loginBody?.data?.accessToken?.length > 0,
  });

  authSuccessRate.add(loggedIn ? 1 : 0);

  if (!loggedIn) {
    console.log(`❌ [VU-${__VU}] 로그인 실패: ${email} (status: ${loginRes.status})`);
    if (loginRes.body) console.log(`   응답: ${loginRes.body}`);
    return;
  }

  const token = loginBody.data.accessToken;
  console.log(`✅ [VU-${__VU}] 로그인 성공: ${email}`);

  // 예약 시도
  const reservationPayload = JSON.stringify({
    reservationDate,
    reservationTime,
  });

  const startTime = Date.now();
  const reservationRes = http.post(
      `${BASE_URL}/gyms/1/trainers/1/reservations`,
      reservationPayload,
      {
        headers: {
          Authorization: token,
          'Content-Type': 'application/json',
        },
        timeout: '15s',
      }
  );
  const duration = Date.now() - startTime;
  reservationDuration.add(duration);

  const reservationCheck = check(reservationRes, {
    '응답 받음': (r) => r.status !== 0,
    '예약 성공 (201)': (r) => {
      if (r.status === 201) {
        console.log(`🎉 [VU-${__VU}] 예약 성공! ${email} (${duration}ms)`);
        reservationSuccessRate.add(1);
        return true;
      }
      return false;
    },
    '중복 예약 (409)': (r) => {
      if (r.status === 409) {
        console.log(`⚠️ [VU-${__VU}] 중복 예약 발생: ${email} (${duration}ms)`);
        conflictErrors.add(1);
        reservationSuccessRate.add(0);
        return true;
      }
      return false;
    },
    '잘못된 요청 (400)': (r) => r.status === 400,
    '인증 실패 (401)': (r) => r.status === 401,
  });

  if (!reservationCheck) {
    console.log(`💥 [VU-${__VU}] 예약 실패: ${email} (status: ${reservationRes.status}, ${duration}ms)`);
    if (reservationRes.body) console.log(`   응답: ${reservationRes.body}`);
  }

  if (duration > 3000) {
    console.log(`🐌 [VU-${__VU}] 느린 응답: ${email} - ${duration}ms (3초 초과)`);
  } else if (duration < 500) {
    console.log(`⚡ [VU-${__VU}] 빠른 응답: ${email} - ${duration}ms`);
  }

  sleep(0.1);
}

export function teardown() {
  console.log('\n🏁 === 테스트 완료 ===');
  console.log('📈 결과는 Grafana 대시보드에서 확인하세요!');
  console.log('🔗 http://localhost:3000');
}

export function handleSummary(data) {
  console.log('\n🏁 === 동시성 테스트 결과 요약 ===');
  console.log(`📊 총 테스트 횟수: ${data.metrics.iterations.count}`);
  console.log(`⏱ 평균 응답시간: ${data.metrics.http_req_duration.avg.toFixed(2)}ms`);
  console.log(`🏆 예약 성공률: ${(data.metrics.reservation_success_rate.rate * 100).toFixed(1)}%`);
  console.log(`🔐 로그인 성공률: ${(data.metrics.auth_success_rate.rate * 100).toFixed(1)}%`);
  console.log(`⚔️ 충돌 수: ${data.metrics.conflict_errors.count || 0}`);
  console.log(`🎯 예상: 1명 예약 성공, 9명은 충돌 또는 실패`);

  return {
    stdout: JSON.stringify(data, null, 2),
  };
}
