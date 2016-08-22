package newBiospheresMod.Generators;

import java.util.Random;

import org.apache.logging.log4j.core.appender.RandomAccessFileAppender;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;;

public class WorldGenNetherTower extends WorldGenerator {

	private static final int ROOM_RADIUS = 4;
	private static final int Y_OFFSET = 8;
	private static final int FLOOR_HEIGHT = 1;
	private static final int ROOM_HEIGHT = 4;
	private static final int WART_CUBE_RADIUS = 2;

	final WeightedRandomChestContent[] lootTable = new WeightedRandomChestContent[] {
			new WeightedRandomChestContent(Items.diamond, 0, 1, 3, 5),
			new WeightedRandomChestContent(Items.iron_ingot, 0, 1, 5, 5),
			new WeightedRandomChestContent(Items.gold_ingot, 0, 1, 3, 15),
			new WeightedRandomChestContent(Items.golden_sword, 0, 1, 1, 5),
			new WeightedRandomChestContent(Items.golden_chestplate, 0, 1, 1, 5),
			new WeightedRandomChestContent(Items.flint_and_steel, 0, 1, 1, 5),
			new WeightedRandomChestContent(Items.nether_wart, 0, 3, 7, 5),
			new WeightedRandomChestContent(Items.saddle, 0, 1, 1, 10),
			new WeightedRandomChestContent(Items.golden_horse_armor, 0, 1, 1, 8),
			new WeightedRandomChestContent(Items.iron_horse_armor, 0, 1, 1, 5),
			new WeightedRandomChestContent(Items.diamond_horse_armor, 0, 1, 1, 3) };

	static Random rand;
	static int RoomNumber;

	public WorldGenNetherTower(Random rand) {
		this.rand = rand;
	}

	/**
	 * IMPORTANT: Has to be called before ANY other Generators taking place in
	 * the Netherrack, or it will glitch out.
	 */
	public boolean generate(World world, Random rand, final int centerX, final int rawCenterY, final int centerZ) {

		final int centerY = rawCenterY + Y_OFFSET;

		if (world.getBlock(centerX, centerY, centerZ) == Blocks.nether_brick)
		{
			return false;
		}

		setUpShape(world, centerX, centerY, centerZ);
		world.setBlock(centerX, centerY + 1, centerZ, Blocks.mob_spawner, 0, 2);
		setSpawnerTo(world, "Ghast", centerX, centerY + 1, centerZ);

		int nodeY;
		for (nodeY = centerY - (FLOOR_HEIGHT + ROOM_HEIGHT); Room.haveSpace(world, centerX, nodeY,
				centerZ); nodeY -= (FLOOR_HEIGHT + ROOM_HEIGHT))
		{
			Room.excavateSpace(world, centerX, nodeY, centerZ);

			placeFenceWindows(world, centerX, nodeY, centerZ);
			world.setBlock(centerX, nodeY + 1, centerZ, Blocks.mob_spawner, 0, 2);
			setSpawnerTo(world, "Blaze", centerX, nodeY + 1, centerZ);
			placeSideFeatures(world, centerX, nodeY, centerZ);

			RoomNumber++;
		}

		nodeY += (FLOOR_HEIGHT + ROOM_HEIGHT);

		setSpawnerTo(world, "WitherSkeleton", centerX, nodeY + 1, centerZ);

		return true;
	}

	private void setUpShape(World world, int centerX, int centerY, int centerZ) {
		int nodeX;
		int nodeZ;

		for (nodeX = centerX - ROOM_RADIUS; nodeX <= centerX + ROOM_RADIUS; ++nodeX)
		{
			for (nodeZ = centerZ - ROOM_RADIUS; nodeZ <= centerZ + ROOM_RADIUS; ++nodeZ)
			{
				if (nodeX == centerX + ROOM_RADIUS || nodeX == centerX - ROOM_RADIUS || nodeZ == centerZ + ROOM_RADIUS
						|| nodeZ == centerZ - ROOM_RADIUS)
				{
					if ((nodeX + nodeZ) % 2 != 0)
					{
						stackToBottom(world, nodeX, centerY + 1, nodeZ);
					}
					else
					{
						world.setBlock(nodeX, centerY + 1, nodeZ, Blocks.nether_brick_fence);
						stackToBottom(world, nodeX, centerY, nodeZ);
					}
				}
				else
				{
					stackToBottom(world, nodeX, centerY, nodeZ);
				}
			}
		}
	}

