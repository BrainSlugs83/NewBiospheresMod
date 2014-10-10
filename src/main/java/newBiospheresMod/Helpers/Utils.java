/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;

public class Utils
{
	public static Block ParseBlock(String blockNameOrId)
	{
		return ParseBlock(blockNameOrId, Blocks.air);
	}

	public static Block ParseBlock(String blockNameOrId, Block fallbackValue)
	{
		Block returnValue = null;

		try
		{
			int id = Integer.parseInt(blockNameOrId);
			returnValue = Block.getBlockById(id);
		}
		catch (Throwable ignore)
		{
			// do nothing
		}

		try
		{
			if (returnValue == null)
			{
				returnValue = Block.getBlockFromName(blockNameOrId);
			}
		}
		catch (Throwable e)
		{
			// do nothing
		}

		if (returnValue == null)
		{
			returnValue = fallbackValue;
		}

		return returnValue;
	}

	public static String GetNameOrIdForBlock(Block block)
	{
		if (block == null) { return "air"; }

		String ret = null;

		try
		{
			ret = Block.blockRegistry.getNameForObject(block);
		}
		catch (Exception ignore)
		{ /* do nothing */
		}

		if (ret == null || ret.length() < 1)
		{
			ret = Integer.toString(Block.getIdFromBlock(block));
		}

		if (ret != null && ret.startsWith("minecraft:"))
		{
			ret = ret.substring(10);
		}

		return ret;
	}

	public static String GetName(Object obj)
	{
		if (obj == null) { return "(null)"; }

		if (obj instanceof Block) { return GetNameOrIdForBlock((Block)obj); }

		String name = obj.getClass().getSimpleName();
		if (name == null || name.length() < 1)
		{
			name = obj.getClass().getName();
		}

		return name;
	}

	public static boolean IsPlayer(Entity e)
	{
		if (e == null) { return false; }

		if (e instanceof EntityPlayer) { return true; }

		// NOTE: This works, but I'm guessing it won't work on the obfuscated code, so, whatever.
		// if (e instanceof EntityLivingBase)
		// {
		// Class c = e.getClass();
		//
		// while (c != null)
		// {
		// try
		// {
		// java.lang.reflect.Method m = c.getDeclaredMethod("isPlayer");
		// m.setAccessible(true);
		//
		// boolean result = ((Boolean)m.invoke(e)).booleanValue();
		// return result;
		// }
		// catch (Throwable ignore)
		// {
		// c = c.getSuperclass();
		// }
		// }
		// }

		return false;
	}

	public static GameRules GetGameRules(World world)
	{
		if (world != null)
		{
			WorldInfo info = world.getWorldInfo();
			if (info != null) { return info.getGameRulesInstance(); }
		}
		return null;
	}

	public static ChunkCoordinates GetCoords(ChunkCoordinates copyMe)
	{
		if (copyMe == null) { return GetCoords(0, 0, 0); }
		return GetCoords(copyMe.posX, copyMe.posY, copyMe.posZ);
	}

	public static ChunkCoordinates GetCoords(Entity e)
	{
		if (e == null) { return GetCoords(0, 0, 0); }
		return GetCoords(e.posX, e.posY, e.posZ);
	}

	public static ChunkCoordinates GetCoords(double x, double y, double z)
	{
		return GetCoords((int)Math.round(x), (int)Math.round(y), (int)Math.round(z));
	}

	public static ChunkCoordinates GetCoords(int x, int y, int z)
	{
		ChunkCoordinates coords = new ChunkCoordinates();
		coords.posX = x;
		coords.posY = y;
		coords.posZ = z;

		return coords;
	}

	public static boolean FuzzyEquals(String a, String b)
	{
		if (a == b) { return true; }
		if (a == null)
		{
			a = "";
		}
		if (b == null)
		{
			b = "";
		}

		a = a.trim();
		b = b.trim();

		return a.equalsIgnoreCase(b);
	}

