package com.spata01.spleef;

import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameManager {

    public enum GameState { NESSUNA_ARENA, ATTESA, STARTING, PLAYING, RESETTING }

    private final JavaPlugin plugin;
    private final MessageManager msg; // Gestore messaggi
    private GameState currentState = GameState.NESSUNA_ARENA;

    // Dati Arena
    private Location arenaAnchor;
    private int currentArenaSize = 11;
    private int currentRadius = 5;

    // Costanti fisse
    private final int GAP_FROM_PLAYER = 5;
    private final int WALL_HEIGHT = 22;

    private final List<UUID> giocatoriVivi = new ArrayList<>();

    public GameManager(JavaPlugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.msg = messageManager;
        caricaArenaDaConfig();
    }

    public GameState getState() { return currentState; }

    // =============================================================================
    // GAME LOOP & LOGICA GIOCO
    // =============================================================================

    private void avviaGameLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentState != GameState.PLAYING) {
                    this.cancel();
                    return;
                }

                // Creiamo una copia della lista per evitare errori mentre la modifichiamo
                for (UUID uuid : new ArrayList<>(giocatoriVivi)) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null && p.isOnline()) {
                        // Controllo sensibile (-0.1 y)
                        Material blockType = p.getLocation().subtract(0, 0.1, 0).getBlock().getType();
                        if (blockType == Material.OBSIDIAN) {
                            eliminaGiocatore(p);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void eliminaGiocatore(Player p) {
        if (!giocatoriVivi.contains(p.getUniqueId())) return;

        giocatoriVivi.remove(p.getUniqueId());
        p.removePotionEffect(PotionEffectType.GLOWING);

        // Effetti Morte
        p.setGameMode(GameMode.SPECTATOR);
        p.teleport(p.getLocation().add(0, 5, 0));
        p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.5f, 2f);

        // Titolo Morte (dal config)
        Title titoloMorte = Title.title(
                msg.getTitle("game.eliminated-title"),
                msg.getTitle("game.eliminated-subtitle")
        );
        p.showTitle(titoloMorte);

        // Broadcast (dal config)
        Bukkit.broadcast(msg.get("game.eliminated-chat", "{player}", p.getName()));

        if (giocatoriVivi.size() <= 1) {
            dichiaraVincitore();
        }
    }

    private void dichiaraVincitore() {
        currentState = GameState.RESETTING;
        String nomeVincitore = "Nessuno";
        Player winner = null;

        if (!giocatoriVivi.isEmpty()) {
            winner = Bukkit.getPlayer(giocatoriVivi.get(0));
            if (winner != null) nomeVincitore = winner.getName();
        }

        // Titolo Vittoria
        Title victoryTitle = Title.title(
                msg.getTitle("game.victory-title"),
                msg.getTitle("game.victory-subtitle", "{winner}", nomeVincitore)
        );
        Bukkit.getServer().showTitle(victoryTitle);

        if (winner != null) winner.playSound(winner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);

        // Chat finale
        Bukkit.broadcast(msg.get("game.victory-chat", "{winner}", nomeVincitore));

        // Reset automatico
        new BukkitRunnable() { @Override public void run() { resettaArena(); } }.runTaskLater(plugin, 100L);
    }

    public void avviaGioco(Player admin) {
        if (currentState != GameState.ATTESA) {
            admin.sendMessage(msg.get("game.not-ready"));
            return;
        }

        giocatoriVivi.clear();
        Location spawnPoint = getSpawnPoint();

        // Raduna giocatori
        for (Player p : arenaAnchor.getWorld().getPlayers()) {
            if (p.getLocation().distance(arenaAnchor) < 50) {
                p.teleport(spawnPoint);
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20);
                p.setFoodLevel(20);
                p.getInventory().clear();
                giocatoriVivi.add(p.getUniqueId());
            }
        }

        if (giocatoriVivi.isEmpty()) {
            admin.sendMessage(msg.get("game.no-players"));
            return;
        }

        currentState = GameState.STARTING;

        // Countdown
        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count > 0) {
                    String color = count == 3 ? "red" : (count == 2 ? "gold" : "yellow");

                    Title title = Title.title(
                            msg.getTitle("game.countdown-title", "{color}", color, "{count}", String.valueOf(count)),
                            msg.getTitle("game.countdown-subtitle"),
                            Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1200), Duration.ofMillis(0))
                    );

                    for(UUID uuid : giocatoriVivi) {
                        Player p = Bukkit.getPlayer(uuid);
                        if(p!=null) {
                            p.showTitle(title);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.5f + (3-count)*0.5f);
                        }
                    }
                    count--;
                } else {
                    currentState = GameState.PLAYING;
                    avviaGameLoop(); // Start controllo ossidiana

                    for(UUID uuid : giocatoriVivi) {
                        Player p = Bukkit.getPlayer(uuid);
                        if(p!=null) {
                            p.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false));
                            p.showTitle(Title.title(msg.getTitle("game.start-title"), net.kyori.adventure.text.Component.empty()));
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                        }
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void resettaArena() {
        if (arenaAnchor == null) return;

        costruisciStruttura(Material.AIR);
        costruisciStruttura(null);

        currentState = GameState.RESETTING;
        Bukkit.broadcast(msg.get("arena.regenerated"));

        new BukkitRunnable() {
            int count = 3;
            @Override
            public void run() {
                if (count > 0) {
                    Title title = Title.title(
                            msg.getTitle("arena.lobby-countdown", "{count}", String.valueOf(count)),
                            msg.getTitle("arena.lobby-subtitle")
                    );
                    for (Player p : arenaAnchor.getWorld().getPlayers()) {
                        if (p.getLocation().distance(arenaAnchor) < 60) p.showTitle(title);
                    }
                    count--;
                } else {
                    Location spawnPoint = getSpawnPoint();
                    for (Player p : arenaAnchor.getWorld().getPlayers()) {
                        if (p.getLocation().distance(arenaAnchor) < 60) {
                            p.teleport(spawnPoint);
                            p.setGameMode(GameMode.SURVIVAL);
                            p.setHealth(20);
                            p.setFoodLevel(20);
                            p.getInventory().clear();
                            for (PotionEffect effect : p.getActivePotionEffects()) p.removePotionEffect(effect.getType());
                        }
                    }
                    currentState = GameState.ATTESA;
                    giocatoriVivi.clear();
                    Bukkit.broadcast(msg.get("arena.ready"));
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    // =============================================================================
    // CREAZIONE E DISTRUZIONE
    // =============================================================================

    public void creaArena(Player p, int dimensioneRichiesta) {
        if (currentState != GameState.NESSUNA_ARENA) {
            p.sendMessage(msg.get("arena.already-exists"));
            return;
        }

        if (dimensioneRichiesta < 11) {
            p.sendMessage(msg.get("admin.invalid-args")); // O messaggio specifico se vuoi aggiungerlo
            return;
        }

        // Forza dispari
        if (dimensioneRichiesta % 2 == 0) dimensioneRichiesta++;

        // Setup dimensioni
        this.currentArenaSize = dimensioneRichiesta;
        this.currentRadius = (currentArenaSize - 1) / 2;

        // Calcolo Vettoriale
        Location playerLoc = p.getLocation();
        Vector direzione = playerLoc.getDirection().setY(0).normalize();
        double distanzaDalPlayer = GAP_FROM_PLAYER + currentRadius + 1;

        Location centroIdeale = playerLoc.clone().add(direzione.multiply(distanzaDalPlayer));
        centroIdeale.setX(centroIdeale.getBlockX());
        centroIdeale.setZ(centroIdeale.getBlockZ());
        centroIdeale.setY(playerLoc.getBlockY());

        this.arenaAnchor = centroIdeale.clone().subtract(currentRadius, 0, currentRadius);

        costruisciStruttura(null);
        salvaArenaSuConfig();
        currentState = GameState.ATTESA;

        p.sendMessage(msg.get("arena.created", "{size}", String.valueOf(currentArenaSize)));
    }

    public void distruggiArena(Player p) {
        if (arenaAnchor == null) {
            p.sendMessage(msg.get("arena.not-found"));
            return;
        }
        costruisciStruttura(Material.AIR);
        cancellaArenaDaConfig();
        currentState = GameState.NESSUNA_ARENA;
        arenaAnchor = null;
        p.sendMessage(msg.get("arena.deleted"));
    }

    private void costruisciStruttura(Material mat) {
        World world = arenaAnchor.getWorld();
        Material vetro = (mat == Material.AIR) ? Material.AIR : Material.LIGHT_BLUE_STAINED_GLASS;

        // Muri
        for (int y = 0; y <= WALL_HEIGHT; y++) {
            for (int x = -1; x <= currentArenaSize; x++) {
                for (int z = -1; z <= currentArenaSize; z++) {
                    if (x == -1 || x == currentArenaSize || z == -1 || z == currentArenaSize)
                        world.getBlockAt(arenaAnchor.clone().add(x, y, z)).setType(vetro);
                }
            }
        }

        // Pavimenti
        for (int x = 0; x < currentArenaSize; x++) {
            for (int z = 0; z < currentArenaSize; z++) {
                Location b = arenaAnchor.clone().add(x, 0, z);
                world.getBlockAt(b).setType(mat != null ? mat : Material.OBSIDIAN);
                for (int i = 1; i <= 5; i++) world.getBlockAt(b.clone().add(0, i * 4, 0)).setType(mat != null ? mat : Material.SNOW_BLOCK);
            }
        }
    }

    private Location getSpawnPoint() {
        return arenaAnchor.clone().add(currentRadius + 0.5, WALL_HEIGHT - 1, currentRadius + 0.5);
    }

    // =============================================================================
    // PERSISTENZA E CONFIG
    // =============================================================================

    private void caricaArenaDaConfig() {
        if (plugin.getConfig().contains("arena.world")) {
            World w = Bukkit.getWorld(plugin.getConfig().getString("arena.world"));
            if (w != null) {
                this.arenaAnchor = new Location(w,
                        plugin.getConfig().getInt("arena.x"),
                        plugin.getConfig().getInt("arena.y"),
                        plugin.getConfig().getInt("arena.z")
                );
                this.currentArenaSize = plugin.getConfig().getInt("arena.size", 11);
                this.currentRadius = (currentArenaSize - 1) / 2;
                this.currentState = GameState.ATTESA;
            }
        }
    }

    private void salvaArenaSuConfig() {
        if (arenaAnchor == null) return;
        plugin.getConfig().set("arena.world", arenaAnchor.getWorld().getName());
        plugin.getConfig().set("arena.x", arenaAnchor.getBlockX());
        plugin.getConfig().set("arena.y", arenaAnchor.getBlockY());
        plugin.getConfig().set("arena.z", arenaAnchor.getBlockZ());
        plugin.getConfig().set("arena.size", currentArenaSize);
        plugin.saveConfig();
    }

    private void cancellaArenaDaConfig() {
        plugin.getConfig().set("arena", null);
        plugin.saveConfig();
    }
}