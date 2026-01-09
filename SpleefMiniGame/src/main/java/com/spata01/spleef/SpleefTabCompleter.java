package com.spata01.spleef;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpleefTabCompleter implements TabCompleter {

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> suggerimenti = new ArrayList<>();

        // Argomento 1: Comandi principali
        if (args.length == 1) {
            suggerimenti.add("create");
            suggerimenti.add("delete");
            suggerimenti.add("reload");
            suggerimenti.add("start");
        }

        // Argomento 2: Dimensioni (solo se il primo era 'create')
        if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
            suggerimenti.add("small");  // 11
            suggerimenti.add("medium"); // 21
            suggerimenti.add("big");    // 31
            suggerimenti.add("[num]");
        }

        return suggerimenti;
    }
}