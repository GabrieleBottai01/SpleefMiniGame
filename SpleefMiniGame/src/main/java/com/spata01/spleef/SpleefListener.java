package com.spata01.spleef;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class SpleefListener implements Listener {

    private final JavaPlugin plugin;
    private final GameManager gameManager;
    private final Set<Location> blocchiInRottura = new HashSet<>();

    public SpleefListener(JavaPlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            // Niente danni da caduta in attesa o durante il gioco
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (gameManager.getState() == GameManager.GameState.PLAYING ||
                        gameManager.getState() == GameManager.GameState.STARTING ||
                        gameManager.getState() == GameManager.GameState.ATTESA) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Ottimizzazione movimenti visuali (rimane valida per la rottura neve)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        if (gameManager.getState() != GameManager.GameState.PLAYING) return;
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) return;

        Block blockUnder = event.getPlayer().getLocation().subtract(0, 1, 0).getBlock();

        // NOTA: Il controllo Ossidiana è stato rimosso da qui e spostato nel GameManager (Game Loop)
        // Qui gestiamo solo lo SPLEEF (rottura neve)

        if (blockUnder.getType() == Material.SNOW_BLOCK) {
            gestisciRotturaNeve(blockUnder);
        }
    }

    private void gestisciRotturaNeve(Block block) {
        if (blocchiInRottura.contains(block.getLocation())) return;

        blocchiInRottura.add(block.getLocation());

        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getType() == Material.SNOW_BLOCK) {
                    block.setType(Material.AIR);
                    block.getWorld().playSound(block.getLocation(), org.bukkit.Sound.BLOCK_SNOW_BREAK, 1f, 1f);
                }
                blocchiInRottura.remove(block.getLocation());
            }
        }.runTaskLater(plugin, 10L); // 0.5 secondi
    }
}