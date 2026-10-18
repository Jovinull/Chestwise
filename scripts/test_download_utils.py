from __future__ import annotations

import http.client
import subprocess
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

from scripts import download_utils
from scripts import quilt_smoke_test


class _Response:
    def __enter__(self) -> _Response:
        return self

    def __exit__(self, *_: object) -> None:
        return None

    def read(self) -> bytes:
        return b"artifact"


class DownloadRetryTests(unittest.TestCase):
    def test_retries_transient_http_failure_and_writes_successful_response(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            destination = Path(temporary_directory) / "artifact.jar"
            failure = urllib.error.HTTPError("https://example.invalid/file", 503, "busy", {}, None)
            with (
                patch.object(download_utils.urllib.request, "urlopen", side_effect=[failure, _Response()]) as opener,
                patch.object(download_utils.time, "sleep") as sleep,
            ):
                download_utils.download("https://example.invalid/file", destination, timeout=5)

            self.assertEqual(opener.call_count, 2)
            sleep.assert_called_once_with(1)
            self.assertEqual(destination.read_bytes(), b"artifact")


class SmokeRetryTests(unittest.TestCase):
    def test_retries_meta_503_even_if_installer_returns_success_without_jar(self) -> None:
        transient_output = (
            "Server returned HTTP response code: 503 for URL: "
            "https://meta.quiltmc.org/v3/versions/loader/1.20.1/0.30.0/server/json\n"
        )

        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)

            def run_installer(command: list[str], **_: object) -> subprocess.CompletedProcess[str]:
                if "--install-dir=" in command[-2]:
                    destination = Path(command[-2].split("=", 1)[1])
                    if destination.name == "quilt-install-2":
                        (destination / "quilt-server-launch.jar").write_bytes(b"launcher")
                        return subprocess.CompletedProcess(command, 0, "installed\n")
                return subprocess.CompletedProcess(command, 0, transient_output)

            with (
                patch.object(quilt_smoke_test.subprocess, "run", side_effect=run_installer) as runner,
                patch.object(quilt_smoke_test.time, "sleep") as sleep,
            ):
                result = quilt_smoke_test.install_quilt_server(
                    Path("java"), Path("installer.jar"), "1.20.1", "0.30.0", root
                )

            self.assertEqual(runner.call_count, 2)
            sleep.assert_called_once_with(1)
            self.assertTrue((result / "quilt-server-launch.jar").is_file())

    def test_quilt_installer_retry_is_bounded_and_only_for_meta_5xx(self) -> None:
        transient_output = (
            "Server returned HTTP response code: 503 for URL: "
            "https://meta.quiltmc.org/v3/versions/loader/1.20.1/0.30.0/server/json\n"
        )

        with tempfile.TemporaryDirectory() as temporary_directory:
            with (
                patch.object(
                    quilt_smoke_test.subprocess,
                    "run",
                    side_effect=lambda command, **_: subprocess.CompletedProcess(
                        command, 0, transient_output
                    ),
                ) as runner,
                patch.object(quilt_smoke_test.time, "sleep") as sleep,
                self.assertRaisesRegex(RuntimeError, "after 3 attempts"),
            ):
                quilt_smoke_test.install_quilt_server(
                    Path("java"), Path("installer.jar"), "1.20.1", "0.30.0", Path(temporary_directory)
                )

            self.assertEqual(runner.call_count, download_utils.MAX_ATTEMPTS)
            self.assertEqual([call.args[0] for call in sleep.call_args_list], [1, 2])

    def test_non_transient_installer_failure_is_not_retried(self) -> None:
        not_found = "HTTP response code: 404 for URL: https://meta.quiltmc.org/missing\n"

        with tempfile.TemporaryDirectory() as temporary_directory:
            command = ["java", "-jar", "installer.jar"]
            with patch.object(
                quilt_smoke_test.subprocess,
                "run",
                return_value=subprocess.CompletedProcess(command, 1, not_found),
            ) as runner, patch.object(quilt_smoke_test.time, "sleep") as sleep:
                with self.assertRaises(subprocess.CalledProcessError):
                    quilt_smoke_test.install_quilt_server(
                        Path("java"), Path("installer.jar"), "1.20.1", "0.30.0", Path(temporary_directory)
                    )

            runner.assert_called_once()
            sleep.assert_not_called()

    def test_does_not_retry_non_transient_http_error(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            destination = Path(temporary_directory) / "artifact.jar"
            failure = urllib.error.HTTPError("https://example.invalid/file", 404, "missing", {}, None)
            with (
                patch.object(download_utils.urllib.request, "urlopen", side_effect=failure) as opener,
                patch.object(download_utils.time, "sleep") as sleep,
                self.assertRaises(urllib.error.HTTPError),
            ):
                download_utils.download("https://example.invalid/file", destination, timeout=5)

            opener.assert_called_once()
            sleep.assert_not_called()
            self.assertFalse(destination.exists())

    def test_bounds_retries_for_incomplete_network_response(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            destination = Path(temporary_directory) / "artifact.jar"
            failure = http.client.IncompleteRead(b"partial", 10)
            with (
                patch.object(download_utils.urllib.request, "urlopen", side_effect=failure) as opener,
                patch.object(download_utils.time, "sleep") as sleep,
                self.assertRaises(http.client.IncompleteRead),
            ):
                download_utils.download("https://example.invalid/file", destination, timeout=5)

            self.assertEqual(opener.call_count, download_utils.MAX_ATTEMPTS)
            self.assertEqual([call.args[0] for call in sleep.call_args_list], [1, 2])
            self.assertFalse(destination.exists())

    def test_retries_url_error_wrapping_a_timeout(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            destination = Path(temporary_directory) / "artifact.jar"
            failure = urllib.error.URLError(TimeoutError("timed out"))
            with (
                patch.object(
                    download_utils.urllib.request,
                    "urlopen",
                    side_effect=[failure, _Response()],
                ) as opener,
                patch.object(download_utils.time, "sleep") as sleep,
            ):
                download_utils.download("https://example.invalid/file", destination, timeout=5)

            self.assertEqual(opener.call_count, 2)
            sleep.assert_called_once_with(1)
            self.assertEqual(destination.read_bytes(), b"artifact")


if __name__ == "__main__":
    unittest.main()
