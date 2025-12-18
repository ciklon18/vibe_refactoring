import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '10s', target: 10 },
    { duration: '20s', target: 25 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.01'],
  },
};

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const res = http.get(`${baseUrl}/api/laptops`);
  check(res, {
    'status is 200': (r) => r.status === 200,
  });
  sleep(1);
}
