#!/usr/bin/env python3
"""
Simple stdlib-based load generator for ShopAPI.
- Targets GET /api/laptops by default.
- Can optionally spin up a lightweight mock server when the real app is unavailable.
- Reports throughput (RPS), latency distribution, and error rate.
"""
import argparse
import json
import threading
import time
from concurrent.futures import ThreadPoolExecutor
from dataclasses import dataclass
from http.server import BaseHTTPRequestHandler, HTTPServer
from statistics import mean
from typing import List
from urllib import request, error


@dataclass
class Metrics:
    total: int
    success: int
    failures: int
    latencies_ms: List[float]
    duration_s: float

    @property
    def rps(self) -> float:
        return self.total / self.duration_s if self.duration_s > 0 else 0.0

    @property
    def error_rate(self) -> float:
        return self.failures / self.total if self.total else 0.0

    def percentile(self, pct: float) -> float:
        if not self.latencies_ms:
            return 0.0
        data = sorted(self.latencies_ms)
        k = (len(data) - 1) * (pct / 100)
        f = int(k)
        c = min(f + 1, len(data) - 1)
        if f == c:
            return data[f]
        return data[f] + (data[c] - data[f]) * (k - f)


class LaptopMockHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith("/api/laptops"):
            body = json.dumps({"items": [], "count": 0}).encode()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        return  # silence


def start_mock_server(port: int) -> HTTPServer:
    server = HTTPServer(("0.0.0.0", port), LaptopMockHandler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    return server


def fetch_once(url: str, timeout: float) -> float:
    start = time.perf_counter()
    req = request.Request(url, method="GET")
    try:
        with request.urlopen(req, timeout=timeout) as resp:
            if resp.status >= 400:
                raise error.HTTPError(req.full_url, resp.status, "", resp.headers, None)
    except Exception:
        raise
    finally:
        elapsed_ms = (time.perf_counter() - start) * 1000
    return elapsed_ms


def run_load(url: str, duration: float, concurrency: int, timeout: float) -> Metrics:
    start = time.perf_counter()
    deadline = start + duration
    successes: List[float] = []
    failures = 0
    total = 0
    lock = threading.Lock()

    def worker():
        nonlocal successes, failures, total
        while time.perf_counter() < deadline:
            try:
                latency = fetch_once(url, timeout)
                with lock:
                    successes.append(latency)
            except Exception:
                with lock:
                    failures += 1
            finally:
                with lock:
                    total += 1

    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        for _ in range(concurrency):
            executor.submit(worker)

    duration_actual = time.perf_counter() - start
    return Metrics(total=total, success=len(successes), failures=failures, latencies_ms=successes, duration_s=duration_actual)


def main():
    parser = argparse.ArgumentParser(description="Run a lightweight load test against ShopAPI.")
    parser.add_argument("--url", default="http://localhost:8080/api/laptops", help="Target URL (default: http://localhost:8080/api/laptops)")
    parser.add_argument("--duration", type=float, default=10.0, help="Test duration in seconds")
    parser.add_argument("--concurrency", type=int, default=16, help="Number of concurrent workers")
    parser.add_argument("--timeout", type=float, default=2.0, help="Per-request timeout in seconds")
    parser.add_argument("--mock", action="store_true", help="Run against a bundled mock server instead of a live app")
    parser.add_argument("--mock-port", type=int, default=18080, help="Port for the mock server (when --mock is set)")
    args = parser.parse_args()

    server = None
    target_url = args.url
    if args.mock:
        server = start_mock_server(args.mock_port)
        target_url = f"http://localhost:{args.mock_port}/api/laptops"
        print(f"[mock] Serving {target_url}")

    print(f"Target: {target_url}\nDuration: {args.duration}s, Concurrency: {args.concurrency}")
    metrics = run_load(target_url, args.duration, args.concurrency, args.timeout)

    if server:
        server.shutdown()

    avg_latency = mean(metrics.latencies_ms) if metrics.latencies_ms else 0.0
    print("\nResults:")
    print(f"  Requests: {metrics.total} (success: {metrics.success}, failures: {metrics.failures})")
    print(f"  Duration: {metrics.duration_s:.2f}s")
    print(f"  RPS: {metrics.rps:.2f}")
    print(f"  Error rate: {metrics.error_rate * 100:.2f}%")
    print("  Latency (ms):")
    print(f"    avg: {avg_latency:.2f}")
    print(f"    p50: {metrics.percentile(50):.2f}")
    print(f"    p90: {metrics.percentile(90):.2f}")
    print(f"    p95: {metrics.percentile(95):.2f}")


if __name__ == "__main__":
    main()
