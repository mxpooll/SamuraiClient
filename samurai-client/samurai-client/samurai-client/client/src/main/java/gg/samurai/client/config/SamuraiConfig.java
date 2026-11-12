package gg.samurai.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gg.samurai.client.mod.HudMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persists per-module settings to config/samurai.json. */
public final class SamuraiConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = new TypeToken<Map<String, Entry>>() {}.getType();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("samurai.json");

    public static final class Entry {
        public boolean enabled;
        public int x;
        public int y;
        public float scale = 1f;
    }

    private SamuraiConfig() {}

    public static void load(List<HudMod> mods) {
        if (!Files.exists(FILE)) return;
        try (Reader r = Files.newBufferedReader(FILE)) {
            Map<String, Entry> map = GSON.fromJson(r, TYPE);
            if (map == null) return;
            for (HudMod m : mods) {
                Entry e = map.get(m.id);
                if (e == null) continue;
                m.enabled = e.enabled;
                m.x = e.x;
                m.y = e.y;
                m.scale = e.scale <= 0 ? 1f : e.scale;
            }
        } catch (IOException | RuntimeException ex) {
            System.err.println("[Samurai] Could not read config: " + ex);
        }
    }

    public static void save(List<HudMod> mods) {
        Map<String, Entry> map = new LinkedHashMap<>();
        for (HudMod m : mods) {
            Entry e = new Entry();
            e.enabled = m.enabled;
            e.x = m.x;
            e.y = m.y;
            e.scale = m.scale;
            map.put(m.id, e);
        }
        try (Writer w = Files.newBufferedWriter(FILE)) {
            GSON.toJson(map, TYPE, w);
        } catch (IOException ex) {
            System.err.println("[Samurai] Could not save config: " + ex);
        }
    }
}
