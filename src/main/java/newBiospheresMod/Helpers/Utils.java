/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import newbiospheresmod.BlockData;

public class Utils {
  private static <T> String convertArrayToString(final T[] args, final Func<T, String> converter) {
    final StringBuilder sb = new StringBuilder();

    boolean first = true;

    if ((args != null) && (converter != null)) {
      for (final T item : args) {
        try {
          String value = first ? "" : ", ";
          value += converter.func(item);
          sb.append(value);
          first = false;
        } catch (final Throwable ignore) {
          // skip this item!
        }
      }
    }

    return sb.toString();
  }

  private static <T> List<T> convertStringToList(final String input, final Func<String, T> converter) {
    final List<T> output = new ArrayList<T>();
    if ((input != null) && (input.length() > 0) && (converter != null)) {
      final String[] results = input.split("\\s*,\\s*");
      for (final String result : results) {
        try {
          if (result != null) {
            output.add(converter.func(result));
          }
        } catch (final Throwable ignore) { /* skip this field */
        }
      }
    }

    return output;
  }

  @SafeVarargs
  public static <T> Predicate<T> and(final Predicate<T>... _clauses) {
    final ArrayList<Predicate<T>> clauses = Utils.filterPredicates(_clauses);
    if (clauses.size() < 1) {
      return null;
    }
    if (clauses.size() == 1) {
      return clauses.get(0);
    }

    return new Predicate<T>() {
      @Override
      public boolean test(final T value) {
        for (final Predicate<T> clause : clauses) {
          if (!clause.test(value)) {
            return false;
          }
        }

        return true;
      }
    };
  }

  @SuppressWarnings("unused")
  public static <T> boolean any(final Iterable<T> input) {
    if (input != null) {
      for (final T obj : input) {
        return true;
      }
    }

    return false;
  }

  public static void assertTrue(final boolean test) {
    Utils.assertTrue(test, null, false, 0);
  }

  public static void assertTrue(final boolean test, final int callerOffset) {
    Utils.assertTrue(test, null, false, callerOffset);
  }

  public static void assertTrue(final boolean test, final String failMessage) {
    Utils.assertTrue(test, failMessage, false, 0);
  }

  public static void assertTrue(final boolean test, final String failMessage, final boolean shouldExplode) {
    Utils.assertTrue(test, failMessage, shouldExplode, 0);
  }

  public static void assertTrue(final boolean test, final String failMessage, final boolean shouldExplode,
      final int callerOffset) {
    if (!test) {
      // Assert Failed at this point, not too worried about performance

      final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      StackTraceElement caller = null;

      int callerIdx;
      for (callerIdx = 1; callerIdx < stack.length; callerIdx++) {
        caller = stack[callerIdx];
        if (caller.getMethodName() != "Assert") {
          break;
        }
      }

      if (callerOffset != 0) {
        caller = stack[callerIdx + callerOffset];
      }

      final StringBuilder sb = new StringBuilder();
      sb.append(ModConsts.ModId);
      sb.append("(");
      sb.append(ModConsts.ModVersion);
      sb.append(") ASSERT FAILED! @ ");
      sb.append(caller.getClassName());
      sb.append("::");
      sb.append(caller.getMethodName());
      sb.append(" (Line: ");
      sb.append(caller.getLineNumber());
      sb.append(")");

      if ((failMessage != null) && (failMessage.length() > 0)) {
        sb.append(": ");
        sb.append(failMessage);
      }

      final String err = sb.toString();
      System.out.println(err);
      if (shouldExplode) {
        throw new Error(err);
      }
    }
  }

  public static void assertTrue(final boolean test, final String failMessage, final int callerOffset) {
    Utils.assertTrue(test, failMessage, false, callerOffset);
  }

  public static String convertDoublesToString(final Double... args) {
    return Utils.convertArrayToString(args, new Func<Double, String>() {
      @Override
      public String func(final Double input) {
        return Double.toString(input.doubleValue());
      }
    });
  }

  public static String convertIntegersToString(final Integer... args) {
    return Utils.convertArrayToString(args, new Func<Integer, String>() {
      @Override
      public String func(final Integer input) {
        return Integer.toString(input.intValue());
      }
    });
  }

  public static List<Double> convertStringToDoubles(final String input) {
    return Utils.convertStringToList(input, new Func<String, Double>() {
      @Override
      public Double func(final String input) {
        return Double.parseDouble(input);
      }
    });
  }

  public static List<Integer> convertStringToIntegers(final String input) {
    return Utils.convertStringToList(input, new Func<String, Integer>() {
      @Override
      public Integer func(final String input) {
        return Integer.parseInt(input);
      }
    });
  }

