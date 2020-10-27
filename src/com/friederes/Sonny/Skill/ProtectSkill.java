package com.friederes.Sonny.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

import com.friederes.Sonny.Bot;

public class ProtectSkill extends Skill implements Listener, CommandSkill
{
	protected Map<String,ArrayList<Location>> chunkProtectionSources;
	protected Material protectionBlockType = Material.LAPIS_BLOCK;
	protected int protectRadius = 16;
	
	/**
	 * Constructor
	 * @param bot
	 */
  public ProtectSkill(Bot bot) {
		super(bot);
	}
  
  /**
   * Check wether the given location is a protection source.
   * @param location Location to be checked
   * @return True, if it is a protection source
   */
  public boolean isProtectionSource(Location location) {
  	return location.getBlock().getType() == this.protectionBlockType;
  }

  /**
   * Return known protection sources in the given chunk.
   * Lazily scans the chunk, if not already done, yet.
   * @param chunk
   * @return Array of protection source locations
   */
  public ArrayList<Location> getChunkProtectionSources(Chunk chunk) {
  	String key = String.format("%s-%d;%d", chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
  	ArrayList<Location> protectLocations = chunkProtectionSources.get(key);
		if (protectLocations == null) {
			// Create location list for chunk
			protectLocations = new ArrayList<Location>();
			chunkProtectionSources.put(key, protectLocations);
			
			// Iterate through all blocks inside chunk checking for protect locations
			Location blockLocation;
			int maxHeight = chunk.getWorld().getMaxHeight();
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					for (int y = 0; y < maxHeight; y++) {
						blockLocation = chunk.getBlock(x, y, z).getLocation();
						if (isProtectionSource(blockLocation)) {
							protectLocations.add(blockLocation.clone());
						}
					}
				}
			}
		}
		return protectLocations;
  }

  /**
   * Update a protection source at the given location.
   * @return True, if protection source has been altered at the given location
   */
  public boolean updateProtectionSource(Location location) {
  	// Search for existing protection sources inside the same chunk
		ArrayList<Location> protectionSources = getChunkProtectionSources(location.getChunk());
		int index = -1;
		boolean found = false;
		while (!found && ++index < protectionSources.size()) {
			found = protectionSources.get(index).distance(location) == 0.0;
		}

		// Either add or remove protection source for the given location
  	boolean protectionSource = isProtectionSource(location);  	
		if (found && !protectionSource) {
			// Remove protection source
			protectionSources.remove(index);
			return true;
		} else if (!found && protectionSource) {
			// Add protection source
			protectionSources.add(location);
			return true;
		}
		return false;
  }

  /**
   * Find a protection source next to the given location, if it is protected.
   * @param location
   * @return Protection source location, if protected
   */
  public Location getProtectionSourceNear(Location location) {
  	// Lookup protect block around spawn Y
  	int chunkX = location.getChunk().getX();
  	int chunkZ = location.getChunk().getZ();
  	int i, relX, relZ;
  	
  	Location protectionSource = null;
  	ArrayList<Location> protectionSources;

  	int chunkRadius = (int) Math.ceil(protectRadius / 16.0);
  	Chunk chunk;
  	
  	relX = -chunkRadius - 1;
  	while (protectionSource == null && ++relX <= chunkRadius) {
  		relZ = -chunkRadius - 1;
  		while (protectionSource == null && ++relZ <= chunkRadius) {
  			// Retrieve protect blocks for this chunk
  			chunk = location.getWorld().getChunkAt(chunkX + relX, chunkZ + relZ);
  	  	protectionSources = getChunkProtectionSources(chunk);
  	  	i = -1;
  			while (protectionSource == null && ++i < protectionSources.size()) {
  				// Check if this protect block is in range
  	  		if (isLocationWithinProtectionSourceRange(location, protectionSources.get(i))) {
  	  			protectionSource = protectionSources.get(i);
  	  		}
  	  	}
    	}
  	}
  	
  	return protectionSource;
  }
  
  /**
   * Check wether a given location is inside a square around another location.
   * @param location
   * @param squareCenter
   * @return True, if the given location is within range
   */
  public boolean isLocationWithinProtectionSourceRange(Location location, Location squareCenter) {
  	return (
  			Math.abs(location.getBlockX() - squareCenter.getBlockX()) <= protectRadius &&
  			Math.abs(location.getBlockZ() - squareCenter.getBlockZ()) <= protectRadius &&
  			Math.abs(location.getBlockY() - squareCenter.getBlockY()) <= protectRadius
  	);
  }
  
	@Override
	public void enable() {
		// Prepare hash map
		chunkProtectionSources = new HashMap<>();
		
		// Scan loaded chunks in advance
		for (World world : bot.getServer().getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				getChunkProtectionSources(chunk);
			}
		}
	}

	@Override
	public void disable() {
		chunkProtectionSources = null;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntitySpawn(CreatureSpawnEvent event) {
		// Is hostile
		if (event.getEntity() instanceof Monster) {
			if (getProtectionSourceNear(event.getLocation()) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (updateProtectionSource(event.getBlock().getLocation())) {
			// Play activate sound
			event.getBlock().getLocation().getWorld().playSound(
	    	event.getBlock().getLocation(),
	      Sound.BLOCK_BEACON_ACTIVATE,
	      10.0F,
	      1.0F
	    );
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (updateProtectionSource(event.getBlock().getLocation())) {
			// Play deactivate sound
			event.getBlock().getLocation().getWorld().playSound(
	    	event.getBlock().getLocation(),
	      Sound.BLOCK_BEACON_DEACTIVATE,
	      10.0F,
	      1.0F
	    );
		}
	}

	@Override
	public boolean test(Player player, String[] args) {
		return (args.length == 1 && args[0].matches("^(secure|secured|protect|protected|secher)$"));
	}

	@Override
	public boolean execute(Player player, String[] args) {
		Location protectLocation = getProtectionSourceNear(player.getLocation());
		if (protectLocation != null) {
			this.bot.getVoiceManager().whisper(
	      player,
	      "Your current location is {protected} by the block at {location}",
	      new Object[] { "protected", protectLocation }
	    );
		} else {
			this.bot.getVoiceManager().whisper(
	      player,
	      "Your current location is {protected}",
	      new Object[] { "not protected" }
	    );
		}
		return true;
	}
}
