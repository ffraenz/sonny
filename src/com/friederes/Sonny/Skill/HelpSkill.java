
package com.friederes.Sonny.Skill;

import com.friederes.Sonny.Bot;
import org.bukkit.entity.Player;

public class HelpSkill extends Skill implements CommandSkill
{
  public HelpSkill(Bot bot) {
		super(bot);
	}

	/**
   * Test wether this command matches the given args.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if this command matches
   */
  public boolean test(Player sender, String[] args) {
    return true;
  }

  /**
   * Executes this command for the given player and arguments.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if successful
   */
  public boolean execute(Player sender, String[] args) {
    if (args.length == 1 && args[0].matches("^(help|hellef|\\?)$")) {
      this.bot.getVoiceManager().whisper(
        sender,
        "Type {home} to be teleported to the world spawn. " +
        "Use {bed} to get back to your bed. That's all I know.",
        new Object[] {
            "/sonny home",
            "/sonny bed",
            "/sonny secure"
        }
      );
    } else {
      this.bot.getVoiceManager().whisper(
        sender,
        "Unknown skill. Type {help} to get started",
        new Object[] { "/sonny help" }
      );
    }

    return true;
  }
}
