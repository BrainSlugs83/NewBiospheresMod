/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Configuration;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Utils;

public class CustomWorldData extends WorldSavedData
{
	public final String ContainerName;
	private final Map<String, String> Data = new ConcurrentHashMapV8<String, String>();

	private boolean isNew = true;
	private boolean makeNotNewTriggered = false;

	public boolean getIsNew()
	{
		return isNew;
	}

	public void MakeNotNew()
	{
		makeNotNewTriggered = true;
	}

	public Set<String> Keys()
	{
		return Data.keySet();
	}

	public boolean ContainsKey(String keyName)
	{
		return Data.containsKey(keyName);
	}

	public void put(String keyName, String value)
	{
		String prevValue = Data.put(keyName, value);

		if (prevValue == null ? value != null : !prevValue.equals(value))
		{
			markDirty();
		}
	}

	public String get(String keyName)
	{
		if (ContainsKey(keyName)) { return Data.get(keyName); }

		return null;
	}

	public String RemoveKey(String keyName)
	{
		String returnValue = Data.remove(keyName);

		if (returnValue != null)
		{
			markDirty();
		}

		return returnValue;
	}

	public CustomWorldData(String containerName)
	{
		super(containerName);
		this.ContainerName = containerName;
	}

	public static CustomWorldData FromWorld(World world)
	{
		return FromWorld(world, ModConsts.ModId.toLowerCase().replace(" ", ""));
	}

	public static CustomWorldData FromWorld(World world, String containerName)
	{
		// Retrieves the CustomWorldData instance for the given world, creating it if necessary
		MapStorage storage = world.mapStorage;
		CustomWorldData result = (CustomWorldData)storage.loadData(CustomWorldData.class, containerName);
		if (result == null)
		{
			result = new CustomWorldData(containerName);
			storage.setData(containerName, result);
		}
		else
		{
			// returned object is not new, it was loaded.
			result.MakeNotNew();
		}

		return result;
	}

	private static List<String> GetNbtTagKeys(NBTTagCompound nbtTag)
	{
		List<String> keys = new ArrayList<String>();

		if (nbtTag != null)
		{
			for (Object _key: nbtTag.func_150296_c())
			{
				String key = null;

				if (_key != null)
				{
					if (_key instanceof String)
					{
						key = (String)_key;
					}
					else
					{
						key = _key.toString();
					}
				}

				if (key != null)
				{
					keys.add(key);
				}
			}
		}

		return keys;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbtTag)
	{
		Data.clear();

		if (nbtTag != null)
		{
			for (String key: GetNbtTagKeys(nbtTag))
			{
				// System.out.println("LOADING: " + key);
				this.put(key, nbtTag.getString(key));
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbtTag)
	{
		if (nbtTag != null)
		{
			for (String key: GetNbtTagKeys(nbtTag))
			{
				nbtTag.removeTag(key);
			}

			for (String key: Keys())
			{
				// System.out.println("SAVING: " + key);
				nbtTag.setString(key, Data.get(key));
			}

			if (makeNotNewTriggered)
			{
				isNew = false;
				makeNotNewTriggered = false;
			}
		}
	}

	public void put(String key, boolean value)
	{
		put(key, Boolean.toString(value));
	}

	public void put(String key, int value)
	{
		put(key, Integer.toString(value));
	}

	public void put(String key, float value)
	{
		put(key, Float.toString(value));
	}

	public void put(String key, double value)
	{
		put(key, Double.toString(value));
	}

	public void put(String key, Block value)
	{
		put(key, Utils.GetNameOrIdForBlock(value));
	}

	public boolean getBool(String key)
	{
		return getBool(key, false);
	}

	public boolean getBool(String key, boolean defaultValue)
	{
		if (ContainsKey(key))
		{
			try
			{
				return Boolean.parseBoolean(get(key));
			}
			catch (Throwable ignore)
			{ /* do nothing */}
		}

		return defaultValue;
	}

	public int getInt(String key)
	{
		return getInt(key, 0);
	}

	public int getInt(String key, int defaultValue)
	{
		if (ContainsKey(key))
		{
			try
			{
				return Integer.parseInt(get(key));
			}
			catch (Throwable ignore)
			{ /* do nothing */}
		}

		return defaultValue;
	}

	public double getDouble(String key)
	{
		return getDouble(key, 0);
	}

	public double getDouble(String key, double defaultValue)
	{
		if (ContainsKey(key))
		{
			try
			{
				return Double.parseDouble(get(key));
			}
			catch (Throwable ignore)
			{ /* do nothing */}
		}

		return defaultValue;
	}

	public float getFloat(String key)
	{
		return getFloat(key, 0);
	}

	public float getFloat(String key, float defaultValue)
	{
		if (ContainsKey(key))
		{
			try
			{
				return Float.parseFloat(get(key));
			}
			catch (Throwable ignore)
			{ /* do nothing */}
		}

		return defaultValue;
	}

	public Block getBlock(String key)
	{
		return getBlock(key, Blx.air);
	}

	public Block getBlock(String key, Block defaultValue)
	{
		if (ContainsKey(key))
		{
			try
			{
				return Utils.ParseBlock(get(key), defaultValue);
			}
			catch (Throwable ignore)
			{ /* do nothing */}
		}

		return defaultValue;
	}

}
