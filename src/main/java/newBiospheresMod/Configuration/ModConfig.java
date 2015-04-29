/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import newbiospheresmod.BiomeEntry;
import newbiospheresmod.BiosphereWorldType;
import newbiospheresmod.BlockData;
import newbiospheresmod.BlockEntry;
import newbiospheresmod.helpers.Blx;
import newbiospheresmod.helpers.Creator;
import newbiospheresmod.helpers.IKeyProvider;
import newbiospheresmod.helpers.LruCacheList;
import newbiospheresmod.helpers.ModConsts;
import newbiospheresmod.helpers.Predicate;
import newbiospheresmod.helpers.Utils;

public class ModConfig {
  // #region Caching

  private class BlockWorldProperty extends WorldProperty<BlockData> {
    BlockWorldProperty(final Property property, final BlockData currentValue, final BlockData defaultValue) {
      super(property, currentValue, defaultValue);
    }

    @Override
    protected String convert(final BlockData input, final CustomWorldData data) {
      return input == null ? BlockData.Empty.toString() : input.toString();
    }

    @Override
    protected BlockData convert(final String input, final CustomWorldData data) throws Throwable {
      return BlockData.parse(input, this.getFallbackValue(data));
    }
  }

  private class BooleanWorldProperty extends WorldProperty<Boolean> {
    BooleanWorldProperty(final Property property, final Boolean currentValue, final Boolean defaultValue) {
      super(property, currentValue, defaultValue);
    }

    @Override
    protected String convert(final Boolean input, final CustomWorldData data) {
      return Boolean.toString(input);
    }

    @Override
    protected Boolean convert(final String input, final CustomWorldData data) throws Throwable {
      return Boolean.parseBoolean(input);
    }
  }

  // #endregion

  // #region Static Fields and Methods

  private class DoubleWorldProperty extends WorldProperty<Double> {
    DoubleWorldProperty(final Property property, final Double currentValue, final Double defaultValue) {
      super(property, currentValue, defaultValue);
    }

    @Override
    protected String convert(final Double input, final CustomWorldData data) {
      return Double.toString(input);
    }

    @Override
    protected Double convert(final String input, final CustomWorldData data) throws Throwable {
      return Double.parseDouble(input);
    }
  }

  private class FloatWorldProperty extends WorldProperty<Float> {
    FloatWorldProperty(final Property property, final Float currentValue, final Float defaultValue) {
      super(property, currentValue, defaultValue);
    }

    @Override
    protected String convert(final Float input, final CustomWorldData data) {
      return Float.toString(input);
    }

    @Override
    protected Float convert(final String input, final CustomWorldData data) throws Throwable {
      return Float.parseFloat(input);
    }
  }

  private class IntegerWorldProperty extends WorldProperty<Integer> {
    IntegerWorldProperty(final Property property, final Integer currentValue, final Integer defaultValue) {
      super(property, currentValue, defaultValue);
    }

    @Override
    protected String convert(final Integer input, final CustomWorldData data) {
      return Integer.toString(input);
    }

    @Override
    protected Integer convert(final String input, final CustomWorldData data) throws Throwable {
      return Integer.parseInt(input);
    }
  }

  private abstract static class MigrationAction {
    public abstract void performConfigMigration(Configuration cfgFile);

    public abstract void performWorldMigration(CustomWorldData data, Configuration cfgFile);
  }

  // #endregion

  // #region Read/Write Delegates

  private static class PropertyMovedMigration extends MigrationAction {
    public final String newCategory;
    public final String oldCategory;
    public final String propertyName;

    public PropertyMovedMigration(final String oldCategory, final String newCategory, final String propertyName) {
      this.oldCategory = oldCategory;
      this.newCategory = newCategory;
      this.propertyName = propertyName;
    }

    @Override
    public void performConfigMigration(final Configuration cfgFile) {
      cfgFile.moveProperty(this.oldCategory, this.propertyName, this.newCategory);
    }

    @Override
    public void performWorldMigration(final CustomWorldData data, final Configuration cfgFile) {
      ModConfig.migrateWorldProperty(data, this.oldCategory, this.newCategory, this.propertyName, this.propertyName);
    }
  }

  private static class PropertyRenamedMigration extends MigrationAction {
    public final String category;
    public final String newPropertyName;
    public final String oldPropertyName;

    public PropertyRenamedMigration(final String category, final String oldPropertyName, final String newPropertyName) {
      this.category = category;
      this.oldPropertyName = oldPropertyName;
      this.newPropertyName = newPropertyName;
    }

    @Override
    public void performConfigMigration(final Configuration cfgFile) {
      if (cfgFile.hasCategory(this.category)) {
        if (cfgFile.hasKey(this.category, this.oldPropertyName)) {
          cfgFile.renameProperty(this.category, this.oldPropertyName, this.newPropertyName);
        }
      }
    }

    @Override
    public void performWorldMigration(final CustomWorldData data, final Configuration cfgFile) {
      ModConfig.migrateWorldProperty(data, this.category, this.category, this.oldPropertyName, this.newPropertyName);
    }
  }

  private abstract class WorldProperty<T> {
    public final T currentValue;
    public final T defaultValue;
    public final Property property;

    WorldProperty(final Property property, final T currentValue, final T defaultValue) {
      this.property = property;
      this.currentValue = currentValue;
      this.defaultValue = defaultValue;
    }

    protected abstract T convert(String input, CustomWorldData data) throws Throwable;

    protected abstract String convert(T input, CustomWorldData data);

    protected T getFallbackValue(final CustomWorldData data) {
      if (data == null) {
        return this.currentValue;
      }
      return data.getIsNew() ? this.currentValue : this.defaultValue;
    }

    public T readWorldValue(final CustomWorldData data) {
      if (data != null) {
        final String keyName = ModConfig.this.getNewWorldProperty(this.property);

        if (data.containsKey(keyName)) {
          try {
            return this.convert(data.get(keyName), data);
          } catch (final Throwable ignore) {
            // ignore
          }
        }
      }
      return this.getFallbackValue(data);
    }

    public void writeWorldValue(final CustomWorldData data) {
      if (data != null) {
        final String keyName = ModConfig.this.getNewWorldProperty(this.property);
        data.put(keyName, this.convert(this.currentValue, data));
      }
    }
  }

  private static final int BLOCK_COUNT = 20;

  private static Configuration cfgFile = null;

  private static final BlockData defaultBridgeRailBlock = new BlockData(Blx.fence);

