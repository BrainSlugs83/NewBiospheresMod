package newBiospheresMod.Models;

import java.util.Random;

import newBiospheresMod.BiosphereChunkProvider;
import newBiospheresMod.Helpers.ModConsts;

public class SphereChunk
{
	// TODO: SPLIT THIS INTO A SECOND GRID CHUNK CLASS, SO THAT WE DON'T HAVE TO REDO ALL THE WORK / STORE EVERYTHING
	// MULTIPLE TIMES FOR EACH CHUNK IN THE GRID

	public final int chunkX, chunkZ;

	public final BiosphereChunkProvider chunkProvider;
	public final Sphere masterSphere;

	public final boolean isNoiseEnabled;
	public final NoiseChunk noise;

	public SphereChunk(BiosphereChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		this.chunkProvider = chunkProvider;
		this.chunkX = chunkX;
		this.chunkZ = chunkZ;

		ModConfig cfg = this.chunkProvider.config;
		this.isNoiseEnabled = cfg.isNoiseEnabled();

		noise = isNoiseEnabled ? NoiseChunk.get(this.chunkProvider.world, chunkX, chunkZ,
			this.chunkProvider.noiseGenerator, cfg.getScale()) : null;

		masterSphere = Sphere.get(chunkProvider, chunkX, chunkZ);
	}

	public Random GetPhaseRandom(String phase)
	{
		return masterSphere.GetPhaseRandom(phase, chunkX, chunkZ);
	}

	public int getChunkBoundSurfaceLevel(int boundX, int boundZ)
	{
		if (this.noise != null) { return noise.getChunkBoundSurfaceLevel(boundX, boundZ); }
		return ModConsts.SEA_LEVEL;
	}
}
