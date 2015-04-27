/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod;

import java.io.File;

import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import newbiospheresmod.configuration.ModConfig;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.Utils;

import com.google.common.io.Files;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = ModConsts.ModId, version = ModConsts.ModVersion, guiFactory = "newBiospheresMod.Configuration.ModConfigGuiFactory")
public class NewBiospheresMod {
  public static WorldType biosphereWorldType;

  @EventHandler
  public void init(final FMLInitializationEvent event) {
    // TODO: Update this to use the new resource localization crap
    LanguageRegistry.instance().addStringLocalization("generator.biosphere", "Biospheres");

    NewBiospheresMod.biosphereWorldType = new BiosphereWorldType("biosphere");

    DimensionManager.unregisterProviderType(0);
    DimensionManager.registerProviderType(0, BiosphereWorldProvider.class, true);

    FMLCommonHandler.instance().bus().register(this);
  }

  @SubscribeEvent
  public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
    if (eventArgs.modID.equalsIgnoreCase(ModConsts.ModId)) {
      ModConfig.updateFile();
    }
  }

  @EventHandler
  public void postInit(final FMLInitializationEvent event) {
    BlockDome.initalizeAllRegisteredBlocks();
  }

  @EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    // Migrate the Configuration File forward to the new Mod Id.

    // TODO: Move this somewhere more fitting
    File configFile = event.getSuggestedConfigurationFile();
    if (!configFile.exists()) {
      final File oldConfigFile = new File(configFile.getParent() + File.separator + ModConsts.OldModId + "."
          + Utils.getFileExtension(configFile.getPath()));
      if (oldConfigFile.exists()) {
        try {
          Files.move(oldConfigFile, configFile);
        } catch (final Exception e) {
          System.out.println("Unable to move config file; attempting to use old config file in place.");
          configFile = oldConfigFile;
        }
      }
    }

    // Load the Configuration File
    ModConfig.setConfigFile(new Configuration(configFile, ModConsts.ModVersion));
    ModConfig.updateFile();
  }
}
