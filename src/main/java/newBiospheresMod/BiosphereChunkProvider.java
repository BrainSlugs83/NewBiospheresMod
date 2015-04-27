/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.AvgCalc;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.Creator;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.Predicate;
import newbiospheresmod.helpers.TopDownBoundingBox;
import newbiospheresmod.helpers.Utils;
import newbiospheresmod.models.Sphere;
import newbiospheresmod.models.SphereChunk;

public class BiosphereChunkProvider implements IChunkProvider {
  // #region Caching

  private static class ChunkCacheKey {
    public final World world;
    public final int x;
    public final int z;

    public ChunkCacheKey(final World world, final int x, final int z) {
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
      if (!(obj instanceof ChunkCacheKey)) {
        return false;
      }
      final ChunkCacheKey other = (ChunkCacheKey) obj;

      return (this.x == other.x) && (this.z == other.z) && (this.world == other.world);
    }

    @Override
    public int hashCode() {
      final int worldHash = (this.world == null) ? 0 : this.world.hashCode();
      return ((this.x & 0xFFFF) | ((this.z & 0xFFFF) << 16)) ^ worldHash ^ 319023957;
    }
  }

  private static final AvgCalc avg = new AvgCalc();

  // #endregion

  // #region Chunk Caching

  private static LruCacheList<BiosphereChunkProvider> chunkProviders = new LruCacheList<BiosphereChunkProvider>(3,
      new IKeyProvider<BiosphereChunkProvider>() {
        @Override
        public Object provideKey(final BiosphereChunkProvider item) {
          if (item == null) {
            return null;
          }
          return item.world;
        }
      });

  private static long lastPrintedAt = Long.MIN_VALUE;

  // #endregion

  // #region Fields

  public static BiosphereChunkProvider get(final World world) {
    return BiosphereChunkProvider.chunkProviders.findOrAdd(world, new Creator<BiosphereChunkProvider>() {
      @Override
      public BiosphereChunkProvider create() {
        return new BiosphereChunkProvider(world);
      }
    });
  }

  private final MapGenBase caveGen = new BiosphereMapGen();
  LruCacheList<Chunk> chunksCache = new LruCacheList<Chunk>(15, new IKeyProvider<Chunk>() {
    @Override
    public Object provideKey(final Chunk item) {
      if (item == null) {
        return 0;
      }
      if (!item.isChunkLoaded) {
        return 0;
      }
      return new ChunkCacheKey(item.worldObj, item.xPosition, item.zPosition);
    }
  });
  public final ModConfig config;
  public final NoiseGeneratorOctaves noiseGenerator;

  // #endregion

  // /**
  // * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
  // */
  // public boolean getMapFeaturesEnabled()
  // {
  // return world.getWorldInfo().isMapFeaturesEnabled();
  // }

  public final World world;

  public final long worldSeed;

  private BiosphereChunkProvider(final World world) {
    this.world = world;
    this.worldSeed = world.getSeed();
    this.config = ModConfig.get(world);
    this.config.update();

    if (this.config.isNoiseEnabled()) {
      this.noiseGenerator = new NoiseGeneratorOctaves(new Random(this.worldSeed), 4);
    } else {
      this.noiseGenerator = null;
    }
  }

  /**
   * Returns if the IChunkProvider supports saving.
   */
  @Override
  public boolean canSave() {
    return true;
  }

  /**
   * Checks to see if a chunk exists at x, z
   */
  @Override
  public boolean chunkExists(final int x, final int z) {
    return true;
  }

  /**
   * Returns the location of the closest structure of the specified type. If not found returns null.
   */
  public ChunkPosition findClosestStructure(final World world1, final String s, final int i, final int j, final int k) {
    return null;
  }

  @Override
  public ChunkPosition func_147416_a(final World w, final String structName, final int x, final int y, final int z) {
    return null;
  }

