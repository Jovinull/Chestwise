"""Shared, bounded retry policy for external smoke-test downloads."""

from __future__ import annotations

from collections.abc import Sequence
import errno
import http.client
import re
import socket
import sys
import time
import urllib.error
import urllib.request
from pathlib import Path


MAX_ATTEMPTS = 3
RETRYABLE_HTTP_STATUSES = {408, 425, 429, 500, 502, 503, 504}
RETRYABLE_ERRNOS = {
    errno.ECONNABORTED,
    errno.ECONNREFUSED,
    errno.ECONNRESET,
    errno.EHOSTUNREACH,
    errno.ENETUNREACH,
    errno.ETIMEDOUT,
}
_HTTP_STATUS_PATTERN = re.compile(
    r"\bHTTP(?:\s+response\s+code:|\s+Error)?\s*(502|503|504)\b", re.IGNORECASE
)


def retry_delay_seconds(attempt: int) -> int:
    """Return the bounded install/download retry delay after a failed attempt."""
    if attempt < 1:
        raise ValueError("attempt must be positive")
    return 1 << (attempt - 1)


def transient_http_status(output: str, hosts: Sequence[str]) -> int | None:
    """Return a transient HTTP status only when installer output names a trusted host."""
    allowed_hosts = tuple(host.lower() for host in hosts)
    for line in output.splitlines():
        if not any(host in line.lower() for host in allowed_hosts):
            continue
        match = _HTTP_STATUS_PATTERN.search(line)
        if match:
            return int(match.group(1))
    return None


def _is_transient(error: Exception) -> bool:
    if isinstance(error, urllib.error.HTTPError):
        return error.code in RETRYABLE_HTTP_STATUSES
    if isinstance(error, (TimeoutError, ConnectionError, http.client.IncompleteRead)):
        return True

    reason = error.reason if isinstance(error, urllib.error.URLError) else error
    if isinstance(reason, (TimeoutError, ConnectionError, http.client.IncompleteRead)):
        return True
    if isinstance(reason, socket.gaierror):
        return reason.errno == getattr(socket, "EAI_AGAIN", None)
    return isinstance(reason, OSError) and reason.errno in RETRYABLE_ERRNOS


def download(
    url: str,
    destination: Path,
    *,
    timeout: int,
    headers: dict[str, str] | None = None,
) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.is_file():
        return

    request: str | urllib.request.Request = (
        urllib.request.Request(url, headers=headers) if headers else url
    )
    for attempt in range(1, MAX_ATTEMPTS + 1):
        try:
            print(f"Downloading {url}", flush=True)
            with urllib.request.urlopen(request, timeout=timeout) as response:
                payload = response.read()
            destination.write_bytes(payload)
            return
        except (
            urllib.error.HTTPError,
            urllib.error.URLError,
            OSError,
            http.client.IncompleteRead,
        ) as error:
            if not _is_transient(error):
                raise
            if attempt == MAX_ATTEMPTS:
                print(
                    f"Download failed after {MAX_ATTEMPTS} attempts: {url}: {error}",
                    file=sys.stderr,
                )
                raise
            delay = retry_delay_seconds(attempt)
            print(
                f"Transient download failure ({error}); retry {attempt + 1}/{MAX_ATTEMPTS} "
                f"in {delay}s: {url}",
                file=sys.stderr,
                flush=True,
            )
            time.sleep(delay)
