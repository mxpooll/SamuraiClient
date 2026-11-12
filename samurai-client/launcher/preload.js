const { contextBridge, ipcRenderer } = require("electron");

const on = (channel) => (cb) => ipcRenderer.on(channel, (_e, data) => cb(data));

contextBridge.exposeInMainWorld("samurai", {
  minimize: () => ipcRenderer.send("win:minimize"),
  close: () => ipcRenderer.send("win:close"),
  getConfig: () => ipcRenderer.invoke("config:get"),
  setConfig: (patch) => ipcRenderer.invoke("config:set", patch),
  systemRam: () => ipcRenderer.invoke("system:ram"),
  listVersions: () => ipcRenderer.invoke("versions:list"),
  login: () => ipcRenderer.invoke("auth:login"),
  selectAccount: (uuid) => ipcRenderer.invoke("auth:select", uuid),
  removeAccount: (uuid) => ipcRenderer.invoke("auth:remove", uuid),
  listAccounts: () => ipcRenderer.invoke("auth:list"),
  launch: () => ipcRenderer.invoke("launch:start"),
  checkUpdate: () => ipcRenderer.invoke("update:check"),
  openGameDir: () => ipcRenderer.send("open:gamedir"),
  openExternal: (url) => ipcRenderer.send("open:external", url),
  onStatus: on("launch:status"),
  onProgress: on("launch:progress"),
  onLog: on("launch:log"),
  onClosed: on("launch:closed"),
});
