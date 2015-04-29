/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.ModConsts;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class BlockDome extends Block {
  // #region Static Methods and Fields

  private static final ConcurrentHashMap<Block, BlockData> InitializedBlocks = new ConcurrentHashMap<Block, BlockData>();

  public static BlockData getDomeBlock(final BlockData baseBlock) {
    if (BlockData.isNullOrEmpty(baseBlock)) {
      return BlockData.Empty;
    }
    if (baseBlock.block instanceof BlockDome) {
      return baseBlock;
    }
    if (baseBlock.block == Blx.anvil) {
      return baseBlock;
    } // Quick hack to fixes issue #15.

    try {
      BlockData ret = BlockDome.InitializedBlocks.get(baseBlock.block);
      if (ret == null) {
        ret = BlockDome.InitializedBlocks.putIfAbsent(baseBlock.block,
            baseBlock.setBlock(new BlockDome(baseBlock.block)));

        if (ret == null) {
          ret = BlockDome.InitializedBlocks.get(baseBlock.block);
        }

        ((BlockDome) ret.block).initBlock();
      }

      return ret;
    } catch (final Exception e) {
      return baseBlock;
    }
  }

  private static String getRegularBlockName(final Block block) {
    String name = block.getUnlocalizedName();
    if (name.toLowerCase().startsWith("tile.")) {
      name = name.substring(5);
    }

    return name;
  }

  public static void initalizeAllRegisteredBlocks() {
    System.out.println("InitalizeAllRegisteredBlocks Entered.");

    // Iterate all blocks, initializing a DomeBlock copy of it.
    for (final Block block : Blx.getAllBlocks()) {
      if (!(block instanceof BlockDome)) {
        System.out.println("InitalizeAllRegisteredBlocks: Copying block " + block.getLocalizedName() + " with id of #"
            + Block.getIdFromBlock(block));
        BlockDome.getDomeBlock(new BlockData(block));
      }
    }

    System.out.println("InitalizeAllRegisteredBlocks Exited.");
  }

  // #endregion

  // #region Instance Data

  private final Block baseBlock;
  private boolean blockInitialized = false;
  private final Object lock = new Object();

  // #endregion

  // #region Constructor

  private BlockDome(final Block baseBlock) {
    super(baseBlock.getMaterial());
    this.baseBlock = baseBlock;
  }

  @Override
  public void addCollisionBoxesToList(final World p_149743_1_, final int p_149743_2_, final int p_149743_3_,
      final int p_149743_4_, final AxisAlignedBB p_149743_5_, final List p_149743_6_, final Entity p_149743_7_) {
    if (this.baseBlock != null) {
      this.baseBlock.addCollisionBoxesToList(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_, p_149743_5_,
          p_149743_6_, p_149743_7_);
    }
  }

  // #endregion

  // #region Modified Behavior

  @Override
  @SideOnly(Side.CLIENT)
  public boolean addDestroyEffects(final World world, final int x, final int y, final int z, final int meta,
      final EffectRenderer effectRenderer) {
    if (this.baseBlock == null) {
      return super.addDestroyEffects(world, x, y, z, meta, effectRenderer);
    }
    return this.baseBlock.addDestroyEffects(world, x, y, z, meta, effectRenderer);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean addHitEffects(final World worldObj, final MovingObjectPosition target,
      final EffectRenderer effectRenderer) {
    if (this.baseBlock == null) {
      return super.addHitEffects(worldObj, target, effectRenderer);
    }
    return this.baseBlock.addHitEffects(worldObj, target, effectRenderer);
  }

  @Override
  public void beginLeavesDecay(final World world, final int x, final int y, final int z) {
    // ignore this event
  }

  @Override
  public void breakBlock(final World p_149749_1_, final int p_149749_2_, final int p_149749_3_, final int p_149749_4_,
      final Block p_149749_5_, final int p_149749_6_) {
    if (this.baseBlock != null) {
      this.baseBlock.breakBlock(p_149749_1_, p_149749_2_, p_149749_3_, p_149749_4_, p_149749_5_, p_149749_6_);
    }
  }

  @Override
  public boolean canBeReplacedByLeaves(final IBlockAccess world, final int x, final int y, final int z) {
    return false;
  }

  @Override
  public boolean canBlockStay(final World p_149718_1_, final int p_149718_2_, final int p_149718_3_,
      final int p_149718_4_) {
    if (this.baseBlock == null) {
      return super.canBlockStay(p_149718_1_, p_149718_2_, p_149718_3_, p_149718_4_);
    }

    return this.baseBlock.canBlockStay(p_149718_1_, p_149718_2_, p_149718_3_, p_149718_4_);
  }

  @Override
  public boolean canCollideCheck(final int p_149678_1_, final boolean p_149678_2_) {
    if (this.baseBlock == null) {
      return super.canCollideCheck(p_149678_1_, p_149678_2_);
    }
    return this.baseBlock.canCollideCheck(p_149678_1_, p_149678_2_);
  }

  @Override
  public boolean canConnectRedstone(final IBlockAccess world, final int x, final int y, final int z, final int side) {
    if (this.baseBlock == null) {
      return super.canConnectRedstone(world, x, y, z, side);
    }
    return this.baseBlock.canConnectRedstone(world, x, y, z, side);
  }

  @Override
  public boolean canCreatureSpawn(final EnumCreatureType type, final IBlockAccess world, final int x, final int y,
      final int z) {
    if (this.baseBlock == null) {
      return false;
    }

    // TODO: Might want to return false here, not sure, need to investigate first.
    return this.baseBlock.canCreatureSpawn(type, world, x, y, z);
  }

  // #endregion

  // #region General Chameleon Methods

  @Override
  public boolean canDropFromExplosion(final Explosion p_149659_1_) {
    if (this.baseBlock == null) {
      return super.canDropFromExplosion(p_149659_1_);
    }
    return this.baseBlock.canDropFromExplosion(p_149659_1_);
  }

  @Override
  public boolean canEntityDestroy(final IBlockAccess world, final int x, final int y, final int z, final Entity entity) {
    if (this.baseBlock == null) {
      return super.canEntityDestroy(world, x, y, z, entity);
    }
    return this.baseBlock.canEntityDestroy(world, x, y, z, entity);
  }

  @Override
  public boolean canHarvestBlock(final EntityPlayer player, final int meta) {
    if (this.baseBlock == null) {
      return super.canHarvestBlock(player, meta);
    }
    return this.baseBlock.canHarvestBlock(player, meta);
  }

  @Override
  public boolean canPlaceBlockAt(final World p_149742_1_, final int p_149742_2_, final int p_149742_3_,
      final int p_149742_4_) {
    if (this.baseBlock == null) {
      return super.canPlaceBlockAt(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
    }
    return this.baseBlock.canPlaceBlockAt(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
  }

  @Override
  public boolean canPlaceBlockOnSide(final World p_149707_1_, final int p_149707_2_, final int p_149707_3_,
      final int p_149707_4_, final int p_149707_5_) {
    if (this.baseBlock == null) {
      return super.canPlaceBlockOnSide(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_, p_149707_5_);
    }
    return this.baseBlock.canPlaceBlockOnSide(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_, p_149707_5_);
  }

  @Override
  public boolean canPlaceTorchOnTop(final World world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.canPlaceTorchOnTop(world, x, y, z);
    }
    return this.baseBlock.canPlaceTorchOnTop(world, x, y, z);
  }

  @Override
  public boolean canProvidePower() {
    if (this.baseBlock == null) {
      return super.canProvidePower();
    }
    return this.baseBlock.canProvidePower();
  }

  @Override
  public boolean canRenderInPass(final int pass) {
    if (this.baseBlock == null) {
      return super.canRenderInPass(pass);
    }
    return this.baseBlock.canRenderInPass(pass);
  }

  @Override
  public boolean canReplace(final World p_149705_1_, final int p_149705_2_, final int p_149705_3_,
      final int p_149705_4_, final int p_149705_5_, final ItemStack p_149705_6_) {
    if (this.baseBlock == null) {
      return super.canReplace(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_, p_149705_5_, p_149705_6_);
    }
    return this.baseBlock.canReplace(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_, p_149705_5_, p_149705_6_);
  }

  @Override
  public boolean canSilkHarvest(final World world, final EntityPlayer player, final int x, final int y, final int z,
      final int metadata) {
    // Prevent silk touch from harvesting this block.
    return false;
  }

  @Override
  public boolean canSustainLeaves(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.canSustainLeaves(world, x, y, z);
    }
    return this.baseBlock.canSustainLeaves(world, x, y, z);
  }

  @Override
  public boolean canSustainPlant(final IBlockAccess world, final int x, final int y, final int z,
      final ForgeDirection direction, final IPlantable plantable) {
    if (this.baseBlock == null) {
      return super.canSustainPlant(world, x, y, z, direction, plantable);
    }
    return this.baseBlock.canSustainPlant(world, x, y, z, direction, plantable);
  }

  @Override
  public MovingObjectPosition collisionRayTrace(final World p_149731_1_, final int p_149731_2_, final int p_149731_3_,
      final int p_149731_4_, final Vec3 p_149731_5_, final Vec3 p_149731_6_) {
    if (this.baseBlock == null) {
      return super.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_, p_149731_5_, p_149731_6_);
    }
    return this.baseBlock.collisionRayTrace(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_, p_149731_5_,
        p_149731_6_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int colorMultiplier(final IBlockAccess p_149720_1_, final int p_149720_2_, final int p_149720_3_,
      final int p_149720_4_) {
    if (this.baseBlock == null) {
      return super.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_);
    }
    return this.baseBlock.colorMultiplier(p_149720_1_, p_149720_2_, p_149720_3_, p_149720_4_);
  }

  @Override
  public TileEntity createTileEntity(final World world, final int metadata) {
    if (this.baseBlock == null) {
      return super.createTileEntity(world, metadata);
    }
    return this.baseBlock.createTileEntity(world, metadata);
  }

  @Override
  public int damageDropped(final int p_149692_1_) {
    if (this.baseBlock == null) {
      return super.damageDropped(p_149692_1_);
    }
    return this.baseBlock.damageDropped(p_149692_1_);
  }

  @Override
  public void dropBlockAsItemWithChance(final World p_149690_1_, final int p_149690_2_, final int p_149690_3_,
      final int p_149690_4_, final int p_149690_5_, final float p_149690_6_, final int p_149690_7_) {
    if (this.baseBlock != null) {
      this.baseBlock.dropBlockAsItemWithChance(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_,
          p_149690_6_, p_149690_7_);
    }
  }

  @Override
  public void dropXpOnBlockBreak(final World w, final int x, final int y, final int z, final int i) {
    if (this.baseBlock != null) {
      this.baseBlock.dropXpOnBlockBreak(w, x, y, z, i);
    }
  }

  @Override
  public void fillWithRain(final World p_149639_1_, final int p_149639_2_, final int p_149639_3_, final int p_149639_4_) {
    if (this.baseBlock != null) {
      this.baseBlock.fillWithRain(p_149639_1_, p_149639_2_, p_149639_3_, p_149639_4_);
    }
  }

  @Override
  public boolean func_149698_L() {
    if (this.baseBlock == null) {
      return super.func_149698_L();
    }
    return this.baseBlock.func_149698_L();
  }

  @Override
  public boolean func_149730_j() {
    if (this.baseBlock == null) {
      return this.opaque;
    }
    return this.baseBlock.func_149730_j();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon func_149735_b(final int p_149735_1_, final int p_149735_2_) {
    if (this.baseBlock == null) {
      return super.func_149735_b(p_149735_1_, p_149735_2_);
    }
    return this.baseBlock.func_149735_b(p_149735_1_, p_149735_2_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public float getAmbientOcclusionLightValue() {
    if (this.baseBlock == null) {
      return super.getAmbientOcclusionLightValue();
    }
    return this.baseBlock.getAmbientOcclusionLightValue();
  }

  @Override
  public int getBedDirection(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getBedDirection(world, x, y, z);
    }
    return this.baseBlock.getBedDirection(world, x, y, z);
  }

  @Override
  public ChunkCoordinates getBedSpawnPosition(final IBlockAccess world, final int x, final int y, final int z,
      final EntityPlayer player) {
    if (this.baseBlock == null) {
      return super.getBedSpawnPosition(world, x, y, z, player);
    }
    return this.baseBlock.getBedSpawnPosition(world, x, y, z, player);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getBlockColor() {
    if (this.baseBlock == null) {
      return super.getBlockColor();
    }
    return this.baseBlock.getBlockColor();
  }

  @Override
  public float getBlockHardness(final World p_149712_1_, final int p_149712_2_, final int p_149712_3_,
      final int p_149712_4_) {
    if (this.baseBlock == null) {
      return super.getBlockHardness(p_149712_1_, p_149712_2_, p_149712_3_, p_149712_4_);
    }
    return this.baseBlock.getBlockHardness(p_149712_1_, p_149712_2_, p_149712_3_, p_149712_4_);
  }

  @Override
  public boolean getBlocksMovement(final IBlockAccess p_149655_1_, final int p_149655_2_, final int p_149655_3_,
      final int p_149655_4_) {
    if (this.baseBlock == null) {
      return super.getBlocksMovement(p_149655_1_, p_149655_2_, p_149655_3_, p_149655_4_);
    }
    return this.baseBlock.getBlocksMovement(p_149655_1_, p_149655_2_, p_149655_3_, p_149655_4_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean getCanBlockGrass() {
    if (this.baseBlock == null) {
      return this.canBlockGrass;
    }
    return this.baseBlock.getCanBlockGrass();
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBoxFromPool(final World p_149668_1_, final int p_149668_2_,
      final int p_149668_3_, final int p_149668_4_) {
    if (this.baseBlock == null) {
      return super.getCollisionBoundingBoxFromPool(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
    }
    return this.baseBlock.getCollisionBoundingBoxFromPool(p_149668_1_, p_149668_2_, p_149668_3_, p_149668_4_);
  }

  @Override
  public int getComparatorInputOverride(final World p_149736_1_, final int p_149736_2_, final int p_149736_3_,
      final int p_149736_4_, final int p_149736_5_) {
    if (this.baseBlock == null) {
      return super.getComparatorInputOverride(p_149736_1_, p_149736_2_, p_149736_3_, p_149736_4_, p_149736_5_);
    }
    return this.baseBlock.getComparatorInputOverride(p_149736_1_, p_149736_2_, p_149736_3_, p_149736_4_, p_149736_5_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public CreativeTabs getCreativeTabToDisplayOn() {
    if (this.baseBlock == null) {
      return super.getCreativeTabToDisplayOn();
    }

    return this.baseBlock.getCreativeTabToDisplayOn();
  }

  @Override
  public int getDamageValue(final World p_149643_1_, final int p_149643_2_, final int p_149643_3_, final int p_149643_4_) {
    if (this.baseBlock == null) {
      return super.getDamageValue(p_149643_1_, p_149643_2_, p_149643_3_, p_149643_4_);
    }

    return this.baseBlock.getDamageValue(p_149643_1_, p_149643_2_, p_149643_3_, p_149643_4_);
  }

  @Override
  public ArrayList<ItemStack> getDrops(final World world, final int x, final int y, final int z, final int metadata,
      final int fortune) {
    if (this.baseBlock == null) {
      return super.getDrops(world, x, y, z, metadata, fortune);
    }
    return this.baseBlock.getDrops(world, x, y, z, metadata, fortune);
  }

  @Override
  public boolean getEnableStats() {
    if (this.baseBlock == null) {
      return super.getEnableStats();
    }
    return this.baseBlock.getEnableStats();
  }

  @Override
  public float getEnchantPowerBonus(final World world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getEnchantPowerBonus(world, x, y, z);
    }
    return this.baseBlock.getEnchantPowerBonus(world, x, y, z);
  }

  @Override
  public int getExpDrop(final IBlockAccess world, final int metadata, final int fortune) {
    if (this.baseBlock == null) {
      return super.getExpDrop(world, metadata, fortune);
    }
    return this.baseBlock.getExpDrop(world, metadata, fortune);
  }

  @Override
  public float getExplosionResistance(final Entity e) {
    if (this.baseBlock == null) {
      return super.getExplosionResistance(e);
    }
    return this.baseBlock.getExplosionResistance(e);
  }

  @Override
  public float getExplosionResistance(final Entity par1Entity, final World world, final int x, final int y,
      final int z, final double explosionX, final double explosionY, final double explosionZ) {
    if (this.baseBlock == null) {
      return super.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
    }
    return this.baseBlock.getExplosionResistance(par1Entity, world, x, y, z, explosionX, explosionY, explosionZ);
  }

  @Override
  public int getFireSpreadSpeed(final IBlockAccess world, final int x, final int y, final int z,
      final ForgeDirection face) {
    if (this.baseBlock == null) {
      return super.getFireSpreadSpeed(world, x, y, z, face);
    }
    return this.baseBlock.getFireSpreadSpeed(world, x, y, z, face);
  }

  @Override
  public int getFlammability(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection face) {
    if (this.baseBlock == null) {
      return super.getFlammability(world, x, y, z, face);
    }
    return this.baseBlock.getFlammability(world, x, y, z, face);
  }

  @Override
  public int getHarvestLevel(final int metadata) {
    if (this.baseBlock == null) {
      return super.getHarvestLevel(metadata);
    }
    return this.baseBlock.getHarvestLevel(metadata);
  }

  @Override
  public String getHarvestTool(final int metadata) {
    if (this.baseBlock == null) {
      return super.getHarvestTool(metadata);
    }
    return this.baseBlock.getHarvestTool(metadata);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final IBlockAccess p_149673_1_, final int p_149673_2_, final int p_149673_3_,
      final int p_149673_4_, final int p_149673_5_) {
    if (this.baseBlock == null) {
      return super.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_, p_149673_5_);
    }
    return this.baseBlock.getIcon(p_149673_1_, p_149673_2_, p_149673_3_, p_149673_4_, p_149673_5_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(final int p_149691_1_, final int p_149691_2_) {
    if (this.baseBlock == null) {
      return super.getIcon(p_149691_1_, p_149691_2_);
    }
    return this.baseBlock.getIcon(p_149691_1_, p_149691_2_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public Item getItem(final World p_149694_1_, final int p_149694_2_, final int p_149694_3_, final int p_149694_4_) {
    if (this.baseBlock == null) {
      return super.getItem(p_149694_1_, p_149694_2_, p_149694_3_, p_149694_4_);
    }

    return this.baseBlock.getItem(p_149694_1_, p_149694_2_, p_149694_3_, p_149694_4_);
  }

  @Override
  public Item getItemDropped(final int p_149650_1_, final Random p_149650_2_, final int p_149650_3_) {
    if (this.baseBlock == null) {
      return super.getItemDropped(p_149650_1_, p_149650_2_, p_149650_3_);
    }
    return this.baseBlock.getItemDropped(p_149650_1_, p_149650_2_, p_149650_3_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public String getItemIconName() {
    if (this.baseBlock == null) {
      return super.getItemIconName();
    }
    return this.baseBlock.getItemIconName();
  }

  @Override
  public int getLightOpacity() {
    if (this.baseBlock == null) {
      return this.lightOpacity;
    }
    return this.baseBlock.getLightOpacity();
  }

  @Override
  public int getLightOpacity(final IBlockAccess world, final int x, final int y, final int z) {
    return 0; // let light pass through?
  }

  @Override
  public int getLightValue() {
    if (this.baseBlock == null) {
      return this.lightValue;
    }
    return this.baseBlock.getLightValue();
  }

  @Override
  public String getLocalizedName() {
    if (this.baseBlock == null) {
      return super.getLocalizedName();
    }
    return this.baseBlock.getLocalizedName();
  }

  @Override
  public MapColor getMapColor(final int p_149728_1_) {
    if (this.baseBlock == null) {
      return super.getMapColor(p_149728_1_);
    }
    return this.baseBlock.getMapColor(p_149728_1_);
  }

  @Override
  public Material getMaterial() {
    if (this.baseBlock == null) {
      return super.getMaterial();
    }
    return this.baseBlock.getMaterial();
  }

  @Override
  public int getMixedBrightnessForBlock(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getMixedBrightnessForBlock(world, x, y, z);
    }
    return this.baseBlock.getMixedBrightnessForBlock(world, x, y, z);
  }

  @Override
  public int getMobilityFlag() {
    if (this.baseBlock == null) {
      return super.getMobilityFlag();
    }
    return this.baseBlock.getMobilityFlag();
  }

  @Override
  @Deprecated
  public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y,
      final int z) {
    if (this.baseBlock == null) {
      return super.getPickBlock(target, world, x, y, z);
    }
    return this.baseBlock.getPickBlock(target, world, x, y, z);
  }

  @Override
  public float getPlayerRelativeBlockHardness(final EntityPlayer p_149737_1_, final World p_149737_2_,
      final int p_149737_3_, final int p_149737_4_, final int p_149737_5_) {
    if (this.baseBlock == null) {
      return super.getPlayerRelativeBlockHardness(p_149737_1_, p_149737_2_, p_149737_3_, p_149737_4_, p_149737_5_);
    }
    return this.baseBlock.getPlayerRelativeBlockHardness(p_149737_1_, p_149737_2_, p_149737_3_, p_149737_4_,
        p_149737_5_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getRenderBlockPass() {
    if (this.baseBlock == null) {
      return super.getRenderBlockPass();
    }
    return this.baseBlock.getRenderBlockPass();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public int getRenderColor(final int p_149741_1_) {
    if (this.baseBlock == null) {
      return super.getRenderColor(p_149741_1_);
    }
    return this.baseBlock.getRenderColor(p_149741_1_);
  }

  @Override
  public int getRenderType() {
    if (this.baseBlock == null) {
      return super.getRenderType();
    }
    return this.baseBlock.getRenderType();
  }

  @Override
  @SideOnly(Side.CLIENT)
  public AxisAlignedBB getSelectedBoundingBoxFromPool(final World w, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getSelectedBoundingBoxFromPool(w, x, y, z);
    }
    return this.baseBlock.getSelectedBoundingBoxFromPool(w, x, y, z);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(final Item p_149666_1_, final CreativeTabs p_149666_2_, final List p_149666_3_) {
    if (this.baseBlock != null) {
      this.baseBlock.getSubBlocks(p_149666_1_, p_149666_2_, p_149666_3_);
    }
  }

  @Override
  public boolean getTickRandomly() {
    if (this.baseBlock == null) {
      return super.getTickRandomly();
    }
    return this.baseBlock.getTickRandomly();
  }

  @Override
  public boolean getUseNeighborBrightness() {
    if (this.baseBlock == null) {
      return this.useNeighborBrightness;
    }
    return this.baseBlock.getUseNeighborBrightness();
  }

  @Override
  public ForgeDirection[] getValidRotations(final World worldObj, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getValidRotations(worldObj, x, y, z);
    }
    return this.baseBlock.getValidRotations(worldObj, x, y, z);
  }

  @Override
  public boolean getWeakChanges(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.getWeakChanges(world, x, y, z);
    }
    return this.baseBlock.getWeakChanges(world, x, y, z);
  }

  @Override
  public void harvestBlock(final World w, final EntityPlayer p, final int x, final int y, final int z,
      final int hardness) {
    if (this.baseBlock != null) {
      this.baseBlock.harvestBlock(w, p, x, y, z, hardness);
    }
  }

  @Override
  public boolean hasComparatorInputOverride() {
    if (this.baseBlock == null) {
      return super.hasComparatorInputOverride();
    }
    return this.baseBlock.hasComparatorInputOverride();
  }

  @Override
  @Deprecated
  // Forge: New Metadata sensitive version.
  public boolean hasTileEntity() {
    if (this.baseBlock == null) {
      return super.hasTileEntity();
    }
    return this.baseBlock.hasTileEntity();
  }

  @Override
  public boolean hasTileEntity(final int metadata) {
    if (this.baseBlock == null) {
      return super.hasTileEntity(metadata);
    }
    return this.baseBlock.hasTileEntity(metadata);
  }

  private void initBlock() {
    synchronized (this.lock) {
      if (this.blockInitialized) {
        return;
      }
      this.blockInitialized = true;
    }

    // Clone as much of BaseBlock as possible:
    this.slipperiness = this.baseBlock.slipperiness;
    this.opaque = false;
    this.canBlockGrass = this.getCanBlockGrass();

    this.useNeighborBrightness = this.getUseNeighborBrightness();

    this.needsRandomTick = this.getTickRandomly();
    this.lightOpacity = this.getLightOpacity();
    this.stepSound = this.baseBlock.stepSound;
    this.lightValue = this.getLightValue();

    this.enableStats = this.baseBlock.getEnableStats();

    this.setBlockName(BlockDome.getRegularBlockName(this.baseBlock));

    synchronized (NewBiospheresMod.biosphereWorldType) {
      final String baseName = BlockDome.getRegularBlockName(this.baseBlock) + "_dome";
      String uniqueName = baseName;
      int num = 2;

      while (GameRegistry.findBlock(ModConsts.ModId, uniqueName) != null) {
        uniqueName = baseName + num;
        num++;
      }

      GameRegistry.registerBlock(this, uniqueName);
    }
  }

  @Override
  public boolean isAir(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isAir(world, x, y, z);
    }
    return this.baseBlock.isAir(world, x, y, z);
  }

  @Override
  public boolean isAssociatedBlock(final Block p_149667_1_) {
    if (this == p_149667_1_) {
      return true;
    }
    if (this.baseBlock != null) {
      return this.baseBlock.isAssociatedBlock(p_149667_1_);
    }

    return false;
  }

  @Override
  public boolean isBeaconBase(final IBlockAccess worldObj, final int x, final int y, final int z, final int beaconX,
      final int beaconY, final int beaconZ) {
    if (this.baseBlock == null) {
      return super.isBeaconBase(worldObj, x, y, z, beaconX, beaconY, beaconZ);
    }
    return this.baseBlock.isBeaconBase(worldObj, x, y, z, beaconX, beaconY, beaconZ);
  }

  @Override
  public boolean isBed(final IBlockAccess world, final int x, final int y, final int z, final EntityLivingBase player) {
    if (this.baseBlock == null) {
      return super.isBed(world, x, y, z, player);
    }
    return this.baseBlock.isBed(world, x, y, z, player);
  }

  @Override
  public boolean isBedFoot(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isBedFoot(world, x, y, z);
    }
    return this.baseBlock.isBedFoot(world, x, y, z);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isBlockNormalCube() {
    if (this.baseBlock == null) {
      return super.isBlockNormalCube();
    }
    return this.baseBlock.isBlockNormalCube();
  }

  @Override
  public boolean isBlockSolid(final IBlockAccess p_149747_1_, final int p_149747_2_, final int p_149747_3_,
      final int p_149747_4_, final int p_149747_5_) {
    if (this.baseBlock == null) {
      return super.isBlockSolid(p_149747_1_, p_149747_2_, p_149747_3_, p_149747_4_, p_149747_5_);
    }
    return this.baseBlock.isBlockSolid(p_149747_1_, p_149747_2_, p_149747_3_, p_149747_4_, p_149747_5_);
  }

  @Override
  public boolean isBurning(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isBurning(world, x, y, z);
    }
    return this.baseBlock.isBurning(world, x, y, z);
  }

  @Override
  public boolean isCollidable() {
    if (this.baseBlock == null) {
      return super.isCollidable();
    }
    return this.baseBlock.isCollidable();
  }

  @Override
  public boolean isFertile(final World world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isFertile(world, x, y, z);
    }
    return this.baseBlock.isFertile(world, x, y, z);
  }

  @Override
  public boolean isFireSource(final World world, final int x, final int y, final int z, final ForgeDirection side) {
    if (this.baseBlock == null) {
      return super.isFireSource(world, x, y, z, side);
    }
    return this.baseBlock.isFireSource(world, x, y, z, side);
  }

  @Override
  public boolean isFlammable(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection face) {
    if (this.baseBlock == null) {
      return super.isFlammable(world, x, y, z, face);
    }
    return this.baseBlock.isFlammable(world, x, y, z, face);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean isFlowerPot() {
    if (this.baseBlock == null) {
      return super.isFlowerPot();
    }
    return this.baseBlock.isFlowerPot();
  }

  @Override
  public boolean isFoliage(final IBlockAccess world, final int x, final int y, final int z) {
    // must return true, to prevent spawning on top of dome.
    return true;
  }

  @Override
  public boolean isLadder(final IBlockAccess world, final int x, final int y, final int z, final EntityLivingBase entity) {
    if (this.baseBlock == null) {
      return super.isLadder(world, x, y, z, entity);
    }
    return this.baseBlock.isLadder(world, x, y, z, entity);
  }

  @Override
  public boolean isLeaves(final IBlockAccess world, final int x, final int y, final int z) {
    return false;
  }

  @Override
  public boolean isNormalCube() {
    if (this.baseBlock == null) {
      return super.isNormalCube();
    }
    return this.baseBlock.isNormalCube();
  }

  @Override
  public boolean isNormalCube(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isNormalCube(world, x, y, z);
    }
    return this.baseBlock.isNormalCube(world, x, y, z);
  }

  @Override
  public boolean isOpaqueCube() {
    if (this.baseBlock == null) {
      return super.isOpaqueCube();
    }
    return this.baseBlock.isOpaqueCube();
  }

  @Override
  public int isProvidingStrongPower(final IBlockAccess p_149748_1_, final int p_149748_2_, final int p_149748_3_,
      final int p_149748_4_, final int p_149748_5_) {
    if (this.baseBlock == null) {
      return super.isProvidingStrongPower(p_149748_1_, p_149748_2_, p_149748_3_, p_149748_4_, p_149748_5_);
    }

    return this.baseBlock.isProvidingStrongPower(p_149748_1_, p_149748_2_, p_149748_3_, p_149748_4_, p_149748_5_);
  }

  @Override
  public int isProvidingWeakPower(final IBlockAccess p_149709_1_, final int p_149709_2_, final int p_149709_3_,
      final int p_149709_4_, final int p_149709_5_) {
    if (this.baseBlock == null) {
      return super.isProvidingWeakPower(p_149709_1_, p_149709_2_, p_149709_3_, p_149709_4_, p_149709_5_);
    }
    return this.baseBlock.isProvidingWeakPower(p_149709_1_, p_149709_2_, p_149709_3_, p_149709_4_, p_149709_5_);
  }

  @Override
  public boolean isReplaceable(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isReplaceable(world, x, y, z);
    }
    return this.baseBlock.isReplaceable(world, x, y, z);
  }

  // #endregion

  // #region Forge Specific Chameleon Methods

  @Override
  public boolean isReplaceableOreGen(final World world, final int x, final int y, final int z, final Block target) {
    return false;
  }

  @Override
  public boolean isSideSolid(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side) {
    if (this.baseBlock == null) {
      return super.isSideSolid(world, x, y, z, side);
    }
    return this.baseBlock.isSideSolid(world, x, y, z, side);
  }

  @Override
  public boolean isToolEffective(final String type, final int metadata) {
    if (this.baseBlock == null) {
      return super.isToolEffective(type, metadata);
    }
    return this.baseBlock.isToolEffective(type, metadata);
  }

  @Override
  public boolean isWood(final IBlockAccess world, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.isWood(world, x, y, z);
    }
    return this.baseBlock.isWood(world, x, y, z);
  }

  @Override
  public boolean onBlockActivated(final World p_149727_1_, final int p_149727_2_, final int p_149727_3_,
      final int p_149727_4_, final EntityPlayer p_149727_5_, final int p_149727_6_, final float p_149727_7_,
      final float p_149727_8_, final float p_149727_9_) {
    if (this.baseBlock == null) {
      return super.onBlockActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_, p_149727_5_, p_149727_6_,
          p_149727_7_, p_149727_8_, p_149727_9_);
    }

    return this.baseBlock.onBlockActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_, p_149727_5_,
        p_149727_6_, p_149727_7_, p_149727_8_, p_149727_9_);
  }

  @Override
  public void onBlockAdded(final World p_149726_1_, final int p_149726_2_, final int p_149726_3_, final int p_149726_4_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockAdded(p_149726_1_, p_149726_2_, p_149726_3_, p_149726_4_);
    }
  }

  @Override
  public void onBlockClicked(final World p_149699_1_, final int p_149699_2_, final int p_149699_3_,
      final int p_149699_4_, final EntityPlayer p_149699_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockClicked(p_149699_1_, p_149699_2_, p_149699_3_, p_149699_4_, p_149699_5_);
    }
  }

  @Override
  public void onBlockDestroyedByExplosion(final World p_149723_1_, final int p_149723_2_, final int p_149723_3_,
      final int p_149723_4_, final Explosion p_149723_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockDestroyedByExplosion(p_149723_1_, p_149723_2_, p_149723_3_, p_149723_4_, p_149723_5_);
    }
  }

  @Override
  public void onBlockDestroyedByPlayer(final World p_149664_1_, final int p_149664_2_, final int p_149664_3_,
      final int p_149664_4_, final int p_149664_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockDestroyedByPlayer(p_149664_1_, p_149664_2_, p_149664_3_, p_149664_4_, p_149664_5_);
    }
  }

  @Override
  public boolean onBlockEventReceived(final World p_149696_1_, final int p_149696_2_, final int p_149696_3_,
      final int p_149696_4_, final int p_149696_5_, final int p_149696_6_) {
    if (this.baseBlock == null) {
      return super.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_, p_149696_6_);
    }

    return this.baseBlock.onBlockEventReceived(p_149696_1_, p_149696_2_, p_149696_3_, p_149696_4_, p_149696_5_,
        p_149696_6_);
  }

  @Override
  public void onBlockExploded(final World world, final int x, final int y, final int z, final Explosion explosion) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockExploded(world, x, y, z, explosion);
    }
  }

  @Override
  public void onBlockHarvested(final World p_149681_1_, final int p_149681_2_, final int p_149681_3_,
      final int p_149681_4_, final int p_149681_5_, final EntityPlayer p_149681_6_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockHarvested(p_149681_1_, p_149681_2_, p_149681_3_, p_149681_4_, p_149681_5_, p_149681_6_);
    }
  }

  @Override
  public int onBlockPlaced(final World p_149660_1_, final int p_149660_2_, final int p_149660_3_,
      final int p_149660_4_, final int p_149660_5_, final float p_149660_6_, final float p_149660_7_,
      final float p_149660_8_, final int p_149660_9_) {
    if (this.baseBlock == null) {
      return super.onBlockPlaced(p_149660_1_, p_149660_2_, p_149660_3_, p_149660_4_, p_149660_5_, p_149660_6_,
          p_149660_7_, p_149660_8_, p_149660_9_);
    }

    return this.baseBlock.onBlockPlaced(p_149660_1_, p_149660_2_, p_149660_3_, p_149660_4_, p_149660_5_, p_149660_6_,
        p_149660_7_, p_149660_8_, p_149660_9_);
  }

  @Override
  public void onBlockPlacedBy(final World p_149689_1_, final int p_149689_2_, final int p_149689_3_,
      final int p_149689_4_, final EntityLivingBase p_149689_5_, final ItemStack p_149689_6_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockPlacedBy(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, p_149689_5_, p_149689_6_);
    }
  }

  @Override
  public void onBlockPreDestroy(final World p_149725_1_, final int p_149725_2_, final int p_149725_3_,
      final int p_149725_4_, final int p_149725_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onBlockPreDestroy(p_149725_1_, p_149725_2_, p_149725_3_, p_149725_4_, p_149725_5_);
    }
  }

  @Override
  public void onEntityCollidedWithBlock(final World p_149670_1_, final int p_149670_2_, final int p_149670_3_,
      final int p_149670_4_, final Entity p_149670_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onEntityCollidedWithBlock(p_149670_1_, p_149670_2_, p_149670_3_, p_149670_4_, p_149670_5_);
    }
  }

  @Override
  public void onEntityWalking(final World p_149724_1_, final int p_149724_2_, final int p_149724_3_,
      final int p_149724_4_, final Entity p_149724_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onEntityWalking(p_149724_1_, p_149724_2_, p_149724_3_, p_149724_4_, p_149724_5_);
    }
  }

  @Override
  public void onFallenUpon(final World p_149746_1_, final int p_149746_2_, final int p_149746_3_,
      final int p_149746_4_, final Entity p_149746_5_, final float p_149746_6_) {
    if (this.baseBlock != null) {
      this.baseBlock.onFallenUpon(p_149746_1_, p_149746_2_, p_149746_3_, p_149746_4_, p_149746_5_, p_149746_6_);
    }
  }

  @Override
  public void onNeighborBlockChange(final World p_149695_1_, final int p_149695_2_, final int p_149695_3_,
      final int p_149695_4_, final Block p_149695_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onNeighborBlockChange(p_149695_1_, p_149695_2_, p_149695_3_, p_149695_4_, p_149695_5_);
    }
  }

  @Override
  public void onNeighborChange(final IBlockAccess world, final int x, final int y, final int z, final int tileX,
      final int tileY, final int tileZ) {
    if (this.baseBlock != null) {
      this.baseBlock.onNeighborChange(world, x, y, z, tileX, tileY, tileZ);
    }
  }

  @Override
  public void onPlantGrow(final World world, final int x, final int y, final int z, final int sourceX,
      final int sourceY, final int sourceZ) {
    if (this.baseBlock != null) {
      this.baseBlock.onPlantGrow(world, x, y, z, sourceX, sourceY, sourceZ);
    }
  }

  @Override
  public void onPostBlockPlaced(final World p_149714_1_, final int p_149714_2_, final int p_149714_3_,
      final int p_149714_4_, final int p_149714_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.onPostBlockPlaced(p_149714_1_, p_149714_2_, p_149714_3_, p_149714_4_, p_149714_5_);
    }
  }

  @Override
  public int quantityDropped(final int meta, final int fortune, final Random random) {
    if (this.baseBlock == null) {
      return super.quantityDropped(meta, fortune, random);
    }
    return this.baseBlock.quantityDropped(meta, fortune, random);
  }

  @Override
  public int quantityDropped(final Random p_149745_1_) {
    if (this.baseBlock == null) {
      return super.quantityDropped(p_149745_1_);
    }
    return this.baseBlock.quantityDropped(p_149745_1_);
  }

  @Override
  public int quantityDroppedWithBonus(final int p_149679_1_, final Random p_149679_2_) {
    if (this.baseBlock == null) {
      return super.quantityDroppedWithBonus(p_149679_1_, p_149679_2_);
    }

    return this.baseBlock.quantityDroppedWithBonus(p_149679_1_, p_149679_2_);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void randomDisplayTick(final World p_149734_1_, final int p_149734_2_, final int p_149734_3_,
      final int p_149734_4_, final Random p_149734_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.randomDisplayTick(p_149734_1_, p_149734_2_, p_149734_3_, p_149734_4_, p_149734_5_);
    }
  }

  @Override
  public boolean recolourBlock(final World world, final int x, final int y, final int z, final ForgeDirection side,
      final int colour) {
    if (this.baseBlock == null) {
      return super.recolourBlock(world, x, y, z, side, colour);
    }
    return this.baseBlock.recolourBlock(world, x, y, z, side, colour);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(final IIconRegister p_149651_1_) {
    if (this.baseBlock != null) {
      this.baseBlock.registerBlockIcons(p_149651_1_);
    }
  }

  @Override
  @Deprecated
  public boolean removedByPlayer(final World world, final EntityPlayer player, final int x, final int y, final int z) {
    if (this.baseBlock == null) {
      return super.removedByPlayer(world, player, x, y, z);
    }
    return this.baseBlock.removedByPlayer(world, player, x, y, z);
  }

  // @Override
  // public ItemStack getPickBlock(MovingObjectPosition target, world world, int x, int y, int z,
  // EntityPlayer player)
  // {
  // if (baseBlock == null) { return super.getPickBlock(target, world, x, y, z, player); }
  // return baseBlock.getPickBlock(target, world, x, y, z, player);
  // }

  @Override
  public boolean removedByPlayer(final World world, final EntityPlayer player, final int x, final int y, final int z,
      final boolean willHarvest) {
    if (this.baseBlock == null) {
      return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }
    return this.baseBlock.removedByPlayer(world, player, x, y, z, willHarvest);
  }

  @Override
  public boolean renderAsNormalBlock() {
    if (this.baseBlock == null) {
      return super.renderAsNormalBlock();
    }
    return this.baseBlock.renderAsNormalBlock();
  }

  @Override
  public boolean rotateBlock(final World worldObj, final int x, final int y, final int z, final ForgeDirection axis) {
    if (this.baseBlock == null) {
      return super.rotateBlock(worldObj, x, y, z, axis);
    }
    return this.baseBlock.rotateBlock(worldObj, x, y, z, axis);
  }

  @Override
  public void setBedOccupied(final IBlockAccess world, final int x, final int y, final int z,
      final EntityPlayer player, final boolean occupied) {
    if (this.baseBlock != null) {
      this.baseBlock.setBedOccupied(world, x, y, z, player, occupied);
    }
  }

  @Override
  public void setBlockBoundsBasedOnState(final IBlockAccess p_149719_1_, final int p_149719_2_, final int p_149719_3_,
      final int p_149719_4_) {
    if (this.baseBlock != null) {
      this.baseBlock.setBlockBoundsBasedOnState(p_149719_1_, p_149719_2_, p_149719_3_, p_149719_4_);
    }
  }

  @Override
  public void setBlockBoundsForItemRender() {
    if (this.baseBlock != null) {
      this.baseBlock.setBlockBoundsForItemRender();
    }
  }

  @Override
  public Block setBlockUnbreakable() {
    if (this.baseBlock == null) {
      return super.setBlockUnbreakable();
    }
    return this.baseBlock.setBlockUnbreakable();
  }

  @Override
  public Block setCreativeTab(final CreativeTabs p_149647_1_) {
    if (this.baseBlock == null) {
      return super.setCreativeTab(p_149647_1_);
    }
    return this.baseBlock.setCreativeTab(p_149647_1_);
  }

  @Override
  public Block setHardness(final float p_149711_1_) {
    if (this.baseBlock == null) {
      return super.setHardness(p_149711_1_);
    }
    return this.baseBlock.setHardness(p_149711_1_);
  }

  @Override
  public void setHarvestLevel(final String toolClass, final int level) {
    if (this.baseBlock != null) {
      this.baseBlock.setHarvestLevel(toolClass, level);
    }
  }

  @Override
  public void setHarvestLevel(final String toolClass, final int level, final int metadata) {
    if (this.baseBlock != null) {
      this.baseBlock.setHarvestLevel(toolClass, level, metadata);
    }
  }

  @Override
  public Block setLightLevel(final float p_149715_1_) {
    if (this.baseBlock == null) {
      return super.setLightLevel(p_149715_1_);
    }
    return this.baseBlock.setLightLevel(p_149715_1_);
  }

  @Override
  public Block setLightOpacity(final int p_149713_1_) {
    if (this.baseBlock == null) {
      return super.setLightOpacity(p_149713_1_);
    }
    return this.baseBlock.setLightOpacity(p_149713_1_);
  }

  @Override
  public Block setResistance(final float p_149752_1_) {
    if (this.baseBlock == null) {
      return super.setResistance(p_149752_1_);
    }
    return this.baseBlock.setResistance(p_149752_1_);
  }

  @Override
  public Block setStepSound(final Block.SoundType p_149672_1_) {
    if (this.baseBlock == null) {
      return super.setStepSound(p_149672_1_);
    }
    return this.baseBlock.setStepSound(p_149672_1_);
  }

  @Override
  public Block setTickRandomly(final boolean p_149675_1_) {
    if (this.baseBlock == null) {
      return super.setTickRandomly(p_149675_1_);
    }
    return this.baseBlock.setTickRandomly(p_149675_1_);
  }

  @Override
  public boolean shouldCheckWeakPower(final IBlockAccess world, final int x, final int y, final int z, final int side) {
    if (this.baseBlock == null) {
      return super.shouldCheckWeakPower(world, x, y, z, side);
    }
    return this.baseBlock.shouldCheckWeakPower(world, x, y, z, side);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(final IBlockAccess world, final int x, final int y, final int z, final int side) {
    if (this.baseBlock == null) {
      return super.shouldSideBeRendered(world, x, y, z, side);
    }
    return this.baseBlock.shouldSideBeRendered(world, x, y, z, side);
  }

  @Override
  public int tickRate(final World p_149738_1_) {
    if (this.baseBlock == null) {
      return super.tickRate(p_149738_1_);
    }
    return this.baseBlock.tickRate(p_149738_1_);
  }

  @Override
  public void updateTick(final World p_149674_1_, final int p_149674_2_, final int p_149674_3_, final int p_149674_4_,
      final Random p_149674_5_) {
    if (this.baseBlock != null) {
      this.baseBlock.updateTick(p_149674_1_, p_149674_2_, p_149674_3_, p_149674_4_, p_149674_5_);
    }
  }

  @Override
  public void velocityToAddToEntity(final World p_149640_1_, final int p_149640_2_, final int p_149640_3_,
      final int p_149640_4_, final Entity p_149640_5_, final Vec3 p_149640_6_) {
    if (this.baseBlock != null) {
      this.baseBlock
          .velocityToAddToEntity(p_149640_1_, p_149640_2_, p_149640_3_, p_149640_4_, p_149640_5_, p_149640_6_);
    }
  }

  // #endregion
}
