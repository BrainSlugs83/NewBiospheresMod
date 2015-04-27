/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

public class AvgCalc {
  private double average = 0d;
  private double count = 0d;

  public void addValue(final double amount) {
    final double newAverage = this.average * this.count;
    this.average = (newAverage + amount) / (++this.count);
  }

  public double getAverage() {
    return this.average;
  }

  public int getCount() {
    return (int) this.count;
  }
}
