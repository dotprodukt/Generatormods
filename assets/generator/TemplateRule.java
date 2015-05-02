package assets.generator;

/*
 *  Source code for the The Great Wall Mod, CellullarAutomata Ruins and Walled City Generator Mods for the game Minecraft
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
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockMobSpawner;
import net.minecraft.init.Blocks;

import java.util.Random;

/*
 * TemplateRule reads in a rule String and defines a rule that blocks can be sampled from.
 */
public class TemplateRule {
	public final static int FIXED_FOR_BUILDING = 5;
	public final static TemplateRule RULE_NOT_PROVIDED = null;
	public final static String BLOCK_NOT_REGISTERED_ERROR_PREFIX = "Error reading rule: BlockID "; //so we can treat this error differently
	public final static String SPECIAL_AIR = "PRESERVE", SPECIAL_STAIR = "WALL_STAIR", SPECIAL_PAINT = "PAINTING";
    public final static TemplateRule AIR_RULE = new TemplateRule(Blocks.air, 0, null);
	public final static TemplateRule STONE_RULE = new TemplateRule(Blocks.stone, 0, null);
	private Block[] blockIDs;
    private int[] blockMDs;
    private String[] extraData;
	public int chance = 100, condition = 0;
	public BlockAndMeta primaryBlock = null;
	private BlockAndMeta fixedRuleChosen = null;

	public TemplateRule(String rule, boolean checkMetaValue) throws Exception {
		String[] items = rule.split(",");
		int numblocks = items.length - 2;
		if (numblocks < 1)
			throw new Exception("Error reading rule: No blockIDs specified for rule!");
		condition = Integer.parseInt(items[0].trim());
		chance = Integer.parseInt(items[1].trim());
		blockIDs = new Block[numblocks];
		blockMDs = new int[numblocks];
        extraData = new String[numblocks];
		String[] data;
        Block temp;
		for (int i = 0; i < numblocks; i++) {
			data = items[i + 2].trim().split("-", 3);
            if(data[0].equals(SPECIAL_AIR)){//Preserve block rule
                blockIDs[i] = Building.PRESERVE_BLOCK.get();
                blockMDs[i] = Building.PRESERVE_BLOCK.getMeta();
                extraData[i] = data[0];
            }else if(data[0].equals(SPECIAL_STAIR)||data[0].equals(SPECIAL_PAINT)){//Walls stairs or paintings block rule
                blockIDs[i] = Blocks.air;
                int x = 0;
                if (data.length > 1) {
                    try {
                        x = Integer.parseInt(data[1]);
                    } catch (Exception ignored) {
                    }
                }
                if(data[0].startsWith(SPECIAL_STAIR.substring(0, 1))){
                    blockMDs[i] = -x;
                }else{
                    blockMDs[i] = Building.PAINTING_BLOCK_OFFSET + x;
                }
                extraData[i] = data[0];
            }else {
                try {
                    temp = GameData.getBlockRegistry().getObjectById(Integer.parseInt(data[0]));
                } catch (Exception e) {
                    temp = GameData.getBlockRegistry().getObject(data[0]);
                }
                if (temp != null) {
                    blockIDs[i] = temp;
                    if (data.length > 1) {
                        try {
                            blockMDs[i] = Integer.parseInt(data[1]);
                        } catch (Exception e) {
                            blockMDs[i] = 0;
                        }
                        if (data.length > 2 && isSpecial(temp)) {
                            extraData[i] = data[2];
                        }
                    } else {
                        blockMDs[i] = 0;
                    }
                } else {
                    throw new Exception(BLOCK_NOT_REGISTERED_ERROR_PREFIX + data[0] + " unknown!");
                }
            }
			if (checkMetaValue && !(blockIDs[i] instanceof BlockAir)) {
				String checkStr = metaValueCheck(i);
				if (checkStr != null)
					throw new Exception("Error reading rule: " + rule + "\nBad meta value " + blockMDs[i] + ". " + checkStr);
			}
		}
		primaryBlock = getPrimaryBlock();
	}

    public TemplateRule(Block block, int meta, String extra) {
        blockIDs = new Block[] { block };
        blockMDs = new int[] { meta };
        if(extra!=null)
            extraData = new String[]{ extra };
        primaryBlock = getPrimaryBlock();
    }

    public TemplateRule(Block block, int meta, int chance_) {
        this(block, meta, null, chance_);
    }

	public TemplateRule(Block block, int meta, String extra, int chance_) {
        this(block, meta, extra);
		chance = chance_;
	}

    public TemplateRule(Block[] blockIDs_, int[] blockMDs_, int chance_) {
        blockIDs = blockIDs_;
        blockMDs = blockMDs_;
        chance = chance_;
        primaryBlock = getPrimaryBlock();
    }

	public TemplateRule(Block[] blockIDs_, int[] blockMDs_, String[] extra, int chance_) {
		blockIDs = blockIDs_;
		blockMDs = blockMDs_;
        extraData = extra;
		chance = chance_;
		primaryBlock = getPrimaryBlock();
	}

	public void setFixedRule(Random random) {
		if (condition == FIXED_FOR_BUILDING) {
			int m = random.nextInt(blockIDs.length);
            if(extraData!=null && extraData[m]!=null && !extraData[m].isEmpty())
                fixedRuleChosen = new BlockExtended(blockIDs[m], blockMDs[m], extraData[m]);
            else
                fixedRuleChosen = new BlockAndMeta(blockIDs[m], blockMDs[m]);
		} else
			fixedRuleChosen = null;
	}

