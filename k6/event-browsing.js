import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
        { duration: '1m', target: 50 },

    // { duration: '2m', target: 100 },
    // { duration: '3m', target: 300 },
    // { duration: '2m', target: 0 },
  ],
};

export default function () {
  const res = http.get('http://localhost:8080/api/events');
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
