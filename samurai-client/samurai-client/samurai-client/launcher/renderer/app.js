const $ = (s) => document.querySelector(s);
const api = window.samurai;
let config;

// ---------- petals ----------
(function petals() {
  const box = $("#petals");
  for (let i = 0; i < 18; i++) {
    const p = document.createElement("div");
    p.className = "petal";
    p.style.left = Math.random() * 100 + "%";
    p.style.setProperty("--dx", (Math.random() * 200 - 100) + "px");
    p.style.animationDuration = 9 + Math.random() * 10 + "s";
    p.style.animationDelay = -Math.random() * 15 + "s";
    p.style.transform = `scale(${0.6 + Math.random() * 0.8})`;
    box.appendChild(p);
  }
})();

// ---------- navigation ----------
document.querySelectorAll(".nav").forEach((b) =>
  b.addEventListener("click", () => {
    document.querySelectorAll(".nav").forEach((n) => n.classList.remove("active"));
    document.querySelectorAll(".page").forEach((p) => p.classList.remove("active"));
    b.classList.add("active");
    $("#page-" + b.dataset.page).classList.add("active");
  })
);
$("#btn-min").onclick = () => api.minimize();
$("#btn-close").onclick = () => api.close();

// ---------- accounts ----------
function renderAccounts({ active, accounts }) {
  const list = $("#account-list");
  list.replaceChildren();
  accounts.forEach((a) => {
    const card = document.createElement("div");
    card.className = "card" + (a.uuid === active ? " active" : "");
    const name = document.createElement("span");
    name.textContent = a.name;
    const rm = document.createElement("button");
    rm.textContent = "Remove";
    rm.onclick = async (e) => {
      e.stopPropagation();
      renderAccounts(await api.removeAccount(a.uuid));
    };
    card.append(name, rm);
    card.onclick = async () => renderAccounts(await api.selectAccount(a.uuid));
    list.appendChild(card);
  });
  const current = accounts.find((a) => a.uuid === active);
  $("#who").textContent = current ? "Signed in as " + current.name : "Not signed in";
}
$("#btn-login").onclick = async () => {
  try {
    renderAccounts(await api.login());
  } catch (e) {
    setStatus("Login failed: " + e.message, true);
  }
};

// ---------- settings ----------
function bindSettings() {
  const total = 16384;
  api.systemRam().then((mb) => {
    const max = Math.max(2048, Math.floor(mb / 256) * 256);
    $("#ramMin").max = max;
    $("#ramMax").max = max;
  });
  const sliders = { ramMin: $("#ramMin"), ramMax: $("#ramMax") };
  for (const [key, el] of Object.entries(sliders)) {
    el.value = config[key];
    $("#" + key + "Label").textContent = (config[key] / 1024).toFixed(1) + " GB";
    el.oninput = async () => {
      $("#" + key + "Label").textContent = (el.value / 1024).toFixed(1) + " GB";
      config = await api.setConfig({ [key]: Number(el.value) });
    };
  }
  $("#jvmArgs").value = config.jvmArgs;
  $("#jvmArgs").onchange = async (e) => (config = await api.setConfig({ jvmArgs: e.target.value }));
  $("#fullscreen").checked = config.fullscreen;
  $("#fullscreen").onchange = async (e) => (config = await api.setConfig({ fullscreen: e.target.checked }));
  $("#background").value = config.background;
  $("#background").onchange = async (e) => {
    config = await api.setConfig({ background: e.target.value });
    $("#bg").dataset.bg = config.background;
  };
  $("#btn-gamedir").onclick = () => api.openGameDir();
}

// ---------- launch ----------
function setStatus(text, isError) {
  $("#progress").hidden = false;
  $("#status").textContent = text;
  $("#status").style.color = isError ? "#ff6b81" : "";
}
api.onStatus((t) => setStatus(t));
api.onProgress((e) => {
  if (e && e.total) $("#bar-fill").style.width = Math.round((e.task / e.total) * 100) + "%";
});
api.onClosed(() => {
  $("#btn-launch").disabled = false;
  $("#progress").hidden = true;
  $("#bar-fill").style.width = "0";
});
$("#btn-launch").onclick = async () => {
  $("#btn-launch").disabled = true;
  try {
    await api.launch();
  } catch (e) {
    setStatus(e.message, true);
    $("#btn-launch").disabled = false;
  }
};

// ---------- init ----------
(async function init() {
  config = await api.getConfig();
  $("#bg").dataset.bg = config.background;
  bindSettings();
  renderAccounts(await api.listAccounts());

  const sel = $("#version");
  let versions = [config.version];
  try { versions = await api.listVersions(); } catch {}
  versions.forEach((v) => sel.add(new Option(v, v)));
  sel.value = versions.includes(config.version) ? config.version : versions[0];
  sel.onchange = async () => (config = await api.setConfig({ version: sel.value }));

  const upd = await api.checkUpdate();
  if (upd.available) {
    const box = $("#update");
    box.hidden = false;
    box.textContent = "Update " + upd.version + " available";
    box.onclick = () => api.openExternal(upd.url);
  }
})();
