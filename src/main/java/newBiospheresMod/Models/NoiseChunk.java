package newBiospheresMod.Models;

import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;
import akka.japi.Creator;

public class NoiseChunk
{
	private static LruCacheList<NoiseChunk> noiseChunks = new LruCacheList<NoiseChunk>(25);

	public static NoiseChunk get(final World world, final int chunkX, final int chunkZ,
			final NoiseGeneratorOctaves noiseGen, final double scale)
	{
		int key = getKey(world, chunkX, chunkZ);

		return noiseChunks.FindOrAdd(key, new Creator<NoiseChunk>()
		{
			@Override
			public NoiseChunk create()
			{
				return new NoiseChunk(world, chunkX, chunkZ, noiseGen, scale);
			}
		});
	}

	public static int getKey(final World world, final int chunkX, final int chunkZ)
	{
		int worldHash = 0;
		if (world != null)
		{
			worldHash = world.hashCode();
		}

		return ((chunkX & 0xFFFF) | ((chunkZ & 0xFFFF) << 16)) ^ worldHash ^ 949032852;
	}

	public final int chunkX;
	public final int chunkZ;
	public final World world;

	private final double[] noise;
	public final double minNoise, maxNoise;
	private final double scale;
	private static final double noiseScale = 0.0078125D;

	public final NoiseGeneratorOctaves noiseGenerator;

	private NoiseChunk(World world, int chunkX, int chunkZ, NoiseGeneratorOctaves noiseGen, double scale)
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
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ)
	{
		return getChunkBoundSurfaceLevel(boundX, boundZ, ModConsts.SEA_LEVEL);
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
		return getRawSurfaceLevel(rawX, rawZ, ModConsts.SEA_LEVEL);
	}

	public int getRawSurfaceLevel(int rawX, int rawZ, int baseLevel)
	{
		int chunkX = (int)Math.floor(rawX / 16d);
		int chunkZ = (int)Math.floor(rawZ / 16d);
		rawX -= (chunkX << 4);
		rawZ -= (chunkZ << 4);

		if (this.chunkX == chunkX && this.chunkZ == chunkZ) { return getChunkBoundSurfaceLevel(rawX, rawZ, baseLevel); }

		return get(world, chunkX, chunkZ, noiseGenerator, scale).getChunkBoundSurfaceLevel(rawX, rawZ);
	}

	public NoiseChunk GetChunkAt(int rawX, int rawZ)
	{
		int chunkX = (int)Math.floor(rawX / 16d);
		int chunkZ = (int)Math.floor(rawZ / 16d);

		if (this.chunkX == chunkX && this.chunkZ == chunkZ) { return this; }

		return get(world, chunkX, chunkZ, noiseGenerator, scale);
	}

	@Override
	public int hashCode()
	{
		return getKey(this.world, this.chunkX, this.chunkZ);
	}
}
