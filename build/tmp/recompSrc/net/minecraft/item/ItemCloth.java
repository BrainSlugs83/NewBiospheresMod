package net.minecraft.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.util.IIcon;

public class ItemCloth extends ItemBlock
{
    private static final String __OBFID = "CL_00000075";

    public ItemCloth(Block p_i45358_1_)
    {
        super(p_i45358_1_);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    /**
     * Gets an icon index based on an item's damage value
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int p_77617_1_)
    {
        return this.field_150939_a.func_149735_b(2, BlockColored.func_150032_b(p_77617_1_));
    }

    /**
     * Returns the metadata of the block which this Item (ItemBlock) can place
     */
    public int getMetadata(int p_77647_1_)
    {
        return p_77647_1_;
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack p_77667_1_)
    {
        return super.getUnlocalizedName() + "." + ItemDye.field_150923_a[BlockColored.func_150032_b(p_77667_1_.getItemDamage())];
    }
}