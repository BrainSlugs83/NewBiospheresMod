package newBiospheresMod;

import net.minecraft.util.ChunkCoordinates;

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

	public static TopDownBoundingBox FromCircle(int cx, int cz, int r)
	{
		return new TopDownBoundingBox(cx - r, cz - r, cx + r, cz + r);
	}

	public static TopDownBoundingBox FromArray(Iterable<ChunkCoordinates> coords)
	{
		if (coords != null)
		{
			boolean first = true;
			int minX, minZ, maxX, maxZ;
			minX = minZ = maxX = maxZ = 0;

			for (ChunkCoordinates coord: coords)
			{
				if (coord != null)
				{
					if (first)
					{
						minX = maxX = coord.posX;
						minZ = maxZ = coord.posZ;
						first = false;
					}
					else
					{
						if (coord.posX < minX)
						{
							minX = coord.posX;
						}
						if (coord.posX > maxX)
						{
							maxX = coord.posX;
						}
						if (coord.posZ < minZ)
						{
							minZ = coord.posZ;
						}
						if (coord.posZ > maxZ)
						{
							maxZ = coord.posZ;
						}
					}
				}
			}

			if (!first) { return new TopDownBoundingBox(minX, minZ, maxX, maxZ); }
		}

		return null;
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

	public boolean CollidesWith(int x, int z)
	{
		if (x < this.x1) { return false; }
		if (z < this.z1) { return false; }
		if (x > this.x2) { return false; }
		if (z > this.z2) { return false; }

		return true;
	}
}
