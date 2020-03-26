
package com.friederes.Sonny;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class SensorManager
  implements Listener, CommandExecutor
{
  public Bot bot;

  protected int sleepDelay = 5 * 20;
  protected Map<Player, Integer> sleepTransitions = new HashMap<Player, Integer>();

  /**
   * Constructor
   * @param bot Bot instance
   */
  public SensorManager(Bot bot) {
    this.bot = bot;
  }

  @EventHandler
  public void onServerListPing(ServerListPingEvent event) {
    event.setMotd(this.bot.getVoiceManager().composeMotd());
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.setJoinMessage(null);
    this.bot.getChatManager().allocatePlayerChatColor(event.getPlayer());
    this.bot.getVoiceManager().say("{player} joined the game", event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    event.setQuitMessage(null);
    this.bot.getVoiceManager().say("{player} left the game", event.getPlayer());
    this.bot.getChatManager().deallocatePlayerChatColor(event.getPlayer());
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    bot.getVoiceManager().broadcastPlayerDeath(event.getEntity(), event.getEntity().getLastDamageCause());
    event.setDeathMessage(null);
  }

  @EventHandler
  public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
    if (bot.isMultiplayer()) {
      this.bot.getVoiceManager().say("{player} changed to {world}", event.getPlayer(), event.getPlayer().getWorld());
    }
  }

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setFormat(this.bot.getChatManager().getChatFormat(event.getPlayer()));
  }

  @EventHandler
  public void onSleep(PlayerBedEnterEvent event) {
    if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
      // Broadcast message to players in the same world
      List<Player> worldPlayers = event.getPlayer().getWorld().getPlayers();
      if (worldPlayers.size() > 1) {
        this.bot.getVoiceManager().whisper(worldPlayers, "{player} went to bed", event.getPlayer());
      }

      // Schedule sleep delayed task
      int transition = bot.getServer().getScheduler().scheduleSyncDelayedTask(bot.getPlugin(), new Runnable() {
        public void run() {
          // Clear sleep transitions
          for (Player player : sleepTransitions.keySet()) {
            int transition = sleepTransitions.get(player);
            sleepTransitions.put(player, 0);

            if (player != event.getPlayer() && transition != 0) {
              bot.getServer().getScheduler().cancelTask(transition);
            }
          }

          // Transition to next day
          bot.transitionToNextDay(event.getPlayer().getWorld());
        }
      }, sleepDelay);

      sleepTransitions.put(event.getPlayer(), transition);
    }
  }

  @EventHandler
  public void onSleepCancel(PlayerBedLeaveEvent event) {
    int transition = sleepTransitions.get(event.getPlayer());
    if (transition != 0) {
      // Cancel ongoing sleep transition
      sleepTransitions.put(event.getPlayer(), 0);
      bot.getServer().getScheduler().cancelTask(transition);

      // Broadcast message to players in the same world
      List<Player> worldPlayers = event.getPlayer().getWorld().getPlayers();
      if (worldPlayers.size() > 1) {
        this.bot.getVoiceManager().whisper(worldPlayers, "{player} left bed", event.getPlayer());
      }
    }
  }

  public boolean onCommand(
    CommandSender sender,
    Command cmd,
    String label,
    String[] args
  ) {
    return this.bot.getSkillManager().handleCommand(sender, args);
  }
}