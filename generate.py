import os
from jinja2 import Environment, FileSystemLoader, select_autoescape
import click
import json
from rich import print as rp
from subprocess import call

env = Environment(loader=FileSystemLoader("."), autoescape=select_autoescape())
properties_template = env.get_template("gradle.properties.j2")


def expand_variant(variant: dict):
    platforms = []
    if "is_fabric" in variant and variant["is_fabric"]:
        platforms.append("fabric")
    if "is_forge" in variant and variant["is_forge"]:
        platforms.append("forge")
    if "is_neo" in variant and variant["is_neo"]:
        platforms.append("neoforge")
    variant['platforms'] = ','.join(platforms)


def get_sets() -> dict[str, dict]:
    with open('rules.json', encoding='utf-8') as f:
        data = json.load(f)
    variants: dict[str, dict] = data['variants']
    reified: dict[str, dict] = {}
    for k, variant in variants.items():
        reified[k] = data['common'].copy()
        reified[k].update(variant)
        expand_variant(reified[k])
    return reified


@click.group()
def main():
    pass


def _switch(variant_name: str, version: str):
    variant_opt = get_sets()[variant_name]
    specific = {
        'version': version
    }
    specific.update(variant_opt)
    result = properties_template.render(**specific)
    with open('gradle.properties', 'w', encoding='utf-8') as f:
        f.write("#\n")
        f.write("# THIS FILE IS GENERATED! Use `generate.py switch` to change variants.\n")
        f.write("#\n")
        f.write("\n")
        f.write(result)
    rp(f'[green]Switched to variant [bold]{variant_name}[/].[/]')

@click.command()
@click.argument('variant_name')
@click.option('--version', default='1.0-SNAPSHOT')
def switch(variant_name: str, version: str):
    _switch(variant_name, version)


@click.command()
@click.option('--version', default='1.0-SNAPSHOT')
def build(version: str):
    variants = get_sets()
    gradlew = os.path.abspath('./gradlew.bat' if os.name == 'nt' else './gradlew')
    for name, variant in variants.items():
        rp(f'[bright_blue]Building [bold]{name}[/][/]')
        _switch(name, version)
        call([
            gradlew, 'build'
        ])


main.add_command(switch)
main.add_command(build)

if __name__ == "__main__":
    main()
