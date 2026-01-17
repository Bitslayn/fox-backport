## Setting Up a Development Environment
1. Use your favorite Python package manager to install the requirements from `requirements.txt`. python >= 3.12.
2. Select a variant to build using `python generate.py switch <variant_id>`. (`fabric_forge_1` (1.20.1 - 1.20.4) is a good starting point, but if you want all three modloaders at once, try `all_2` (1.20.6 - 1.21.1).) The list of variants is in `rules.json`.

## Publishing
1. Delete the `fabric/build/libs`, `forge/build/libs`, and `neoforge/build/libs` folders.
2. Close IntelliJ IDEA, or turn off auto-sync (gradle tool window > settings button > auto-sync settings... > uncheck the box)
3. Run `python generate.py build --version 1.2.3`. This will take a while as it builds the project 7 times (one for each version split). (adjust version number as needed)
4. Run `python publish.py` to upload to Modrinth.
