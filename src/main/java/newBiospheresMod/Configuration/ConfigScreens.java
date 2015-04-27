/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

public class ConfigScreens {
  public static class BiomeWeightsGuiConfigTab extends SimpleGuiConfigTab {
    private static final String Category = Categories.BiomeWeights;

    public BiomeWeightsGuiConfigTab(final GuiScreen parent) {
      super(parent, BiomeWeightsGuiConfigTab.Category);
    }

    public BiomeWeightsGuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider) {
      super(parent, provider, BiomeWeightsGuiConfigTab.Category);
    }
  }

  public static class BiospheresGuiConfigTab extends SimpleGuiConfigTab {
    private static final String Category = Categories.Biospheres;

    public BiospheresGuiConfigTab(final GuiScreen parent) {
      super(parent, BiospheresGuiConfigTab.Category);
    }

    public BiospheresGuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider) {
      super(parent, provider, BiospheresGuiConfigTab.Category);
    }
  }

  public static class GeneralGuiConfigTab extends SimpleGuiConfigTab {
    private static final String Category = Categories.General;

    public GeneralGuiConfigTab(final GuiScreen parent) {
      super(parent, GeneralGuiConfigTab.Category);
    }

    public GeneralGuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider) {
      super(parent, provider, GeneralGuiConfigTab.Category);
    }
  }

  // public static class OreOrbOreBlocksGuiConfigTab extends SimpleGuiConfigTab
  // {
  // private static final String category = Categories.OreOrbOreBlocks;
  //
  // public OreOrbOreBlocksGuiConfigTab(GuiScreen parent)
  // {
  // super(parent, category);
  // }
  //
  // public OreOrbOreBlocksGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
  // {
  // super(parent, provider, category);
  // }
  // }

  public static class OreOrbsGuiConfigTab extends SimpleGuiConfigTab {
    private static final String Category = Categories.OreOrbs;

    public OreOrbsGuiConfigTab(final GuiScreen parent) {
      super(parent, OreOrbsGuiConfigTab.Category);
    }

    public OreOrbsGuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider) {
      super(parent, provider, OreOrbsGuiConfigTab.Category);
    }
  }

  private abstract static class SimpleGuiConfigTab extends GuiConfigTab {
    public SimpleGuiConfigTab(final GuiScreen parent, final IGuiConfigTabProvider provider, final String category) {
      super(parent, provider, category, new ConfigElement(ModConfig.getConfigFile().getCategory(category))
          .getChildElements());
    }

    public SimpleGuiConfigTab(final GuiScreen parent, final String category) {
      this(parent, ModGuiConfigTabProvider.SingletonInstance, category);
    }
  }

}
