
package com.friederes.Sonny.Skill;

import com.friederes.Sonny.Bot;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportSkill
  implements CommandSkill
{
  public Bot bot;

  /**
   * Constructor
   * @param bot Bot instance
   */
  public TeleportSkill(Bot bot) {
    this.bot = bot;
  }

  /**
   * Test wether this command matches the given args.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if this command matches
   */
  public boolean test(Player sender, String[] args) {
    return (args.length == 1 && args[0].matches("^(home|spawn|heem|bed|bett)$"));
  }

  /**
   * Executes this command for the given player and arguments.
   * @param sender Sender
   * @param args Command arguments
   * @return True, if successful
   */
  public boolean execute(Player sender, String[] args) {
    boolean worldSpawn = args[0].matches("^(home|spawn|heem)$");
    Location destination = worldSpawn
        ? sender.getWorld().getSpawnLocation()
        : sender.getBedSpawnLocation();

    if (destination == null) {
      this.bot.getVoiceManager().whisper(sender, "Your destination is not available");
      return false;
    }

    teleportSafely(sender, destination);

    this.bot.getVoiceManager().say("{player} teleported {to_destination}", sender, worldSpawn ? "to home" : "to bed");
    return true;
  }

  /**
   * Verify destination coordinates to make sure it is save to teleport to.
   * @param entity Entity to be teleported
   * @param location Destination
   */
  public void teleportSafely(Entity entity, Location location) {
    float yaw = entity.getLocation().getYaw();
    float pitch = entity.getLocation().getPitch();
    int x = (int)location.getX();
    int y = (int)location.getY() - 1;
    int z = (int)location.getZ();
    int height = (int)Math.ceil(entity.getBoundingBox().getHeight());

    // Check wether destination is safe
    boolean safe = false;
    while (!safe && ++y < location.getWorld().getMaxHeight()) {
      int i = 0;
      safe = true;
      while (safe && i < height) {
        safe = location.getWorld().getBlockAt(x, y + i, z).getType().isAir();
        i++;
      }
    }

    Location origin = entity.getLocation();
    Location destination = new Location(
      location.getWorld(),
      x + 0.5D,
      y,
      z + 0.5D,
      yaw,
      pitch
    );

    // Teleport entity
    entity.teleport(destination, PlayerTeleportEvent.TeleportCause.PLUGIN);

    // Trigger effect at origin
    origin.getWorld().spawnParticle(Particle.SPELL_INSTANT, origin, 30);
    origin.getWorld().playSound(
      destination,
      Sound.ENTITY_ENDERMAN_TELEPORT,
      10.0F,
      1.0F
    );

    // Trigger effect at destination
    location.getWorld().spawnParticle(Particle.SPELL_INSTANT, destination, 30);
    location.getWorld().playSound(
      destination,
      Sound.ENTITY_ENDERMAN_TELEPORT,
      10.0F,
      1.0F
    );
  }
}
