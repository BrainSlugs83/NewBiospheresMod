package newBiospheresMod.Generators;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenGlowStone extends WorldGenerator {

	public boolean generate(World world, Random rand, int posX, int posY, int posZ) {
		if (!world.isAirBlock(posX, posY, posZ))
		{
			return false;
		}
		else
		{
			world.setBlock(posX, posY, posZ, Blocks.glowstone, 0, 2);

			for (int l = 0; l < 1500; ++l)
			{
				int i1 = posX + rand.nextInt(8) - rand.nextInt(8);
				int j1 = posY - rand.nextInt(12);
				int k1 = posZ + rand.nextInt(8) - rand.nextInt(8);

				if (world.getBlock(i1, j1, k1).getMaterial() == Material.air)
				{
					int l1 = 0;

					for (int i2 = 0; i2 < 6; ++i2)
					{
						Block block = null;

						if (i2 == 0)
						{
							block = world.getBlock(i1 - 1, j1, k1);
						}

						if (i2 == 1)
						{
							block = world.getBlock(i1 + 1, j1, k1);
						}

						if (i2 == 2)
						{
							block = world.getBlock(i1, j1 - 1, k1);
						}

						if (i2 == 3)
						{
							block = world.getBlock(i1, j1 + 1, k1);
						}

						if (i2 == 4)
						{
							block = world.getBlock(i1, j1, k1 - 1);
						}

						if (i2 == 5)
						{
							block = world.getBlock(i1, j1, k1 + 1);
						}

						if (block == Blocks.glowstone)
						{
							++l1;
						}
					}

					if (l1 == 1)
					{
						world.setBlock(i1, j1, k1, Blocks.glowstone, 0, 2);
					}
				}
			}

			return true;
		}
	}
}