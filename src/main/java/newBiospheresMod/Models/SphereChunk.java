/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.models;

import java.util.Random;

import newbiospheresmod.BiosphereChunkProvider;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.Creator;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;

public class SphereChunk {
  // #region Caching

  private static class CacheKey {
    public final BiosphereChunkProvider chunkProvider;
    public final int x;
    public final int z;

    public CacheKey(final BiosphereChunkProvider chunkProvider, final int x, final int z) {
      this.chunkProvider = chunkProvider;
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

      return (this.x == other.x) && (this.z == other.z) && (this.chunkProvider == other.chunkProvider);
    }

    @Override
    public int hashCode() {
      final int chunkProviderHash = (this.chunkProvider == null) ? 0 : this.chunkProvider.hashCode();
      return ((this.x & 0xFFFF) | ((this.z & 0xFFFF) << 16)) ^ chunkProviderHash ^ 1890321837;
    }
  }

  private static LruCacheList<SphereChunk> sphereChunksCache = new LruCacheList<SphereChunk>(15,
      new IKeyProvider<SphereChunk>() {
        @Override
        public Object provideKey(final SphereChunk item) {
          if (item == null) {
            return null;
          }
          return new CacheKey(item.chunkProvider, item.chunkX, item.chunkZ);
        }
      });

  public static SphereChunk get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ) {
    return SphereChunk.sphereChunksCache.findOrAdd(new CacheKey(chunkProvider, chunkX, chunkZ),
        new Creator<SphereChunk>() {
          @Override
          public SphereChunk create() {
            return new SphereChunk(chunkProvider, chunkX, chunkZ);
          }
        });
  }

  // #endregion

  public final BiosphereChunkProvider chunkProvider;

  public final int chunkX;
  public final int chunkZ;
  public final boolean isNoiseEnabled;

  public final Sphere masterSphere;
  public final NoiseChunk noise;

  public SphereChunk(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ) {
    this.chunkProvider = chunkProvider;
    this.chunkX = chunkX;
    this.chunkZ = chunkZ;

    final ModConfig cfg = this.chunkProvider.config;
    this.isNoiseEnabled = cfg.isNoiseEnabled();

    this.noise = this.isNoiseEnabled ? NoiseChunk.get(this.chunkProvider.world, chunkX, chunkZ,
        this.chunkProvider.noiseGenerator, cfg.getScale(), cfg.getSeaLevel()) : null;

    this.masterSphere = Sphere.get(chunkProvider, chunkX, chunkZ);
  }

  public int getChunkBoundSurfaceLevel(final int boundX, final int boundZ) {
    if (this.noise != null) {
      return this.noise.getChunkBoundSurfaceLevel(boundX, boundZ);
    }
    return this.chunkProvider.config.getSeaLevel();
  }

  public Random getPhaseRandom(final String phase) {
    return this.masterSphere.getPhaseRandom(phase, this.chunkX, this.chunkZ);
  }
}
