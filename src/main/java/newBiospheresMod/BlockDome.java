/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.creativetab.*;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import net.minecraftforge.common.*;
import net.minecraftforge.common.util.*;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;
import cpw.mods.fml.common.registry.*;
import cpw.mods.fml.relauncher.*;

public final class BlockDome extends Block
{
	// #region Static Methods and Fields

	private static final ConcurrentHashMap<Block, BlockDome> InitializedBlocks = new ConcurrentHashMap<Block, BlockDome>();

	public static void InitalizeAllRegisteredBlocks()
	{
		System.out.println("InitalizeAllRegisteredBlocks Entered.");

		int idx = 0;
		int failCount = 0;
		while (true)
		{
			Block block = null;
			try
			{
				System.out.println("InitalizeAllRegisteredBlocks: Copying Block #" + idx);
				block = Block.getBlockById(idx++);
			}
			catch (Exception ex)
			{ /* do nothing */}

			if (block == null || block == Blx.air)
			{
				failCount++;
			}
			else
			{
				GetDomeBlock(new BlockData(block));
				failCount = 0;
			}

			if (failCount >= 10)
			{
				break;
			}
		}

		System.out.println("InitalizeAllRegisteredBlocks Exited.");
	}

	public static BlockData GetDomeBlock(BlockData baseBlock)
	{
		if (BlockData.IsNullOrEmpty(baseBlock)) { return BlockData.Empty; }
		if (baseBlock.Block instanceof BlockDome) { return baseBlock; }

		try
		{
			BlockDome ret = InitializedBlocks.get(baseBlock.Block);
			if (ret == null)
			{
				ret = InitializedBlocks.putIfAbsent
				(
					baseBlock.Block,
					new BlockDome(baseBlock.Block)
				);

				if (ret == null)
				{
					ret = InitializedBlocks.get(baseBlock);
				}

				ret.InitBlock();
			}

			return baseBlock.setBlock(ret);
		}
		catch (Exception e)
		{
			return baseBlock;
		}
	}

	private static String getRegularBlockName(Block block)
	{
		String name = block.getUnlocalizedName();
		if (name.toLowerCase().startsWith("tile."))
		{
			name = name.substring(5);
		}

		return name;
	}

	// #endregion

	// #region Instance Data

	private final Block baseBlock;
	private boolean blockInitialized = false;
	private Object lock = new Object();

	// #endregion

	// #region Constructor

	private BlockDome(Block baseBlock)
	{
		super(baseBlock.getMaterial());
		this.baseBlock = baseBlock;
	}

	private void InitBlock()
	{
		synchronized (lock)
		{
			if (blockInitialized) { return; }
			blockInitialized = true;
		}

		// Clone as much of BaseBlock as possible:
		this.slipperiness = baseBlock.slipperiness;
		this.opaque = false;
		this.canBlockGrass = this.getCanBlockGrass();

		this.useNeighborBrightness = this.getUseNeighborBrightness();

		this.needsRandomTick = this.getTickRandomly();
		this.lightOpacity = this.getLightOpacity();
		this.stepSound = baseBlock.stepSound;
		this.lightValue = this.getLightValue();

		this.enableStats = baseBlock.getEnableStats();

		this.setBlockName(getRegularBlockName(baseBlock) + "Dome");
		// setUnlocalizedName(getRegularBlockName(BaseBlock) + "Dome");

		synchronized(NewBiospheresMod.biosphereWorldType)
		{
			String baseName = getRegularBlockName(baseBlock) + "_dome";
			String uniqueName = baseName;
			int num = 2;

			while (GameRegistry.findBlock(ModConsts.ModId, uniqueName) != null)
			{
				uniqueName = baseName + num;
				num++;
			}

			GameRegistry.registerBlock
			(
				this,
				uniqueName
			);
		}
	}

	// #endregion

	// #region Modified Behavior

	@Override
	public boolean isFoliage(IBlockAccess world, int x, int y, int z)
	{
		// must return true, to prevent spawning on top of dome.
		return true;
	}

