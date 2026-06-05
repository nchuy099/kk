import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1000,
  duration: '30s',
};

export function setup() {
  const res = http.post('http://localhost:8088/realms/event-hub/protocol/openid-connect/token', {
    client_id: 'event-hub-cli',
    grant_type: 'password',
    username: 'user',
    password: 'user',
  });
  return { token: res.json('access_token') };
}

export default function (data) {
  const payload = JSON.stringify({
    eventId: '00000000-0000-0000-0000-000000000101',
    items: [
      {
        ticketCategoryId: '11111111-1111-1111-1111-111111111111',
        quantity: 1,
      },
    ],
  });

  const params = {
    headers: {
      Authorization: `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  };

  const res = http.post('http://localhost:8080/api/orders', payload, params);
  check(res, {
    'status is success or conflict': (r) => r.status === 200 || r.status === 201 || r.status === 409,
  });
}
