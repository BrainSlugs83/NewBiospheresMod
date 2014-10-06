package woop;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.biome.BiomeGenBase;

public class SphereChunk
{
	public final int chunkX, chunkZ;

	public final BiosphereChunkProvider chunkProvider;
	public final ChunkCoordinates sphereLocation;
	public final ChunkCoordinates orbLocation;
	public final ChunkCoordinates lakeLocation;

	private final long seed;

	public final double scaledSphereRadius;

	public final double lakeRadius;
	public final double lakeEdgeRadius;

	public final boolean lavaLake;
	public final boolean hasLake;

	public final BiomeGenBase biome;
	public final NoiseChunk noise;

	public final int scaledOrbRadius;
	public final boolean isNoiseEnabled;
	public final int scaledGridSize;
	public final int bridgeWidth;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		this.chunkProvider = chunkProvider;
		ModConfig cfg = this.chunkProvider.config;

		this.scaledOrbRadius = cfg.getScaledOrbRadius();
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
		this.scaledSphereRadius = Math.round(minRad + (rnd.nextDouble() * radRange));

		// Get lake radius
		double lakeRatio = cfg.getMinLakeRatio() + ((cfg.getMaxLakeRatio() - cfg.getMinLakeRatio()) * rnd.nextDouble());
		this.lakeRadius = Math.round(this.scaledSphereRadius * lakeRatio);
		this.lakeEdgeRadius = lakeRadius + 2.0d;

		this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(sphereLocation.posX,
			sphereLocation.posZ);

		this.lavaLake = this.biome == BiomeGenBase.hell || this.biome != BiomeGenBase.swampland
				&& this.biome != BiomeGenBase.taiga && this.biome != BiomeGenBase.icePlains
				&& this.biome != BiomeGenBase.sky && rnd.nextInt(10) == 0;
		this.hasLake = this.biome == BiomeGenBase.swampland || this.biome != BiomeGenBase.sky && rnd.nextInt(2) == 0;

		orbLocation = Utils.GetCoords(sphereLocation);
		int lowY = this.sphereLocation.posY - (int)scaledSphereRadius;
		int highY = this.sphereLocation.posY + (int)scaledSphereRadius;

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
			double lakeMin = noise.GetChunkAt(sphereLocation.posX, sphereLocation.posZ).minNoise;

			lakeLocation.posY = (int)Math.round(ModConsts.SEA_LEVEL + lakeMin * 8.0D * cfg.getScale());
			lakeLocation.posY -= (1 + (int)(Math.round((rnd.nextDouble() * 3d) * cfg.getScale())));
		}

		// Reseed random generator (specific to the chunk).
		xm = rnd.nextLong() / 2L * 2L + 1L;
		zm = rnd.nextLong() / 2L * 2L + 1L;
		this.seed = (chunkX * xm + chunkZ * zm) * 3168045L ^ this.chunkProvider.worldSeed;
		rnd.setSeed(this.seed);

	}

	private boolean ValidOrbLocation()
	{
		if (orbLocation.posY < (ModConsts.WORLD_MIN_Y + this.scaledOrbRadius)) { return false; }
		if (orbLocation.posY > (ModConsts.WORLD_MAX_Y - this.scaledOrbRadius)) { return false; }

		int groundLevel = getRawSurfaceLevel(orbLocation.posX, orbLocation.posZ);
		if (Math.abs(groundLevel - orbLocation.posY) <= (this.scaledOrbRadius + 2)) { return false; }

		return Utils.GetDistance(orbLocation, sphereLocation) > (this.scaledOrbRadius + this.scaledSphereRadius);
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

	public int getLakeDistance(int rawX, int rawY, int rawZ)
	{
		if (!hasLake) { return Integer.MAX_VALUE; }

		int lly = lakeLocation.posY;

		// a positive "dy" value indicates you are above the lake, while negative indicates below the lake.
		double dy = rawY - lly;

		// for below the lake, lets multiply the distance by 2, giving us a shallow ellipsoid shape, for above the lake
		// let's multiply by 2/3 -- this will give us a more natural lake.

		dy = (dy > 0) ? (dy * 2d / 3d) : (dy * 2d);

		if (this.isNoiseEnabled && dy < 0)
		{
			// make the bottom uneven
			double offset = ((getRawSurfaceLevel(rawX, rawZ) - ModConsts.SEA_LEVEL));
			dy -= (offset / 2D);

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
			final int _sphereRadius = (int)Math.ceil(this.scaledSphereRadius + 1);

			boundingBoxes = new ArrayList<TopDownBoundingBox>();

			// Sphere
			boundingBoxes.add(new TopDownBoundingBox(sphereLocation.posX - _sphereRadius, sphereLocation.posZ
					- _sphereRadius, sphereLocation.posX + _sphereRadius, sphereLocation.posZ + _sphereRadius));

			// Ore Orb
			boundingBoxes.add(new TopDownBoundingBox(orbLocation.posX - (scaledOrbRadius + 1), orbLocation.posZ
					- (scaledOrbRadius + 1), orbLocation.posX + (scaledOrbRadius + 1), orbLocation.posZ
					+ (scaledOrbRadius + 1)));

			// Z-aligned bridge
			boundingBoxes.add(new TopDownBoundingBox(sphereLocation.posX - (bridgeWidth + 1), Integer.MIN_VALUE,
				sphereLocation.posX + (bridgeWidth + 1), Integer.MAX_VALUE));

			// X-aligned bridge
			boundingBoxes.add(new TopDownBoundingBox(Integer.MIN_VALUE, sphereLocation.posZ - (bridgeWidth + 1),
				Integer.MAX_VALUE, sphereLocation.posZ + (bridgeWidth + 1)));
		}

		return boundingBoxes;

	}
}
