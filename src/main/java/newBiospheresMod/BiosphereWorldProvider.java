package newBiospheresMod;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import newBiospheresMod.Helpers.AvgCalc;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;
import newBiospheresMod.Models.ModConfig;

public class BiosphereWorldProvider extends WorldProviderSurface
{
	private ModConfig config;

	@Override
	public ChunkCoordinates getRandomizedSpawnPoint()
	{
		ChunkCoordinates coords = super.getRandomizedSpawnPoint();

		FixSpawnLocation(coords);

		return coords;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		ChunkCoordinates coords = super.getSpawnPoint();

		FixSpawnLocation(coords);

		return coords;
	}

	private boolean SpawnedOnTopOfDome(ChunkCoordinates coords)
	{
		// Sanity Checking
		if (coords == null) { return false; }

		World world = super.worldObj;
		if (!BiosphereWorldType.IsBiosphereWorld(world)) { return false; }

		if (config == null || config.World != world)
		{
			config = ModConfig.get(world);
		}

		if (coords.posY >= ModConsts.WORLD_MAX_Y) { return true; }

		int domeBlockCount = 0;

		for (double yo = -10; yo <= 10; yo++)
		{
			int y = (int)Math.round(coords.posY + yo);
			Block block = world.getBlock(coords.posX, y, coords.posZ);

			// System.out.println("SPAWN BLOCK [" + x + ", " + y + ", "
			// + z + "](" + yo + "): " + WoopMod.GetNameOrIdForBlock(block));

			if (block == config.getDomeBlock())
			{
				domeBlockCount++;

				if (domeBlockCount > 3)
				{
					// not dome world.
					return false;
				}
			}
			else if (block != Blocks.air)
			{
				if (block != config.getOutsideFillerBlock()) { return false; }
			}
		}

		return domeBlockCount >= 1; // spawned on top of a dome!!
	}

	private boolean ValidSpawnLocation(ChunkCoordinates coords)
	{
		if (coords == null) { return true; }

		World world = this.worldObj;
		if (world == null) { return true; }

		int x = coords.posX;
		int y = coords.posY;
		int z = coords.posZ;

		for (int i = 0; i < 3; i++)
		{
			Block block = world.getBlock(x, y + i, z);

			if (block != Blocks.air)
			{
				// trying to spawn in the middle of non-empty blocks
				return false;
			}
		}

		Block under = world.getBlock(x, y - 1, z);
		if (!under.isOpaqueCube()) { return false; }
		if (under.isAir(world, x, y - 1, z)) { return false; }

		return true;
	}

	private static final double searchGridSize = 4;
	private static final double searchGridAngles = 12;
	private static final double toRadians = Math.PI / (searchGridAngles / 2);

	private static final AvgCalc avg = new AvgCalc();
	private static long lastPrintedAt = Long.MIN_VALUE;

	public void FixSpawnLocation(ChunkCoordinates coords)
	{
		long startedAt = System.currentTimeMillis();

		if (BiosphereWorldType.IsBiosphereWorld(super.worldObj))
		{
			ChunkCoordinates orgCoords = Utils.GetCoords(coords);

			double angle = 0;
			double power = 1;

			while (SpawnedOnTopOfDome(coords) && !__TryFixSpawnLocation(coords))
			{
				angle++;
				if (angle >= searchGridAngles)
				{
					angle -= searchGridAngles;
					power++;
				}

				coords.posY = orgCoords.posY;

				if (power >= 20)
				{
					coords.posX = orgCoords.posX;
					coords.posZ = orgCoords.posZ;
					System.out.println("WARNING FAILED TO FIND A VALID SPAWN LOCATION!");

					return;
				}

				double x = Math.cos(angle * toRadians) * (power * searchGridSize);
				double z = Math.sin(angle * toRadians) * (power * searchGridSize);

				coords.posX = orgCoords.posX + (int)Math.round(x);
				coords.posZ = orgCoords.posZ + (int)Math.round(z);
			}

			long now = System.currentTimeMillis();
			long elapsed = now - startedAt;
			avg.addValue(elapsed / 1000d);

			if (elapsed >= 100)
			{
				System.out.printf("BIOSPHERE FIX SPAWN LOCATION TOOK %.3f SECONDS!%n", (elapsed / 1000d));
			}

			if (lastPrintedAt == Long.MIN_VALUE || (now - lastPrintedAt) > 2500)
			{
				lastPrintedAt = now;

				if (avg.getCount() >= 5)
				{
					double av = avg.getAverage();
					if (av >= .001D)
					{
						System.out.printf("BIOSPHERE FIX SPAWN LOCATION ON AVERAGE TAKES %.3f SECONDS.%n", av);
					}
				}
			}
		}
	}

	private boolean __TryFixSpawnLocation(ChunkCoordinates coords)
	{
		if (coords == null) { return false; }

		World world = this.worldObj;
		if (world == null) { return false; }

		boolean locationModified = false;
		int orgPosY = coords.posY;

		while (SpawnedOnTopOfDome(coords))
		{
			locationModified = true;
			coords.posY -= 8;

			if (coords.posY < 0)
			{
				coords.posY = orgPosY;
				return false;
			}

			while (!ValidSpawnLocation(coords))
			{
				coords.posY -= 1;
				if (coords.posY < 0)
				{
					coords.posY = orgPosY;
					return false;
				}
			}
		}

		return locationModified;
	}
}
