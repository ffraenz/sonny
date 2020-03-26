
package com.friederes.Sonny;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatManager
{
  protected Bot bot;

  public String[] colorPool = new String[] {
    ChatColor.RED.toString(),
    ChatColor.YELLOW.toString(),
    ChatColor.GREEN.toString(),
    ChatColor.LIGHT_PURPLE.toString(),
    ChatColor.BLUE.toString()
  };

  private Map<Player, String> playerChatColors = new HashMap<>();

  /**
   * Constructor
   * @param bot Bot instance
   */
  public ChatManager(Bot bot) {
    this.bot = bot;
  }

  /**
   * Allocates a chat color to the given player.
   * @param player Player in question
   */
  public void allocatePlayerChatColor(Player player) {
    if (this.playerChatColors.containsKey(player)) {
      return;
    }

    Collection<String> usedColors = this.playerChatColors.values();
    int minPlayerCount = Integer.MAX_VALUE;
    int minPlayerCountIndex = 0;

    for (int i = 0; i < this.colorPool.length; i++) {
      int playerCount = Collections.frequency(usedColors, this.colorPool[i]);
      if (playerCount < minPlayerCount) {
        minPlayerCount = playerCount;
        minPlayerCountIndex = i;
      }
    }

    this.playerChatColors.put(player, this.colorPool[minPlayerCountIndex]);
  }

  /**
   * Deallocates a player chat color.
   * @param player Player in question
   */
  public void deallocatePlayerChatColor(Player player) {
    this.playerChatColors.remove(player);
  }

  /**
   * Returns the current player chat color.
   * @param player Player in question
   * @return Chat color
   */
  public String getPlayerChatColor(Player player) {
    if (!this.playerChatColors.containsKey(player)) {
      return ChatColor.GRAY.toString();
    }
    return this.playerChatColors.get(player);
  }

  /**
   * Returns the chat format for the given player.
   * @param player Player
   * @return Chat format
   */
  public String getChatFormat(Player player) {
    return getChatFormat(getPlayerChatColor(player));
  }

  /**
   * Returns the chat format for the given chat color.
   * @param playerColor Chat color
   * @return Chat format
   */
  public String getChatFormat(String playerColor) {
    String reset = ChatColor.RESET.toString();
    return String.valueOf(playerColor) + "<%1$s>" + reset + " %2$s";
  }
}
