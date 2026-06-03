import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1000,
  duration: '30s',
};

export default function () {
  const payload = JSON.stringify({
    userId: `user-${__VU}-${__ITER}`,
    ticketTypeId: '11111111-1111-1111-1111-111111111111',
    quantity: 1,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post('http://localhost:8083/orders', payload, params);
  check(res, {
    'status is success or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409,
  });
}
