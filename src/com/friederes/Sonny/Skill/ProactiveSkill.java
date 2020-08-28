
package com.friederes.Sonny.Skill;

public interface ProactiveSkill {
  /**
   * Returns the chance (1/n) of this proactive skill to occur.
   * @return Chance value
   */
  int getChance();

  /**
   * Triggers this proactive skill.
   */
  void execute();
}
