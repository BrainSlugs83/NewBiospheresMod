package newBiospheresMod.Generators;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.DungeonHooks;
import static net.minecraftforge.common.ChestGenHooks.DUNGEON_CHEST;;

public class WorldGenDungeon extends WorldGenerator
{
    public static final WeightedRandomChestContent[] dungeonChestContent = new WeightedRandomChestContent[] {new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 10), new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bread, 0, 1, 1, 10), new WeightedRandomChestContent(Items.wheat, 0, 1, 4, 10), new WeightedRandomChestContent(Items.gunpowder, 0, 1, 4, 10), new WeightedRandomChestContent(Items.string, 0, 1, 4, 10), new WeightedRandomChestContent(Items.bucket, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_apple, 0, 1, 1, 1), new WeightedRandomChestContent(Items.redstone, 0, 1, 4, 10), new WeightedRandomChestContent(Items.record_13, 0, 1, 1, 10), new WeightedRandomChestContent(Items.record_cat, 0, 1, 1, 10), new WeightedRandomChestContent(Items.name_tag, 0, 1, 1, 10), new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 2), new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 5), new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 1)};
    private static final String __OBFID = "CL_00000425";

    public boolean generate(World world, Random rand, int centerX, int centerY, int centerZ)
    {
        int radiusX = rand.nextInt(2) + 2;
        int radiusY = 3;
        int radiusZ = rand.nextInt(2) + 2;
        int nodeX;
        int nodeY;
        int nodeZ;

        int airBlocks = 0;
        
        for (nodeX = centerX - radiusX - 1; nodeX <= centerX + radiusX + 1; ++nodeX)
        {
            for (nodeY = centerY - 1; nodeY <= centerY + radiusY + 1; ++nodeY)
            {
                for (nodeZ = centerZ - radiusZ - 1; nodeZ <= centerZ + radiusZ + 1; ++nodeZ)
                {
                    Material material = world.getBlock(nodeX, nodeY, nodeZ).getMaterial();

                    if (nodeY == centerY - 1 && !material.isSolid())
                    {
                        return false;
                    }

                    if (nodeY == centerY + radiusY + 1 && !material.isSolid())
                    {
                        return false;
                    }

                    if ((nodeX == centerX - radiusX - 1 || nodeX == centerX + radiusX + 1 || nodeZ == centerZ - radiusZ - 1 || nodeZ == centerZ + radiusZ + 1) && world.isAirBlock(nodeX, nodeY, nodeZ))
                    {
                        ++airBlocks;
                    }
                }
            }
        }

        if (airBlocks == 0)
        {
            for (nodeX = centerX - radiusX - 1; nodeX <= centerX + radiusX + 1; ++nodeX)
            {
                for (nodeY = centerY + radiusY; nodeY >= centerY - 1; --nodeY)
                {
                    for (nodeZ = centerZ - radiusZ - 1; nodeZ <= centerZ + radiusZ + 1; ++nodeZ)
                    {
                        if (nodeX != centerX - radiusX - 1 && nodeY != centerY - 1 && nodeZ != centerZ - radiusZ - 1 && nodeX != centerX + radiusX + 1 && nodeY != centerY + radiusY + 1 && nodeZ != centerZ + radiusZ + 1)
                        {
                            world.setBlockToAir(nodeX, nodeY, nodeZ);
                        }
                        else if (nodeY >= 0 && !world.getBlock(nodeX, nodeY - 1, nodeZ).getMaterial().isSolid())
                        {
                            world.setBlockToAir(nodeX, nodeY, nodeZ);
                        }
                        else if (world.getBlock(nodeX, nodeY, nodeZ).getMaterial().isSolid())
                        {
                            if (nodeY == centerY - 1 && rand.nextInt(4) != 0)
                            {
                                world.setBlock(nodeX, nodeY, nodeZ, Blocks.mossy_cobblestone, 0, 2);
                            }
                            else
                            {
                                world.setBlock(nodeX, nodeY, nodeZ, Blocks.cobblestone, 0, 2);
                            }
                        }
                    }
                }
            }

            nodeX = 0;

            while (nodeX < 2)
            {
                nodeY = 0;

                while (true)
                {
                    if (nodeY < 3)
                    {
                        label197:
                        {
                            nodeZ = centerX + rand.nextInt(radiusX * 2 + 1) - radiusX;
                            int j2 = centerZ + rand.nextInt(radiusZ * 2 + 1) - radiusZ;

                            if (world.isAirBlock(nodeZ, centerY, j2))
                            {
                                int k2 = 0;

                                if (world.getBlock(nodeZ - 1, centerY, j2).getMaterial().isSolid())
                                {
                                    ++k2;
                                }

                                if (world.getBlock(nodeZ + 1, centerY, j2).getMaterial().isSolid())
                                {
                                    ++k2;
                                }

                                if (world.getBlock(nodeZ, centerY, j2 - 1).getMaterial().isSolid())
                                {
                                    ++k2;
                                }

                                if (world.getBlock(nodeZ, centerY, j2 + 1).getMaterial().isSolid())
                                {
                                    ++k2;
                                }

                                if (k2 == 1)
                                {
                                    world.setBlock(nodeZ, centerY, j2, Blocks.chest, 0, 2);
                                    TileEntityChest tileentitychest = (TileEntityChest)world.getTileEntity(nodeZ, centerY, j2);

                                    if (tileentitychest != null)
                                    {
                                        WeightedRandomChestContent.generateChestContents(rand, ChestGenHooks.getItems(DUNGEON_CHEST, rand), tileentitychest, ChestGenHooks.getCount(DUNGEON_CHEST, rand));
                                    }

                                    break label197;
                                }
                            }

                            ++nodeY;
                            continue;
                        }
                    }

                    ++nodeX;
                    break;
                }
            }

            world.setBlock(centerX, centerY, centerZ, Blocks.mob_spawner, 0, 2);
            TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner)world.getTileEntity(centerX, centerY, centerZ);

            if (tileentitymobspawner != null)
            {
                tileentitymobspawner.func_145881_a().setEntityName(this.pickMobSpawner(rand));
            }
            else
            {
                System.err.println("Failed to fetch mob spawner entity at (" + centerX + ", " + centerY + ", " + centerZ + ")");
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Randomly decides which spawner to use in a dungeon
     */
    private String pickMobSpawner(Random p_76543_1_)
    {
        return DungeonHooks.getRandomDungeonMob(p_76543_1_);
    }
}