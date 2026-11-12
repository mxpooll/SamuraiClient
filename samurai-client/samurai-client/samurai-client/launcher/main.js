const { app, BrowserWindow, ipcMain, shell } = require("electron");
const path = require("path");
const fs = require("fs");
const os = require("os");

// Third-party deps (npm install): msmc handles Microsoft/Xbox/Minecraft login,
// minecraft-launcher-core downloads and starts the game.
const { Auth } = require("msmc");
const { Client } = require("minecraft-launcher-core");

const ROOT = path.join(app.getPath("appData"), ".samurai-client");
const GAME_DIR = path.join(ROOT, "game");
const CONFIG_FILE = path.join(ROOT, "launcher.json");
const FABRIC_META = "https://meta.fabricmc.net/v2";
const MODRINTH = "https://api.modrinth.com/v2";
const UPDATE_URL = "https://example.com/samurai/latest.json"; // TODO: your update manifest

const DEFAULTS = {
  version: "1.21.4",
  ramMin: 2048,
  ramMax: 4096,
  jvmArgs: "",
  fullscreen: false,
  background: "default",
  accounts: [],
  activeAccount: null,
};

fs.mkdirSync(GAME_DIR, { recursive: true });

function loadConfig() {
  try {
    return { ...DEFAULTS, ...JSON.parse(fs.readFileSync(CONFIG_FILE, "utf8")) };
  } catch {
    return { ...DEFAULTS };
  }
}
function saveConfig(cfg) {
  fs.writeFileSync(CONFIG_FILE, JSON.stringify(cfg, null, 2));
}

