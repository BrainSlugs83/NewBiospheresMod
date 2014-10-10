/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import newBiospheresMod.BiomeEntry;
import newBiospheresMod.BiosphereWorldType;
import newBiospheresMod.Helpers.Creator;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Predicate;
import newBiospheresMod.Helpers.Utils;

public class ModConfig
{
	// #region Caching

	private static LruCacheList<ModConfig> modConfigs = new LruCacheList<ModConfig>(10, new IKeyProvider<ModConfig>()
	{
		@Override
		public Object provideKey(ModConfig item)
		{
			if (item == null) { return null; }
			return item.World;
		}
	});

	public static ModConfig get(final World world)
	{
		return modConfigs.FindOrAdd(world, new Creator<ModConfig>()
		{
			@Override
			public ModConfig create()
			{
				return new ModConfig(world);
			}
		});
	}

	// #endregion

	// #region Static Fields and Methods

	private static Configuration cfgFile = null;

	public static Configuration getConfigFile()
	{
		return cfgFile;
	}

	public static void setConfigFile(Configuration value)
	{
		cfgFile = value;

		if (cfgFile != null)
		{
			cfgFile.setCategoryComment(
				Configuration.CATEGORY_GENERAL,
				ModConsts.ModId
					+ " "
					+ ModConsts.ModVersion
					+ ": Note, these settings only affect new Worlds; previously created Worlds will persist with their existing settings.");
		}
	}

	public static void updateFile()
	{
		setConfigFile(getConfigFile());
		ModConfig.get(null).update();
	}

	// #endregion

	// #region Fields & Properties

	public final World World;
	public final List<BiomeEntry> AllBiomes;

	// #region boolean NoiseEnabled

	private static final boolean defaultNoiseEnabled = true;
	private boolean noiseEnabled = defaultNoiseEnabled;

	public boolean isNoiseEnabled()
	{
		return noiseEnabled;
	}

	public void setNoiseEnabled(boolean noiseEnabled)
	{
		this.noiseEnabled = noiseEnabled;
	}

