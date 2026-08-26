#!/usr/bin/env python3
"""Boot a packaged Chestwise JAR on an official loader dedicated server."""

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


FABRIC_INSTALLER_VERSION = "1.1.1"
STARTUP_TIMEOUT_SECONDS = 240


def download(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.is_file():
        return
    print(f"Downloading {url}", flush=True)
    request = urllib.request.Request(url, headers={"User-Agent": "Chestwise-release-smoke/1.0"})
    with urllib.request.urlopen(request, timeout=90) as response:
        destination.write_bytes(response.read())


def stream_output(process: subprocess.Popen[str], lines: queue.Queue[str | None]) -> None:
    assert process.stdout is not None
    for line in process.stdout:
        lines.put(line)
    lines.put(None)


def write_log(line: str) -> None:
    sys.stdout.buffer.write(line.encode("utf-8", errors="replace"))
    sys.stdout.buffer.flush()


def run_server(command: list[str], server_dir: Path, label: str) -> None:
    process = subprocess.Popen(
        command,
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
            ready |= "Done (" in line and "For help" in line
            if initialized and ready:
                break
        if not initialized:
            raise RuntimeError(f"{label} never initialized the packaged Chestwise mod")
        if not ready:
            raise RuntimeError(f"{label} did not reach its ready state before timeout")
        assert process.stdin is not None
        process.stdin.write("stop\n")
        process.stdin.flush()
        exit_code = process.wait(timeout=60)
        if exit_code != 0:
            raise RuntimeError(f"{label} exited with code {exit_code}")
    finally:
        if process.poll() is None:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()


def install_fabric(args: argparse.Namespace, java: Path, server_dir: Path) -> list[str]:
    server_jar = server_dir / "fabric-server-launch.jar"
    download(
        "https://meta.fabricmc.net/v2/versions/loader/"
        f"{args.minecraft}/{args.loader_version}/{FABRIC_INSTALLER_VERSION}/server/jar",
        server_jar,
    )
    if not args.fabric_api:
        raise ValueError("--fabric-api is required for Fabric")
    api_name = f"fabric-api-{args.fabric_api}.jar"
    download(
        "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/"
        f"{args.fabric_api}/{api_name}",
        server_dir / "mods" / api_name,
    )
    return [str(java), "-Xms512M", "-Xmx1G", "-jar", str(server_jar), "nogui"]


def install_forge_family(args: argparse.Namespace, java: Path, server_dir: Path) -> list[str]:
    if args.loader == "forge":
        artifact = "forge"
        base = "https://maven.minecraftforge.net/net/minecraftforge/forge"
    elif args.minecraft == "1.20.1":
        artifact = "forge"
        base = "https://maven.neoforged.net/releases/net/neoforged/forge"
    else:
        artifact = "neoforge"
        base = "https://maven.neoforged.net/releases/net/neoforged/neoforge"
    installer_name = f"{artifact}-{args.loader_version}-installer.jar"
    installer = args.work_root.resolve() / "installers" / installer_name
    download(f"{base}/{args.loader_version}/{installer_name}", installer)
    installation = subprocess.run(
        [str(java), "-jar", str(installer), "--installServer", str(server_dir)],
        check=True,
        cwd=server_dir,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    installer_lines = installation.stdout.splitlines()
    for line in installer_lines[-5:]:
        write_log(line + "\n")
    argument_name = "win_args.txt" if sys.platform == "win32" else "unix_args.txt"
    argument_files = list((server_dir / "libraries").rglob(argument_name))
    if len(argument_files) != 1:
        raise RuntimeError(f"installer produced {len(argument_files)} {argument_name} files")
    loader_arguments = argument_files[0].relative_to(server_dir)
    return [str(java), "@user_jvm_args.txt", f"@{loader_arguments}", "nogui"]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--loader", choices=("fabric", "forge", "neoforge"), required=True)
    parser.add_argument("--minecraft", required=True)
    parser.add_argument("--loader-version", required=True)
    parser.add_argument("--artifact", required=True, type=Path)
    parser.add_argument("--fabric-api")
    parser.add_argument("--java", default="java", type=Path)
    parser.add_argument("--work-root", default=Path("build/loader-smoke"), type=Path)
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
    server_dir = (args.work_root / f"{args.minecraft}-{args.loader}-{stamp}").resolve()
    (server_dir / "mods").mkdir(parents=True, exist_ok=False)
    shutil.copy2(artifact, server_dir / "mods" / artifact.name)
    (server_dir / "eula.txt").write_text("eula=true\n", encoding="utf-8")
    (server_dir / "server.properties").write_text(
        "online-mode=false\nserver-port=0\nview-distance=2\nsimulation-distance=2\n",
        encoding="utf-8",
    )
    command = install_fabric(args, java, server_dir) if args.loader == "fabric" else install_forge_family(args, java, server_dir)
    label = f"{args.loader} {args.loader_version} / Minecraft {args.minecraft}"
    run_server(command, server_dir, label)
    print(f"PASS: Chestwise packaged artifact on {label}")
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, RuntimeError, subprocess.SubprocessError) as error:
        print(f"FAIL: {error}", file=sys.stderr)
        sys.exit(1)
