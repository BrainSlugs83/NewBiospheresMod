/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import net.minecraft.block.Block;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.Utils;

public final class BlockData {
  // #region Static Utilities

  public static final BlockData Empty = new BlockData(null, 0);

  private static byte fixMetadata(final int value) {
    // Detect Issues
    Utils.assertTrue((0 <= value) && (value < 16), "metadata out of range.", 1);

    // Clamp the Value
    return (byte) (value & 0x0F);
  }

  public static Block[] getBlockArray(final BlockData[] input) {
    if (input == null) {
      return null;
    }

    final Block[] output = new Block[input.length];
    for (int i = 0; i < input.length; i++) {
      if (input[i] != null) {
        output[i] = input[i].block;
      }
    }

    return output;
  }

  public static byte[] getMetadataArray(final BlockData[] input) {
    if (input == null) {
      return null;
    }

    final byte[] output = new byte[input.length];
    for (int i = 0; i < input.length; i++) {
      if (input[i] != null) {
        output[i] = input[i].metadata;
      }
    }

    return output;
  }

  public static boolean isNullOrEmpty(final BlockData input) {
    return (input == null) ? true : input.isEmpty();
  }

  // #endregion

  // #region Instance Data

  public static BlockData parse(final String value) {
    return BlockData.parse(value, BlockData.Empty);
  }

  public static BlockData parse(final String value, final BlockData fallbackBlock) {
    Block block = net.minecraft.block.Block.getBlockFromName(value);

    if (block != null) {
      // block was parsable, it does not contain metadata.
      return new BlockData(block, 0);
    } else {
      final int sIndex = value.lastIndexOf(':');
      if (sIndex >= 0) {
        try {
          final String blockString = value.substring(0, sIndex);
          final String metaString = value.substring(sIndex + 1);

          block = net.minecraft.block.Block.getBlockFromName(blockString);
          final int metadata = Integer.parseInt(metaString);

          if (block != null) {
            return new BlockData(block, metadata);
          }
        } catch (final Throwable ignore) { /* do nothing */
        }
      }

      Utils.assertTrue(false, "Unable to parse BlockData: " + value, 1);
      return fallbackBlock;
    }
  }

  // #endregion

  // #region Properties

  public static String toString(final Block block, int metadata) {
    String ret = null;

    try {
      ret = net.minecraft.block.Block.blockRegistry.getNameForObject(block);
    } catch (final Exception ignore) { /* do nothing */
    }

    if ((ret == null) || (ret.length() < 1)) {
      ret = Integer.toString(net.minecraft.block.Block.getIdFromBlock(block));
    }

    if ((ret != null) && ret.toLowerCase().startsWith("minecraft:")) {
      ret = ret.substring(10);
    }

    metadata = BlockData.fixMetadata(metadata);

    if (metadata != 0) {
      ret += ":" + metadata;
    }

    return ret;
  }

  public final Block block;

  // #endregion

  // #region Constructors

  public final byte metadata;

  public BlockData(final Block block) {
    this(block, 0);
  }

  public BlockData(Block block, final byte metadata) {
    if (block == null) {
      block = Blx.air;
    }

    this.block = block;
    this.metadata = BlockData.fixMetadata(metadata);
  }

  // #endregion

  // #region Methods

  public BlockData(final Block block, final int metadata) {
    this(block, BlockData.fixMetadata(metadata));
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  // #endregion

  // #region ToString Support

  public boolean isEmpty() {
    return ((this.block == Blx.air) || (this.block == null)) && (this.metadata == 0);
  }

  public BlockData setBlock(final Block block) {
    return new BlockData(block, this.metadata);
  }

  public BlockData setMetadata(final int metadata) {
    return new BlockData(this.block, metadata);
  }

  // #endregion

  // #region Parse Support

  @Override
  public String toString() {
    return BlockData.toString(this.block, this.metadata);
  }

  // #endregion
}
