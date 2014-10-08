package newBiospheresMod;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.config.GuiConfig;

public class ModConfigGui extends GuiConfig
{
	public ModConfigGui(GuiScreen parent)
	{
		super(
			parent,
			new ConfigElement(ModConfig.getConfigFile().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
			NewBiospheresMod.MODID, false, false, GuiConfig.getAbridgedConfigPath(ModConfig.getConfigFile().toString()));
	}
}
