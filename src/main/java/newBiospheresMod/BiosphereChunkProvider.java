/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

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
import newBiospheresMod.Helpers.AvgCalc;
import newBiospheresMod.Helpers.Creator;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Predicate;
import newBiospheresMod.Helpers.TopDownBoundingBox;
import newBiospheresMod.Helpers.Utils;
import newBiospheresMod.Models.ModConfig;
import newBiospheresMod.Models.Sphere;
import newBiospheresMod.Models.SphereChunk;

public class BiosphereChunkProvider implements IChunkProvider
{
	// #region Caching

	private static LruCacheList<BiosphereChunkProvider> chunkProviders = new LruCacheList<BiosphereChunkProvider>(3,
		new IKeyProvider<BiosphereChunkProvider>()
		{
			@Override
			public Object provideKey(BiosphereChunkProvider item)
			{
				if (item == null) { return null; }
				return item.world;
			}
		});

	public static BiosphereChunkProvider get(final World world)
	{
		return chunkProviders.FindOrAdd(world, new Creator<BiosphereChunkProvider>()
		{
			@Override
			public BiosphereChunkProvider create()
			{
				return new BiosphereChunkProvider(world);
			}
		});
	}

	// #endregion

	// #region Chunk Caching

	private static class ChunkCacheKey
	{
		public final int x;
		public final int z;
		public final World world;

		public ChunkCacheKey(final World world, final int x, final int z)
		{
			this.world = world;
			this.x = x;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) { return false; }
			if (this == obj) { return true; }
			if (!(obj instanceof ChunkCacheKey)) { return false; }
			ChunkCacheKey other = (ChunkCacheKey)obj;

			return this.x == other.x && this.z == other.z && this.world == other.world;
		}