  public static void doLine(int x0, int y0, final int x1, final int y1, final Func2<Integer, Integer, Boolean> func) {
    if (func != null) {
      final int dx = Math.abs(x1 - x0);
      final int dy = Math.abs(y1 - y0);

      final int sx = x0 < x1 ? 1 : -1;
      final int sy = y0 < y1 ? 1 : -1;

      int err = dx - dy;
      int e2;

      while (true) {
        try {
          if (!func.func(x0, y0)) {
            break;
          }
        } catch (final Throwable ignore) {
          break;
        }

        if ((x0 == x1) && (y0 == y1)) {
          break;
        }

        e2 = 2 * err;
        if (e2 > -dy) {
          err = err - dy;
          x0 = x0 + sx;
        }

        if (e2 < dx) {
          err = err + dx;
          y0 = y0 + sy;
        }
      }
    }
  }

  @SafeVarargs
  private static <T> ArrayList<Predicate<T>> filterPredicates(final Predicate<T>... clauses) {
    final ArrayList<Predicate<T>> toRet = new ArrayList<Predicate<T>>();

    if ((clauses != null) && (clauses.length > 0)) {
      // Pre-Filter the nulls, quickly

      for (final Predicate<T> clause : clauses) {
        if (clause != null) {
          toRet.add(clause);
        }
      }
    }

    return toRet;
  }

  public static boolean fuzzyEquals(String a, String b) {
    if (a == b) {
      return true;
    }
    if (a == null) {
      a = "";
    }
    if (b == null) {
      b = "";
    }

    a = a.trim();
    b = b.trim();

    return a.equalsIgnoreCase(b);
  }

  public static ChunkCoordinates getCoords(final ChunkCoordinates copyMe) {
    if (copyMe == null) {
      return Utils.getCoords(0, 0, 0);
    }
    return Utils.getCoords(copyMe.posX, copyMe.posY, copyMe.posZ);
  }

  // #region LINQ-like helpers

  public static ChunkCoordinates getCoords(final double x, final double y, final double z) {
    return Utils.getCoords((int) Math.round(x), (int) Math.round(y), (int) Math.round(z));
  }

  public static ChunkCoordinates getCoords(final Entity e) {
    if (e == null) {
      return Utils.getCoords(0, 0, 0);
    }
    return Utils.getCoords(e.posX, e.posY, e.posZ);
  }

  public static ChunkCoordinates getCoords(final int x, final int y, final int z) {
    final ChunkCoordinates coords = new ChunkCoordinates();
    coords.posX = x;
    coords.posY = y;
    coords.posZ = z;

    return coords;
  }

  public static int getDistance(final ChunkCoordinates coords1, ChunkCoordinates coords2) {
    if (coords2 == null) {
      coords2 = new ChunkCoordinates();
    }
    return Utils.getDistance(coords1, coords2.posX, coords2.posY, coords2.posZ);
  }

  public static int getDistance(ChunkCoordinates coords, final int x, final int y, final int z) {
    if (coords == null) {
      coords = new ChunkCoordinates();
    }
    return Utils.getDistance(coords.posX, coords.posY, coords.posZ, x, y, z);
  }

  public static double getDistance(final double x1, final double y1, final double z1, final double x2, final double y2,
      final double z2) {
    return Math.sqrt(((y2 - y1) * (y2 - y1)) + ((x2 - x1) * (x2 - x1)) + ((z2 - z1) * (z2 - z1)));
  }

  public static int getDistance(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
    return (int) Math.round(Utils.getDistance((double) x1, (double) y1, (double) z1, (double) x2, (double) y2,
        (double) z2));
  }

  public static String getFileExtension(final String path) {
    String extension = "";

    if ((path != null) && (path.length() > 0)) {
      final int idx = path.lastIndexOf('.');
      if (idx >= 0) {
        final int lastSlashIdx = path.lastIndexOf(java.io.File.separatorChar);

        if (idx > lastSlashIdx) {
          extension = path.substring(idx + 1);
        }
      }
    }

    return extension;
  }

  public static GameRules getGameRules(final World world) {
    if (world != null) {
      final WorldInfo info = world.getWorldInfo();
      if (info != null) {
        return info.getGameRulesInstance();
      }
    }
    return null;
  }

  public static String getName(final Object obj) {
    if (obj == null) {
      return "(null)";
    }

    if (obj instanceof BlockData) {
      return ((BlockData) obj).toString();
    }
    if (obj instanceof Block) {
      return BlockData.toString((Block) obj, 0);
    }

    String name = obj.getClass().getSimpleName();
    if ((name == null) || (name.length() < 1)) {
      name = obj.getClass().getName();
    }

    return name;
  }

