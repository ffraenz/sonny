
package com.friederes.Sonny;

import com.friederes.Sonny.Skill.HelpSkill;
import com.friederes.Sonny.Skill.RandomStatsSkill;
import com.friederes.Sonny.Skill.Skill;
import com.friederes.Sonny.Skill.TeleportSkill;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class Bot
{
  protected SonnyPlugin plugin;
  protected PluginManager pluginManager;
  protected SensorManager sensorManager;
  protected ChatManager chatManager;
  protected SkillManager skillManager;
  protected VoiceManager voiceManager;

  /**
   * Constructor
   * @param plugin Plugin instance
   */
  public Bot(SonnyPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Initialize plugin
   */
  public void init() {
    this.pluginManager = this.plugin.getServer().getPluginManager();
    this.pluginManager.registerEvents(getSensorManager(), (Plugin)this.plugin);

    getVoiceManager().configure(this.plugin.getConfig());

    Skill[] skills = {
      (Skill)new TeleportSkill(this),
      (Skill)new HelpSkill(this),
      (Skill)new RandomStatsSkill(this)
    };

    this.skillManager = new SkillManager(this, skills);
    System.out.println(String.format("[SonnyPlugin] %d skills activated.", new Object[] { Integer.valueOf(skills.length) }));

    this.plugin.getCommand("sonny").setExecutor(getSensorManager());

    for (Player player : getServer().getOnlinePlayers()) {
      getChatManager().allocatePlayerChatColor(player);
    }

    getSkillManager().scheduleRandomEvents();
  }

  /**
   * Tear down plugin
   */
  public void destroy() {
    getSkillManager().disableRandomEvents();
  }

  /**
   * Returns the plugin instance
   */
  public SonnyPlugin getPlugin() {
    return this.plugin;
  }

  /**
   * Returns the server instance
   */
  public Server getServer() {
    return this.plugin.getServer();
  }

  /**
   * Returns the skill manager instance
   */
  public SkillManager getSkillManager() {
    return this.skillManager;
  }

  /**
   * Returns the sensor manager instance
   */
  public SensorManager getSensorManager() {
    if (this.sensorManager == null) {
      this.sensorManager = new SensorManager(this);
    }
    return this.sensorManager;
  }

  /**
   * Returns the chat manager instance
   */
  public ChatManager getChatManager() {
    if (this.chatManager == null) {
      this.chatManager = new ChatManager(this);
    }
    return this.chatManager;
  }

  /**
   * Returns the voice manager instance
   */
  public VoiceManager getVoiceManager() {
    if (this.voiceManager == null) {
      this.voiceManager = new VoiceManager(this);
    }
    return this.voiceManager;
  }

  /**
   * Returns wether multiple players are currently present on the server.
   * @return True, if multiple players are currently present
   */
  public boolean isMultiplayer() {
    return this.getServer().getOnlinePlayers().size() > 1;
  }

  /**
   * Transitions the given world to the next day.
   * @param world World to transition time in
   */
  public void transitionToNextDay(World world) {
    // Clear storms
    if (world.hasStorm()) {
        world.setStorm(false);
    }

    // Clear thunderings
    if (world.isThundering()) {
        world.setThundering(false);
    }

    // Set daytime
    long relativeTime = 24000 - world.getTime();
    System.out.println(world.getFullTime());
    world.setFullTime(world.getFullTime() + relativeTime);
  }
}
