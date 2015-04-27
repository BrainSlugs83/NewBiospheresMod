/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.models;

import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import newbiospheresmod.helpers.Creator;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.Utils;

public class NoiseChunk {
  // #region Caching

  private static class CacheKey {
    public final World world;
    public final int x;
    public final int z;

    public CacheKey(final World world, final int x, final int z) {
      this.world = world;
      this.x = x;
      this.z = z;
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == null) {
        return false;
      }
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof CacheKey)) {
        return false;
      }
      final CacheKey other = (CacheKey) obj;

      return (this.x == other.x) && (this.z == other.z) && (this.world == other.world);
    }

    @Override
    public int hashCode() {
      final int worldHash = (this.world == null) ? 0 : this.world.hashCode();
      return ((this.x & 0xFFFF) | ((this.z & 0xFFFF) << 16)) ^ worldHash ^ 949032852;
    }
  }

  private static LruCacheList<NoiseChunk> noiseChunks = new LruCacheList<NoiseChunk>(25,
      new IKeyProvider<NoiseChunk>() {
        @Override
        public Object provideKey(final NoiseChunk item) {
          if (item == null) {
            return null;
          }

          return new CacheKey(item.world, item.chunkX, item.chunkZ);
        }
      });

  private static final double noiseScale = 0.0078125D;

  // #endregion

  public static NoiseChunk get(final World world, final int chunkX, final int chunkZ,
      final NoiseGeneratorOctaves noiseGen, final double scale, final int seaLevel) {
    final Object key = new CacheKey(world, chunkX, chunkZ);

    return NoiseChunk.noiseChunks.findOrAdd(key, new Creator<NoiseChunk>() {
      @Override
      public NoiseChunk create() {
        return new NoiseChunk(world, chunkX, chunkZ, noiseGen, scale, seaLevel);
      }
    });
  }

  public final int chunkX;
  public final int chunkZ;

  public final double maxNoise;
  public final double minNoise;
  private final double[] noise;
  public final NoiseGeneratorOctaves noiseGenerator;
  private final double scale;

  private final int seaLevel;

  public final World world;

  private NoiseChunk(final World world, final int chunkX, final int chunkZ, final NoiseGeneratorOctaves noiseGen,
      final double scale, final int seaLevel) {
    this.chunkX = chunkX;
    this.chunkZ = chunkZ;
    this.world = world;
    this.scale = scale;
    this.noiseGenerator = noiseGen;

    this.noise = noiseGen.generateNoiseOctaves(null, chunkX << 4, ModConsts.WORLD_HEIGHT, chunkZ << 4, 16, 1, 16,
        NoiseChunk.noiseScale, 1.0D, NoiseChunk.noiseScale);

    this.minNoise = Utils.min(this.noise);
    this.maxNoise = Utils.max(this.noise);

    this.seaLevel = seaLevel;
  }

  public NoiseChunk getChunkAt(final int rawX, final int rawZ) {
    final int chunkX = (int) Math.floor(rawX / 16d);
    final int chunkZ = (int) Math.floor(rawZ / 16d);

    if ((this.chunkX == chunkX) && (this.chunkZ == chunkZ)) {
      return this;
    }

    return NoiseChunk.get(this.world, chunkX, chunkZ, this.noiseGenerator, this.scale, this.seaLevel);
  }

  public int getChunkBoundSurfaceLevel(final int boundX, final int boundZ) {
    return this.getChunkBoundSurfaceLevel(boundX, boundZ, this.seaLevel);
  }

  public int getChunkBoundSurfaceLevel(final int boundX, final int boundZ, final int baseLevel) {
    if ((boundX < 0) || (boundX >= 16)) {
      throw new IndexOutOfBoundsException("boundX");
    }
    if ((boundZ < 0) || (boundZ >= 16)) {
      throw new IndexOutOfBoundsException("boundZ");
    }

    double ret = baseLevel;
    ret += this.noise[boundZ + (boundX << 4)] * 8.0D * this.scale;
    return (int) Math.round(ret);
  }

  public int getRawSurfaceLevel(final int rawX, final int rawZ) {
    return this.getRawSurfaceLevel(rawX, rawZ, this.seaLevel);
  }

  public int getRawSurfaceLevel(int rawX, int rawZ, final int baseLevel) {
    final int chunkX = (int) Math.floor(rawX / 16d);
    final int chunkZ = (int) Math.floor(rawZ / 16d);
    rawX -= (chunkX << 4);
    rawZ -= (chunkZ << 4);

    if ((this.chunkX == chunkX) && (this.chunkZ == chunkZ)) {
      return this.getChunkBoundSurfaceLevel(rawX, rawZ, baseLevel);
    }

    return NoiseChunk.get(this.world, chunkX, chunkZ, this.noiseGenerator, this.scale, this.seaLevel)
        .getChunkBoundSurfaceLevel(rawX, rawZ);
  }
}
