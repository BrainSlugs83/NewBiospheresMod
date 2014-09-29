package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class WorldGenWaterlily extends WorldGenerator
{
    private static final String __OBFID = "CL_00000189";

    public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_)
    {
        for (int l = 0; l < 10; ++l)
        {
            int i1 = p_76484_3_ + p_76484_2_.nextInt(8) - p_76484_2_.nextInt(8);
            int j1 = p_76484_4_ + p_76484_2_.nextInt(4) - p_76484_2_.nextInt(4);
            int k1 = p_76484_5_ + p_76484_2_.nextInt(8) - p_76484_2_.nextInt(8);

            if (p_76484_1_.isAirBlock(i1, j1, k1) && Blocks.waterlily.canPlaceBlockAt(p_76484_1_, i1, j1, k1))
            {
                p_76484_1_.setBlock(i1, j1, k1, Blocks.waterlily, 0, 2);
            }
        }

        return true;
    }
}