# Optional artwork rebuilds

All final textures and models are already in src/main/resources. Building the mod needs Java 21 and Gradle; Node is only needed to rebuild artwork.

From this project root, run these in order:

1. node tools/build-review-textures.cjs
2. node tools/build-visual-refinements.cjs
3. node tools/build-modifier-overlays.cjs

The source artwork used by these scripts is included under art-source. The PNG helper uses only Node built-in modules.

For the separate BeeBlock addon accents, also run: node tools/build-bee-overlays.cjs
