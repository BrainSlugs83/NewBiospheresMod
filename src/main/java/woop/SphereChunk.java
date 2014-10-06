package woop;

import java.util.Random;

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

	public final double radius;

	public final double lakeRadius;
	public final double lakeEdgeRadius;

	public final boolean lavaLake;
	public final boolean hasLake;

	public final BiomeGenBase biome;
	public final NoiseChunk noise;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		this.chunkProvider = chunkProvider;
		ModConfig cfg = this.chunkProvider.config;

		if (cfg.isNoiseEnabled())
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
		this.radius = Math.round(minRad + (rnd.nextDouble() * radRange));

		// Get lake radius
		double lakeRatio = cfg.getMinLakeRatio() + ((cfg.getMaxLakeRatio() - cfg.getMinLakeRatio()) * rnd.nextDouble());
		this.lakeRadius = Math.round(this.radius * lakeRatio);
		this.lakeEdgeRadius = lakeRadius + 2.0d;

		this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(sphereLocation.posX,
			sphereLocation.posZ);

		this.lavaLake = this.biome == BiomeGenBase.hell || this.biome != BiomeGenBase.swampland
				&& this.biome != BiomeGenBase.taiga && this.biome != BiomeGenBase.icePlains
				&& this.biome != BiomeGenBase.sky && rnd.nextInt(10) == 0;
		this.hasLake = this.biome == BiomeGenBase.swampland || this.biome != BiomeGenBase.sky && rnd.nextInt(2) == 0;

		orbLocation = Utils.GetCoords(sphereLocation);

		// int lowY = this.sphereLocation.posY - (int)radius;
		// int highY = this.sphereLocation.posY + (int)radius;

		int orbRange = ((cfg.getScaledGridSize() * 8)) - cfg.getScaledOrbRadius();

		orbLocation.posX = this.sphereLocation.posX + orbRange;
		orbLocation.posZ = this.sphereLocation.posZ + orbRange;
		orbLocation.posY = ModConsts.SEA_LEVEL;

		// int orbRange = ((cfg.getScaledGridSize() * 16) / 2) - cfg.getScaledOrbRadius();
		// int giveUpAfter = 100;
		// while (!ValidOrbLocation() && giveUpAfter > 0)
		// {
		// giveUpAfter--;
		// orbLocation.posX = this.sphereLocation.posX + (int)Math.round(rnd.nextDouble() * orbRange);
		// orbLocation.posZ = this.sphereLocation.posZ + (int)Math.round(rnd.nextDouble() * orbRange);
		// orbLocation.posY = Utils.RndBetween(rnd, lowY, highY);
		// }
		//
		// if (!ValidOrbLocation())
		// {
		// orbLocation.posY = ModConsts.SEA_LEVEL;
		// orbLocation.posX = sphereLocation.posX + (cfg.getScaledGridSize() * 32);
		// orbLocation.posZ = sphereLocation.posZ + (cfg.getScaledGridSize() * 32);
		// }

		lakeLocation = Utils.GetCoords(sphereLocation);

		if (this.hasLake && cfg.isNoiseEnabled())
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
		final ModConfig cfg = this.chunkProvider.config;
		final int orbRadius = cfg.getScaledOrbRadius();

		if (orbLocation.posY < (ModConsts.WORLD_MIN_Y + orbRadius)) { return false; }
		if (orbLocation.posY > (ModConsts.WORLD_MAX_Y - orbRadius)) { return false; }

		int groundLevel = getRawSurfaceLevel(orbLocation.posX, orbLocation.posZ);
		if (Math.abs(groundLevel - orbLocation.posY) <= (orbRadius + 2)) { return false; }

		return Utils.GetDistance(orbLocation, sphereLocation) > (orbRadius + this.radius);
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

		int chunkOffsetToCenterX = -(int)Math.floor(Math.IEEEremainder(chunkX, cfg.getScaledGridSize()));
		int chunkOffsetToCenterZ = -(int)Math.floor(Math.IEEEremainder(chunkZ, cfg.getScaledGridSize()));

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

		// if (this.chunkProvider != null)
		// {
		// if (this.chunkProvider.config != null)
		// {
		// ModConfig cfg = this.chunkProvider.config;
		//
		// if (cfg.isNoiseEnabled())
		// {
		// // make the bottom uneven
		// int offset = getSurfaceLevel(rawX, rawZ, 16, noise, cfg.getScale()) - ModConsts.SEA_LEVEL;
		// lly -= (offset * 2);
		// }
		// }
		// }

		int dy = rawY - lly;
		dy = (dy > 0) ? dy : (dy * -2);

		return Utils.GetDistance(lakeLocation.posX, 0, lakeLocation.posZ, rawX, dy, rawZ);
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

	// public int getSurfaceLevel(int x, int z)
	// {
	// if (this.noise != null)
	// {
	// if (this.chunkProvider != null)
	// {
	// int chunkX = this.chunkX;
	// if (x < 0 || x >= 16)
	// {
	// chunkX = (int)Math.floor(x / 16d);
	// x -= (chunkX * 16);
	// }
	//
	// int chunkZ = this.chunkZ;
	// if (z < 0 || z >= 16)
	// {
	// z -= (((int)Math.floor(z / 16d)) * 16);
	// }
	//
	// ModConfig cfg = this.chunkProvider.config;
	// if (cfg != null) { return getSurfaceLevel(x, z, this.noise, cfg.getScale()); }
	// }
	// }
	//
	// return ModConsts.SEA_LEVEL;
	// }
}
