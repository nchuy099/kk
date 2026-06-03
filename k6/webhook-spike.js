import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 200,
  duration: '20s',
};

function randomUuid() {
  const hex = Array.from({ length: 32 }, () => Math.floor(Math.random() * 16).toString(16)).join('');
  return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
}

export function setup() {
  const orderId = randomUuid();
  const createPaymentPayload = JSON.stringify({
    orderId,
    amount: 500000,
  });
  const createPaymentRes = http.post('http://localhost:8084/payments', createPaymentPayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  return { paymentId: createPaymentRes.json('paymentId'), orderId };
}

export default function (data) {
  const payload = JSON.stringify({
    providerEventId: `evt-${__VU % 40}`,
    transactionId: `txn-${data.paymentId}`,
    orderId: data.orderId,
    status: 'SUCCESS',
    amount: 500000,
  });

  const params = {
    headers: { 'Content-Type': 'application/json' },
  };

  const res = http.post('http://localhost:8084/payments/webhook', payload, params);
  check(res, { 'status 200': (r) => r.status === 200 });
}
