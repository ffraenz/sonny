
package com.friederes.Sonny.Skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.friederes.Sonny.Bot;

public class NarratorSkill extends Skill implements Listener {

	public NarratorSkill(Bot bot) {
		super(bot);
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
}
