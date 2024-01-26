import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    thresholds: {
        http_req_failed: [{threshold: 'rate<0.01', abortOnFail: true}],
        http_req_duration: ['p(99)<500'], // 99% of requests should be below 500ms
    },
    scenarios: {
        load_test: {
            executor: 'ramping-vus',
            stages: [
                {duration: '10s', target: 20},
                {duration: '20s', target: 20},
                {duration: '5s', target: 0},
            ],
        },
    },
};

export default function () {
    const res = http.get('http://localhost:8080/trace/random');
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);
}
