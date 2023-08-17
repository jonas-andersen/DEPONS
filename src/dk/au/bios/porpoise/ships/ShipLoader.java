/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License version 2 and only version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, see 
 * <https://www.gnu.org/licenses>.
 * 
 * Linking DEPONS statically or dynamically with other modules is making a combined work based on DEPONS. 
 * Thus, the terms and conditions of the GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of DEPONS give you permission to combine DEPONS 
 * with free software programs or libraries that are released under the GNU LGPL and with code included in the 
 * standard release of Repast Simphony under the Repast Suite License (or modified versions of such code, with unchanged license). 
 * You may copy and distribute such a system following the terms of the GNU GPL for DEPONS and the licenses of the 
 * other code concerned.
 * 
 * Note that people who make modified versions of DEPONS are not obligated to grant this special exception for 
 * their modified versions; it is their choice whether to do so. 
 * The GNU General Public License gives permission to release a modified version without this exception; 
 * this exception also makes it possible to release a modified version which carries forward this exception.
 */

package dk.au.bios.porpoise.ships;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.landscape.CellDataSource;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.context.Context;

public class ShipLoader {

	private static final String SHIPS_FILE_REGEX = "ships(\\d\\d\\d\\d)_(XX|\\d\\d)_(XX|\\d\\d)(_.*)?\\.json";
	private static final Pattern SHIPS_FILE_PATTERN = Pattern.compile(SHIPS_FILE_REGEX);

	private static List<RollingFile> creationList;
	private static CellDataSource source;

	public void load(final Context<Agent> context, final CellDataSource source) throws IOException {
		ShipLoader.creationList = null;
		ShipLoader.source = source;

		// rolling file
		var shipFiles = source.getNamesMatching(SHIPS_FILE_REGEX);

		if (shipFiles.size() > 0) {
			var shipFilesList = shipFiles.stream().map(sp -> new RollingFile(sp, calcTickStart(sp))).collect(Collectors.toList());
			shipFilesList.sort((f1, f2) -> f1.getTickStart() - f2.getTickStart());
			var distinctCount = shipFilesList.stream().mapToInt(RollingFile::getTickStart).distinct().count();
			if (distinctCount != shipFilesList.size()) {
				throw new IOException("Two or more ships files have same start time");
			}
			
			System.out.println("Found " + shipFilesList.size() + " ships files");
			ShipLoader.creationList = shipFilesList;
			loadNextFileIfNecessary(context);
			return;
		}

		// Single file
		if (source.hasData("ships.json")) {
			try (InputStream dataIS = new ByteArrayInputStream(source.getRawData("ships.json"))) {
				System.out.println("Loading ships from ships.json");
				loadFromStream(context, dataIS);
			}
		} else {
			throw new IOException("File ships.json does not exist for landscape");
		}
	}

	public static void loadNextFileIfNecessary(final Context<Agent> context) {
		if (creationList == null || creationList.size() < 1) {
			return;
		}

		if ((SimulationTime.getTick() < 0 && creationList.get(0).getTickStart() == 0)
				|| (SimulationTime.getTick() >= creationList.get(0).getTickStart())) {
			var rf = creationList.remove(0);

			System.out.println("Loading ships from " + rf.getFileName());

			var shipsToRemove = new ArrayList<Agent>();
			context.getObjects(dk.au.bios.porpoise.Ship.class).forEach(shipsToRemove::add);
			shipsToRemove.forEach(context::remove);
			
			try (InputStream dataIS = new ByteArrayInputStream(ShipLoader.source.getRawData(rf.getFileName()))) {
				loadFromStream(context, dataIS);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private int calcTickStart(String fileName) {
		var matcher = SHIPS_FILE_PATTERN.matcher(fileName);

		if (!matcher.matches()) {
			throw new RuntimeException("Unexpectedly not matching filename pattern");
		}
		
		String yearStr = matcher.group(1); // year
		String monthStr = matcher.group(2); // month
		String dayStr = matcher.group(3); // day

		int year = Integer.parseInt(yearStr);
		int month;
		if ("XX".equals(monthStr)) {
			month = 0;
		} else {
			month = Integer.parseInt(monthStr) - 1;
			if (month < 0 || month > 11) {
				throw new RuntimeException("Invalid month for file " + fileName);
			}
		}
		int day;
		if ("XX".equals(dayStr)) {
			day = 0;
		} else {
			day = Integer.parseInt(dayStr) - 1;
			if (day < 0 || day > 29) {
				throw new RuntimeException("Invalid day for file " + fileName);
			}
		}

		int startTick = (year * 360 * 48) + (month * 30 * 48) + (day * 48);
		return startTick;
	}
	
	private static void loadFromStream(final Context<Agent> context, final InputStream source)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper objMapper = new ObjectMapper();

		ShipsData shipsData = objMapper.readValue(source, ShipsData.class);

		for (Ship s : shipsData.getShips()) {
			dk.au.bios.porpoise.Ship agent = (dk.au.bios.porpoise.Ship) s;

			verifyRoute(agent);

			context.add(agent);
			agent.initialize();
		}
	}

	private static void verifyRoute(dk.au.bios.porpoise.Ship agent) {
		var minX = Globals.getXllCorner();
		var maxX = Globals.getXllCorner() + (Globals.getWorldWidth() * SimulationConstants.REQUIRED_CELL_SIZE);
		var minY = Globals.getYllCorner();
		var maxY = Globals.getYllCorner() + (Globals.getWorldHeight() * SimulationConstants.REQUIRED_CELL_SIZE);

		var buoysOutsideLandscape = agent.getRoute().getRoute().stream()
				.filter(b -> b.getX() < minX || b.getX() >= maxX || b.getY() < minY || b.getY() >= maxY)
				.collect(Collectors.toList());
		if (!buoysOutsideLandscape.isEmpty()) {
			throw new RuntimeException(
					"Ship " + agent.getName() + " has one or more coordinates outside the landscape");
		}
	}

	private static class RollingFile {
		private final String fileName;
		private final int tickStart;

		public RollingFile(String fileName, int tickStart) {
			super();
			this.fileName = fileName;
			this.tickStart = tickStart;
		}

		public String getFileName() {
			return fileName;
		}

		public int getTickStart() {
			return tickStart;
		}

	}
}