  private SphereChunk generateChunk(final int chunkX, final int chunkZ, final BlockData[] blocks) {
    Arrays.fill(blocks, this.config.getOutsideFillerBlock());

    final SphereChunk chunk = SphereChunk.get(this, chunkX, chunkZ);

    if ((chunk != null) && (chunk.masterSphere != null)) {
      final Sphere sphere = chunk.masterSphere;

      final TopDownBoundingBox chunkBox = TopDownBoundingBox.fromChunk(chunkX, chunkZ);

      if (Utils.any(Utils.where(sphere.getBoundingBoxes(), new Predicate<TopDownBoundingBox>() {
        @Override
        public boolean test(final TopDownBoundingBox box) {
          return chunkBox.collidesWith(box);
        }
      }))) {
        // we collided with something like a sphere, ore orb, bridge, or other feature.
        this.generateChunkInner(chunkX, chunkZ, blocks, chunk);
      }
    }

    return chunk;
  }

  private void generateChunkInner(final int chunkX, final int chunkZ, final BlockData[] blocks, final SphereChunk chunk) {
    final int baseX = chunkX << 4;
    final int baseZ = chunkZ << 4;
    final int bridgeWidth = this.config.getBridgeWidth();
    final BlockData outsideFillerBlock = this.config.getOutsideFillerBlock();

    final Sphere sphere = chunk.masterSphere;
    final Random rnd = chunk.getPhaseRandom("GenerateChunk");

    for (int zo = 0; zo < 16; ++zo) {
      for (int xo = 0; xo < 16; ++xo) {
        final int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

        for (int rawY = ModConsts.WORLD_MAX_Y; rawY >= ModConsts.WORLD_MIN_Y; rawY--) {
          final int idx = ModConsts.getChunkArrayIndex(xo, rawY, zo);
          BlockData block = BlockData.Empty;

          final int rawX = baseX + xo;
          final int rawZ = baseZ + zo;

          final int sphereDistance = sphere.getMainDistance(rawX, rawY, rawZ);
          final int orbDistance = sphere.getOrbDistance(rawX, rawY, rawZ);
          final int lakeDistance = sphere.getLakeDistance(rawX, rawY, rawZ);

          if (sphereDistance > sphere.scaledSphereRadius) {
            final BlockData stairwayBlock = sphere.getOrbStairwayBlock(rawX, rawY, rawZ);

            if (stairwayBlock != null) {
              block = stairwayBlock;
            }
          }

          if (rawY > midY) {
            if (sphere.scaledSphereRadius == sphereDistance) {
              if ((rawY >= (midY + 4))
                  || ((Math.abs(rawX - sphere.sphereLocation.posX) > bridgeWidth) && (Math.abs(rawZ
                      - sphere.sphereLocation.posZ) > bridgeWidth))) {
                block = sphere.getDomeBlock(rawX, rawY, rawZ);
              }
            } else if (sphere.hasLake && this.config.isNoiseEnabled() && sphere.isDesert
                && ((lakeDistance > sphere.scaledLakeRadius) && (lakeDistance <= sphere.scaledLakeEdgeRadius))) {
              if (rawY == sphere.lakeLocation.posY) {
                block = new BlockData(sphere.biome.topBlock);
              } else if (rawY < sphere.lakeLocation.posY) {
                block = new BlockData(sphere.biome.fillerBlock);
              }
            } else if (sphere.hasLake && this.config.isNoiseEnabled() && sphere.isDesert
                && (lakeDistance <= sphere.scaledLakeRadius)) {
              if ((rawY == sphere.lakeLocation.posY) && (sphere.biome == BiomeGenBase.icePlains)) {
                block = new BlockData(Blx.ice);
              } else if (rawY <= sphere.lakeLocation.posY) {
                block = sphere.getLakeBlock();
              }
            } else if (this.config.doesNeedProtectionGlass()
                && (rawY <= (midY + 4))
                && (sphereDistance > sphere.scaledSphereRadius)
                && ((Math.abs(rawX - sphere.sphereLocation.posX) == bridgeWidth) || (Math.abs(rawZ
                    - sphere.sphereLocation.posZ) == bridgeWidth))) {
              block = sphere.getDomeBlock(rawX, rawY, rawZ);
            } else if (this.config.doesNeedProtectionGlass()
                && (rawY == (midY + 4))
                && (sphereDistance > sphere.scaledSphereRadius)
                && ((Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth) || (Math.abs(rawZ
                    - sphere.sphereLocation.posZ) < bridgeWidth))) {
              block = sphere.getDomeBlock(rawX, rawY, rawZ);
            } else if (this.config.doesNeedProtectionGlass()
                && (rawY < (midY + 4))
                && (sphereDistance > sphere.scaledSphereRadius)
                && ((Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth) || (Math.abs(rawZ
                    - sphere.sphereLocation.posZ) < bridgeWidth))) {
              block = BlockData.Empty;
            } else if (this.config.doesNeedProtectionGlass() && (sphereDistance > sphere.scaledSphereRadius)) {
              block = outsideFillerBlock;
            } else if ((rawY == (midY + 1))
                && (sphereDistance > sphere.scaledSphereRadius)
                && ((Math.abs(rawX - sphere.sphereLocation.posX) == bridgeWidth) || (Math.abs(rawZ
                    - sphere.sphereLocation.posZ) == bridgeWidth))) {
              block = this.config.getBridgeRailBlock();
            }
          } else if (sphere.scaledSphereRadius == sphereDistance) {
            block = new BlockData(Blx.stone);
          } else if (sphere.hasLake && sphere.isDesert && (lakeDistance <= sphere.scaledLakeRadius)) {
            if ((rawY == sphere.lakeLocation.posY) && (sphere.biome == BiomeGenBase.icePlains)) {
              block = new BlockData(Blx.ice);
            } else if (rawY <= sphere.lakeLocation.posY) {
              block = sphere.getLakeBlock();
            }
          } else if (sphere.hasLake && (rawY < (sphere.lakeLocation.posY - 1)) && sphere.isDesert
              && (lakeDistance <= sphere.scaledLakeEdgeRadius)) {
            if (ModConsts.DEBUG) {
              block = new BlockData(Blx.glowstone);
            } else {
              block = new BlockData(sphere.lavaLake ? Blx.gravel : Blx.sand);
            }
          } else if (sphereDistance < sphere.scaledSphereRadius) {
            if (rawY == midY) {
              block = new BlockData(sphere.biome.topBlock);
            } else if (rawY == (midY - 1)) {
              block = new BlockData(sphere.biome.fillerBlock);
            } else {
              block = new BlockData(Blx.stone);
            }
          } else if ((rawY == midY)
              && (sphereDistance > sphere.scaledSphereRadius)
              && ((Math.abs(rawX - sphere.sphereLocation.posX) < (bridgeWidth + 1)) || (Math.abs(rawZ
                  - sphere.sphereLocation.posZ) < (bridgeWidth + 1)))) {
            block = this.config.getBridgeSupportBlock();
          } else if (this.config.doesNeedProtectionGlass() && (sphereDistance > sphere.scaledSphereRadius)) {
            block = outsideFillerBlock;
          }

          if (sphere.scaledOrbRadius == orbDistance) {
            block = this.config.getOrbBlock();
          } else if (orbDistance < sphere.scaledOrbRadius) {
            block = ((BlockEntry) WeightedRandom.getRandomItem(rnd, this.config.oreOrbBlocks)).block;
          }

          blocks[idx] = block;
        }
      }
    }
  }