	private void stackToBottom(World world, final int x, final int y, final int z) {
		int nodeY = y;

		while (!world.getBlock(x, nodeY, z).getMaterial().isSolid() || world.getBlock(x, nodeY, z) == Blocks.netherrack)
		{
			world.setBlock(x, nodeY, z, Blocks.nether_brick);
			nodeY--;
		}
	}

	private void placeSideFeatures(World world, final int centerX, final int floorY, final int centerZ) {
		if (rand.nextInt(2) == 0)
		{
			if (rand.nextBoolean() && WartCube.haveSpace(world, centerX - ROOM_RADIUS - WART_CUBE_RADIUS,
					floorY + WART_CUBE_RADIUS, centerZ))
			{
				WartCube.create(world, centerX - ROOM_RADIUS - WART_CUBE_RADIUS, floorY + WART_CUBE_RADIUS, centerZ);
			}
			else
			{
				placeChest(world, centerX - ROOM_RADIUS + 1, floorY + 1, centerZ);
			}
		}
		if (rand.nextInt(2) == 0)
		{
			if (rand.nextBoolean() && WartCube.haveSpace(world, centerX + ROOM_RADIUS + WART_CUBE_RADIUS,
					floorY + WART_CUBE_RADIUS, centerZ))
			{
				WartCube.create(world, centerX + ROOM_RADIUS + WART_CUBE_RADIUS, floorY + WART_CUBE_RADIUS, centerZ);
			}
			else
			{
				placeChest(world, centerX + ROOM_RADIUS - 1, floorY + 1, centerZ);
			}
		}
		if (rand.nextInt(2) == 0)
		{
			if (rand.nextBoolean() && WartCube.haveSpace(world, centerX, floorY + WART_CUBE_RADIUS,
					centerZ - ROOM_RADIUS - WART_CUBE_RADIUS))
			{
				WartCube.create(world, centerX, floorY + WART_CUBE_RADIUS, centerZ - ROOM_RADIUS - WART_CUBE_RADIUS);
			}
			else
			{
				placeChest(world, centerX, floorY + 1, centerZ - ROOM_RADIUS + 1);
			}

		}
		if (rand.nextInt(2) == 0)
		{
			if (rand.nextBoolean() && WartCube.haveSpace(world, centerX, floorY + WART_CUBE_RADIUS,
					centerZ + ROOM_RADIUS + WART_CUBE_RADIUS))
			{
				WartCube.create(world, centerX, floorY + WART_CUBE_RADIUS, centerZ + ROOM_RADIUS + WART_CUBE_RADIUS);
			}
			else
			{
				placeChest(world, centerX, floorY + 1, centerZ + ROOM_RADIUS - 1);
			}
		}
	}

