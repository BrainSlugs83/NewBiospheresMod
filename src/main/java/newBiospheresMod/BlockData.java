/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import net.minecraft.block.*;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.Utils;

public final class BlockData
{
	// #region Static Utilities

	public static final BlockData Empty = new BlockData(null, 0);

	public static boolean IsNullOrEmpty(BlockData input)
	{
		return (input == null) ? true : input.IsEmpty();
	}

	private static byte FixMetadata(int value)
	{
		// Detect Issues
		Utils.Assert(0 <= value && value < 16, "metadata out of range.", 1);

		// Clamp the Value
		return (byte)(value & 0x0F);
	}

	public static Block[] getBlockArray(BlockData[] input)
	{
		if (input == null) { return null; }

		Block[] output = new Block[input.length];
		for (int i = 0; i < input.length; i++)
		{
			if (input[i] != null)
			{
				output[i] = input[i].Block;
			}
		}

		return output;
	}

	public static byte[] getMetadataArray(BlockData[] input)
	{
		if (input == null) { return null; }

		byte[] output = new byte[input.length];
		for (int i = 0; i < input.length; i++)
		{
			if (input[i] != null)
			{
				output[i] = input[i].Metadata;
			}
		}

		return output;
	}

	// #endregion

	// #region Instance Data

	public final Block Block;
	public final byte Metadata;

	// #endregion

	// #region Properties

	public boolean IsEmpty()
	{
		return (this.Block == Blx.air || this.Block == null) && this.Metadata == 0;
	}

	// #endregion

	// #region Constructors

	public BlockData(Block block)
	{
		this(block, 0);
	}

	public BlockData(Block block, int metadata)
	{
		this(block, FixMetadata(metadata));
	}

	public BlockData(Block block, byte metadata)
	{
		if (block == null)
		{
			block = Blx.air;
		}

		this.Block = block;
		this.Metadata = FixMetadata(metadata);
	}

	// #endregion

	// #region Methods

	public BlockData setBlock(Block block)
	{
		return new BlockData(block, this.Metadata);
	}

	public BlockData setMetadata(int metadata)
	{
		return new BlockData(this.Block, metadata);
	}

	// #endregion

	// #region ToString Support

	@Override
	public String toString()
	{
		return toString(this.Block, this.Metadata);
	}

	public static String toString(Block block, int metadata)
	{
		String ret = null;

		try
		{
			ret = net.minecraft.block.Block.blockRegistry.getNameForObject(block);
		}
		catch (Exception ignore)
		{ /* do nothing */}

		if (ret == null || ret.length() < 1)
		{
			ret = Integer.toString(net.minecraft.block.Block.getIdFromBlock(block));
		}

		if (ret != null && ret.toLowerCase().startsWith("minecraft:"))
		{
			ret = ret.substring(10);
		}

		metadata = FixMetadata(metadata);

		if (metadata != 0)
		{
			ret += ":" + metadata;
		}

		return ret;
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}

	// #endregion

	// #region Parse Support

	public static BlockData Parse(String value)
	{
		return Parse(value, BlockData.Empty);
	}

	public static BlockData Parse(String value, BlockData fallbackBlock)
	{
		Block block = net.minecraft.block.Block.getBlockFromName(value);

		if (block != null)
		{
			// Block was parsable, it does not contain metadata.
			return new BlockData(block, 0);
		}
		else
		{
			int sIndex = value.lastIndexOf(':');
			if (sIndex >= 0)
			{
				try
				{
					String blockString = value.substring(0, sIndex);
					String metaString = value.substring(sIndex + 1);

					block = net.minecraft.block.Block.getBlockFromName(blockString);
					int metadata = Integer.parseInt(metaString);

					if (block != null)
					{
						return new BlockData(block, metadata);
					}
				}
				catch (Throwable ignore)
				{ /* do nothing */ }
			}

			Utils.Assert(false, "Unable to parse BlockData: " + value, 1);
			return fallbackBlock;
		}
	}

	// #endregion
}
