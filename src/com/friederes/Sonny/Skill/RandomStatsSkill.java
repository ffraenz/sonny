
package com.friederes.Sonny.Skill;

import com.friederes.Sonny.Bot;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class RandomStatsSkill
  implements ProactiveSkill
{
  protected String[] statsPool = new String[] {
    "mob_kills",
    "player_kills",
    "deaths",
    "play_time",
    "distance",
    "jumps"
  };

  public Bot bot;

  /**
   * Constructor
   * @param bot Bot instance
   */
  public RandomStatsSkill(Bot bot) {
    this.bot = bot;
  }

  /**
   * Returns the chance (1/n) of this proactive skill to occur.
   * @return Chance value
   */
  public int getChance() {
    if (this.bot.getServer().getOnlinePlayers().size() == 0) {
      return 0;
    }

    return 32;
  }

  /**
   * Triggers this proactive skill.
   */
  public void execute() {
    int value, playerCount = this.bot.getServer().getOnlinePlayers().size();
    int playerIndex = (int)(playerCount * Math.random());
    Player player =
      (Player)this.bot.getServer().getOnlinePlayers().toArray()[playerIndex];

    String type = this.statsPool[(int)(this.statsPool.length * Math.random())];

    switch (type) {
    case "play_time":
      value = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000;
      this.bot.getVoiceManager().say(
        "{player} played for {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "hour" : "hours"
        }
      );
      break;

    case "deaths":
      value = player.getStatistic(Statistic.DEATHS);
      this.bot.getVoiceManager().say(
        "{player} died {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "time" : "times"
        }
      );
      break;

    case "player_kills":
      value = player.getStatistic(Statistic.PLAYER_KILLS);
      this.bot.getVoiceManager().say(
        "{player} killed {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "fellow player" : "fellow players"
        }
      );
      break;

    case "jumps":
      value = player.getStatistic(Statistic.JUMP);
      this.bot.getVoiceManager().say(
        "{player} jumped {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "time" : "times"
        }
      );
      break;

    case "distance":
      value = (
        player.getStatistic(Statistic.WALK_ONE_CM) +
        player.getStatistic(Statistic.SPRINT_ONE_CM)
      ) / 10;
      this.bot.getVoiceManager().say(
        "{player} walked {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "meter" : "meters"
        }
      );
      break;

    case "mob_kills":
      value = player.getStatistic(Statistic.MOB_KILLS);
      this.bot.getVoiceManager().say(
        "{player} killed {count} {unit} so far",
        new Object[] {
          player,
          Integer.valueOf(value),
          (value == 1) ? "mob" : "mobs"
        }
      );
      break;
    }
  }
}
