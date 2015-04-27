/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeEntry extends WeightedRandom.Item {
  public final BiomeGenBase biome;

  public BiomeEntry(final BiomeGenBase biomegenbase, final int i) {
    super(i);
    this.biome = biomegenbase;
  }
}
