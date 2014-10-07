package woop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.biome.BiomeGenBase;
import woop.ModConfig.WorldCharacteristics;
import akka.japi.Function2;

public class SphereChunk
{
	// TODO: SPLIT THIS INTO A SECOND GRID CHUNK CLASS, SO THAT WE DON'T HAVE TO REDO ALL THE WORK / STORE EVERYTHING
	// MULTIPLE TIMES FOR EACH CHUNK IN THE GRID

	public final int chunkX, chunkZ;

	public final BiosphereChunkProvider chunkProvider;
	public final ChunkCoordinates sphereLocation;
	public final ChunkCoordinates orbLocation;
	public final ChunkCoordinates lakeLocation;

	private final long seed;

	public final int scaledSphereRadius;
	public final int scaledLakeRadius;
	public final int scaledLakeEdgeRadius;

	public final boolean lavaLake;
	public final boolean hasLake;

	public final BiomeGenBase biome;
	public final NoiseChunk noise;

	public final int scaledOrbRadius;
	public final boolean isNoiseEnabled;
	public final int scaledGridSize;
	public final int bridgeWidth;

	private TopDownBoundingBox orbStairwayBox;

	private Map<ChunkCoordinates, Block> orbStairwayBlocks;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		this.chunkProvider = chunkProvider;
		ModConfig cfg = this.chunkProvider.config;

		this.scaledOrbRadius = cfg.getScaledOrbRadius();
		// this.scaledOrbRadiusSquared = scaledOrbRadius * scaledOrbRadius;
		// this.scaledOrbRadiusSquaredMinusOne = (this.scaledOrbRadius - 1) * (this.scaledOrbRadius - 1);

		this.isNoiseEnabled = cfg.isNoiseEnabled();
		this.scaledGridSize = cfg.getScaledGridSize();
		this.bridgeWidth = cfg.getBridgeWidth();

		if (this.isNoiseEnabled)
		{
			noise = NoiseChunk.get(this.chunkProvider.world, chunkX, chunkZ, this.chunkProvider.noiseGenerator,
				cfg.getScale());
		}
		else
		{
			noise = null;
		}

		// Set sphere location
		this.sphereLocation = GetSphereCenter(chunkX, chunkZ);

		// Seed local random number generator
		Random rnd = new Random(chunkProvider.worldSeed);
		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;
		long _seed = (sphereLocation.posX * xm + sphereLocation.posZ * zm) * 2512576L ^ chunkProvider.worldSeed;
		rnd.setSeed(_seed);

		double minRad = cfg.getMinSphereRadius() * cfg.getScale();
		double maxRad = cfg.getMaxSphereRadius() * cfg.getScale();

		double radRange = (maxRad - minRad);

		// Get sphere radius
		this.scaledSphereRadius = (int)Math.round(minRad + (rnd.nextDouble() * radRange));
		// this.scaledSphereRadiusSquared = scaledSphereRadius * scaledSphereRadius;
		// this.scaledSphereRadiusSquaredMinusOne = (this.scaledSphereRadius - 1) * (this.scaledSphereRadius - 1);

		// Get lake radius
		double lakeRatio = cfg.getMinLakeRatio() + ((cfg.getMaxLakeRatio() - cfg.getMinLakeRatio()) * rnd.nextDouble());
		this.scaledLakeRadius = (int)Math.round(this.scaledSphereRadius * lakeRatio);
		// this.lakeRadiusSquared = this.lakeRadius * this.lakeRadius;
		this.scaledLakeEdgeRadius = scaledLakeRadius + 2;
		// this.lakeEdgeRadiusSquared = this.lakeEdgeRadius * this.lakeEdgeRadius;

		this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(sphereLocation.posX,
			sphereLocation.posZ);

		this.lavaLake = this.biome == BiomeGenBase.hell || this.biome != BiomeGenBase.swampland
				&& this.biome != BiomeGenBase.taiga && this.biome != BiomeGenBase.icePlains
				&& this.biome != BiomeGenBase.sky && rnd.nextInt(10) == 0;
		this.hasLake = this.biome == BiomeGenBase.swampland || this.biome != BiomeGenBase.sky && rnd.nextInt(2) == 0;

