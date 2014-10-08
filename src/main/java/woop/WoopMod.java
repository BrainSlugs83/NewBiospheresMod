package woop;

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

@Mod(modid = WoopMod.MODID, version = WoopMod.VERSION, guiFactory = "woop.ModConfigGuiFactory")
public class WoopMod
{
	public static final String MODID = "WoopMod";
	public static final String VERSION = "1.0";

	public static WorldType Biosphere;
	public static Events Events;

	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		ModConfig.setConfigFile(new Configuration(event.getSuggestedConfigurationFile(), WoopMod.VERSION));
		ModConfig.updateFile();
	}

	@EventHandler
	public void Init(FMLInitializationEvent event)
	{
		// some example code
		// System.out.println("DIRT BLOCK >> " +
		// Blocks.dirt.getUnlocalizedName());

		// GameRegistry.addShapelessRecipe(new ItemStack(Blocks.diamond_block),
		// new ItemStack(Blocks.dirt));

		LanguageRegistry.instance().addStringLocalization("generator.biosphere", "Biospheres");
		Biosphere = new BiosphereWorldType("biosphere");

		// MinecraftForge.EVENT_BUS.register(this);

		DimensionManager.unregisterProviderType(0);
		DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);

		FMLCommonHandler.instance().bus().register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent eventArgs)
	{
		if (eventArgs.modID.equalsIgnoreCase(WoopMod.MODID))
		{
			ModConfig.updateFile();
		}
	}
}
