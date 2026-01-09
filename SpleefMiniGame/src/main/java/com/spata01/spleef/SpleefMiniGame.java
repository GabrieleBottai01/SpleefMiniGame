package com.spata01.spleef; // Il tuo package

import org.bukkit.plugin.java.JavaPlugin;

public final class SpleefMiniGame extends JavaPlugin {

    private GameManager gameManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        // 1. Carichiamo i messaggi PRIMA di tutto
        this.messageManager = new MessageManager(this);

        // 2. Passiamo il MessageManager al GameManager
        this.gameManager = new GameManager(this, messageManager);

        // 3. Passiamo entrambi al Comando
        getCommand("spleef").setExecutor(new SpleefCommand(gameManager, messageManager));
        getCommand("spleef").setTabCompleter(new SpleefTabCompleter());

        // 4. Passiamo il GameManager al Listener (il listener non stampa messaggi,
        // ma se lo facesse dovresti passargli messageManager)
        getServer().getPluginManager().registerEvents(new SpleefListener(this, gameManager), this);
    }
}