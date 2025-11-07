package net.kassin.abstractPlugin.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Config extends YamlConfiguration {

    private final File file;
    private final String name;
    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
        this.file = new File(plugin.getDataFolder(), name);

        if (!file.exists()) {
            plugin.saveResource(name, false);
        }
        reloadDefaultConfig();
    }

    public void saveConfig() {
        try {
            save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Erro ao salvar arquivo de configuração: " + name);
            e.printStackTrace();
        }
    }

    private void reloadConfig() {
        if (!file.exists()) return;

        try {
            load(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao carregar arquivo de configuração: " + name);
            e.printStackTrace();
        }
    }

    public void reloadDefaultConfig() {
        reloadConfig();
    }

}

