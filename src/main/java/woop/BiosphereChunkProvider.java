package woop;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.NoiseGeneratorOctaves;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCactus;
import net.minecraft.world.gen.feature.WorldGenClay;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenerator;
import akka.japi.Creator;
import akka.japi.Predicate;

public class BiosphereChunkProvider implements IChunkProvider
{
	private static LruCacheList<BiosphereChunkProvider> chunkProviders = new LruCacheList<BiosphereChunkProvider>(3);

	public static BiosphereChunkProvider get(final World world)
	{
		return chunkProviders.FindOrAdd(new Predicate<BiosphereChunkProvider>()
		{
			@Override
			public boolean test(BiosphereChunkProvider chunkProvider)
			{
				return chunkProvider.world == world;
			}
		}, new Creator<BiosphereChunkProvider>()
		{
			@Override
			public BiosphereChunkProvider create()
			{
				return new BiosphereChunkProvider(world);
			}
		});
	}

	public final World world;
	public final ModConfig config;
	public final NoiseGeneratorOctaves noiseGenerator;

	/**
	 * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
	 */
	public boolean getMapFeaturesEnabled()
	{
		return world.getWorldInfo().isMapFeaturesEnabled();
	}

	private MapGenBase caveGen = new BiosphereMapGen();

	public static final int zShift = 7;
	public static final int xShift = 11;
	public final long worldSeed;

	private final LruCacheList<SphereChunk> chunks = new LruCacheList<SphereChunk>(10);

	public synchronized SphereChunk GetSphereChunk(final int chunkX, final int chunkZ)
	{
		final BiosphereChunkProvider _this = this;

		return chunks.FindOrAdd(new Predicate<SphereChunk>()
		{
			@Override
			public boolean test(SphereChunk chunk)
			{
				return chunk.chunkX == chunkX && chunk.chunkZ == chunkZ;
			}
		}, new Creator<SphereChunk>()
		{
			@Override
			public SphereChunk create()
			{
				return new SphereChunk(_this, chunkX, chunkZ);
			}
		});
	}

	private BiosphereChunkProvider(World world)
	{
		this.world = world;
		this.worldSeed = world.getSeed();
		this.config = ModConfig.get(world);

		if (this.config.isNoiseEnabled())
		{
			noiseGenerator = new NoiseGeneratorOctaves(new Random(this.worldSeed), 4);
		}
		else
		{
			noiseGenerator = null;
		}
	}

	private void GenerateChunk(int chunkX, int chunkZ, Block[] blocks)
	{
		SphereChunk chunk = GetSphereChunk(chunkX, chunkZ);

		final TopDownBoundingBox chunkBox = TopDownBoundingBox.FromChunk(chunkX, chunkZ);

		if (Utils.Any(Utils.Where(chunk.getBoundingBoxes(), new Predicate<TopDownBoundingBox>()
		{
			@Override
			public boolean test(TopDownBoundingBox box)
			{
				return chunkBox.CollidesWith(box);
			}
		})))
		{
			// we collided with something like a sphere, ore orb, bridge, or other feature.
			__GenerateChunk(chunkX, chunkZ, blocks, chunk);
		}
		else
		{
			// this chunk is outside and is not a part of any features
			Arrays.fill(blocks, config.getOutsideFillerBlock());
		}
	}

