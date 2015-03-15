/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Helpers;

public class ModConsts
{
	public static final String ModId = "New Biospheres Mod";
	public static final String ModVersion = "0.83";

	public static final int WORLD_HEIGHT = 128;

	public static final int WORLD_MAX_Y = WORLD_HEIGHT - 1;
	public static final int WORLD_MIN_Y = 0;

	public static final int LAVA_LEVEL = 50;

	public static final boolean DEBUG = false;

	// In a chunk array that is [16 * 16 * WORLD_HEIGHT] you can address any block via:
	// (x << ModConsts.xShift) | (z << ModConsts.zShift) | y
	// Where (x, and z are between 0 and 15, and y is between 0 and (WORLD_HEIGHT - 1).
	public static final int xShift = 11;
	public static final int zShift = 7;

}
