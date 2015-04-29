/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

public class ModConsts {
  public static final boolean DEBUG = false;
  public static final int LAVA_LEVEL = 50;
  public static final String ModId = "NewBiospheresMod";

  public static final String ModVersion = "0.85";

  public static final String OldModId = "New Biospheres Mod";
  public static final int WORLD_HEIGHT = 256;

  public static final int WORLD_MAX_Y = ModConsts.WORLD_HEIGHT - 1;

  public static final int WORLD_MIN_Y = 0;

  public static int getChunkArrayIndex(final int x, final int y, final int z) {
    return (x * ModConsts.WORLD_HEIGHT * 16) + (z * ModConsts.WORLD_HEIGHT) + y;
  }

  public static int getChunkArraySize() {
    return 16 * ModConsts.WORLD_HEIGHT * 16;
  }
}
