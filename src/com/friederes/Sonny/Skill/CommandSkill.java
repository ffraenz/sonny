
package com.friederes.Sonny.Skill;

import org.bukkit.entity.Player;

public interface CommandSkill extends Skill {
  /**
   * Test wether this command matches the given args.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if this command matches
   */
  boolean test(Player player, String[] args);

  /**
   * Executes this command for the given player and arguments.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if successful
   */
  boolean execute(Player player, String[] args);
}
