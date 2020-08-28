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
	protected Map<Chunk,ArrayList<Block>> chunkProtectBlocks;
	protected Material protectionBlockType = Material.LAPIS_BLOCK;
	
  public ProtectSkill(Bot bot) {
		super(bot);
	}
  
  public ArrayList<Block> getAndCreateProtectBlocksForChunk(Chunk chunk) {
  	ArrayList<Block> protectBlocks = chunkProtectBlocks.get(chunk);
		if (protectBlocks == null) {
			protectBlocks = new ArrayList<Block>();
			chunkProtectBlocks.put(chunk, protectBlocks);
		}
		return protectBlocks;
  }
  
  public Block getProtectBlockAt(Location location) {
  	// Retrieve protect blocks in location chunk
  	ArrayList<Block> protectBlocks = chunkProtectBlocks.get(location.getChunk());
  	if (protectBlocks == null) {
  		return null;
  	}
  	
  	// Lookup protect block around spawn Y
  	int spawnY = location.getBlockY();
  	int i = -1;
  	int y;
  	Block protectBlock = null;
  	
  	while (protectBlock == null && ++i < protectBlocks.size()) {
  		y = protectBlocks.get(i).getY();
  		if (spawnY >= y - 8 && spawnY < y + 8) {
  			protectBlock = protectBlocks.get(i);
  		}
  	}
  	
  	return protectBlock;
  }

	public void scanChunk(Chunk chunk) {
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
  
  public boolean scanBlock(Block block) {
		if (block.getType() == this.protectionBlockType) {
			this.getAndCreateProtectBlocksForChunk(block.getChunk()).add(block);
			return true;
		}
		return false;
  }

	public void removeChunk(Chunk chunk) {
		chunkProtectBlocks.remove(chunk);
	}
	
	public boolean removeBlock(Block block) {
		ArrayList<Block> protectBlocks = chunkProtectBlocks.get(block.getChunk());
		if (protectBlocks != null) {
			int i = protectBlocks.indexOf(block);
			if (i != -1) {
				protectBlocks.remove(i);
				return true;
			}
  	}
		return false;
	}
  
	@Override
	public void enable() {
		// Prepare hash map
		chunkProtectBlocks = new HashMap<>();
		
		// Scan loaded chunks
		for (World world : bot.getServer().getWorlds()) {
			for (Chunk chunk : world.getLoadedChunks()) {
				scanChunk(chunk);
			}
		}
	}

	@Override
	public void disable() {
		chunkProtectBlocks = null;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntitySpawn(CreatureSpawnEvent event) {
		// Is hostile
		if (event.getEntity() instanceof Monster) {
			if (getProtectBlockAt(event.getLocation()) != null) {
				event.setCancelled(true);
				System.out.println("Cancelled spawn event");
			}
		}
	}

	@Override
	public boolean test(Player player, String[] args) {
		return (args.length == 1 && args[0].matches("^(secure|secured|protect|protected|secher)$"));
	}

	@Override
	public boolean execute(Player player, String[] args) {
		Block protectBlock = getProtectBlockAt(player.getLocation());
		if (protectBlock != null) {
			this.bot.getVoiceManager().whisper(
	      player,
	      "Your current location is {protected} by the block at {location}",
	      new Object[] { "protected", protectBlock.getLocation() }
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

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		scanChunk(event.getChunk());
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		removeChunk(event.getChunk());
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
}