  public static boolean isPlayer(final Entity e) {
    if (e == null) {
      return false;
    }

    if (e instanceof EntityPlayer) {
      return true;
    }

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

  public static double max(final double[] input) {
    if ((input == null) || (input.length < 1)) {
      return 0;
    }

    double output = input[0];
    for (int i = 1; i < input.length; i++) {
      if (input[i] > output) {
        output = input[i];
      }
    }

    return output;
  }

  public static double min(final double[] input) {
    if ((input == null) || (input.length < 1)) {
      return 0;
    }

    double output = input[0];
    for (int i = 1; i < input.length; i++) {
      if (input[i] < output) {
        output = input[i];
      }
    }

    return output;
  }

  public static <T> Predicate<T> notNull() {
    return new Predicate<T>() {
      @Override
      public boolean test(final T value) {
        return value != null;
      }
    };
  }

  public static <T> Predicate<T> notNull(final Class<T> clazz) {
    // Man, the Java type system is a fucking nightmare.

    // No comment.

    return new Predicate<T>() {
      @Override
      public boolean test(final T value) {
        return value != null;
      }
    };
  }

  @SafeVarargs
  public static <T> Predicate<T> or(final Predicate<T>... _clauses) {
    final ArrayList<Predicate<T>> clauses = Utils.filterPredicates(_clauses);
    if (clauses.size() < 1) {
      return null;
    }
    if (clauses.size() == 1) {
      return clauses.get(0);
    }

    return new Predicate<T>() {
      @Override
      public boolean test(final T value) {
        for (final Predicate<T> clause : clauses) {
          if (clause.test(value)) {
            return true;
          }
        }

        return false;
      }
    };
  }

  public static <T extends Enum<T>> T parseEnum(final Class<T> _class, final String input) {
    return Utils.parseEnum(_class, input, null);
  }

  // #endregion

  // #region GetDistance / GetInverseDistance

  @SuppressWarnings("unchecked")
  public static <T extends Enum<T>> T parseEnum(final Class<T> _class, final String input, final T fallbackValue) {
    if ((input != null) && (input.length() > 0)) {

      for (final Field f : _class.getDeclaredFields()) {
        try {
          if (f.isEnumConstant()) {
            if (Utils.fuzzyEquals(f.getName(), input)) {
              return (T) f.get(null);
            }
          }
        } catch (final Throwable ignoreMe) { /* do nothing */
        }
      }
    }

    return fallbackValue;
  }

  public static int rndBetween(final Random rnd, final int low, final int high) {
    double range = high - low;
    range *= rnd.nextDouble();
    range += low;
    range = Math.round(range);
    if (range < low) {
      range = low;
    }
    if (range > high) {
      range = high;
    }

    return (int) range;
  }

  public static <T> Iterable<T> toIterable(final T[] input) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private int idx = 0;

          @Override
          public boolean hasNext() {
            return ((input != null) && (this.idx < input.length));
          }

          @Override
          public T next() {
            if (this.hasNext()) {
              return input[this.idx++];
            }

            return null;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException("remove not supported");
          }
        };
      }

    };
  }

  public static <T> ArrayList<T> toList(final Iterable<T> input) {
    final ArrayList<T> output = new ArrayList<T>();
    if (input != null) {
      for (final T item : input) {
        output.add(item);
      }
    }

    return output;
  }

  // #endregion

  // #region File System Utilities

  public static <T> Iterable<T> where(final Iterable<T> input, final Predicate<T> predicate) {
    if ((predicate == null) && (input != null)) {
      return input;
    }
    if (input == null) {
      return new ArrayList<T>();
    } // empty collection

    final Iterator<T> inner = input.iterator();
    if (inner == null) {
      return new ArrayList<T>();
    } // empty collection

    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {
          private T next = null;
          private boolean hasItem = false;

          private T getNext() {
            while (!this.hasItem && inner.hasNext()) {
              final T item = inner.next();
              if (predicate.test(item)) {
                this.next = item;
                this.hasItem = true;
              }
            }

            if (!this.hasItem) {
              this.next = null;
              return null;
            }

            return this.next;
          }

          @Override
          public boolean hasNext() {
            this.getNext();
            return this.hasItem;
          }

          @Override
          public T next() {
            final T next = this.getNext();
            this.hasItem = false;
            return next;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException("remove not supported");
          }

        };
      }

    };
  }

  // #endregion

}
