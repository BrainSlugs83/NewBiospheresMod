package newBiospheresMod.Generators;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

public class WorldGenLakebed extends WorldGenerator
{
    private Block block;
    /** The number of blocks to generate. */
    private int numberOfBlocks;
    private static final String __OBFID = "CL_00000405";

    public WorldGenLakebed(Block block, int veinSize)
    {
        this.block = block;
        this.numberOfBlocks = veinSize;
    }

    public boolean generate(World world, Random rand, int x, int y, int z)
    {
        if (world.getBlock(x, y, z).getMaterial() != Material.water)
        {
            return false;
        }
        else
        {
            int localVeinSize = rand.nextInt(this.numberOfBlocks - 2) + 2;
            byte rad = 1;

            for (int i1 = x - localVeinSize; i1 <= x + localVeinSize; ++i1)
            {
                for (int j1 = z - localVeinSize; j1 <= z + localVeinSize; ++j1)
                {
                    int k1 = i1 - x;
                    int l1 = j1 - z;

                    if (k1 * k1 + l1 * l1 <= localVeinSize * localVeinSize)
                    {
                        for (int i2 = y - rad; i2 <= y + rad; ++i2)
                        {
                            Block block = world.getBlock(i1, i2, j1);

                            if (block == Blocks.sand || block == this.block)
                            {
                                world.setBlock(i1, i2, j1, this.block, 0, 2);
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}