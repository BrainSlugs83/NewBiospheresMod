package woop;

public class AvgCalc
{
	private double average = 0d;
	private double count = 0d;

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
