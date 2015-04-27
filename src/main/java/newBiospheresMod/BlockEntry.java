/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import net.minecraft.block.Block;
import net.minecraft.util.WeightedRandom;

public class BlockEntry extends WeightedRandom.Item {
  public static BlockEntry parse(final String input) {
    if ((input != null) && (input.length() > 0)) {
      try {
        final int idx = input.lastIndexOf(",");

        String blockName = null;
        String weight = null;

        if (idx > 0) {
          blockName = input.substring(0, idx).trim();
          weight = input.substring(idx + 1).trim();
        } else {
          blockName = input;
        }

        if ((blockName == null) || (blockName.length() < 1)) {
          blockName = "air";
        }

        if ((weight == null) || (weight.length() < 1)) {
          weight = "10";
        }

        int iWeight = Integer.parseInt(weight);
        if (iWeight < 0) {
          iWeight = 0;
        }

        return new BlockEntry(BlockData.parse(blockName), iWeight);
      } catch (final Throwable ignore) {
        // do nothing
      }
    }

    return new BlockEntry(BlockData.Empty, 0);
  }

  public final BlockData block;

  public BlockEntry(final Block block, final int metadata, final int weight) {
    this(new BlockData(block, metadata), weight);
  }

  public BlockEntry(BlockData block, final int weight) {
    super(weight);

    if (block == null) {
      block = BlockData.Empty;
    }

    this.block = block;
  }

  @Override
  public String toString() {
    return this.block + ", " + this.itemWeight;
  }
}
