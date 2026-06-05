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
  const competitions = http.get('http://localhost:8080/api/competitions');
  check(competitions, { 'competitions status is 200': (r) => r.status === 200 });

  const events = http.get('http://localhost:8080/api/competitions/00000000-0000-0000-0000-000000000001/events');
  check(events, { 'competition events status is 200': (r) => r.status === 200 });

  const categories = http.get('http://localhost:8080/api/events/00000000-0000-0000-0000-000000000101/ticket-categories');
  check(categories, { 'ticket categories status is 200': (r) => r.status === 200 });
  sleep(1);
}
