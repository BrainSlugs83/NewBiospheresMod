package woop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import akka.japi.Creator;
import akka.japi.Predicate;

public class ModConfig
{
	// TODO: MOVE THIS SOMEWHERE GOOD.
	// private void SetupBlocks()
	// {
	// BiomeGenBase.hell.topBlock = BiomeGenBase.hell.fillerBlock = Blocks.netherrack;
	// BiomeGenBase.sky.topBlock = BiomeGenBase.sky.fillerBlock = Blocks.end_stone;
	//
	// if (WATERWORLD)
	// {
	// Blocks.water.setLightOpacity(0);
	// Blocks.flowing_water.setLightOpacity(0);
	// }
	// }

	public enum WorldCharacteristics
	{
		NormalWorld, WaterWorld, LavaWorld
	}

	private class ModProps extends java.util.Properties
	{
		public Enumeration keys()
		{
			Enumeration keysEnum = super.keys();
			Vector<String> keyList = new Vector<String>();
			while (keysEnum.hasMoreElements())
			{
				keyList.add((String)keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return keyList.elements();
		}

		public Block getProperty(String propertyName, Block fallbackValue)
		{
			return Utils.ParseBlock(this.getProperty(propertyName, Utils.GetNameOrIdForBlock(fallbackValue)));
		}

		public void setProperty(String propertyName, Block value)
		{
			this.setProperty(propertyName, Utils.GetNameOrIdForBlock(value));
		}

		public int getProperty(String propertyName, int fallbackValue)
		{
			try
			{
				return Integer.parseInt(this.getProperty(propertyName, Integer.toString(fallbackValue)));
			}
			catch (Throwable ignore)
			{
				return fallbackValue;
			}
		}

		public void setProperty(String propertyName, int value)
		{
			setProperty(propertyName, Integer.toString(value));
		}

		public float getProperty(String propertyName, float fallbackValue)
		{
			try
			{
				return Float.parseFloat(this.getProperty(propertyName, Float.toString(fallbackValue)));
			}
			catch (Throwable ignore)
			{
				return fallbackValue;
			}
		}

		public void setProperty(String propertyName, float value)
		{
			setProperty(propertyName, Float.toString(value));
		}

		public double getProperty(String propertyName, double fallbackValue)
		{
			try
			{
				return Double.parseDouble(this.getProperty(propertyName, Double.toString(fallbackValue)));
			}
			catch (Throwable ignore)
			{
				return fallbackValue;
			}
		}

		public void setProperty(String propertyName, double value)
		{
			setProperty(propertyName, Double.toString(value));
		}

		public boolean getProperty(String propertyName, boolean fallbackValue)
		{
			try
			{
				return Boolean.parseBoolean(this.getProperty(propertyName, Boolean.toString(fallbackValue)));
			}
			catch (Throwable ignore)
			{
				return fallbackValue;
			}
		}

		public void setProperty(String propertyName, boolean value)
		{
			setProperty(propertyName, Boolean.toString(value));
		}

		public <T extends Enum<T>> T getEnumProperty(Class<T> _class, String propertyName, T fallbackValue)
		{
			try
			{
				return Utils.ParseEnum(_class, getProperty(propertyName), fallbackValue);
			}
			catch (Throwable ignore)
			{
				return fallbackValue;
			}
		}

		public <T extends Enum<T>> void setEnumProperty(String propertyName, T value)
		{
			setProperty(propertyName, value == null ? "" : value.toString());
		}

		public List<Double> getDoubles(String propertyName, Double... fallbackValues)
		{
			List<Double> doubles = Utils.ConvertStringToDoubles(getProperty(
				propertyName,
				Utils.ConvertDoublesToString(fallbackValues)));

			if (doubles == null)
			{
				doubles = new ArrayList<Double>();
			}

			if (doubles.size() == 0)
			{
				for (Double value: fallbackValues)
				{
					doubles.add(value);
				}
			}
			else if (doubles.size() == 1)
			{
				Double firstValue = doubles.get(0);

				while (doubles.size() < fallbackValues.length)
				{
					doubles.add(firstValue);
				}
			}
			else
			{
				while (doubles.size() < fallbackValues.length)
				{
					doubles.add(fallbackValues[doubles.size()]);
				}
			}

			return doubles;
		}

		public void setDoubles(String propertyName, Double... values)
		{
			setProperty(propertyName, Utils.ConvertDoublesToString(values));
		}
	}

	private static final File cfgFile = new File(Minecraft.getMinecraft().mcDataDir, "/config/Biosphere.cfg");

	// #region Fields & Properties

	public final World World;
	public final List<BiomeEntry> AllBiomes;

	// #region boolean NoiseEnabled

	private boolean noiseEnabled = false;

	public boolean isNoiseEnabled()
	{
		return noiseEnabled;
	}

	public void setNoiseEnabled(boolean noiseEnabled)
	{
		this.noiseEnabled = noiseEnabled;
	}

	// #endregion

	// #region float Scale

	private float scale = 1.0f;

	public float getScale()
	{
		return scale;
	}

	public void setScale(float scale)
	{
		if (scale <= 0) { throw new IllegalArgumentException("scale must be a positive non-zero number."); }
		this.scale = scale;
		this.scaledGridSize = 0;
		this.scaledOrbRadius = 0;
	}

	// #endregion

	// #region Block DomeBlock

	private Block domeBlock = Blocks.glass;

	public Block getDomeBlock()
	{
		return domeBlock;
	}

	public void setDomeBlock(Block value)
	{
		if (value == null)
		{
			value = Blocks.air;
		}
		this.domeBlock = value;
	}

	// #endregion

	// #region Block BridgeSupportBlock

	private Block bridgeSupportBlock = Blocks.planks;

	public Block getBridgeSupportBlock()
	{
		return bridgeSupportBlock;
	}

	public void setBridgeSupportBlock(Block value)
	{
		if (value == null)
		{
			value = Blocks.air;
		}
		this.bridgeSupportBlock = value;
	}

	// #endregion

	// #region Block BridgeRailBlock

	private Block bridgeRailBlock = Blocks.fence;

	public Block getBridgeRailBlock()
	{
		return bridgeRailBlock;
	}

	public void setBridgeRailBlock(Block value)
	{
		if (value == null)
		{
			value = Blocks.air;
		}
		this.bridgeRailBlock = value;
	}

	// #endregion

	// #region WorldCharacteristics Characteristics

	private WorldCharacteristics characteristics = WorldCharacteristics.NormalWorld;

	public WorldCharacteristics getCharacteristics()
	{
		return characteristics;
	}

	public void setCharacteristics(WorldCharacteristics characteristics)
	{
		this.characteristics = characteristics;
	}

	// #endregion

	// #region boolean TallGrassEnabled

	private boolean tallGrassEnabled = true;

	public boolean isTallGrassEnabled()
	{
		return tallGrassEnabled;
	}

	public void setTallGrassEnabled(boolean tallGrass)
	{
		this.tallGrassEnabled = tallGrass;
	}

	// #endregion

	// #region int GridSize

	private int gridSize = 9;

	public int getGridSize()
	{
		return gridSize;
	}

	public void setGridSize(int gridSize)
	{
		if (gridSize <= 0) { throw new IllegalArgumentException("gridSize must be a positive non-zero number."); }
		this.gridSize = gridSize;
		this.scaledGridSize = 0;
	}

	// #endregion

	// #region int BridgeWidth

	private int bridgeWidth = 2;

	public int getBridgeWidth()
	{
		return bridgeWidth;
	}

	public void setBridgeWidth(int bridgeWidth)
	{
		if (bridgeWidth <= 0) { throw new IllegalArgumentException("bridgeWidth must be a positive non-zero number."); }
		this.bridgeWidth = bridgeWidth;
	}

	// #endregion

	// #region double MinSphereRadius

	private double minSphereRadius = 20;

	public double getMinSphereRadius()
	{
		return minSphereRadius;
	}

	public void setMinSphereRadius(double minSphereRadius)
	{
		if (minSphereRadius <= 0) { throw new IllegalArgumentException(
			"minSphereRadius must be a positive non-zero number."); }
		this.minSphereRadius = minSphereRadius;
	}

	// #endregion

	// #region double MaxSphereRadius

	private double maxSphereRadius = 50;

	public double getMaxSphereRadius()
	{
		return maxSphereRadius;
	}

	public void setMaxSphereRadius(double maxSphereRadius)
	{
		if (maxSphereRadius <= 0) { throw new IllegalArgumentException(
			"maxSphereRadius must be a positive non-zero number."); }
		this.maxSphereRadius = maxSphereRadius;
	}

	// #endregion

	// #region double OrbRadius

	private double orbRadius = 7;

	public double getOrbRadius()
	{
		return orbRadius;
	}

	public void setOrbRadius(double orbRadius)
	{
		if (orbRadius <= 0) { throw new IllegalArgumentException("orbRadius must be a positive non-zero number."); }
		this.orbRadius = orbRadius;
		this.scaledOrbRadius = 0;
	}

	// #endregion

	// #region double MinLakeRatio

	private double minLakeRatio = 0.3d;

	public double getMinLakeRatio()
	{
		return minLakeRatio;
	}

	public void setMinLakeRatio(double minLakeRatio)
	{
		if (minLakeRatio <= 0) { throw new IllegalArgumentException("minLakeRatio must be a positive non-zero number."); }
		this.minLakeRatio = minLakeRatio;
	}

	// #endregion

	// #region double MaxLakeRatio

	private double maxLakeRatio = 0.6d;

	public double getMaxLakeRatio()
	{
		return maxLakeRatio;
	}

	public void setMaxLakeRatio(double maxLakeRatio)
	{
		if (maxLakeRatio <= 0) { throw new IllegalArgumentException("maxLakeRatio must be a positive non-zero number."); }
		this.maxLakeRatio = maxLakeRatio;
	}

	// #endregion

	// #region int ScaledGridSize

	private int scaledGridSize = 0;

	public int getScaledGridSize()
	{
		if (scaledGridSize == 0)
		{
			scaledGridSize = (int)((float)gridSize * scale);
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

	public Block getOutsideFillerBlock()
	{
		switch (this.getCharacteristics())
		{
		case LavaWorld:
			return Blocks.lava;
		case WaterWorld:
			return Blocks.water;
		default:
			return Blocks.air;
		}
	}

	public static boolean getNoiseEnabled()
	{
		return false;
	}

	// #endregion

	private static Predicate<BiomeEntry> SearchFor(final BiomeGenBase biome)
	{
		return new Predicate<BiomeEntry>()
		{
			public boolean test(BiomeEntry entry)
			{
				return entry.biome == biome;
			}
		};
	}

	private static LruCacheList<ModConfig> modConfigs = new LruCacheList<ModConfig>(10);

	public static ModConfig get(final World world)
	{
		return modConfigs.FindOrAdd(new Predicate<ModConfig>()
		{
			@Override
			public boolean test(ModConfig config)
			{
				return config.World == world;
			}
		}, new Creator<ModConfig>()
		{
			@Override
			public ModConfig create()
			{
				return new ModConfig(world);
			}
		});
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
		LoadConfigurationFromFile();
	}

	public void LoadConfigurationFromFile()
	{
		try
		{
			cfgFile.getParentFile().mkdirs();

			if (cfgFile.exists() || cfgFile.createNewFile())
			{
				if (cfgFile.canRead())
				{
					FileInputStream fs = null;
					try
					{
						fs = new FileInputStream(cfgFile);
						ModProps props = new ModProps();
						props.load(fs);

						this.setDomeBlock(props.getProperty("DomeBlock", this.getDomeBlock()));
						this.setNoiseEnabled(props.getProperty("NoiseEnabled", this.isNoiseEnabled()));
						this.setTallGrassEnabled(props.getProperty("TallGrassEnabled", this.isTallGrassEnabled()));

						this.setCharacteristics(props.getEnumProperty(
							WorldCharacteristics.class,
							"Characteristics",
							this.getCharacteristics()));

						this.setGridSize(props.getProperty("GridSize", this.getGridSize()));
						this.setOrbRadius(props.getProperty("OrbRadius", this.getOrbRadius()));
						this.setBridgeWidth(props.getProperty("BridgeWidth", this.getBridgeWidth()));
						this.setBridgeSupportBlock(props.getProperty("BridgeSupportBlock", this.getBridgeSupportBlock()));
						this.setBridgeRailBlock(props.getProperty("BridgeRailBlock", this.getBridgeRailBlock()));
						this.setScale(props.getProperty("Scale", this.getScale()));

						List<Double> doubles;
						doubles = props.getDoubles("SphereRadius", this.getMinSphereRadius(), this.getMaxSphereRadius());
						this.setMinSphereRadius(doubles.get(0));
						this.setMaxSphereRadius(doubles.get(1));

						doubles = props.getDoubles("LakeRatio", this.getMinLakeRatio(), this.getMaxLakeRatio());
						this.setMinLakeRatio(doubles.get(0));
						this.setMaxLakeRatio(doubles.get(1));

						for (BiomeEntry biome: AllBiomes)
						{
							biome.itemWeight = Integer.parseInt(props.getProperty("biomeWeight_"
									+ biome.biome.biomeName, Integer.toString(biome.itemWeight)));
						}

					}
					finally
					{
						if (fs != null)
						{
							fs.close();
						}
					}
				}
			}

			SaveConfigurationToFile();
		}
		catch (Throwable ignore)
		{ /* do nothing */}
	}

	private void SaveConfigurationToFile()
	{
		try
		{
			cfgFile.getParentFile().mkdirs();
			if (cfgFile.exists() || cfgFile.createNewFile())
			{
				if (cfgFile.canWrite())
				{
					FileOutputStream fs = null;
					try
					{
						fs = new FileOutputStream(cfgFile);
						ModProps props = new ModProps();

						props.setProperty("DomeBlock", this.getDomeBlock());
						props.setProperty("NoiseEnabled", this.isNoiseEnabled());
						props.setProperty("TallGrassEnabled", this.isTallGrassEnabled());
						props.setEnumProperty("Characteristics", this.getCharacteristics());
						props.setProperty("GridSize", this.getGridSize());
						props.setProperty("OrbRadius", this.getOrbRadius());
						props.setProperty("BridgeWidth", this.getBridgeWidth());
						props.setProperty("BridgeSupportBlock", this.getBridgeSupportBlock());
						props.setProperty("BridgeRailBlock", this.getBridgeRailBlock());
						props.setProperty("Scale", this.getScale());
						props.setDoubles("SphereRadius", this.getMinSphereRadius(), this.getMaxSphereRadius());
						props.setDoubles("LakeRatio", this.getMinLakeRatio(), this.getMaxLakeRatio());

						for (BiomeEntry biome: AllBiomes)
						{
							props.setProperty(
								"biomeWeight_" + biome.biome.biomeName,
								Integer.toString(biome.itemWeight));
						}

						props.store(fs, "Biosphere Config");
					}
					finally
					{
						if (fs != null)
						{
							fs.close();
						}
					}
				}
			}
		}
		catch (Throwable ignore)
		{ /* do nothing */}
	}
}
