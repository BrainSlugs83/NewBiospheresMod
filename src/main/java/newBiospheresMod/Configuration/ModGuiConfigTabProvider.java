/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import newbiospheresmod.helpers.Func2;
import newbiospheresmod.helpers.ModConsts;

public class ModGuiConfigTabProvider implements IGuiConfigTabProvider {
  public static final ModGuiConfigTabProvider SingletonInstance = new ModGuiConfigTabProvider();

  private ModGuiConfigTabProvider() {
    // hidden
  }

  @Override
  public boolean getAllRequireMcRestart() {
    return false;
  }

  @Override
  public boolean getAllRequireWorldRestart() {
    return false;
  }

  @Override
  public Iterable<GuiConfigTabEntry> getTabs() {
    final List<GuiConfigTabEntry> tabs = new ArrayList<GuiConfigTabEntry>();

    tabs.add(new GuiConfigTabEntry(Categories.General, "General",
        new Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen>() {
          @Override
          public GuiScreen func(final GuiScreen parent, final IGuiConfigTabProvider provider) {
            return new ConfigScreens.GeneralGuiConfigTab(parent, provider);
          }
        }));

    tabs.add(new GuiConfigTabEntry(Categories.Biospheres, "Biospheres",
        new Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen>() {
          @Override
          public GuiScreen func(final GuiScreen parent, final IGuiConfigTabProvider provider) {
            return new ConfigScreens.BiospheresGuiConfigTab(parent, provider);
          }
        }));

    tabs.add(new GuiConfigTabEntry(Categories.OreOrbs, "Ore Orbs",
        new Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen>() {
          @Override
          public GuiScreen func(final GuiScreen parent, final IGuiConfigTabProvider provider) {
            return new ConfigScreens.OreOrbsGuiConfigTab(parent, provider);
          }
        }));

    // tabs.add(new GuiConfigTabEntry(Categories.OreOrbOreBlocks, "Ore Orbs 2",
    // new Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen>()
    // {
    // @Override
    // public GuiScreen func(GuiScreen parent, IGuiConfigTabProvider provider)
    // {
    // return new ConfigScreens.OreOrbOreBlocksGuiConfigTab(parent, provider);
    // }
    // }));

    tabs.add(new GuiConfigTabEntry(Categories.BiomeWeights, "Biomes",
        new Func2<GuiScreen, IGuiConfigTabProvider, GuiScreen>() {
          @Override
          public GuiScreen func(final GuiScreen parent, final IGuiConfigTabProvider provider) {
            return new ConfigScreens.BiomeWeightsGuiConfigTab(parent, provider);
          }
        }));

    return tabs;
  }

  @Override
  public String getTitle() {
    return ModConsts.ModId;
  }
}