  private Chunk generateNewChunk(final int x, final int z) {
    final long startedAt = System.currentTimeMillis();

    final BlockData[] blockData = new BlockData[ModConsts.getChunkArraySize()];
    final SphereChunk sphereChunk = this.generateChunk(x, z, blockData);

    final Block[] blocks = BlockData.getBlockArray(blockData);
    final byte[] metadata = BlockData.getMetadataArray(blockData);

    this.caveGen.func_151539_a(this, this.world, x, z, blocks); // func_151539_a == generate

    final Chunk chunk = new Chunk(this.world, blocks, metadata, x, z);
    chunk.generateSkylightMap();

    // Fix for Issue #24:
    if (sphereChunk != null) {
      if (sphereChunk.masterSphere != null) {
        if (sphereChunk.masterSphere.biome != null) {
          // System.out.println("Biome at " + sphereChunk.chunkX + ", " + sphereChunk.chunkZ + ": "
          // +
          // sphereChunk.masterSphere.biome.biomeName);

          final byte[] biomes = new byte[256];
          Arrays.fill(biomes, (byte) (sphereChunk.masterSphere.biome.biomeID & 0xFF));
          chunk.setBiomeArray(biomes);
        }
      }
    }

    // It's normal to see performance warnings for a few chunks at start-up and maybe every once in
    // a while after
    // that, but the average should drop down to less than a millisecond within a minute or so.

    final long now = System.currentTimeMillis();
    final long elapsed = now - startedAt;
    BiosphereChunkProvider.avg.addValue(elapsed / 1000d);
    if (elapsed >= 100) {
      System.out.printf("WARNING: BIOSPHERE GENERATE NEW CHUNK @ [%d, %d] TOOK %.3f SECONDS!%n", x, z,
          (elapsed / 1000d));
    }

    if ((BiosphereChunkProvider.lastPrintedAt == Long.MIN_VALUE)
        || ((now - BiosphereChunkProvider.lastPrintedAt) > 2500)) {
      BiosphereChunkProvider.lastPrintedAt = now;
      if (BiosphereChunkProvider.avg.getCount() >= 5) {
        final double av = BiosphereChunkProvider.avg.getAverage();
        if (av >= .02D) {
          System.out.printf("INFO: BIOSPHERE GENERATE NEW CHUNK ON AVERAGE TAKES %.3f SECONDS.%n", av);
        }
      }
    }

    return chunk;
  }

