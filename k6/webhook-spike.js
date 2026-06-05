import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 200,
  duration: '20s',
};

export function setup() {
  const tokenRes = http.post('http://localhost:8088/realms/event-hub/protocol/openid-connect/token', {
    client_id: 'event-hub-cli',
    grant_type: 'password',
    username: 'user',
    password: 'user',
  });
  const token = tokenRes.json('access_token');

  const orderRes = http.post('http://localhost:8080/api/orders', JSON.stringify({
    eventId: '00000000-0000-0000-0000-000000000101',
    items: [
      {
        ticketCategoryId: '11111111-1111-1111-1111-111111111111',
        quantity: 1,
      },
    ],
  }), {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
  });

  const orderId = orderRes.json('id');
  let payment = null;
  for (let attempt = 0; attempt < 10; attempt += 1) {
    const paymentRes = http.get(`http://localhost:8080/api/payments/by-order/${orderId}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    if (paymentRes.status === 200) {
      payment = paymentRes.json();
      break;
    }
    sleep(1);
  }

  return { orderId, token, paymentId: payment ? payment.paymentId : null };
}

export default function (data) {
  const payload = JSON.stringify({
    providerEventId: `evt-duplicate-${__VU % 40}`,
    transactionId: `txn-${data.orderId}`,
    orderId: data.orderId,
    status: 'SUCCEEDED',
    amount: 500000,
  });

  const params = {
    headers: {
      Authorization: `Bearer ${data.token}`,
      'Content-Type': 'application/json',
    },
  };

  const res = http.post('http://localhost:8080/api/payments/webhook', payload, params);
  check(res, { 'status 200': (r) => r.status === 200 });
}
