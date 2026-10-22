# WT Workshop 0.7 + BeeBlock addon 0.1 validation

Minecraft 1.21.1 / NeoForge 21.1.263 / Java 21.

## Engine coverage
Base-only run: 27 required tests passed, with 493 default viewer recipes.
Addon run: 32 required tests passed (the conditional Forestry test is inactive when Forestry is absent). Gameplay tests verify Waxed/Honeybound application, exact costs, slot restrictions, partial progress, maximum levels, normal/shift-click glass returns, reduced real wear, absorbed-hit exclusion and Honeybound cooldown/expiry. The 504-entry catalog is compared against real station logic.
An unavailable-addon fixture retains its ability slot and namespaced data through item save/load and network roundtrip. Data mutations and sponge removal preserve expansion state. Existing 0.6 leveling, repair, milestone, smeltery, heavy-tool, migration and modifier persistence checks remain included.

## Client compatibility
- Real connected-player Forestry 3.0.0-alpha8 + Patchouli 1.21.1-93: beeswax and honey-drop station transactions succeeded and consumed the original tool exactly once.
- Station previews and original wax/honey accents were inspected at large and inventory sizes, separately and together.
- Native EMI 1.1.24 and JEI 19.69.0.457 client runs registered/rendered the addon catalog. EMI registered 504 entries. Inspected the corrected Waxed percentage text in the native JEI page.
- Standalone base client startup and station/level-up capture passed with neither addon nor Forestry installed.
- The base has no hard reference to the BeeBlock addon or Forestry. Optional mod ingredients are tags, and vanilla fallback ingredients remain available.
- Development tests/capture helpers are excluded from both release JARs.

## Test harness limits and upstream notices
Forestry cannot be used with this engine harness's simulated ServerPlayer connections: its login handler tries to send forestry:taxon_sync to a fake client connection and throws. Eight fake-player tests failed in that combined harness run for this reason. Addon gameplay tests run without Forestry; Forestry item compatibility was instead verified with actual connected players in the isolated client.
The isolated Forestry client also logged an upstream Patchouli foresters_manual mail/letters category error. It did not stop the connected-player station checks. This addon does not modify that book.
The complete BeeBlock pack, dedicated multiplayer behavior and every configurable balance combination still need survival playtesting. The conditional Forestry ingredient GameTest only asserts those optional item IDs when Forestry is installed.