  private int getBioHeightValue(final int x, final int z) {
    return this.world.getHeightValue(x, z);
  }

  private int getBioTopSolidOrLiquidBlock(final int x, final int z) {
    for (int y = this.world.getHeightValue(x, z); y > 0; y--) {
      final Block b = this.world.getBlock(x, y, z);
      if (!(b instanceof BlockDome) && b.getMaterial().blocksMovement() && (b.getMaterial() != Material.leaves)
          && !b.isFoliage(this.world, x, y, z)) {
        return y + 1;
      }
    }

    return -1;
  }

  @Override
  public int getLoadedChunkCount() {
    return 0;
  }

  /**
   * Returns a list of creatures of the specified type that can spawn at the given location.
   */
  @Override
  public List<?> getPossibleCreatures(final EnumCreatureType enumcreaturetype, final int i, final int j, final int k) {
    final BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(i, k);
    return biomegenbase == null ? null : biomegenbase.getSpawnableList(enumcreaturetype);
  }

  /**
   * loads or generates the chunk at the chunk location specified
   */
  @Override
  public Chunk loadChunk(final int x, final int z) {
    return this.provideChunk(x, z);
  }

  /**
   * Converts the instance data to a readable string.
   */
  @Override
  public String makeString() {
    return "RandomLevelSource";
  }

