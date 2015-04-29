/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

import net.minecraft.util.ChunkCoordinates;

public class TopDownBoundingBox {
  public static TopDownBoundingBox fromArray(final Iterable<ChunkCoordinates> coords) {
    if (coords != null) {
      boolean first = true;
      int minX;
      int minZ;
      int maxX;
      int maxZ;
      minX = minZ = maxX = maxZ = 0;

      for (final ChunkCoordinates coord : coords) {
        if (coord != null) {
          if (first) {
            minX = maxX = coord.posX;
            minZ = maxZ = coord.posZ;
            first = false;
          } else {
            if (coord.posX < minX) {
              minX = coord.posX;
            }
            if (coord.posX > maxX) {
              maxX = coord.posX;
            }
            if (coord.posZ < minZ) {
              minZ = coord.posZ;
            }
            if (coord.posZ > maxZ) {
              maxZ = coord.posZ;
            }
          }
        }
      }

      if (!first) {
        return new TopDownBoundingBox(minX, minZ, maxX, maxZ);
      }
    }

    return null;
  }

  public static TopDownBoundingBox fromChunk(int chunkX, int chunkZ) {
    chunkX <<= 4;
    chunkZ <<= 4;

    return new TopDownBoundingBox(chunkX, chunkZ, chunkX + 15, chunkZ + 15);
  }

  public static TopDownBoundingBox fromCircle(final int cx, final int cz, final int r) {
    return new TopDownBoundingBox(cx - r, cz - r, cx + r, cz + r);
  }

  public final int x1;
  public final int x2;
  public final int z1;
  public final int z2;

  public TopDownBoundingBox(final int x1, final int z1, final int x2, final int z2) {
    this.x1 = x1;
    this.z1 = z1;
    this.x2 = x2;
    this.z2 = z2;
  }

  public boolean collidesWith(final int x, final int z) {
    if (x < this.x1) {
      return false;
    }
    if (z < this.z1) {
      return false;
    }
    if (x > this.x2) {
      return false;
    }
    if (z > this.z2) {
      return false;
    }

    return true;
  }

  public boolean collidesWith(final TopDownBoundingBox box) {
    if (box == this) {
      return true;
    }
    if (box == null) {
      return false;
    }

    if (this.x2 < box.x1) {
      return false;
    }
    if (this.z2 < box.z1) {
      return false;
    }
    if (this.x1 > box.x2) {
      return false;
    }
    if (this.z1 > box.z2) {
      return false;
    }

    return true;
  }
}
