package com.friederes.Sonny.Skill;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.friederes.Sonny.Bot;

public class ChatSkill extends Skill implements Listener {

	public ChatSkill(Bot bot) {
		super(bot);
	}

  @EventHandler
  public void onPlayerChat(AsyncPlayerChatEvent event) {
    event.setFormat(this.bot.getChatManager().getChatFormat(event.getPlayer()));
  }
}