	private static Property getNoiseEnabledProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Noise Enabled", defaultNoiseEnabled,
			"Controls whether a noise generator is used to generate terrain heights or if the World should be flat.");
	}

	// #endregion

	// #region float Scale

	private static final float minScale = .2f;
	private static final float maxScale = 10f;
	private static final float defaultScale = 1.0f;
	private float scale = defaultScale;

	public float getScale()
	{
		return scale;
	}

	public void setScale(float value)
	{
		if (value < minScale) value = minScale;
		else if (value > maxScale) value = maxScale;

		this.scale = value;
		this.scaledGridSize = 0;
		this.scaledOrbRadius = 0;
	}

	private static Property getScaleProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Scale", defaultScale,
			"The scale of the world to generate.", minScale, maxScale);
	}

	// #endregion

	// #region Block DomeBlock

	private static final Block defaultDomeBlock = Blocks.glass;
	private Block domeBlock = defaultDomeBlock;

	public Block getDomeBlock()
	{
		return domeBlock;
	}

	public void setDomeBlock(Block value)
	{
		if (value == null) value = defaultDomeBlock;

		this.domeBlock = value;
	}

	private static Property getDomeBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Dome Block", Utils.GetName(defaultDomeBlock),
			"The Block to use for the generated bio-domes.");
	}

	// #endregion

	// #region Block BridgeSupportBlock

	private static final Block defaultBridgeSupportBlock = Blocks.planks;
	private Block bridgeSupportBlock = defaultBridgeSupportBlock;

	public Block getBridgeSupportBlock()
	{
		return bridgeSupportBlock;
	}

	public void setBridgeSupportBlock(Block value)
	{
		if (value == null) value = defaultBridgeSupportBlock;

		this.bridgeSupportBlock = value;
	}

	private static Property getBridgeSupportBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Bridge Support Block",
			Utils.GetName(defaultBridgeSupportBlock),
			"The Block to use for bridges between bio-domes and stairways to ore-orbs.");
	}

	// #endregion

	// #region Block BridgeRailBlock

	private static final Block defaultBridgeRailBlock = Blocks.fence;
	private Block bridgeRailBlock = defaultBridgeRailBlock;

	public Block getBridgeRailBlock()
	{
		return bridgeRailBlock;
	}

	public void setBridgeRailBlock(Block value)
	{
		if (value == null) value = defaultBridgeRailBlock;

		this.bridgeRailBlock = value;
	}

	private static Property getBridgeRailBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Bridge Rail Block", Utils.GetName(defaultBridgeRailBlock),
			"The Block to use for the rails on the bridges between bio-domes.");
	}

	// #endregion

	// #region Block OutsideFillerBlock

	private static final Block defaultOutsideFillerBlock = Blocks.air;
	private Block outsideFillerBlock = defaultOutsideFillerBlock;

	public Block getOutsideFillerBlock()
	{
		return outsideFillerBlock;
	}

	public void setOutsideFillerBlock(Block value)
	{
		if (value == null) value = defaultOutsideFillerBlock;

		// if (value == Blocks.lava) value = Blocks.flowing_lava;
		// else if (value == Blocks.water) value = Blocks.flowing_water;

		outsideFillerBlock = value;
	}

	private static Property getOutsideFillerBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Outside Filler Block",
			Utils.GetName(defaultOutsideFillerBlock),
			"The block used to fill the area outside of the domes [air, water, and lava are good choices].");
	}

	// #endregion

	// #region boolean TallGrassEnabled

	private static final boolean defaultTallGrassEnabled = true;
	private boolean tallGrassEnabled = defaultTallGrassEnabled;

	public boolean isTallGrassEnabled()
	{
		return tallGrassEnabled;
	}

	public void setTallGrassEnabled(boolean tallGrass)
	{
		this.tallGrassEnabled = tallGrass;
	}

	private static Property getTallGrassEnabledProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Tall Grass Enabled", defaultTallGrassEnabled,
			"Controls whether tall grass is generated or not.");
	}

	// #endregion

	// #region int GridSize

	private static final int minGridSize = 5;
	private static final int maxGridSize = 25;
	private static final int defaultGridSize = 9;
	private int gridSize = defaultGridSize;

	public int getGridSize()
	{
		return gridSize;
	}

	public void setGridSize(int value)
	{
		if (value < minGridSize) value = minGridSize;
		else if (value > maxGridSize) value = maxGridSize;

		this.gridSize = value;
		this.scaledGridSize = 0;
	}

	private static Property getGridSizeProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Grid Size", defaultGridSize,
			"The size of the grid (for one sphere and orb) in chunks (pre-scaled)[a 'chunk' is 16 blocks square].",
			minGridSize, maxGridSize);
	}

	// #endregion

	// #region int BridgeWidth

	private static final int minBridgeWidth = 1;
	private static final int maxBridgeWidth = 15;
	private static final int defaultBridgeWidth = 2;
	private int bridgeWidth = defaultBridgeWidth;

	public int getBridgeWidth()
	{
		return bridgeWidth;
	}

	public void setBridgeWidth(int value)
	{
		if (value < minBridgeWidth) value = minBridgeWidth;
		else if (value > maxBridgeWidth) value = maxBridgeWidth;

		this.bridgeWidth = value;
	}

	private static Property getBridgeWidthProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Bridge Width", defaultBridgeWidth,
			"Bridge Width: the width of the bridge [from the center to the edge].", minBridgeWidth, maxBridgeWidth);
	}

	// #endregion

	private static final double sphereRadiusMinimumValue = 15d;
	private static final double sphereRadiusMaximumValue = 80d;

	// #region double MinSphereRadius

	private static final double defaultMinSphereRadius = 20;
	private double minSphereRadius = defaultMinSphereRadius;

	public double getMinSphereRadius()
	{
		return minSphereRadius;
	}

	public void setMinSphereRadius(double value)
	{
		if (value < sphereRadiusMinimumValue) value = sphereRadiusMinimumValue;
		else if (value > sphereRadiusMaximumValue) value = sphereRadiusMaximumValue;

		this.minSphereRadius = value;
	}

	private static Property getMinSphereRadiusProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Sphere Radius (Minimum)", defaultMinSphereRadius,
			"The minimum (pre-scaled) sphere radius to generate.", sphereRadiusMinimumValue, sphereRadiusMaximumValue);
	}

	// #endregion

	// #region double MaxSphereRadius

	private static final double defaultMaxSphereRadius = 50;
	private double maxSphereRadius = defaultMaxSphereRadius;

	public double getMaxSphereRadius()
	{
		return maxSphereRadius;
	}

	public void setMaxSphereRadius(double value)
	{
		if (value < sphereRadiusMinimumValue) value = sphereRadiusMinimumValue;
		else if (value > sphereRadiusMaximumValue) value = sphereRadiusMaximumValue;

		this.maxSphereRadius = value;
	}

	private static Property getMaxSphereRadiusProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Sphere Radius (Maximum)", defaultMaxSphereRadius,
			"The maximum (pre-scaled) sphere radius to generate.", sphereRadiusMinimumValue, sphereRadiusMaximumValue);
	}

	// #endregion

	// #region double OrbRadius

	private static final double minOrbRadius = 1d;
	private static final double maxOrbRadius = 25d;
	private static final double defaultOrbRadius = 7;
	private double orbRadius = defaultOrbRadius;

	public double getOrbRadius()
	{
		return orbRadius;
	}

	public void setOrbRadius(double value)
	{
		if (value < minOrbRadius) value = minOrbRadius;
		else if (value > maxOrbRadius) value = maxOrbRadius;

		this.orbRadius = value;
		this.scaledOrbRadius = 0;
	}

	private static Property getOrbRadiusProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Ore Orb Radius", defaultOrbRadius,
			"The radius (pre-scaled) of the ore orbs to generate.", minOrbRadius, maxOrbRadius);
	}

	// #endregion

	private static final double lakeRatioMinimumValue = 0.1d;
	private static final double lakeRatioMaximumValue = 0.75d;

	// #region double MinLakeRatio

	private static final double defaultMinLakeRatio = 0.3d;
	private double minLakeRatio = defaultMinLakeRatio;

	public double getMinLakeRatio()
	{
		return minLakeRatio;
	}

	public void setMinLakeRatio(double value)
	{
		if (value < lakeRatioMinimumValue) value = lakeRatioMinimumValue;
		else if (value > lakeRatioMaximumValue) value = lakeRatioMaximumValue;

		this.minLakeRatio = value;
	}

	private static Property getMinLakeRatioProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Lake Ratio (Minimum)", defaultMinLakeRatio,
			"The minimum ratio of lake size to sphere size.", lakeRatioMinimumValue, lakeRatioMaximumValue);
	}

	// #endregion

	// #region double MaxLakeRatio

	private static final double defaultMaxLakeRatio = 0.6d;
	private double maxLakeRatio = defaultMaxLakeRatio;

	public double getMaxLakeRatio()
	{
		return maxLakeRatio;
	}

	public void setMaxLakeRatio(double value)
	{
		if (value < lakeRatioMinimumValue) value = lakeRatioMinimumValue;
		else if (value > lakeRatioMaximumValue) value = lakeRatioMaximumValue;

		this.maxLakeRatio = value;
	}

	private static Property getMaxLakeRatioProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Configuration.CATEGORY_GENERAL, "Lake Ratio (Maximum)", defaultMaxLakeRatio,
			"The maximum ratio of lake size to sphere size.", lakeRatioMinimumValue, lakeRatioMaximumValue);
	}

	// #endregion

	// #region int ScaledGridSize

	private int scaledGridSize = 0;

	public int getScaledGridSize()
	{
		if (scaledGridSize == 0)
		{
			scaledGridSize = (int)(gridSize * scale);
		}

		return scaledGridSize;
	}

	// #endregion

	// #region int ScaledOrbRadius

	private int scaledOrbRadius = 0;

	public int getScaledOrbRadius()
	{
		if (scaledOrbRadius == 0)
		{
			scaledOrbRadius = (int)((float)orbRadius * scale);
		}

		return scaledOrbRadius;
	}

	// #endregion

	public boolean doesNeedProtectionGlass()
	{
		return getOutsideFillerBlock() != Blocks.air;
	}

	// #endregion

	private static Predicate<BiomeEntry> SearchFor(final BiomeGenBase biome)
	{
		return new Predicate<BiomeEntry>()
		{
			@Override
			public boolean test(BiomeEntry entry)
			{
				return entry.biome == biome;
			}
		};
	}

	private ModConfig(World world)
	{
		this.World = world;

		// Setup Defaults
		List<BiomeEntry> entries = new ArrayList<BiomeEntry>();
		entries.add(new BiomeEntry(BiomeGenBase.forest, 50));
		entries.add(new BiomeEntry(BiomeGenBase.taiga, 40));
		entries.add(new BiomeEntry(BiomeGenBase.swampland, 40));
		entries.add(new BiomeEntry(BiomeGenBase.hell, 10));
		entries.add(new BiomeEntry(BiomeGenBase.mushroomIsland, 5));
		entries.add(new BiomeEntry(BiomeGenBase.sky, 2));

		for (BiomeGenBase biome: BiomeGenBase.getBiomeGenArray())
		{
			if (biome != null)
			{
				if (!Utils.Any(Utils.Where(entries, SearchFor(biome))))
				{
					entries.add(new BiomeEntry(biome, 25));
				}
			}
		}

		this.AllBiomes = Collections.unmodifiableList(entries);
		update();
	}

	public void update()
	{
		LoadConfigurationFromFile();
		SaveConfigurationToFile();

		LoadConfigurationFromWorld();
		SaveConfigurationToWorld();
	}

	private void LoadConfigurationFromWorld()
	{
		if (!BiosphereWorldType.IsBiosphereWorld(this.World)) { return; }

		GameRules rules = Utils.GetGameRules(this.World);
		if (rules != null)
		{
			String ruleName = ModConsts.ModId + "." + getNoiseEnabledProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setNoiseEnabled(rules.getGameRuleBooleanValue(ruleName));
			}

			ruleName = ModConsts.ModId + "." + getScaleProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setScale(Float.parseFloat(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getDomeBlockProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setDomeBlock(Utils.ParseBlock(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getBridgeSupportBlockProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setBridgeSupportBlock(Utils.ParseBlock(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getOutsideFillerBlockProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setOutsideFillerBlock(Utils.ParseBlock(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getTallGrassEnabledProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setTallGrassEnabled(rules.getGameRuleBooleanValue(ruleName));
			}

			ruleName = ModConsts.ModId + "." + getGridSizeProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setGridSize(Integer.parseInt(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getBridgeWidthProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setBridgeWidth(Integer.parseInt(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getMinSphereRadiusProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setMinSphereRadius(Double.parseDouble(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getMaxSphereRadiusProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setMaxSphereRadius(Double.parseDouble(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getOrbRadiusProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setOrbRadius(Double.parseDouble(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getMinLakeRatioProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setMinLakeRatio(Double.parseDouble(rules.getGameRuleStringValue(ruleName)));
			}

			ruleName = ModConsts.ModId + "." + getMaxLakeRatioProperty().getName();
			if (rules.hasRule(ruleName))
			{
				setMaxLakeRatio(Double.parseDouble(rules.getGameRuleStringValue(ruleName)));
			}
		}
	}

	private void SaveConfigurationToWorld()
	{
		if (!BiosphereWorldType.IsBiosphereWorld(this.World)) { return; }

		GameRules rules = Utils.GetGameRules(this.World);
		if (rules != null)
		{
			String ruleName = ModConsts.ModId + "." + getNoiseEnabledProperty().getName();
			rules.setOrCreateGameRule(ruleName, Boolean.toString(isNoiseEnabled()));

			ruleName = ModConsts.ModId + "." + getScaleProperty().getName();
			rules.setOrCreateGameRule(ruleName, Float.toString(getScale()));

			ruleName = ModConsts.ModId + "." + getDomeBlockProperty().getName();
			rules.setOrCreateGameRule(ruleName, Utils.GetName(getDomeBlock()));

			ruleName = ModConsts.ModId + "." + getBridgeSupportBlockProperty().getName();
			rules.setOrCreateGameRule(ruleName, Utils.GetName(getBridgeSupportBlock()));

			ruleName = ModConsts.ModId + "." + getOutsideFillerBlockProperty().getName();
			rules.setOrCreateGameRule(ruleName, Utils.GetName(getOutsideFillerBlock()));

			ruleName = ModConsts.ModId + "." + getTallGrassEnabledProperty().getName();
			rules.setOrCreateGameRule(ruleName, Boolean.toString(isTallGrassEnabled()));

			ruleName = ModConsts.ModId + "." + getGridSizeProperty().getName();
			rules.setOrCreateGameRule(ruleName, Integer.toString(getGridSize()));

			ruleName = ModConsts.ModId + "." + getBridgeWidthProperty().getName();
			rules.setOrCreateGameRule(ruleName, Integer.toString(getBridgeWidth()));

			ruleName = ModConsts.ModId + "." + getMinSphereRadiusProperty().getName();
			rules.setOrCreateGameRule(ruleName, Double.toString(getMinSphereRadius()));

			ruleName = ModConsts.ModId + "." + getMaxSphereRadiusProperty().getName();
			rules.setOrCreateGameRule(ruleName, Double.toString(getMaxSphereRadius()));

			ruleName = ModConsts.ModId + "." + getOrbRadiusProperty().getName();
			rules.setOrCreateGameRule(ruleName, Double.toString(getOrbRadius()));

			ruleName = ModConsts.ModId + "." + getMinLakeRatioProperty().getName();
			rules.setOrCreateGameRule(ruleName, Double.toString(getMinLakeRatio()));

			ruleName = ModConsts.ModId + "." + getMaxLakeRatioProperty().getName();
			rules.setOrCreateGameRule(ruleName, Double.toString(getMaxLakeRatio()));
		}
	}

	private void LoadConfigurationFromFile()
	{
		if (cfgFile == null) { return; }

		this.setNoiseEnabled(getNoiseEnabledProperty().getBoolean());
		this.setScale((float)getScaleProperty().getDouble());
		this.setDomeBlock(Utils.ParseBlock(getDomeBlockProperty().getString(), defaultDomeBlock));
		this.setBridgeSupportBlock(Utils.ParseBlock(getBridgeSupportBlockProperty().getString(),
			defaultBridgeSupportBlock));
		this.setBridgeRailBlock(Utils.ParseBlock(getBridgeRailBlockProperty().getString(), defaultBridgeRailBlock));
		this.setOutsideFillerBlock(Utils.ParseBlock(getOutsideFillerBlockProperty().getString(),
			defaultOutsideFillerBlock));
		this.setTallGrassEnabled(getTallGrassEnabledProperty().getBoolean());
		this.setGridSize(getGridSizeProperty().getInt());
		this.setBridgeWidth(getBridgeWidthProperty().getInt());
		this.setMinSphereRadius(getMinSphereRadiusProperty().getDouble());
		this.setMaxSphereRadius(getMaxSphereRadiusProperty().getDouble());
		this.setOrbRadius(getOrbRadiusProperty().getDouble());
		this.setMinLakeRatio(getMinLakeRatioProperty().getDouble());
		this.setMaxLakeRatio(getMaxLakeRatioProperty().getDouble());

		if (cfgFile.hasChanged())
		{
			cfgFile.save();
		}
	}

	private void SaveConfigurationToFile()
	{
		if (cfgFile == null) { return; }

		getNoiseEnabledProperty().set(isNoiseEnabled());
		getScaleProperty().set(getScale());
		getDomeBlockProperty().set(Utils.GetName(getDomeBlock()));
		getBridgeSupportBlockProperty().set(Utils.GetName(getBridgeSupportBlock()));
		getBridgeRailBlockProperty().set(Utils.GetName(getBridgeRailBlock()));
		getOutsideFillerBlockProperty().set(Utils.GetName(getOutsideFillerBlock()));
		getTallGrassEnabledProperty().set(isTallGrassEnabled());
		getGridSizeProperty().set(getGridSize());
		getBridgeWidthProperty().set(getBridgeWidth());
		getMinSphereRadiusProperty().set(getMinSphereRadius());
		getMaxSphereRadiusProperty().set(getMaxSphereRadius());
		getOrbRadiusProperty().set(getOrbRadius());
		getMinLakeRatioProperty().set(getMinLakeRatio());
		getMaxLakeRatioProperty().set(getMaxLakeRatio());

		if (cfgFile.hasChanged())
		{
			cfgFile.save();
		}
	}
}
