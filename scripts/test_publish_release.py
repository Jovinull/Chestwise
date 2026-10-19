import contextlib
import hashlib
import io
import json
import os
import tempfile
import unittest
import zipfile
from pathlib import Path
from unittest.mock import patch

from scripts import publish_release


def write_test_artifacts(directory: Path) -> None:
    lines = []
    for artifact in publish_release.ARTIFACTS:
        path = directory / artifact.filename
        metadata_path = publish_release._metadata_path(artifact)
        if artifact.loader == "fabric":
            metadata = json.dumps(
                {
                    "id": "chestwise",
                    "version": "0.1.0",
                    "depends": {"minecraft": artifact.minecraft, "fabric-api": ">=0"},
                    "entrypoints": {"main": ["dev.chestwise.Chestwise"]},
                }
            )
        else:
            mod_version = (
                "${file.jarVersion}"
                if artifact.loader == "neoforge" and artifact.minecraft == "1.20.1"
                else "0.1.0"
            )
            minecraft_range = "[26.3,26.4)" if artifact.loader == "forge" and artifact.minecraft == "26.3" else f"[{artifact.minecraft}]"
            metadata = (
                'modId = "chestwise"\n'
                f'version = "{mod_version}"\n'
                '[[dependencies.chestwise]]\n'
                'modId = "minecraft"\n'
                f'versionRange = "{minecraft_range}"\n'
            )
            if artifact.loader == "forge" and artifact.minecraft == "26.3":
                metadata += (
                    '[[dependencies.chestwise]]\n'
                    'modId = "forge"\n'
                    'versionRange = "[66,)"\n'
                )
        with zipfile.ZipFile(path, "w") as jar:
            jar.writestr(metadata_path, metadata)
            if artifact.loader == "neoforge" and artifact.minecraft == "1.20.1":
                jar.writestr("META-INF/MANIFEST.MF", "Implementation-Version: 0.1.0\n")
        lines.append(f"{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}")
    (directory / "SHA256SUMS").write_text("\n".join(lines) + "\n", encoding="utf-8")


class ReleasePackageTests(unittest.TestCase):
    def test_dry_run_resolves_nine_artifacts_and_eighteen_upload_mappings(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            artifact_dir = Path(temporary)
            write_test_artifacts(artifact_dir)
            output = io.StringIO()
            with contextlib.redirect_stdout(output):
                result = publish_release.main(["--artifact-dir", str(artifact_dir)])

        self.assertEqual(result, 0)
        self.assertIn("9 artifacts, 18 staged platform uploads", output.getvalue())
        self.assertIn("0.1.0+mc26.3-neoforge", output.getvalue())

    def test_fabric_metadata_has_exact_dependency_mapping(self) -> None:
        artifact = publish_release.Artifact("1.21.1", "fabric", "unused.jar")
        payload = publish_release._modrinth_payload(artifact, "project-id")
        self.assertEqual(payload["game_versions"], ["1.21.1"])
        self.assertEqual(payload["loaders"], ["fabric"])
        self.assertEqual(
            payload["dependencies"],
            [{"project_id": publish_release.FABRIC_API_PROJECT_ID, "dependency_type": "required"}],
        )
        self.assertEqual(payload["version_type"], "beta")
        self.assertEqual(payload["status"], "draft")

    def test_legacy_neoforge_uses_legacy_mod_metadata_path(self) -> None:
        artifact = publish_release.Artifact("1.20.1", "neoforge", "unused.jar")
        self.assertEqual(publish_release._metadata_path(artifact), "META-INF/mods.toml")

    def test_modern_neoforge_uses_neoforge_metadata_path(self) -> None:
        artifact = publish_release.Artifact("26.3", "neoforge", "unused.jar")
        self.assertEqual(publish_release._metadata_path(artifact), "META-INF/neoforge.mods.toml")

    def test_non_fabric_metadata_has_no_fabric_api_dependency(self) -> None:
        artifact = publish_release.Artifact("26.3", "neoforge", "unused.jar")
        self.assertEqual(publish_release._modrinth_payload(artifact, "project-id")["dependencies"], [])
        self.assertEqual(publish_release._curseforge_payload(artifact)["relations"]["projects"], [])

    def test_bad_hash_aborts_package_validation(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            artifact_dir = Path(temporary)
            write_test_artifacts(artifact_dir)
            sums_path = artifact_dir / "SHA256SUMS"
            lines = sums_path.read_text(encoding="utf-8").splitlines()
            lines[0] = "0" * 64 + lines[0][64:]
            sums_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
            with self.assertRaisesRegex(publish_release.ReleaseValidationError, "SHA-256 mismatch"):
                publish_release.validate_package(artifact_dir, "0.1.0")

    def test_missing_credentials_fail_before_any_network_request(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            artifact_dir = Path(temporary)
            write_test_artifacts(artifact_dir)
            environment = {"GITHUB_EVENT_NAME": "push", "GITHUB_REF": "refs/tags/v0.1.0"}
            output = io.StringIO()
            with patch.dict(os.environ, environment, clear=True):
                with patch.object(publish_release.urllib.request, "urlopen") as urlopen:
                    with contextlib.redirect_stderr(output):
                        result = publish_release.main(
                            ["--artifact-dir", str(artifact_dir), "--publish"]
                        )

        self.assertEqual(result, 2)
        self.assertIn("no upload was attempted", output.getvalue())
        urlopen.assert_not_called()

    def test_publish_mode_rejects_branch_dispatch_even_with_credentials(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            artifact_dir = Path(temporary)
            write_test_artifacts(artifact_dir)
            environment = {
                "GITHUB_EVENT_NAME": "workflow_dispatch",
                "GITHUB_REF": "refs/heads/release/0.1.0-rc",
                "CHESTWISE_MODRINTH_PROJECT_ID": "modrinth-id",
                "CHESTWISE_CURSEFORGE_PROJECT_ID": "curseforge-id",
                "MODRINTH_TOKEN": "not-a-real-token",
                "CURSEFORGE_TOKEN": "not-a-real-token",
            }
            output = io.StringIO()
            with patch.dict(os.environ, environment, clear=True):
                with patch.object(publish_release.urllib.request, "urlopen") as urlopen:
                    with contextlib.redirect_stderr(output):
                        result = publish_release.main(
                            ["--artifact-dir", str(artifact_dir), "--publish"]
                        )

        self.assertEqual(result, 2)
        self.assertIn("only on a push of the exact v0.1.0 tag", output.getvalue())
        urlopen.assert_not_called()

    def test_workflow_dispatch_on_release_tag_does_not_publish(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            artifact_dir = Path(temporary)
            write_test_artifacts(artifact_dir)
            environment = {
                "GITHUB_EVENT_NAME": "workflow_dispatch",
                "GITHUB_REF": "refs/tags/v0.1.0",
                "CHESTWISE_MODRINTH_PROJECT_ID": "modrinth-id",
                "CHESTWISE_CURSEFORGE_PROJECT_ID": "curseforge-id",
                "MODRINTH_TOKEN": "not-a-real-token",
                "CURSEFORGE_TOKEN": "not-a-real-token",
            }
            output = io.StringIO()
            with patch.dict(os.environ, environment, clear=True):
                with patch.object(publish_release.urllib.request, "urlopen") as urlopen:
                    with contextlib.redirect_stderr(output):
                        result = publish_release.main(
                            ["--artifact-dir", str(artifact_dir), "--publish"]
                        )

        self.assertEqual(result, 2)
        self.assertIn("only on a push of the exact v0.1.0 tag", output.getvalue())
        urlopen.assert_not_called()


if __name__ == "__main__":
    unittest.main()
