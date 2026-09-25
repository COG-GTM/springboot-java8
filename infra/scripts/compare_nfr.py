#!/usr/bin/env python3
"""Compare a k6 summary against the captured baseline; exit 1 on NFR regression."""
import json
import sys

P95_TOLERANCE = 1.5      # candidate p95 may be at most 1.5x baseline ...
P95_FLOOR_MS = 25.0      # ... or baseline + 25ms, whichever is larger (absorbs sub-ms noise)
ERR_TOLERANCE = 0.01     # absolute error-rate ceiling
RPS_TOLERANCE = 0.6      # candidate must keep >= 60% of baseline throughput


def load(path):
    with open(path) as fh:
        return json.load(fh)


def main(baseline_path, candidate_path):
    base, cand = load(baseline_path), load(candidate_path)
    rows = [
        ("p50 ms", base["latency_ms"]["p50"], cand["latency_ms"]["p50"], None),
        ("p95 ms", base["latency_ms"]["p95"], cand["latency_ms"]["p95"],
         cand["latency_ms"]["p95"] <= max(base["latency_ms"]["p95"] * P95_TOLERANCE,
                                          base["latency_ms"]["p95"] + P95_FLOOR_MS)),
        ("p99 ms", base["latency_ms"]["p99"], cand["latency_ms"]["p99"], None),
        ("rps", base["rps"], cand["rps"], cand["rps"] >= base["rps"] * RPS_TOLERANCE),
        ("error rate", base["error_rate"], cand["error_rate"], cand["error_rate"] <= ERR_TOLERANCE),
        ("report p95 ms", base["endpoint_p95_ms"]["report"], cand["endpoint_p95_ms"]["report"], None),
    ]
    print(f"{'metric':<15}{'baseline':>12}{'candidate':>12}  verdict")
    failed = False
    for name, b, c, ok in rows:
        verdict = "" if ok is None else ("ok" if ok else "REGRESSION")
        failed |= ok is False
        print(f"{name:<15}{b:>12}{c:>12}  {verdict}")
    for name, status in cand.get("thresholds", {}).items():
        print(f"k6 threshold {name}: {status}")
        failed |= status != "pass"
    if failed:
        print("NFR comparison FAILED against baseline", file=sys.stderr)
        sys.exit(1)
    print("NFR comparison passed")


if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("usage: compare_nfr.py BASELINE.json latest.json", file=sys.stderr)
        sys.exit(2)
    main(sys.argv[1], sys.argv[2])