	@Override
	public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata)
	{
		// Prevent silk touch from harvesting this block.
		return false;
	}

	@Override
	public int getLightOpacity(IBlockAccess world, int x, int y, int z)
	{
		return 0; // let light pass through?
	}

	@Override
	public boolean isAssociatedBlock(Block p_149667_1_)
	{
		if (this == p_149667_1_) { return true; }
		if (baseBlock != null) { return baseBlock.isAssociatedBlock(p_149667_1_); }

		return false;
	}

	@Override
	public void beginLeavesDecay(World world, int x, int y, int z)
	{
		// ignore this event
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return false; }

		// TODO: Might want to return false here, not sure, need to investigate first.
		return baseBlock.canCreatureSpawn(type, world, x, y, z);
	}

	@Override
	public boolean isLeaves(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target)
	{
		return false;
	}

	// #endregion

	// #region General Chameleon Methods

	@Override
	public boolean func_149730_j()
	{
		if (baseBlock == null) { return this.opaque; }
		return baseBlock.func_149730_j();
	}

	@Override
	public int getLightOpacity()
	{
		if (baseBlock == null) { return this.lightOpacity; }
		return baseBlock.getLightOpacity();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean getCanBlockGrass()
	{
		if (baseBlock == null) { return this.canBlockGrass; }
		return baseBlock.getCanBlockGrass();
	}

	@Override
	public int getLightValue()
	{
		if (baseBlock == null) { return this.lightValue; }
		return baseBlock.getLightValue();
	}

	@Override
	public boolean getUseNeighborBrightness()
	{
		if (baseBlock == null) { return this.useNeighborBrightness; }
		return baseBlock.getUseNeighborBrightness();
	}

	@Override
	public Material getMaterial()
	{
		if (baseBlock == null) { return super.getMaterial(); }
		return baseBlock.getMaterial();
	}

	@Override
	public MapColor getMapColor(int p_149728_1_)
	{
		if (baseBlock == null) { return super.getMapColor(p_149728_1_); }
		return baseBlock.getMapColor(p_149728_1_);
	}

	@Override
	public Block setStepSound(Block.SoundType p_149672_1_)
	{
		if (baseBlock == null) { return super.setStepSound(p_149672_1_); }
		return baseBlock.setStepSound(p_149672_1_);
	}

	@Override
	public Block setLightOpacity(int p_149713_1_)
	{
		if (baseBlock == null) { return super.setLightOpacity(p_149713_1_); }
		return baseBlock.setLightOpacity(p_149713_1_);
	}

	@Override
	public Block setLightLevel(float p_149715_1_)
	{
		if (baseBlock == null) { return super.setLightLevel(p_149715_1_); }
		return baseBlock.setLightLevel(p_149715_1_);
	}

	@Override
	public Block setResistance(float p_149752_1_)
	{
		if (baseBlock == null) { return super.setResistance(p_149752_1_); }
		return baseBlock.setResistance(p_149752_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isBlockNormalCube()
	{
		if (baseBlock == null) { return super.isBlockNormalCube(); }
		return baseBlock.isBlockNormalCube();
	}

	@Override
	public boolean isNormalCube()
	{
		if (baseBlock == null) { return super.isNormalCube(); }
		return baseBlock.isNormalCube();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		if (baseBlock == null) { return super.renderAsNormalBlock(); }
		return baseBlock.renderAsNormalBlock();
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess p_149655_1_, int p_149655_2_, int p_149655_3_, int p_149655_4_)
	{
		if (baseBlock == null) { return super.getBlocksMovement(p_149655_1_, p_149655_2_, p_149655_3_, p_149655_4_); }
		return baseBlock.getBlocksMovement(p_149655_1_, p_149655_2_, p_149655_3_, p_149655_4_);
	}

	@Override
	public int getRenderType()
	{
		if (baseBlock == null) { return super.getRenderType(); }
		return baseBlock.getRenderType();
	}

	@Override
	public Block setHardness(float p_149711_1_)
	{
		if (baseBlock == null) { return super.setHardness(p_149711_1_); }
		return baseBlock.setHardness(p_149711_1_);
	}

	@Override
	public Block setBlockUnbreakable()
	{
		if (baseBlock == null) { return super.setBlockUnbreakable(); }
		return baseBlock.setBlockUnbreakable();
	}

	@Override
	public float getBlockHardness(World p_149712_1_, int p_149712_2_, int p_149712_3_, int p_149712_4_)
	{
		if (baseBlock == null) { return super.getBlockHardness(p_149712_1_, p_149712_2_, p_149712_3_, p_149712_4_); }
		return baseBlock.getBlockHardness(p_149712_1_, p_149712_2_, p_149712_3_, p_149712_4_);
	}

	@Override
	public Block setTickRandomly(boolean p_149675_1_)
	{
		if (baseBlock == null) { return super.setTickRandomly(p_149675_1_); }
		return baseBlock.setTickRandomly(p_149675_1_);
	}

	@Override
	public boolean getTickRandomly()
	{
		if (baseBlock == null) { return super.getTickRandomly(); }
		return baseBlock.getTickRandomly();
	}

	@Override
	@Deprecated
	// Forge: New Metadata sensitive version.
	public boolean hasTileEntity()
	{
		if (baseBlock == null) { return super.hasTileEntity(); }
		return baseBlock.hasTileEntity();
	}

	@Override
	public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getMixedBrightnessForBlock(world, x, y, z); }
		return baseBlock.getMixedBrightnessForBlock(world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		if (baseBlock == null) { return super.shouldSideBeRendered(world, x, y, z, side); }
		return baseBlock.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public boolean isBlockSolid(IBlockAccess p_149747_1_, int p_149747_2_, int p_149747_3_, int p_149747_4_,
		int p_149747_5_)
	{
		if (baseBlock == null) { return super.isBlockSolid(p_149747_1_, p_149747_2_, p_149747_3_, p_149747_4_,
			p_149747_5_); }
		return baseBlock.isBlockSolid(p_149747_1_, p_149747_2_, p_149747_3_, p_149747_4_, p_149747_5_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess p_149673_1_, int p_149673_2_, int p_149673_3_, int p_149673_4_, int p_149673_5_)
	{
		if (baseBlock == null) { return super.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_, p_149673_5_); }
		return baseBlock.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_, p_149673_5_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_)
	{
		if (baseBlock == null) { return super.getIcon(p_149691_1_, p_149691_2_); }
		return baseBlock.getIcon(p_149691_1_, p_149691_2_);
	}

	@Override
	public void addCollisionBoxesToList(World p_149743_1_, int p_149743_2_, int p_149743_3_, int p_149743_4_,
		AxisAlignedBB p_149743_5_, List p_149743_6_, Entity p_149743_7_)
	{
		if (baseBlock != null)
		{
			baseBlock.addCollisionBoxesToList
				(
					p_149743_1_,
					p_149743_2_,
					p_149743_3_,
					p_149743_4_,
					p_149743_5_,
					p_149743_6_,
					p_149743_7_
				);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World p_149668_1_, int p_149668_2_, int p_149668_3_,
		int p_149668_4_)
	{
		if (baseBlock == null) { return super.getCollisionBoundingBoxFromPool(p_149668_1_, p_149668_2_, p_149668_3_,
			p_149668_4_); }
		return baseBlock.getCollisionBoundingBoxFromPool(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World p_149633_1_, int p_149633_2_, int p_149633_3_,
		int p_149633_4_)
	{
		if (baseBlock == null) { return super.getSelectedBoundingBoxFromPool(p_149633_1_, p_149633_2_, p_149633_3_,
			p_149633_4_); }
		return baseBlock.getSelectedBoundingBoxFromPool(p_149633_1_, p_149633_2_, p_149633_3_, p_149633_4_);
	}

	@Override
	public boolean isOpaqueCube()
	{
		if (baseBlock == null) { return super.isOpaqueCube(); }
		return baseBlock.isOpaqueCube();
	}

	@Override
	public boolean canCollideCheck(int p_149678_1_, boolean p_149678_2_)
	{
		if (baseBlock == null) { return super.canCollideCheck(p_149678_1_, p_149678_2_); }
		return baseBlock.canCollideCheck(p_149678_1_, p_149678_2_);
	}

	@Override
	public boolean isCollidable()
	{
		if (baseBlock == null) { return super.isCollidable(); }
		return baseBlock.isCollidable();
	}

	@Override
	public void updateTick(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_,
		Random p_149734_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.randomDisplayTick(p_149734_1_, p_149734_2_, p_149734_3_, p_149734_4_, p_149734_5_);
		}
	}

	@Override
	public void onBlockDestroyedByPlayer(World p_149664_1_, int p_149664_2_, int p_149664_3_, int p_149664_4_,
		int p_149664_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockDestroyedByPlayer(p_149664_1_, p_149664_2_, p_149664_3_, p_149664_4_, p_149664_5_);
		}
	}

	@Override
	public void onNeighborBlockChange(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_,
		Block p_149695_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onNeighborBlockChange(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_, p_149695_5_);
		}
	}

	@Override
	public int tickRate(World p_149738_1_)
	{
		if (baseBlock == null) { return super.tickRate(p_149738_1_); }
		return baseBlock.tickRate(p_149738_1_);
	}

	@Override
	public void onBlockAdded(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
		}
	}

	@Override
	public void breakBlock(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_,
		int p_149749_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
		}
	}

	@Override
	public int quantityDropped(Random p_149745_1_)
	{
		if (baseBlock == null) { return super.quantityDropped(p_149745_1_); }
		return baseBlock.quantityDropped(p_149745_1_);
	}

	@Override
	public Item getItemDropped(int p_149650_1_, Random p_149650_2_, int p_149650_3_)
	{
		if (baseBlock == null) { return super.getItemDropped(p_149650_1_, p_149650_2_, p_149650_3_); }
		return baseBlock.getItemDropped(p_149650_1_, p_149650_2_, p_149650_3_);
	}

	@Override
	public float getPlayerRelativeBlockHardness(EntityPlayer p_149737_1_, World p_149737_2_, int p_149737_3_,
		int p_149737_4_, int p_149737_5_)
	{
		if (baseBlock == null) { return super.getPlayerRelativeBlockHardness(p_149737_1_, p_149737_2_, p_149737_3_,
			p_149737_4_, p_149737_5_); }
		return baseBlock
			.getPlayerRelativeBlockHardness(p_149737_1_, p_149737_2_, p_149737_3_, p_149737_4_, p_149737_5_);
	}

	@Override
	public void dropBlockAsItemWithChance(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_,
		int p_149690_5_, float p_149690_6_, int p_149690_7_)
	{
		if (baseBlock != null)
		{
			baseBlock.dropBlockAsItemWithChance
				(
					p_149690_1_, p_149690_2_, p_149690_3_,
					p_149690_4_, p_149690_5_, p_149690_6_,
					p_149690_7_
				);
		}
	}

	@Override
	public void dropXpOnBlockBreak(World p_149657_1_, int p_149657_2_, int p_149657_3_, int p_149657_4_, int p_149657_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.dropXpOnBlockBreak(p_149657_1_, p_149657_2_, p_149657_3_, p_149657_4_, p_149657_5_);
		}
	}

	@Override
	public int damageDropped(int p_149692_1_)
	{
		if (baseBlock == null) { return super.damageDropped(p_149692_1_); }
		return baseBlock.damageDropped(p_149692_1_);
	}

	@Override
	public float getExplosionResistance(Entity p_149638_1_)
	{
		if (baseBlock == null) { return super.getExplosionResistance(p_149638_1_); }
		return baseBlock.getExplosionResistance(p_149638_1_);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World p_149731_1_, int p_149731_2_, int p_149731_3_, int p_149731_4_,
		Vec3 p_149731_5_, Vec3 p_149731_6_)
	{
		if (baseBlock == null) { return super.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_,
			p_149731_5_, p_149731_6_); }
		return baseBlock
			.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_, p_149731_5_, p_149731_6_);
	}

	@Override
	public void onBlockDestroyedByExplosion(World p_149723_1_, int p_149723_2_, int p_149723_3_, int p_149723_4_,
		Explosion p_149723_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockDestroyedByExplosion(p_149723_1_, p_149723_2_, p_149723_3_, p_149723_4_, p_149723_5_);
		}
	}

	@Override
	public boolean canReplace(World p_149705_1_, int p_149705_2_, int p_149705_3_, int p_149705_4_, int p_149705_5_,
		ItemStack p_149705_6_)
	{
		if (baseBlock == null) { return super.canReplace(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_,
			p_149705_5_, p_149705_6_); }
		return baseBlock.canReplace(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_, p_149705_5_, p_149705_6_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		if (baseBlock == null) { return super.getRenderBlockPass(); }
		return baseBlock.getRenderBlockPass();
	}

	@Override
	public boolean canPlaceBlockOnSide(World p_149707_1_, int p_149707_2_, int p_149707_3_, int p_149707_4_,
		int p_149707_5_)
	{
		if (baseBlock == null) { return super.canPlaceBlockOnSide(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_,
			p_149707_5_); }
		return baseBlock.canPlaceBlockOnSide(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_, p_149707_5_);
	}

	@Override
	public boolean canPlaceBlockAt(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_)
	{
		if (baseBlock == null) { return super.canPlaceBlockAt(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_); }
		return baseBlock.canPlaceBlockAt(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
	}

	@Override
	public boolean onBlockActivated(World p_149727_1_, int p_149727_2_, int p_149727_3_, int p_149727_4_,
		EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
	{
		if (baseBlock == null) { return super.onBlockActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_,
			p_149727_5_, p_149727_6_,
			p_149727_7_, p_149727_8_, p_149727_9_); }

		return baseBlock.onBlockActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_, p_149727_5_, p_149727_6_,
			p_149727_7_, p_149727_8_, p_149727_9_);
	}

	@Override
	public void onEntityWalking(World p_149724_1_, int p_149724_2_, int p_149724_3_, int p_149724_4_, Entity p_149724_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onEntityWalking(p_149724_1_, p_149724_2_, p_149724_3_, p_149724_4_, p_149724_5_);
		}
	}

	@Override
	public int onBlockPlaced(World p_149660_1_, int p_149660_2_, int p_149660_3_, int p_149660_4_, int p_149660_5_,
		float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_)
	{
		if (baseBlock == null) { return super.onBlockPlaced(p_149660_1_, p_149660_2_, p_149660_3_, p_149660_4_,
			p_149660_5_, p_149660_6_,
			p_149660_7_, p_149660_8_, p_149660_9_); }

		return baseBlock.onBlockPlaced(p_149660_1_, p_149660_2_, p_149660_3_, p_149660_4_, p_149660_5_, p_149660_6_,
			p_149660_7_, p_149660_8_, p_149660_9_);
	}

	@Override
	public void onBlockClicked(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_,
		EntityPlayer p_149699_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockClicked(p_149699_1_, p_149699_2_, p_149699_3_, p_149699_4_, p_149699_5_);
		}
	}

	@Override
	public void velocityToAddToEntity(World p_149640_1_, int p_149640_2_, int p_149640_3_, int p_149640_4_,
		Entity p_149640_5_, Vec3 p_149640_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.velocityToAddToEntity(p_149640_1_, p_149640_2_, p_149640_3_, p_149640_4_, p_149640_5_,
				p_149640_6_);
		}
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_)
	{
		if (baseBlock != null)
		{
			baseBlock.setBlockBoundsBasedOnState(p_149719_1_, p_149719_2_, p_149719_3_, p_149719_4_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBlockColor()
	{
		if (baseBlock == null) { return super.getBlockColor(); }
		return baseBlock.getBlockColor();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderColor(int p_149741_1_)
	{
		if (baseBlock == null) { return super.getRenderColor(p_149741_1_); }
		return baseBlock.getRenderColor(p_149741_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_)
	{
		if (baseBlock == null) { return super.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_); }
		return baseBlock.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess p_149709_1_, int p_149709_2_, int p_149709_3_, int p_149709_4_,
		int p_149709_5_)
	{
		if (baseBlock == null) { return super.isProvidingWeakPower(p_149709_1_, p_149709_2_, p_149709_3_, p_149709_4_,
			p_149709_5_); }
		return baseBlock.isProvidingWeakPower(p_149709_1_, p_149709_2_, p_149709_3_, p_149709_4_, p_149709_5_);
	}

	@Override
	public boolean canProvidePower()
	{
		if (baseBlock == null) { return super.canProvidePower(); }
		return baseBlock.canProvidePower();
	}

	@Override
	public void onEntityCollidedWithBlock(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_,
		Entity p_149670_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onEntityCollidedWithBlock(p_149670_1_, p_149670_2_, p_149670_3_, p_149670_4_, p_149670_5_);
		}
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess p_149748_1_, int p_149748_2_, int p_149748_3_, int p_149748_4_,
		int p_149748_5_)
	{
		if (baseBlock == null) { return super.isProvidingStrongPower(p_149748_1_, p_149748_2_, p_149748_3_,
			p_149748_4_, p_149748_5_); }

		return baseBlock.isProvidingStrongPower(p_149748_1_, p_149748_2_, p_149748_3_, p_149748_4_, p_149748_5_);
	}

	@Override
	public void setBlockBoundsForItemRender()
	{
		if (baseBlock != null)
		{
			baseBlock.setBlockBoundsForItemRender();
		}
	}

	@Override
	public void harvestBlock(World p_149636_1_, EntityPlayer p_149636_2_, int p_149636_3_, int p_149636_4_,
		int p_149636_5_, int p_149636_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.harvestBlock(p_149636_1_, p_149636_2_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_);
		}
	}

	@Override
	public int quantityDroppedWithBonus(int p_149679_1_, Random p_149679_2_)
	{
		if (baseBlock == null) { return super.quantityDroppedWithBonus(p_149679_1_, p_149679_2_); }

		return baseBlock.quantityDroppedWithBonus(p_149679_1_, p_149679_2_);
	}

	@Override
	public boolean canBlockStay(World p_149718_1_, int p_149718_2_, int p_149718_3_, int p_149718_4_)
	{
		if (baseBlock == null) { return super.canBlockStay(p_149718_1_, p_149718_2_, p_149718_3_, p_149718_4_); }

		return baseBlock.canBlockStay(p_149718_1_, p_149718_2_, p_149718_3_, p_149718_4_);
	}

	@Override
	public void onBlockPlacedBy(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_,
		EntityLivingBase p_149689_5_, ItemStack p_149689_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockPlacedBy(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, p_149689_5_, p_149689_6_);
		}
	}

	@Override
	public void onPostBlockPlaced(World p_149714_1_, int p_149714_2_, int p_149714_3_, int p_149714_4_, int p_149714_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onPostBlockPlaced(p_149714_1_, p_149714_2_, p_149714_3_, p_149714_4_, p_149714_5_);
		}
	}

	@Override
	public boolean onBlockEventReceived(World p_149696_1_, int p_149696_2_, int p_149696_3_, int p_149696_4_,
		int p_149696_5_, int p_149696_6_)
	{
		if (baseBlock == null) { return super.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_,
			p_149696_5_,
			p_149696_6_); }

		return baseBlock.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_,
			p_149696_6_);
	}

	@Override
	public boolean getEnableStats()
	{
		if (baseBlock == null) { return super.getEnableStats(); }
		return baseBlock.getEnableStats();
	}

	@Override
	public int getMobilityFlag()
	{
		if (baseBlock == null) { return super.getMobilityFlag(); }
		return baseBlock.getMobilityFlag();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getAmbientOcclusionLightValue()
	{
		if (baseBlock == null) { return super.getAmbientOcclusionLightValue(); }
		return baseBlock.getAmbientOcclusionLightValue();
	}

	@Override
	public void onFallenUpon(World p_149746_1_, int p_149746_2_, int p_149746_3_, int p_149746_4_, Entity p_149746_5_,
		float p_149746_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.onFallenUpon(p_149746_1_, p_149746_2_, p_149746_3_, p_149746_4_, p_149746_5_, p_149746_6_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
	{
		if (baseBlock == null) { return super.getItem(p_149694_1_, p_149694_2_, p_149694_3_, p_149694_4_); }

		return baseBlock.getItem(p_149694_1_, p_149694_2_, p_149694_3_, p_149694_4_);
	}

	@Override
	public int getDamageValue(World p_149643_1_, int p_149643_2_, int p_149643_3_, int p_149643_4_)
	{
		if (baseBlock == null) { return super.getDamageValue(p_149643_1_, p_149643_2_, p_149643_3_, p_149643_4_); }

		return baseBlock.getDamageValue(p_149643_1_, p_149643_2_, p_149643_3_, p_149643_4_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_)
	{
		if (baseBlock != null)
		{
			baseBlock.getSubBlocks(p_149666_1_, p_149666_2_, p_149666_3_);
		}
	}

	@Override
	public Block setCreativeTab(CreativeTabs p_149647_1_)
	{
		if (baseBlock == null) { return super.setCreativeTab(p_149647_1_); }
		return baseBlock.setCreativeTab(p_149647_1_);
	}

	@Override
	public void onBlockHarvested(World p_149681_1_, int p_149681_2_, int p_149681_3_, int p_149681_4_, int p_149681_5_,
		EntityPlayer p_149681_6_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockHarvested(p_149681_1_, p_149681_2_, p_149681_3_, p_149681_4_, p_149681_5_, p_149681_6_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTabToDisplayOn()
	{
		if (baseBlock == null) { return super.getCreativeTabToDisplayOn(); }

		return baseBlock.getCreativeTabToDisplayOn();
	}

	@Override
	public void onBlockPreDestroy(World p_149725_1_, int p_149725_2_, int p_149725_3_, int p_149725_4_, int p_149725_5_)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockPreDestroy(p_149725_1_, p_149725_2_, p_149725_3_, p_149725_4_, p_149725_5_);
		}
	}

	@Override
	public void fillWithRain(World p_149639_1_, int p_149639_2_, int p_149639_3_, int p_149639_4_)
	{
		if (baseBlock != null)
		{
			baseBlock.fillWithRain(p_149639_1_, p_149639_2_, p_149639_3_, p_149639_4_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFlowerPot()
	{
		if (baseBlock == null) { return super.isFlowerPot(); }
		return baseBlock.isFlowerPot();
	}

	@Override
	public boolean func_149698_L()
	{
		if (baseBlock == null) { return super.func_149698_L(); }
		return baseBlock.func_149698_L();
	}

	@Override
	public boolean canDropFromExplosion(Explosion p_149659_1_)
	{
		if (baseBlock == null) { return super.canDropFromExplosion(p_149659_1_); }
		return baseBlock.canDropFromExplosion(p_149659_1_);
	}

	@Override
	public boolean hasComparatorInputOverride()
	{
		if (baseBlock == null) { return super.hasComparatorInputOverride(); }
		return baseBlock.hasComparatorInputOverride();
	}

	@Override
	public int getComparatorInputOverride(World p_149736_1_, int p_149736_2_, int p_149736_3_, int p_149736_4_,
		int p_149736_5_)
	{
		if (baseBlock == null) { return super.getComparatorInputOverride(p_149736_1_, p_149736_2_, p_149736_3_,
			p_149736_4_, p_149736_5_); }
		return baseBlock.getComparatorInputOverride(p_149736_1_, p_149736_2_, p_149736_3_, p_149736_4_, p_149736_5_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon func_149735_b(int p_149735_1_, int p_149735_2_)
	{
		if (baseBlock == null) { return super.func_149735_b(p_149735_1_, p_149735_2_); }
		return baseBlock.func_149735_b(p_149735_1_, p_149735_2_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_)
	{
		if (baseBlock != null)
		{
			baseBlock.registerBlockIcons(p_149651_1_);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemIconName()
	{
		if (baseBlock == null) { return super.getItemIconName(); }
		return baseBlock.getItemIconName();
	}

	// #endregion

	// #region Forge Specific Chameleon Methods

	@Override
	public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity)
	{
		if (baseBlock == null) { return super.isLadder(world, x, y, z, entity); }
		return baseBlock.isLadder(world, x, y, z, entity);
	}

	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isNormalCube(world, x, y, z); }
		return baseBlock.isNormalCube(world, x, y, z);
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		if (baseBlock == null) { return super.isSideSolid(world, x, y, z, side); }
		return baseBlock.isSideSolid(world, x, y, z, side);
	}

	@Override
	public boolean isReplaceable(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isReplaceable(world, x, y, z); }
		return baseBlock.isReplaceable(world, x, y, z);
	}

	@Override
	public boolean isBurning(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isBurning(world, x, y, z); }
		return baseBlock.isBurning(world, x, y, z);
	}

	@Override
	public boolean isAir(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isAir(world, x, y, z); }
		return baseBlock.isAir(world, x, y, z);
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		if (baseBlock == null) { return super.canHarvestBlock(player, meta); }
		return baseBlock.canHarvestBlock(player, meta);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest)
	{
		if (baseBlock == null) { return super.removedByPlayer(world, player, x, y, z, willHarvest); }
		return baseBlock.removedByPlayer(world, player, x, y, z, willHarvest);
	}

	@Override
	@Deprecated
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (baseBlock == null) { return super.removedByPlayer(world, player, x, y, z); }
		return baseBlock.removedByPlayer(world, player, x, y, z);
	}

	@Override
	public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		if (baseBlock == null) { return super.getFlammability(world, x, y, z, face); }
		return baseBlock.getFlammability(world, x, y, z, face);
	}

	@Override
	public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		if (baseBlock == null) { return super.isFlammable(world, x, y, z, face); }
		return baseBlock.isFlammable(world, x, y, z, face);
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face)
	{
		if (baseBlock == null) { return super.getFireSpreadSpeed(world, x, y, z, face); }
		return baseBlock.getFireSpreadSpeed(world, x, y, z, face);
	}

	@Override
	public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side)
	{
		if (baseBlock == null) { return super.isFireSource(world, x, y, z, side); }
		return baseBlock.isFireSource(world, x, y, z, side);
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		if (baseBlock == null) { return super.hasTileEntity(metadata); }
		return baseBlock.hasTileEntity(metadata);
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		if (baseBlock == null) { return super.createTileEntity(world, metadata); }
		return baseBlock.createTileEntity(world, metadata);
	}

	@Override
	public int quantityDropped(int meta, int fortune, Random random)
	{
		if (baseBlock == null) { return super.quantityDropped(meta, fortune, random); }
		return baseBlock.quantityDropped(meta, fortune, random);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		if (baseBlock == null) { return super.getDrops(world, x, y, z, metadata, fortune); }
		return baseBlock.getDrops(world, x, y, z, metadata, fortune);
	}

	@Override
	public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player)
	{
		if (baseBlock == null) { return super.isBed(world, x, y, z, player); }
		return baseBlock.isBed(world, x, y, z, player);
	}

	@Override
	public ChunkCoordinates getBedSpawnPosition(IBlockAccess world, int x, int y, int z, EntityPlayer player)
	{
		if (baseBlock == null) { return super.getBedSpawnPosition(world, x, y, z, player); }
		return baseBlock.getBedSpawnPosition(world, x, y, z, player);
	}

	@Override
	public void setBedOccupied(IBlockAccess world, int x, int y, int z, EntityPlayer player, boolean occupied)
	{
		if (baseBlock != null)
		{
			baseBlock.setBedOccupied(world, x, y, z, player, occupied);
		}
	}

	@Override
	public int getBedDirection(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getBedDirection(world, x, y, z); }
		return baseBlock.getBedDirection(world, x, y, z);
	}

	@Override
	public boolean isBedFoot(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isBedFoot(world, x, y, z); }
		return baseBlock.isBedFoot(world, x, y, z);
	}

	@Override
	public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.canSustainLeaves(world, x, y, z); }
		return baseBlock.canSustainLeaves(world, x, y, z);
	}

	@Override
	public boolean isWood(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isWood(world, x, y, z); }
		return baseBlock.isWood(world, x, y, z);
	}

	@Override
	public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX,
		double explosionY, double explosionZ)
	{
		if (baseBlock == null) { return super.getExplosionResistance(par1Entity, world, x, y, z, explosionX,
			explosionY, explosionZ); }
		return baseBlock.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion)
	{
		if (baseBlock != null)
		{
			baseBlock.onBlockExploded(world, x, y, z, explosion);
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		if (baseBlock == null) { return super.canConnectRedstone(world, x, y, z, side); }
		return baseBlock.canConnectRedstone(world, x, y, z, side);
	}

	@Override
	public boolean canPlaceTorchOnTop(World world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.canPlaceTorchOnTop(world, x, y, z); }
		return baseBlock.canPlaceTorchOnTop(world, x, y, z);
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		if (baseBlock == null) { return super.canRenderInPass(pass); }
		return baseBlock.canRenderInPass(pass);
	}

