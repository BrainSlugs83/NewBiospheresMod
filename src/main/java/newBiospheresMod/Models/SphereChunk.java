package newBiospheresMod.Models;

import java.util.Random;

import newBiospheresMod.BiosphereChunkProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import akka.japi.Creator;

public class SphereChunk
{
	private static LruCacheList<SphereChunk> sphereChunkCache = new LruCacheList<SphereChunk>(15);

	public static SphereChunk get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ)
	{
		return sphereChunkCache.FindOrAdd(getKey(chunkProvider, chunkX, chunkZ), new Creator<SphereChunk>()
		{
			@Override
			public SphereChunk create()
			{
				return new SphereChunk(chunkProvider, chunkX, chunkZ);
			}
		});
	}

	public static int getKey(final BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		return BiosphereChunkProvider.getChunkKey(chunkProvider, chunkX, chunkZ) ^ 1890321837;
	}

	public final int chunkX, chunkZ;

	public final BiosphereChunkProvider chunkProvider;
	public final Sphere masterSphere;

	public final boolean isNoiseEnabled;
	public final NoiseChunk noise;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkProvider = chunkProvider;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		ModConfig cfg = this.chunkProvider.config;
		this.isNoiseEnabled = cfg.isNoiseEnabled();

		noise = isNoiseEnabled ? NoiseChunk.get(this.chunkProvider.world, chunkX, chunkZ,
			this.chunkProvider.noiseGenerator, cfg.getScale()) : null;

		masterSphere = Sphere.get(chunkProvider, chunkX, chunkZ);
	}

	public Random GetPhaseRandom(String phase)
	{
		return masterSphere.GetPhaseRandom(phase, chunkX, chunkZ);
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ)
	{
		if (this.noise != null) { return noise.getChunkBoundSurfaceLevel(boundX, boundZ); }
		return ModConsts.SEA_LEVEL;
	}

	@Override
	public int hashCode()
	{
		return getKey(chunkProvider, chunkX, chunkZ);
	}
}
