package com.spata01.spleef;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGenCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        sender.sendMessage(Component.text("Inizio generazione mondo... (Attendi)", NamedTextColor.YELLOW));

        // 1. Setup del creatore
        WorldCreator creator = new WorldCreator("mondo_test");
        creator.type(WorldType.FLAT);
        creator.generateStructures(false);

        // 2. Creazione (Blocca il thread principale per un attimo)
        World nuovoMondo = Bukkit.createWorld(creator);

        if (nuovoMondo != null) {
            // Setup regole (come prima)
            nuovoMondo.setGameRule(GameRule.DO_MOB_SPAWNING, false);
            nuovoMondo.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            nuovoMondo.setTime(6000);
            nuovoMondo.setSpawnFlags(false, false);

            sender.sendMessage(Component.text("Mondo creato!", NamedTextColor.GREEN));

            // --- NUOVA LOGICA DI TELETRASPORTO ---

            // DOMANDA: Chi ha lanciato il comando? È un umano?
            if (sender instanceof Player) {
                // Sì, è un umano. Ora possiamo trasformare (castare) 'sender' in 'Player'
                Player giocatore = (Player) sender;

                // Prendiamo il punto di spawn del nuovo mondo
                Location spawnPoint = nuovoMondo.getSpawnLocation();

                // Teletrasporto
                giocatore.teleport(spawnPoint);

                giocatore.sendMessage(Component.text("Benvenuto nel nulla!", NamedTextColor.AQUA));
            } else {
                // No, è la console o un command block
                sender.sendMessage(Component.text("Mondo creato, ma non posso teletrasportarti (sei la Console!)", NamedTextColor.RED));
            }

        } else {
            sender.sendMessage(Component.text("Errore fatale nella creazione.", NamedTextColor.DARK_RED));
        }

        return true;
    }
}