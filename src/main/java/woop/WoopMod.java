package woop;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = WoopMod.MODID, version = WoopMod.VERSION)
public class WoopMod
{
	public static final String MODID = "WoopMod";
	public static final String VERSION = "Ver. 0";

	public static WorldType Biosphere;
	public static Events Events;

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
	}
}
