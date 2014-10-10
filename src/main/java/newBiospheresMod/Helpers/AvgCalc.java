/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Helpers;

public class AvgCalc
{
	private double average = 0d;
	private double count = 0d;

	public int getCount()
	{
		return (int)count;
	}

	public double getAverage()
	{
		return average;
	}

	public void addValue(double amount)
	{
		double newAverage = average * count;
		average = (newAverage + amount) / (++count);
	}
}
