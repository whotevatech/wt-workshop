# WT Workshop addon API 0.7 (alpha)

The base mod contains no BeeBlock item dependencies. A separate Java addon registers namespaced modifier definitions with net.wtworkshop.api.AddonModifiers during its mod constructor. The included beeblock-addon source is the working example.

## Definition fields
AddonModifier supplies a ResourceLocation ID, upgrade/ability slot kind, live maximum-level supplier, positive points-per-level, Ingredient supplier, allowed-tool predicate, localized description supplier, text colour, conflict ID set, durability-saving supplier and optional per-tool overlay model function.

Register on both client and server, with a required AFTER dependency on WT Workshop. Registration freezes at common setup. IDs must use your own namespace. A completed level takes one slot. Each ingredient item contributes one point; partial batches are saved. A zero maximum disables new applications and the included effects; historical data/occupied slots are retained.

Use an Ingredient backed by your own item tag to let packs replace ingredients. Optional mod items belong in tags with required:false. Ingredient suppliers run after tags are available, rather than resolving optional registry IDs during construction. Do not overlap built-in modifier ingredients: repair and built-in actions have priority. If two addon definitions accept one ingredient, the station rejects it with an explanatory message.

## Included hooks
The station handles applicability, limits, slots, partial progress, conflicts in both directions, preview costs and crafting remainders. Tooltips and native JEI/EMI pages include the addon automatically. A single ingredient consumes one item per point. Honey bottles demonstrate returning container items on real pickup and shift-click.
Durability saving composes with remaining built-in wear, with a final 90% global cap. Other gameplay effects can use ordinary NeoForge events, as demonstrated by HoneyboundEvents. Test actual health loss for melee effects so absorbed or blocked damage cannot trigger them.

## Persistent data
Use ToolData.modifierLevel(String), progressOf(String), withModifierAdded(String), withProgress(String,int), and withAddonSlot(String,Modifier.Slot). Station applications record slot kind automatically.
Namespaced IDs, progress and slot metadata live in the tool component and its save/network codecs. If an addon is removed, effects/art are absent, but saved IDs and occupied slot types remain. Tooltips identify unavailable modifiers; sponge can remove them. Changing a published modifier ID or slot kind is a breaking addon change.

## Art
Return null from overlayModel for no artwork, or a standalone model resource such as your_addon:item/modifier_layers/pickaxe_waxed. Models are registered and layered by the core client integration. The example uses transparent layers 0-2 and coloured artwork on layer3, preserving core material tints.
Keep client-only code out of common addon constructors.

## Build
Java 21: gradlew.bat build
This produces the base JAR and separate wt-beeblock-workshop-0.1.0-alpha.1.jar.
Core engine tests: gradlew.bat runGameTestServer
Addon engine tests: gradlew.bat runGameTestServer -PwithBeeAddon
Optional installed Forestry test dependencies: -PbeeModsDir=<instance mods directory>.
Optional viewers: -Pviewer=emi, jei or both.

No external mod binaries are bundled. This is an alpha Java API; pin your addon dependency to a compatible base release.