let win;
function createWindow() {
  win = new BrowserWindow({
    width: 1100,
    height: 680,
    minWidth: 900,
    minHeight: 560,
    frame: false,
    backgroundColor: "#0b0b0d",
    title: "Samurai Client",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  win.loadFile(path.join(__dirname, "renderer", "index.html"));
}
app.whenReady().then(createWindow);
app.on("window-all-closed", () => app.quit());

const send = (channel, payload) => win && win.webContents.send(channel, payload);

// ---------- window controls ----------
ipcMain.on("win:minimize", () => win.minimize());
ipcMain.on("win:close", () => win.close());

// ---------- config ----------
ipcMain.handle("config:get", () => loadConfig());
ipcMain.handle("config:set", (_e, patch) => {
  const cfg = { ...loadConfig(), ...patch };
  saveConfig(cfg);
  return cfg;
});
ipcMain.handle("system:ram", () => Math.floor(os.totalmem() / 1024 / 1024));

// ---------- versions ----------
ipcMain.handle("versions:list", async () => {
  // Fabric supports every release; we expose the 1.21.x line for this client.
  const res = await fetch(`${FABRIC_META}/versions/game`);
  const all = await res.json();
  return all
    .filter((v) => v.stable && /^1\.21(\.\d+)?$/.test(v.version))
    .map((v) => v.version);
});

// ---------- Microsoft authentication ----------
ipcMain.handle("auth:login", async () => {
  const authManager = new Auth("select_account");
  const xbox = await authManager.launch("electron");
  const mc = await xbox.getMinecraft();
  const cfg = loadConfig();
  const account = {
    uuid: mc.profile.id,
    name: mc.profile.name,
    // msmc's MCLC-compatible auth object; contains a refreshable token.
    mclc: mc.mclc(),
    refresh: xbox.save(),
  };
  cfg.accounts = cfg.accounts.filter((a) => a.uuid !== account.uuid).concat(account);
  cfg.activeAccount = account.uuid;
  saveConfig(cfg);
  return publicAccounts(cfg);
});
ipcMain.handle("auth:select", (_e, uuid) => {
  const cfg = loadConfig();
  cfg.activeAccount = uuid;
  saveConfig(cfg);
  return publicAccounts(cfg);
});
ipcMain.handle("auth:remove", (_e, uuid) => {
  const cfg = loadConfig();
  cfg.accounts = cfg.accounts.filter((a) => a.uuid !== uuid);
  if (cfg.activeAccount === uuid) cfg.activeAccount = cfg.accounts[0]?.uuid ?? null;
  saveConfig(cfg);
  return publicAccounts(cfg);
});
ipcMain.handle("auth:list", () => publicAccounts(loadConfig()));
function publicAccounts(cfg) {
  return {
    active: cfg.activeAccount,
    accounts: cfg.accounts.map((a) => ({ uuid: a.uuid, name: a.name })),
  };
}

// ---------- client bootstrap (Fabric + Fabric API + Samurai mod) ----------
async function download(url, dest) {
  const res = await fetch(url);
  if (!res.ok) throw new Error(`Download failed (${res.status}): ${url}`);
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  fs.writeFileSync(dest, Buffer.from(await res.arrayBuffer()));
}

async function installFabric(mcVersion) {
  const loaders = await (await fetch(`${FABRIC_META}/versions/loader/${mcVersion}`)).json();
  const loader = loaders.find((l) => l.loader.stable)?.loader.version;
  if (!loader) throw new Error(`No Fabric loader for ${mcVersion}`);
  const id = `fabric-loader-${loader}-${mcVersion}`;
  const profile = path.join(GAME_DIR, "versions", id, `${id}.json`);
  if (!fs.existsSync(profile)) {
    send("launch:status", "Installing Fabric loader…");
    await download(`${FABRIC_META}/versions/loader/${mcVersion}/${loader}/profile/json`, profile);
  }
  return id;
}

async function ensureFabricApi(mcVersion) {
  const modsDir = path.join(GAME_DIR, "mods");
  fs.mkdirSync(modsDir, { recursive: true });
  const marker = path.join(modsDir, `.fabric-api-${mcVersion}`);
  if (fs.existsSync(marker)) return;
  send("launch:status", "Downloading Fabric API…");
  const q = new URLSearchParams({
    game_versions: JSON.stringify([mcVersion]),
    loaders: JSON.stringify(["fabric"]),
  });
  const versions = await (await fetch(`${MODRINTH}/project/fabric-api/version?${q}`)).json();
  const file = versions[0]?.files.find((f) => f.primary) ?? versions[0]?.files[0];
  if (!file) throw new Error(`Fabric API not available for ${mcVersion}`);
  await download(file.url, path.join(modsDir, file.filename));
  fs.writeFileSync(marker, file.filename);
}

function installSamuraiMod(mcVersion) {
  // Build ../client (gradle build) and copy build/libs/samurai-client-<mc>-<ver>.jar into launcher/bundled/
  const bundled = path.join(__dirname, "bundled");
  const prefix = `samurai-client-${mcVersion}-`;
  const jar = fs.existsSync(bundled)
    ? fs.readdirSync(bundled).find((f) => f.startsWith(prefix) && f.endsWith(".jar") && !f.includes("sources"))
    : null;
  const modsDir = path.join(GAME_DIR, "mods");
  for (const f of fs.readdirSync(modsDir)) {
    if (f.startsWith("samurai-client")) fs.unlinkSync(path.join(modsDir, f));
  }
  if (!jar) {
    throw new Error(`Samurai client jar for ${mcVersion} not found in launcher/bundled/`);
  }
  fs.copyFileSync(path.join(bundled, jar), path.join(modsDir, jar));
}

// ---------- launch ----------
let launching = false;
ipcMain.handle("launch:start", async () => {
  if (launching) return;
  const cfg = loadConfig();
  const account = cfg.accounts.find((a) => a.uuid === cfg.activeAccount);
  if (!account) throw new Error("Sign in with a Microsoft account first.");
  launching = true;
  try {
    const fabricId = await installFabric(cfg.version);
    await ensureFabricApi(cfg.version);
    installSamuraiMod(cfg.version);

    const launcher = new Client();
    launcher.on("progress", (e) => send("launch:progress", e));
    launcher.on("download-status", (e) => send("launch:progress", e));
    launcher.on("debug", (m) => send("launch:log", m));
    launcher.on("data", (m) => send("launch:log", String(m)));
    launcher.on("close", () => {
      launching = false;
      send("launch:closed");
      win.show();
    });

    send("launch:status", "Starting Minecraft…");
    await launcher.launch({
      authorization: account.mclc,
      root: GAME_DIR,
      version: { number: cfg.version, type: "release", custom: fabricId },
      memory: { min: `${cfg.ramMin}M`, max: `${cfg.ramMax}M` },
      customArgs: cfg.jvmArgs ? cfg.jvmArgs.split(/\s+/).filter(Boolean) : [],
      window: cfg.fullscreen ? { fullscreen: true } : undefined,
    });
    send("launch:status", "Running");
    win.minimize();
  } catch (err) {
    launching = false;
    throw err;
  }
});

// ---------- update checker ----------
ipcMain.handle("update:check", async () => {
  try {
    const res = await fetch(UPDATE_URL);
    if (!res.ok) return { available: false };
    const latest = await res.json(); // { version, url, notes }
    return { available: latest.version !== app.getVersion(), ...latest };
  } catch {
    return { available: false };
  }
});
ipcMain.on("open:external", (_e, url) => {
  if (/^https:\/\//.test(url)) shell.openExternal(url);
});
ipcMain.on("open:gamedir", () => shell.openPath(GAME_DIR));
