# WT Workshop 0.7.0-alpha.1

Minecraft 1.21.1 / NeoForge. Built and tested with NeoForge 21.1.263 and Java 21.

## Install
Replace the existing WT Workshop JAR with mods/wt-workshop-0.7.0-alpha.1.jar. Install one version at a time on both client and server. Do not place the source folder in mods.

## BeeBlock addon
Install the new base JAR and the separate wt-beeblock-workshop-0.1.0-alpha.1.jar on client and server. Replace your old WT Workshop base JAR; keep only one base version. Your existing tools and 13-quest chapter continue to work.
Other packs can use the base by itself, or use this addon with vanilla ingredients and their own ingredient tags.

| Modifier | Ingredients per level | Slot | Default effect |
| --- | --- | --- | --- |
| Waxed | 8 Forestry beeswax, Productive Bees wax or vanilla honeycomb, mixed as desired | 1 upgrade | Each level prevents 10% of remaining durability wear; maximum 3 levels |
| Honeybound | 8 Forestry honey drops or vanilla honey bottles, mixed as desired | 1 ability | Slowness after actual melee health damage, 2 seconds, strength equal to level; maximum 2 levels |

Honeybound works on swords, axes and broad axes. It has a shared 20-tick cooldown and excludes player targets by default. Absorbed/blocked hits do not trigger it. Honey bottles return glass bottles. Partial quantities can be applied over several station visits.

Waxed combines with material traits/Reinforced while retaining a 90% global saving cap. Each completed modifier level uses another slot.
Both modifiers have original additive tool accents, tooltips, station previews and native JEI/EMI lookup entries.

Config: wt_beeblock_workshop-server.toml in the world's serverconfig (or defaultconfigs for new worlds). Adjust maximum levels, wax saving chance, honey duration/cooldown and PvP. Restart after edits. Maximum level 0 disables applying/using the corresponding effect. Existing paid levels and slots stay recorded.
Ingredient tags are wt_beeblock_workshop:wax and wt_beeblock_workshop:honey. The addon does not require Forestry/Productive Bees; absent optional entries are ignored. See ADDON-API.md for building your own addon.

## Feedback features from 0.6
The Tool Station shows exact ingredient consumption, modifier effects, slot costs and partial progress before taking the result. Hover below the comparison table for full details. Successful changes still happen only when taking the output.
Level-up feedback now includes a persistent chat summary of upgrade slots, ability slots and the configured speed/durability bonus, plus the existing action-bar message and sound. Wear ratio is preserved when leveling changes maximum durability.

## Optional BeeBlock quest chapter
This download includes a 13-step WT Workshop chapter: benches, patterns, assembled pickaxe, real repairs, Haste, level milestones, smeltery, casting, heavy tools and expansion braces.
Replace the mod JAR first. Close Minecraft, then run Install-Quests.ps1 from the extracted release folder. Its default target is D:\Curse\Instances\WT BeeBlock; pass -InstancePath for another copy. It installs only overrides/config/ftbquests/quests/chapters/15_wt_workshop.snbt. Restart fully. For manual installation, merge overrides into the instance/server root.

The installer does not replace your old JAR automatically. Install the new JAR on client and server, and the chapter on the server for multiplayer. Keep one WT Workshop JAR version installed.
Existing assembled tools carried in inventory or offhand are recognized automatically for pickaxe, level, Haste, expansion and heavy-tool milestones. An actual repair must be performed after updating. Old quest IDs and chapters are untouched. Quest rewards are small amounts of normal player XP, not tool XP.
The chapter belongs to BeeBlock's existing side-progress group. FTB Quests is optional for WT Workshop itself; no FTB binaries are bundled.

