/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law. You can redistribute
 * it and/or modify it under the terms of the Do What The Fuck You Want To Public License, Version 2, as published by
 * Sam Hocevar. See http://www.wtfpl.net/ for more details.
 */

package newBiospheresMod.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import newBiospheresMod.BlockData;
import newBiospheresMod.BlockDome;

public class Blx
{
	private Blx()
	{
		// Do nothing
	}

	public static final Block air = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.air;
			}
		},
		"minecraft:air", 0
	);

	public static final Block stone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone;
			}
		},
		"minecraft:stone", 1
	);

	public static final Block grass = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.grass;
			}
		},
		"minecraft:grass", 2
	);

	public static final Block dirt = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.dirt;
			}
		},
		"minecraft:dirt", 3
	);

	public static final Block cobblestone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cobblestone;
			}
		},
		"minecraft:cobblestone", 4
	);

	public static final Block planks = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.planks;
			}
		},
		"minecraft:planks", 5
	);

	public static final Block sapling = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sapling;
			}
		},
		"minecraft:sapling", 6
	);

	public static final Block bedrock = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.bedrock;
			}
		},
		"minecraft:bedrock", 7
	);

	public static final Block flowing_water = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.flowing_water;
			}
		},
		"minecraft:flowing_water", 8
	);

	public static final Block water = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.water;
			}
		},
		"minecraft:water", 9
	);

	public static final Block flowing_lava = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.flowing_lava;
			}
		},
		"minecraft:flowing_lava", 10
	);

	public static final Block lava = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lava;
			}
		},
		"minecraft:lava", 11
	);

	public static final Block sand = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sand;
			}
		},
		"minecraft:sand", 12
	);

	public static final Block gravel = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.gravel;
			}
		},
		"minecraft:gravel", 13
	);

	public static final Block gold_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.gold_ore;
			}
		},
		"minecraft:gold_ore", 14
	);

	public static final Block iron_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.iron_ore;
			}
		},
		"minecraft:iron_ore", 15
	);

	public static final Block coal_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.coal_ore;
			}
		},
		"minecraft:coal_ore", 16
	);

	public static final Block log = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.log;
			}
		},
		"minecraft:log", 17
	);

	public static final Block leaves = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.leaves;
			}
		},
		"minecraft:leaves", 18
	);

	public static final Block sponge = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sponge;
			}
		},
		"minecraft:sponge", 19
	);

	public static final Block glass = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.glass;
			}
		},
		"minecraft:glass", 20
	);

	public static final Block lapis_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lapis_ore;
			}
		},
		"minecraft:lapis_ore", 21
	);

	public static final Block lapis_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lapis_block;
			}
		},
		"minecraft:lapis_block", 22
	);

	public static final Block dispenser = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.dispenser;
			}
		},
		"minecraft:dispenser", 23
	);

	public static final Block sandstone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sandstone;
			}
		},
		"minecraft:sandstone", 24
	);

	public static final Block noteblock = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.noteblock;
			}
		},
		"minecraft:noteblock", 25
	);

	public static final Block bed = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.bed;
			}
		},
		"minecraft:bed", 26
	);

	public static final Block golden_rail = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.golden_rail;
			}
		},
		"minecraft:golden_rail", 27
	);

	public static final Block detector_rail = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.detector_rail;
			}
		},
		"minecraft:detector_rail", 28
	);

	public static final Block sticky_piston = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sticky_piston;
			}
		},
		"minecraft:sticky_piston", 29
	);

	public static final Block web = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.web;
			}
		},
		"minecraft:web", 30
	);

	public static final Block tallgrass = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.tallgrass;
			}
		},
		"minecraft:tallgrass", 31
	);

	public static final Block deadbush = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.deadbush;
			}
		},
		"minecraft:deadbush", 32
	);

	public static final Block piston = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.piston;
			}
		},
		"minecraft:piston", 33
	);

	public static final Block piston_head = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.piston_head;
			}
		},
		"minecraft:piston_head", 34
	);

	public static final Block wool = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wool;
			}
		},
		"minecraft:wool", 35
	);

	public static final Block piston_extension = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.piston_extension;
			}
		},
		"minecraft:piston_extension", 36
	);

	public static final Block yellow_flower = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.yellow_flower;
			}
		},
		"minecraft:yellow_flower", 37
	);

	public static final Block red_flower = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.red_flower;
			}
		},
		"minecraft:red_flower", 38
	);

	public static final Block brown_mushroom = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.brown_mushroom;
			}
		},
		"minecraft:brown_mushroom", 39
	);

	public static final Block red_mushroom = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.red_mushroom;
			}
		},
		"minecraft:red_mushroom", 40
	);

	public static final Block gold_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.gold_block;
			}
		},
		"minecraft:gold_block", 41
	);

	public static final Block iron_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.iron_block;
			}
		},
		"minecraft:iron_block", 42
	);

	public static final Block double_stone_slab = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.double_stone_slab;
			}
		},
		"minecraft:double_stone_slab", 43
	);

	public static final Block stone_slab = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone_slab;
			}
		},
		"minecraft:stone_slab", 44
	);

	public static final Block brick_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.brick_block;
			}
		},
		"minecraft:brick_block", 45
	);

	public static final Block tnt = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.tnt;
			}
		},
		"minecraft:tnt", 46
	);

	public static final Block bookshelf = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.bookshelf;
			}
		},
		"minecraft:bookshelf", 47
	);

	public static final Block mossy_cobblestone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.mossy_cobblestone;
			}
		},
		"minecraft:mossy_cobblestone", 48
	);

	public static final Block obsidian = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.obsidian;
			}
		},
		"minecraft:obsidian", 49
	);

	public static final Block torch = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.torch;
			}
		},
		"minecraft:torch", 50
	);

	public static final Block fire = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.fire;
			}
		},
		"minecraft:fire", 51
	);

	public static final Block mob_spawner = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.mob_spawner;
			}
		},
		"minecraft:mob_spawner", 52
	);

	public static final Block oak_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.oak_stairs;
			}
		},
		"minecraft:oak_stairs", 53
	);

	public static final Block chest = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.chest;
			}
		},
		"minecraft:chest", 54
	);

	public static final Block redstone_wire = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.redstone_wire;
			}
		},
		"minecraft:redstone_wire", 55
	);

	public static final Block diamond_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.diamond_ore;
			}
		},
		"minecraft:diamond_ore", 56
	);

	public static final Block diamond_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.diamond_block;
			}
		},
		"minecraft:diamond_block", 57
	);

	public static final Block crafting_table = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.crafting_table;
			}
		},
		"minecraft:crafting_table", 58
	);

	public static final Block wheat = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wheat;
			}
		},
		"minecraft:wheat", 59
	);

	public static final Block farmland = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.farmland;
			}
		},
		"minecraft:farmland", 60
	);

	public static final Block furnace = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.furnace;
			}
		},
		"minecraft:furnace", 61
	);

	public static final Block lit_furnace = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lit_furnace;
			}
		},
		"minecraft:lit_furnace", 62
	);

	public static final Block standing_sign = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.standing_sign;
			}
		},
		"minecraft:standing_sign", 63
	);

	public static final Block wooden_door = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wooden_door;
			}
		},
		"minecraft:wooden_door", 64
	);

	public static final Block ladder = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.ladder;
			}
		},
		"minecraft:ladder", 65
	);

	public static final Block rail = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.rail;
			}
		},
		"minecraft:rail", 66
	);

	public static final Block stone_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone_stairs;
			}
		},
		"minecraft:stone_stairs", 67
	);

	public static final Block wall_sign = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wall_sign;
			}
		},
		"minecraft:wall_sign", 68
	);

	public static final Block lever = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lever;
			}
		},
		"minecraft:lever", 69
	);

	public static final Block stone_pressure_plate = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone_pressure_plate;
			}
		},
		"minecraft:stone_pressure_plate", 70
	);

	public static final Block iron_door = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.iron_door;
			}
		},
		"minecraft:iron_door", 71
	);

	public static final Block wooden_pressure_plate = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wooden_pressure_plate;
			}
		},
		"minecraft:wooden_pressure_plate", 72
	);

	public static final Block redstone_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.redstone_ore;
			}
		},
		"minecraft:redstone_ore", 73
	);

	public static final Block lit_redstone_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lit_redstone_ore;
			}
		},
		"minecraft:lit_redstone_ore", 74
	);

	public static final Block unlit_redstone_torch = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.unlit_redstone_torch;
			}
		},
		"minecraft:unlit_redstone_torch", 75
	);

	public static final Block redstone_torch = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.redstone_torch;
			}
		},
		"minecraft:redstone_torch", 76
	);

	public static final Block stone_button = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone_button;
			}
		},
		"minecraft:stone_button", 77
	);

	public static final Block snow_layer = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.snow_layer;
			}
		},
		"minecraft:snow_layer", 78
	);

	public static final Block ice = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.ice;
			}
		},
		"minecraft:ice", 79
	);

	public static final Block snow = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.snow;
			}
		},
		"minecraft:snow", 80
	);

	public static final Block cactus = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cactus;
			}
		},
		"minecraft:cactus", 81
	);

	public static final Block clay = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.clay;
			}
		},
		"minecraft:clay", 82
	);

	public static final Block reeds = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.reeds;
			}
		},
		"minecraft:reeds", 83
	);

	public static final Block jukebox = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.jukebox;
			}
		},
		"minecraft:jukebox", 84
	);

	public static final Block fence = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.fence;
			}
		},
		"minecraft:fence", 85
	);

	public static final Block pumpkin = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.pumpkin;
			}
		},
		"minecraft:pumpkin", 86
	);

	public static final Block netherrack = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.netherrack;
			}
		},
		"minecraft:netherrack", 87
	);

	public static final Block soul_sand = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.soul_sand;
			}
		},
		"minecraft:soul_sand", 88
	);

	public static final Block glowstone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.glowstone;
			}
		},
		"minecraft:glowstone", 89
	);

	public static final Block portal = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.portal;
			}
		},
		"minecraft:portal", 90
	);

	public static final Block lit_pumpkin = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lit_pumpkin;
			}
		},
		"minecraft:lit_pumpkin", 91
	);

	public static final Block cake = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cake;
			}
		},
		"minecraft:cake", 92
	);

	public static final Block unpowered_repeater = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.unpowered_repeater;
			}
		},
		"minecraft:unpowered_repeater", 93
	);

	public static final Block powered_repeater = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.powered_repeater;
			}
		},
		"minecraft:powered_repeater", 94
	);

	public static final Block stained_glass = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stained_glass;
			}
		},
		"minecraft:stained_glass", 95
	);

	public static final Block trapdoor = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.trapdoor;
			}
		},
		"minecraft:trapdoor", 96
	);

	public static final Block monster_egg = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.monster_egg;
			}
		},
		"minecraft:monster_egg", 97
	);

	public static final Block stonebrick = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stonebrick;
			}
		},
		"minecraft:stonebrick", 98
	);

	public static final Block brown_mushroom_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.brown_mushroom_block;
			}
		},
		"minecraft:brown_mushroom_block", 99
	);

	public static final Block red_mushroom_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.red_mushroom_block;
			}
		},
		"minecraft:red_mushroom_block", 100
	);

	public static final Block iron_bars = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.iron_bars;
			}
		},
		"minecraft:iron_bars", 101
	);

	public static final Block glass_pane = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.glass_pane;
			}
		},
		"minecraft:glass_pane", 102
	);

	public static final Block melon_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.melon_block;
			}
		},
		"minecraft:melon_block", 103
	);

	public static final Block pumpkin_stem = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.pumpkin_stem;
			}
		},
		"minecraft:pumpkin_stem", 104
	);

	public static final Block melon_stem = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.melon_stem;
			}
		},
		"minecraft:melon_stem", 105
	);

	public static final Block vine = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.vine;
			}
		},
		"minecraft:vine", 106
	);

	public static final Block fence_gate = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.fence_gate;
			}
		},
		"minecraft:fence_gate", 107
	);

	public static final Block brick_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.brick_stairs;
			}
		},
		"minecraft:brick_stairs", 108
	);

	public static final Block stone_brick_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stone_brick_stairs;
			}
		},
		"minecraft:stone_brick_stairs", 109
	);

	public static final Block mycelium = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.mycelium;
			}
		},
		"minecraft:mycelium", 110
	);

	public static final Block waterlily = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.waterlily;
			}
		},
		"minecraft:waterlily", 111
	);

	public static final Block nether_brick = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.nether_brick;
			}
		},
		"minecraft:nether_brick", 112
	);

	public static final Block nether_brick_fence = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.nether_brick_fence;
			}
		},
		"minecraft:nether_brick_fence", 113
	);

	public static final Block nether_brick_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.nether_brick_stairs;
			}
		},
		"minecraft:nether_brick_stairs", 114
	);

	public static final Block nether_wart = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.nether_wart;
			}
		},
		"minecraft:nether_wart", 115
	);

	public static final Block enchanting_table = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.enchanting_table;
			}
		},
		"minecraft:enchanting_table", 116
	);

	public static final Block brewing_stand = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.brewing_stand;
			}
		},
		"minecraft:brewing_stand", 117
	);

	public static final Block cauldron = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cauldron;
			}
		},
		"minecraft:cauldron", 118
	);

	public static final Block end_portal = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.end_portal;
			}
		},
		"minecraft:end_portal", 119
	);

	public static final Block end_portal_frame = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.end_portal_frame;
			}
		},
		"minecraft:end_portal_frame", 120
	);

	public static final Block end_stone = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.end_stone;
			}
		},
		"minecraft:end_stone", 121
	);

	public static final Block dragon_egg = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.dragon_egg;
			}
		},
		"minecraft:dragon_egg", 122
	);

	public static final Block redstone_lamp = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.redstone_lamp;
			}
		},
		"minecraft:redstone_lamp", 123
	);

	public static final Block lit_redstone_lamp = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.lit_redstone_lamp;
			}
		},
		"minecraft:lit_redstone_lamp", 124
	);

	public static final Block double_wooden_slab = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.double_wooden_slab;
			}
		},
		"minecraft:double_wooden_slab", 125
	);

	public static final Block wooden_slab = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wooden_slab;
			}
		},
		"minecraft:wooden_slab", 126
	);

	public static final Block cocoa = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cocoa;
			}
		},
		"minecraft:cocoa", 127
	);

	public static final Block sandstone_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.sandstone_stairs;
			}
		},
		"minecraft:sandstone_stairs", 128
	);

	public static final Block emerald_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.emerald_ore;
			}
		},
		"minecraft:emerald_ore", 129
	);

	public static final Block ender_chest = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.ender_chest;
			}
		},
		"minecraft:ender_chest", 130
	);

	public static final Block tripwire_hook = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.tripwire_hook;
			}
		},
		"minecraft:tripwire_hook", 131
	);

	public static final Block tripwire = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.tripwire;
			}
		},
		"minecraft:tripwire", 132
	);

	public static final Block emerald_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.emerald_block;
			}
		},
		"minecraft:emerald_block", 133
	);

	public static final Block spruce_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.spruce_stairs;
			}
		},
		"minecraft:spruce_stairs", 134
	);

	public static final Block birch_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.birch_stairs;
			}
		},
		"minecraft:birch_stairs", 135
	);

	public static final Block jungle_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.jungle_stairs;
			}
		},
		"minecraft:jungle_stairs", 136
	);

	public static final Block command_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.command_block;
			}
		},
		"minecraft:command_block", 137
	);

	public static final Block beacon = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.beacon;
			}
		},
		"minecraft:beacon", 138
	);

	public static final Block cobblestone_wall = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.cobblestone_wall;
			}
		},
		"minecraft:cobblestone_wall", 139
	);

	public static final Block flower_pot = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.flower_pot;
			}
		},
		"minecraft:flower_pot", 140
	);

	public static final Block carrots = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.carrots;
			}
		},
		"minecraft:carrots", 141
	);

	public static final Block potatoes = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.potatoes;
			}
		},
		"minecraft:potatoes", 142
	);

	public static final Block wooden_button = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.wooden_button;
			}
		},
		"minecraft:wooden_button", 143
	);

	public static final Block skull = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.skull;
			}
		},
		"minecraft:skull", 144
	);

	public static final Block anvil = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.anvil;
			}
		},
		"minecraft:anvil", 145
	);

	public static final Block trapped_chest = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.trapped_chest;
			}
		},
		"minecraft:trapped_chest", 146
	);

	public static final Block light_weighted_pressure_plate = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.light_weighted_pressure_plate;
			}
		},
		"minecraft:light_weighted_pressure_plate", 147
	);

	public static final Block heavy_weighted_pressure_plate = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.heavy_weighted_pressure_plate;
			}
		},
		"minecraft:heavy_weighted_pressure_plate", 148
	);

	public static final Block unpowered_comparator = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.unpowered_comparator;
			}
		},
		"minecraft:unpowered_comparator", 149
	);

	public static final Block powered_comparator = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.powered_comparator;
			}
		},
		"minecraft:powered_comparator", 150
	);

	public static final Block daylight_detector = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.daylight_detector;
			}
		},
		"minecraft:daylight_detector", 151
	);

	public static final Block redstone_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.redstone_block;
			}
		},
		"minecraft:redstone_block", 152
	);

	public static final Block quartz_ore = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.quartz_ore;
			}
		},
		"minecraft:quartz_ore", 153
	);

	public static final Block hopper = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.hopper;
			}
		},
		"minecraft:hopper", 154
	);

	public static final Block quartz_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.quartz_block;
			}
		},
		"minecraft:quartz_block", 155
	);

	public static final Block quartz_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.quartz_stairs;
			}
		},
		"minecraft:quartz_stairs", 156
	);

	public static final Block activator_rail = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.activator_rail;
			}
		},
		"minecraft:activator_rail", 157
	);

	public static final Block dropper = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.dropper;
			}
		},
		"minecraft:dropper", 158
	);

	public static final Block stained_hardened_clay = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stained_hardened_clay;
			}
		},
		"minecraft:stained_hardened_clay", 159
	);

	public static final Block stained_glass_pane = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.stained_glass_pane;
			}
		},
		"minecraft:stained_glass_pane", 160
	);

	public static final Block leaves2 = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.leaves2;
			}
		},
		"minecraft:leaves2", 161
	);

	public static final Block log2 = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.log2;
			}
		},
		"minecraft:log2", 162
	);

	public static final Block acacia_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.acacia_stairs;
			}
		},
		"minecraft:acacia_stairs", 163
	);

	public static final Block dark_oak_stairs = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.dark_oak_stairs;
			}
		},
		"minecraft:dark_oak_stairs", 164
	);

	public static final Block hay_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.hay_block;
			}
		},
		"minecraft:hay_block", 170
	);

	public static final Block carpet = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.carpet;
			}
		},
		"minecraft:carpet", 171
	);

	public static final Block hardened_clay = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.hardened_clay;
			}
		},
		"minecraft:hardened_clay", 172
	);

	public static final Block coal_block = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.coal_block;
			}
		},
		"minecraft:coal_block", 173
	);

	public static final Block packed_ice = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.packed_ice;
			}
		},
		"minecraft:packed_ice", 174
	);

	public static final Block double_plant = LoadBlock
	(
		new Creator<Block>()
		{
			@Override
			public Block create()
			{
				return Blocks.double_plant;
			}
		},
		"minecraft:double_plant", 175
	);

	private static Block LoadBlock(Creator<Block> getBlock, String fallbackName, int fallbackId)
	{
		Block returnValue = null;

		if (returnValue == null)
		{
			try
			{
				returnValue = getBlock.create();
			}
			catch (Throwable ignoreMe)
			{
				// do nothing
			}
		}

		if (returnValue == null)
		{
			try
			{
				returnValue = Block.getBlockFromName(fallbackName);
			}
			catch (Throwable ignoreMe)
			{
				// do nothing
			}
		}

		if (returnValue == null)
		{
			try
			{

				returnValue = Block.getBlockById(fallbackId);
			}
			catch (Throwable ignoreMe)
			{
				// do nothing
			}
		}

		if (returnValue == null)
		{
			try
			{
				returnValue = Blocks.air;
			}
			catch (Throwable ignoreMe)
			{
				// do nothing
			}
		}

		return returnValue;
	}

	public static ArrayList<Block> getAllBlocks()
	{
		return getAllBlocks(null);
	}

	public static ArrayList<Block> getAllBlocks(final Predicate<Block> whereClause)
	{
		// Copy all of the blocks into a new static collection for the caller
		// to safely iterate.  This allows the caller to modify the block
		// registry in a safe manner while iterating over the returned collection
		// avoiding crashing due to corruption or stack overflows, etc.

		ArrayList<Block> allBlocks = Utils.ToList
		(
			Utils.Where
			(
				GameData.getBlockRegistry().typeSafeIterable(),
				Utils.And
				(
					Utils.NotNull(Block.class),
					whereClause
				)
			)
		);

		// This is not really needed, as the block registry sorts by ID anyways
		// But that's an implementation detail, and we want to be safe as
		// otherwise we could (in principle) load something as the wrong block
		// due to load order changes.
		Collections.sort
		(
			allBlocks,
			new Comparator<Block>()
			{
				@Override
				public int compare(Block arg0, Block arg1)
				{
					return Integer.compare
					(
						Block.getIdFromBlock(arg0),
						Block.getIdFromBlock(arg1)
					);
				}
			}
		);

		return allBlocks;
	}
}
