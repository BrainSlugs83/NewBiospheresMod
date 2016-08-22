package newBiospheresMod.Generators;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class WorldGenEndFissure {

	public boolean generate(World world, Random rand, int x, int y, int z) {

		boolean success = true;
		final int[][] portalOffsets = new int[][] { { 0, 0 }, { -1, 0 }, { -2, 0 }, { 2, 1 }, { 1, 1 }, { 0, 1 },
				{ -1, 1 }, { -2, 1 }, { -3, 1 }, { 1, 2 }, { 0, 2 }, { -1, 2 }, { -2, 2 }, { -3, 2 }, { -1, 3 },
				{ -2, 3 }, { -3, 3 }, { -4, 3 }, { -3, 4 }, { -4, 4 } };

		final int[][] frameOffsets = new int[][] { { -3, 0 }, { -4, 1 }, { -4, 2 }, { -5, 3 }, { -5, 4 }, { -5, 5 },
				{ -4, 5 }, { -3, 5 }, { -2, 4 }, { -1, 4 }, { 0, 3 }, { 1, 3 }, { 2, 2 }, { 3, 1 } };

		for (int[] portalOffset : portalOffsets) {
			boolean a = setPortal(world, x + portalOffset[0], y, z + portalOffset[1]);
			boolean b = setPortal(world, x - portalOffset[0], y, z - portalOffset[1]);
			if (!a || !b) {
				success = false;
			}
		}

		for (int[] frameOffset : frameOffsets) {
			boolean a = setFrame(world, x + frameOffset[0], y, z + frameOffset[1]);
			boolean b = setFrame(world, x - frameOffset[0], y, z - frameOffset[1]);
			if (!a || !b) {
				success = false;
			}
		}

		return success;

	}

	boolean setPortal(World world, int x, int y, int z) {
		boolean a = world.setBlock(x, y + 1, z, Blocks.air);
		boolean b = world.setBlock(x, y + 1, z, Blocks.air);
		boolean c = world.setBlock(x, y + 2, z, Blocks.air);
		boolean d = world.setBlock(x, y + 3, z, Blocks.air);
		boolean e = world.setBlock(x, y, z, Blocks.end_portal);
		boolean f = world.setBlock(x, y - 1, z, Blocks.bedrock);

		return a && b && c && d && e && f;
	}

	boolean setFrame(World world, int x, int y, int z) {
		return world.setBlock(x, y, z, Blocks.bedrock);
	}

}
