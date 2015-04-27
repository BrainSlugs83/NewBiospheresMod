/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderSurface;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.AvgCalc;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.Utils;

public class BiosphereWorldProvider extends WorldProviderSurface {
  @SuppressWarnings("unused")
  private static final AvgCalc avg = new AvgCalc();

  @SuppressWarnings("unused")
  private static long lastPrintedAt = Long.MIN_VALUE;

  private static final double searchGridAngles = 12;

  private static final double searchGridSize = 2.5d;

  private static final double toRadians = Math.PI / (BiosphereWorldProvider.searchGridAngles / 2);
  @SuppressWarnings("unused")
  private ModConfig config;

  public void fixSpawnLocation(final ChunkCoordinates coords) {
    final ChunkCoordinates orgCoords = Utils.getCoords(coords);

    double angle = 0;
    double power = 1;

    while (!this.isValidSpawnLocation(coords)) {
      angle++;
      if (angle >= BiosphereWorldProvider.searchGridAngles) {
        angle -= BiosphereWorldProvider.searchGridAngles;
        power++;
      }

      if (power >= 50) {
        coords.posX = orgCoords.posX;
        coords.posZ = orgCoords.posZ;

        if (ModConsts.DEBUG) {
          System.out.println("WARNING: BIOSPHERE FIX SPAWN LOCATION FAILED!!");
        }

        break;
      }

      final double x = Math.cos(angle * BiosphereWorldProvider.toRadians)
          * (power * BiosphereWorldProvider.searchGridSize);
      final double z = Math.sin(angle * BiosphereWorldProvider.toRadians)
          * (power * BiosphereWorldProvider.searchGridSize);

      coords.posX = orgCoords.posX + (int) Math.round(x);
      coords.posZ = orgCoords.posZ + (int) Math.round(z);
    }
  }

  @Override
  public ChunkCoordinates getRandomizedSpawnPoint() {
    final ChunkCoordinates coords = super.getSpawnPoint();

    this.fixSpawnLocation(coords); // , true);

    return coords;
  }

  @Override
  public ChunkCoordinates getSpawnPoint() {
    final ChunkCoordinates coords = super.getSpawnPoint();

    this.fixSpawnLocation(coords); // , false);

    return coords;
  }

  private boolean isValidSpawnLocation(final ChunkCoordinates coords) {
    if (coords == null) {
      return true;
    }

    final World world = this.worldObj;
    if (world == null) {
      return true;
    }

    final int x = coords.posX;
    final int z = coords.posZ;

    for (int y = 0; y < ModConsts.WORLD_HEIGHT; y++) {
      final Block block = world.getBlock(x, y, z);
      if ((block != Blx.air) && !block.isAir(world, x, y, z)) {
        return true;
      }
    }

    // no solid ground at this location!
    return false;
  }
}
