package assets.generator;

/*
 *  Source code for the CellullarAutomata Ruins for the game Minecraft
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

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

@Mod(modid = "CARuins", name = "Cellular Automata Generator", version = BuildingExplorationHandler.VERSION, dependencies = BuildingExplorationHandler.LOADED_BEFORE, acceptableRemoteVersions = "*")
public final class PopulatorCARuins extends BuildingExplorationHandler {
	@Instance("CARuins")
	public static PopulatorCARuins instance;
	private final static String AUTOMATA_RULES_STRING = "AUTOMATA RULES";
	private final static TemplateRule DEFAULT_TEMPLATE = new TemplateRule(new Block[] {Blocks.cobblestone, Blocks.mossy_cobblestone, Blocks.mossy_cobblestone}, new int[] { 0, 0, 0 }, 100);
	private static TemplateRule[] DEFAULT_BLOCK_RULES = new TemplateRule[BIOME_NAMES.length];
	static {
		DEFAULT_BLOCK_RULES[0] = DEFAULT_TEMPLATE; //Underground, unused
		DEFAULT_BLOCK_RULES[1] = DEFAULT_TEMPLATE; //Ocean
		DEFAULT_BLOCK_RULES[2] = new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick}, new int[] { 0, 1, 2 }, 100); //Plains
		DEFAULT_BLOCK_RULES[3] = new TemplateRule(Blocks.sandstone, 0, 100); //Desert
		DEFAULT_BLOCK_RULES[4] = new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick}, new int[] { 0, 0, 2 }, 100); //Hills
		DEFAULT_BLOCK_RULES[5] = DEFAULT_TEMPLATE; //Forest            
		DEFAULT_BLOCK_RULES[6] = DEFAULT_TEMPLATE; //Taiga             
		DEFAULT_BLOCK_RULES[7] = DEFAULT_TEMPLATE; //Swampland         
		DEFAULT_BLOCK_RULES[8] = DEFAULT_TEMPLATE; //River             
		DEFAULT_BLOCK_RULES[9] = new TemplateRule(Blocks.nether_brick, 0, 100);//Nether
		DEFAULT_BLOCK_RULES[10] = new TemplateRule(Blocks.end_stone, 0, 100); //Sky
		DEFAULT_BLOCK_RULES[11] = new TemplateRule(new Block[] {Blocks.ice, Blocks.snow, Blocks.stonebrick}, new int[] { 0, 0, 2 }, 100); //FrozenOcean
		DEFAULT_BLOCK_RULES[12] = new TemplateRule(new Block[] {Blocks.ice, Blocks.snow, Blocks.stonebrick}, new int[] { 0, 0, 2 }, 100); //FrozenRiver
		DEFAULT_BLOCK_RULES[13] = new TemplateRule(new Block[] {Blocks.snow, Blocks.stonebrick, Blocks.stonebrick}, new int[] { 0, 2, 2 }, 100); //IcePlains
		DEFAULT_BLOCK_RULES[14] = new TemplateRule(new Block[] {Blocks.snow, Blocks.stonebrick, Blocks.stonebrick}, new int[] { 0, 2, 2 }, 100); //IceMountains
		DEFAULT_BLOCK_RULES[15] = DEFAULT_TEMPLATE; //MushroomIsland    
		DEFAULT_BLOCK_RULES[16] = DEFAULT_TEMPLATE; //Shore
		DEFAULT_BLOCK_RULES[17] = DEFAULT_TEMPLATE; //Beach
		DEFAULT_BLOCK_RULES[18] = new TemplateRule(Blocks.sandstone, 0, 100); //DesertHills
		DEFAULT_BLOCK_RULES[19] = DEFAULT_TEMPLATE; //ForestHills
		DEFAULT_BLOCK_RULES[20] = DEFAULT_TEMPLATE; //TaigaHills
		DEFAULT_BLOCK_RULES[21] = new TemplateRule(new Block[] {Blocks.stone, Blocks.stonebrick, Blocks.stonebrick }, new int[] { 0, 0, 2 }, 100);//ExtremeHillsEdge
	}
	//WARNING! Make sure the first DEFAULT_BLOCK_RULES.length biome Strings in Building are the ones we want here.
	private static String[] BLOCK_RULE_NAMES = new String[DEFAULT_BLOCK_RULES.length];
	private final static String[] SPAWNER_RULE_NAMES = new String[] { "MediumLightNarrowFloor", "MediumLightWideFloor", "LowLight" };
	public final static CARule[] DEFAULT_CA_RULES = new CARule[] {
        //3-rule
        new CARule("B3", "S23", 5, "Life - good for weird temples"),
        new CARule("B36", "S013468", 3, "pillars and hands"),
        new CARule("B367", "S02347", 2, "towers with interiors and chasms"),
        new CARule("B34", "S2356", 3, "towers with hetrogenous shapes"),
        new CARule("B368", "S245", 8, "Morley - good hanging bits"),
        new CARule("B36", "S125", 4, "2x2 - pillar & arch temple/tower/statue"),
        new CARule("B36", "S23", 4, "High Life - space invaders, hanging arms."),
        new CARule("B3568", "S148", 4, "fuzzy stilts"), new CARule("B3","S1245", 8, "complex"),
        new CARule("B3567", "S13468", 5, "fat fuzzy"),
        new CARule("B356", "S16", 5, "fuzzy with spurs"),new CARule("B3468", "S123", 3, "towers with arches"),
        new CARule("B35678", "S015678", 2, "checkerboard"),new CARule("B35678", "S0156", 15, "spermatazoa"),
        //2-rule
        new CARule("B26", "S12368", 1, "mayan pyramid"), new CARule("B248", "S45", 1, "gaudi pyramid"),
        new CARule("B2457", "S013458", 1, "complex interior pyramid"),
        //4-rule
        new CARule("B45", "S2345", 6, "45-rule - square towers") };
	public final static String[] SEED_TYPE_STRINGS = new String[] { "Symmetric", "Linear", "Circular", "Cruciform" };
	public int[] seedTypeWeights = new int[] { 8, 2, 2, 1 };
	public float SymmetricSeedDensity = 0.5F;
	public int MinHeight = 20, MaxHeight = 70;
	public int ContainerWidth = 40, ContainerLength = 40;
	public int MinHeightBeforeOscillation = 12;
	public boolean SmoothWithStairs = true, MakeFloors = true;
	public TemplateRule[] blockRules = new TemplateRule[DEFAULT_BLOCK_RULES.length];
	public TemplateRule[] spawnerRules = new TemplateRule[] { BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE, BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_WIDE_SPAWNER_RULE,
			BuildingCellularAutomaton.DEFAULT_LOW_LIGHT_SPAWNER_RULE };
	ArrayList<CARule> caRules = null;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		settingsFileName = "CARuinsSettings.txt";
        trySendMUD(event);
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuild());
        event.registerServerCommand(new CommandScan());
	}

	//****************************  FUNCTION - loadDataFiles *************************************************************************************//
	public final void loadDataFiles() {
		try {
			initializeLogging("Loading options for the Cellular Automata Generator.");
			for (int i = 0; i < BIOME_NAMES.length; i++) {
				if (BIOME_NAMES[i] != null) {
					if (i > 21)
						DEFAULT_BLOCK_RULES[i] = DEFAULT_TEMPLATE;
					BLOCK_RULE_NAMES[i] = BIOME_NAMES[i].replaceAll("\\s", "") + "BlockRule";
				}
			}
			//read and check values from file
			caRules = new ArrayList<CARule>();
			getGlobalOptions();
			finalizeLoading(false, "ruin");
		} catch (Exception e) {
			errFlag = true;
			logOrPrint("There was a problem loading the Cellular Automata Generator: " + e.getMessage(), "SEVERE");
			lw.println("There was a problem loading the Cellular Automata Generator: " + e.getMessage());
			e.printStackTrace();
		} finally {
			if (lw != null)
				lw.close();
		}
		if (GlobalFrequency < 0.000001 || caRules == null || caRules.size() == 0)
			errFlag = true;
		dataFilesLoaded = true;
	}

	//****************************  FUNCTION - getGlobalOptions *************************************************************************************//
	@Override
	public void loadGlobalOptions(BufferedReader br) {
		try {
			for (String read = br.readLine(); read != null; read = br.readLine()) {
				readGlobalOptions(lw, read);
				if (read.startsWith("MinHeight"))
					MinHeight = readIntParam(lw, MinHeight, ":", read);
				if (read.startsWith("MaxHeight"))
					MaxHeight = readIntParam(lw, MaxHeight, ":", read);
				if (read.startsWith("MinHeightBeforeOscillation"))
					MinHeightBeforeOscillation = readIntParam(lw, MinHeightBeforeOscillation, ":", read);
				if (read.startsWith("SmoothWithStairs"))
					SmoothWithStairs = readBooleanParam(lw, SmoothWithStairs, ":", read);
				if (read.startsWith("MakeFloors"))
					MakeFloors = readBooleanParam(lw, MakeFloors, ":", read);
				if (read.startsWith("ContainerWidth"))
					ContainerWidth = readIntParam(lw, ContainerWidth, ":", read);
				if (read.startsWith("ContainerLength"))
					ContainerLength = readIntParam(lw, ContainerLength, ":", read);
				readChestItemsList(lw, read, br);
				if (read.startsWith("SymmetricSeedDensity"))
					SymmetricSeedDensity = readFloatParam(lw, SymmetricSeedDensity, ":", read);
				for (int m = 0; m < SEED_TYPE_STRINGS.length; m++) {
					if (read.startsWith(SEED_TYPE_STRINGS[m]+"SeedWeight"))
						seedTypeWeights[m] = readIntParam(lw, seedTypeWeights[m], ":", read);
				}
				for (int m = 0; m < spawnerRules.length; m++) {
					if (read.startsWith(SPAWNER_RULE_NAMES[m]+"SpawnerRule")) {
						try {
							spawnerRules[m] = readRuleIdOrRule(":", read, null);
						} catch (Exception e) {
							spawnerRules[m] = BuildingCellularAutomaton.DEFAULT_MEDIUM_LIGHT_NARROW_SPAWNER_RULE;
							lw.println(e.getMessage());
						}
					}
				}
				for (int m = 0; m < DEFAULT_BLOCK_RULES.length; m++) {
					if (BIOME_NAMES[m] != null && read.startsWith(BLOCK_RULE_NAMES[m])) {
						try {
							blockRules[m] = readRuleIdOrRule(":", read, null);
						} catch (Exception e) {
							blockRules[m] = DEFAULT_BLOCK_RULES[m];
							lw.println(e.getMessage());
						}
					}
				}
				if (read.startsWith(AUTOMATA_RULES_STRING)) {
					for (read = br.readLine(); read != null; read = br.readLine()) {
                        try {
                            CARule caRule = new CARule(read, lw);
                            caRules.add(caRule);
                        }catch (IllegalArgumentException ill){
                           break;
                        }
					}
					break;
				}
			}
			if (TriesPerChunk > MAX_TRIES_PER_CHUNK)
				TriesPerChunk = MAX_TRIES_PER_CHUNK;
		} catch (IOException e) {
			lw.println(e.getMessage());
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ignored) {
			}
		}
	}

	@Override
	public void writeGlobalOptions(PrintWriter pw) {
		printGlobalOptions(pw, true);
		pw.println();
		pw.println("<-MinHeight and MaxHeight are the minimum and maximum allowed height of the structures->");
		pw.println("<-MinHeightBeforeOscillation - Any structures that form oscillators before MaxOscillatorCullStep will be culled.->");
		pw.println("<-Smooth with stairs - If set to true, will smooth out ruins by placing extra stair blocks.->");
		pw.println("<-ContainerWidth and ContainerLength are the dimensions of the bounding rectangle.->");
		pw.println("MinHeight:" + MinHeight);
		pw.println("MaxHeight:" + MaxHeight);
		pw.println("MinHeightBeforeOscillation:" + MinHeightBeforeOscillation);
		pw.println("SmoothWithStairs:" + SmoothWithStairs);
		pw.println("MakeFloors:" + MakeFloors);
		pw.println("ContainerWidth:" + ContainerWidth);
		pw.println("ContainerLength:" + ContainerLength);
		pw.println();
		printDefaultChestItems(pw);
		pw.println();
		pw.println("<-Seed type weights are the relative likelihood weights that different seeds will be used. Weights are nonnegative integers.->");
		pw.println("<-SymmetricSeedDensity is the density (out of 1.0) of live blocks in the symmetric seed.->");
		pw.println("SymmetricSeedDensity:" + SymmetricSeedDensity);
		for (int m = 0; m < SEED_TYPE_STRINGS.length; m++) {
			pw.println(SEED_TYPE_STRINGS[m] + "SeedWeight:" + seedTypeWeights[m]);
		}
		pw.println();
		pw.println("<-These spawner rule variables control what spawners will be used depending on the light level and floor width.->");
		for (int m = 0; m < spawnerRules.length; m++) {
			pw.println(SPAWNER_RULE_NAMES[m] + "SpawnerRule:" + spawnerRules[m]);
		}
		pw.println();
		pw.println("<-BlockRule is the template rule that controls what blocks the structure will be made out of.->");
		pw.println("<-Default is BiomeNameBlockRule:" + DEFAULT_TEMPLATE.toString() + "->");
		pw.println("<-Which translates into: (special condition) then,(100%=complete)ruin in either normal(1 out of 3 chance) or mossy cobblestone(2 out of 3) in said biome->");
		pw.println("<-Metadatas are supported, use blockname-blockmetadata syntax->");
		for (int m = 0; m < DEFAULT_BLOCK_RULES.length; m++) {
			if (BLOCK_RULE_NAMES[m] != null)
				pw.println(BLOCK_RULE_NAMES[m] + ":" + DEFAULT_BLOCK_RULES[m]);
		}
		pw.println();
		pw.println("<-An automata rule should be in the form B<neighbor digits>/S<neighbor digits>, where B stands for \"birth\" and S stands->");
		pw.println("<-   for \"survive\". <neighbor digits> are the subset the digits from 0 to 8 on which the rule will birth or survive.->");
		pw.println("<-   For example, the Game of Life has the rule code B3/S23.->");
		pw.println("<-Rule weights are the relative likelihood weights that different rules will be used. Weights are nonnegative integers.->");
		pw.println(AUTOMATA_RULES_STRING);
        for (CARule defaultRule : DEFAULT_CA_RULES) {
            pw.println(defaultRule);
            caRules.add(defaultRule);
        }
        pw.close();
	}

	//****************************  FUNCTION - generate *************************************************************************************//
	@Override
	public final void generate(World world, Random random, int i, int k) {
		if (random.nextFloat() < GlobalFrequency)
			(new WorldGenCARuins(this, world, random, i, k, TriesPerChunk, GlobalFrequency)).run();
	}

	@Override
	public String toString() {
		return "CARuins";
	}

	@EventHandler
	public void modsLoaded(FMLPostInitializationEvent event) {
		if (!dataFilesLoaded)
			loadDataFiles();
		if (!errFlag) {
			GameRegistry.registerWorldGenerator(this, 2);
		}
	}

    public CARule pick(Random random){
        return caRules.get(RandomPicker.pickWeightedOption(random, caRules));
    }
}
