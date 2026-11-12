# Samurai Client

Fabric-based Minecraft client (1.21.4) with its own Electron launcher.

```
launcher/   Electron app: Microsoft login, version + RAM settings, auto-installs Fabric,
            Fabric API and the Samurai jar, then launches the game
client/     Fabric mod: branded title/pause screens, module framework, first modules
```

## Build and run

1. **Client**: `cd client && gradle wrapper && ./gradlew build`
   Copy `build/libs/samurai-client-1.21.4-0.1.0.jar` (not `-sources`) to `launcher/bundled/`.
2. **Launcher**: `cd launcher && npm install && npm start`
   Package with `npm run dist`.
3. In `launcher/main.js` set `UPDATE_URL` to your own update manifest
   (`{ "version": "0.2.0", "url": "https://...", "notes": "..." }`).

Check `client/gradle.properties` against https://fabricmc.net/develop for current
loader / Fabric API versions. This code was written without being compiled
(no network access in the authoring environment), so expect to fix a few
version-specific API names on first build.

## Done in this phase

- Launcher: animated UI, Microsoft auth (msmc), account switching, version picker, RAM sliders,
  JVM args, fullscreen, background themes, update checker, Fabric bootstrap
- In-game: Samurai title screen, branded pause screen, module menu (Right Shift), window title
- Modules (12): FPS, Coordinates, Direction HUD, CPS, Keystrokes, Armor Status, Ping,
  Memory Usage, Toggle Sprint, Toggle Sneak, Zoom (hold C), Fullbright

## Not done yet

- Custom loading screen, multiplayer screen and settings screen
- HUD drag-and-drop editor (position/scale are stored in `config/samurai.json` but edited by hand)
- Remaining ~60 modules from the spec (see below)
- Launcher: skin/avatar display, crash reports, per-version profiles, code signing

## Adding a module

Subclass `HudMod`, give it an id/name/category/default position, implement `render` (HUD)
and/or `onTick`, then add it in `BuiltInMods.registerAll`. It appears in the module menu
and config automatically.

## Remaining module roadmap

Suggested order, easiest first:

1. **HUD-only** (same pattern as FPS): Boss Bar, Potion Effects, Scoreboard, Server Address, Saturation,
   Day Counter, Clock, Playtime, Item Counter, Stopwatch, Combo Counter, Reach Display, Totem Counter,
   Cooldowns, PvP Info
2. **Option/state toggles**: FOV Changer, Freelook, Snaplook, Hurt Cam, Crosshair, Titles, Menu Blur,
   Time Changer, Weather Changer, Color Saturation, Block Outline, Chunk Borders
3. **Mixin work**: Hitbox, Hit Color, Motion Blur, Fog Customizer, Particle Changer, Glint Colorizer,
   NameTags, Team View, TNT Countdown, Scrollable Tooltips, Lighting, 2D Items, Item Physics
4. **Network/server specific**: Hypixel Mods / Bedwars / Quickplay, UHC Overlay, Nick Hider, Tab Editor
5. **Large features**: 3D Skins, Waypoints, WAILA, Pack Display/Organizer, Screenshot Uploader,
   Auto Text Hotkey, Better Sounds, Item Tracker
6. **Integrations**: WorldEdit CUI, Mumble Link, Momentum Mod, Replay Mod (Replay Mod is a large
   existing project; bundling its code requires respecting its license)

## Getting the .exe (no local Gradle needed)

1. Push this folder to a GitHub repository (branch `main`).
2. Open the repo's **Actions** tab, run **Build Samurai Client exe** (it also runs on every push).
3. Download the `SamuraiClient` artifact: it contains `SamuraiClient.exe`, a single portable file with
   the client mod already bundled. Double-click to run.

If the build fails on a Minecraft API name, the Gradle step's log shows which line to fix.