	public static <T extends Enum<T>> T ParseEnum(Class<T> _class, String input)
	{
		return ParseEnum(_class, input, null);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T ParseEnum(Class<T> _class, String input, T fallbackValue)
	{
		if (input != null && input.length() > 0)
		{

			for (Field f: _class.getDeclaredFields())
			{
				try
				{
					if (f.isEnumConstant())
					{
						if (FuzzyEquals(f.getName(), input)) { return (T)f.get(null); }
					}
				}
				catch (Throwable ignoreMe)
				{ /* do nothing */}
			}
		}

		return fallbackValue;
	}

	public static int RndBetween(Random rnd, int low, int high)
	{
		double range = high - low;
		range *= rnd.nextDouble();
		range += low;
		range = Math.round(range);
		if (range < low)
		{
			range = low;
		}
		if (range > high)
		{
			range = high;
		}

		return (int)range;
	}

	public static void DoLine(int x0, int y0, int x1, int y1, Func2<Integer, Integer, Boolean> func)
	{
		if (func != null)
		{
			int dx = Math.abs(x1 - x0);
			int dy = Math.abs(y1 - y0);

			int sx = x0 < x1 ? 1 : -1;
			int sy = y0 < y1 ? 1 : -1;

			int err = dx - dy;
			int e2;

			while (true)
			{
				try
				{
					if (!func.func(x0, y0))
					{
						break;
					}
				}
				catch (Throwable ignore)
				{
					break;
				}

				if (x0 == x1 && y0 == y1) break;

				e2 = 2 * err;
				if (e2 > -dy)
				{
					err = err - dy;
					x0 = x0 + sx;
				}

				if (e2 < dx)
				{
					err = err + dx;
					y0 = y0 + sy;
				}
			}
		}
	}

	// #region LINQ-like helpers

	public static <T> Iterable<T> Where(final Iterable<T> input, final Predicate<T> predicate)
	{
		if (predicate == null && input != null) { return input; }
		if (input == null) { return new ArrayList<T>(); } // empty collection

		final Iterator<T> inner = input.iterator();
		if (inner == null) { return new ArrayList<T>(); } // empty collection

		return new Iterable<T>()
		{
			@Override
			public Iterator<T> iterator()
			{
				return new Iterator<T>()
				{
					private boolean hasItem = false;
					private T _next = null;

					private T GetNext()
					{
						while (!hasItem && inner.hasNext())
						{
							T item = inner.next();
							if (predicate.test(item))
							{
								_next = item;
								hasItem = true;
							}
						}

						if (!hasItem)
						{
							_next = null;
							return null;
						}

						return _next;
					}

					@Override
					public boolean hasNext()
					{
						GetNext();
						return hasItem;
					}

					@Override
					public T next()
					{
						T next = GetNext();
						hasItem = false;
						return next;
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException("remove not supported");
					}

				};
			}

		};
	}

	public static <T> boolean Any(final Iterable<T> input)
	{
		if (input != null)
		{
			for (T obj: input)
			{
				return true;
			}
		}

		return false;
	}

	public static double Min(final double[] input)
	{
		if (input == null || input.length < 1) { return 0; }

		double output = input[0];
		for (int i = 1; i < input.length; i++)
		{
			if (input[i] < output)
			{
				output = input[i];
			}
		}

		return output;
	}

	public static double Max(final double[] input)
	{
		if (input == null || input.length < 1) { return 0; }

		double output = input[0];
		for (int i = 1; i < input.length; i++)
		{
			if (input[i] > output)
			{
				output = input[i];
			}
		}

		return output;
	}

	// public static long Avg(Iterable<Long> it)
	// {
	// long result = 0;
	// long count = 0;
	//
	// if (it != null)
	// {
	// for (long value: it)
	// {
	// result += value;
	// count++;
	// }
	// }
	//
	// if (count > 0)
	// {
	// result /= count;
	// }
	// return result;
	// }

	// #endregion

	// #region Array Serialization

	private static <T> String __ConvertArrayToString(T[] args, Func<T, String> converter)
	{
		StringBuilder sb = new StringBuilder();

		boolean first = true;

		if (args != null && converter != null)
		{
			for (T item: args)
			{
				try
				{
					String value = first ? "" : ", ";
					value += converter.func(item);
					sb.append(value);
					first = false;
				}
				catch (Throwable ignore)
				{
					// skip this item!
				}
			}
		}

		return sb.toString();
	}

	private static <T> List<T> __ConvertStringToList(String input, Func<String, T> converter)
	{
		List<T> output = new ArrayList<T>();
		if (input != null && input.length() > 0 && converter != null)
		{
			String[] results = input.split("\\s*,\\s*");
			for (String result: results)
			{
				try
				{
					if (result != null)
					{
						output.add(converter.func(result));
					}
				}
				catch (Throwable ignore)
				{ /* skip this field */}
			}
		}

		return output;
	}

	public static String ConvertIntegersToString(Integer... args)
	{
		return __ConvertArrayToString(args, new Func<Integer, String>()
		{
			@Override
			public String func(Integer input)
			{
				return Integer.toString(input.intValue());
			}
		});
	}

	public static List<Integer> ConvertStringToIntegers(String input)
	{
		return __ConvertStringToList(input, new Func<String, Integer>()
		{
			@Override
			public Integer func(String input)
			{
				return Integer.parseInt(input);
			}
		});
	}

	public static String ConvertDoublesToString(Double... args)
	{
		return __ConvertArrayToString(args, new Func<Double, String>()
		{
			@Override
			public String func(Double input)
			{
				return Double.toString(input.doubleValue());
			}
		});
	}

	public static List<Double> ConvertStringToDoubles(String input)
	{
		return __ConvertStringToList(input, new Func<String, Double>()
		{
			@Override
			public Double func(String input)
			{
				return Double.parseDouble(input);
			}
		});
	}

	// #endregion

	// #region GetDistance / GetInverseDistance

	public static int GetDistance(ChunkCoordinates coords1, ChunkCoordinates coords2)
	{
		if (coords2 == null)
		{
			coords2 = new ChunkCoordinates();
		}
		return GetDistance(coords1, coords2.posX, coords2.posY, coords2.posZ);
	}

	public static int GetDistance(ChunkCoordinates coords, int x, int y, int z)
	{
		if (coords == null)
		{
			coords = new ChunkCoordinates();
		}
		return GetDistance(coords.posX, coords.posY, coords.posZ, x, y, z);
	}

	public static int GetDistance(int x1, int y1, int z1, int x2, int y2, int z2)
	{
		return (int)Math.round(GetDistance((double)x1, (double)y1, (double)z1, (double)x2, (double)y2, (double)z2));
	}

	public static double GetDistance(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1) + (z2 - z1) * (z2 - z1));
	}

	// #endregion
}
