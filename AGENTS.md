# Codex Notes For Xuanhuan Earth

- This is the independent repository for `Xuanhuan Earth / 玄幻地球`.
- Do not merge gameplay code into `earth_on_minecraft` core unless explicitly requested.
- Keep the mod independently runnable. Integrate `earth_on_minecraft`, `earth_human`, and `fantasy_earth` only through optional compatibility layers.
- Preserve the technical mod id `xuanhuan_earth` unless a deliberate save-breaking migration is requested.
- Target line follows `NeoForge 26.1.2.124 / MC 26.1.2 / JDK 25` to match the rest of the Earth series. The module directory is still named `neoforge-26.2`; always read `neoforge-26.2/build.gradle` for the real target.
- Build with `JAVA_HOME=D:\\Java\\jdk-25` and `.\\gradlew.bat --offline build` from `neoforge-26.2`.
- **NEVER launch the game client (`gradlew runClient`, launcher, or client smoke scripts) without explicit permission from the user in the current turn.** Verify client code via static checks (`tools/check_model_parts.py`), bytecode inspection, or headless unit tests; leave in-game client verification to the user.
- This mod recreates xuanhuan content and systems, not real survival difficulty.
