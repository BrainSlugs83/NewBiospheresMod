/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import newBiospheresMod.BiomeEntry;
import newBiospheresMod.BiosphereWorldType;
import newBiospheresMod.BlockEntry;
import newBiospheresMod.Helpers.Blx;
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
			cfgFile
				.setCategoryComment(
					Categories.General,
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

	private final static int ORE_ORB_BLOCK_COUNT = 25;
	public final List<BlockEntry> OreOrbBlocks = new ArrayList<BlockEntry>();

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

		return cfgFile.get(Categories.General, "Noise Enabled", defaultNoiseEnabled,
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

		return cfgFile.get(Categories.General, "Scale", defaultScale,
			"The scale of the world to generate.", minScale, maxScale);
	}

	// #endregion

	// #region Block DomeBlock

	private static final Block defaultDomeBlock = Blx.glass;
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

		return cfgFile.get(Categories.Biospheres, "Dome Block", Utils.GetNameOrIdForBlock(defaultDomeBlock),
			"The Block to use for the generated bio-domes.");
	}

	// #endregion

	// #region Block OrbBlock

	private static final Block defaultOrbBlock = Blx.glass;
	private Block orbBlock = defaultDomeBlock;

	public Block getOrbBlock()
	{
		return orbBlock;
	}

	public void setOrbBlock(Block value)
	{
		if (value == null) value = defaultOrbBlock;

		this.orbBlock = value;
	}

	private static Property getOrbBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Categories.OreOrbs, "Ore Orb Shell Block", Utils.GetNameOrIdForBlock(defaultOrbBlock),
			"The Block to use for the shell of the generated Ore Orbs.");
	}

	// #endregion

	// #region Block BridgeSupportBlock

	private static final Block defaultBridgeSupportBlock = Blx.planks;
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

		return cfgFile.get(Categories.General, "Bridge Support Block",
			Utils.GetNameOrIdForBlock(defaultBridgeSupportBlock),
			"The Block to use for bridges between bio-domes and stairways to ore-orbs.");
	}

	// #endregion

	// #region Block BridgeRailBlock

	private static final Block defaultBridgeRailBlock = Blx.fence;
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

		return cfgFile.get(Categories.General, "Bridge Rail Block", Utils.GetNameOrIdForBlock(defaultBridgeRailBlock),
			"The Block to use for the rails on the bridges between bio-domes.");
	}

	// #endregion

	// #region Block OutsideFillerBlock

	private static final Block defaultOutsideFillerBlock = Blx.air;
	private Block outsideFillerBlock = defaultOutsideFillerBlock;

	public Block getOutsideFillerBlock()
	{
		return outsideFillerBlock;
	}

	public void setOutsideFillerBlock(Block value)
	{
		if (value == null) value = defaultOutsideFillerBlock;

		// if (value == Blx.lava) value = Blx.flowing_lava;
		// else if (value == Blx.water) value = Blx.flowing_water;

		outsideFillerBlock = value;
	}

	private static Property getOutsideFillerBlockProperty()
	{
		if (cfgFile == null) { return null; }

		return cfgFile.get(Categories.General, "Outside Filler Block",
			Utils.GetNameOrIdForBlock(defaultOutsideFillerBlock),
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

		return cfgFile.get(Categories.Biospheres, "Tall Grass Enabled", defaultTallGrassEnabled,
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

		return cfgFile.get(Categories.General, "Grid Size", defaultGridSize,
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

		return cfgFile.get(Categories.General, "Bridge Width", defaultBridgeWidth,
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

		return cfgFile.get(Categories.Biospheres, "Sphere Radius (Minimum)", defaultMinSphereRadius,
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

		return cfgFile.get(Categories.Biospheres, "Sphere Radius (Maximum)", defaultMaxSphereRadius,
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

		return cfgFile.get(Categories.OreOrbs, "Ore Orb Radius", defaultOrbRadius,
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

		return cfgFile.get(Categories.Biospheres, "Lake Ratio (Minimum)", defaultMinLakeRatio,
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

		return cfgFile.get(Categories.Biospheres, "Lake Ratio (Maximum)", defaultMaxLakeRatio,
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
		return getOutsideFillerBlock() != Blx.air;
	}

	// #endregion

	// #region Migrations

	private String GetCategoryName(Property input)
	{
		String fallback = null;
		String propName = input.getName();

		for (String catName: cfgFile.getCategoryNames())
		{
			ConfigCategory cat = cfgFile.getCategory(catName);
			if (cat != null)
			{
				if (cat.containsKey(propName))
				{
					if (fallback == null) fallback = catName;
					if (cat.get(propName) == input) { return catName; }
				}
			}
		}

		return fallback;
	}

	private String GetOldWorldProperty(Property input)
	{
		return GetOldWorldProperty(input.getName());
	}

	private String GetOldWorldProperty(String propName)
	{
		return ModConsts.ModId + "." + propName;
	}

	private String GetNewWorldProperty(Property input)
	{
		return GetNewWorldProperty(GetCategoryName(input), input.getName());
	}

	private String GetNewWorldProperty(String category, String propName)
	{
		String result = category + "." + propName;
		result = result.toLowerCase().replace(" ", "");
		return result;
	}

	private static class MigrationEntry
	{
		public final String OldCategory;
		public final String NewCategory;
		public final String PropertyName;

		public MigrationEntry(String oldCategory, String newCategory, String propertyName)
		{
			this.OldCategory = oldCategory;
			this.NewCategory = newCategory;
			this.PropertyName = propertyName;
		}
	}

	private static List<MigrationEntry> migrations = null;

	private synchronized void InitMigrations()
	{
		if (migrations == null)
		{
			migrations = new ArrayList<MigrationEntry>();
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Dome Block"));
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Sphere Radius (Minimum)"));
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Sphere Radius (Maximum)"));
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Lake Ratio (Minimum)"));
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Lake Ratio (Maximum)"));
			migrations.add(new MigrationEntry(Categories.General, Categories.Biospheres, "Tall Grass Enabled"));
			migrations.add(new MigrationEntry(Categories.General, Categories.OreOrbs, "Ore Orb Radius"));
		}
	}

	private void PerformFileMigrations()
	{
		InitMigrations();

		if (cfgFile != null)
		{
			for (MigrationEntry mige: migrations)
			{
				cfgFile.moveProperty(mige.OldCategory, mige.PropertyName, mige.NewCategory);
			}

			if (cfgFile.hasChanged())
			{
				cfgFile.save();
			}
		}
	}

	private void MigrateWorldProperty(GameRules rules, CustomWorldData data, String category, String propertyName)
	{
		if (rules != null && data != null)
		{
			String oldName = GetOldWorldProperty(propertyName);
			if (rules.hasRule(oldName))
			{
				String value = rules.getGameRuleStringValue(oldName);
				data.put(GetNewWorldProperty(category, propertyName), value);
			}
		}
	}

	private void MigrateWorldProperty(CustomWorldData data, String oldCategory, String newCategory, String propertyName)
	{
		if (data != null)
		{
			String oldName = GetNewWorldProperty(oldCategory, propertyName);
			String newName = GetNewWorldProperty(newCategory, propertyName);

			if (data.ContainsKey(oldName))
			{
				String value = data.get(oldName);
				data.RemoveKey(oldName);
				data.put(newName, value);
			}
		}
	}

	private void PerformWorldMigrations()
	{
		InitMigrations();

		if (World == null) { return; }

		CustomWorldData data = CustomWorldData.FromWorld(World);
		if (data == null) { return; }

		GameRules rules = Utils.GetGameRules(this.World);
		if (rules != null)
		{
			String ruleName = ModConsts.ModId + ".Is Biosphere World";
			if (rules.getGameRuleBooleanValue(ruleName))
			{
				data.put(BiosphereWorldType.IsBiosphereWorldKey, true);
			}
		}

		if (rules != null && cfgFile != null)
		{
			for (String catName: cfgFile.getCategoryNames())
			{
				ConfigCategory cat = cfgFile.getCategory(catName);
				for (String propName: cat.getValues().keySet())
				{
					MigrateWorldProperty(rules, data, catName, propName);
				}
			}
		}

		for (MigrationEntry mige: migrations)
		{
			MigrateWorldProperty(data, mige.OldCategory, mige.NewCategory, mige.PropertyName);
		}

		data.setDirty(true);
	}

	// #endregion

	// #region Property Helpers

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

	private static int GetDefaultBiomeWeight(BiomeGenBase biome)
	{
		if (biome == BiomeGenBase.forest) { return 50; }
		if (biome == BiomeGenBase.taiga) { return 40; }
		if (biome == BiomeGenBase.swampland) { return 40; }
		if (biome == BiomeGenBase.hell) { return 10; }
		if (biome == BiomeGenBase.mushroomIsland) { return 5; }
		if (biome == BiomeGenBase.sky) { return 2; }

		return 25;
	}

	private Property GetBiomeEntryProperty(BiomeEntry biomeEntry)
	{
		if (cfgFile == null) { return null; }
		if (biomeEntry == null) { return null; }
		BiomeGenBase biome = biomeEntry.biome;
		if (biome == null) { return null; }

		return cfgFile.get(Categories.BiomeWeights, biome.biomeName, GetDefaultBiomeWeight(biome),
			"The weighted chance that the \"" + biome.biomeName + "\" biome will be generated.", 0, 1000);
	}

	private static BlockEntry GetDefaultOreBlockEntry(int index)
	{
		if (index == 0) return new BlockEntry(Blx.lapis_ore, 5);
		if (index == 1) return new BlockEntry(Blx.emerald_ore, 5);
		if (index == 2) return new BlockEntry(Blx.diamond_ore, 5);
		if (index == 3) return new BlockEntry(Blx.iron_ore, 10);
		if (index == 4) return new BlockEntry(Blx.gold_ore, 10);
		if (index == 5) return new BlockEntry(Blx.coal_ore, 15);
		if (index == 6) return new BlockEntry(Blx.redstone_ore, 15);
		if (index == 7) return new BlockEntry(Blx.quartz_ore, 10);
		if (index == 8) return new BlockEntry(Blx.gravel, 100);
		if (index == 9) return new BlockEntry(Blx.lava, 15);
		if (index == 10) return new BlockEntry(Blx.stone, 310);

		return new BlockEntry(Blx.air, 0);
	}

	private Property GetRandomOreBlockEntryProperty(int index)
	{
		if (cfgFile == null) { return null; }

		BlockEntry be = GetDefaultOreBlockEntry(index);

		String idxStr = Integer.toString(index);
		while (idxStr.length() < 2)
		{
			idxStr = "0" + idxStr;
		}

		return cfgFile.get(Categories.OreOrbs, "Random Ore Block #" + idxStr, be.toString(),
			"The chance that the Ore Orb will produce a given block.  Values have two parts, and are separated by a "
				+ "comma.  The left side of the comma specifies the block name or Id, and the right side of the comma "
				+ "specifies the weighted chance to produce that block inside an Ore Orb.");
	}

	// #endregion

	private ModConfig(World world)
	{
		this.World = world;

		// Setup Defaults
		List<BiomeEntry> entries = new ArrayList<BiomeEntry>();

		for (BiomeGenBase biome: BiomeGenBase.getBiomeGenArray())
		{
			if (biome != null)
			{
				if (!Utils.Any(Utils.Where(entries, SearchFor(biome))))
				{
					entries.add(new BiomeEntry(biome, GetDefaultBiomeWeight(biome)));
				}
			}
		}

		this.AllBiomes = Collections.unmodifiableList(entries);

		update();
	}

	public void update()
	{
		PerformFileMigrations();
		LoadConfigurationFromFile();
		SaveConfigurationToFile();

		PerformWorldMigrations();
		LoadConfigurationFromWorld();
		SaveConfigurationToWorld();
	}

	// #region Load & Save from World

	private void LoadConfigurationFromWorld()
	{
		if (!BiosphereWorldType.IsBiosphereWorld(this.World)) { return; }

		CustomWorldData data = CustomWorldData.FromWorld(this.World);
		if (data == null) { return; }

		String keyName;

		keyName = GetNewWorldProperty(getNoiseEnabledProperty());
		if (data.ContainsKey(keyName))
		{
			setNoiseEnabled(data.getBool(keyName, isNoiseEnabled()));
		}

		keyName = GetNewWorldProperty(getScaleProperty());
		if (data.ContainsKey(keyName))
		{
			setScale(data.getFloat(keyName, getScale()));
		}

		keyName = GetNewWorldProperty(getDomeBlockProperty());
		if (data.ContainsKey(keyName))
		{
			setDomeBlock(data.getBlock(keyName, getDomeBlock()));
		}

		keyName = GetNewWorldProperty(getOrbBlockProperty());
		if (data.ContainsKey(keyName))
		{
			setOrbBlock(data.getBlock(keyName, getOrbBlock()));
		}

		keyName = GetNewWorldProperty(getBridgeSupportBlockProperty());
		if (data.ContainsKey(keyName))
		{
			setBridgeSupportBlock(data.getBlock(keyName, getBridgeSupportBlock()));
		}

		keyName = GetNewWorldProperty(getOutsideFillerBlockProperty());
		if (data.ContainsKey(keyName))
		{
			setOutsideFillerBlock(data.getBlock(keyName, getOutsideFillerBlock()));
		}

		keyName = GetNewWorldProperty(getTallGrassEnabledProperty());
		if (data.ContainsKey(keyName))
		{
			setTallGrassEnabled(data.getBool(keyName, isTallGrassEnabled()));
		}

		keyName = GetNewWorldProperty(getGridSizeProperty());
		if (data.ContainsKey(keyName))
		{
			setGridSize(data.getInt(keyName, getGridSize()));
		}

		keyName = GetNewWorldProperty(getBridgeWidthProperty());
		if (data.ContainsKey(keyName))
		{
			setBridgeWidth(data.getInt(keyName, getBridgeWidth()));
		}

		keyName = GetNewWorldProperty(getMinSphereRadiusProperty());
		if (data.ContainsKey(keyName))
		{
			setMinSphereRadius(data.getDouble(keyName, getMinSphereRadius()));
		}

		keyName = GetNewWorldProperty(getMaxSphereRadiusProperty());
		if (data.ContainsKey(keyName))
		{
			setMaxSphereRadius(data.getDouble(keyName, getMaxSphereRadius()));
		}

		keyName = GetNewWorldProperty(getOrbRadiusProperty());
		if (data.ContainsKey(keyName))
		{
			setOrbRadius(data.getDouble(keyName, getOrbRadius()));
		}

		keyName = GetNewWorldProperty(getMinLakeRatioProperty());
		if (data.ContainsKey(keyName))
		{
			setMinLakeRatio(data.getDouble(keyName, getMinLakeRatio()));
		}

		keyName = GetNewWorldProperty(getMaxLakeRatioProperty());
		if (data.ContainsKey(keyName))
		{
			setMaxLakeRatio(data.getDouble(keyName, getMaxLakeRatio()));
		}

		for (int i = 0; i < ORE_ORB_BLOCK_COUNT; i++)
		{
			Property prop = GetRandomOreBlockEntryProperty(i);

			if (prop != null)
			{
				keyName = GetNewWorldProperty(prop);

				if (data.ContainsKey(keyName))
				{
					BlockEntry value = BlockEntry.Parse(data.get(keyName));

					if (OreOrbBlocks.size() > i)
					{
						OreOrbBlocks.set(i, value);
					}
					else
					{
						OreOrbBlocks.add(value);
					}
				}
			}
		}

		int biomeCount = 0;
		for (BiomeEntry entry: AllBiomes)
		{
			Property prop = GetBiomeEntryProperty(entry);
			if (prop != null)
			{
				entry.itemWeight = data.getInt(GetNewWorldProperty(prop), entry.itemWeight);
				biomeCount += entry.itemWeight;
			}
		}

		if (biomeCount <= 0)
		{
			LoadBiomeWeightsFromFile();
		}
	}

	private void SaveConfigurationToWorld()
	{
		if (!BiosphereWorldType.IsBiosphereWorld(this.World)) { return; }

		CustomWorldData data = CustomWorldData.FromWorld(this.World);
		if (data == null) { return; }

		String keyName;

		keyName = GetNewWorldProperty(getNoiseEnabledProperty());
		data.put(keyName, isNoiseEnabled());

		keyName = GetNewWorldProperty(getScaleProperty());
		data.put(keyName, getScale());

		keyName = GetNewWorldProperty(getDomeBlockProperty());
		data.put(keyName, getDomeBlock());

		keyName = GetNewWorldProperty(getOrbBlockProperty());
		data.put(keyName, getOrbBlock());

		keyName = GetNewWorldProperty(getBridgeSupportBlockProperty());
		data.put(keyName, getBridgeSupportBlock());

		keyName = GetNewWorldProperty(getOutsideFillerBlockProperty());
		data.put(keyName, getOutsideFillerBlock());

		keyName = GetNewWorldProperty(getTallGrassEnabledProperty());
		data.put(keyName, isTallGrassEnabled());

		keyName = GetNewWorldProperty(getGridSizeProperty());
		data.put(keyName, getGridSize());

		keyName = GetNewWorldProperty(getBridgeWidthProperty());
		data.put(keyName, getBridgeWidth());

		keyName = GetNewWorldProperty(getMinSphereRadiusProperty());
		data.put(keyName, getMinSphereRadius());

		keyName = GetNewWorldProperty(getMaxSphereRadiusProperty());
		data.put(keyName, getMaxSphereRadius());

		keyName = GetNewWorldProperty(getOrbRadiusProperty());
		data.put(keyName, getOrbRadius());

		keyName = GetNewWorldProperty(getMinLakeRatioProperty());
		data.put(keyName, getMinLakeRatio());

		keyName = GetNewWorldProperty(getMaxLakeRatioProperty());
		data.put(keyName, getMaxLakeRatio());

		for (int i = 0; i < ORE_ORB_BLOCK_COUNT; i++)
		{
			Property prop = GetRandomOreBlockEntryProperty(i);

			if (prop != null)
			{
				keyName = GetNewWorldProperty(prop);
				String value = "air, 0";

				if (OreOrbBlocks.size() > i)
				{
					value = OreOrbBlocks.get(i).toString();
				}

				data.put(keyName, value);
			}
		}

		for (BiomeEntry entry: AllBiomes)
		{
			Property prop = GetBiomeEntryProperty(entry);
			if (prop != null)
			{
				keyName = GetNewWorldProperty(prop);
				data.put(keyName, entry.itemWeight);
			}
		}
	}

	// #endregion

	// #region Load & Save from File

	private void LoadConfigurationFromFile()
	{
		if (cfgFile == null) { return; }

		this.setNoiseEnabled(getNoiseEnabledProperty().getBoolean());
		this.setScale((float)getScaleProperty().getDouble());
		this.setDomeBlock(Utils.ParseBlock(getDomeBlockProperty().getString(), defaultDomeBlock));
		this.setOrbBlock(Utils.ParseBlock(getOrbBlockProperty().getString(), defaultOrbBlock));
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

		for (int i = 0; i < ORE_ORB_BLOCK_COUNT; i++)
		{
			Property prop = GetRandomOreBlockEntryProperty(i);

			if (prop != null)
			{
				BlockEntry value = BlockEntry.Parse(prop.getString());

				if (OreOrbBlocks.size() > i)
				{
					OreOrbBlocks.set(i, value);
				}
				else
				{
					OreOrbBlocks.add(value);
				}
			}
		}

		int oreCount = 0;
		for (BlockEntry block: OreOrbBlocks)
		{
			oreCount += block.itemWeight;
		}

		if (oreCount <= 0)
		{
			OreOrbBlocks.add(new BlockEntry(Blx.stone, 1));
		}

		LoadBiomeWeightsFromFile();

		if (cfgFile.hasChanged())
		{
			cfgFile.save();
		}
	}

	private void LoadBiomeWeightsFromFile()
	{
		int count = 0;

		if (cfgFile != null)
		{
			for (BiomeEntry entry: AllBiomes)
			{
				Property prop = GetBiomeEntryProperty(entry);
				if (prop != null)
				{
					int weight = prop.getInt(GetDefaultBiomeWeight(entry.biome));
					if (weight < 0)
					{
						weight = 0;
					}

					entry.itemWeight = weight;
					count += weight;
				}
			}
		}

		if (count <= 0)
		{
			for (BiomeEntry entry: AllBiomes)
			{
				entry.itemWeight = GetDefaultBiomeWeight(entry.biome);
			}
		}
	}

	private void SaveConfigurationToFile()
	{
		if (cfgFile == null) { return; }

		getNoiseEnabledProperty().set(isNoiseEnabled());
		getScaleProperty().set(getScale());
		getDomeBlockProperty().set(Utils.GetNameOrIdForBlock(getDomeBlock()));
		getOrbBlockProperty().set(Utils.GetNameOrIdForBlock(getOrbBlock()));
		getBridgeSupportBlockProperty().set(Utils.GetNameOrIdForBlock(getBridgeSupportBlock()));
		getBridgeRailBlockProperty().set(Utils.GetNameOrIdForBlock(getBridgeRailBlock()));
		getOutsideFillerBlockProperty().set(Utils.GetNameOrIdForBlock(getOutsideFillerBlock()));
		getTallGrassEnabledProperty().set(isTallGrassEnabled());
		getGridSizeProperty().set(getGridSize());
		getBridgeWidthProperty().set(getBridgeWidth());
		getMinSphereRadiusProperty().set(getMinSphereRadius());
		getMaxSphereRadiusProperty().set(getMaxSphereRadius());
		getOrbRadiusProperty().set(getOrbRadius());
		getMinLakeRatioProperty().set(getMinLakeRatio());
		getMaxLakeRatioProperty().set(getMaxLakeRatio());

		for (int i = 0; i < ORE_ORB_BLOCK_COUNT; i++)
		{
			Property prop = GetRandomOreBlockEntryProperty(i);

			if (prop != null)
			{
				String value = "air, 0";

				if (OreOrbBlocks.size() > i)
				{
					value = OreOrbBlocks.get(i).toString();
				}

				prop.set(value);
			}
		}

		for (BiomeEntry entry: AllBiomes)
		{
			Property prop = GetBiomeEntryProperty(entry);
			if (prop != null)
			{
				prop.set(GetDefaultBiomeWeight(entry.biome));
			}
		}

		if (cfgFile.hasChanged())
		{
			cfgFile.save();
		}
	}

	// #endregion
}
