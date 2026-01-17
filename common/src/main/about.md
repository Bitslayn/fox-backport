## what's going on here?
Basically, it's a cheap version of Stonecutter (the Minecraft build system). Variants (`rules.json`) can define differing source sets to use with the `commonSrcSets` key; only those specified will be used to build (along with the default `java`.)

This is used to achieve version-specific code while still having a common core.

Generally, only 1 of each category should be used at once (i.e. `gfx1;item1` is OK, `gfx1;gfx2;item1` is not), but there is nothing actually _stopping_ this from happening. Try not to introduce conflicts.

## specifics?
if you change `rules.json` this may no longer match

| Minecraft version(s) |  gfx   |  item   |
|---------------------:|:------:|:-------:|
|      1.20.1 - 1.20.4 | `gfx1` | `item1` |
|      1.20.6 - 1.21.1 | `gfx1` | `item2` |
|      1.21.3 - 1.21.4 | `gfx2` | `item2` |
|               1.21.5 | `gfx3` | `item2` |
|               1.21.6 | `gfx4` | `item2` |
|              1.21.10 | `gfx4` | `item3` |