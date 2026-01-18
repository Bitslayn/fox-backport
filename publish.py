from collections import defaultdict
import itertools
import json
import shutil
import tomllib
from typing import Literal, NamedTuple, NoReturn
from zipfile import ZipFile
import requests
from requests_toolbelt import MultipartEncoder
from rich import print as rp
from pathlib import Path


SOURCES = [
    Path("fabric/build/libs"),
    Path("forge/build/libs"),
    Path("neoforge/build/libs"),
]


class Conf(NamedTuple):
    api_key: str
    project_id: str


class BuildMeta(NamedTuple):
    aux: bool


def die(reason: str, exitcode: int = 1) -> NoReturn:
    rp(f"[red]{reason}[/]")
    exit(exitcode)


def load_config() -> Conf:
    path = Path("./mr-info.json")
    if not path.exists():
        template_path = Path("./mr-info.json.template")
        if not template_path.exists():
            die("Missing Modrinth configuration file (mr-info.json). Also, the template is missing.")
        shutil.copy(template_path, path)
        die("Missing Modrinth configuration file (mr-info.json).\n"
        "[green](A new configuration file has been created based on the template.)[/]")
    if not path.is_file():
        die("Modrinth configuration file (mr-info.json) is not a file")
    
    with open(path, "r") as f:
        data = json.load(f)

    if "api_key" not in data or data["api_key"] == "PUT_API_KEY_HERE":
        die("Modrinth configuration file (mr-info.json): bad api_key")

    if "project_id" not in data or data["project_id"] == "PROJECT_ID_NOT_SLUG":
        die("Modrinth configuration file (mr-info.json): bad project_id")
    
    return Conf(data["api_key"], data["project_id"])


def modloader(zip: ZipFile) -> tuple[str, str]:
    try:
        with zip.open("fabric.mod.json") as f:
            data = json.load(f)
        return "fabric", data["depends"]["minecraft"]
    except KeyError:
        pass
    try:
        with zip.open("META-INF/neoforge.mods.toml", "r") as f:
            data = tomllib.load(f)
        deps: list[dict] = next(iter(data["dependencies"].values()))
        mc = next(x for x in deps if x["modId"] == "minecraft")
        return "neoforge", mc["versionRange"]
    except KeyError:
        pass
    try:
        with zip.open("META-INF/mods.toml", "r") as f:
            data = tomllib.load(f)
        deps: list[dict] = next(iter(data["dependencies"].values()))
        mc = next(x for x in deps if x["modId"] == "minecraft")
        return "forge", mc["versionRange"]
    except KeyError:
        pass
    raise KeyError(f"Cannot determine modloader for {zip.filename}")


# see rules.json
MINECRAFT = {
    # fabric_forge_1
    "[1.20.1,1.20.4]": ["1.20.1", "1.20.2", "1.20.4"],
    ">=1.20.1 <=1.20.4": ["1.20.1", "1.20.2", "1.20.4"],
    # neo_1
    "[1.20.2,1.20.4]": ["1.20.2", "1.20.4"],
    # all_2
    "[1.20.6,1.21.1]": ["1.20.6", "1.21.1"],
    ">=1.20.6 <=1.21.1": ["1.20.6", "1.21.1"],
    # all_3
    "[1.21.3,1.21.4]": ["1.21.3", "1.21.4"],
    ">=1.21.3 <=1.21.4": ["1.21.3", "1.21.4"],
    # fabric_4
    "1.21.5": ["1.21.5"],
    # fabric_5
    ">=1.21.6 <=1.21.8": ["1.21.6", "1.21.8"],
    # fabric_6
    "1.21.10": ["1.21.10"]
}


def get_build_meta(path: Path) -> None | tuple[str, list[str]]:
    if path.stem.endswith("-dev-shadow") or path.stem.endswith("-sources"):
        return None

    with ZipFile(path) as zip:
        # Determine what modloader this is for based on presence of metadata files
        ml, ver = modloader(zip)
        flat_versions = MINECRAFT[ver]
        return ml, flat_versions


STAGING = False
MR_API_URL = f"https://{'staging-' if STAGING else ''}api.modrinth.com/v3"
counters = defaultdict(int)


def publish(session: requests.Session, project: str, file: Path, version: str, modloader: str, mc: list[str], changelog: str | None):
    publish_info = {
        "version_number": f"{version}+{mc[0]}-{modloader}",
        "name": f"v{version} {modloader.capitalize()} {', '.join(mc)}",
        "dependencies": [],
        "game_versions": mc,
        "version_type": "release",
        "loaders": [modloader],
        "featured": False,
        "project_id": project,
        # "status": "listed",
        "file_parts": ["file.jar"],
        "file_types": {"file.jar": None},
        "primary_file": "file.jar",
        "changelog": changelog,
        "environment": "client_only"
    }
    if not STAGING:
        publish_info["dependencies"].append({"dependency_type": "required", "project_id": "s9gIPDom"})
    with open(file, "rb") as f:
        m = MultipartEncoder(
            fields = {
                'data': json.dumps(publish_info),
                'file.jar': (file.name, f, 'application/java-archive')
            }
        )
        # rp(m.to_string())
        # return
        req = session.request(
            "POST",
            f"{MR_API_URL}/version",
            data = m,
            headers = {'Content-Type': m.content_type}
        )
        if req.status_code != 200:
            rp(f"[bold red]{req.status_code}[/] [red]{req.text}[/]")
        else:
            rp(f"[bold green]{req.status_code}[/]")


def main():
    cfg = load_config()

    for source in SOURCES:
        if not source.exists():
            die(f"Can't find builds in {source}. Did you run [green]generate.py build[/] yet?")

    changelog_path = Path("mr-changelog.md")
    if not changelog_path.exists():
        changelog = None
        rp("[yellow]No changelog found. Create mr-changelog.md to specify changelog for uploaded versions.")
    else:
        with open(changelog_path, encoding="utf-8", mode="r") as f:
            changelog = f.read()

    session = requests.Session()
    session.headers["User-Agent"] = "mr-publisher python-requests"
    session.headers["Authorization"] = cfg.api_key

    version = input("what version is this? (semver, like '1.0.0') ")
    
    for file in itertools.chain(*[x.glob("*.jar") for x in SOURCES]):
        meta = get_build_meta(file)
        if meta is None: 
            continue
        modloader, mc = meta
        rp(meta)
    
        publish(
            session, cfg.project_id, file,
            version, modloader, mc, changelog
        )


if __name__ == "__main__":
    main()