	private void __GenerateChunk(int chunkX, int chunkZ, Block[] blocks, SphereChunk chunk)
	{
		final int baseX = chunkX << 4;
		final int baseZ = chunkZ << 4;
		final int bridgeWidth = config.getBridgeWidth();
		final Block outsideFillerBlock = config.getOutsideFillerBlock();

		Random rnd = chunk.GetPhaseRandom("GenerateChunk");

		for (int zo = 0; zo < 16; ++zo)
		{
			for (int xo = 0; xo < 16; ++xo)
			{
				int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

				for (int rawY = ModConsts.WORLD_MAX_Y; rawY >= ModConsts.WORLD_MIN_Y; rawY--)
				{
					int idx = (xo << xShift) | (zo << zShift) | rawY;
					Block block = Blocks.air;

					int rawX = baseX + xo;
					int rawZ = baseZ + zo;

					int sphereDistance = chunk.getMainDistance(rawX, rawY, rawZ);
					int orbDistance = chunk.getOrbDistance(rawX, rawY, rawZ);
					int lakeDistance = chunk.getLakeDistance(rawX, rawY, rawZ);

					if (rawY > midY)
					{
						if (chunk.scaledSphereRadius == sphereDistance)
						{
							if (rawY >= midY + 4 || Math.abs(rawX - chunk.sphereLocation.posX) > bridgeWidth
									&& Math.abs(rawZ - chunk.sphereLocation.posZ) > bridgeWidth)
							{
								block = config.getDomeBlock();
							}
						}
						else if (chunk.hasLake && config.getNoiseEnabled() && chunk.biome != BiomeGenBase.desert
								&& (lakeDistance > chunk.scaledLakeRadius && lakeDistance <= chunk.scaledLakeEdgeRadius))
						{
							if (rawY == chunk.lakeLocation.posY)
							{
								block = chunk.biome.topBlock;
							}
							else if (rawY < chunk.lakeLocation.posY)
							{
								block = chunk.biome.fillerBlock;
							}
						}
						else if (chunk.hasLake && config.getNoiseEnabled() && chunk.biome != BiomeGenBase.desert
								&& lakeDistance <= chunk.scaledLakeRadius)
						{
							if (rawY == chunk.lakeLocation.posY && chunk.biome == BiomeGenBase.icePlains)
							{
								block = Blocks.ice;
							}
							else if (rawY <= chunk.lakeLocation.posY)
							{
								block = chunk.GetLakeBlock();
							}
						}
						else if (config.doesNeedProtectionGlass()
								&& rawY <= midY + 4
								&& sphereDistance > chunk.scaledSphereRadius
								&& (Math.abs(rawX - chunk.sphereLocation.posX) == bridgeWidth || Math.abs(rawZ
										- chunk.sphereLocation.posZ) == bridgeWidth))
						{
							block = config.getDomeBlock();
						}
						else if (config.doesNeedProtectionGlass()
								&& rawY == midY + 4
								&& sphereDistance > chunk.scaledSphereRadius
								&& (Math.abs(rawX - chunk.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
										- chunk.sphereLocation.posZ) < bridgeWidth))
						{
							block = config.getDomeBlock();
						}
						else if (config.doesNeedProtectionGlass()
								&& rawY < midY + 4
								&& sphereDistance > chunk.scaledSphereRadius
								&& (Math.abs(rawX - chunk.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
										- chunk.sphereLocation.posZ) < bridgeWidth))
						{
							block = Blocks.air;
						}
						else if (config.doesNeedProtectionGlass() && sphereDistance > chunk.scaledSphereRadius)
						{
							block = outsideFillerBlock;
						}
						else if (rawY == midY + 1
								&& sphereDistance > chunk.scaledSphereRadius
								&& (Math.abs(rawX - chunk.sphereLocation.posX) == bridgeWidth || Math.abs(rawZ
										- chunk.sphereLocation.posZ) == bridgeWidth))
						{
							block = config.getBridgeRailBlock();
						}
					}
					else if (chunk.scaledSphereRadius == sphereDistance)
					{
						block = Blocks.stone;
					}
					else if (chunk.hasLake && chunk.biome != BiomeGenBase.desert && lakeDistance <= chunk.scaledLakeRadius)
					{
						if (rawY == chunk.lakeLocation.posY && chunk.biome == BiomeGenBase.icePlains)
						{
							block = Blocks.ice;
						}
						else if (rawY <= chunk.lakeLocation.posY)
						{
							block = chunk.GetLakeBlock();
						}
					}
					else if (chunk.hasLake && rawY < chunk.lakeLocation.posY - 1 && chunk.biome != BiomeGenBase.desert
							&& lakeDistance <= chunk.scaledLakeEdgeRadius)
					{
						if (ModConsts.DEBUG)
						{
							block = Blocks.glowstone;
						}
						else
						{
							block = (chunk.lavaLake ? Blocks.gravel : Blocks.sand);
						}
					}
					else if (sphereDistance < chunk.scaledSphereRadius)
					{
						if (rawY == midY)
						{
							block = chunk.biome.topBlock;
						}
						else if (rawY == midY - 1)
						{
							block = chunk.biome.fillerBlock;
						}
						else
						{
							block = Blocks.stone;
						}
					}
					else if (rawY == midY
							&& sphereDistance > chunk.scaledSphereRadius
							&& (Math.abs(rawX - chunk.sphereLocation.posX) < bridgeWidth + 1 || Math.abs(rawZ
									- chunk.sphereLocation.posZ) < bridgeWidth + 1))
					{
						block = config.getBridgeSupportBlock();
					}
					else if (config.doesNeedProtectionGlass() && sphereDistance > chunk.scaledSphereRadius)
					{
						block = outsideFillerBlock;
					}

					if (chunk.scaledOrbRadius == orbDistance)
					{
						block = config.getDomeBlock();
					}
					else if (orbDistance < chunk.scaledOrbRadius)
					{

						int oreChance = rnd.nextInt(500);

						if (oreChance < 5) // 1%
						{
							block = Blocks.lapis_ore;
						}
						else if (oreChance < 10) // 1%
						{
							block = Blocks.emerald_ore;
						}
						else if (oreChance < 15) // 1%
						{
							block = Blocks.diamond_ore;
						}
						else if (oreChance < 25) // 2%
						{
							block = Blocks.iron_ore;
						}
						else if (oreChance < 35) // 2%
						{
							block = Blocks.gold_ore;
						}
						else if (oreChance < 50) // 3%
						{
							block = Blocks.coal_ore;
						}
						else if (oreChance < 65) // 3%
						{
							block = Blocks.redstone_ore;
						}
						else if (oreChance < 75) // 2%
						{
							block = Blocks.quartz_ore;
						}
						else if (oreChance < 175) // 20%
						{
							block = Blocks.gravel;
						}
						else if (oreChance < 190) // 3%
						{
							block = Blocks.lava;
						}
						else
						// 62%
						{
							block = Blocks.stone;
						}
					}

					blocks[idx] = block;
				}
			}
		}
	}