	public TemplateRule getFixedRule(Random random) {
		if (condition != FIXED_FOR_BUILDING)
			return this;
		int m = random.nextInt(blockIDs.length);
        if(extraData!=null && extraData[m]!=null && !extraData[m].isEmpty())
		    return new TemplateRule(blockIDs[m], blockMDs[m], extraData[m], chance);
        else
            return new TemplateRule(blockIDs[m], blockMDs[m], chance);
	}

	public BlockAndMeta getBlockOrHole(Random random) {
		if (chance >= 100 || random.nextInt(100) < chance) {
			if (fixedRuleChosen != null)
				return fixedRuleChosen;
			int m = random.nextInt(blockIDs.length);
            if(extraData!=null && extraData[m]!=null && !extraData[m].isEmpty())
                return new BlockExtended(blockIDs[m], blockMDs[m], extraData[m]);
            else
                return new BlockAndMeta(blockIDs[m], blockMDs[m]);
		}
		return Building.HOLE_BLOCK_LIGHTING;
	}

	public boolean isPreserveRule() {
		for (int i = 0; i<blockIDs.length; i++){
			if(blockIDs[i] != Building.PRESERVE_BLOCK.get())
				return false;
            if(blockMDs[i] != Building.PRESERVE_BLOCK.getMeta())
                return false;
            if(extraData == null || extraData[i] == null || !extraData[i].equals(SPECIAL_AIR))
                return false;
        }
		return true;
	}

    public boolean hasUndeadSpawner(){
        if(extraData!=null)
            for (int i = 0; i<blockIDs.length; i++){
                //Zombie, Skeleton, Creeper, EASY, UPRIGHT spawners
                if(blockIDs[i] instanceof BlockMobSpawner) {
                    String txt = extraData[i];
                    if (txt != null && (txt.equals("Zombie") || txt.equals("Skeleton") || txt.equals("Creeper") || txt.equals("EASY") || txt.equals("UPRIGHT")))
                        return true;
                }
            }
        return false;
    }

	public BlockAndMeta getNonAirBlock(Random random) {
		int m = random.nextInt(blockIDs.length);
        if(extraData!=null && extraData[m]!=null && !extraData[m].isEmpty())
            return new BlockExtended(blockIDs[m], blockMDs[m], extraData[m]);
        else
            return new BlockAndMeta(blockIDs[m], blockMDs[m]);
	}

	@Override
	public String toString() {
		String str = condition + "," + chance;
        if(blockIDs!=null)
            for (int m = 0; m < blockIDs.length; m++) {
                str += "," + GameData.getBlockRegistry().getNameForObject(blockIDs[m]) + "-" + blockMDs[m];
                if(extraData!=null && extraData[m]!=null && !extraData[m].isEmpty())
                    str += "-" + extraData[m];
            }
		return str;
	}

	//returns the most frequent block in rule
	private BlockAndMeta getPrimaryBlock() {
		int[] hist = new int[blockIDs.length];
		for (int l = 0; l < hist.length; l++)
			for (int m = 0; m < hist.length; m++)
				if (blockIDs[l] == blockIDs[m])
					hist[l]++;
		int maxFreq = 0, pos = 0;
		for (int l = 0; l < hist.length; l++) {
			if (hist[l] > maxFreq) {
				maxFreq = hist[l];
                pos = l;
			}
		}
        if(extraData!=null && extraData[pos]!=null && !extraData[pos].isEmpty())
		    return new BlockExtended(blockIDs[pos], blockMDs[pos], extraData[pos]);
        else
            return new BlockAndMeta(blockIDs[pos], blockMDs[pos]);
	}

    public boolean isSpecial(Block block){
        return block instanceof BlockAir || block instanceof BlockMobSpawner || block instanceof BlockChest;
    }

    private String metaValueCheck(int i) {
        Block blockID = blockIDs[i];
        int metadata = blockMDs[i];
        if (metadata < 0 || metadata >= 16)
            return "All Minecraft meta values should be between 0 and 15";
        String fail = blockID.getUnlocalizedName() + " meta value should be between";
        if (BlockProperties.get(blockID).isStair)
            return metadata < 8 ? null : fail + " 0 and 7";
        // orientation metas
        if(blockID==Blocks.rail){
            return metadata < 10 ? null : fail + " 0 and 9";
        }else if(blockID==Blocks.stone_button || blockID== Blocks.wooden_button){
            return metadata % 8 > 0 && metadata % 8 < 5 ? null : fail + " 1 and 4 or 9 and 12";
        }else if(blockID==Blocks.ladder||blockID==Blocks.dispenser||blockID==Blocks.furnace||blockID==Blocks.lit_furnace||blockID==Blocks.wall_sign
                ||blockID==Blocks.piston||blockID==Blocks.piston_extension||blockID==Blocks.chest||blockID==Blocks.hopper||blockID==Blocks.dropper||blockID==Blocks.golden_rail||blockID==Blocks.detector_rail||blockID==Blocks.activator_rail){
            return metadata % 8 < 6 ? null : fail + " 0 and 5 or 8 and 13";
        }else if(blockID==Blocks.pumpkin||blockID==Blocks.lit_pumpkin){
            return metadata < 5 ? null : fail + " 0 and 4";
        }else if(blockID==Blocks.fence_gate){
            return metadata < 8 ? null : fail + " 0 and 7";
        }else if(blockID==Blocks.wooden_slab ||blockID==Blocks.bed){
            return metadata % 8 < 4 ? null : fail + " 0 and 3 or 8 and 11";
        }else if(blockID==Blocks.torch||blockID==Blocks.redstone_torch||blockID==Blocks.unlit_redstone_torch){
            return metadata > 0 && metadata < 7 ? null : fail + " 1 and 6";
        }
        return null;
    }
}