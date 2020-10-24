package com.friederes.Sonny.Skill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.friederes.Sonny.Bot;

public class ProtectSkill extends Skill implements Listener, CommandSkill
{
	protected Map<String,ArrayList<Location>> chunkProtectLocations;
	protected Material protectionBlockType = Material.LAPIS_BLOCK;
	
	protected int protectRadius = 16;
	
  public ProtectSkill(Bot bot) {
		super(bot);
	}
  
  public ArrayList<Location> getOrCreateProtectLocationsForChunk(Chunk chunk, boolean create) {
  	return getOrCreateProtectLocationsForChunk(chunk.getX(), chunk.getZ(), create);
  }
  
  public ArrayList<Location> getOrCreateProtectLocationsForChunk(int x, int z, boolean create) {
  	String key = String.format("%d;%d", x, z);
  	ArrayList<Location> protectLocations = chunkProtectLocations.get(key);
		if (create && protectLocations == null) {
			protectLocations = new ArrayList<Location>();
			chunkProtectLocations.put(key, protectLocations);
		}
		return protectLocations;
  }

  /**
   * Find a protect block location next to the given location, if it is protected.
   */
  public Location getProtectLocationNear(Location location) {
  	// Lookup protect block around spawn Y
  	int chunkX = location.getChunk().getX();
  	int chunkZ = location.getChunk().getZ();
  	int i, relX, relZ;
  	
  	Location protectLocation = null;
  	ArrayList<Location> protectLocations;

  	int chunkRadius = (int) Math.ceil(protectRadius / 16.0);
  	
  	relX = -chunkRadius - 1;
  	while (protectLocation == null && ++relX <= chunkRadius) {
  		relZ = -chunkRadius - 1;
  		while (protectLocation == null && ++relZ <= chunkRadius) {
  			// Retrieve protect blocks for this chunk
  	  	protectLocations = getOrCreateProtectLocationsForChunk(chunkX + relX, chunkZ + relZ, false);
  	  	i = -1;
  			while (protectLocation == null && protectLocations != null && ++i < protectLocations.size()) {
  				// Check if this protect block is in range
  	  		if (isLocationWithinSquare(location, protectLocations.get(i), protectRadius)) {
  	  			protectLocation = protectLocations.get(i);
  	  		}
  	  	}
    	}
  	}
  	
  	return protectLocation;
  }
  
  /**
   * Check wether a given location is inside a square around another location.
   */
  public boolean isLocationWithinSquare(Location location, Location squareCenter, int squareRadius) {
  	return (
  			Math.abs(location.getBlockX() - squareCenter.getBlockX()) <= squareRadius &&
  			Math.abs(location.getBlockZ() - squareCenter.getBlockZ()) <= squareRadius &&
  			Math.abs(location.getBlockY() - squareCenter.getBlockY()) <= squareRadius
  	);
  }

  /**
   * Scan chunk for protected locations.
   */
	public void scanChunk(Chunk chunk) {
		// Skip chunk, if already scanned
		if (getOrCreateProtectLocationsForChunk(chunk, false) != null) {
			return;
		}
		
		// Add chunk to hash table
		getOrCreateProtectLocationsForChunk(chunk, true);
		
		// Iterate through all blocks inside chunk and scan them
		int maxHeight = chunk.getWorld().getMaxHeight();
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < maxHeight; y++) {
					this.scanBlock(chunk.getBlock(x, y, z));
				}
			}
		}
	}

  /**
   * Determine wether a given block ensures protection.
   */
  public boolean scanBlock(Block block) {
		if (block.getType() == this.protectionBlockType) {
			getOrCreateProtectLocationsForChunk(block.getChunk(), true)
				.add(block.getLocation().clone());
			return true;
		}
		return false;
  }

  /**
   * Remove the given block as a protected location.
   */
	public boolean removeBlock(Block block) {
		Location location = block.getLocation();
		ArrayList<Location> protectLocations = getOrCreateProtectLocationsForChunk(block.getChunk(), false);
		if (protectLocations != null) {
			int index = -1;
			boolean found = false;
			while (!found && ++index < protectLocations.size()) {
				found = protectLocations.get(index).distance(location) == 0.0;
			}
			if (found) {
				protectLocations.remove(index);
				return true;
			}
  	}
		return false;
	}
  
	@Override
	public void enable() {
		// Prepare hash map
		chunkProtectLocations = new HashMap<>();
		
		// Scan loaded chunks
		for (World world : bot.getServer().getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				scanChunk(chunk);
			}
		}
	}

	@Override
	public void disable() {
		chunkProtectLocations = null;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntitySpawn(CreatureSpawnEvent event) {
		// Is hostile
		if (event.getEntity() instanceof Monster) {
			if (getProtectLocationNear(event.getLocation()) != null) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		// Scan old chunks async for protect blocks
		if (!event.isNewChunk()) {
			this.bot.getServer().getScheduler().runTaskAsynchronously(this.bot.getPlugin(), new Runnable() {
				public void run() {
					scanChunk(event.getChunk());
				}
			});
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		// Keep protection blocks of unloaded chunks in memory
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (scanBlock(event.getBlock())) {
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
		if (removeBlock(event.getBlock())) {
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
		Location protectLocation = getProtectLocationNear(player.getLocation());
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
