/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.models.Sphere;
import newbiospheresmod.models.SphereChunk;

public class BiosphereMapGen extends MapGenBase {
  BiosphereChunkProvider chunkProvider = null;

  protected void a(final int i, final int j, final Block[] blocks, final double d, final double d1, final double d2) {
    this.a(i, j, blocks, d, d1, d2, 10.0F + (this.rand.nextFloat() * 20.0F), 0.0F, 0.0F, -1, -1, 0.5D);
  }

  protected void a(final int chunkX, final int chunkZ, final Block[] blocks, double d, double d1, double d2,
      final float f, float f1, float f2, int k, int l, final double d3) {
    final int chunkHeight = blocks.length / (16 * 16);

    SphereChunk chunk = null;
    Sphere sphere = null;

    if (this.chunkProvider != null) {
      chunk = SphereChunk.get(this.chunkProvider, chunkX, chunkZ);
      if (chunk != null) {
        sphere = chunk.masterSphere;
      }
    }

    final double ccx = (chunkX * 16) + 8;
    final double ccz = (chunkZ * 16) + 8;
    float f3 = 0.0F;
    float f4 = 0.0F;
    final Random random = new Random(this.rand.nextLong());

    if (l <= 0) {
      final int flag = (this.range * 16) - 16;
      l = flag - random.nextInt(flag / 4);
    }

    boolean var61 = false;

    if (k == -1) {
      k = l / 2;
      var61 = true;
    }

    final int j1 = random.nextInt(l / 2) + (l / 4);

    for (final boolean flag1 = random.nextInt(6) == 0; k < l; ++k) {
      final double d6 = 1.5D + (MathHelper.sin((k * (float) Math.PI) / l) * f * 1.0F);
      final double d7 = d6 * d3;
      final float f5 = MathHelper.cos(f2);
      final float f6 = MathHelper.sin(f2);
      d += MathHelper.cos(f1) * f5;
      d1 += f6;
      d2 += MathHelper.sin(f1) * f5;

      if (flag1) {
        f2 *= 0.92F;
      } else {
        f2 *= 0.7F;
      }

      f2 += f4 * 0.1F;
      f1 += f3 * 0.1F;
      f4 *= 0.9F;
      f3 *= 0.75F;
      f4 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 2.0F;
      f3 += (random.nextFloat() - random.nextFloat()) * random.nextFloat() * 4.0F;

      if (!var61 && (k == j1) && (f > 1.0F)) {
        this.a(chunkX, chunkZ, blocks, d, d1, d2, (random.nextFloat() * 0.5F) + 0.5F, f1 - ((float) Math.PI / 2F),
            f2 / 3.0F, k, l, 1.0D);
        this.a(chunkX, chunkZ, blocks, d, d1, d2, (random.nextFloat() * 0.5F) + 0.5F, f1 + ((float) Math.PI / 2F),
            f2 / 3.0F, k, l, 1.0D);
        return;
      }

      if (var61 || (random.nextInt(4) != 0)) {
        final double d8 = d - ccx;
        final double d9 = d2 - ccz;
        final double d10 = l - k;
        final double d11 = f + 2.0F + 16.0F;

        if ((((d8 * d8) + (d9 * d9)) - (d10 * d10)) > (d11 * d11)) {
          return;
        }

        if ((d >= (ccx - 16.0D - (d6 * 2.0D))) && (d2 >= (ccz - 16.0D - (d6 * 2.0D)))
            && (d <= (ccx + 16.0D + (d6 * 2.0D))) && (d2 <= (ccz + 16.0D + (d6 * 2.0D)))) {
          int k1 = MathHelper.truncateDoubleToInt(d - d6) - (chunkX * 16) - 1;
          int l1 = (MathHelper.truncateDoubleToInt(d + d6) - (chunkX * 16)) + 1;
          int i2 = MathHelper.truncateDoubleToInt(d1 - d7) - 1;

          if (k1 < 0) {
            k1 = 0;
          }

          if (l1 > 16) {
            l1 = 16;
          }

          if (i2 < 1) {
            i2 = 1;
          }

          int j2 = MathHelper.truncateDoubleToInt(d1 + d7) + 1;
          int k2 = MathHelper.truncateDoubleToInt(d2 - d6) - (chunkZ * 16) - 1;
          int l2 = (MathHelper.truncateDoubleToInt(d2 + d6) - (chunkZ * 16)) + 1;

          if (j2 > 120) {
            j2 = 120;
          }

          if (k2 < 0) {
            k2 = 0;
          }

          if (l2 > 16) {
            l2 = 16;
          }

          boolean flag2 = false;
          int j3;
          int l3;

          for (l3 = k1; !flag2 && (l3 < l1); ++l3) {
            for (int d12 = k2; !flag2 && (d12 < l2); ++d12) {
              for (int j4 = j2 + 1; !flag2 && (j4 >= (i2 - 1)); --j4) {
                j3 = (((l3 * 16) + d12) * chunkHeight) + j4;

                if ((j4 >= 0) && (j4 < chunkHeight)) {
                  if ((blocks[j3] == Blx.flowing_water) || (blocks[j3] == Blx.water)
                      || (blocks[j3] == Blx.flowing_lava) || (blocks[j3] == Blx.lava)) {
                    flag2 = true;
                  }

                  if ((j4 != (i2 - 1)) && (l3 != k1) && (l3 != (l1 - 1)) && (d12 != k2) && (d12 != (l2 - 1))) {
                    j4 = i2;
                  }
                }
              }
            }
          }

          if (!flag2) {
            for (l3 = k1; l3 < l1; ++l3) {
              final double var62 = ((l3 + (chunkX * 16) + 0.5D) - d) / d6;

              for (j3 = k2; j3 < l2; ++j3) {
                final int midY = chunk.getChunkBoundSurfaceLevel(l3, j3);
                final double d13 = ((j3 + (chunkZ * 16) + 0.5D) - d2) / d6;
                int k4 = (((l3 * 16) + j3) * chunkHeight) + j2;

                for (int l4 = j2 - 1; l4 >= i2; --l4) {
                  final double d14 = ((l4 + 0.5D) - d1) / d7;

                  if ((d14 > -0.7D) && (((var62 * var62) + (d14 * d14) + (d13 * d13)) < 1.0D)) {
                    final Block block = blocks[k4];

                    if ((block == Blx.stone) || (block == Blx.sand) || (block == Blx.gravel)
                        || (block == Blx.diamond_ore) || (block == Blx.lapis_ore) || (block == Blx.emerald_ore)) {
                      if (l4 < ModConsts.LAVA_LEVEL) {
                        if (this.chunkProvider != null) {
                          final double d15 = sphere.getMainDistance((int) Math.round((ccx + l3) - 8.0D), l4 - 1,
                              (int) Math.round((ccz + j3) - 8.0D));

                          if ((d15 >= sphere.scaledSphereRadius) && (d15 < (sphere.scaledSphereRadius + 5d))) {
                            blocks[k4] = Blx.obsidian;
                          } else if (d15 < sphere.scaledSphereRadius) {
                            blocks[k4] = Blx.flowing_lava;
                          }
                        } else {
                          blocks[k4] = Blx.flowing_lava;
                        }
                      } else if ((l4 < (midY - 2)) || (l4 > (midY - 1))) {
                        blocks[k4] = Blx.air;
                      }
                    }
                  }

                  --k4;
                }
              }
            }

            if (var61) {
              break;
            }
          }
        }
      }
    }
  }

