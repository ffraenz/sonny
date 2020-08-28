
package com.friederes.Sonny;

import com.friederes.Sonny.Skill.CommandSkill;
import com.friederes.Sonny.Skill.ProactiveSkill;
import com.friederes.Sonny.Skill.Skill;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SkillManager
{
  protected Bot bot;
  public Skill[] skills;
  protected int randomEventTickInterval = 12000;

  protected BukkitRunnable randomEventScheduler;

  /**
   * Constructor
   * @param bot Bot instance
   * @param skills Array of skills to be installed
   */
  public SkillManager(Bot bot, Skill[] skills) {
    this.bot = bot;
    this.skills = skills;
    
    // Enable skills initially
    for (Skill skill : skills) {
    	enableSkill(skill);
    }
  }
  
  public void enableSkill(Skill skill) {
  	skill.enable();
  	
  	// Register events
  	if (skill instanceof Listener) {
      this.bot.getPluginManager().registerEvents((Listener)skill, (Plugin)this.bot.getPlugin());
  	}
  	
  	System.out.println(String.format("[SonnyPlugin] Skill %s enabled.", skill.getClass().getSimpleName()));
  }
  
  public void disableSkill(Skill skill) {
  	// Unregister events
  	if (skill instanceof Listener) {
  		HandlerList.unregisterAll((Listener)skill);
  	}

  	skill.disable();

  	System.out.println(String.format("[SonnyPlugin] Skill %s disabled.", skill.getClass().getSimpleName()));
  }

  /**
   * Handles an incoming command.
   * @param sender Command sender
   * @param args Command arguments
   * @return True, if command was successful
   */
  public boolean handleCommand(CommandSender sender, String[] args) {
    // Handle non-player command senders
    if (!(sender instanceof Player)) {
      sender.sendMessage("I can only respond to players");
    }

    Player senderPlayer = (Player)sender;

    // Find matching skill
    CommandSkill skill = null;
    int i = -1;
    while (skill == null && ++i < this.skills.length) {
      if (this.skills[i] instanceof CommandSkill && (
        (CommandSkill)this.skills[i]).test(senderPlayer, args)) {
        skill = (CommandSkill)this.skills[i];
      }
    }

    // Execute skill, if any
    if (skill != null) {
      return skill.execute(senderPlayer, args);
    }

    return false;
  }

  /**
   * Handles random event tick.
   */
  public void handleRandomEventTick() {
    ProactiveSkill skill = null;
    int i = 0;
    while (skill == null && i < this.skills.length) {
      if (this.skills[i] instanceof ProactiveSkill) {
        double random = Math.random();
        int chance = ((ProactiveSkill)this.skills[i]).getChance();
        if (chance > 0 && random < 1.0D / chance) {
          skill = (ProactiveSkill)this.skills[i];
        }
      }
      i++;
    }

    if (skill != null) {
      skill.execute();
    }
  }

  /**
   * Schedules random event ticks.
   */
  public void scheduleRandomEvents() {
    if (this.randomEventScheduler == null) {
      this.randomEventScheduler = new BukkitRunnable() {
        public void run() {
          SkillManager.this.handleRandomEventTick();
        }
      };
      this.randomEventScheduler.runTaskTimer(
        (Plugin)this.bot.getPlugin(),
        this.randomEventTickInterval,
        this.randomEventTickInterval
      );
    }
  }

  /**
   * Disables random events.
   */
  public void disableRandomEvents() {
    if (this.randomEventScheduler != null) {
      this.randomEventScheduler.cancel();
      this.randomEventScheduler = null;
    }
  }
}
