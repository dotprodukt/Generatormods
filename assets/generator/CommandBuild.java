package assets.generator;

import java.util.List;
import java.util.Random;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

public class CommandBuild extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getCommandName() {
		return "build";
	}

	@Override
	public String getCommandUsage(ICommandSender commandSender) {
		return "/" + getCommandName() + " <ruin:wall:city:undcity> <dimensionID,default:0> <x> <z>";
	}

	@Override
	public List getCommandAliases() {
		return null;
	}

	@Override
	public void processCommand(ICommandSender var1, String[] coordinate) {
		if (coordinate.length == 4 || coordinate.length == 3) {
			notifyAdmins(var1, 0, "/build command used by " + var1.getCommandSenderName(), new Object[] { var1.getCommandSenderName(), coordinate });
			int posX = parseInt(var1, coordinate[coordinate.length - 2]);
			int posZ = parseInt(var1, coordinate[coordinate.length - 1]);
			World world = MinecraftServer.getServer().worldServers[coordinate.length == 3 ? 0 : Integer.parseInt(coordinate[1])];
			if ("ruin".equalsIgnoreCase(coordinate[0])) {
				(new WorldGenCARuins(PopulatorCARuins.instance, world, new Random(), posX, posZ, 1, 1)).run();
			} else if ("wall".equalsIgnoreCase(coordinate[0])) {
				(new WorldGenGreatWall(PopulatorGreatWall.instance, world, new Random(), posX, posZ, 1, 1)).run();
			}
			/*
			 * else if ("monowall".equalsIgnoreCase(coordinate[0])) {
			 * wall=PopulatorGreatWall.instance; if(wall.placedCoords==null ||
			 * wall.placedWorld!=world){ wall.placedCoords=new
			 * int[]{posX,world.getHeightValue(posX, posZ),posZ};
			 * wall.placedWorld=world;
			 * var1.sendChatToPlayer("First set of wall coordinates stored."); }
			 * else{ wall.master.exploreThreads.add(new WorldGenSingleWall(
			 * wall, world, world.rand, new int[]{posX,
			 * world.getHeightValue(posX, posZ), posZ}));
			 * wall.placedCoords=null; wall.placedWorld=null; } }
			 */
			else if ("city".equalsIgnoreCase(coordinate[0])) {
				(new WorldGenWalledCity(PopulatorWalledCity.instance, world, new Random(), posX, posZ, 1, 1)).run();
			} else if ("undcity".equalsIgnoreCase(coordinate[0])) {
				PopulatorWalledCity city = PopulatorWalledCity.instance;
				WorldGeneratorThread wgt = new WorldGenUndergroundCity(city, world, new Random(), posX, posZ, 1, 1);
				int maxSpawnHeight = Building.findSurfaceJ(world, posX, posZ, Building.WORLD_MAX_Y, false, Building.IGNORE_WATER) - WorldGenUndergroundCity.MAX_DIAM / 2 - 5; //44 at sea level
				int minSpawnHeight = PopulatorWalledCity.MAX_FOG_HEIGHT + WorldGenUndergroundCity.MAX_DIAM / 2 - 8; //34, a pretty thin margin. Too thin for underocean cities?
				if (minSpawnHeight <= maxSpawnHeight)
					wgt.setSpawnHeight(minSpawnHeight, maxSpawnHeight, false);
				(wgt).run();
			}
		} else {
			throw new WrongUsageException(getCommandUsage(var1));
		}
	}

	@Override
	public List addTabCompletionOptions(ICommandSender var1, String[] var2) {
		return var2.length == 1 ? getListOfStringsMatchingLastWord(var2, new String[] { "ruin", "wall", "city", "undcity" }) : null;
	}
}