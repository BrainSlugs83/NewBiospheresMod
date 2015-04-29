/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import java.util.List;
import java.util.Random;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import newbiospheresmod.configuration.ModConfig;

public class BiosphereChunkManager extends WorldChunkManager {
  private static long lastSeed = 0;
  private final ModConfig config;

  private final long seed;
  @SuppressWarnings("unused")
  private final World world;

  // private final Random rnd;

  // private static long getNewSeed()
  // {
  // long uuid = UUID.randomUUID().hashCode()
  // return uuid | (uuid << 32);
  // }
  //
  public BiosphereChunkManager() {
    this(BiosphereChunkManager.lastSeed, NewBiospheresMod.biosphereWorldType);
  }

  public BiosphereChunkManager(final long worldSeed, final WorldType worldType) {
    this(null, worldSeed, worldType);
  }

  public BiosphereChunkManager(final World world) {
    this(world, world.getSeed(), NewBiospheresMod.biosphereWorldType);
  }

  public BiosphereChunkManager(final World world, final long worldSeed, final WorldType worldType) {
    super(worldSeed, worldType);

    this.world = world;
    this.seed = worldSeed;
    BiosphereChunkManager.lastSeed = worldSeed;
    // this.rnd = new Random(this.seed);
    this.config = ModConfig.get(world);
  }

  /**
   * Checks given Chunk's Biomes against List of allowed ones.
   */
  @Override
  @SuppressWarnings("rawtypes")
  public boolean areBiomesViable(final int i, final int j, final int k, final List list) {
    return true;
  }

  // public float getHumid(int i, int j)
  // {
  // float f = this.getBiomeGenAt(i, j).rainfall;
  // return f <= 1.0F ? f : 1.0F;
  // }

  // /**
  // * Returns a list of rainfall values for the specified blocks. Args: listToReuse, x, z, width,
  // length.
  // */
  // @Override
  // public float[] getRainfall(float[] listToReuse, int x, int z, int width, int length)
  // {
  // IntCache.resetIntCache();
  //
  // if (listToReuse == null || listToReuse.length < width * length)
  // {
  // listToReuse = new float[width * length];
  // }
  //
  // float f = this.getHumid(x, z);
  // int i1 = 0;
  //
  // for (int j1 = 0; j1 < width; ++j1)
  // {
  // for (int k1 = 0; k1 < length; ++k1)
  // {
  // listToReuse[i1] = f;
  // ++i1;
  // }
  // }
  //
  // return listToReuse;
  // }

  // public float getTemp(int i, int j)
  // {
  // float f = this.getBiomeGenAt(i, j).temperature;
  // return f <= 1.0F ? f : 1.0F;
  // }

  // /**
  // * Returns a list of temperatures to use for the specified blocks. Args: listToReuse, x, z,
  // width, length
  // */
  // public float[] getTemperatures(float[] listToReuse, int x, int z, int width, int length)
  // {
  // IntCache.resetIntCache();
  //
  // if (listToReuse == null || listToReuse.length < width * length)
  // {
  // listToReuse = new float[width * length];
  // }
  //
  // float f = this.getTemp(x, z);
  // int i1 = 0;
  //
  // for (int j1 = 0; j1 < width; ++j1)
  // {
  // for (int k1 = 0; k1 < length; ++k1)
  // {
  // listToReuse[i1] = f;
  // ++i1;
  // }
  // }
  //
  // return listToReuse;
  // }

  /**
   * Returns the BiomeGenBase related to the x, z position on the world.
   */

  @Override
  public BiomeGenBase getBiomeGenAt(final int x, final int z) {
    final int k = x >> 4;
    final int l = z >> 4;
    final int i1 = ((k - (int) Math.floor(Math.IEEEremainder(k, this.config.getScaledGridSize()))) << 4) + 8;
    final int j1 = ((l - (int) Math.floor(Math.IEEEremainder(l, this.config.getScaledGridSize()))) << 4) + 8;

    final Random rnd = new Random(this.seed);
    final long l1 = ((rnd.nextLong() / 2L) * 2L) + 1L;
    final long l2 = ((rnd.nextLong() / 2L) * 2L) + 1L;
    rnd.setSeed((((i1 * l1) + (j1 * l2)) * 7215145L) ^ this.seed);
    return ((BiomeEntry) WeightedRandom.getRandomItem(rnd, this.config.allBiomes)).biome;
  }

  // /**
  // * Returns an array of biomes for the location input.
  // */
  // @Override
  // public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomes, int i, int j, int k, int l)
  // {
  // return this.getBiomeGenAt(biomes, i, j, k, l, false);
  // }

  // /**
  // * Return a list of biomes for the specified blocks. Args: listToReuse, x, y, width, length,
  // cacheFlag (if false,
  // * don't check biomeCache to avoid infinite loop in BiomeCacheBlock)
  // */
  // @Override
  // public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] biomes, int x, int z, int width, int length,
  // boolean
  // cacheFlag)
  // {
  // IntCache.resetIntCache();
  //
  // final int size = width * length;
  //
  // if (biomes == null || biomes.length < size)
  // {
  // biomes = new BiomeGenBase[size];
  // }
  //
  // BiomeGenBase biomegenbase = this.getBiomeGenAt(x, z);
  //
  // for (int i = 0; i < size; i++)
  // {
  // biomes[i] = biomegenbase;
  // }
  //
  // return biomes;
  // }

  // /**
  // * Finds a valid position within a range, that is in one of the listed biomes. Searches
  // {par1,par2} +-par3 blocks.
  // * Strongly favors positive y positions.
  // */
  // @Override
  // public ChunkPosition findBiomePosition(int i, int j, int k, List list, Random random)
  // {
  // return new ChunkPosition(0, 64, 0);
  // }

}