  @Override
  public void func_151539_a(final IChunkProvider chunkProvider, final World world, final int x, final int z,
      final Block[] blocks) {
    if ((this.chunkProvider == null) && (chunkProvider instanceof BiosphereChunkProvider)) {
      this.chunkProvider = (BiosphereChunkProvider) chunkProvider;
    }

    super.func_151539_a(chunkProvider, world, x, z, blocks);
  }

  /**
   * Recursively called by generate() (generate) and optionally by itself.
   */
  protected void recursiveGenerate(final World world, final int chunkX, final int chunkZ, final int k, final int l,
      final Block[] blocks) {
    int i1 = this.rand.nextInt(this.rand.nextInt(this.rand.nextInt(10) + 1) + 1);

    if (this.rand.nextInt(5) != 0) {
      i1 = 0;
    }

    for (int j1 = 0; j1 < i1; ++j1) {
      final double x = (chunkX * 16) + this.rand.nextInt(16);
      final double y = this.rand.nextInt(ModConsts.WORLD_HEIGHT);
      final double z = (chunkZ * 16) + this.rand.nextInt(16);
      int k1 = 1;

      if (this.rand.nextInt(4) == 0) {
        this.a(k, l, blocks, x, y, z);
        k1 += this.rand.nextInt(4);
      }

      for (int l1 = 0; l1 < k1; ++l1) {
        final float f = this.rand.nextFloat() * (float) Math.PI * 2.0F;
        final float f1 = ((this.rand.nextFloat() - 0.5F) * 2.0F) / 8.0F;
        final float f2 = (this.rand.nextFloat() * 2.0F) + this.rand.nextFloat();
        this.a(k, l, blocks, x, y, z, f2 * 5.0F, f, f1, 0, 0, 0.5D);
      }
    }
  }
}
