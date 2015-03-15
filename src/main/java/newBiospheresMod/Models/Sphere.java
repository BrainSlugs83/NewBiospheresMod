/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import newBiospheresMod.BiosphereChunkProvider;
import newBiospheresMod.BlockEntry;
import newBiospheresMod.Configuration.ModConfig;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.Creator;
import newBiospheresMod.Helpers.Func2;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.TopDownBoundingBox;
import newBiospheresMod.Helpers.Utils;

public class Sphere
{
	// #region Caching

	private static class CacheKey
	{
		public final int x;
		public final int z;
		public final BiosphereChunkProvider chunkProvider;

		public CacheKey(final BiosphereChunkProvider chunkProvider, final int x, final int z)
		{
			this.chunkProvider = chunkProvider;
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) { return false; }
			if (this == obj) { return true; }
			if (!(obj instanceof CacheKey)) { return false; }
			CacheKey other = (CacheKey)obj;

			return this.x == other.x && this.z == other.z && this.chunkProvider == other.chunkProvider;
		}

		@Override
		public int hashCode()
		{
			int chunkProviderHash = (chunkProvider == null) ? 0 : chunkProvider.hashCode();
			return ((x & 0xFFFF) | ((z & 0xFFFF) << 16)) ^ chunkProviderHash ^ 438129048;
		}
	}

	private static LruCacheList<Sphere> spheresCache = new LruCacheList<Sphere>(12, new IKeyProvider<Sphere>()
	{
		@Override
		public Object provideKey(Sphere item)
		{
			if (item == null) { return null; }
			return new CacheKey(item.chunkProvider, item.sphereLocation.posX, item.sphereLocation.posZ);
		}
	});

	public static Sphere get(final World world, final int chunkX, final int chunkZ)
	{
		if (world == null) { return null; }
		final BiosphereChunkProvider provider = BiosphereChunkProvider.get(world);
		return get(provider, chunkX, chunkZ);
	}

	public static Sphere get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ)
	{
		if (chunkProvider == null) { return null; }

		ChunkCoordinates coords = GetSphereCenter(chunkX, chunkZ, chunkProvider.config);
		Object key = new CacheKey(chunkProvider, coords.posX, coords.posZ);

		return spheresCache.FindOrAdd(key, new Creator<Sphere>()
		{
			@Override
			public Sphere create()
			{
				return new Sphere(chunkProvider, chunkX, chunkZ);
			}
		});
	}

	// #endregion

	// #region Fields

	private final int centerChunkX, centerChunkZ;
	private final NoiseChunk centerNoiseChunk;
	private final long seed;

	public final BiosphereChunkProvider chunkProvider;

	public final ChunkCoordinates sphereLocation;
	public final ChunkCoordinates orbLocation;
	public final ChunkCoordinates lakeLocation;
	public final boolean lavaLake;
	public final boolean hasLake;

	public final int scaledLakeEdgeRadius;
	public final int scaledOrbRadius;
	public final int scaledSphereRadius;
	public final int scaledLakeRadius;

	public final BiomeGenBase biome;

	public final double scale;
	public final boolean isNoiseEnabled;
	public final int scaledGridSize;
	public final int bridgeWidth;

	private TopDownBoundingBox orbStairwayBox;
	private Map<ChunkCoordinates, Block> orbStairwayBlx;

	private List<TopDownBoundingBox> boundingBoxes = null;

	public final int seaLevel;

	public final int sphereType;

	// #endregion

	private Sphere(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkProvider = chunkProvider;
		ModConfig _cfg = this.chunkProvider.config;

		this.seaLevel = _cfg.getSeaLevel();
		this.scale = _cfg.getScale();
		this.isNoiseEnabled = _cfg.isNoiseEnabled();
		this.scaledGridSize = _cfg.getScaledGridSize();
		this.bridgeWidth = _cfg.getBridgeWidth();
		this.scaledOrbRadius = _cfg.getScaledOrbRadius();

		this.sphereLocation = GetSphereCenter(chunkX, chunkZ, _cfg);
		centerChunkX = sphereLocation.posX >> 4;
		centerChunkZ = sphereLocation.posZ >> 4;

		if (this.isNoiseEnabled)
		{
			centerNoiseChunk = NoiseChunk.get(chunkProvider.world, centerChunkX, centerChunkZ,
				this.chunkProvider.noiseGenerator, this.scale, this.seaLevel);
		}
		else
		{
			centerNoiseChunk = null;
		}

		// Correct the Y value if Noise is enabled.
		this.sphereLocation.posY = getRawSurfaceLevel(sphereLocation.posX, sphereLocation.posZ);

		// Seed local random number generator
		Random rnd = new Random(chunkProvider.worldSeed);
		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;
		long _seed = (sphereLocation.posX * xm + sphereLocation.posZ * zm) * 2512576L ^ chunkProvider.worldSeed;
		rnd.setSeed(_seed);

		double minRad = _cfg.getMinSphereRadius() * scale;
		double maxRad = _cfg.getMaxSphereRadius() * scale;

		double radRange = (maxRad - minRad);

		// Get sphere radius
		this.scaledSphereRadius = (int)Math.round(minRad + (rnd.nextDouble() * radRange));

		// Get lake radius
		double lakeRatio = _cfg.getMinLakeRatio()
			+ ((_cfg.getMaxLakeRatio() - _cfg.getMinLakeRatio()) * rnd.nextDouble());
		this.scaledLakeRadius = (int)Math.round(this.scaledSphereRadius * lakeRatio);
		this.scaledLakeEdgeRadius = scaledLakeRadius + 2;

		// Get the biome for the sphere.
		this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(sphereLocation.posX,
			sphereLocation.posZ);

		// is it a lava lake?
		this.lavaLake = this.biome == BiomeGenBase.hell || this.biome != BiomeGenBase.swampland
			&& this.biome != BiomeGenBase.taiga && this.biome != BiomeGenBase.icePlains
			&& this.biome != BiomeGenBase.sky && rnd.nextInt(10) == 0;
		this.hasLake = this.biome == BiomeGenBase.swampland || this.biome != BiomeGenBase.sky && rnd.nextInt(2) == 0;

		// Get the location of the ore orb
		orbLocation = Utils.GetCoords(sphereLocation);
		int lowY = this.sphereLocation.posY - scaledSphereRadius;
		int highY = this.sphereLocation.posY + scaledSphereRadius;

		int orbRange = ((this.scaledGridSize * 8)) - this.scaledOrbRadius;

		orbLocation.posX = this.sphereLocation.posX;
		orbLocation.posZ = this.sphereLocation.posZ;
		orbLocation.posY = this.seaLevel;

		// TODO: let's make this algorithm more efficient
		int giveUpAfter = 100;
		while (!ValidOrbLocation() && giveUpAfter > 0)
		{
			giveUpAfter--;
			orbLocation.posX = (this.sphereLocation.posX - orbRange)
				+ (int)Math.round(rnd.nextDouble() * (orbRange * 2));

			orbLocation.posZ = (this.sphereLocation.posZ - orbRange)
				+ (int)Math.round(rnd.nextDouble() * (orbRange * 2));

			orbLocation.posY = Utils.RndBetween(rnd, lowY, highY);
		}

		if (!ValidOrbLocation())
		{
			// put the orb out of range
			orbLocation.posY = this.seaLevel;
			orbLocation.posX = sphereLocation.posX + (this.scaledGridSize * 32);
			orbLocation.posZ = sphereLocation.posZ + (this.scaledGridSize * 32);
		}

		lakeLocation = Utils.GetCoords(sphereLocation);

		if (this.hasLake && this.isNoiseEnabled)
		{
			SetLakeHeight();

			if (rnd.nextDouble() > .65d)
			{
				lakeLocation.posY -= (int)(Math.round((rnd.nextDouble() * 2d) * scale));
			}
		}

		SetupOrbStairway(rnd);

		xm = rnd.nextLong() / 2L * 2L + 1L;
		zm = rnd.nextLong() / 2L * 2L + 1L;
		this.seed = (centerChunkX * xm + centerChunkZ * zm) * 3168045L ^ this.chunkProvider.worldSeed;

		rnd = GetPhaseRandom("SphereType", centerChunkX, centerChunkZ);
		int sType = -1;
		if (AnySphereTypeValid())
		{
			while (!SphereTypeValid(sType))
			{
				sType = rnd.nextInt() % ModConfig.DOMETYPE_COUNT;
			}
		}
		this.sphereType = sType;
	}

	// #region Public Methods

	public Block getDomeBlock(int x, int y, int z)
	{
		Block ret = Blx.glass;

		if (SphereTypeValid(this.sphereType))
		{
			Random rnd = GetPhaseRandom
			(
				"SphereType_" + Integer.toString(x) + "_" + Integer.toString(y) + "_" + Integer.toString(z),
				centerChunkX,
				centerChunkZ
			);

			ret = ((BlockEntry)WeightedRandom.getRandomItem(rnd, this.chunkProvider.config.DomeBlocks[this.sphereType])).Block;
		}

		return ret;
	}

	public int getMainDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.sphereLocation, rawX, rawY, rawZ);
	}

	public int getOrbDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.orbLocation, rawX, rawY, rawZ);
	}

	public int getLakeDistance(int rawX, int rawY, int rawZ)
	{
		if (!hasLake) { return Integer.MAX_VALUE; }

		int lly = lakeLocation.posY;

		// a positive "dy" value indicates you are above the lake, while negative indicates below the lake.
		double dy = rawY - lly;

		// lets increase the distance slightly to give an ellipsoid shape.
		if (dy < 0)
		{
			dy = dy * 2;

			if (this.isNoiseEnabled)
			{
				int beforeNoise = (int)Math.round(Utils.GetDistance(lakeLocation.posX, 0, lakeLocation.posZ, rawX, dy,
					rawZ));

				if (rawX >= (lakeLocation.posX - scaledLakeEdgeRadius)
					&& rawZ >= (lakeLocation.posZ - scaledLakeEdgeRadius)
					&& rawX <= (lakeLocation.posX + scaledLakeEdgeRadius)
					&& rawZ <= (lakeLocation.posZ + scaledLakeEdgeRadius))
				{
					int x = sphereLocation.posX + ((rawX - sphereLocation.posX) * 20);
					int z = sphereLocation.posZ + ((rawZ - sphereLocation.posZ) * 20);

					double offset = getRawSurfaceLevel(x, z) - this.seaLevel;
					dy += Math.abs((offset / 4d));
				}

				int afterNoise = (int)Math.round(Utils.GetDistance(lakeLocation.posX, 0, lakeLocation.posZ, rawX, dy,
					rawZ));
				if (afterNoise < beforeNoise)
				{
					return afterNoise;
				}
				else
				{
					return beforeNoise;
				}
			}
		}

		return (int)Math.round(Utils.GetDistance(lakeLocation.posX, 0, lakeLocation.posZ, rawX, dy, rawZ));
	}

	public int getRawSurfaceLevel(int rawX, int rawZ)
	{
		if (centerNoiseChunk != null) { return centerNoiseChunk.getRawSurfaceLevel(rawX, rawZ); }
		return this.seaLevel;
	}

	public Block GetLakeBlock()
	{
		if (!this.hasLake) { return Blx.air; }
		return (this.lavaLake ? Blx.flowing_lava : Blx.flowing_water);
	}

	public List<TopDownBoundingBox> getBoundingBoxes()
	{
		if (boundingBoxes == null)
		{
			boundingBoxes = new ArrayList<TopDownBoundingBox>();

			// Sphere
			boundingBoxes.add(TopDownBoundingBox.FromCircle(sphereLocation.posX, sphereLocation.posZ,
				this.scaledSphereRadius + 1));

			// Ore Orb
			boundingBoxes.add(TopDownBoundingBox.FromCircle(orbLocation.posX, orbLocation.posZ, (scaledOrbRadius + 1)));

			// Z-aligned bridge
			boundingBoxes.add(new TopDownBoundingBox(sphereLocation.posX - (bridgeWidth + 1), Integer.MIN_VALUE,
				sphereLocation.posX + (bridgeWidth + 1), Integer.MAX_VALUE));

			// X-aligned bridge
			boundingBoxes.add(new TopDownBoundingBox(Integer.MIN_VALUE, sphereLocation.posZ - (bridgeWidth + 1),
				Integer.MAX_VALUE, sphereLocation.posZ + (bridgeWidth + 1)));

			// Stairway
			if (orbStairwayBox != null)
			{
				boundingBoxes.add(orbStairwayBox);
			}
		}

		return boundingBoxes;
	}

	public Block getOrbStairwayBlock(int x, int y, int z)
	{
		if (orbStairwayBox == null) { return null; }
		if (!orbStairwayBox.CollidesWith(x, z)) { return null; }

		ChunkCoordinates key = Utils.GetCoords(x, y, z);

		if (orbStairwayBlx.containsKey(key)) { return orbStairwayBlx.get(key); }

		return null;
	}

	public Random GetPhaseRandom(String phase, int chunkX, int chunkZ)
	{
		Random rnd = new Random(this.seed);

		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;

		long _seed = (chunkX * xm + chunkZ * zm) * phase.hashCode() ^ this.chunkProvider.worldSeed;

		rnd.setSeed(_seed);
		return rnd;
	}

	// #endregion

	// #region Private Methods

	private boolean AnySphereTypeValid()
	{
		for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++)
		{
			if (SphereTypeValid(i)) { return true; }
		}

		return false;
	}

	private boolean SphereTypeValid(int typeIndex)
	{
		if (typeIndex < 0) { return false; }
		if (typeIndex >= ModConfig.DOMETYPE_COUNT) { return false; }

		ModConfig config = chunkProvider.config;

		if (config.DomeBlocks[typeIndex] != null)
		{
			for (int i = 0; i < config.DomeBlocks[typeIndex].size(); i++)
			{
				if (config.DomeBlocks[typeIndex].get(i).itemWeight > 0)
				{
					return true;
				}
			}
		}

		return false;
	}

	private static ChunkCoordinates GetSphereCenter(int chunkX, int chunkZ, ModConfig cfg)
	{
		if (cfg == null) { return null; }

		int chunkOffsetToCenterX = -(int)Math.floor(Math.IEEEremainder(chunkX, cfg.getScaledGridSize()));
		int chunkOffsetToCenterZ = -(int)Math.floor(Math.IEEEremainder(chunkZ, cfg.getScaledGridSize()));

		chunkX += chunkOffsetToCenterX;
		chunkZ += chunkOffsetToCenterZ;

		int x = ((chunkX) << 4) + 8;
		int z = ((chunkZ) << 4) + 8;
		int y = cfg.getSeaLevel();

		return Utils.GetCoords(x, y, z);
	}

	private boolean ValidOrbLocation()
	{
		if (orbLocation.posY < (ModConsts.WORLD_MIN_Y + this.scaledOrbRadius)) { return false; }
		if (orbLocation.posY > (ModConsts.WORLD_MAX_Y - this.scaledOrbRadius)) { return false; }

		int groundLevel = getRawSurfaceLevel(orbLocation.posX, orbLocation.posZ);
		if (Math.abs(groundLevel - orbLocation.posY) <= (this.scaledOrbRadius + 2)) { return false; }

		TopDownBoundingBox sphereBox = TopDownBoundingBox.FromCircle(sphereLocation.posX, sphereLocation.posZ,
			this.scaledSphereRadius + 1);

		TopDownBoundingBox orbBox = TopDownBoundingBox.FromCircle(orbLocation.posX, orbLocation.posZ,
			(scaledOrbRadius + 1));

		return !orbBox.CollidesWith(sphereBox);

		// return Utils.GetDistance(orbLocation, sphereLocation) > (this.scaledOrbRadius + this.scaledSphereRadius);
	}

	private void SetLakeHeight()
	{
		if (centerNoiseChunk == null)
		{
			lakeLocation.posY = this.seaLevel;
		}
		else
		{
			int x = lakeLocation.posX - scaledLakeRadius;
			int z = lakeLocation.posZ - scaledLakeRadius;
			boolean first = true;
			double min = 0;

			while (x <= (lakeLocation.posX + scaledLakeRadius))
			{
				while (z <= (lakeLocation.posZ + scaledLakeRadius))
				{
					double val = centerNoiseChunk.GetChunkAt(x, z).minNoise;
					if (first || val < min)
					{
						min = val;
						first = false;
					}

					z += 16;
				}
				x += 16;
			}

			lakeLocation.posY = (int)Math.round(this.seaLevel + min * 8.0D * scale);
		}
	}

	private void SetupOrbStairway(final Random rnd)
	{
		if (chunkProvider.config.doesNeedProtectionGlass()) { return; }

		final List<BlockEntry> stairwayBlocks = chunkProvider.config.StairwayBlocks;

		boolean foundBlock = false;
		for (BlockEntry be: stairwayBlocks)
		{
			if (be.itemWeight > 0)
			{
				foundBlock = true;
				break;
			}
		}
		if (!foundBlock) { return; }

		int orbDistX, orbDistZ;

		orbDistX = orbLocation.posX - sphereLocation.posX;
		orbDistZ = orbLocation.posZ - sphereLocation.posZ;

		if (Math.abs(orbDistX) < Math.abs(orbDistZ))
		{
			orbDistZ = 0;
		}
		else if (Math.abs(orbDistX) > Math.abs(orbDistZ))
		{
			orbDistX = 0;
		}
		else
		{
			if (rnd.nextBoolean())
			{
				orbDistX = 0;
			}
			else
			{
				orbDistZ = 0;
			}
		}

		// bridge intersection point
		int ix = orbLocation.posX - orbDistX;
		int iz = orbLocation.posZ - orbDistZ;
		int iy = getRawSurfaceLevel(ix, iz);

		final int tox = (orbDistX == 0 ? 0 : ((orbDistX > 0) ? 1 : -1));
		final int toz = (orbDistZ == 0 ? 0 : ((orbDistZ > 0) ? 1 : -1));

		orbStairwayBlx = new HashMap<ChunkCoordinates, Block>();

		if (orbDistZ == 0)
		{
			Utils.DoLine(orbLocation.posX, orbLocation.posY, ix, iy, new Func2<Integer, Integer, Boolean>()
			{
				@Override
				public Boolean func(Integer x, Integer y)
				{
					for (int z = orbLocation.posZ - bridgeWidth; z <= orbLocation.posZ + bridgeWidth; z++)
					{
						Block block = ((BlockEntry)WeightedRandom.getRandomItem(rnd, stairwayBlocks)).Block;
						if (block != Blx.air)
						{
							orbStairwayBlx.put(Utils.GetCoords(x, y, z), block);
						}

						block = ((BlockEntry)WeightedRandom.getRandomItem(rnd, stairwayBlocks)).Block;
						if (block != Blx.air)
						{
							orbStairwayBlx.put(Utils.GetCoords(x + tox, y, z + toz), block);
						}
					}

					return true;
				}
			});
		}
		else
		{
			Utils.DoLine(orbLocation.posZ, orbLocation.posY, iz, iy, new Func2<Integer, Integer, Boolean>()
			{
				@Override
				public Boolean func(Integer z, Integer y)
				{
					for (int x = orbLocation.posX - bridgeWidth; x <= orbLocation.posX + bridgeWidth; x++)
					{
						Block block = ((BlockEntry)WeightedRandom.getRandomItem(rnd, stairwayBlocks)).Block;
						if (block != Blx.air)
						{
							orbStairwayBlx.put(Utils.GetCoords(x, y, z), block);
						}

						block = ((BlockEntry)WeightedRandom.getRandomItem(rnd, stairwayBlocks)).Block;
						if (block != Blx.air)
						{
							orbStairwayBlx.put(Utils.GetCoords(x + tox, y, z + toz), block);
						}
					}

					return true;
				}
			});
		}

		orbStairwayBox = TopDownBoundingBox.FromArray(orbStairwayBlx.keySet());
	}
	// #endregion

}
