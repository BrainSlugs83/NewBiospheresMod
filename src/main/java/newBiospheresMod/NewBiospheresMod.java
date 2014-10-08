package newBiospheresMod;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = NewBiospheresMod.MODID, version = NewBiospheresMod.VERSION, guiFactory = "newBiospheresMod.ModConfigGuiFactory")
public class NewBiospheresMod
{
	public static final String MODID = "New Biospheres Mod";
	public static final String VERSION = "0.9";

	public static WorldType Biosphere;
	public static Events Events;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		ModConfig.setConfigFile(new Configuration(event.getSuggestedConfigurationFile(), NewBiospheresMod.VERSION));
		ModConfig.updateFile();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		LanguageRegistry.instance().addStringLocalization("generator.biosphere", "Biospheres");
		Biosphere = new BiosphereWorldType("biosphere");

		DimensionManager.unregisterProviderType(0);
		DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);

		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if (eventArgs.modID.equalsIgnoreCase(NewBiospheresMod.MODID))
		{
			ModConfig.updateFile();
		}
	}
}
