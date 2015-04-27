/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import newbiospheresmod.configuration.CustomWorldData;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;

public class BiosphereWorldType extends WorldType {
  // #region Ownership Tracking

  private static final LruCacheList<World> BiosphereWorlds = new LruCacheList<World>(3, new IKeyProvider<World>() {
    @Override
    public Object provideKey(final World item) {
      return item;
    }
  });

  public static final String IsBiosphereWorldKey = "IsBiosphereWorld";

  private static void ensureWorldIsTracked(final World world) {
    if (world != null) {
      BiosphereWorldType.BiosphereWorlds.push(world);

      final CustomWorldData data = CustomWorldData.fromWorld(world);
      if (data != null) {
        data.put(BiosphereWorldType.IsBiosphereWorldKey, true);
      }

      ModConfig.get(world).update();
    }
  }

  // #endregion

  public static boolean isBiosphereWorld(final World world) {
    if (world != null) {
      if (BiosphereWorldType.BiosphereWorlds.contains(world)) {
        return true;
      }

      final CustomWorldData data = CustomWorldData.fromWorld(world);
      if (data != null) {
        if (data.getBool(BiosphereWorldType.IsBiosphereWorldKey)) {
          BiosphereWorldType.ensureWorldIsTracked(world);
          return true;
        }
      }
    }

    return false;
  }

  public BiosphereWorldType(final String s) {
    super(s);
  }

  @Override
  public IChunkProvider getChunkGenerator(final World world, final String params) {
    BiosphereWorldType.BiosphereWorlds.push(world);
    return BiosphereChunkProvider.get(world);
  }

  @Override
  public WorldChunkManager getChunkManager(final World world) {
    // TODO: FIND A WAY TO UNREGISTER THIS IF THE PLAYER LOADS ANOTHER WORLD.
    BiomeGenBase.hell.topBlock = BiomeGenBase.hell.fillerBlock = Blx.netherrack;
    BiomeGenBase.sky.topBlock = BiomeGenBase.sky.fillerBlock = Blx.end_stone;

    Blx.water.setLightOpacity(0);
    Blx.flowing_water.setLightOpacity(0);

    Blx.lava.setLightOpacity(0);
    Blx.flowing_lava.setLightOpacity(0);

    BiosphereWorldType.BiosphereWorlds.push(world);
    return new BiosphereChunkManager(world);
  }

  public int getSeaLevel(final World world) {
    BiosphereWorldType.BiosphereWorlds.push(world);
    return ModConfig.get(world).getSeaLevel() + 1;
  }

  @Override
  public boolean hasVoidParticles(final boolean flag) {
    return false;
  }

  @Override
  public double voidFadeMagnitude() {
    return 1.0D;
  }
}
