package woop;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.ModClassLoader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

import java.util.HashMap;

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

		
		//MinecraftForge.EVENT_BUS.register(this);
		
		DimensionManager.unregisterProviderType(0);
		DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);
	}
	
//	@SubscribeEvent
//	public void EntityJoinWorldEvent(EntityJoinWorldEvent e)
//	{
//		if (e != null)
//		{
//			TryFixSpawnLocation(e.entity);
//		}
//	}
//
//	@SubscribeEvent
//	public void LivingUpdateEvent(LivingEvent.LivingUpdateEvent e)
//	{
//		if (e != null)
//		{
//			TryFixSpawnLocation(e.entity);
//		}
//	}
}