  /**
   * Populates chunk with ores etc etc
   */
  @Override
  public void populate(final IChunkProvider chunkProvider, final int chunkX, final int chunkZ) {
    final SphereChunk chunk = SphereChunk.get(this, chunkX, chunkZ);
    final Sphere sphere = chunk.masterSphere;
    final Random rnd = chunk.getPhaseRandom("populate");

    BlockFalling.fallInstantly = true;
    final int absX = chunkX << 4;
    final int absZ = chunkZ << 4;

    for (int i = 0; i < 20; i++) {
      final int x = absX + rnd.nextInt(16);
      final int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
      final int z = absZ + rnd.nextInt(16);

      (new WorldGenMinable(Blx.coal_ore, 16)).generate(this.world, rnd, x, y, z);
    }

    for (int i = 0; i < 20; i++) {
      final int x = absX + rnd.nextInt(16);
      final int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
      final int z = absZ + rnd.nextInt(16);

      (new WorldGenMinable(Blx.iron_ore, 8)).generate(this.world, rnd, x, y, z);
    }

    for (int i = 0; i < 2; i++) {
      final int x = absX + rnd.nextInt(16);
      final int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
      final int z = absZ + rnd.nextInt(16);

      (new WorldGenMinable(Blx.gold_ore, 8)).generate(this.world, rnd, x, y, z);
    }

    for (int i = 0; i < 8; i++) {
      final int x = absX + rnd.nextInt(16);
      final int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
      final int z = absZ + rnd.nextInt(16);

      (new WorldGenMinable(Blx.redstone_ore, 7)).generate(this.world, rnd, x, y, z);
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.sandPerChunk2; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      final int y = this.getBioTopSolidOrLiquidBlock(x, z);

      sphere.biome.theBiomeDecorator.sandGen.generate(this.world, rnd, x, y, z);
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.clayPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      final int y = this.getBioTopSolidOrLiquidBlock(x, z);

      if (y > 0) {
        sphere.biome.theBiomeDecorator.clayGen.generate(this.world, rnd, x, y, z);
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.sandPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      final int y = this.getBioTopSolidOrLiquidBlock(x, z);

      if (y > 0) {
        sphere.biome.theBiomeDecorator.gravelAsSandGen.generate(this.world, rnd, x, y, z);
      }
    }

    int treesPerChunk = sphere.biome.theBiomeDecorator.treesPerChunk;

    if (rnd.nextInt(10) == 0) {
      treesPerChunk++;
    }

    for (int i = 0; i < treesPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      final int y = this.world.getHeightValue(x, z);

      if (y > 0) {
        // func_150567_a == getRandomWorldGenForTrees
        final WorldGenerator gen = sphere.biome.func_150567_a(rnd);

        gen.setScale(this.config.getScale(), this.config.getScale(), this.config.getScale());
        gen.generate(this.world, rnd, x, y, z);
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.bigMushroomsPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      final int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        sphere.biome.theBiomeDecorator.bigMushroomGen.generate(this.world, rnd, x, y, z);
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.flowersPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        y = rnd.nextInt(y + 32);
        final String s = sphere.biome.func_150572_a(rnd, x, y, z);

        final BlockFlower f = BlockFlower.func_149857_e(s);
        if (f.getMaterial() != Material.air) {
          (new WorldGenFlowers(f)).generate(this.world, rnd, x, y, z);
        }
      }
    }

    for (int i = 0; i <= sphere.biome.theBiomeDecorator.mushroomsPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        if (i == 0) {
          y = rnd.nextInt(y * 2);
        }

        if (rnd.nextInt(4) == 0) {
          sphere.biome.theBiomeDecorator.mushroomBrownGen.generate(this.world, rnd, x, y, z);
        }

        if (rnd.nextInt(8) == 0) {
          if (i != 0) {
            y = rnd.nextInt(y * 2);
          }

          sphere.biome.theBiomeDecorator.mushroomRedGen.generate(this.world, rnd, x, y, z);
        }
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.deadBushPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        y = rnd.nextInt(y * 2);
        (new WorldGenDeadBush(Blx.deadbush)).generate(this.world, rnd, x, y, z);
      }
    }

    if (this.config.isTallGrassEnabled()) {
      final int grassPerChunk = sphere.biome.theBiomeDecorator.grassPerChunk;
      for (int i = 0; i < grassPerChunk; i++) {
        final int x = absX + rnd.nextInt(16) + 8;
        final int z = absZ + rnd.nextInt(16) + 8;
        int y = this.getBioHeightValue(x, z);

        if (y > 0) {
          y = rnd.nextInt(y * 2);
          final WorldGenerator worldgenerator = sphere.biome.getRandomWorldGenForGrass(rnd);
          worldgenerator.generate(this.world, rnd, x, y, z);
        }
      }
    }

    for (int i = 0; i < (sphere.biome.theBiomeDecorator.reedsPerChunk + 10); i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        y = rnd.nextInt(y * 2);
        (new WorldGenReed()).generate(this.world, rnd, x, y, z);
      }
    }

