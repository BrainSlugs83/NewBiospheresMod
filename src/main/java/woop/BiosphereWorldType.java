package woop;

import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

public class BiosphereWorldType extends WorldType
{
	private static final LruCacheList<World> BiosphereWorlds = new LruCacheList<World>(3);

	private static final String IsBiosphereWorldKey = "Is Biosphere World {50D04041-9ADB-4F60-95A6-DD7AB56E2F73}";

	public static boolean IsBiosphereWorld(World world)
	{
		if (world != null)
		{
			if (BiosphereWorlds.Contains(world)) { return true; }

			GameRules rules = GetGameRules(world);
			if (rules != null)
			{
				if (rules.getGameRuleBooleanValue(IsBiosphereWorldKey))
				{
					EnsureWorldIsTracked(world);
					return true;
				}
			}
		}

		return false;
	}

	public BiosphereWorldType(String s)
	{
		super(s);
	}

	@Override
	public WorldChunkManager getChunkManager(World world)
	{
		BiosphereWorlds.Push(world);
		return new BiosphereChunkManager(world);
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String params)
	{
		BiosphereWorlds.Push(world);
		return BiosphereChunkProvider.get(world);
	}

	@Override
	public boolean hasVoidParticles(boolean flag)
	{
		return false;
	}

	public int getSeaLevel(World world)
	{
		BiosphereWorlds.Push(world);
		return ModConsts.SEA_LEVEL + 1;
	}

	@Override
	public double voidFadeMagnitude()
	{
		return 1.0D;
	}

	private static void EnsureWorldIsTracked(World world)
	{
		if (world != null)
		{
			BiosphereWorlds.Push(world);
			GameRules rules = GetGameRules(world);
			if (rules != null)
			{
				rules.addGameRule(IsBiosphereWorldKey, "true");
			}
		}
	}

	private static GameRules GetGameRules(World world)
	{
		if (world != null)
		{
			WorldInfo info = world.getWorldInfo();
			if (info != null) { return info.getGameRulesInstance(); }
		}
		return null;
	}
}