## Recipe lookup and visible modifiers
- Native JEI and native EMI pages for pattern carving, part building, example tool assembly, melting, alloying, casting, modifiers and slot expansions.
- The default data produces 493 entries. Pages read the live material/melting/alloy recipes and modifier rules.
- Molten ingredients show exact internal units: 90 units = one ingot. They are searchable recipe ingredients, not obtainable buckets.
- Reusable patterns and casts are shown separately and labelled. Gold cast-making correctly shows its consumed template.
- Modifier pages show ingredient amounts, slot type, maximum level, prerequisites and conflicts. Tool assembly and modifier examples use a representative iron/wood/string build. Compatible builds can use the same rules.
- Completed Haste, Luck, Diamond, Sharpness, Reinforced, Fiery and Siphoning modifiers have additive artwork on all eight tool types, retaining material colours.
- Partial modifier progress has no completed overlay. Existing tools display their recorded modifiers automatically; removal updates the appearance.
- Other modifiers retain their gameplay effects; their individual artwork is future work.


## Expansion braces and Siphoning
Apply crafted braces in the Tool Station material slot, in order: Copper, Gilded, Astral. Each tier is permanent and can be applied once per tool. Each adds one upgrade slot; Astral also adds one ability slot. Expansion does not cost an existing slot. Leveling still grants its usual slots. Sponge removes modifiers, never a paid expansion.

| Brace | Shaped recipe ingredients |
| --- | --- |
| Copper | 4 copper ingots, 2 diamonds, 2 amethyst shards, 1 iron ingot |
| Gilded | 4 gold ingots, 2 emeralds, 2 obsidian, 1 iron block |
| Astral | 4 amethyst bronze ingots, 1 nether star, 2 diamonds, 1 iron block, 1 netherite ingot |

Use vanilla crafting recipe lookup for the layouts and the new Slot Expansions category for application order and bonuses.

Siphoning uses four ghast tears per level and one ability slot per completed level, on swords, axes and broad axes. Default maximum level is three. It restores 5% of actual melee health damage per level, capped at one heart per proc, with a shared ten-tick cooldown. Absorption and overkill do not inflate healing. Player targets are excluded by default.

Pack configuration is in the generated wt_workshop-server.toml server config (world/serverconfig, or your pack's defaultconfigs template). The expansions section controls maximumTier (0–3), upgradeSlotsPerTier and astralAbilitySlots. The siphoning section controls maximumLevel (0 disables), healingFractionPerLevel, healingCap (health points), cooldownTicks and affectsPlayers. Lowering the expansion purchase limit retains existing paid tiers; changing slot bonuses changes their effect. Restart the world after configuration changes.

This update also fixes a Tool Station duplication bug affecting repair, modification, removal and part replacement: taking the result now consumes the original tool exactly once.

## Viewer setup
JEI and EMI are optional. The mod also works with neither installed. When both are present, the native EMI pages take priority to prevent duplicate WT Workshop pages. Neither viewer is bundled with this download.

Tested: EMI 1.1.24+1.21.1; JEI 19.21.0.247 and 19.69.0.457. The combined test used EMI 1.1.24 and JEI 19.21.0.247.

Use your viewer's recipe and usage shortcuts on parts, patterns, casts, materials, molten ingredients or tools. The station blocks are category catalysts. These are lookup pages; automatic inventory filling is not implemented.

## Visual refinements in 0.3.0-alpha.3
- Redrawn axe and loose axe heads, shovel heads, sword blades, handles, tough handles, crossguards, bindings and tough bindings.
- Matching pattern and cast silhouettes for the redrawn parts. All material tints still apply.
- Tool Station and Part Builder are open wooden workbenches with a thick worktop, legs and braces; each has its own tabletop detail.
- Smeltery has an actual recessed basin and stone rim.
- Warm parchment panels, wooden frame colours and darker readable coloured text across the three menus.
- New block collision/selection shapes match the open models.
- Retains the 0.3.0-alpha.2 area-mining and smeltery timing fixes.

## Existing tools
The existing 0.3 data components and registry IDs are retained. The original migration from 0.2 custom data is retained and tested for materials, modifiers, level, XP, custom name, unrelated custom data and wear ratio.

## Source
This editable project was reconstructed from the supplied compiled 0.3.0-alpha.1 JAR using Vineflower 1.10.1, then fixed and rebuilt. It is not your original source project: comments, original formatting and original build setup could not be recovered. Generic type information required a few compilation repairs. Development tests and screenshot helpers are excluded from the release JAR.

Build using Java 21: gradlew.bat build
Engine tests: gradlew.bat runGameTestServer

See VALIDATION.md for the exact test coverage and limits.
