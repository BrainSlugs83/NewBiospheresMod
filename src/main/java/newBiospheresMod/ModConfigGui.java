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
