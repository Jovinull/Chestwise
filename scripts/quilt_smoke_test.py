#!/usr/bin/env python3
"""Run a packaged Chestwise Fabric JAR on a real Quilt dedicated server."""

from __future__ import annotations

import argparse
import queue
import shutil
import subprocess
import sys
import threading
import time
import urllib.request
from pathlib import Path


INSTALLER_VERSION = "0.15.1"
DEFAULT_LOADER_VERSION = "0.30.0"
STARTUP_TIMEOUT_SECONDS = 180


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.is_file():
        return
    print(f"Downloading {url}", flush=True)
    with urllib.request.urlopen(url, timeout=60) as response:
        destination.write_bytes(response.read())


def stream_output(process: subprocess.Popen[str], lines: queue.Queue[str | None]) -> None:
    assert process.stdout is not None
    for line in process.stdout:
        lines.put(line)
    lines.put(None)


def write_log(line: str) -> None:
    """Write Minecraft's UTF-8 log without depending on the host console code page."""
    sys.stdout.buffer.write(line.encode("utf-8", errors="replace"))
    sys.stdout.buffer.flush()


def run_server(java: Path, server_dir: Path) -> None:
    process = subprocess.Popen(
        [str(java), "-Xms512M", "-Xmx1G", "-jar", "quilt-server-launch.jar", "nogui"],
        cwd=server_dir,
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
        bufsize=1,
    )
    lines: queue.Queue[str | None] = queue.Queue()
    threading.Thread(target=stream_output, args=(process, lines), daemon=True).start()

    deadline = time.monotonic() + STARTUP_TIMEOUT_SECONDS
    initialized = False
    ready = False
    try:
        while time.monotonic() < deadline:
            try:
                line = lines.get(timeout=1)
            except queue.Empty:
                if process.poll() is not None:
                    break
                continue
            if line is None:
                break
            write_log(line)
            initialized |= "Chestwise " in line and " initializing" in line
            if "Done (" in line and "For help" in line:
                ready = True
                break

        if not initialized:
            raise RuntimeError("Quilt never initialized the packaged Chestwise mod")
        if not ready:
            raise RuntimeError("Quilt server did not reach its ready state before timeout")

        assert process.stdin is not None
        process.stdin.write("stop\n")
        process.stdin.flush()
        exit_code = process.wait(timeout=60)
        if exit_code != 0:
            raise RuntimeError(f"Quilt server exited with code {exit_code}")
    finally:
        if process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--minecraft", required=True)
    parser.add_argument("--fabric-api", required=True)
    parser.add_argument("--artifact", required=True, type=Path)
    parser.add_argument("--java", default="java", type=Path)
    parser.add_argument("--loader", default=DEFAULT_LOADER_VERSION)
    parser.add_argument("--work-root", default=Path("build/quilt-smoke"), type=Path)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    artifact = args.artifact.resolve()
    if not artifact.is_file():
        raise FileNotFoundError(f"Packaged mod does not exist: {artifact}")

    java_command = shutil.which(str(args.java))
    java = Path(java_command).resolve() if java_command else args.java.resolve()
    if not java.is_file():
        raise FileNotFoundError(f"Java executable does not exist: {java}")

    stamp = time.strftime("%Y%m%d-%H%M%S")
    server_dir = (args.work_root / f"{args.minecraft}-{stamp}").resolve()
    server_dir.mkdir(parents=True, exist_ok=False)

    installer = args.work_root.resolve() / f"quilt-installer-{INSTALLER_VERSION}.jar"
    download(
        "https://maven.quiltmc.org/repository/release/org/quiltmc/quilt-installer/"
        f"{INSTALLER_VERSION}/quilt-installer-{INSTALLER_VERSION}.jar",
        installer,
    )
    subprocess.run(
        [
            str(java),
            "-jar",
            str(installer),
            "install",
            "server",
            args.minecraft,
            args.loader,
            f"--install-dir={server_dir}",
            "--download-server",
        ],
        check=True,
    )

    mods = server_dir / "mods"
    mods.mkdir()
    shutil.copy2(artifact, mods / artifact.name)
    api_name = f"fabric-api-{args.fabric_api}.jar"
    download(
        "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/"
        f"{args.fabric_api}/{api_name}",
        mods / api_name,
    )
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (server_dir / "server.properties").write_text(
        "online-mode=false\nserver-port=0\nview-distance=2\nsimulation-distance=2\n",
        encoding="utf-8",
    )

    run_server(java, server_dir)
    print(f"PASS: Chestwise {args.minecraft} Fabric artifact on Quilt {args.loader}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, RuntimeError, subprocess.SubprocessError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        sys.exit(1)
