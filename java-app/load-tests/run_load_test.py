#!/usr/bin/env python3
"""Simple load test runner using only the Python standard library.

Features:
- Adjustable duration and concurrency
- Computes RPS, average latency, and percentile latencies
- Reports error rate
- Optional in-process mock server to avoid external dependencies
"""
from __future__ import annotations

import argparse
import json
import queue
import random
import threading
import time
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer
from statistics import mean
from typing import List


def start_mock_server(port: int) -> HTTPServer:
    class Handler(BaseHTTPRequestHandler):
        def do_GET(self):
            payload = {
                "laptops": {"count": 5, "stock": 12, "averagePrice": 990.0, "inventoryValue": 11880.0},
                "monitors": {"count": 3, "stock": 7, "averagePrice": 240.0, "inventoryValue": 1680.0},
                "totalCount": 8,
                "totalStock": 19,
            }
            encoded = json.dumps(payload).encode()
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(encoded)))
            self.end_headers()
            self.wfile.write(encoded)

        def log_message(self, *args, **kwargs):
            return  # silence default logging

    server = HTTPServer(("0.0.0.0", port), Handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    return server


def percentile(values: List[float], pct: float) -> float:
    if not values:
        return 0.0
    values_sorted = sorted(values)
    k = (len(values_sorted) - 1) * pct
    f = int(k)
    c = min(f + 1, len(values_sorted) - 1)
    if f == c:
        return values_sorted[int(k)]
    d0 = values_sorted[f] * (c - k)
    d1 = values_sorted[c] * (k - f)
    return d0 + d1


def worker(url: str, timeout: float, stop_event: threading.Event, results: queue.Queue):
    while not stop_event.is_set():
        start = time.perf_counter()
        success = True
        try:
            with urllib.request.urlopen(url, timeout=timeout) as resp:
                # read response to ensure connection fully consumed
                resp.read()
                status = resp.status
                if status >= 500:
                    success = False
        except (urllib.error.URLError, urllib.error.HTTPError, TimeoutError):
            success = False
        elapsed = time.perf_counter() - start
        results.put((elapsed, success))


def run_load(url: str, duration: float, concurrency: int, timeout: float) -> None:
    stop_event = threading.Event()
    results: queue.Queue = queue.Queue()
    threads = [threading.Thread(target=worker, args=(url, timeout, stop_event, results), daemon=True) for _ in range(concurrency)]

    for t in threads:
        t.start()

    time.sleep(duration)
    stop_event.set()

    # allow workers to finish outstanding requests
    for t in threads:
        t.join()

    latencies: List[float] = []
    successes = 0
    total = 0
    while not results.empty():
        elapsed, ok = results.get()
        latencies.append(elapsed)
        successes += 1 if ok else 0
        total += 1

    errors = total - successes
    rps = total / duration if duration > 0 else 0
    avg_latency_ms = mean(latencies) * 1000 if latencies else 0

    print("=== Load Test Report ===")
    print(f"Target URL: {url}")
    print(f"Duration: {duration}s, Concurrency: {concurrency}")
    print(f"Requests: {total}, Success: {successes}, Errors: {errors}, Error rate: {errors / total * 100 if total else 0:.2f}%")
    print(f"RPS: {rps:.2f}")
    print(f"Average latency: {avg_latency_ms:.2f} ms")
    print(f"p50 latency: {percentile(latencies, 0.50) * 1000:.2f} ms")
    print(f"p95 latency: {percentile(latencies, 0.95) * 1000:.2f} ms")
    print(f"p99 latency: {percentile(latencies, 0.99) * 1000:.2f} ms")


def main():
    parser = argparse.ArgumentParser(description="Lightweight HTTP load test with optional mock server")
    parser.add_argument("--url", default="http://localhost:8080/api/stats", help="Target URL")
    parser.add_argument("--duration", type=float, default=10.0, help="Test duration in seconds")
    parser.add_argument("--concurrency", type=int, default=20, help="Number of concurrent workers")
    parser.add_argument("--timeout", type=float, default=2.0, help="Request timeout in seconds")
    parser.add_argument("--mock", action="store_true", help="Run against a local mock server (port 18080)")
    args = parser.parse_args()

    server = None
    url = args.url
    if args.mock:
        port = 18080
        server = start_mock_server(port)
        url = f"http://localhost:{port}/api/stats"
        print(f"Started mock server on {url}")
        # small jitter to avoid stampeding the mock instantly
        time.sleep(random.uniform(0.1, 0.3))

    try:
        run_load(url, args.duration, args.concurrency, args.timeout)
    finally:
        if server:
            server.shutdown()
            server.server_close()


if __name__ == "__main__":
    main()