    if (rnd.nextInt(32) == 0) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);
      if (y > 0) {
        y = rnd.nextInt(y);
        (new WorldGenPumpkin()).generate(this.world, rnd, x, y, z);
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.cactiPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.getBioHeightValue(x, z);

      if (y > 0) {
        y = rnd.nextInt(y * 2);
        (new WorldGenCactus()).generate(this.world, rnd, x, y, z);
      }
    }

    if (sphere.biome == BiomeGenBase.hell) {
      if (rnd.nextBoolean()) {
        final int x = absX + rnd.nextInt(16) + 8;
        final int z = absZ + rnd.nextInt(16) + 8;
        final int y = this.world.getHeightValue(x, z);
        if (y > 0) {
          (new WorldGenFire()).generate(this.world, rnd, x, y, z);
        }
      }

      if (rnd.nextBoolean()) {
        final int x = absX + rnd.nextInt(16) + 8;
        final int z = absZ + rnd.nextInt(16) + 8;
        final int y = this.world.getHeightValue(x, z);
        if (y > 0) {
          (new WorldGenFlowers(Blx.brown_mushroom)).generate(this.world, rnd, x, y, z);
        }
      }

      if (rnd.nextBoolean()) {
        final int x = absX + rnd.nextInt(16) + 8;
        final int z = absZ + rnd.nextInt(16) + 8;
        final int y = this.world.getHeightValue(x, z);
        if (y > 0) {
          (new WorldGenFlowers(Blx.red_mushroom)).generate(this.world, rnd, x, y, z);
        }
      }
    } else if (sphere.biome.getEnableSnow()) {
      for (int zo = 0; zo < 16; zo++) {
        for (int xo = 0; xo < 16; xo++) {
          final int x = xo + absX;
          final int z = zo + absZ;
          int y = this.world.getHeightValue(x, z) + 16;
          while ((y > 0) && (this.world.isAirBlock(x, --y, z) || (this.world.getBlock(x, y, z) instanceof BlockDome))) { /*
                                                                                                                          * do
                                                                                                                          * nothing
                                                                                                                          */
          }

          if (y > 0) {
            if (this.world.isBlockFreezable(x, y, z)) {
              this.world.setBlock(x, y, z, Blx.ice);
            } else if (this.world.func_147478_e(x, y, z, false)
                && Blx.snow_layer.canPlaceBlockAt(this.world, x, y + 1, z)) {
              if (!(this.world.getBlock(x, y + 1, z) instanceof BlockDome)) {
                this.world.setBlock(x, y + 1, z, Blx.snow_layer, 0, 2);
              }
            }
          }
        }
      }
    }

    for (int i = 0; i < sphere.biome.theBiomeDecorator.waterlilyPerChunk; i++) {
      final int x = absX + rnd.nextInt(16) + 8;
      final int z = absZ + rnd.nextInt(16) + 8;
      int y = this.world.getHeightValue(x, z);

      if (y > 0) {
        while ((y < this.world.getActualHeight()) && !this.world.isAirBlock(x, ++y, z)) { /* do nothing */
        }

        while ((y > 0) && (this.world.isAirBlock(x, --y - 1, z) || (this.world.getBlock(x, y, z) instanceof BlockDome))) { /*
                                                                                                                            * do
                                                                                                                            * nothing
                                                                                                                            */
        }

        (new WorldGenWaterlily()).generate(this.world, rnd, x, y + 1, z);
      }
    }

    SpawnerAnimals.performWorldGenSpawning(this.world, sphere.biome, absX + 8, absZ + 8, 16, 16, rnd);
    BlockFalling.fallInstantly = false;
  }

  /**
   * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
   * specified chunk from the map seed and chunk seed
   */
  @Override
  public Chunk provideChunk(final int x, final int z) {
    return this.chunksCache.findOrAdd(new ChunkCacheKey(this.world, x, z), new Creator<Chunk>() {
      @Override
      public Chunk create() {
        return BiosphereChunkProvider.this.generateNewChunk(x, z);
      }
    });
  }

  @Override
  public void recreateStructures(final int var1, final int var2) {
    /* do nothing */
  }

  /**
   * Two modes of operation: if passed true, save all Chunks in one go. If passed false, save up to two chunks. Return
   * true if all chunks have been saved.
   */
  @Override
  public boolean saveChunks(final boolean flag, final IProgressUpdate iprogressupdate) {
    return true;
  }

  @Override
  public void saveExtraData() {
    /* do nothing */
  }

  /**
   * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
   */
  @Override
  public boolean unloadQueuedChunks() {
    return false;
  }
}