		orbLocation = Utils.GetCoords(sphereLocation);
		int lowY = this.sphereLocation.posY - scaledSphereRadius;
		int highY = this.sphereLocation.posY + scaledSphereRadius;

		int orbRange = ((this.scaledGridSize * 8)) - this.scaledOrbRadius;

		orbLocation.posX = this.sphereLocation.posX;
		orbLocation.posZ = this.sphereLocation.posZ;
		orbLocation.posY = ModConsts.SEA_LEVEL;

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
				lakeLocation.posY -= (int)(Math.round((rnd.nextDouble() * 2d) * cfg.getScale()));
			}
		}

		SetupOrbStairway(rnd);

		// Reseed random generator (specific to the chunk).
		xm = rnd.nextLong() / 2L * 2L + 1L;
		zm = rnd.nextLong() / 2L * 2L + 1L;
		this.seed = (chunkX * xm + chunkZ * zm) * 3168045L ^ this.chunkProvider.worldSeed;
		rnd.setSeed(this.seed);
	}

	private void SetupOrbStairway(final Random rnd)
	{
		if (chunkProvider.config.getCharacteristics() != WorldCharacteristics.NormalWorld) { return; }

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

	public Block getOrbStairwayBlock(int x, int y, int z)
	{
		if (orbStairwayBox == null) { return null; }
		if (!orbStairwayBox.CollidesWith(x, z)) { return null; }

		ChunkCoordinates key = Utils.GetCoords(x, y, z);

		if (orbStairwayBlocks.containsKey(key)) { return orbStairwayBlocks.get(key); }

		return null;
	}

	private void SetLakeHeight()
	{
		double scale = 1d;
		if (this.chunkProvider != null && this.chunkProvider.config != null)
		{
			scale = this.chunkProvider.config.getScale();
		}

		int x = lakeLocation.posX - scaledLakeRadius;
		int z = lakeLocation.posZ - scaledLakeRadius;
		boolean first = true;
		double min = 0;

		while (x <= lakeLocation.posX + scaledLakeRadius)
		{
			while (z <= lakeLocation.posZ + scaledLakeRadius)
			{
				double val = noise.GetChunkAt(x, z).minNoise;
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

	public Random GetPhaseRandom(String phase)
	{
		Random rnd = new Random(this.seed);

		long xm = rnd.nextLong() / 2L * 2L + 1L;
		long zm = rnd.nextLong() / 2L * 2L + 1L;

		long _seed = (chunkX * xm + chunkZ * zm) * phase.hashCode() ^ this.chunkProvider.worldSeed;

		rnd.setSeed(_seed);
		return rnd;
	}

	private ChunkCoordinates GetSphereCenter(int chunkX, int chunkZ)
	{
		ModConfig cfg = this.chunkProvider.config;

		int chunkOffsetToCenterX = -(int)Math.floor(Math.IEEEremainder(chunkX, this.scaledGridSize));
		int chunkOffsetToCenterZ = -(int)Math.floor(Math.IEEEremainder(chunkZ, this.scaledGridSize));

		chunkX += chunkOffsetToCenterX;
		chunkZ += chunkOffsetToCenterZ;

		int x = ((chunkX) << 4) + 8;
		int z = ((chunkZ) << 4) + 8;
		int y = this.getRawSurfaceLevel(x, z);

		return Utils.GetCoords(x, y, z);
	}

	public int getMainDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.sphereLocation, rawX, rawY, rawZ);
	}

	public int getOrbDistance(int rawX, int rawY, int rawZ)
	{
		return Utils.GetDistance(this.orbLocation, rawX, rawY, rawZ);
	}

	private static double lastOffset = Double.MIN_VALUE;

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

	public Block GetLakeBlock()
	{
		if (!this.hasLake) { return Blocks.air; }
		return (this.lavaLake ? Blocks.flowing_lava : Blocks.flowing_water);
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ)
	{
		if (this.noise != null) { return noise.getChunkBoundSurfaceLevel(boundX, boundZ); }
		return ModConsts.SEA_LEVEL;
	}

	public int getRawSurfaceLevel(int rawX, int rawZ)
	{
		if (this.noise != null) { return noise.getRawSurfaceLevel(rawX, rawZ); }
		return ModConsts.SEA_LEVEL;
	}

	private List<TopDownBoundingBox> boundingBoxes = null;

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
}
