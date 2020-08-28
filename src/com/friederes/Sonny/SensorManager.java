
package com.friederes.Sonny;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class SensorManager
  implements Listener, CommandExecutor
{
  public Bot bot;

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

  public boolean onCommand(
    CommandSender sender,
    Command cmd,
    String label,
    String[] args
  ) {
    return this.bot.getSkillManager().handleCommand(sender, args);
  }
}
