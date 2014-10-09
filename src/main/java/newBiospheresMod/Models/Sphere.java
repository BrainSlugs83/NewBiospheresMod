package newBiospheresMod.Models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.biome.BiomeGenBase;
import newBiospheresMod.BiosphereChunkProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.TopDownBoundingBox;
import newBiospheresMod.Helpers.Utils;
import akka.japi.Creator;
import akka.japi.Function2;
import akka.japi.Predicate;

public class Sphere
{
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
	private Map<ChunkCoordinates, Block> orbStairwayBlocks;

	private List<TopDownBoundingBox> boundingBoxes = null;

	// #endregion

	private static LruCacheList<Sphere> sphereCache = new LruCacheList<Sphere>(12);

	public static Sphere get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ)
	{
		return sphereCache.FindOrAdd(new Predicate<Sphere>()
		{
			@Override
			public boolean test(Sphere sphere)
			{
				if (sphere != null)
				{
					ChunkCoordinates coords = GetSphereCenter(chunkX, chunkZ, chunkProvider.config);
					return sphere.sphereLocation.posX == coords.posX && sphere.sphereLocation.posZ == coords.posZ
						&& sphere.chunkProvider == chunkProvider;
				}

				return false;
			}
		}, new Creator<Sphere>()
		{
			@Override
			public Sphere create()
			{
				return new Sphere(chunkProvider, chunkX, chunkZ);
			}
		});
	}

	private Sphere(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkProvider = chunkProvider;
		ModConfig _cfg = this.chunkProvider.config;

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
				this.chunkProvider.noiseGenerator, this.scale);
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
		orbLocation.posY = ModConsts.SEA_LEVEL;

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
			orbLocation.posY = ModConsts.SEA_LEVEL;
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
	}

	// #region Public Methods

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

					double offset = getRawSurfaceLevel(x, z) - ModConsts.SEA_LEVEL;
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
		return ModConsts.SEA_LEVEL;
	}

	public Block GetLakeBlock()
	{
		if (!this.hasLake) { return Blocks.air; }
		return (this.lavaLake ? Blocks.flowing_lava : Blocks.flowing_water);
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

		if (orbStairwayBlocks.containsKey(key)) { return orbStairwayBlocks.get(key); }

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

	private static ChunkCoordinates GetSphereCenter(int chunkX, int chunkZ, ModConfig cfg)
	{
		int chunkOffsetToCenterX = -(int)Math.floor(Math.IEEEremainder(chunkX, cfg.getScaledGridSize()));
		int chunkOffsetToCenterZ = -(int)Math.floor(Math.IEEEremainder(chunkZ, cfg.getScaledGridSize()));

		chunkX += chunkOffsetToCenterX;
		chunkZ += chunkOffsetToCenterZ;

		int x = ((chunkX) << 4) + 8;
		int z = ((chunkZ) << 4) + 8;
		int y = ModConsts.SEA_LEVEL;

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
			lakeLocation.posY = ModConsts.SEA_LEVEL;
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

			lakeLocation.posY = (int)Math.round(ModConsts.SEA_LEVEL + min * 8.0D * scale);
		}
	}

	private void SetupOrbStairway(final Random rnd)
	{
		if (chunkProvider.config.doesNeedProtectionGlass()) { return; }

		final Block bridgeBlock = chunkProvider.config.getBridgeSupportBlock();
		final Block railBlock = chunkProvider.config.getBridgeRailBlock();

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

		orbStairwayBlocks = new HashMap<ChunkCoordinates, Block>();

		if (orbDistZ == 0)
		{
			Utils.DoLine(orbLocation.posX, orbLocation.posY, ix, iy, new Function2<Integer, Integer, Boolean>()
			{
				@Override
				public Boolean apply(Integer x, Integer y)
				{
					for (int z = orbLocation.posZ - bridgeWidth; z <= orbLocation.posZ + bridgeWidth; z++)
					{
						if (rnd.nextBoolean())
						{
							orbStairwayBlocks.put(Utils.GetCoords(x, y, z), bridgeBlock);
						}
						if (rnd.nextBoolean())
						{
							orbStairwayBlocks.put(Utils.GetCoords(x + tox, y, z + toz), bridgeBlock);
						}
					}

					return true;
				}
			});
		}
		else
		{
			Utils.DoLine(orbLocation.posZ, orbLocation.posY, iz, iy, new Function2<Integer, Integer, Boolean>()
			{
				@Override
				public Boolean apply(Integer z, Integer y)
				{
					for (int x = orbLocation.posX - bridgeWidth; x <= orbLocation.posX + bridgeWidth; x++)
					{
						if (rnd.nextBoolean())
						{
							orbStairwayBlocks.put(Utils.GetCoords(x, y, z), bridgeBlock);
						}
						if (rnd.nextBoolean())
						{
							orbStairwayBlocks.put(Utils.GetCoords(x + tox, y, z + toz), bridgeBlock);
						}
					}

					return true;
				}
			});
		}

		orbStairwayBox = TopDownBoundingBox.FromArray(orbStairwayBlocks.keySet());
	}

	// #endregion

}
