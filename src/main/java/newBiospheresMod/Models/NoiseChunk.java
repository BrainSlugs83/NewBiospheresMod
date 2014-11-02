/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Models;

import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import newBiospheresMod.Helpers.Creator;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;

public class NoiseChunk
{
	// #region Caching

	private static class CacheKey
	{
		public final int x;
		public final int z;
		public final World world;

		public CacheKey(final World world, final int x, final int z)
		{
			this.world = world;
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

			return this.x == other.x && this.z == other.z && this.world == other.world;
		}

		@Override
		public int hashCode()
		{
			int worldHash = (world == null) ? 0 : world.hashCode();
			return ((x & 0xFFFF) | ((z & 0xFFFF) << 16)) ^ worldHash ^ 949032852;
		}
	}

	private static LruCacheList<NoiseChunk> noiseChunks = new LruCacheList<NoiseChunk>(25,
		new IKeyProvider<NoiseChunk>()
		{
			@Override
			public Object provideKey(NoiseChunk item)
			{
				if (item == null) { return null; }

				return new CacheKey(item.world, item.chunkX, item.chunkZ);
			}
		});

	public static NoiseChunk get(final World world, final int chunkX, final int chunkZ,
		final NoiseGeneratorOctaves noiseGen, final double scale, final int seaLevel)
	{
		Object key = new CacheKey(world, chunkX, chunkZ);

		return noiseChunks.FindOrAdd(key, new Creator<NoiseChunk>()
		{
			@Override
			public NoiseChunk create()
			{
				return new NoiseChunk(world, chunkX, chunkZ, noiseGen, scale, seaLevel);
			}
		});
	}

	// #endregion

	public final int chunkX;
	public final int chunkZ;
	public final World world;

	private final double[] noise;
	public final double minNoise, maxNoise;
	private final double scale;
	private static final double noiseScale = 0.0078125D;

	private final int seaLevel;

	public final NoiseGeneratorOctaves noiseGenerator;

	private NoiseChunk(World world, int chunkX, int chunkZ, NoiseGeneratorOctaves noiseGen, double scale, int seaLevel)
	{
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;
		this.world = world;
		this.scale = scale;
		this.noiseGenerator = noiseGen;

		noise = noiseGen.generateNoiseOctaves(null, chunkX << 4, ModConsts.WORLD_HEIGHT, chunkZ << 4, 16, 1, 16,
			noiseScale, 1.0D, noiseScale);

		minNoise = Utils.Min(noise);
		maxNoise = Utils.Max(noise);

		this.seaLevel = seaLevel;
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ)
	{
		return getChunkBoundSurfaceLevel(boundX, boundZ, this.seaLevel);
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ, int baseLevel)
	{
		if (boundX < 0 || boundX >= 16) { throw new IndexOutOfBoundsException("boundX"); }
		if (boundZ < 0 || boundZ >= 16) { throw new IndexOutOfBoundsException("boundZ"); }

		double ret = baseLevel;
		ret += noise[boundZ + (boundX << 4)] * 8.0D * scale;
		return (int)Math.round(ret);
	}

	public int getRawSurfaceLevel(int rawX, int rawZ)
	{
		return getRawSurfaceLevel(rawX, rawZ, this.seaLevel);
	}

	public int getRawSurfaceLevel(int rawX, int rawZ, int baseLevel)
	{
		int chunkX = (int)Math.floor(rawX / 16d);
		int chunkZ = (int)Math.floor(rawZ / 16d);
		rawX -= (chunkX << 4);
		rawZ -= (chunkZ << 4);

		if (this.chunkX == chunkX && this.chunkZ == chunkZ) { return getChunkBoundSurfaceLevel(rawX, rawZ, baseLevel); }

		return get(world, chunkX, chunkZ, noiseGenerator, scale, seaLevel).getChunkBoundSurfaceLevel(rawX, rawZ);
	}

	public NoiseChunk GetChunkAt(int rawX, int rawZ)
	{
		int chunkX = (int)Math.floor(rawX / 16d);
		int chunkZ = (int)Math.floor(rawZ / 16d);

		if (this.chunkX == chunkX && this.chunkZ == chunkZ) { return this; }

		return get(world, chunkX, chunkZ, noiseGenerator, scale, seaLevel);
	}
}
