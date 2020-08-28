package com.friederes.Sonny.Skill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

import com.friederes.Sonny.Bot;

public class BedSkill extends Skill implements Listener {

  protected int sleepDelay = 5 * 20;
  protected Map<Player, Integer> sleepTransitions = new HashMap<Player, Integer>();

	public BedSkill(Bot bot) {
		super(bot);
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
  
}
