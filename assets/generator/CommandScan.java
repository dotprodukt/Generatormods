package assets.generator;

/*
 *  Source code for the The Great Wall Mod and Walled City Generator Mods for the game Minecraft
 *  Copyright (C) 2011 by Formivore - 2012 by GotoLink
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Scan command for players to get templates from structures built in game in the specified location
 */
public final class CommandScan extends CommandBase{
    public final String[] modes = {"wall", "building"};
    private final List<BlockAndMeta> blocks = new ArrayList<BlockAndMeta>();
    private final List<String> rules = new ArrayList<String>();
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender commandSender){
        return commandSender instanceof EntityPlayer;
    }

    @Override
    public String getCommandName() {
        return "scantml";
    }

    @Override
    public String getCommandUsage(ICommandSender var1) {
        return "/" + getCommandName() + " <templatetype:wall/building> <templatename> <x0,x1> <y0,y1> <z0,z1>";
    }

    @Override
    public void processCommand(ICommandSender var1, String[] command) {
        if(command.length == 5){
            int a = parseInt(var1, command[2].split(",")[0]);
            int b = parseInt(var1, command[2].split(",")[1]);
            int maxX = Math.max(a,b);
            int minX = Math.min(a,b);
            a = parseInt(var1, command[3].split(",")[0]);
            b = parseInt(var1, command[3].split(",")[1]);
            int maxY = Math.max(a,b);
            int minY = Math.min(a,b);
            a = parseInt(var1, command[4].split(",")[0]);
            b = parseInt(var1, command[4].split(",")[1]);
            int maxZ = Math.max(a,b);
            int minZ = Math.min(a,b);
            File template = new File(BuildingExplorationHandler.CONFIG_DIRECTORY, command[1]+".tml");
            try{
            if(template.createNewFile()){
                blocks.clear();
                blocks.add(new BlockAndMeta(Blocks.air, 0));
                HashMap<Integer, List<Integer>> layers = new HashMap<Integer, List<Integer>>();
                rules.clear();
                HashSet<String> biomes = new HashSet<String>();
                int SIZE_0, SIZE_1;
                int SIZE_2 = maxZ-minZ+1;
                if(command[0].equals(modes[1])) {
                    SIZE_0 = maxY - minY + 1;
                    SIZE_1 = maxX - minX + 1;
                    for (int y = minY; y <= maxY; y++) {
                        List<Integer> temp = new ArrayList<Integer>(SIZE_1*SIZE_2);
                        for (int x = minX; x <= maxX; x++) {
                            for (int z = minZ; z <= maxZ; z++) {
                                parseBlock(var1.getEntityWorld(), x, y, z, temp, command[1]);
                                if(y==minY)
                                    biomes.add(var1.getEntityWorld().getBiomeGenForCoords(x, z).biomeName);
                            }
                        }
                        layers.put(y - minY, temp);
                    }
                }else if(command[0].equals(modes[0])){
                    SIZE_0 = maxX - minX + 1;
                    SIZE_1 = maxY - minY + 1;
                    for (int x = minX; x <= maxX; x++) {
                        List<Integer> temp = new ArrayList<Integer>(SIZE_1*SIZE_2);
                        for (int z = minZ; z <= maxZ; z++) {
                            biomes.add(var1.getEntityWorld().getBiomeGenForCoords(x, z).biomeName);
                            for (int y = minY; y <= maxY; y++) {
                                parseBlock(var1.getEntityWorld(), x, y, z, temp, command[1]);
                            }
                        }
                        layers.put(x - minX, temp);
                    }
                }else {
                    throw new WrongUsageException(getCommandUsage(var1));
                }
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(template)));
                writer.println("# Author: "+var1.getCommandSenderName());
                writer.println("# Generated by in-game template scanner");
                writer.println("weight=1");
                writer.println("embed_into_distance=0");
                writer.println("max_leveling=2");
                writer.println("dimensions="+SIZE_0+","+SIZE_1+","+SIZE_2);
                StringBuffer buf = new StringBuffer("biomes=");
                for(String biome:biomes){
                    buf.append(biome).append(",");
                }
                buf.deleteCharAt(buf.length()-1);
                writer.println(buf.toString());
                if(command[0].equals(modes[0])){
                    writer.println("building_templates=NONE");
                    writer.println("min_length=100");
                    writer.println("building_interval=45");
                    writer.println("max_length=300");
                    writer.println("make_buildings=1");
                    writer.println("make_end_towers=1");
                    writer.println("tower_rule=1");
                    writer.println("default_tower_weight=1");
                    writer.println("spawner_rule=0,10,52-0-Pig");
                    writer.println("spawner_count=1");
                    writer.println("circular_probability=0.25");
                    writer.println("square_min_height=10");
                    writer.println("square_max_height=10");
                    writer.println("square_min_width=7");
                    writer.println("square_max_width=7");
                    writer.println("square_roof_styles=Crenel,Steep");
                    writer.println("circular_tower_min_height=8");
                    writer.println("circular_tower_max_height=15");
                    writer.println("circular_tower_min_width=10");
                    writer.println("circular_tower_max_width=10");
                    writer.println("circular_tower_roof_styles=Crenel,Dome");
                }
                for(String rule:rules){
                    writer.println(rule);
                }
                for(int index:layers.keySet()){
                    writer.println("layer");
                    for(int j = 0; j<SIZE_1; j++){
                        buf = new StringBuffer();
                        for(int i = 0; i<SIZE_2; i++){
                            int id = layers.get(index).get(i+j*SIZE_2);
                            buf.append(id);
                            if(i<SIZE_2-1){
                                buf.append(",");
                            }
                        }
                        writer.println(buf.toString());
                    }
                    writer.println("endlayer");
                }
                writer.close();
                func_152373_a(var1, this, "Template scanner used by " + var1.getCommandSenderName(), var1.getCommandSenderName(), command);
            }
            }catch (IOException io){}
        } else {
            throw new WrongUsageException(getCommandUsage(var1));
        }
    }

    private void parseBlock(World world, int x, int y, int z, List<Integer> temp, String template) {
        BlockAndMeta blc = new BlockAndMeta(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
        if (!blocks.contains(blc)) {
            rules.add("rule" + blocks.size() + "=0,100," + GameData.getBlockRegistry().getNameForObject(blc.get()) + "-" + blc.getMeta());
            blocks.add(blc);
        }
        temp.add(blocks.indexOf(blc));
        if(blc.get() instanceof BlockChest){
            IInventory tile = (IInventory) world.getTileEntity(x, y, z);
            if(tile!=null) {
                try {
                    File chest = new File(BuildingExplorationHandler.CONFIG_DIRECTORY, template+" Chests.txt");
                    chest.createNewFile();
                    if (chest.canWrite()) {
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(chest)));
                        writer.println("<- Move below lines into the setting files with a different chest name, after tinkering the values ->");
                        writer.println("CHEST_AUTOGENERATED");
                        writer.println("Tries:1");
                        for(int i = 0; i < tile.getSizeInventory(); i++){
                            ItemStack stack = tile.getStackInSlot(i);
                            if(stack!=null){
                                String name = GameData.getItemRegistry().getNameForObject(stack.getItem());
                                if(name==null)
                                    name = GameData.getBlockRegistry().getNameForObject(Block.getBlockFromItem(stack.getItem()));
                                if(name!=null) {
                                    name = name +"-"+stack.getItemDamage()+",1,"+stack.stackSize+","+stack.getMaxStackSize();
                                    if(stack.hasTagCompound())
                                        name = name +","+ stack.getTagCompound();
                                    writer.println(name);
                                }
                            }
                        }
                        writer.close();
                    }
                } catch (Throwable e) {

                }
            }
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender var1, String[] var2) {
        return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, modes) : var2.length == 2 ? getListOfStringsMatchingLastWord(var2, "TemplateDefault") : null;
    }

    @Override
    public int compareTo(Object par1Obj){
        return this.compareTo((ICommand)par1Obj);
    }
}
