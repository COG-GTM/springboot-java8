// k6 load profile for topics-api, run through the load balancer.
// Env: BASE_URL, VUS, DURATION, P95_MS (threshold), ERR_RATE (threshold), OUT (summary path)
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://lb.topics.internal';
const P95_MS = Number(__ENV.P95_MS || 250);
const ERR_RATE = Number(__ENV.ERR_RATE || 0.01);
const OUT = __ENV.OUT || '/results/latest.json';

const listLatency = new Trend('topic_list_ms', true);
const reportLatency = new Trend('topic_report_ms', true);

export const options = {
  vus: Number(__ENV.VUS || 20),
  duration: __ENV.DURATION || '30s',
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(50)', 'p(90)', 'p(95)', 'p(99)'],
  thresholds: {
    http_req_failed: [`rate<${ERR_RATE}`],
    http_req_duration: [`p(95)<${P95_MS}`],
  },
};

export default function () {
  const list = http.get(`${BASE_URL}/topic`, { tags: { name: 'GET /topic' } });
  listLatency.add(list.timings.duration);
  check(list, { 'list 200': (r) => r.status === 200 });

  const one = http.get(`${BASE_URL}/topic/spring`, { tags: { name: 'GET /topic/{id}' } });
  check(one, { 'get 200': (r) => r.status === 200 });

  const sorted = http.get(`${BASE_URL}/topic/sort`, { tags: { name: 'GET /topic/sort' } });
  check(sorted, { 'sort 200': (r) => r.status === 200 });

  const report = http.get(`${BASE_URL}/topic/report`, { tags: { name: 'GET /topic/report' } });
  reportLatency.add(report.timings.duration);
  check(report, { 'report 200': (r) => r.status === 200 });

  const id = `load-${__VU}-${__ITER}`;
  const created = http.post(`${BASE_URL}/topic`,
    JSON.stringify({ id, subjectName: 'Load', subjectDescription: 'k6' }),
    { headers: { 'Content-Type': 'application/json' }, tags: { name: 'POST /topic' } });
  check(created, { 'post 201': (r) => r.status === 201 });

  const deleted = http.del(`${BASE_URL}/topic/${id}`, null, { tags: { name: 'DELETE /topic/{id}' } });
  check(deleted, { 'delete 204': (r) => r.status === 204 });

  sleep(0.05);
}

function pct(metric, p) {
  if (!metric || !metric.values || metric.values[p] === undefined) return null;
  return Number(metric.values[p].toFixed(2));
}

export function handleSummary(data) {
  const d = data.metrics.http_req_duration;
  const thresholds = {};
  for (const [name, m] of Object.entries(data.metrics)) {
    if (m.thresholds) {
      for (const [expr, t] of Object.entries(m.thresholds)) thresholds[`${name}: ${expr}`] = t.ok ? 'pass' : 'FAIL';
    }
  }
  const summary = {
    generated_at: new Date().toISOString(),
    target: BASE_URL,
    profile: { vus: options.vus, duration: options.duration },
    requests: data.metrics.http_reqs.values.count,
    rps: Number(data.metrics.http_reqs.values.rate.toFixed(2)),
    latency_ms: {
      p50: pct(d, 'p(50)'), p90: pct(d, 'p(90)'), p95: pct(d, 'p(95)'), p99: pct(d, 'p(99)'),
      avg: pct(d, 'avg'), max: pct(d, 'max'),
    },
    endpoint_p95_ms: {
      list: pct(data.metrics.topic_list_ms, 'p(95)'),
      report: pct(data.metrics.topic_report_ms, 'p(95)'),
    },
    error_rate: Number(data.metrics.http_req_failed.values.rate.toFixed(4)),
    checks_failed: data.metrics.checks ? data.metrics.checks.values.fails : 0,
    thresholds,
  };
  const out = {};
  out[OUT] = JSON.stringify(summary, null, 2) + '\n';
  out.stdout = `\n== k6 summary ==\n${JSON.stringify(summary, null, 2)}\n`;
  return out;
}