  // #endregion

  // #region Fields & Properties

  private static final BlockData defaultBridgeSupportBlock = new BlockData(Blx.planks);
  private static final int defaultBridgeWidth = 2;

  private static final int defaultGridSize = 9;
  private static final double defaultMaxLakeRatio = 0.6d;
  private static final double defaultMaxSphereRadius = 50;

  private static final double defaultMinLakeRatio = 0.3d;
  private static final double defaultMinSphereRadius = 20;
  private static final boolean defaultNoiseEnabled = true;

  // #region boolean NoiseEnabled

  private static final BlockData defaultOrbBlock = new BlockData(Blx.glass);
  private static final double defaultOrbRadius = 7;

  private static final BlockData defaultOutsideFillerBlock = new BlockData(Blx.air);

  private static final float defaultScale = 1.0f;

  private static final int defaultSeaLevel = 63;

  private static final boolean defaultTallGrassEnabled = true;

  // #endregion

  // #region float Scale

  public static final int DOMETYPE_BLOCK_COUNT = 4;
  public static final int DOMETYPE_COUNT = 4;
  public static final List<BlockEntry>[] DomeBlocks = new ArrayList[ModConfig.DOMETYPE_COUNT];
  private static final double lakeRatioMaximumValue = 0.75d;

  private static final double lakeRatioMinimumValue = 0.1d;

  private static final int maxBridgeWidth = 15;

  private static final int maxGridSize = 25;

  private static final double maxOrbRadius = 25d;

  // #endregion

  // #region block OrbBlock

  private static final float maxScale = 10f;
  private static List<MigrationAction> migrations = null;

  private static final int minBridgeWidth = 1;

  private static final int minGridSize = 5;

  private static final double minOrbRadius = 1d;

  private static final float minScale = .2f;

  // #endregion

  // #region block BridgeSupportBlock

  private static LruCacheList<ModConfig> modConfigs = new LruCacheList<ModConfig>(10, new IKeyProvider<ModConfig>() {
    @Override
    public Object provideKey(final ModConfig item) {
      if (item == null) {
        return null;
      }
      return item.world;
    }
  });
  private static final int seaLevelMaximumValue = 111;

  private static final int seaLevelMinimumValue = 15;

  private static final double sphereRadiusMaximumValue = 80d;

  private static final double sphereRadiusMinimumValue = 15d;

  public static ModConfig get(final World world) {
    return ModConfig.modConfigs.findOrAdd(world, new Creator<ModConfig>() {
      @Override
      public ModConfig create() {
        return new ModConfig(world);
      }
    });
  }

  // #endregion

  // #region block BridgeRailBlock

