# Development dependencies

WT Workshop does not bundle JEI or EMI. Optional integrations compile against the public APIs published by their maintainers:

- EMI: https://github.com/emilyploszaj/emi. Development/test version 1.1.24+1.21.1.
- Just Enough Items: https://github.com/mezz/JustEnoughItems. Compile API 19.21.0.247; also tested against 19.69.0.457.
- NeoForge and Minecraft development artifacts are resolved by the Gradle ModDev plugin.
- Gradle wrapper files and Gradle license are included for building the source.

No viewer binaries, Minecraft game files, Tinkers' Construct code or Tinkers' Construct assets are distributed in this package. Original source reconstruction is documented in README.md.

Quest runtime validation used installed FTB Quests 2101.1.36 and its FTB/Architectury dependencies. Those binaries are not included.

Forestry 3.0.0-alpha8 and Patchouli 1.21.1-93 were used for runtime compatibility checks. Their code, assets and binaries are not bundled. The addon references optional registry IDs through item tags.
