
package com.friederes.Sonny;

import org.bukkit.plugin.java.JavaPlugin;

public class SonnyPlugin
  extends JavaPlugin
{
  public Bot bot;

  public void onEnable() {
    this.bot = new Bot(this);
    this.bot.init();
  }

  public void onDisable() {
    this.bot.destroy();
    this.bot = null;
  }
}
