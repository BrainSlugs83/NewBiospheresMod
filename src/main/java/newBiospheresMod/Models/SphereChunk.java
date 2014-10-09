package newBiospheresMod.Models;

import java.util.Random;

import newBiospheresMod.BiosphereChunkProvider;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import akka.japi.Creator;

public class SphereChunk
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
			return ((x & 0xFFFF) | ((z & 0xFFFF) << 16)) ^ chunkProviderHash ^ 1890321837;
		}
	}

	private static LruCacheList<SphereChunk> sphereChunksCache = new LruCacheList<SphereChunk>(15,
		new IKeyProvider<SphereChunk>()
		{
			@Override
			public Object provideKey(SphereChunk item)
			{
				if (item == null) { return null; }
				return new CacheKey(item.chunkProvider, item.chunkX, item.chunkZ);
			}
		});

	public static SphereChunk get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ)
	{
		return sphereChunksCache.FindOrAdd(new CacheKey(chunkProvider, chunkX, chunkZ), new Creator<SphereChunk>()
		{
			@Override
			public SphereChunk create()
			{
				return new SphereChunk(chunkProvider, chunkX, chunkZ);
			}
		});
	}

	// #endregion

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
}
