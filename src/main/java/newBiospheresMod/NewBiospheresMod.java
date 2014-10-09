package newBiospheresMod;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Models.ModConfig;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = ModConsts.ModId, version = ModConsts.ModVersion, guiFactory = "newBiospheresMod.ModConfigGuiFactory")
public class NewBiospheresMod
{
	public static WorldType biosphereWorldType;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		ModConfig.setConfigFile(new Configuration(event.getSuggestedConfigurationFile(), ModConsts.ModVersion));
		ModConfig.updateFile();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		// TODO: Update this to use the new resource localization crap
		LanguageRegistry.instance().addStringLocalization("generator.biosphere", "Biospheres");

		biosphereWorldType = new BiosphereWorldType("biosphere");

		DimensionManager.unregisterProviderType(0);
		DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);

		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if (eventArgs.modID.equalsIgnoreCase(ModConsts.ModId))
		{
			ModConfig.updateFile();
		}
	}
}