	private boolean setSpawnerTo(World world, String mob, final int x, final int y, final int z) {
		TileEntityMobSpawner tileentitymobspawner = (TileEntityMobSpawner) world.getTileEntity(x, y, z);
		if (tileentitymobspawner != null)
		{
			if (mob.equals("WitherSkeleton"))
			{
				tileentitymobspawner.func_145881_a().setEntityName("Skeleton");

				EntitySkeleton skeleton = new EntitySkeleton(world);
				skeleton.setSkeletonType(1);
				skeleton.setCurrentItemOrArmor(0, new ItemStack(Items.stone_sword));
				skeleton.getEntityAttribute(SharedMonsterAttributes.attackDamage).setBaseValue(4.0D);

				NBTTagCompound skeletonNBT = new NBTTagCompound();
				skeleton.writeEntityToNBT(skeletonNBT);

				NBTTagCompound spawnDataNBT = new NBTTagCompound();
				tileentitymobspawner.func_145881_a().writeToNBT(spawnDataNBT);
				spawnDataNBT.setTag("SpawnData", skeletonNBT);

				tileentitymobspawner.func_145881_a().readFromNBT(spawnDataNBT);
			}
			else
			{
				tileentitymobspawner.func_145881_a().setEntityName(mob);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	private void placeChest(World world, final int x, final int y, final int z) {

		world.setBlock(x, y, z, Blocks.chest, 0, 2);
		TileEntityChest tileentitychest = (TileEntityChest) world.getTileEntity(x, y, z);

		if (tileentitychest != null)
		{
			WeightedRandomChestContent.generateChestContents(rand, lootTable, tileentitychest,
					4 + rand.nextInt(RoomNumber * 3 + 1));
		}
	}

	private void placeFenceWindows(World world, final int centerX, final int floorY, final int centerZ) {
		int nodeX;
		int nodeY;
		int nodeZ;

		for (nodeX = centerX - ROOM_RADIUS; nodeX <= centerX + ROOM_RADIUS; ++nodeX)
		{
			for (nodeZ = centerZ - ROOM_RADIUS; nodeZ <= centerZ + ROOM_RADIUS; ++nodeZ)
			{
				for (nodeY = floorY + 2; nodeY <= floorY + ROOM_HEIGHT - 1; ++nodeY)
				{
					if (nodeX == centerX + ROOM_RADIUS || nodeX == centerX - ROOM_RADIUS
							|| nodeZ == centerZ + ROOM_RADIUS || nodeZ == centerZ - ROOM_RADIUS)
					{
						if ((nodeX + nodeZ) % 2 != 0 && getNeighbourAirBlockCount(world, nodeX, nodeY, nodeZ) == 2)
						{
							world.setBlock(nodeX, nodeY, nodeZ, Blocks.nether_brick_fence);
						}
					}
				}
			}
		}
	}

	private int getNeighbourAirBlockCount(World world, final int x, final int y, final int z) {
		int air = 0;

		if (world.getBlock(x + 1, y, z) == Blocks.air)
		{
			air++;
		}
		if (world.getBlock(x - 1, y, z) == Blocks.air)
		{
			air++;
		}
		if (world.getBlock(x, y + 1, z) == Blocks.air)
		{
			air++;
		}
		if (world.getBlock(x, y - 1, z) == Blocks.air)
		{
			air++;
		}
		if (world.getBlock(x, y, z + 1) == Blocks.air)
		{
			air++;
		}
		if (world.getBlock(x, y, z - 1) == Blocks.air)
		{
			air++;
		}

		return air;

	}

	private void plantNetherWarts(World world, final int centerX, final int floorY, final int centerZ) {
		int nodeX;
		int nodeZ;

		for (nodeX = centerX - ROOM_RADIUS + 1; nodeX <= centerX + ROOM_RADIUS - 1; ++nodeX)
		{
			for (nodeZ = centerZ - ROOM_RADIUS + 1; nodeZ <= centerZ + ROOM_RADIUS - 1; ++nodeZ)
			{
				if (nodeX != centerX && nodeZ != centerZ)
				{
					if (world.getBlock(nodeX, floorY, nodeZ) == Blocks.nether_brick)
					{
						world.setBlock(nodeX, floorY, nodeZ, Blocks.soul_sand);
						world.setBlock(nodeX, floorY + 1, nodeZ, Blocks.nether_wart, rand.nextInt(4), 3);
					}
				}
			}
		}
	}

	private static class Room {

		private static boolean haveSpace(World world, final int centerX, final int floorY, final int centerZ) {
			int nodeX;
			int nodeY;
			int nodeZ;

			for (nodeX = centerX - ROOM_RADIUS; nodeX <= centerX + ROOM_RADIUS; ++nodeX)
			{
				for (nodeZ = centerZ - ROOM_RADIUS; nodeZ <= centerZ + ROOM_RADIUS; ++nodeZ)
				{
					for (nodeY = floorY; nodeY <= floorY + (ROOM_HEIGHT + FLOOR_HEIGHT); ++nodeY)
					{
						if (world.getBlock(nodeX, nodeY, nodeZ) != Blocks.nether_brick)
						{
							return false;
						}
					}
				}
			}
			return true;
		}

		private static void excavateSpace(World world, final int centerX, final int floorY, final int centerZ) {
			int nodeX;
			int nodeY;
			int nodeZ;

			for (nodeX = centerX - ROOM_RADIUS + 1; nodeX <= centerX + ROOM_RADIUS - 1; ++nodeX)
			{
				for (nodeZ = centerZ - ROOM_RADIUS + 1; nodeZ <= centerZ + ROOM_RADIUS - 1; ++nodeZ)
				{
					for (nodeY = floorY + 1; nodeY <= floorY + ROOM_HEIGHT; ++nodeY)
					{
						world.setBlockToAir(nodeX, nodeY, nodeZ);
					}
				}
			}
		}

	}

	private static class WartCube {
		private static boolean haveSpace(World world, final int centerX, final int centerY, final int centerZ) {
			int nodeX;
			int nodeY;
			int nodeZ;

			for (nodeX = centerX - WART_CUBE_RADIUS; nodeX <= centerX + WART_CUBE_RADIUS; ++nodeX)
			{
				for (nodeY = centerY - WART_CUBE_RADIUS; nodeY <= centerY + WART_CUBE_RADIUS; ++nodeY)
				{
					for (nodeZ = centerZ - WART_CUBE_RADIUS; nodeZ <= centerZ + WART_CUBE_RADIUS; ++nodeZ)
					{
						if (world.getBlock(nodeX, nodeY, nodeZ) != Blocks.netherrack
								&& world.getBlock(nodeX, nodeY, nodeZ) != Blocks.nether_brick)
						{
							return false;
						}
					}
				}
			}
			return true;
		}

		private static void create(World world, final int centerX, final int centerY, final int centerZ) {
			int nodeX;
			int nodeY;
			int nodeZ;

			for (nodeX = centerX - WART_CUBE_RADIUS; nodeX <= centerX + WART_CUBE_RADIUS; ++nodeX)
			{
				for (nodeY = centerY - WART_CUBE_RADIUS; nodeY <= centerY + WART_CUBE_RADIUS; ++nodeY)
				{
					for (nodeZ = centerZ - WART_CUBE_RADIUS; nodeZ <= centerZ + WART_CUBE_RADIUS; ++nodeZ)
					{
						if (nodeX == centerX - WART_CUBE_RADIUS || nodeX == centerX + WART_CUBE_RADIUS
								|| nodeZ == centerZ - WART_CUBE_RADIUS || nodeZ == centerZ + WART_CUBE_RADIUS
								|| nodeY == centerY + WART_CUBE_RADIUS)
						{
							boolean a = (nodeX == centerX - WART_CUBE_RADIUS || nodeX == centerX + WART_CUBE_RADIUS);
							boolean b = (nodeY == centerY - WART_CUBE_RADIUS || nodeY == centerY + WART_CUBE_RADIUS);
							boolean c = (nodeZ == centerZ - WART_CUBE_RADIUS || nodeZ == centerZ + WART_CUBE_RADIUS);
							boolean onlyOne = (a != b) != c && !(a && b && c);

							if (world.getBlock(nodeX, nodeY, nodeZ) == Blocks.nether_brick && onlyOne)
							{
								if (nodeY == centerY + WART_CUBE_RADIUS - 1)
								{
									world.setBlock(nodeX, nodeY, nodeZ, Blocks.nether_brick_fence);
								}
								else
								{
									world.setBlockToAir(nodeX, nodeY, nodeZ);
								}
							}
							else
							{
								world.setBlock(nodeX, nodeY, nodeZ, Blocks.nether_brick);
							}
						}
						else if (nodeY == centerY - WART_CUBE_RADIUS)
						{
							world.setBlock(nodeX, nodeY, nodeZ, Blocks.soul_sand);
						}
						else if (nodeY == centerY - WART_CUBE_RADIUS + 1)
						{
							world.setBlock(nodeX, nodeY, nodeZ, Blocks.nether_wart, rand.nextInt(3), 3);
						}
						else
						{
							world.setBlockToAir(nodeX, nodeY, nodeZ);
						}
					}
				}
			}
		}
	}

}
