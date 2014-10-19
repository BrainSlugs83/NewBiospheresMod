/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Configuration;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

public class ConfigScreens
{
	public static class GeneralGuiConfigTab extends SimpleGuiConfigTab
	{
		private static final String Category = Categories.General;

		public GeneralGuiConfigTab(GuiScreen parent)
		{
			super(parent, Category);
		}

		public GeneralGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
		{
			super(parent, provider, Category);
		}
	}

	public static class BiospheresGuiConfigTab extends SimpleGuiConfigTab
	{
		private static final String Category = Categories.Biospheres;

		public BiospheresGuiConfigTab(GuiScreen parent)
		{
			super(parent, Category);
		}

		public BiospheresGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
		{
			super(parent, provider, Category);
		}
	}

	public static class OreOrbsGuiConfigTab extends SimpleGuiConfigTab
	{
		private static final String Category = Categories.OreOrbs;

		public OreOrbsGuiConfigTab(GuiScreen parent)
		{
			super(parent, Category);
		}

		public OreOrbsGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
		{
			super(parent, provider, Category);
		}
	}

	// public static class OreOrbOreBlocksGuiConfigTab extends SimpleGuiConfigTab
	// {
	// private static final String Category = Categories.OreOrbOreBlocks;
	//
	// public OreOrbOreBlocksGuiConfigTab(GuiScreen parent)
	// {
	// super(parent, Category);
	// }
	//
	// public OreOrbOreBlocksGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
	// {
	// super(parent, provider, Category);
	// }
	// }

	public static class BiomeWeightsGuiConfigTab extends SimpleGuiConfigTab
	{
		private static final String Category = Categories.BiomeWeights;

		public BiomeWeightsGuiConfigTab(GuiScreen parent)
		{
			super(parent, Category);
		}

		public BiomeWeightsGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider)
		{
			super(parent, provider, Category);
		}
	}

	private static abstract class SimpleGuiConfigTab extends GuiConfigTab
	{
		public SimpleGuiConfigTab(GuiScreen parent, String category)
		{
			this(parent, ModGuiConfigTabProvider.SingletonInstance, category);
		}

		public SimpleGuiConfigTab(GuiScreen parent, IGuiConfigTabProvider provider, String category)
		{
			super(parent, provider, category,
				new ConfigElement(ModConfig.getConfigFile().getCategory(category)).getChildElements());
		}
	}

}
