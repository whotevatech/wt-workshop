# 0.7.0-beta.1 + BeeBlock addon 0.1.0-beta.1 (first public beta)

- First public release. Gameplay is the same as 0.7.0-alpha.1 / addon 0.1.0-alpha.1.
- Each jar now includes the MIT licence, third-party notice and a mod logo.
- The addon requires WT Workshop 0.7.x (from 0.7.0-beta.1; 0.8 pre-releases are excluded).
- The GameTest structure is no longer packed into the release jar.

# 0.7.0-alpha.1 + BeeBlock addon 0.1.0-alpha.1

- Added a namespaced Java addon modifier API, optional per-tool art and automatic station/tooltip/viewer integration.
- Added persistent addon slot kinds so unavailable addons cannot silently free spent slots.
- Added crafting-container returns on station transactions.
- Added separate configurable Waxed durability protection and Honeybound melee slowing addon.
- Included optional Forestry/Productive Bees ingredients via tags, with vanilla fallbacks.
- Preserved existing tool saves, modifier IDs, expansions and quest milestones.

# 0.6.0-alpha.1

- Show exact station ingredient costs, slot changes, modifier descriptions and partial progress before taking results.
- Widen the comparison panel and provide full details on hover.
- Report actual upgrade/ability slots earned and configured stat bonuses at level-up, with persistent chat, action-bar text and sound.
- Preserve wear ratio when a level changes durability; ordinary XP does not alter wear.
- Add persistent server-side workshop milestones for real repairs and assembled-tool progress.
- Include an optional 13-quest BeeBlock chapter and guarded installer, with existing-tool catch-up.
- FTB Quests remains optional and is not bundled.

# 0.5.0-alpha.1

- Added three ordered, single-use expansion braces and crafting recipes.
- Added configurable upgrade/ability slot bonuses, alongside leveling.
- Added Siphoning melee life steal with health-damage accounting, cooldown, cap and PvP configuration.
- Added Sharpness, Reinforced, Fiery and Siphoning artwork; seven accent types now stack through additive model passes.
- Added native JEI/EMI expansion pages and updated modifier limits/descriptions.
- Preserved old tool saves; persisted expansions through saving, networking and tool data changes.
- Fixed station output leaving the original tool behind during modification, repairs, removal and swaps.
- Added engine regression coverage for expansion transactions, persistence and real combat healing.

# 0.4.0-alpha.1

- Added native JEI/EMI recipe lookup with seven categories and 466 default entries.
- Added searchable internal molten ingredients with exact amounts and reusable pattern/cast indicators.
- Added modifier costs, slot requirements, prerequisites and conflict information.
- Added example tool assembly pages.
- Added combined Redstone/Haste, Lapis/Luck and Diamond overlays for all eight tools.
- Retained compatibility with existing tools, saved state and material tints.
- Added engine checks for recipe display accuracy and overlay save/load behaviour.
- Recorded future modifier and slot-expansion ideas in ROADMAP.md.

# 0.3.0-alpha.3

- Redrawn axe, axe/shovel heads, sword blades, handles, guards and binding parts.
- Updated matching patterns and casts.
- Open wooden workstation models, distinct tabletop details and recessed smeltery basin.
- Warm wood/parchment menu styling and readable trait colours.
- Updated collision and selection shapes to match the new models.

# 0.3.0-alpha.2

- Fixed heavy tools triggering area mining before a canceled center-block break.
- Reset melting progress when changing inputs.
- Reset casting progress when changing molds.
- Redrew hoe, hammer, excavator and broad axe with matching parts, patterns and casts.
- Recovered a buildable source project from the supplied JAR.
