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

		FixSpawnLocation(coords); //, true);

		return coords;
	}

	@Override
	public ChunkCoordinates getSpawnPoint()
	{
		ChunkCoordinates coords = super.getSpawnPoint();

		FixSpawnLocation(coords); //, false);

		return coords;
	}

	private boolean ValidSpawnLocation(ChunkCoordinates coords)
	{
		if (coords == null) { return true; }

		World world = this.worldObj;
		if (world == null) { return true; }

		int x = coords.posX;
		int z = coords.posZ;

		for (int y = 0; y < 256; y++)
		{
			Block block = world.getBlock(x, y, z);
			if (block != Blx.air && !block.isAir(world, x, y, z))
			{
				return true;
			}
		}

		// no solid ground at this location!
		return false;
	}

	private static final double searchGridSize = 2.5d;
	private static final double searchGridAngles = 12;
	private static final double toRadians = Math.PI / (searchGridAngles / 2);

	private static final AvgCalc avg = new AvgCalc();
	private static long lastPrintedAt = Long.MIN_VALUE;

	public void FixSpawnLocation(ChunkCoordinates coords)
	{
		ChunkCoordinates orgCoords = Utils.GetCoords(coords);

		double angle = 0;
		double power = 1;

		while (!ValidSpawnLocation(coords))
		{
			angle++;
			if (angle >= searchGridAngles)
			{
				angle -= searchGridAngles;
				power++;
			}

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
}
