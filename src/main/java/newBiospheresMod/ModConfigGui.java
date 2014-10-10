/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Models.ModConfig;
import cpw.mods.fml.client.config.GuiConfig;

public class ModConfigGui extends GuiConfig
{
	public ModConfigGui(GuiScreen parent)
	{
		super(
			parent,
			new ConfigElement(ModConfig.getConfigFile().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
			ModConsts.ModId, false, false, GuiConfig.getAbridgedConfigPath(ModConfig.getConfigFile().toString()));
	}
}
