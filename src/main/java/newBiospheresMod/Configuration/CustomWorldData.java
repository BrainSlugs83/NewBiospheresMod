/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.configuration;

import io.netty.util.internal.chmv8.ConcurrentHashMapV8;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import newbiospheresmod.BlockData;
import newbiospheresmod.helpers.ModConsts;

public class CustomWorldData extends WorldSavedData {
  public static CustomWorldData fromWorld(final World world) {
    return CustomWorldData.fromWorld(world, ModConsts.ModId.toLowerCase().replace(" ", ""));
  }

  public static CustomWorldData fromWorld(final World world, final String containerName) {
    // Retrieves the CustomWorldData instance for the given world, creating it if necessary
    final MapStorage storage = world.mapStorage;
    CustomWorldData result = (CustomWorldData) storage.loadData(CustomWorldData.class, containerName);
    if (result == null) {
      result = new CustomWorldData(containerName);
      storage.setData(containerName, result);
    } else {
      // returned object is not new, it was loaded.
      result.makeNotNew();
    }

    return result;
  }

  private static List<String> getNbtTagKeys(final NBTTagCompound nbtTag) {
    final List<String> keys = new ArrayList<String>();

    if (nbtTag != null) {
      for (final Object key : nbtTag.func_150296_c()) {
        String keyToAdd = null;

        if (key != null) {
          if (key instanceof String) {
            keyToAdd = (String) key;
          } else {
            keyToAdd = key.toString();
          }
        }

        if (keyToAdd != null) {
          keys.add(keyToAdd);
        }
      }
    }

    return keys;
  }

  public final String containerName;

  private final Map<String, String> data = new ConcurrentHashMapV8<String, String>();

  private boolean isNew = true;

  private boolean makeNotNewTriggered = false;

  public CustomWorldData(final String containerName) {
    super(containerName);
    this.containerName = containerName;
  }

  public boolean containsKey(final String keyName) {
    return this.data.containsKey(keyName);
  }

  public String get(final String keyName) {
    if (this.containsKey(keyName)) {
      return this.data.get(keyName);
    }

    return null;
  }

  public BlockData getBlock(final String key) {
    return this.getBlock(key, BlockData.Empty);
  }

  public BlockData getBlock(final String key, final BlockData defaultValue) {
    if (this.containsKey(key)) {
      try {
        return BlockData.parse(this.get(key), defaultValue);
      } catch (final Throwable ignore) { /* do nothing */
      }
    }

    return defaultValue;
  }

  public boolean getBool(final String key) {
    return this.getBool(key, false);
  }

  public boolean getBool(final String key, final boolean defaultValue) {
    if (this.containsKey(key)) {
      try {
        return Boolean.parseBoolean(this.get(key));
      } catch (final Throwable ignore) { /* do nothing */
      }
    }

    return defaultValue;
  }

  public double getDouble(final String key) {
    return this.getDouble(key, 0);
  }

  public double getDouble(final String key, final double defaultValue) {
    if (this.containsKey(key)) {
      try {
        return Double.parseDouble(this.get(key));
      } catch (final Throwable ignore) { /* do nothing */
      }
    }

    return defaultValue;
  }

  public float getFloat(final String key) {
    return this.getFloat(key, 0);
  }

  public float getFloat(final String key, final float defaultValue) {
    if (this.containsKey(key)) {
      try {
        return Float.parseFloat(this.get(key));
      } catch (final Throwable ignore) { /* do nothing */
      }
    }

    return defaultValue;
  }

  public int getInt(final String key) {
    return this.getInt(key, 0);
  }

  public int getInt(final String key, final int defaultValue) {
    if (this.containsKey(key)) {
      try {
        return Integer.parseInt(this.get(key));
      } catch (final Throwable ignore) { /* do nothing */
      }
    }

    return defaultValue;
  }

  public boolean getIsNew() {
    return this.isNew;
  }

  public Set<String> keys() {
    return this.data.keySet();
  }

  public void makeNotNew() {
    this.makeNotNewTriggered = true;
  }

  public void put(final String key, final BlockData value) {
    this.put(key, value.toString());
  }

  public void put(final String key, final boolean value) {
    this.put(key, Boolean.toString(value));
  }

  public void put(final String key, final double value) {
    this.put(key, Double.toString(value));
  }

  public void put(final String key, final float value) {
    this.put(key, Float.toString(value));
  }

  public void put(final String key, final int value) {
    this.put(key, Integer.toString(value));
  }

  public void put(final String keyName, final String value) {
    final String prevValue = this.data.put(keyName, value);

    if (prevValue == null ? value != null : !prevValue.equals(value)) {
      this.markDirty();
    }
  }

  @Override
  public void readFromNBT(final NBTTagCompound nbtTag) {
    this.data.clear();

    if (nbtTag != null) {
      for (final String key : CustomWorldData.getNbtTagKeys(nbtTag)) {
        // System.out.println("LOADING: " + key);
        this.put(key, nbtTag.getString(key));
      }
    }
  }

  public String removeKey(final String keyName) {
    final String returnValue = this.data.remove(keyName);

    if (returnValue != null) {
      this.markDirty();
    }

    return returnValue;
  }

  @Override
  public void writeToNBT(final NBTTagCompound nbtTag) {
    if (nbtTag != null) {
      for (final String key : CustomWorldData.getNbtTagKeys(nbtTag)) {
        nbtTag.removeTag(key);
      }

      for (final String key : this.keys()) {
        // System.out.println("SAVING: " + key);
        nbtTag.setString(key, this.data.get(key));
      }

      if (this.makeNotNewTriggered) {
        this.isNew = false;
        this.makeNotNewTriggered = false;
      }
    }
  }

}
