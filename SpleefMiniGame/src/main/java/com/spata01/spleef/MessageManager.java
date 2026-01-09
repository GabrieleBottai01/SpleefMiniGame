package com.spata01.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File file;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    // Carica o Ricarica il file
    public void load() {
        // Crea il file se non esiste
        file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        // Legge il file
        config = YamlConfiguration.loadConfiguration(file);
        plugin.getLogger().info("Messaggi caricati correttamente.");
    }

    // Metodo base: Prende una stringa dal config e la trasforma in Component
    public Component get(String path) {
        String raw = config.getString(path, "<red>Messaggio mancante: " + path);
        // Aggiungiamo sempre il prefisso se non è il prefisso stesso a essere richiesto
        if (!path.equals("prefix")) {
            String prefix = config.getString("prefix", "");
            // Se il messaggio contiene <noprefix> (opzionale), non mettiamo il prefisso
            return mm.deserialize(prefix + raw);
        }
        return mm.deserialize(raw);
    }

    // Metodo avanzato: Supporta placeholder (variabili)
    // Uso: get("arena.created", "{size}", "21", "{player}", "Steve")
    public Component get(String path, String... placeholders) {
        String raw = config.getString(path, "<red>Messaggio mancante: " + path);
        String prefix = config.getString("prefix", "");

        // Sostituzione variabili manuale (veloce ed efficace)
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                String target = placeholders[i];
                String replacement = placeholders[i + 1];
                raw = raw.replace(target, replacement);
            }
        }

        return mm.deserialize(prefix + raw);
    }

    // Metodo per ottenere la stringa raw (senza prefisso) utile per i Titoli
    public Component getTitle(String path, String... placeholders) {
        String raw = config.getString(path, "");
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                raw = raw.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return mm.deserialize(raw);
    }
}