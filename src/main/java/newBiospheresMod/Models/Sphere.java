/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import newbiospheresmod.BiosphereChunkProvider;
import newbiospheresmod.BlockData;
import newbiospheresmod.BlockDome;
import newbiospheresmod.BlockEntry;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.Creator;
import newbiospheresmod.helpers.Func2;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.TopDownBoundingBox;
import newbiospheresmod.helpers.Utils;

public class Sphere {
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
      return ((this.x & 0xFFFF) | ((this.z & 0xFFFF) << 16)) ^ chunkProviderHash ^ 438129048;
    }
  }

  private static final BlockData flowing_lava = new BlockData(Blx.flowing_lava);

  private static final BlockData flowing_water = new BlockData(Blx.flowing_water);

  private static final BlockData glassDomeBlock = new BlockData(Blx.glass);

  // #endregion

  // #region Fields

  private static LruCacheList<Sphere> spheresCache = new LruCacheList<Sphere>(12, new IKeyProvider<Sphere>() {
    @Override
    public Object provideKey(final Sphere item) {
      if (item == null) {
        return null;
      }
      return new CacheKey(item.chunkProvider, item.sphereLocation.posX, item.sphereLocation.posZ);
    }
  });

  public static Sphere get(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ) {
    if (chunkProvider == null) {
      return null;
    }

    final ChunkCoordinates coords = Sphere.getSphereCenter(chunkX, chunkZ, chunkProvider.config);
    final Object key = new CacheKey(chunkProvider, coords.posX, coords.posZ);

    return Sphere.spheresCache.findOrAdd(key, new Creator<Sphere>() {
      @Override
      public Sphere create() {
        return new Sphere(chunkProvider, chunkX, chunkZ);
      }
    });
  }

  public static Sphere get(final World world, final int chunkX, final int chunkZ) {
    if (world == null) {
      return null;
    }
    final BiosphereChunkProvider provider = BiosphereChunkProvider.get(world);
    return Sphere.get(provider, chunkX, chunkZ);
  }

  private static ChunkCoordinates getSphereCenter(int chunkX, int chunkZ, final ModConfig cfg) {
    if (cfg == null) {
      return null;
    }

    final int chunkOffsetToCenterX = -(int) Math.floor(Math.IEEEremainder(chunkX, cfg.getScaledGridSize()));
    final int chunkOffsetToCenterZ = -(int) Math.floor(Math.IEEEremainder(chunkZ, cfg.getScaledGridSize()));

    chunkX += chunkOffsetToCenterX;
    chunkZ += chunkOffsetToCenterZ;

    final int x = ((chunkX) << 4) + 8;
    final int z = ((chunkZ) << 4) + 8;
    final int y = cfg.getSeaLevel();

    return Utils.getCoords(x, y, z);
  }

  public final BiomeGenBase biome;
  private List<TopDownBoundingBox> boundingBoxes = null;
  public final int bridgeWidth;
  private final int centerChunkX;
  private final int centerChunkZ;
  private final NoiseChunk centerNoiseChunk;

  public final BiosphereChunkProvider chunkProvider;
  public final boolean hasLake;
  public final boolean isDesert;
  public final boolean isNoiseEnabled;

  public final ChunkCoordinates lakeLocation;

  public final boolean lavaLake;
  public final ChunkCoordinates orbLocation;
  private Map<ChunkCoordinates, BlockData> orbStairwayBlx;
  private TopDownBoundingBox orbStairwayBox;

  public final double scale;
  public final int scaledGridSize;

  public final int scaledLakeEdgeRadius;

  public final int scaledLakeRadius;

  public final int scaledOrbRadius;

  public final int scaledSphereRadius;

  // #endregion

  public final int seaLevel;

  // #region Public Methods

  private final long seed;

  public final ChunkCoordinates sphereLocation;

  public final int sphereType;

  private Sphere(final BiosphereChunkProvider chunkProvider, final int chunkX, final int chunkZ) {
    this.chunkProvider = chunkProvider;
    final ModConfig _cfg = this.chunkProvider.config;

    this.seaLevel = _cfg.getSeaLevel();
    this.scale = _cfg.getScale();
    this.isNoiseEnabled = _cfg.isNoiseEnabled();
    this.scaledGridSize = _cfg.getScaledGridSize();
    this.bridgeWidth = _cfg.getBridgeWidth();
    this.scaledOrbRadius = _cfg.getScaledOrbRadius();

    this.sphereLocation = Sphere.getSphereCenter(chunkX, chunkZ, _cfg);
    this.centerChunkX = this.sphereLocation.posX >> 4;
    this.centerChunkZ = this.sphereLocation.posZ >> 4;

    if (this.isNoiseEnabled) {
      this.centerNoiseChunk = NoiseChunk.get(chunkProvider.world, this.centerChunkX, this.centerChunkZ,
          this.chunkProvider.noiseGenerator, this.scale, this.seaLevel);
    } else {
      this.centerNoiseChunk = null;
    }

    // Correct the Y value if Noise is enabled.
    this.sphereLocation.posY = this.getRawSurfaceLevel(this.sphereLocation.posX, this.sphereLocation.posZ);

    // Seed local random number generator
    Random rnd = new Random(chunkProvider.worldSeed);
    long xm = ((rnd.nextLong() / 2L) * 2L) + 1L;
    long zm = ((rnd.nextLong() / 2L) * 2L) + 1L;
    final long _seed = (((this.sphereLocation.posX * xm) + (this.sphereLocation.posZ * zm)) * 2512576L)
        ^ chunkProvider.worldSeed;
    rnd.setSeed(_seed);

    final double minRad = _cfg.getMinSphereRadius() * this.scale;
    final double maxRad = _cfg.getMaxSphereRadius() * this.scale;

    final double radRange = (maxRad - minRad);

    // Get sphere radius
    this.scaledSphereRadius = (int) Math.round(minRad + (rnd.nextDouble() * radRange));

    // Get lake radius
    final double lakeRatio = _cfg.getMinLakeRatio()
        + ((_cfg.getMaxLakeRatio() - _cfg.getMinLakeRatio()) * rnd.nextDouble());
    this.scaledLakeRadius = (int) Math.round(this.scaledSphereRadius * lakeRatio);
    this.scaledLakeEdgeRadius = this.scaledLakeRadius + 2;

    // Get the biome for the sphere.
    this.biome = this.chunkProvider.world.getWorldChunkManager().getBiomeGenAt(this.sphereLocation.posX,
        this.sphereLocation.posZ);

    this.isDesert = (this.biome.getFloatRainfall() <= 0);

    // is it a lava lake?
    this.lavaLake = (this.biome == BiomeGenBase.hell)
        || ((this.biome != BiomeGenBase.swampland) && (this.biome != BiomeGenBase.taiga)
            && (this.biome != BiomeGenBase.icePlains) && (this.biome != BiomeGenBase.sky) && (rnd.nextInt(10) == 0));
    this.hasLake = (this.biome == BiomeGenBase.swampland)
        || ((this.biome != BiomeGenBase.sky) && (rnd.nextInt(2) == 0));

    // Get the location of the ore orb
    this.orbLocation = Utils.getCoords(this.sphereLocation);
    final int lowY = this.sphereLocation.posY - this.scaledSphereRadius;
    final int highY = this.sphereLocation.posY + this.scaledSphereRadius;

    final int orbRange = ((this.scaledGridSize * 8)) - this.scaledOrbRadius;

    this.orbLocation.posX = this.sphereLocation.posX;
    this.orbLocation.posZ = this.sphereLocation.posZ;
    this.orbLocation.posY = this.seaLevel;

    // TODO: let's make this algorithm more efficient
    int giveUpAfter = 100;
    while (!this.isValidOrbLocation() && (giveUpAfter > 0)) {
      giveUpAfter--;
      this.orbLocation.posX = (this.sphereLocation.posX - orbRange)
          + (int) Math.round(rnd.nextDouble() * (orbRange * 2));

      this.orbLocation.posZ = (this.sphereLocation.posZ - orbRange)
          + (int) Math.round(rnd.nextDouble() * (orbRange * 2));

      this.orbLocation.posY = Utils.rndBetween(rnd, lowY, highY);
    }

    if (!this.isValidOrbLocation()) {
      // put the orb out of range
      this.orbLocation.posY = this.seaLevel;
      this.orbLocation.posX = this.sphereLocation.posX + (this.scaledGridSize * 32);
      this.orbLocation.posZ = this.sphereLocation.posZ + (this.scaledGridSize * 32);
    }

    this.lakeLocation = Utils.getCoords(this.sphereLocation);

    if (this.hasLake && this.isNoiseEnabled) {
      this.setLakeHeight();

      if (rnd.nextDouble() > .65d) {
        this.lakeLocation.posY -= (int) (Math.round((rnd.nextDouble() * 2d) * this.scale));
      }
    }

    this.setupOrbStairway(rnd);

    xm = ((rnd.nextLong() / 2L) * 2L) + 1L;
    zm = ((rnd.nextLong() / 2L) * 2L) + 1L;
    this.seed = (((this.centerChunkX * xm) + (this.centerChunkZ * zm)) * 3168045L) ^ this.chunkProvider.worldSeed;

    rnd = this.getPhaseRandom("SphereType", this.centerChunkX, this.centerChunkZ);
    int sType = -1;
    if (this.anySphereTypeValid()) {
      while (!this.isSphereTypeValid(sType)) {
        sType = rnd.nextInt() % ModConfig.DOMETYPE_COUNT;
      }
    }
    this.sphereType = sType;
  }

  private boolean anySphereTypeValid() {
    for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++) {
      if (this.isSphereTypeValid(i)) {
        return true;
      }
    }

    return false;
  }

  public List<TopDownBoundingBox> getBoundingBoxes() {
    if (this.boundingBoxes == null) {
      this.boundingBoxes = new ArrayList<TopDownBoundingBox>();

      // Sphere
      this.boundingBoxes.add(TopDownBoundingBox.fromCircle(this.sphereLocation.posX, this.sphereLocation.posZ,
          this.scaledSphereRadius + 1));

      // Ore Orb
      this.boundingBoxes.add(TopDownBoundingBox.fromCircle(this.orbLocation.posX, this.orbLocation.posZ,
          (this.scaledOrbRadius + 1)));

      // Z-aligned bridge
      this.boundingBoxes.add(new TopDownBoundingBox(this.sphereLocation.posX - (this.bridgeWidth + 1),
          Integer.MIN_VALUE, this.sphereLocation.posX + (this.bridgeWidth + 1), Integer.MAX_VALUE));

      // X-aligned bridge
      this.boundingBoxes.add(new TopDownBoundingBox(Integer.MIN_VALUE, this.sphereLocation.posZ
          - (this.bridgeWidth + 1), Integer.MAX_VALUE, this.sphereLocation.posZ + (this.bridgeWidth + 1)));

      // Stairway
      if (this.orbStairwayBox != null) {
        this.boundingBoxes.add(this.orbStairwayBox);
      }
    }

    return this.boundingBoxes;
  }

  public BlockData getDomeBlock(final int x, final int y, final int z) {
    BlockData ret = Sphere.glassDomeBlock;

    if (this.isSphereTypeValid(this.sphereType)) {
      final Random rnd = this.getPhaseRandom("SphereType_" + Integer.toString(x) + "_" + Integer.toString(y) + "_"
          + Integer.toString(z), this.centerChunkX, this.centerChunkZ);

      ret = ((BlockEntry) WeightedRandom.getRandomItem(rnd, ModConfig.DomeBlocks[this.sphereType])).block;
    }

    return BlockDome.getDomeBlock(ret);
  }

  public BlockData getLakeBlock() {
    if (!this.hasLake) {
      return BlockData.Empty;
    }
    return this.lavaLake ? Sphere.flowing_lava : Sphere.flowing_water;
  }

  public int getLakeDistance(final int rawX, final int rawY, final int rawZ) {
    if (!this.hasLake) {
      return Integer.MAX_VALUE;
    }

    final int lly = this.lakeLocation.posY;

    // a positive "dy" value indicates you are above the lake, while negative indicates below the
    // lake.
    double dy = rawY - lly;

    // lets increase the distance slightly to give an ellipsoid shape.
    if (dy < 0) {
      dy = dy * 2;

      if (this.isNoiseEnabled) {
        final int beforeNoise = (int) Math.round(Utils.getDistance(this.lakeLocation.posX, 0, this.lakeLocation.posZ,
            rawX, dy, rawZ));

        if ((rawX >= (this.lakeLocation.posX - this.scaledLakeEdgeRadius))
            && (rawZ >= (this.lakeLocation.posZ - this.scaledLakeEdgeRadius))
            && (rawX <= (this.lakeLocation.posX + this.scaledLakeEdgeRadius))
            && (rawZ <= (this.lakeLocation.posZ + this.scaledLakeEdgeRadius))) {
          final int x = this.sphereLocation.posX + ((rawX - this.sphereLocation.posX) * 20);
          final int z = this.sphereLocation.posZ + ((rawZ - this.sphereLocation.posZ) * 20);

          final double offset = this.getRawSurfaceLevel(x, z) - this.seaLevel;
          dy += Math.abs((offset / 4d));
        }

        final int afterNoise = (int) Math.round(Utils.getDistance(this.lakeLocation.posX, 0, this.lakeLocation.posZ,
            rawX, dy, rawZ));
        if (afterNoise < beforeNoise) {
          return afterNoise;
        } else {
          return beforeNoise;
        }
      }
    }

    return (int) Math.round(Utils.getDistance(this.lakeLocation.posX, 0, this.lakeLocation.posZ, rawX, dy, rawZ));
  }

  public int getMainDistance(final int rawX, final int rawY, final int rawZ) {
    return Utils.getDistance(this.sphereLocation, rawX, rawY, rawZ);
  }

  public int getOrbDistance(final int rawX, final int rawY, final int rawZ) {
    return Utils.getDistance(this.orbLocation, rawX, rawY, rawZ);
  }

  public BlockData getOrbStairwayBlock(final int x, final int y, final int z) {
    if (this.orbStairwayBox == null) {
      return null;
    }
    if (!this.orbStairwayBox.collidesWith(x, z)) {
      return null;
    }

    final ChunkCoordinates key = Utils.getCoords(x, y, z);

    if (this.orbStairwayBlx.containsKey(key)) {
      return this.orbStairwayBlx.get(key);
    }

    return null;
  }

  // #endregion

  // #region Private Methods

  public Random getPhaseRandom(final String phase, final int chunkX, final int chunkZ) {
    final Random rnd = new Random(this.seed);

    final long xm = ((rnd.nextLong() / 2L) * 2L) + 1L;
    final long zm = ((rnd.nextLong() / 2L) * 2L) + 1L;

    final long _seed = (((chunkX * xm) + (chunkZ * zm)) * phase.hashCode()) ^ this.chunkProvider.worldSeed;

    rnd.setSeed(_seed);
    return rnd;
  }

  public int getRawSurfaceLevel(final int rawX, final int rawZ) {
    if (this.centerNoiseChunk != null) {
      return this.centerNoiseChunk.getRawSurfaceLevel(rawX, rawZ);
    }
    return this.seaLevel;
  }

  private void setLakeHeight() {
    if (this.centerNoiseChunk == null) {
      this.lakeLocation.posY = this.seaLevel;
    } else {
      int x = this.lakeLocation.posX - this.scaledLakeRadius;
      int z = this.lakeLocation.posZ - this.scaledLakeRadius;
      boolean first = true;
      double min = 0;

      while (x <= (this.lakeLocation.posX + this.scaledLakeRadius)) {
        while (z <= (this.lakeLocation.posZ + this.scaledLakeRadius)) {
          final double val = this.centerNoiseChunk.getChunkAt(x, z).minNoise;
          if (first || (val < min)) {
            min = val;
            first = false;
          }

          z += 16;
        }
        x += 16;
      }

      this.lakeLocation.posY = (int) Math.round(this.seaLevel + (min * 8.0D * this.scale));
    }
  }

  private void setupOrbStairway(final Random rnd) {
    if (this.chunkProvider.config.doesNeedProtectionGlass()) {
      return;
    }

    final List<BlockEntry> stairwayBlocks = this.chunkProvider.config.stairwayBlocks;

    boolean foundBlock = false;
    for (final BlockEntry be : stairwayBlocks) {
      if (be.itemWeight > 0) {
        foundBlock = true;
        break;
      }
    }
    if (!foundBlock) {
      return;
    }

    int orbDistX;
    int orbDistZ;

    orbDistX = this.orbLocation.posX - this.sphereLocation.posX;
    orbDistZ = this.orbLocation.posZ - this.sphereLocation.posZ;

    if (Math.abs(orbDistX) < Math.abs(orbDistZ)) {
      orbDistZ = 0;
    } else if (Math.abs(orbDistX) > Math.abs(orbDistZ)) {
      orbDistX = 0;
    } else {
      if (rnd.nextBoolean()) {
        orbDistX = 0;
      } else {
        orbDistZ = 0;
      }
    }

    // bridge intersection point
    final int ix = this.orbLocation.posX - orbDistX;
    final int iz = this.orbLocation.posZ - orbDistZ;
    final int iy = this.getRawSurfaceLevel(ix, iz);

    final int tox = (orbDistX == 0 ? 0 : ((orbDistX > 0) ? 1 : -1));
    final int toz = (orbDistZ == 0 ? 0 : ((orbDistZ > 0) ? 1 : -1));

    this.orbStairwayBlx = new HashMap<ChunkCoordinates, BlockData>();

    if (orbDistZ == 0) {
      Utils.doLine(this.orbLocation.posX, this.orbLocation.posY, ix, iy, new Func2<Integer, Integer, Boolean>() {
        @Override
        public Boolean func(final Integer x, final Integer y) {
          for (int z = Sphere.this.orbLocation.posZ - Sphere.this.bridgeWidth; z <= (Sphere.this.orbLocation.posZ + Sphere.this.bridgeWidth); z++) {
            BlockData block = ((BlockEntry) WeightedRandom.getRandomItem(rnd, stairwayBlocks)).block;
            if (!BlockData.isNullOrEmpty(block)) {
              Sphere.this.orbStairwayBlx.put(Utils.getCoords(x, y, z), block);
            }

            block = ((BlockEntry) WeightedRandom.getRandomItem(rnd, stairwayBlocks)).block;
            if (!BlockData.isNullOrEmpty(block)) {
              Sphere.this.orbStairwayBlx.put(Utils.getCoords(x + tox, y, z + toz), block);
            }
          }

          return true;
        }
      });
    } else {
      Utils.doLine(this.orbLocation.posZ, this.orbLocation.posY, iz, iy, new Func2<Integer, Integer, Boolean>() {
        @Override
        public Boolean func(final Integer z, final Integer y) {
          for (int x = Sphere.this.orbLocation.posX - Sphere.this.bridgeWidth; x <= (Sphere.this.orbLocation.posX + Sphere.this.bridgeWidth); x++) {
            BlockData block = ((BlockEntry) WeightedRandom.getRandomItem(rnd, stairwayBlocks)).block;
            if (!BlockData.isNullOrEmpty(block)) {
              Sphere.this.orbStairwayBlx.put(Utils.getCoords(x, y, z), block);
            }

            block = ((BlockEntry) WeightedRandom.getRandomItem(rnd, stairwayBlocks)).block;
            if (!BlockData.isNullOrEmpty(block)) {
              Sphere.this.orbStairwayBlx.put(Utils.getCoords(x + tox, y, z + toz), block);
            }
          }

          return true;
        }
      });
    }

    this.orbStairwayBox = TopDownBoundingBox.fromArray(this.orbStairwayBlx.keySet());
  }

  // #endregion

  private boolean isSphereTypeValid(final int typeIndex) {
    if (typeIndex < 0) {
      return false;
    }
    if (typeIndex >= ModConfig.DOMETYPE_COUNT) {
      return false;
    }

    final ModConfig config = this.chunkProvider.config;

    if (ModConfig.DomeBlocks[typeIndex] != null) {
      for (int i = 0; i < ModConfig.DomeBlocks[typeIndex].size(); i++) {
        if (ModConfig.DomeBlocks[typeIndex].get(i).itemWeight > 0) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean isValidOrbLocation() {
    if (this.orbLocation.posY < (ModConsts.WORLD_MIN_Y + this.scaledOrbRadius)) {
      return false;
    }
    if (this.orbLocation.posY > (ModConsts.WORLD_MAX_Y - this.scaledOrbRadius)) {
      return false;
    }

    final int groundLevel = this.getRawSurfaceLevel(this.orbLocation.posX, this.orbLocation.posZ);
    if (Math.abs(groundLevel - this.orbLocation.posY) <= (this.scaledOrbRadius + 2)) {
      return false;
    }

    final TopDownBoundingBox sphereBox = TopDownBoundingBox.fromCircle(this.sphereLocation.posX,
        this.sphereLocation.posZ, this.scaledSphereRadius + 1);

    final TopDownBoundingBox orbBox = TopDownBoundingBox.fromCircle(this.orbLocation.posX, this.orbLocation.posZ,
        (this.scaledOrbRadius + 1));

    return !orbBox.collidesWith(sphereBox);

    // return Utils.GetDistance(orbLocation, sphereLocation) > (this.scaledOrbRadius +
    // this.scaledSphereRadius);
  }

}
