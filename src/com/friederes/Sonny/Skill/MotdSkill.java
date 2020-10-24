
package com.friederes.Sonny.Skill;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import com.friederes.Sonny.Bot;

public class MotdSkill extends Skill implements Listener {

	public MotdSkill(Bot bot) {
		super(bot);
	}

  @EventHandler
  public void onServerListPing(ServerListPingEvent event) {
    String message = ChatColor.GRAY + this.bot.getVoiceManager().serverName;
    String separator = ChatColor.GRAY + " ◆ ";

    Object[] players = this.bot.getServer().getOnlinePlayers().toArray();
    if (players.length > 0) {
      // Players online
      for (int i = 0; i < players.length; i++) {
        Player player = (Player)players[i];
        message +=
          separator +
          this.bot.getChatManager().getPlayerChatColor(player) +
          player.getName();
      }
    } else {
      // No players online
      message +=
        separator +
        ChatColor.RESET +
        this.bot.getVoiceManager().translate("No players online", null);
    }

    event.setMotd(message);
  }
}