//	@Override
//	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
//	{
//		if (baseBlock == null) { return super.getPickBlock(target, world, x, y, z, player); }
//		return baseBlock.getPickBlock(target, world, x, y, z, player);
//	}

	@Override
	@Deprecated
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getPickBlock(target, world, x, y, z); }
		return baseBlock.getPickBlock(target, world, x, y, z);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		if (baseBlock == null) { return super.addHitEffects(worldObj, target, effectRenderer); }
		return baseBlock.addHitEffects(worldObj, target, effectRenderer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
	{
		if (baseBlock == null) { return super.addDestroyEffects(world, x, y, z, meta, effectRenderer); }
		return baseBlock.addDestroyEffects(world, x, y, z, meta, effectRenderer);
	}

	@Override
	public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction,
		IPlantable plantable)
	{
		if (baseBlock == null) { return super.canSustainPlant(world, x, y, z, direction, plantable); }
		return baseBlock.canSustainPlant(world, x, y, z, direction, plantable);
	}

	@Override
	public void onPlantGrow(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ)
	{
		if (baseBlock != null)
		{
			baseBlock.onPlantGrow(world, x, y, z, sourceX, sourceY, sourceZ);
		}
	}

	@Override
	public boolean isFertile(World world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.isFertile(world, x, y, z); }
		return baseBlock.isFertile(world, x, y, z);
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity)
	{
		if (baseBlock == null) { return super.canEntityDestroy(world, x, y, z, entity); }
		return baseBlock.canEntityDestroy(world, x, y, z, entity);
	}

	@Override
	public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ)
	{
		if (baseBlock == null) { return super.isBeaconBase(worldObj, x, y, z, beaconX, beaconY, beaconZ); }
		return baseBlock.isBeaconBase(worldObj, x, y, z, beaconX, beaconY, beaconZ);
	}

	@Override
	public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
	{
		if (baseBlock == null) { return super.rotateBlock(worldObj, x, y, z, axis); }
		return baseBlock.rotateBlock(worldObj, x, y, z, axis);
	}

	@Override
	public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getValidRotations(worldObj, x, y, z); }
		return baseBlock.getValidRotations(worldObj, x, y, z);
	}

	@Override
	public float getEnchantPowerBonus(World world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getEnchantPowerBonus(world, x, y, z); }
		return baseBlock.getEnchantPowerBonus(world, x, y, z);
	}

	@Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour)
	{
		if (baseBlock == null) { return super.recolourBlock(world, x, y, z, side, colour); }
		return baseBlock.recolourBlock(world, x, y, z, side, colour);
	}

	@Override
	public int getExpDrop(IBlockAccess world, int metadata, int fortune)
	{
		if (baseBlock == null) { return super.getExpDrop(world, metadata, fortune); }
		return baseBlock.getExpDrop(world, metadata, fortune);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ)
	{
		if (baseBlock != null)
		{
			baseBlock.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
		}
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		if (baseBlock == null) { return super.shouldCheckWeakPower(world, x, y, z, side); }
		return baseBlock.shouldCheckWeakPower(world, x, y, z, side);
	}

	@Override
	public boolean getWeakChanges(IBlockAccess world, int x, int y, int z)
	{
		if (baseBlock == null) { return super.getWeakChanges(world, x, y, z); }
		return baseBlock.getWeakChanges(world, x, y, z);
	}

	@Override
	public void setHarvestLevel(String toolClass, int level)
	{
		if (baseBlock != null)
		{
			baseBlock.setHarvestLevel(toolClass, level);
		}
	}

	@Override
	public void setHarvestLevel(String toolClass, int level, int metadata)
	{
		if (baseBlock != null)
		{
			baseBlock.setHarvestLevel(toolClass, level, metadata);
		}
	}

	@Override
	public String getHarvestTool(int metadata)
	{
		if (baseBlock == null) { return super.getHarvestTool(metadata); }
		return baseBlock.getHarvestTool(metadata);
	}

	@Override
	public int getHarvestLevel(int metadata)
	{
		if (baseBlock == null) { return super.getHarvestLevel(metadata); }
		return baseBlock.getHarvestLevel(metadata);
	}

	@Override
	public boolean isToolEffective(String type, int metadata)
	{
		if (baseBlock == null) { return super.isToolEffective(type, metadata); }
		return baseBlock.isToolEffective(type, metadata);
	}

	// #endregion
}
