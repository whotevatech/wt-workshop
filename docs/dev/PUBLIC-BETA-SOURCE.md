# Public Beta source

WT Workshop 0.7.0-beta.1 and WT BeeBlock Workshop 0.1.0-beta.1. Minecraft 1.21.1 / NeoForge 21.1.263 / JDK 21.

Build both production JARs with: gradlew.bat build (Windows), or ./gradlew build. Outputs are in build/libs. The beeAddonJar task builds the separate addon. Do not ship development test classes; production Jar tasks exclude them. The source archive intentionally includes GameTests for development.

Optional runs: gradlew.bat runClient; add -PwithBeeAddon to load the addon; add -Pviewer=jei or -Pviewer=emi to test an optional recipe viewer. Tests: gradlew.bat runGameTestServer, with -PwithBeeAddon for addon tests. Internet access is needed for Gradle and dependencies.

Project branding can be re-exported with node tools/export-project-icons.cjs. See tools and art-source for the original artwork pipeline and addon API documentation in ADDON-API.md. Historical README, CHANGELOG, ROADMAP and VALIDATION files record earlier development; the public versions, page copy and release validation in the publishing kit are authoritative for this Beta.

License: MIT; see LICENSE and NOTICE-THIRD-PARTY.md. Gradle wrapper licensing is included separately. No Minecraft, NeoForge, Forestry, FTB, JEI or EMI binaries are bundled.
