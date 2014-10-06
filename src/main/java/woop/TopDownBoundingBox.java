package woop;

public class TopDownBoundingBox
{
	public final int x1, z1, x2, z2;

	public TopDownBoundingBox(int x1, int z1, int x2, int z2)
	{
		this.x1 = x1;
		this.z1 = z1;
		this.x2 = x2;
		this.z2 = z2;
	}

	public static TopDownBoundingBox FromChunk(int chunkX, int chunkZ)
	{
		chunkX <<= 4;
		chunkZ <<= 4;

		return new TopDownBoundingBox(chunkX, chunkZ, chunkX + 15, chunkZ + 15);
	}

	public boolean CollidesWith(TopDownBoundingBox box)
	{
		if (box == this) { return true; }
		if (box == null) { return false; }

		if (this.x2 < box.x1) { return false; }
		if (this.z2 < box.z1) { return false; }
		if (this.x1 > box.x2) { return false; }
		if (this.z1 > box.z2) { return false; }

		return true;
	}
}