	/**
	 * loads or generates the chunk at the chunk location specified
	 */
	@Override
	public Chunk loadChunk(int x, int z)
	{
		return this.provideChunk(x, z);
	}

	AvgCalc avg = new AvgCalc();
	long lastPrintedAt = Long.MIN_VALUE;

	/**
	 * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
	 * specified chunk from the map seed and chunk seed
	 */
	@Override
	public Chunk provideChunk(int x, int z)
	{
		long startedAt = System.currentTimeMillis();

		// this.setRand(x, z);
		Block[] blocks = new Block[16 * 16 * ModConsts.WORLD_HEIGHT];

		this.GenerateChunk(x, z, blocks);
		this.caveGen.func_151539_a(this, this.world, x, z, blocks); // func_151539_a == generate

		Chunk chunk = new Chunk(this.world, blocks, x, z);
		chunk.generateSkylightMap();

		if (ModConsts.DEBUG)
		{
			long now = System.currentTimeMillis();
			long elapsed = now - startedAt;
			avg.addValue(elapsed / 1000d);

			if (elapsed >= 100)
			{
				System.out.printf("BIOSPHERE PROVIDE CHUNK TOOK %.3f SECONDS!%n", (elapsed / 1000d));
			}

			if (lastPrintedAt == Long.MIN_VALUE || (now - lastPrintedAt) > 2500)
			{
				lastPrintedAt = now;

				if (avg.getCount() >= 5)
				{
					System.out.printf("PROVIDE CHUNK ON AVERAGE TAKES %.3f SECONDS.%n", avg.getAverage());
				}
			}
		}

		return chunk;
	}

	/**
	 * Checks to see if a chunk exists at x, z
	 */
	@Override
	public boolean chunkExists(int x, int z)
	{
		return true;
	}

