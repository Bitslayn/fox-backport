<h1 align="center"> FOX's Misc Figura Backport </h1>

# Features

Backports popular changes and fixes I've written for Figura 0.1.6 to Figura 0.1.5 such as:

* Searchable offline avatars https://github.com/FiguraMC/Figura/pull/449
* Render task fixes https://github.com/FiguraMC/Figura/pull/433 & https://github.com/FiguraMC/Figura/pull/462
* Popup menu usable on skulls https://github.com/FiguraMC/Figura/pull/468
* Quick sound settings in popup menu https://github.com/FiguraMC/Figura/pull/468

<i>This addon is not a replacement for the afformentioned PRs, and as such, is not intended for use with Figura 0.1.6</i>

## Searchable offline avatars

Figura for whatever reason explicitly prevented offline avatars from being searchable. Because changing permissions is important for players with skulls, this consequentially led to users naming their avatars using hoists allowing them to display at the top of the player list.

This change allows for offline avatars to be searchable in the permissions tab.

<img width="512" alt="image" src="https://github.com/user-attachments/assets/66d6dea8-a9f2-453a-b60f-2005d925d429" />

## Render task fixes

Fixes a bug causing render tasks, such as ItemTasks or SpriteTasks to become dark or tinted red. This bug is caused by other avatars setting the light level or effect overlays of a group ModelPart, causing all tasks in all avatars regardless of which avatar set this effect to become visually bugged.

A side effect of this bug is that tasks don't become properly shaded by the environment. This change fixes both these issues.

<img width="512" alt="image" src="https://github.com/user-attachments/assets/be2f96b3-b808-4f3a-a6ac-31a6fb6a359b" />

## Popup menu usable on skulls

Adds the avatar popup menu functionality to player skulls which have an avatar. This allows for quickly increasing the permissions of the skull's avatar, or reloading that avatar in case of an error.

<img width="512" alt="image" src="https://github.com/user-attachments/assets/4a6de338-05a1-4915-92c4-6f4939159224" />

## Quick sound settings in popup menu

Adds a button to the popup menu that cycles the volume through three different volume states: 100%, 50%, and 0%

<img width="512" alt="image" src="https://github.com/user-attachments/assets/30e015b0-9acd-4255-b003-f8eff150adb6" />

# Development

## Setting Up a Development Environment
1. Use your favorite Python package manager to install the requirements from `requirements.txt`. python >= 3.12.
2. Select a variant to build using `python generate.py switch <variant_id>`. (`fabric_forge_1` (1.20.1 - 1.20.4) is a good starting point, but if you want all three modloaders at once, try `all_2` (1.20.6 - 1.21.1).) The list of variants is in `rules.json`.

## Publishing
1. Delete the `fabric/build/libs`, `forge/build/libs`, and `neoforge/build/libs` folders.
2. Close IntelliJ IDEA, or turn off auto-sync (gradle tool window > settings button > auto-sync settings... > uncheck the box)
3. Run `python generate.py build --version <mod_semver>`. This will take a while as it builds the project 7 times (one for each version split). (adjust version number as needed)
4. Run `python publish.py` to upload to Modrinth.
