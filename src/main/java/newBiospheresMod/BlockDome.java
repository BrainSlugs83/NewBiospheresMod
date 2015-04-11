/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.world.*;
import cpw.mods.fml.common.registry.*;

public class BlockDome extends BlockGlass
{
	private static boolean registered = false;

	public static Block blockDome;

	public static void register()
	{
		if (!registered)
		{
			registered = true;
			blockDome = GameRegistry.registerBlock(new BlockDome(), "glass_dome");
		}
	}

	protected BlockDome()
	{
		super(Material.glass, false);

		setHardness(1.0f);
		setStepSound(soundTypeGlass);

		this.setBlockName("glassDome");
		//setUnlocalizedName("glassDome");
	}

	@Override
	public boolean isFoliage(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}

	@Override
	protected boolean canSilkHarvest()
	{
		return false;
	}
}
