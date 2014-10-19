package newBiospheresMod;

import net.minecraft.block.Block;
import net.minecraft.util.WeightedRandom;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.Utils;

public class BlockEntry extends WeightedRandom.Item
{
	public final Block Block;

	public BlockEntry(Block block, int i)
	{
		super(i);
		this.Block = block;
	}

	@Override
	public String toString()
	{
		return Utils.GetNameOrIdForBlock(Block) + ", " + this.itemWeight;
	}

	public static BlockEntry Parse(String input)
	{
		if (input != null && input.length() > 0)
		{
			try
			{
				int idx = input.lastIndexOf(",");

				String blockName = null;
				String weight = null;

				if (idx > 0)
				{
					blockName = input.substring(0, idx).trim();
					weight = input.substring(idx + 1).trim();
				}
				else
				{
					blockName = input;
				}

				if (blockName == null || blockName.length() < 1)
				{
					blockName = "air";
				}

				if (weight == null || weight.length() < 1)
				{
					weight = "10";
				}

				int iWeight = Integer.parseInt(weight);
				if (iWeight < 0)
				{
					iWeight = 0;
				}

				return new BlockEntry(Utils.ParseBlock(blockName), iWeight);
			}
			catch (Throwable ignore)
			{
				// do nothing
			}
		}

		return new BlockEntry(Blx.air, 0);
	}
}