	/**
	 * Populates chunk with ores etc etc
	 */
	@Override
	public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ)
	{
		SphereChunk chunk = GetSphereChunk(chunkX, chunkZ);
		Random rnd = chunk.GetPhaseRandom("populate");

		BlockSand.fallInstantly = true;
		int absX = chunkX << 4;
		int absZ = chunkZ << 4;

		for (int i = 0; i < 10; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);
			(new WorldGenClay(4)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 20; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);
			(new WorldGenMinable(Blocks.coal_ore, 16)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 20; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);
			(new WorldGenMinable(Blocks.iron_ore, 8)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 2; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);
			(new WorldGenMinable(Blocks.gold_ore, 8)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 8; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);
			(new WorldGenMinable(Blocks.redstone_ore, 7)).generate(this.world, rnd, x, y, z);
		}

		int treesPerChunk = chunk.biome.theBiomeDecorator.treesPerChunk;

		if (rnd.nextInt(10) == 0)
		{
			treesPerChunk++;
		}

		for (int i = 0; i < treesPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = this.world.getHeightValue(x, z);

			// func_150567_a == getRandomWorldGenForTrees
			WorldGenerator gen = chunk.biome.func_150567_a(rnd);

			gen.setScale(config.getScale(), config.getScale(), config.getScale());
			gen.generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 2; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;

			(new WorldGenFlowers(Blocks.yellow_flower)).generate(this.world, rnd, x, y, z);
		}

		if (rnd.nextInt(2) == 0)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;
			(new WorldGenFlowers(Blocks.red_flower)).generate(this.world, rnd, x, y, z);
		}

		if (rnd.nextInt(4) == 0)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;
			(new WorldGenFlowers(Blocks.brown_mushroom)).generate(this.world, rnd, x, y, z);
		}

		if (rnd.nextInt(8) == 0)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;
			(new WorldGenFlowers(Blocks.red_mushroom)).generate(this.world, rnd, x, y, z);
		}

		int l13;

		if (config.isTallGrassEnabled())
		{
			int grassPerChunk = chunk.biome.theBiomeDecorator.grassPerChunk;

			for (int i = 0; i < grassPerChunk; i++)
			{
				byte metadata = 1; // grass height maybe?

				if (chunk.biome == BiomeGenBase.desert && rnd.nextInt(3) != 0)
				{
					metadata = 2;
				}

				int x = absX + rnd.nextInt(16) + 8;
				int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
				int z = absZ + rnd.nextInt(16) + 8;

				(new WorldGenTallGrass(Blocks.tallgrass, metadata)).generate(this.world, rnd, x, y, z);
			}
		}

		for (int i = 0; i < 20; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;
			(new WorldGenReed()).generate(this.world, rnd, x, y, z);
		}

		if (rnd.nextInt(32) == 0)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16) + 8;
			(new WorldGenPumpkin()).generate(this.world, rnd, x, y, z);
		}

		if (chunk.biome == BiomeGenBase.desert)
		{
			int count = rnd.nextInt(5);

			for (int i = 0; i < count; i++)
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);

				(new WorldGenCactus()).generate(this.world, rnd, x, y, z);
			}
		}
		else if (chunk.biome == BiomeGenBase.hell)
		{
			if (rnd.nextBoolean())
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);

				(new WorldGenFire()).generate(this.world, rnd, x, y, z);
			}
		}
		else if (chunk.biome == BiomeGenBase.mushroomIsland)
		{
			for (int i = 0; i < 2; i++)
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);

				(new WorldGenBigMushroom()).generate(this.world, rnd, x, y, z);
			}

			for (int i = 0; i < 1; i++)
			{
				if (rnd.nextInt(4) == 0)
				{
					int x = absX + rnd.nextInt(16) + 8;
					int z = absZ + rnd.nextInt(16) + 8;
					int y = this.world.getHeightValue(x, z);

					(new WorldGenFlowers(Blocks.yellow_flower)).generate(this.world, rnd, x, y, z);
				}

				if (rnd.nextInt(8) == 0)
				{
					int x = absX + rnd.nextInt(16) + 8;
					int z = absZ + rnd.nextInt(16) + 8;
					int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);

					(new WorldGenFlowers(Blocks.red_flower)).generate(this.world, rnd, x, y, z);
				}
			}
		}
		else if (chunk.biome == BiomeGenBase.taiga || chunk.biome == BiomeGenBase.icePlains)
		{
			for (int zo = 0; zo < 16; zo++)
			{
				for (int xo = 0; xo < 16; xo++)
				{
					int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

					int x = xo + absX;
					int z = zo + absZ;
					int y = midY + 1;

					int distanceSquared = chunk.getMainDistance(x, midY, z);

					if (distanceSquared <= chunk.scaledSphereRadius && this.world.isBlockFreezable(x, y, z))
					{
						this.world.setBlock(x, y, z, Blocks.snow);
					}
				}
			}
		}

		SpawnerAnimals.performWorldGenSpawning(this.world, chunk.biome, absX + 8, absZ + 8, 16, 16, rnd);
		BlockSand.fallInstantly = false;
	}

	/**
	 * Two modes of operation: if passed true, save all Chunks in one go. If passed false, save up to two chunks. Return
	 * true if all chunks have been saved.
	 */
	@Override
	public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
	{
		return true;
	}

	/**
	 * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
	 */
	@Override
	public boolean unloadQueuedChunks()
	{
		return false;
	}

	/**
	 * Returns if the IChunkProvider supports saving.
	 */
	@Override
	public boolean canSave()
	{
		return true;
	}

	/**
	 * Converts the instance data to a readable string.
	 */
	@Override
	public String makeString()
	{
		return "RandomLevelSource";
	}

	/**
	 * Returns a list of creatures of the specified type that can spawn at the given location.
	 */
	@Override
	public List getPossibleCreatures(EnumCreatureType enumcreaturetype, int i, int j, int k)
	{
		BiomeGenBase biomegenbase = this.world.getBiomeGenForCoords(i, k);
		return biomegenbase == null ? null : biomegenbase.getSpawnableList(enumcreaturetype);
	}

	/**
	 * Returns the location of the closest structure of the specified type. If not found returns null.
	 */
	public ChunkPosition findClosestStructure(World world1, String s, int i, int j, int k)
	{
		return null;
	}

	@Override
	public int getLoadedChunkCount()
	{
		return 0;
	}

	@Override
	public void recreateStructures(int var1, int var2)
	{

	}

	public void func_104112_b()
	{}

	@Override
	public void saveExtraData()
	{
		/* do nothing */
	}

	@Override
	public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_,
			int p_147416_5_)
	{

		return null;
	}
}
