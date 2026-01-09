package com.spata01.spleef;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpleefCommand implements CommandExecutor {

    private final GameManager gameManager;
    private final MessageManager msg; // Riferimento ai messaggi

    public SpleefCommand(GameManager gameManager, MessageManager msg) {
        this.gameManager = gameManager;
        this.msg = msg;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        // Controllo se è un giocatore
        if (!(sender instanceof Player)) {
            sender.sendMessage(msg.get("admin.no-console"));
            return true;
        }

        Player player = (Player) sender;

        // Controllo Argomenti vuoti
        if (args.length == 0) {
            player.sendMessage(msg.get("admin.invalid-args"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                int dimensione = 11; // Default

                // Parsing dimensione (es. /spleef create 21)
                if (args.length > 1) {
                    String input = args[1].toLowerCase();
                    if (input.equals("small")) {
                        dimensione = 11;
                    } else if (input.equals("medium")) {
                        dimensione = 21;
                    } else if (input.equals("big")) {
                        dimensione = 31;
                    } else {
                        try {
                            dimensione = Integer.parseInt(input);
                        } catch (NumberFormatException e) {
                            player.sendMessage(msg.get("admin.invalid-args"));
                            return true;
                        }
                    }
                }
                gameManager.creaArena(player, dimensione);
                break;

            case "delete":
                gameManager.distruggiArena(player);
                break;

            case "start":
                gameManager.avviaGioco(player);
                break;

            case "reload":
                msg.load(); // Ricarica il file messages.yml
                player.sendMessage(msg.get("admin.reload"));
                break;

            default:
                player.sendMessage(msg.get("arena.not-found"));
        }

        return true;
    }
}