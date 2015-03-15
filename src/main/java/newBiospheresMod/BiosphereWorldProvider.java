/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import newBiospheresMod.Configuration.ModConfig;
import newBiospheresMod.Helpers.AvgCalc;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;
import newBiospheresMod.Models.Sphere;

public class BiosphereWorldProvider extends WorldProviderSurface
{
	private ModConfig config;

	@Override
	public ChunkCoordinates getRandomizedSpawnPoint()
	{
		ChunkCoordinates coords = super.getSpawnPoint();

		FixSpawnLocation(coords, true);

		return coords;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		ChunkCoordinates coords = super.getSpawnPoint();

		FixSpawnLocation(coords, false);

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

			/*if (block == config.getDomeBlock())
			{
				domeBlockCount++;

				if (domeBlockCount > 3)
				{
					// not dome world.
					return false;
				}
			}
			else*/ if (block != Blx.air)
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

			if (block != Blx.air)
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

	private static final double searchGridSize = 2.5d;
	private static final double searchGridAngles = 12;
	private static final double toRadians = Math.PI / (searchGridAngles / 2);

	private static final AvgCalc avg = new AvgCalc();
	private static long lastPrintedAt = Long.MIN_VALUE;

	public void FixSpawnLocation(ChunkCoordinates coords, boolean randomized)
	{
		long startedAt = System.currentTimeMillis();

		if (BiosphereWorldType.IsBiosphereWorld(super.worldObj))
		{
			Sphere sphere = Sphere.get(this.worldObj, coords.posX >> 4, coords.posZ >> 4);
			if (sphere == null)
			{
				if (ModConsts.DEBUG)
				{
					System.out.println("WARNING: COULDN'T FIND SPHERE, USING FALLBACK LOGIC TO FIND SPAWN LOCATION.");
				}

				FixSpawnLocation_fallback_logic(coords);
			}
			else
			{
				double xDist = coords.posX - sphere.sphereLocation.posX;
				double zDist = coords.posZ - sphere.sphereLocation.posZ;

				double dir = randomized
					? this.worldObj.rand.nextDouble() * (Math.PI * 2d)
					: Math.atan2(zDist, xDist);

				double min = sphere.hasLake ? (sphere.scaledLakeEdgeRadius + 1) : 0;
				double max = (sphere.scaledSphereRadius - 3);

				double dist = randomized
					? (min + (this.worldObj.rand.nextDouble() * (max - min)))
					: Math.sqrt(zDist * zDist + xDist * xDist);

				if (dist < min) dist = min;
				if (dist > max) dist = max;

				double vDist = Math.abs(dist - min) > Math.abs(dist - max) ? -1 : 1;
				int bumpCount = 0;

				while (true)
				{
					coords.posX = (int)Math.round(sphere.sphereLocation.posX + (Math.cos(dir) * dist));
					coords.posZ = (int)Math.round(sphere.sphereLocation.posZ + (Math.sin(dir) * dist));
					coords.posY = ModConsts.WORLD_HEIGHT;

					if (bumpCount >= 2)
					{
						if (ModConsts.DEBUG)
						{
							System.out
								.println(
								"WARNING: COULDN'T FIND VALID SPAWN LOCATION VIA NORMAL LOGIC, USING FALLBACK LOGIC TO FIX SPAWN LOCATION.");
						}
						FixSpawnLocation_fallback_logic(coords);
						break;
					}

					if (__TryFixSpawnLocation(coords))
					{
						break;
					}
					else
					{
						dist += vDist;

						if (dist < min)
						{
							dist = min;
							vDist *= -1;
							bumpCount++;
						}
						else if (dist > max)
						{
							dist = max;
							vDist *= -1;
							bumpCount++;
						}

						if (bumpCount >= 2)
						{
							dist = min + ((max - min) * 0.5d);
							vDist = 0;
						}
					}
				}

			}

			// ========== PERF MONITORING ==========

			long now = System.currentTimeMillis();
			long elapsed = now - startedAt;
			avg.addValue(elapsed / 1000d);

			if (elapsed >= 100)
			{
				System.out.printf("WARNING: BIOSPHERE FIX SPAWN LOCATION TOOK %.3f SECONDS!%n", (elapsed / 1000d));
			}

			if (lastPrintedAt == Long.MIN_VALUE || (now - lastPrintedAt) > 2500)
			{
				lastPrintedAt = now;

				if (avg.getCount() >= 5)
				{
					double av = avg.getAverage();
					if (av >= .02D)
					{
						System.out.printf("INFO: BIOSPHERE FIX SPAWN LOCATION ON AVERAGE TAKES %.3f SECONDS.%n", av);
					}
				}
			}
		}
	}

	public void FixSpawnLocation_fallback_logic(ChunkCoordinates coords)
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

			if (power >= 50)
			{
				coords.posX = orgCoords.posX;
				coords.posZ = orgCoords.posZ;

				if (ModConsts.DEBUG)
				{
					System.out.println("WARNING: BIOSPHERE FIX SPAWN LOCATION FAILED!!");
				}

				break;
			}

			double x = Math.cos(angle * toRadians) * (power * searchGridSize);
			double z = Math.sin(angle * toRadians) * (power * searchGridSize);

			coords.posX = orgCoords.posX + (int)Math.round(x);
			coords.posZ = orgCoords.posZ + (int)Math.round(z);
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
