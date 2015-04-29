/*
 * This is free software. It comes without any warranty, to the extent permitted by applicable law.
 * You can redistribute it and/or modify it under the terms of the Do What The Fuck You Want To
 * Public License, Version 2, as published by Sam Hocevar. See http://www.wtfpl.net/ for more
 * details.
 */

package newbiospheresmod.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import cpw.mods.fml.common.registry.GameData;

public class Blx {
  public static final Block acacia_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.acacia_stairs;
    }
  }, "minecraft:acacia_stairs", 163);

  public static final Block activator_rail = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.activator_rail;
    }
  }, "minecraft:activator_rail", 157);

  public static final Block air = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.air;
    }
  }, "minecraft:air", 0);

  public static final Block anvil = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.anvil;
    }
  }, "minecraft:anvil", 145);

  public static final Block beacon = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.beacon;
    }
  }, "minecraft:beacon", 138);

  public static final Block bed = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.bed;
    }
  }, "minecraft:bed", 26);

  public static final Block bedrock = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.bedrock;
    }
  }, "minecraft:bedrock", 7);

  public static final Block birch_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.birch_stairs;
    }
  }, "minecraft:birch_stairs", 135);

  public static final Block bookshelf = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.bookshelf;
    }
  }, "minecraft:bookshelf", 47);

  public static final Block brewing_stand = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.brewing_stand;
    }
  }, "minecraft:brewing_stand", 117);

  public static final Block brick_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.brick_block;
    }
  }, "minecraft:brick_block", 45);

  public static final Block brick_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.brick_stairs;
    }
  }, "minecraft:brick_stairs", 108);

  public static final Block brown_mushroom = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.brown_mushroom;
    }
  }, "minecraft:brown_mushroom", 39);

  public static final Block brown_mushroom_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.brown_mushroom_block;
    }
  }, "minecraft:brown_mushroom_block", 99);

  public static final Block cactus = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cactus;
    }
  }, "minecraft:cactus", 81);

  public static final Block cake = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cake;
    }
  }, "minecraft:cake", 92);

  public static final Block carpet = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.carpet;
    }
  }, "minecraft:carpet", 171);

  public static final Block carrots = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.carrots;
    }
  }, "minecraft:carrots", 141);

  public static final Block cauldron = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cauldron;
    }
  }, "minecraft:cauldron", 118);

  public static final Block chest = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.chest;
    }
  }, "minecraft:chest", 54);

  public static final Block clay = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.clay;
    }
  }, "minecraft:clay", 82);

  public static final Block coal_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.coal_block;
    }
  }, "minecraft:coal_block", 173);

  public static final Block coal_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.coal_ore;
    }
  }, "minecraft:coal_ore", 16);

  public static final Block cobblestone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cobblestone;
    }
  }, "minecraft:cobblestone", 4);

  public static final Block cobblestone_wall = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cobblestone_wall;
    }
  }, "minecraft:cobblestone_wall", 139);

  public static final Block cocoa = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.cocoa;
    }
  }, "minecraft:cocoa", 127);

  public static final Block command_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.command_block;
    }
  }, "minecraft:command_block", 137);

  public static final Block crafting_table = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.crafting_table;
    }
  }, "minecraft:crafting_table", 58);

  public static final Block dark_oak_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.dark_oak_stairs;
    }
  }, "minecraft:dark_oak_stairs", 164);

  public static final Block daylight_detector = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.daylight_detector;
    }
  }, "minecraft:daylight_detector", 151);

  public static final Block deadbush = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.deadbush;
    }
  }, "minecraft:deadbush", 32);

  public static final Block detector_rail = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.detector_rail;
    }
  }, "minecraft:detector_rail", 28);

  public static final Block diamond_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.diamond_block;
    }
  }, "minecraft:diamond_block", 57);

  public static final Block diamond_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.diamond_ore;
    }
  }, "minecraft:diamond_ore", 56);

  public static final Block dirt = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.dirt;
    }
  }, "minecraft:dirt", 3);

  public static final Block dispenser = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.dispenser;
    }
  }, "minecraft:dispenser", 23);

  public static final Block double_plant = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.double_plant;
    }
  }, "minecraft:double_plant", 175);

  public static final Block double_stone_slab = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.double_stone_slab;
    }
  }, "minecraft:double_stone_slab", 43);

  public static final Block double_wooden_slab = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.double_wooden_slab;
    }
  }, "minecraft:double_wooden_slab", 125);

  public static final Block dragon_egg = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.dragon_egg;
    }
  }, "minecraft:dragon_egg", 122);

  public static final Block dropper = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.dropper;
    }
  }, "minecraft:dropper", 158);

  public static final Block emerald_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.emerald_block;
    }
  }, "minecraft:emerald_block", 133);

  public static final Block emerald_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.emerald_ore;
    }
  }, "minecraft:emerald_ore", 129);

  public static final Block enchanting_table = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.enchanting_table;
    }
  }, "minecraft:enchanting_table", 116);

  public static final Block end_portal = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.end_portal;
    }
  }, "minecraft:end_portal", 119);

  public static final Block end_portal_frame = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.end_portal_frame;
    }
  }, "minecraft:end_portal_frame", 120);

  public static final Block end_stone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.end_stone;
    }
  }, "minecraft:end_stone", 121);

  public static final Block ender_chest = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.ender_chest;
    }
  }, "minecraft:ender_chest", 130);

  public static final Block farmland = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.farmland;
    }
  }, "minecraft:farmland", 60);

  public static final Block fence = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.fence;
    }
  }, "minecraft:fence", 85);

  public static final Block fence_gate = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.fence_gate;
    }
  }, "minecraft:fence_gate", 107);

  public static final Block fire = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.fire;
    }
  }, "minecraft:fire", 51);

  public static final Block flower_pot = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.flower_pot;
    }
  }, "minecraft:flower_pot", 140);

  public static final Block flowing_lava = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.flowing_lava;
    }
  }, "minecraft:flowing_lava", 10);

  public static final Block flowing_water = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.flowing_water;
    }
  }, "minecraft:flowing_water", 8);

  public static final Block furnace = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.furnace;
    }
  }, "minecraft:furnace", 61);

  public static final Block glass = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.glass;
    }
  }, "minecraft:glass", 20);

  public static final Block glass_pane = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.glass_pane;
    }
  }, "minecraft:glass_pane", 102);

  public static final Block glowstone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.glowstone;
    }
  }, "minecraft:glowstone", 89);

  public static final Block gold_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.gold_block;
    }
  }, "minecraft:gold_block", 41);

  public static final Block gold_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.gold_ore;
    }
  }, "minecraft:gold_ore", 14);

  public static final Block golden_rail = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.golden_rail;
    }
  }, "minecraft:golden_rail", 27);

  public static final Block grass = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.grass;
    }
  }, "minecraft:grass", 2);

  public static final Block gravel = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.gravel;
    }
  }, "minecraft:gravel", 13);

  public static final Block hardened_clay = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.hardened_clay;
    }
  }, "minecraft:hardened_clay", 172);

  public static final Block hay_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.hay_block;
    }
  }, "minecraft:hay_block", 170);

  public static final Block heavy_weighted_pressure_plate = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.heavy_weighted_pressure_plate;
    }
  }, "minecraft:heavy_weighted_pressure_plate", 148);

  public static final Block hopper = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.hopper;
    }
  }, "minecraft:hopper", 154);

  public static final Block ice = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.ice;
    }
  }, "minecraft:ice", 79);

  public static final Block iron_bars = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.iron_bars;
    }
  }, "minecraft:iron_bars", 101);

  public static final Block iron_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.iron_block;
    }
  }, "minecraft:iron_block", 42);

  public static final Block iron_door = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.iron_door;
    }
  }, "minecraft:iron_door", 71);

  public static final Block iron_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.iron_ore;
    }
  }, "minecraft:iron_ore", 15);

  public static final Block jukebox = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.jukebox;
    }
  }, "minecraft:jukebox", 84);

  public static final Block jungle_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.jungle_stairs;
    }
  }, "minecraft:jungle_stairs", 136);

  public static final Block ladder = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.ladder;
    }
  }, "minecraft:ladder", 65);

  public static final Block lapis_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lapis_block;
    }
  }, "minecraft:lapis_block", 22);

  public static final Block lapis_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lapis_ore;
    }
  }, "minecraft:lapis_ore", 21);

  public static final Block lava = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lava;
    }
  }, "minecraft:lava", 11);

  public static final Block leaves = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.leaves;
    }
  }, "minecraft:leaves", 18);

  public static final Block leaves2 = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.leaves2;
    }
  }, "minecraft:leaves2", 161);

  public static final Block lever = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lever;
    }
  }, "minecraft:lever", 69);

  public static final Block light_weighted_pressure_plate = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.light_weighted_pressure_plate;
    }
  }, "minecraft:light_weighted_pressure_plate", 147);

  public static final Block lit_furnace = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lit_furnace;
    }
  }, "minecraft:lit_furnace", 62);

  public static final Block lit_pumpkin = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lit_pumpkin;
    }
  }, "minecraft:lit_pumpkin", 91);

  public static final Block lit_redstone_lamp = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lit_redstone_lamp;
    }
  }, "minecraft:lit_redstone_lamp", 124);

  public static final Block lit_redstone_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.lit_redstone_ore;
    }
  }, "minecraft:lit_redstone_ore", 74);

  public static final Block log = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.log;
    }
  }, "minecraft:log", 17);

  public static final Block log2 = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.log2;
    }
  }, "minecraft:log2", 162);

  public static final Block melon_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.melon_block;
    }
  }, "minecraft:melon_block", 103);

  public static final Block melon_stem = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.melon_stem;
    }
  }, "minecraft:melon_stem", 105);

  public static final Block mob_spawner = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.mob_spawner;
    }
  }, "minecraft:mob_spawner", 52);

  public static final Block monster_egg = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.monster_egg;
    }
  }, "minecraft:monster_egg", 97);

  public static final Block mossy_cobblestone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.mossy_cobblestone;
    }
  }, "minecraft:mossy_cobblestone", 48);

  public static final Block mycelium = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.mycelium;
    }
  }, "minecraft:mycelium", 110);

  public static final Block nether_brick = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.nether_brick;
    }
  }, "minecraft:nether_brick", 112);

  public static final Block nether_brick_fence = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.nether_brick_fence;
    }
  }, "minecraft:nether_brick_fence", 113);

  public static final Block nether_brick_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.nether_brick_stairs;
    }
  }, "minecraft:nether_brick_stairs", 114);

  public static final Block nether_wart = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.nether_wart;
    }
  }, "minecraft:nether_wart", 115);

  public static final Block netherrack = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.netherrack;
    }
  }, "minecraft:netherrack", 87);

  public static final Block noteblock = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.noteblock;
    }
  }, "minecraft:noteblock", 25);

  public static final Block oak_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.oak_stairs;
    }
  }, "minecraft:oak_stairs", 53);

  public static final Block obsidian = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.obsidian;
    }
  }, "minecraft:obsidian", 49);

  public static final Block packed_ice = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.packed_ice;
    }
  }, "minecraft:packed_ice", 174);

  public static final Block piston = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.piston;
    }
  }, "minecraft:piston", 33);

  public static final Block piston_extension = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.piston_extension;
    }
  }, "minecraft:piston_extension", 36);

  public static final Block piston_head = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.piston_head;
    }
  }, "minecraft:piston_head", 34);

  public static final Block planks = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.planks;
    }
  }, "minecraft:planks", 5);

  public static final Block portal = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.portal;
    }
  }, "minecraft:portal", 90);

  public static final Block potatoes = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.potatoes;
    }
  }, "minecraft:potatoes", 142);

  public static final Block powered_comparator = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.powered_comparator;
    }
  }, "minecraft:powered_comparator", 150);

  public static final Block powered_repeater = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.powered_repeater;
    }
  }, "minecraft:powered_repeater", 94);

  public static final Block pumpkin = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.pumpkin;
    }
  }, "minecraft:pumpkin", 86);

  public static final Block pumpkin_stem = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.pumpkin_stem;
    }
  }, "minecraft:pumpkin_stem", 104);

  public static final Block quartz_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.quartz_block;
    }
  }, "minecraft:quartz_block", 155);

  public static final Block quartz_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.quartz_ore;
    }
  }, "minecraft:quartz_ore", 153);

  public static final Block quartz_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.quartz_stairs;
    }
  }, "minecraft:quartz_stairs", 156);

  public static final Block rail = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.rail;
    }
  }, "minecraft:rail", 66);

  public static final Block red_flower = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.red_flower;
    }
  }, "minecraft:red_flower", 38);

  public static final Block red_mushroom = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.red_mushroom;
    }
  }, "minecraft:red_mushroom", 40);

  public static final Block red_mushroom_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.red_mushroom_block;
    }
  }, "minecraft:red_mushroom_block", 100);

  public static final Block redstone_block = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.redstone_block;
    }
  }, "minecraft:redstone_block", 152);

  public static final Block redstone_lamp = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.redstone_lamp;
    }
  }, "minecraft:redstone_lamp", 123);

  public static final Block redstone_ore = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.redstone_ore;
    }
  }, "minecraft:redstone_ore", 73);

  public static final Block redstone_torch = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.redstone_torch;
    }
  }, "minecraft:redstone_torch", 76);

  public static final Block redstone_wire = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.redstone_wire;
    }
  }, "minecraft:redstone_wire", 55);

  public static final Block reeds = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.reeds;
    }
  }, "minecraft:reeds", 83);

  public static final Block sand = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sand;
    }
  }, "minecraft:sand", 12);

  public static final Block sandstone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sandstone;
    }
  }, "minecraft:sandstone", 24);

  public static final Block sandstone_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sandstone_stairs;
    }
  }, "minecraft:sandstone_stairs", 128);

  public static final Block sapling = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sapling;
    }
  }, "minecraft:sapling", 6);

  public static final Block skull = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.skull;
    }
  }, "minecraft:skull", 144);

  public static final Block snow = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.snow;
    }
  }, "minecraft:snow", 80);

  public static final Block snow_layer = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.snow_layer;
    }
  }, "minecraft:snow_layer", 78);

  public static final Block soul_sand = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.soul_sand;
    }
  }, "minecraft:soul_sand", 88);

  public static final Block sponge = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sponge;
    }
  }, "minecraft:sponge", 19);

  public static final Block spruce_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.spruce_stairs;
    }
  }, "minecraft:spruce_stairs", 134);

  public static final Block stained_glass = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stained_glass;
    }
  }, "minecraft:stained_glass", 95);

  public static final Block stained_glass_pane = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stained_glass_pane;
    }
  }, "minecraft:stained_glass_pane", 160);

  public static final Block stained_hardened_clay = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stained_hardened_clay;
    }
  }, "minecraft:stained_hardened_clay", 159);

  public static final Block standing_sign = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.standing_sign;
    }
  }, "minecraft:standing_sign", 63);

  public static final Block sticky_piston = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.sticky_piston;
    }
  }, "minecraft:sticky_piston", 29);

  public static final Block stone = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone;
    }
  }, "minecraft:stone", 1);

  public static final Block stone_brick_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone_brick_stairs;
    }
  }, "minecraft:stone_brick_stairs", 109);

  public static final Block stone_button = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone_button;
    }
  }, "minecraft:stone_button", 77);

  public static final Block stone_pressure_plate = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone_pressure_plate;
    }
  }, "minecraft:stone_pressure_plate", 70);

  public static final Block stone_slab = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone_slab;
    }
  }, "minecraft:stone_slab", 44);

  public static final Block stone_stairs = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stone_stairs;
    }
  }, "minecraft:stone_stairs", 67);

  public static final Block stonebrick = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.stonebrick;
    }
  }, "minecraft:stonebrick", 98);

  public static final Block tallgrass = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.tallgrass;
    }
  }, "minecraft:tallgrass", 31);

  public static final Block tnt = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.tnt;
    }
  }, "minecraft:tnt", 46);

  public static final Block torch = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.torch;
    }
  }, "minecraft:torch", 50);

  public static final Block trapdoor = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.trapdoor;
    }
  }, "minecraft:trapdoor", 96);

  public static final Block trapped_chest = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.trapped_chest;
    }
  }, "minecraft:trapped_chest", 146);

  public static final Block tripwire = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.tripwire;
    }
  }, "minecraft:tripwire", 132);

  public static final Block tripwire_hook = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.tripwire_hook;
    }
  }, "minecraft:tripwire_hook", 131);

  public static final Block unlit_redstone_torch = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.unlit_redstone_torch;
    }
  }, "minecraft:unlit_redstone_torch", 75);

  public static final Block unpowered_comparator = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.unpowered_comparator;
    }
  }, "minecraft:unpowered_comparator", 149);

  public static final Block unpowered_repeater = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.unpowered_repeater;
    }
  }, "minecraft:unpowered_repeater", 93);

  public static final Block vine = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.vine;
    }
  }, "minecraft:vine", 106);

  public static final Block wall_sign = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wall_sign;
    }
  }, "minecraft:wall_sign", 68);

  public static final Block water = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.water;
    }
  }, "minecraft:water", 9);

  public static final Block waterlily = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.waterlily;
    }
  }, "minecraft:waterlily", 111);

  public static final Block web = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.web;
    }
  }, "minecraft:web", 30);

  public static final Block wheat = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wheat;
    }
  }, "minecraft:wheat", 59);

  public static final Block wooden_button = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wooden_button;
    }
  }, "minecraft:wooden_button", 143);

  public static final Block wooden_door = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wooden_door;
    }
  }, "minecraft:wooden_door", 64);

  public static final Block wooden_pressure_plate = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wooden_pressure_plate;
    }
  }, "minecraft:wooden_pressure_plate", 72);

  public static final Block wooden_slab = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wooden_slab;
    }
  }, "minecraft:wooden_slab", 126);

  public static final Block wool = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.wool;
    }
  }, "minecraft:wool", 35);

  public static final Block yellow_flower = Blx.loadBlock(new Creator<Block>() {
    @Override
    public Block create() {
      return Blocks.yellow_flower;
    }
  }, "minecraft:yellow_flower", 37);

  public static ArrayList<Block> getAllBlocks() {
    return Blx.getAllBlocks(null);
  }

  public static ArrayList<Block> getAllBlocks(final Predicate<Block> whereClause) {
    // Copy all of the blocks into a new static collection for the caller
    // to safely iterate. This allows the caller to modify the block
    // registry in a safe manner while iterating over the returned collection
    // avoiding crashing due to corruption or stack overflows, etc.

    final ArrayList<Block> allBlocks = Utils.toList(Utils.where(GameData.getBlockRegistry().typeSafeIterable(),
        Utils.and(Utils.notNull(Block.class), whereClause)));

    // This is not really needed, as the block registry sorts by ID anyways
    // But that's an implementation detail, and we want to be safe as
    // otherwise we could (in principle) load something as the wrong block
    // due to load order changes.
    Collections.sort(allBlocks, new Comparator<Block>() {
      @Override
      public int compare(final Block arg0, final Block arg1) {
        return Integer.compare(Block.getIdFromBlock(arg0), Block.getIdFromBlock(arg1));
      }
    });

    return allBlocks;
  }

  private static Block loadBlock(final Creator<Block> getBlock, final String fallbackName, final int fallbackId) {
    Block returnValue = null;

    if (returnValue == null) {
      try {
        returnValue = getBlock.create();
      } catch (final Throwable ignoreMe) {
        // do nothing
      }
    }

    if (returnValue == null) {
      try {
        returnValue = Block.getBlockFromName(fallbackName);
      } catch (final Throwable ignoreMe) {
        // do nothing
      }
    }

    if (returnValue == null) {
      try {

        returnValue = Block.getBlockById(fallbackId);
      } catch (final Throwable ignoreMe) {
        // do nothing
      }
    }

    if (returnValue == null) {
      try {
        returnValue = Blocks.air;
      } catch (final Throwable ignoreMe) {
        // do nothing
      }
    }

    return returnValue;
  }

  private Blx() {
    // Do nothing
  }
}
