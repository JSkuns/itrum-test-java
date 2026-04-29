import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
  stages: [
    { duration: '30s', target: 100 },   // Разгон до 100 RPS
    { duration: '1m', target: 500 },    // До 500 RPS
    { duration: '2m', target: 1000 },   // До 1000 RPS
    { duration: '3m', target: 1000 },   // Держим 1000 RPS
    { duration: '1m', target: 0 },      // Остановка
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],     // Ошибок < 1%
    http_req_duration: ['p(95)<500'],   // 95% запросов < 500ms
  },
};

const WALLET_ID = '550e8400-e29b-41d4-a716-446655440000';

export default function () {
  const payload = {
    walletId: WALLET_ID,
    operationType: 'DEPOSIT',
    amount: 1,
  };

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post('http://localhost:8080/api/v1/wallet', JSON.stringify(payload), params);
  
  check(res, {
    'status is 200 or 400 or 404 or 409': (r) => 
      [200, 400, 404, 409].includes(r.status),
    'no 5xx errors': (r) => r.status < 500,
  });

  sleep(0.001); // Минимальная задержка
}