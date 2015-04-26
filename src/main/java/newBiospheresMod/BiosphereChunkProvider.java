/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import scala.Array;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.BlockSand;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.WeightedRandom;
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
import net.minecraft.world.gen.feature.WorldGenDeadBush;
import net.minecraft.world.gen.feature.WorldGenFire;
import net.minecraft.world.gen.feature.WorldGenFlowers;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraft.world.gen.feature.WorldGenPumpkin;
import net.minecraft.world.gen.feature.WorldGenReed;
import net.minecraft.world.gen.feature.WorldGenTallGrass;
import net.minecraft.world.gen.feature.WorldGenWaterlily;
import net.minecraft.world.gen.feature.WorldGenerator;
import newBiospheresMod.Configuration.ModConfig;
import newBiospheresMod.Helpers.AvgCalc;
import newBiospheresMod.Helpers.Blx;
import newBiospheresMod.Helpers.Creator;
import newBiospheresMod.Helpers.IKeyProvider;
import newBiospheresMod.Helpers.LruCacheList;
import newBiospheresMod.Helpers.ModConsts;
import newBiospheresMod.Helpers.Predicate;
import newBiospheresMod.Helpers.TopDownBoundingBox;
import newBiospheresMod.Helpers.Utils;
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
			if (!item.isChunkLoaded) { return 0; }
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

	private SphereChunk GenerateChunk(int chunkX, int chunkZ, BlockData[] blocks)
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

		return chunk;
	}

	private void GenerateChunkInner(int chunkX, int chunkZ, BlockData[] blocks, SphereChunk chunk)
	{
		final int baseX = chunkX << 4;
		final int baseZ = chunkZ << 4;
		final int bridgeWidth = config.getBridgeWidth();
		final BlockData outsideFillerBlock = config.getOutsideFillerBlock();

		Sphere sphere = chunk.masterSphere;
		Random rnd = chunk.GetPhaseRandom("GenerateChunk");

		for (int zo = 0; zo < 16; ++zo)
		{
			for (int xo = 0; xo < 16; ++xo)
			{
				int midY = chunk.getChunkBoundSurfaceLevel(xo, zo);

				for (int rawY = ModConsts.WORLD_MAX_Y; rawY >= ModConsts.WORLD_MIN_Y; rawY--)
				{
					int idx = ModConsts.GetChunkArrayIndex(xo, rawY, zo);
					BlockData block = BlockData.Empty;

					int rawX = baseX + xo;
					int rawZ = baseZ + zo;

					int sphereDistance = sphere.getMainDistance(rawX, rawY, rawZ);
					int orbDistance = sphere.getOrbDistance(rawX, rawY, rawZ);
					int lakeDistance = sphere.getLakeDistance(rawX, rawY, rawZ);

					if (sphereDistance > sphere.scaledSphereRadius)
					{
						BlockData stairwayBlock = sphere.getOrbStairwayBlock(rawX, rawY, rawZ);

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
								block = sphere.getDomeBlock(rawX, rawY, rawZ);
							}
						}
						else if (sphere.hasLake && config.isNoiseEnabled() && sphere.isDesert
							&& (lakeDistance > sphere.scaledLakeRadius && lakeDistance <= sphere.scaledLakeEdgeRadius))
						{
							if (rawY == sphere.lakeLocation.posY)
							{
								block = new BlockData(sphere.biome.topBlock);
							}
							else if (rawY < sphere.lakeLocation.posY)
							{
								block = new BlockData(sphere.biome.fillerBlock);
							}
						}
						else if (sphere.hasLake && config.isNoiseEnabled() && sphere.isDesert
							&& lakeDistance <= sphere.scaledLakeRadius)
						{
							if (rawY == sphere.lakeLocation.posY && sphere.biome == BiomeGenBase.icePlains)
							{
								block = new BlockData(Blx.ice);
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
							block = sphere.getDomeBlock(rawX, rawY, rawZ);
						}
						else if (config.doesNeedProtectionGlass()
							&& rawY == midY + 4
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) < bridgeWidth))
						{
							block = sphere.getDomeBlock(rawX, rawY, rawZ);
						}
						else if (config.doesNeedProtectionGlass()
							&& rawY < midY + 4
							&& sphereDistance > sphere.scaledSphereRadius
							&& (Math.abs(rawX - sphere.sphereLocation.posX) < bridgeWidth || Math.abs(rawZ
								- sphere.sphereLocation.posZ) < bridgeWidth))
						{
							block = BlockData.Empty;
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
						block = new BlockData(Blx.stone);
					}
					else if (sphere.hasLake && sphere.isDesert
						&& lakeDistance <= sphere.scaledLakeRadius)
					{
						if (rawY == sphere.lakeLocation.posY && sphere.biome == BiomeGenBase.icePlains)
						{
							block = new BlockData(Blx.ice);
						}
						else if (rawY <= sphere.lakeLocation.posY)
						{
							block = sphere.GetLakeBlock();
						}
					}
					else if (sphere.hasLake && rawY < sphere.lakeLocation.posY - 1
						&& sphere.isDesert && lakeDistance <= sphere.scaledLakeEdgeRadius)
					{
						if (ModConsts.DEBUG)
						{
							block = new BlockData(Blx.glowstone);
						}
						else
						{
							block = new BlockData(sphere.lavaLake ? Blx.gravel : Blx.sand);
						}
					}
					else if (sphereDistance < sphere.scaledSphereRadius)
					{
						if (rawY == midY)
						{
							block = new BlockData(sphere.biome.topBlock);
						}
						else if (rawY == midY - 1)
						{
							block = new BlockData(sphere.biome.fillerBlock);
						}
						else
						{
							block = new BlockData(Blx.stone);
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
						block = config.getOrbBlock();
					}
					else if (orbDistance < sphere.scaledOrbRadius)
					{
						block = ((BlockEntry)WeightedRandom.getRandomItem(rnd, config.OreOrbBlocks)).Block;
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

		BlockData[] blockData = new BlockData[ModConsts.GetChunkArraySize()];
		SphereChunk sphereChunk = this.GenerateChunk(x, z, blockData);

		Block[] blocks = BlockData.getBlockArray(blockData);
		byte[] metadata = BlockData.getMetadataArray(blockData);

		this.caveGen.func_151539_a(this, this.world, x, z, blocks); // func_151539_a == generate

		Chunk chunk = new Chunk(this.world, blocks, metadata, x, z);
		chunk.generateSkylightMap();

		// Fix for Issue #24:
		if (sphereChunk != null)
		{
			if (sphereChunk.masterSphere != null)
			{
				if (sphereChunk.masterSphere.biome != null)
				{
					// System.out.println("Biome at " + sphereChunk.chunkX + ", " + sphereChunk.chunkZ + ": " +
					// sphereChunk.masterSphere.biome.biomeName);

					byte[] biomes = new byte[256];
					Arrays.fill(biomes, (byte)(sphereChunk.masterSphere.biome.biomeID & 0xFF));
					chunk.setBiomeArray(biomes);
				}
			}
		}

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
				if (av >= .02D)
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

		for (int i = 0; i < 20; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);

			(new WorldGenMinable(Blx.coal_ore, 16)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 20; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);

			(new WorldGenMinable(Blx.iron_ore, 8)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 2; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);

			(new WorldGenMinable(Blx.gold_ore, 8)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < 8; i++)
		{
			int x = absX + rnd.nextInt(16);
			int y = rnd.nextInt(ModConsts.WORLD_HEIGHT);
			int z = absZ + rnd.nextInt(16);

			(new WorldGenMinable(Blx.redstone_ore, 7)).generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.sandPerChunk2; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioTopSolidOrLiquidBlock(x, z);

			sphere.biome.theBiomeDecorator.sandGen.generate(this.world, rnd, x, y, z);
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.clayPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioTopSolidOrLiquidBlock(x, z);

			if (y > 0)
			{
				sphere.biome.theBiomeDecorator.clayGen.generate(this.world, rnd, x, y, z);
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.sandPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioTopSolidOrLiquidBlock(x, z);

			if (y > 0)
			{
				sphere.biome.theBiomeDecorator.gravelAsSandGen.generate(this.world, rnd, x, y, z);
			}
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

			if (y > 0)
			{
				// func_150567_a == getRandomWorldGenForTrees
				WorldGenerator gen = sphere.biome.func_150567_a(rnd);

				gen.setScale(config.getScale(), config.getScale(), config.getScale());
				gen.generate(this.world, rnd, x, y, z);
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.bigMushroomsPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				sphere.biome.theBiomeDecorator.bigMushroomGen.generate(this.world, rnd, x, y, z);
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.flowersPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				y = rnd.nextInt(y + 32);
				String s = sphere.biome.func_150572_a(rnd, x, y, z);

				BlockFlower f = BlockFlower.func_149857_e(s);
				if (f.getMaterial() != Material.air)
				{
					(new WorldGenFlowers(f)).generate(this.world, rnd, x, y, z);
				}
			}
		}

		for (int i = 0; i <= sphere.biome.theBiomeDecorator.mushroomsPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				if (i == 0)
				{ y = rnd.nextInt(y * 2); }

				if (rnd.nextInt(4) == 0)
				{
					sphere.biome.theBiomeDecorator.mushroomBrownGen.generate(world, rnd, x, y, z);
				}

				if (rnd.nextInt(8) == 0)
				{
					if (i != 0)
					{ y = rnd.nextInt(y * 2); }

					sphere.biome.theBiomeDecorator.mushroomRedGen.generate(world, rnd, x, y, z);
				}
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.deadBushPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				y = rnd.nextInt(y * 2);
				(new WorldGenDeadBush(Blx.deadbush)).generate(this.world, rnd, x, y, z);
			}
		}

		if (config.isTallGrassEnabled())
		{
			int grassPerChunk = sphere.biome.theBiomeDecorator.grassPerChunk;
			for (int i = 0; i < grassPerChunk; i++)
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = getBioHeightValue(x, z);

				if (y > 0)
				{
					y = rnd.nextInt(y * 2);
					WorldGenerator worldgenerator = sphere.biome.getRandomWorldGenForGrass(rnd);
					worldgenerator.generate(world, rnd, x, y, z);
				}
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.reedsPerChunk + 10; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				y = rnd.nextInt(y * 2);
				(new WorldGenReed()).generate(this.world, rnd, x, y, z);
			}
		}

		if (rnd.nextInt(32) == 0)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);
			if (y > 0)
			{
				y = rnd.nextInt(y);
				(new WorldGenPumpkin()).generate(this.world, rnd, x, y, z);
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.cactiPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = getBioHeightValue(x, z);

			if (y > 0)
			{
				y = rnd.nextInt(y * 2);
				(new WorldGenCactus()).generate(this.world, rnd, x, y, z);
			}
		}

		if (sphere.biome == BiomeGenBase.hell)
		{
			if (rnd.nextBoolean())
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);
				if (y > 0)
				{
					(new WorldGenFire()).generate(this.world, rnd, x, y, z);
				}
			}

			if (rnd.nextBoolean())
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);
				if (y > 0)
				{
					(new WorldGenFlowers(Blx.brown_mushroom)).generate(world, rnd, x, y, z);
				}
			}

			if (rnd.nextBoolean())
			{
				int x = absX + rnd.nextInt(16) + 8;
				int z = absZ + rnd.nextInt(16) + 8;
				int y = this.world.getHeightValue(x, z);
				if (y > 0)
				{
					(new WorldGenFlowers(Blx.red_mushroom)).generate(world, rnd, x, y, z);
				}
			}
		}
		else if (sphere.biome.getEnableSnow())
		{
			for (int zo = 0; zo < 16; zo++)
			{
				for (int xo = 0; xo < 16; xo++)
				{
					int x = xo + absX;
					int z = zo + absZ;
					int y = this.world.getHeightValue(x, z) + 16;
					while (y > 0 && (world.isAirBlock(x, --y, z) || (world.getBlock(x, y, z) instanceof BlockDome)))
					{ /* do nothing */}

					if (y > 0)
					{
						if (world.isBlockFreezable(x, y, z))
						{
							world.setBlock(x, y, z, Blx.ice);
						}
						else if (world.func_147478_e(x, y, z, false) &&
							Blx.snow_layer.canPlaceBlockAt(world, x, y + 1, z))
						{
							world.setBlock(x, y + 1, z, Blx.snow_layer, 0, 2);
						}
					}
				}
			}
		}

		for (int i = 0; i < sphere.biome.theBiomeDecorator.waterlilyPerChunk; i++)
		{
			int x = absX + rnd.nextInt(16) + 8;
			int z = absZ + rnd.nextInt(16) + 8;
			int y = this.world.getHeightValue(x, z);

			if (y > 0)
			{
				while (y < world.getActualHeight() && !world.isAirBlock(x, ++y, z))
				{ /* do nothing */ }

				while (y > 0 && (world.isAirBlock(x, --y - 1, z) || world.getBlock(x, y, z) instanceof BlockDome))
				{ /* do nothing */ }

				(new WorldGenWaterlily()).generate(this.world, rnd, x, y + 1, z);
			}
		}

		SpawnerAnimals.performWorldGenSpawning(this.world, sphere.biome, absX + 8, absZ + 8, 16, 16, rnd);
		BlockSand.fallInstantly = false;
	}

	private int getBioHeightValue(int x, int z)
	{
		return world.getHeightValue(x, z);
	}

	private int getBioTopSolidOrLiquidBlock(int x, int z)
	{
		for (int y = world.getHeightValue(x, z); y > 0; y--)
		{
			Block b = world.getBlock(x, y, z);
			if (!(b instanceof BlockDome) && b.getMaterial().blocksMovement()
				&& b.getMaterial() != Material.leaves && !b.isFoliage(world, x, y, z))
			{
				if (world.getBlock(x, y + 1, z) == Blx.air)
				{
					return y + 1;
				}
				else
				{
					break;
				}
			}
		}

		return -1;
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
		/* do nothing */
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
