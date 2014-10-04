package woop;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;

public class BiosphereGenerator extends WorldType
{
	public BiosphereGenerator(String s)
	{
		super(s);
	}

	public WorldChunkManager getChunkManager(World world)
	{
		return new BiosphereWeather(world);
	}

	public IChunkProvider getChunkGenerator(World world, String params)
	{
		return new BiosphereGen(world);
	}

	public boolean hasVoidParticles(boolean flag)
	{
		return false;
	}

	public int getSeaLevel(World world)
	{
		return 64;
	}

	public double voidFadeMagnitude()
	{
		return 1.0D;
	}
}