  private static Property getBridgeRailBlockProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Bridge Rail block", ModConfig.defaultBridgeRailBlock.toString(),
        "The block to use for the rails on the bridges between bio-domes.");
  }

  private static Property getBridgeSupportBlockProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Bridge Support block",
        ModConfig.defaultBridgeSupportBlock.toString(),
        "The block to use for bridges between bio-domes and stairways to ore-orbs.");
  }

  private static Property getBridgeWidthProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Bridge Width", ModConfig.defaultBridgeWidth,
        "Bridge Width: the width of the bridge [from the center to the edge].", ModConfig.minBridgeWidth,
        ModConfig.maxBridgeWidth);
  }

  private static String getCategoryName(final Property input, final Configuration cfgFile) {
    String fallback = null;
    final String propName = input.getName();

    for (final String catName : cfgFile.getCategoryNames()) {
      final ConfigCategory cat = cfgFile.getCategory(catName);
      if (cat != null) {
        if (cat.containsKey(propName)) {
          if (fallback == null) {
            fallback = catName;
          }
          if (cat.get(propName) == input) {
            return catName;
          }
        }
      }
    }

    return fallback;
  }

  public static Configuration getConfigFile() {
    return ModConfig.cfgFile;
  }

  private static int getDefaultBiomeWeight(final BiomeGenBase biome) {
    if (biome == BiomeGenBase.forest) {
      return 50;
    }
    if (biome == BiomeGenBase.taiga) {
      return 40;
    }
    if (biome == BiomeGenBase.swampland) {
      return 40;
    }
    if (biome == BiomeGenBase.hell) {
      return 10;
    }
    if (biome == BiomeGenBase.mushroomIsland) {
      return 5;
    }
    if (biome == BiomeGenBase.sky) {
      return 2;
    }

    return 25;
  }

  // #endregion

  // #region block OutsideFillerBlock

  private static BlockEntry getDefaultDomeBlockProperty(final int domeTypeIndex, final int blockIndex) {
    if ((blockIndex == 0) && (domeTypeIndex == 0)) {
      return new BlockEntry(Blx.glass, 0, 10);
    }
    return new BlockEntry(Blx.air, 0, 0);
  }

  private static BlockEntry getDefaultOreBlockEntry(final int index) {
    if (index == 0) {
      return new BlockEntry(Blx.lapis_ore, 0, 5);
    }
    if (index == 1) {
      return new BlockEntry(Blx.emerald_ore, 0, 5);
    }
    if (index == 2) {
      return new BlockEntry(Blx.diamond_ore, 0, 5);
    }
    if (index == 3) {
      return new BlockEntry(Blx.iron_ore, 0, 10);
    }
    if (index == 4) {
      return new BlockEntry(Blx.gold_ore, 0, 10);
    }
    if (index == 5) {
      return new BlockEntry(Blx.coal_ore, 0, 15);
    }
    if (index == 6) {
      return new BlockEntry(Blx.redstone_ore, 0, 15);
    }
    if (index == 7) {
      return new BlockEntry(Blx.quartz_ore, 0, 10);
    }
    if (index == 8) {
      return new BlockEntry(Blx.gravel, 0, 100);
    }
    if (index == 9) {
      return new BlockEntry(Blx.lava, 0, 15);
    }
    if (index == 10) {
      return new BlockEntry(Blx.stone, 0, 310);
    }

    return new BlockEntry(Blx.air, 0, 0);
  }

  private static BlockEntry getDefaultStairBlockEntry(final int index) {
    if (index == 0) {
      return new BlockEntry(Blx.planks, 0, 50);
    }
    if (index == 1) {
      return new BlockEntry(Blx.air, 0, 50);
    }

    return new BlockEntry(Blx.air, 0, 0);
  }

  private static Property getGridSizeProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Grid Size", ModConfig.defaultGridSize,
        "The size of the grid (for one sphere and orb) in chunks (pre-scaled)[a 'chunk' is 16 blocks square].",
        ModConfig.minGridSize, ModConfig.maxGridSize);
  }

  private static Property getMaxLakeRatioProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.Biospheres, "Lake Ratio (Maximum)", ModConfig.defaultMaxLakeRatio,
        "The maximum ratio of lake size to sphere size.", ModConfig.lakeRatioMinimumValue,
        ModConfig.lakeRatioMaximumValue);
  }

  private static Property getMaxSphereRadiusProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.Biospheres, "Sphere Radius (Maximum)", ModConfig.defaultMaxSphereRadius,
        "The maximum (pre-scaled) sphere radius to generate.", ModConfig.sphereRadiusMinimumValue,
        ModConfig.sphereRadiusMaximumValue);
  }

  // #endregion

  // #region boolean TallGrassEnabled

  private static Property getMinLakeRatioProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.Biospheres, "Lake Ratio (Minimum)", ModConfig.defaultMinLakeRatio,
        "The minimum ratio of lake size to sphere size.", ModConfig.lakeRatioMinimumValue,
        ModConfig.lakeRatioMaximumValue);
  }

  private static Property getMinSphereRadiusProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.Biospheres, "Sphere Radius (Minimum)", ModConfig.defaultMinSphereRadius,
        "The minimum (pre-scaled) sphere radius to generate.", ModConfig.sphereRadiusMinimumValue,
        ModConfig.sphereRadiusMaximumValue);
  }

  private static String getNewWorldProperty(final Property input, final Configuration cfgFile) {
    return ModConfig.getNewWorldProperty(ModConfig.getCategoryName(input, cfgFile), input.getName());
  }

  private static String getNewWorldProperty(final String category, final String propName) {
    String result = category + "." + propName;
    result = result.toLowerCase().replace(" ", "");
    return result;
  }

  private static Property getNoiseEnabledProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Noise Enabled", ModConfig.defaultNoiseEnabled,
        "Controls whether a noise generator is used to generate terrain heights or if the world should be flat.");
  }

  private static String getOldWorldProperty(final String propName) {
    return ModConsts.ModId + "." + propName;
  }

  // #endregion

  // #region int GridSize

  private static Property getOrbBlockProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.OreOrbs, "Ore Orb Shell block", ModConfig.defaultOrbBlock.toString(),
        "The block to use for the shell of the generated Ore Orbs.");
  }

  private static Property getOrbRadiusProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.OreOrbs, "Ore Orb Radius", ModConfig.defaultOrbRadius,
        "The radius (pre-scaled) of the ore orbs to generate.", ModConfig.minOrbRadius, ModConfig.maxOrbRadius);
  }

  private static Property getOutsideFillerBlockProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Outside Filler block",
        ModConfig.defaultOutsideFillerBlock.toString(),
        "The block used to fill the area outside of the domes [air, water, and lava are good choices].");
  }

  private static Property getScaleProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Scale", ModConfig.defaultScale,
        "The scale of the world to generate.", ModConfig.minScale, ModConfig.maxScale);
  }

  private static Property getSeaLevelProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.General, "Sea Level", ModConfig.defaultSeaLevel,
        "Sea Level (the default vertical center of the Biospheres).", ModConfig.seaLevelMinimumValue,
        ModConfig.seaLevelMaximumValue);
  }

  private static Property getTallGrassEnabledProperty() {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.Biospheres, "Tall Grass Enabled", ModConfig.defaultTallGrassEnabled,
        "Controls whether tall grass is generated or not.");
  }

  private static void migrateWorldProperty(final CustomWorldData data, final String oldCategory,
      final String newCategory, final String oldPropertyName, final String newPropertyName) {
    if (data != null) {
      final String oldName = ModConfig.getNewWorldProperty(oldCategory, oldPropertyName);
      final String newName = ModConfig.getNewWorldProperty(newCategory, newPropertyName);

      if (data.containsKey(oldName)) {
        final String value = data.get(oldName);
        data.removeKey(oldName);
        data.put(newName, value);
      }
    }
  }

  private static void migrateWorldProperty(final GameRules rules, final CustomWorldData data, final String category,
      final String propertyName) {
    if ((rules != null) && (data != null)) {
      final String oldName = ModConfig.getOldWorldProperty(propertyName);
      if (rules.hasRule(oldName)) {
        final String value = rules.getGameRuleStringValue(oldName);
        data.put(ModConfig.getNewWorldProperty(category, propertyName), value);
      }
    }
  }

  // #endregion

  // #region int BridgeWidth

  private static Predicate<BiomeEntry> searchFor(final BiomeGenBase biome) {
    return new Predicate<BiomeEntry>() {
      @Override
      public boolean test(final BiomeEntry entry) {
        return entry.biome == biome;
      }
    };
  }

  public static void setConfigFile(final Configuration value) {
    ModConfig.cfgFile = value;

    if (ModConfig.cfgFile != null) {
      ModConfig.cfgFile
          .setCategoryComment(
              Categories.General,
              ModConsts.ModId
                  + " "
                  + ModConsts.ModVersion
                  + ": Note, these settings only affect new Worlds; previously created Worlds will persist with their existing settings.");
    }
  }

  public static void updateFile() {
    ModConfig.setConfigFile(ModConfig.getConfigFile());
    ModConfig.get(null).update();
  }

  public final List<BiomeEntry> allBiomes;

  private BlockData bridgeRailBlock = ModConfig.defaultBridgeRailBlock;

  private BlockData bridgeSupportBlock = ModConfig.defaultBridgeSupportBlock;

  private int bridgeWidth = ModConfig.defaultBridgeWidth;

  private int gridSize = ModConfig.defaultGridSize;

  // #endregion

  // #region Min & Max Sphere Radius

  private double maxLakeRatio = ModConfig.defaultMaxLakeRatio;
  private double maxSphereRadius = ModConfig.defaultMaxSphereRadius;

  // #region double MinSphereRadius

  private double minLakeRatio = ModConfig.defaultMinLakeRatio;
  private double minSphereRadius = ModConfig.defaultMinSphereRadius;

  private boolean noiseEnabled = ModConfig.defaultNoiseEnabled;

  private BlockData orbBlock = ModConfig.defaultOrbBlock;

  private double orbRadius = ModConfig.defaultOrbRadius;

  public final List<BlockEntry> oreOrbBlocks = new ArrayList<BlockEntry>();

  // #endregion

  // #region double MaxSphereRadius

  private BlockData outsideFillerBlock = ModConfig.defaultOutsideFillerBlock;
  private float scale = ModConfig.defaultScale;

  private int scaledGridSize = 0;

  private int scaledOrbRadius = 0;

  private int seaLevel = ModConfig.defaultSeaLevel;

  public final List<BlockEntry> stairwayBlocks = new ArrayList<BlockEntry>();

  // #endregion

  // #endregion

  // #region double OrbRadius

  private boolean tallGrassEnabled = ModConfig.defaultTallGrassEnabled;
  public final World world;

  private ModConfig(final World world) {
    this.world = world;

    // Setup Defaults
    final List<BiomeEntry> entries = new ArrayList<BiomeEntry>();

    for (final BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
      if (biome != null) {
        if (!Utils.any(Utils.where(entries, ModConfig.searchFor(biome)))) {
          entries.add(new BiomeEntry(biome, ModConfig.getDefaultBiomeWeight(biome)));
        }
      }
    }

    this.allBiomes = Collections.unmodifiableList(entries);

    this.update();
  }

  public boolean doesNeedProtectionGlass() {
    final BlockData block = this.getOutsideFillerBlock();
    if (block != null) {
      return block.block != Blx.air;
    }

    return false;
  }

  private Property getBiomeEntryProperty(final BiomeEntry biomeEntry) {
    if (ModConfig.cfgFile == null) {
      return null;
    }
    if (biomeEntry == null) {
      return null;
    }
    final BiomeGenBase biome = biomeEntry.biome;
    if (biome == null) {
      return null;
    }

    return ModConfig.cfgFile.get(Categories.BiomeWeights, biome.biomeName, ModConfig.getDefaultBiomeWeight(biome),
        "The weighted chance that the \"" + biome.biomeName + "\" biome will be generated.", 0, 1000);
  }

  public BlockData getBridgeRailBlock() {
    return this.bridgeRailBlock;
  }

  private BlockWorldProperty getBridgeRailBlockWorldProperty() {
    return new BlockWorldProperty(ModConfig.getBridgeRailBlockProperty(), this.getBridgeRailBlock(),
        ModConfig.defaultBridgeRailBlock);
  }

  public BlockData getBridgeSupportBlock() {
    return this.bridgeSupportBlock;
  }

  // #endregion

  // #region Min & Max Lake Ratio

  private BlockWorldProperty getBridgeSupportBlockWorldProperty() {
    return new BlockWorldProperty(ModConfig.getBridgeSupportBlockProperty(), this.getBridgeSupportBlock(),
        ModConfig.defaultBridgeSupportBlock);
  }

  public int getBridgeWidth() {
    return this.bridgeWidth;
  }

  // #region double MinLakeRatio

  private IntegerWorldProperty getBridgeWidthWorldProperty() {
    return new IntegerWorldProperty(ModConfig.getBridgeWidthProperty(), this.getBridgeWidth(),
        ModConfig.defaultBridgeWidth);
  }

  private String getCategoryName(final Property input) {
    return ModConfig.getCategoryName(input, ModConfig.cfgFile);
  }

  private Property getDomeBlockProperty(final int domeTypeIndex, final int blockIndex) {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    final BlockEntry be = ModConfig.getDefaultDomeBlockProperty(domeTypeIndex, blockIndex);

    final String domeIdxStr = Integer.toString(domeTypeIndex);
    final String blockIdxStr = Integer.toString(blockIndex);

    // while (domeIdxStr.length() < 2) { domeIdxStr = "0" + domeIdxStr; }
    // while (blockIdxStr.length() < 2) { blockIdxStr = "0" + blockIdxStr; }

    final Property ret = ModConfig.cfgFile
        .get(
            Categories.Biospheres,
            "Dome Type #" + domeIdxStr + " - block #" + blockIdxStr,
            be.toString(),
            "The chance that a given dome type will produce a given block.  Values have two parts, and are separated "
                + "by a comma.  The left side of the comma specifies the block name or Id, and the right side of the comma "
                + "specifies the weighted chance to produce that block as part of a given dome type.");

    return ret;
  }

  public int getGridSize() {
    return this.gridSize;
  }

  private IntegerWorldProperty getGridSizeWorldProperty() {
    return new IntegerWorldProperty(ModConfig.getGridSizeProperty(), this.getGridSize(), ModConfig.defaultGridSize);
  }

  public double getMaxLakeRatio() {
    return this.maxLakeRatio;
  }

  // #endregion

  // #region double MaxLakeRatio

  private DoubleWorldProperty getMaxLakeRatioWorldProperty() {
    return new DoubleWorldProperty(ModConfig.getMaxLakeRatioProperty(), this.getMaxLakeRatio(),
        ModConfig.defaultMaxLakeRatio);
  }

  public double getMaxSphereRadius() {
    return this.maxSphereRadius;
  }

  private DoubleWorldProperty getMaxSphereRadiusWorldProperty() {
    return new DoubleWorldProperty(ModConfig.getMaxSphereRadiusProperty(), this.getMaxSphereRadius(),
        ModConfig.defaultMaxSphereRadius);
  }

  public double getMinLakeRatio() {
    return this.minLakeRatio;
  }

  private DoubleWorldProperty getMinLakeRatioWorldProperty() {
    return new DoubleWorldProperty(ModConfig.getMinLakeRatioProperty(), this.getMinLakeRatio(),
        ModConfig.defaultMinLakeRatio);
  }

  public double getMinSphereRadius() {
    return this.minSphereRadius;
  }

  // #endregion

  // #endregion

  // #region int SeaLevel

  private DoubleWorldProperty getMinSphereRadiusWorldProperty() {
    return new DoubleWorldProperty(ModConfig.getMinSphereRadiusProperty(), this.getMinSphereRadius(),
        ModConfig.defaultMinSphereRadius);
  }

  private String getNewWorldProperty(final Property input) {
    return ModConfig.getNewWorldProperty(input, ModConfig.cfgFile);
  }

  private BooleanWorldProperty getNoiseEnabledWorldProperty() {
    return new BooleanWorldProperty(ModConfig.getNoiseEnabledProperty(), this.isNoiseEnabled(),
        ModConfig.defaultNoiseEnabled);
  }

  private String getOldWorldProperty(final Property input) {
    return ModConfig.getOldWorldProperty(input.getName());
  }

  public BlockData getOrbBlock() {
    return this.orbBlock;
  }

  private BlockWorldProperty getOrbBlockWorldProperty() {
    return new BlockWorldProperty(ModConfig.getOrbBlockProperty(), this.getOrbBlock(), ModConfig.defaultOrbBlock);
  }

  public double getOrbRadius() {
    return this.orbRadius;
  }

  private DoubleWorldProperty getOrbRadiusWorldProperty() {
    return new DoubleWorldProperty(ModConfig.getOrbRadiusProperty(), this.getOrbRadius(), ModConfig.defaultOrbRadius);
  }

  // #endregion

  // #region int ScaledGridSize

  public BlockData getOutsideFillerBlock() {
    return this.outsideFillerBlock;
  }

  private BlockWorldProperty getOutsideFillerBlockWorldProperty() {
    return new BlockWorldProperty(ModConfig.getOutsideFillerBlockProperty(), this.getOutsideFillerBlock(),
        ModConfig.defaultOutsideFillerBlock);
  }

  // #endregion

  // #region int ScaledOrbRadius

  private Property getRandomOreBlockEntryProperty(final int index) {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    final BlockEntry be = ModConfig.getDefaultOreBlockEntry(index);

    String idxStr = Integer.toString(index);
    while (idxStr.length() < 2) {
      idxStr = "0" + idxStr;
    }

    return ModConfig.cfgFile.get(Categories.OreOrbs, "Random Ore #" + idxStr, be.toString(),
        "The chance that the Ore Orb will produce a given block.  Values have two parts, and are separated by a "
            + "comma.  The left side of the comma specifies the block name or Id, and the right side of the comma "
            + "specifies the weighted chance to produce that block inside an Ore Orb.");
  }

  private Property getRandomStairwayBlockEntryProperty(final int index) {
    if (ModConfig.cfgFile == null) {
      return null;
    }

    final BlockEntry be = ModConfig.getDefaultStairBlockEntry(index);

    String idxStr = Integer.toString(index);
    while (idxStr.length() < 2) {
      idxStr = "0" + idxStr;
    }

    return ModConfig.cfgFile.get(Categories.OreOrbs, "Random Stairway #" + idxStr, be.toString(),
        "The chance that a given block will be present in an Ore Orb's stairway pattern.  Values have two parts, "
            + "and are separated by a comma.  The left side of the comma specifies the block name or Id, and the "
            + "right side of the comma specifies the weighted chance to produce that block as a part of an Ore "
            + "Orb's stairway.");
  }

  // #endregion

  public float getScale() {
    return this.scale;
  }

  // #endregion

  // #region Migrations

  public int getScaledGridSize() {
    if (this.scaledGridSize == 0) {
      this.scaledGridSize = (int) (this.gridSize * this.scale);
    }

    return this.scaledGridSize;
  }

  public int getScaledOrbRadius() {
    if (this.scaledOrbRadius == 0) {
      this.scaledOrbRadius = (int) ((float) this.orbRadius * this.scale);
    }

    return this.scaledOrbRadius;
  }

  private FloatWorldProperty getScaleWorldProperty() {
    return new FloatWorldProperty(ModConfig.getScaleProperty(), this.getScale(), ModConfig.defaultScale);
  }

  public int getSeaLevel() {
    return this.seaLevel;
  }

  private IntegerWorldProperty getSeaLevelWorldProperty() {
    return new IntegerWorldProperty(ModConfig.getSeaLevelProperty(), this.getSeaLevel(), ModConfig.defaultSeaLevel);
  }

  private BooleanWorldProperty getTallGrassEnabledWorldProperty() {
    return new BooleanWorldProperty(ModConfig.getTallGrassEnabledProperty(), this.isTallGrassEnabled(),
        ModConfig.defaultTallGrassEnabled);
  }

  private synchronized void initMigrations() {
    if (ModConfig.migrations == null) {
      ModConfig.migrations = new ArrayList<MigrationAction>();
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres, "Dome block"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres,
          "Sphere Radius (Minimum)"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres,
          "Sphere Radius (Maximum)"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres,
          "Lake Ratio (Minimum)"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres,
          "Lake Ratio (Maximum)"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.Biospheres,
          "Tall Grass Enabled"));
      ModConfig.migrations.add(new PropertyMovedMigration(Categories.General, Categories.OreOrbs, "Ore Orb Radius"));
      ModConfig.migrations.add(new PropertyRenamedMigration(Categories.Biospheres, "Dome block",
          "Dome Type #0 - block #0"));
    }
  }

  public boolean isNoiseEnabled() {
    return this.noiseEnabled;
  }

  public boolean isTallGrassEnabled() {
    return this.tallGrassEnabled;
  }

  private void loadBiomeWeightsFromFile() {
    int count = 0;

    if (ModConfig.cfgFile != null) {
      for (final BiomeEntry entry : this.allBiomes) {
        final Property prop = this.getBiomeEntryProperty(entry);
        if (prop != null) {
          int weight = prop.getInt(ModConfig.getDefaultBiomeWeight(entry.biome));
          if (weight < 0) {
            weight = 0;
          }

          entry.itemWeight = weight;
          count += weight;
        }
      }
    }

    if (count <= 0) {
      for (final BiomeEntry entry : this.allBiomes) {
        entry.itemWeight = ModConfig.getDefaultBiomeWeight(entry.biome);
      }
    }
  }

  private void loadConfigurationFromFile() {
    if (ModConfig.cfgFile == null) {
      return;
    }

    this.setNoiseEnabled(ModConfig.getNoiseEnabledProperty().getBoolean());
    this.setScale((float) ModConfig.getScaleProperty().getDouble());

    // this.setDomeBlock(Utils.ParseBlock(getDomeBlockProperty().getString(), defaultDomeBlock));

    this.setOrbBlock(BlockData.parse(ModConfig.getOrbBlockProperty().getString(), ModConfig.defaultOrbBlock));

    this.setBridgeSupportBlock(BlockData.parse(ModConfig.getBridgeSupportBlockProperty().getString(),
        ModConfig.defaultBridgeSupportBlock));

    this.setBridgeRailBlock(BlockData.parse(ModConfig.getBridgeRailBlockProperty().getString(),
        ModConfig.defaultBridgeRailBlock));

    this.setOutsideFillerBlock(BlockData.parse(ModConfig.getOutsideFillerBlockProperty().getString(),
        ModConfig.defaultOutsideFillerBlock));

    this.setTallGrassEnabled(ModConfig.getTallGrassEnabledProperty().getBoolean());
    this.setGridSize(ModConfig.getGridSizeProperty().getInt());
    this.setBridgeWidth(ModConfig.getBridgeWidthProperty().getInt());
    this.setMinSphereRadius(ModConfig.getMinSphereRadiusProperty().getDouble());
    this.setMaxSphereRadius(ModConfig.getMaxSphereRadiusProperty().getDouble());
    this.setOrbRadius(ModConfig.getOrbRadiusProperty().getDouble());
    this.setMinLakeRatio(ModConfig.getMinLakeRatioProperty().getDouble());
    this.setMaxLakeRatio(ModConfig.getMaxLakeRatioProperty().getDouble());
    this.setSeaLevel(ModConfig.getSeaLevelProperty().getInt());

    this.loadDomeBlocksFromFile();
    this.loadOreBlocksFromFile();
    this.loadStairwayBlocksFromFile();
    this.loadBiomeWeightsFromFile();

    if (ModConfig.cfgFile.hasChanged()) {
      ModConfig.cfgFile.save();
    }
  }

  private void loadConfigurationFromWorld() {
    if (this.world == null) {
      return;
    }
    if (!BiosphereWorldType.isBiosphereWorld(this.world)) {
      return;
    }

    final CustomWorldData data = CustomWorldData.fromWorld(this.world);
    if (data == null) {
      return;
    }

    this.setNoiseEnabled(this.getNoiseEnabledWorldProperty().readWorldValue(data));
    this.setScale(this.getScaleWorldProperty().readWorldValue(data));
    // setDomeBlock(getDomeBlockWorldProperty().ReadWorldValue(data));
    this.setOrbBlock(this.getOrbBlockWorldProperty().readWorldValue(data));
    this.setBridgeSupportBlock(this.getBridgeSupportBlockWorldProperty().readWorldValue(data));
    this.setBridgeRailBlock(this.getBridgeRailBlockWorldProperty().readWorldValue(data));
    this.setOutsideFillerBlock(this.getOutsideFillerBlockWorldProperty().readWorldValue(data));
    this.setTallGrassEnabled(this.getTallGrassEnabledWorldProperty().readWorldValue(data));
    this.setGridSize(this.getGridSizeWorldProperty().readWorldValue(data));
    this.setBridgeWidth(this.getBridgeWidthWorldProperty().readWorldValue(data));
    this.setMinSphereRadius(this.getMinSphereRadiusWorldProperty().readWorldValue(data));
    this.setMaxSphereRadius(this.getMaxSphereRadiusWorldProperty().readWorldValue(data));
    this.setOrbRadius(this.getOrbRadiusWorldProperty().readWorldValue(data));
    this.setMinLakeRatio(this.getMinLakeRatioWorldProperty().readWorldValue(data));
    this.setMaxLakeRatio(this.getMaxLakeRatioWorldProperty().readWorldValue(data));
    this.setSeaLevel(this.getSeaLevelWorldProperty().readWorldValue(data));

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomOreBlockEntryProperty(i);

      if (prop != null) {
        final String keyName = this.getNewWorldProperty(prop);

        if (data.containsKey(keyName)) {
          final BlockEntry value = BlockEntry.parse(data.get(keyName));

          if (this.oreOrbBlocks.size() > i) {
            this.oreOrbBlocks.set(i, value);
          } else {
            this.oreOrbBlocks.add(value);
          }
        }
      }
    }

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomStairwayBlockEntryProperty(i);

      if (prop != null) {
        final String keyName = this.getNewWorldProperty(prop);

        if (data.containsKey(keyName)) {
          final BlockEntry value = BlockEntry.parse(data.get(keyName));

          if (this.stairwayBlocks.size() > i) {
            this.stairwayBlocks.set(i, value);
          } else {
            this.stairwayBlocks.add(value);
          }
        }
      }
    }

    int biomeCount = 0;
    for (final BiomeEntry entry : this.allBiomes) {
      final Property prop = this.getBiomeEntryProperty(entry);
      if (prop != null) {
        entry.itemWeight = data.getInt(this.getNewWorldProperty(prop), entry.itemWeight);
        biomeCount += entry.itemWeight;
      }
    }

    if (biomeCount <= 0) {
      this.loadBiomeWeightsFromFile();
    }

    for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++) {
      if (ModConfig.DomeBlocks[i] == null) {
        ModConfig.DomeBlocks[i] = new ArrayList<BlockEntry>();
      }

      for (int j = 0; j < ModConfig.DOMETYPE_BLOCK_COUNT; j++) {
        final Property prop = this.getDomeBlockProperty(i, j);
        if (prop != null) {
          final String keyName = this.getNewWorldProperty(prop);

          if (data.containsKey(keyName)) {
            final BlockEntry value = BlockEntry.parse(data.get(keyName));

            // System.err.println(keyName + " = " + value.toString());

            if (ModConfig.DomeBlocks[i].size() > j) {
              ModConfig.DomeBlocks[i].set(j, value);
            } else {
              ModConfig.DomeBlocks[i].add(value);
            }
          }
        }
      }
    }
  }

  private void loadDomeBlocksFromFile() {
    for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++) {
      if (ModConfig.DomeBlocks[i] == null) {
        ModConfig.DomeBlocks[i] = new ArrayList<BlockEntry>();
      }

      for (int j = 0; j < ModConfig.DOMETYPE_BLOCK_COUNT; j++) {
        final Property prop = this.getDomeBlockProperty(i, j);

        if (prop != null) {
          final BlockEntry value = BlockEntry.parse(prop.getString());

          if (ModConfig.DomeBlocks[i].size() > j) {
            ModConfig.DomeBlocks[i].set(j, value);
          } else {
            ModConfig.DomeBlocks[i].add(value);
          }
        }
      }
    }

  }

  private void loadOreBlocksFromFile() {
    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomOreBlockEntryProperty(i);

      if (prop != null) {
        final BlockEntry value = BlockEntry.parse(prop.getString());

        if (this.oreOrbBlocks.size() > i) {
          this.oreOrbBlocks.set(i, value);
        } else {
          this.oreOrbBlocks.add(value);
        }
      }
    }

    int oreCount = 0;
    for (final BlockEntry block : this.oreOrbBlocks) {
      oreCount += block.itemWeight;
    }

    if (oreCount <= 0) {
      this.oreOrbBlocks.add(new BlockEntry(Blx.stone, 0, 1));
    }
  }

  private void loadStairwayBlocksFromFile() {
    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomStairwayBlockEntryProperty(i);

      if (prop != null) {
        final BlockEntry value = BlockEntry.parse(prop.getString());

        if (this.stairwayBlocks.size() > i) {
          this.stairwayBlocks.set(i, value);
        } else {
          this.stairwayBlocks.add(value);
        }
      }
    }

    int blockCount = 0;
    for (final BlockEntry block : this.stairwayBlocks) {
      blockCount += block.itemWeight;
    }

    if (blockCount <= 0) {
      this.stairwayBlocks.add(new BlockEntry(Blx.air, 0, 1));
    }
  }

  private void performFileMigrations() {
    this.initMigrations();

    if (ModConfig.cfgFile != null) {
      for (final MigrationAction mige : ModConfig.migrations) {
        mige.performConfigMigration(ModConfig.cfgFile);
      }

      if (ModConfig.cfgFile.hasChanged()) {
        ModConfig.cfgFile.save();
      }
    }
  }

  // #endregion

  // #region property Helpers

  private void performWorldMigrations() {
    this.initMigrations();

    if (this.world == null) {
      return;
    }

    final CustomWorldData data = CustomWorldData.fromWorld(this.world);
    if (data == null) {
      return;
    }

    final GameRules rules = Utils.getGameRules(this.world);
    if (rules != null) {
      final String ruleName = ModConsts.ModId + ".Is Biosphere world";
      if (rules.getGameRuleBooleanValue(ruleName)) {
        data.put(BiosphereWorldType.IsBiosphereWorldKey, true);
      }
    }

    if ((rules != null) && (ModConfig.cfgFile != null)) {
      for (final String catName : ModConfig.cfgFile.getCategoryNames()) {
        final ConfigCategory cat = ModConfig.cfgFile.getCategory(catName);
        for (final String propName : cat.getValues().keySet()) {
          ModConfig.migrateWorldProperty(rules, data, catName, propName);
        }
      }
    }

    for (final MigrationAction mige : ModConfig.migrations) {
      mige.performWorldMigration(data, ModConfig.cfgFile);
    }

    data.makeNotNew();
    data.setDirty(true);
  }

  private void saveConfigurationToFile() {
    if (ModConfig.cfgFile == null) {
      return;
    }

    ModConfig.getNoiseEnabledProperty().set(this.isNoiseEnabled());
    ModConfig.getScaleProperty().set(this.getScale());
    // getDomeBlockProperty().set(Utils.GetNameOrIdForBlock(getDomeBlock()));
    ModConfig.getOrbBlockProperty().set(this.getOrbBlock().toString());
    ModConfig.getBridgeSupportBlockProperty().set(this.getBridgeSupportBlock().toString());
    ModConfig.getBridgeRailBlockProperty().set(this.getBridgeRailBlock().toString());
    ModConfig.getOutsideFillerBlockProperty().set(this.getOutsideFillerBlock().toString());
    ModConfig.getTallGrassEnabledProperty().set(this.isTallGrassEnabled());
    ModConfig.getGridSizeProperty().set(this.getGridSize());
    ModConfig.getBridgeWidthProperty().set(this.getBridgeWidth());
    ModConfig.getMinSphereRadiusProperty().set(this.getMinSphereRadius());
    ModConfig.getMaxSphereRadiusProperty().set(this.getMaxSphereRadius());
    ModConfig.getOrbRadiusProperty().set(this.getOrbRadius());
    ModConfig.getMinLakeRatioProperty().set(this.getMinLakeRatio());
    ModConfig.getMaxLakeRatioProperty().set(this.getMaxLakeRatio());
    ModConfig.getSeaLevelProperty().set(this.getSeaLevel());

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomOreBlockEntryProperty(i);

      if (prop != null) {
        String value = "air, 0";

        if (this.oreOrbBlocks.size() > i) {
          value = this.oreOrbBlocks.get(i).toString();
        }

        prop.set(value);
      }
    }

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomStairwayBlockEntryProperty(i);

      if (prop != null) {
        String value = "air, 0";

        if (this.stairwayBlocks.size() > i) {
          value = this.stairwayBlocks.get(i).toString();
        }

        prop.set(value);
      }
    }

    for (final BiomeEntry entry : this.allBiomes) {
      final Property prop = this.getBiomeEntryProperty(entry);
      if (prop != null) {
        prop.set(ModConfig.getDefaultBiomeWeight(entry.biome));
      }
    }

    for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++) {
      for (int j = 0; j < ModConfig.DOMETYPE_BLOCK_COUNT; j++) {
        final Property prop = this.getDomeBlockProperty(i, j);
        if (prop != null) {
          String value = "air, 0";

          if ((ModConfig.DomeBlocks[i] != null) && (ModConfig.DomeBlocks[i].size() > j)) {
            value = ModConfig.DomeBlocks[i].get(j).toString();
          }

          prop.set(value);
        }
      }
    }

    if (ModConfig.cfgFile.hasChanged()) {
      ModConfig.cfgFile.save();
    }
  }

  private void saveConfigurationToWorld() {
    if (!BiosphereWorldType.isBiosphereWorld(this.world)) {
      return;
    }

    final CustomWorldData data = CustomWorldData.fromWorld(this.world);
    if (data == null) {
      return;
    }

    this.getNoiseEnabledWorldProperty().writeWorldValue(data);
    this.getScaleWorldProperty().writeWorldValue(data);
    // getDomeBlockWorldProperty().WriteWorldValue(data);
    this.getOrbBlockWorldProperty().writeWorldValue(data);
    this.getBridgeSupportBlockWorldProperty().writeWorldValue(data);
    this.getBridgeRailBlockWorldProperty().writeWorldValue(data);
    this.getOutsideFillerBlockWorldProperty().writeWorldValue(data);
    this.getTallGrassEnabledWorldProperty().writeWorldValue(data);
    this.getGridSizeWorldProperty().writeWorldValue(data);
    this.getBridgeWidthWorldProperty().writeWorldValue(data);
    this.getMinSphereRadiusWorldProperty().writeWorldValue(data);
    this.getMaxSphereRadiusWorldProperty().writeWorldValue(data);
    this.getOrbRadiusWorldProperty().writeWorldValue(data);
    this.getMinLakeRatioWorldProperty().writeWorldValue(data);
    this.getMaxLakeRatioWorldProperty().writeWorldValue(data);
    this.getSeaLevelWorldProperty().writeWorldValue(data);

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomOreBlockEntryProperty(i);

      if (prop != null) {
        final String keyName = this.getNewWorldProperty(prop);
        String value = "air, 0";

        if (this.oreOrbBlocks.size() > i) {
          value = this.oreOrbBlocks.get(i).toString();
        }

        data.put(keyName, value);
      }
    }

    for (int i = 0; i < ModConfig.BLOCK_COUNT; i++) {
      final Property prop = this.getRandomStairwayBlockEntryProperty(i);

      if (prop != null) {
        final String keyName = this.getNewWorldProperty(prop);
        String value = "air, 0";

        if (this.stairwayBlocks.size() > i) {
          value = this.stairwayBlocks.get(i).toString();
        }

        data.put(keyName, value);
      }
    }

    for (final BiomeEntry entry : this.allBiomes) {
      final Property prop = this.getBiomeEntryProperty(entry);
      if (prop != null) {
        final String keyName = this.getNewWorldProperty(prop);
        data.put(keyName, entry.itemWeight);
      }
    }

    for (int i = 0; i < ModConfig.DOMETYPE_COUNT; i++) {
      for (int j = 0; j < ModConfig.DOMETYPE_BLOCK_COUNT; j++) {
        final Property prop = this.getDomeBlockProperty(i, j);
        if (prop != null) {
          final String keyName = this.getNewWorldProperty(prop);
          String value = "air, 0";

          if ((ModConfig.DomeBlocks[i] != null) && (ModConfig.DomeBlocks[i].size() > j)) {
            value = ModConfig.DomeBlocks[i].get(j).toString();
          }

          data.put(keyName, value);
        }
      }
    }
  }

  public void setBridgeRailBlock(BlockData value) {
    if (value == null) {
      value = ModConfig.defaultBridgeRailBlock;
    }
    this.bridgeRailBlock = value;
  }

  public void setBridgeSupportBlock(BlockData value) {
    if (value == null) {
      value = ModConfig.defaultBridgeSupportBlock;
    }
    this.bridgeSupportBlock = value;
  }

  public void setBridgeWidth(int value) {
    if (value < ModConfig.minBridgeWidth) {
      value = ModConfig.minBridgeWidth;
    } else if (value > ModConfig.maxBridgeWidth) {
      value = ModConfig.maxBridgeWidth;
    }

    this.bridgeWidth = value;
  }

  public void setGridSize(int value) {
    if (value < ModConfig.minGridSize) {
      value = ModConfig.minGridSize;
    } else if (value > ModConfig.maxGridSize) {
      value = ModConfig.maxGridSize;
    }

    this.gridSize = value;
    this.scaledGridSize = 0;
  }

  public void setMaxLakeRatio(double value) {
    if (value < ModConfig.lakeRatioMinimumValue) {
      value = ModConfig.lakeRatioMinimumValue;
    } else if (value > ModConfig.lakeRatioMaximumValue) {
      value = ModConfig.lakeRatioMaximumValue;
    }

    this.maxLakeRatio = value;
  }

  public void setMaxSphereRadius(double value) {
    if (value < ModConfig.sphereRadiusMinimumValue) {
      value = ModConfig.sphereRadiusMinimumValue;
    } else if (value > ModConfig.sphereRadiusMaximumValue) {
      value = ModConfig.sphereRadiusMaximumValue;
    }

    this.maxSphereRadius = value;
  }

  // #endregion

  public void setMinLakeRatio(double value) {
    if (value < ModConfig.lakeRatioMinimumValue) {
      value = ModConfig.lakeRatioMinimumValue;
    } else if (value > ModConfig.lakeRatioMaximumValue) {
      value = ModConfig.lakeRatioMaximumValue;
    }

    this.minLakeRatio = value;
  }

  public void setMinSphereRadius(double value) {
    if (value < ModConfig.sphereRadiusMinimumValue) {
      value = ModConfig.sphereRadiusMinimumValue;
    } else if (value > ModConfig.sphereRadiusMaximumValue) {
      value = ModConfig.sphereRadiusMaximumValue;
    }

    this.minSphereRadius = value;
  }

  // #region Load & Save from world

  public void setNoiseEnabled(final boolean noiseEnabled) {
    this.noiseEnabled = noiseEnabled;
  }

  public void setOrbBlock(BlockData value) {
    if (value == null) {
      value = ModConfig.defaultOrbBlock;
    }
    this.orbBlock = value;
  }

  // #endregion

  // #region Load & Save from File

  public void setOrbRadius(double value) {
    if (value < ModConfig.minOrbRadius) {
      value = ModConfig.minOrbRadius;
    } else if (value > ModConfig.maxOrbRadius) {
      value = ModConfig.maxOrbRadius;
    }

    this.orbRadius = value;
    this.scaledOrbRadius = 0;
  }

  public void setOutsideFillerBlock(BlockData value) {
    if (value == null) {
      value = ModConfig.defaultOutsideFillerBlock;
    }

    // if (value.Block == Blx.lava) { value = value.setBlock(Blx.flowing_lava); }
    // else if (value.Block == Blx.water) { value = value.setBlock(Blx.flowing_water); }

    this.outsideFillerBlock = value;
  }

  public void setScale(float value) {
    if (value < ModConfig.minScale) {
      value = ModConfig.minScale;
    } else if (value > ModConfig.maxScale) {
      value = ModConfig.maxScale;
    }

    this.scale = value;
    this.scaledGridSize = 0;
    this.scaledOrbRadius = 0;
  }

  public void setSeaLevel(int value) {
    if (value < ModConfig.seaLevelMinimumValue) {
      value = ModConfig.seaLevelMinimumValue;
    } else if (value > ModConfig.seaLevelMaximumValue) {
      value = ModConfig.seaLevelMaximumValue;
    }

    // System.out.println("New Sea Level: " + value + ", Old: " + seaLevel);

    this.seaLevel = value;
  }

  public void setTallGrassEnabled(final boolean tallGrass) {
    this.tallGrassEnabled = tallGrass;
  }

  public void update() {
    this.performFileMigrations();
    this.loadConfigurationFromFile();
    this.saveConfigurationToFile();

    this.performWorldMigrations();
    this.loadConfigurationFromWorld();
    this.saveConfigurationToWorld();
  }

  // #endregion
}
