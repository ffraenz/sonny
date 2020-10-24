
package com.friederes.Sonny;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class VoiceManager
{
  public Bot bot;
  public Map<String, ArrayList<String>> translations = new HashMap<>();

  public String serverName = "Sonny";
  public String botName = "Sonny";
  public String botColor = ChatColor.AQUA.toString();

  /**
   * Constructor
   * @param bot Bot instance
   */
  public VoiceManager(Bot bot) {
    this.bot = bot;
  }

  /**
   * Say something loudly in the chat so that everybody understands.
   * @param message Message to be said
   * @param args Message arguments
   */
  public void say(String message, Object... args) {
    message(null, message, args);
  }

  /**
   * Whispers something quietly to a multiple players.
   * @param receivers Players the message should be sent to
   * @param message Message to be whispered
   * @param args Message arguments
   */
  public void whisper(Iterable<Player> receivers, String message, Object... args) {
    for (Player receiver : receivers) {
      message(receiver, message, args);
    }
  }

  /**
   * Whispers something quietly to a single player.
   * @param receiver Player the message should be sent to
   * @param message Message to be whispered
   * @param args Message arguments
   */
  public void whisper(Player receiver, String message, Object... args) {
    message(receiver, message, args);
  }

  /**
   * Sends a message to the given receivers.
   * @param receiver Players the message should be delivered to
   * @param template Message to be delivered
   * @param args Message arguments
   */
  public void message(Player receiver, String template, Object[] args) {
    // Collect placeholders embedded in the message template
    Pattern pattern = Pattern.compile("\\{([^\\}\\s]+)\\}");
    Matcher matcher = pattern.matcher(template);
    ArrayList<String> placeholders = new ArrayList<>();

    while (matcher.find()) {
      String placeholder = matcher.group(1);
      if (!placeholders.contains(placeholder)) {
        placeholders.add(placeholder);
      }
    }

    // Handle the problem of placeholders and arguments not matching up
    if (placeholders.size() != args.length) {
      System.out.println(
        "[SonnyPlugin] Message template placeholders do not match the given args: " + template);
      return;
    }

    // Translate the message itself
    String message = translate(template, receiver);

    // Iterate through placeholders
    for (int i = 0; i < args.length; i++) {
      String placeholder = placeholders.get(i);
      Object object = args[i];
      String description = null;

      if (object instanceof String) {
        // Describe String argument
        description = translate((String)object, receiver);
      } else if (object instanceof Location) {
      	// Describe Location argument
      	Location location = (Location)object;
      	description = String.format("(%d,%d,%d)", location.getBlockX(), location.getBlockY(), location.getBlockZ());
      } else if (object instanceof Player) {
        // Describe Player argument
        Player player = (Player)object;
        String playerColor = this.bot.getChatManager().getPlayerChatColor(player);
        description = String.valueOf(playerColor) + "@" + player.getName();
      } else if (object instanceof Entity) {
        // Describe Entity argument
        Entity entity = (Entity)object;
        if (entity.getCustomName() != null) {
          // Use custom entity name if available
          description = entity.getCustomName();
        } else {
          // Check if this entity type has been translated
          String translationKey = String.format("entity_%s", entity.getType().toString());
          description = translate(translationKey, receiver);
          if (description.equals(translationKey)) {
            // Use generic entity type
            description = translate("entity_OTHER", receiver);
            System.out.println(
                "[SonnyPlugin] Missing translation: " + translationKey);
          }
        }
      } else if (object instanceof World) {
        // Describe world argument
        World world = (World)object;
        String worldNameTranslationKey = String.format("world_%s", world.getName());
        description = translate(worldNameTranslationKey, receiver);
        if (description.equals(worldNameTranslationKey)) {
          description = world.getName();
          System.out.println(
              "[SonnyPlugin] Missing translation: " + worldNameTranslationKey);
        }
      } else if (object instanceof Integer) {
        // Describe Integer argument
        NumberFormat numberFormatter =
          NumberFormat.getInstance(new Locale("de", "DE"));
        description = numberFormatter.format(object);
      }

      // Fulfill placeholder in message
      if (description != null) {
        message = message.replaceAll(
          String.format("\\{%s\\}", placeholder),
          String.valueOf(this.botColor) + description + ChatColor.RESET
        );
      }
    }
    
    // Uppercase first letter of the message
    // message = message.substring(0, 1).toUpperCase() + message.substring(1);

    // Inject bot name following the chat format
    message = String.format(
      this.bot.getChatManager().getChatFormat(this.botColor),
      new Object[] { this.botName, message });

    // Deliver message
    if (receiver == null) {
      this.bot.getServer().broadcastMessage(message);
    } else {
      receiver.sendMessage(message);
    }
  }

  public void broadcastPlayerDeath(Player player, EntityDamageEvent deathCause) {
    switch (deathCause.getCause()) {
    case DROWNING:
      say("{player} drowned", player);
      break;
    case FIRE:
    case FIRE_TICK:
      say("{player} burned to death", player);
      break;
    case FLY_INTO_WALL:
      say("{player} flew into a wall", player);
      break;
    case LAVA:
      say("{player} fell in lava", player);
      break;
    case POISON:
      say("{player} died due to poison", player);
      break;
    case ENTITY_ATTACK:
    case ENTITY_EXPLOSION:
    case ENTITY_SWEEP_ATTACK:
      Entity damager = ((EntityDamageByEntityEvent) deathCause).getDamager();
      say("{player} has been killed by {entity}", player, damager);
      break;
    case PROJECTILE:
      Projectile projectile = (Projectile) ((EntityDamageByEntityEvent) deathCause).getDamager();
      if (projectile.getShooter() instanceof Entity) {
        damager = (Entity) projectile.getShooter();
        say("{player} has been shot by {entity}", player, damager);
      } else {
        say("{player} has been shot", player);
      }
      break;
    case FALL:
      say("{player} fell from a high place", player);
      break;
    case STARVATION:
      say("{player} starved to death", player);
      break;
    case SUFFOCATION:
      say("{player} suffocated", player);
      break;
    default:
      say("{player} died", player);
    }
  }

  /**
   * Translates a string for the given player.
   * @param string String to be translated
   * @param player Player the string is being translated for (optional)
   * @return Translated string
   */
  public String translate(String string, Player player) {
    if (this.translations.containsKey(string)) {
      ArrayList<String> choices = this.translations.get(string);
      if (choices.size() > 0) {
        return choices.get((int)(Math.random() * choices.size()));
      }
    }
    return string;
  }

  /**
   * Configures the voice manager.
   * @param config Config file
   */
  public void configure(FileConfiguration config) {
    if (config.getString("server.name") != null) {
      this.serverName = config.getString("server.name");
    }

    if (config.getString("bot.name") != null) {
      this.botName = config.getString("bot.name");
    }

    List<Map<?, ?>> entries = config.getMapList("translations");
    for (Map<?, ?> entry : entries) {

      if (!entry.containsKey("key") ||
        !entry.containsKey("translations")) {
        continue;
      }

      Object rawKey = entry.get("key");
      if (!(rawKey instanceof String)) {
        continue;
      }

      Object rawTranslations = entry.get("translations");
      ArrayList<String> entryTranslations = new ArrayList<>();

      if (rawTranslations instanceof String) {
        entryTranslations.add((String)rawTranslations);
      }

      if (rawTranslations instanceof ArrayList) {
        for (Object choice : (ArrayList<?>) rawTranslations) {
          if (choice instanceof String) {
            entryTranslations.add((String)choice);
          }
        }
      }

      if (entryTranslations.size() > 0) {
        this.translations.put((String)rawKey, entryTranslations);
      }
    }
  }
}