		@Override
		public int hashCode()
		{
			int worldHash = (world == null) ? 0 : world.hashCode();
			return ((x & 0xFFFF) | ((z & 0xFFFF) << 16)) ^ worldHash ^ 319023957;
		}
	}

	LruCacheList<Chunk> ChunksCache = new LruCacheList(15, new IKeyProvider<Chunk>()
	{
		@Override
		public Object provideKey(Chunk item)
		{
			if (item == null) { return 0; }
			return new ChunkCacheKey(item.worldObj, item.xPosition, item.zPosition);
		}
	});

	// #endregion

	// #region Fields

	public final World world;
	public final ModConfig config;
	public final NoiseGeneratorOctaves noiseGenerator;
	public final long worldSeed;
	private final MapGenBase caveGen = new BiosphereMapGen();

	// #endregion

	// /**
	// * Get whether the map features (e.g. strongholds) generation is enabled or disabled.
	// */
	// public boolean getMapFeaturesEnabled()
	// {
	// return world.getWorldInfo().isMapFeaturesEnabled();
	// }

	private BiosphereChunkProvider(World world)
	{
		this.world = world;
		this.worldSeed = world.getSeed();
		this.config = ModConfig.get(world);
		this.config.update();

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
		Arrays.fill(blocks, config.getOutsideFillerBlock());

		SphereChunk chunk = SphereChunk.get(this, chunkX, chunkZ);

		if (chunk != null && chunk.masterSphere != null)
		{
			Sphere sphere = chunk.masterSphere;

			final TopDownBoundingBox chunkBox = TopDownBoundingBox.FromChunk(chunkX, chunkZ);

			if (Utils.Any(Utils.Where(sphere.getBoundingBoxes(), new Predicate<TopDownBoundingBox>()
			{
				@Override
				public boolean test(TopDownBoundingBox box)
				{
					return chunkBox.CollidesWith(box);
				}
			})))
			{
				// we collided with something like a sphere, ore orb, bridge, or other feature.
				GenerateChunkInner(chunkX, chunkZ, blocks, chunk);
			}
		}
	}

	// TODO: Build a helper for all the blocks, if this is something we have to worry about, it will save us time in the
	// long run.
	private static Block sand = null;

	private void GenerateChunkInner(int chunkX, int chunkZ, Block[] blocks, SphereChunk chunk)
	{
		if (sand == null)
		{
			try
			{
				// 1.7.2 either returns null, or throws an exception on this block. Super lame.
				sand = Blocks.sand;
			}
			catch (Throwable ignore)
			{
				System.out.println("Accessing Blocks.sand threw an exception.");
				ignore.printStackTrace();
			}

			if (sand == null)
			{
				System.out.println("Sand block is null, loading in hard coded block by ID #12.");
				sand = Block.getBlockById(12);
			}
		}

		final int baseX = chunkX << 4;
		final int baseZ = chunkZ << 4;
		final int bridgeWidth = config.getBridgeWidth();
		final Block outsideFillerBlock = config.getOutsideFillerBlock();

		Sphere sphere = chunk.masterSphere;
		Random rnd = chunk.GetPhaseRandom("GenerateChunk");

		for (int zo = 0; zo < 16; ++zo)
		{
			for (int xo = 0; xo < 16; ++xo)
			{
				int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

				for (int rawY = ModConsts.WORLD_MAX_Y; rawY >= ModConsts.WORLD_MIN_Y; rawY--)
				{
					int idx = (xo << ModConsts.xShift) | (zo << ModConsts.zShift) | rawY;
					Block block = Blocks.air;

					int rawX = baseX + xo;
					int rawZ = baseZ + zo;

					int sphereDistance = sphere.getMainDistance(rawX, rawY, rawZ);
					int orbDistance = sphere.getOrbDistance(rawX, rawY, rawZ);
					int lakeDistance = sphere.getLakeDistance(rawX, rawY, rawZ);

					if (sphereDistance > sphere.scaledSphereRadius)
					{
						Block stairwayBlock = sphere.getOrbStairwayBlock(rawX, rawY, rawZ);

						if (stairwayBlock != null)
						{
							block = stairwayBlock;
						}
					}

					if (rawY > midY)
					{
						if (sphere.scaledSphereRadius == sphereDistance)
						{
							if (rawY >= midY + 4 || Math.abs(rawX - sphere.sphereLocation.posX) > bridgeWidth
								&& Math.abs(rawZ - sphere.sphereLocation.posZ) > bridgeWidth)
							{
								block = config.getDomeBlock();
							}
						}
						else if (sphere.hasLake && config.isNoiseEnabled() && sphere.biome != BiomeGenBase.desert
							&& (lakeDistance > sphere.scaledLakeRadius && lakeDistance <= sphere.scaledLakeEdgeRadius))
						{
							if (rawY == sphere.lakeLocation.posY)
							{
								block = sphere.biome.topBlock;
							}
							else if (rawY < sphere.lakeLocation.posY)
							{
								block = sphere.biome.fillerBlock;
							}
						}
						else if (sphere.hasLake && config.isNoiseEnabled() && sphere.biome != BiomeGenBase.desert
							&& lakeDistance <= sphere.scaledLakeRadius)
						{
							if (rawY == sphere.lakeLocation.posY && sphere.biome == BiomeGenBase.icePlains)
							{
								block = Blocks.ice;
							}
							else if (rawY <= sphere.lakeLocation.posY)
							{
								block = sphere.GetLakeBlock();
							}
						}
						else if (config.doesNeedProtectionGlass()
							&& rawY <= midY + 4
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) == bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) == bridgeWidth))
						{
							block = config.getDomeBlock();
						}
						else if (config.doesNeedProtectionGlass()
							&& rawY == midY + 4
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) < bridgeWidth))
						{
							block = config.getDomeBlock();
						}
						else if (config.doesNeedProtectionGlass()
							&& rawY < midY + 4
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) < bridgeWidth))
						{
							block = Blocks.air;
						}
						else if (config.doesNeedProtectionGlass() && sphereDistance > sphere.scaledSphereRadius)
						{
							block = outsideFillerBlock;
						}
						else if (rawY == midY + 1
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) == bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) == bridgeWidth))
						{
							block = config.getBridgeRailBlock();
						}
					}
					else if (sphere.scaledSphereRadius == sphereDistance)
					{
						block = Blocks.stone;
					}
					else if (sphere.hasLake && sphere.biome != BiomeGenBase.desert
						&& lakeDistance <= sphere.scaledLakeRadius)
					{
						if (rawY == sphere.lakeLocation.posY && sphere.biome == BiomeGenBase.icePlains)
						{
							block = Blocks.ice;
						}
						else if (rawY <= sphere.lakeLocation.posY)
						{
							block = sphere.GetLakeBlock();
						}
					}
					else if (sphere.hasLake && rawY < sphere.lakeLocation.posY - 1
						&& sphere.biome != BiomeGenBase.desert && lakeDistance <= sphere.scaledLakeEdgeRadius)
					{
						if (ModConsts.DEBUG)
						{
							block = Blocks.glowstone;
						}
						else
						{
							block = (sphere.lavaLake ? Blocks.gravel : sand);
						}
					}
					else if (sphereDistance < sphere.scaledSphereRadius)
					{
						if (rawY == midY)
						{
							block = sphere.biome.topBlock;
						}
						else if (rawY == midY - 1)
						{
							block = sphere.biome.fillerBlock;
						}
						else
						{
							block = Blocks.stone;
						}
					}
					else if (rawY == midY
						&& sphereDistance > sphere.scaledSphereRadius
						&& (Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth + 1 || Math.abs(rawZ
							- sphere.sphereLocation.posZ) < bridgeWidth + 1))
					{
						block = config.getBridgeSupportBlock();
					}
					else if (config.doesNeedProtectionGlass() && sphereDistance > sphere.scaledSphereRadius)
					{
						block = outsideFillerBlock;
					}

					if (sphere.scaledOrbRadius == orbDistance)
					{
						block = config.getDomeBlock();
					}
					else if (orbDistance < sphere.scaledOrbRadius)
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

	/**
	 * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
	 * specified chunk from the map seed and chunk seed
	 */
	@Override
	public Chunk provideChunk(final int x, final int z)
	{
		return ChunksCache.FindOrAdd(new ChunkCacheKey(this.world, x, z), new Creator<Chunk>()
		{
			@Override
			public Chunk create()
			{
				return GenerateNewChunk(x, z);
			}
		});
	}

	private static final AvgCalc avg = new AvgCalc();
	private static long lastPrintedAt = Long.MIN_VALUE;

	private Chunk GenerateNewChunk(int x, int z)
	{
		long startedAt = System.currentTimeMillis();

		Block[] blocks = new Block[16 * 16 * ModConsts.WORLD_HEIGHT];

		this.GenerateChunk(x, z, blocks);
		this.caveGen.func_151539_a(this, this.world, x, z, blocks); // func_151539_a == generate

		Chunk chunk = new Chunk(this.world, blocks, x, z);
		chunk.generateSkylightMap();

		// It's normal to see performance warnings for a few chunks at start-up and maybe every once in a while after
		// that, but the average should drop down to less than a millisecond within a minute or so.

		long now = System.currentTimeMillis();
		long elapsed = now - startedAt;
		avg.addValue(elapsed / 1000d);
		if (elapsed >= 100)
		{
			System.out.printf("WARNING: BIOSPHERE GENERATE NEW CHUNK @ [%d, %d] TOOK %.3f SECONDS!%n", x, z,
				(elapsed / 1000d));
		}

		if (lastPrintedAt == Long.MIN_VALUE || (now - lastPrintedAt) > 2500)
		{
			lastPrintedAt = now;
			if (avg.getCount() >= 5)
			{
				double av = avg.getAverage();
				if (av >= .001D)
				{
					System.out.printf("INFO: BIOSPHERE GENERATE NEW CHUNK ON AVERAGE TAKES %.3f SECONDS.%n", av);
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
		SphereChunk chunk = SphereChunk.get(this, chunkX, chunkZ);
		Sphere sphere = chunk.masterSphere;
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

		int treesPerChunk = sphere.biome.theBiomeDecorator.treesPerChunk;

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
			WorldGenerator gen = sphere.biome.func_150567_a(rnd);

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
			int grassPerChunk = sphere.biome.theBiomeDecorator.grassPerChunk;

			for (int i = 0; i < grassPerChunk; i++)
			{
				byte metadata = 1; // grass height maybe?

				if (sphere.biome == BiomeGenBase.desert && rnd.nextInt(3) != 0)
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

		if (sphere.biome == BiomeGenBase.desert)
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
		else if (sphere.biome == BiomeGenBase.hell)
		{
			if (rnd.nextBoolean())
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);

				(new WorldGenFire()).generate(this.world, rnd, x, y, z);
			}
		}
		else if (sphere.biome == BiomeGenBase.mushroomIsland)
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
		else if (sphere.biome == BiomeGenBase.taiga || sphere.biome == BiomeGenBase.icePlains)
		{
			for (int zo = 0; zo < 16; zo++)
			{
				for (int xo = 0; xo < 16; xo++)
				{
					int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

					int x = xo + absX;
					int z = zo + absZ;
					int y = midY + 1;

					int distanceSquared = sphere.getMainDistance(x, midY, z);

					if (distanceSquared <= sphere.scaledSphereRadius && this.world.isBlockFreezable(x, y, z))
					{
						this.world.setBlock(x, y, z, Blocks.snow);
					}
				}
			}
		}

		SpawnerAnimals.performWorldGenSpawning(this.world, sphere.biome, absX + 8, absZ + 8, 16, 16, rnd);
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
