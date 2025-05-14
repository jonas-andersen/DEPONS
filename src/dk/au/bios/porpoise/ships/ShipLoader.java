/*
 * Copyright (C) 2017-2025 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.landscape.CellDataSource;
import dk.au.bios.porpoise.landscape.RollingDateFile;
import dk.au.bios.porpoise.ui.ShipStyleColorMap;
import repast.simphony.context.Context;

public class ShipLoader {

	private static ShipLoader INSTANCE;
	
	private static final String SHIPS_FILE_PREFIX = "ships";
	private static final String SHIPS_FILE_EXTENSION = ".json";
	private final CellDataSource source;
	private final RollingDateFile rollingDateFile;

	public ShipLoader(CellDataSource source) throws IOException {
		this.source = source;
		this.rollingDateFile = new RollingDateFile(SHIPS_FILE_PREFIX, SHIPS_FILE_EXTENSION, source);
	}
	
	public static void initialize(final Context<Agent> context, final CellDataSource source) throws IOException {
		ShipLoader.INSTANCE = new ShipLoader(source);
		ShipLoader.loadNextFileIfNecessary(context);
	}

	public static void loadNextFileIfNecessary(final Context<Agent> context) throws IOException {
		if (INSTANCE == null) {
			return;
		}

		if (INSTANCE.rollingDateFile.shouldLoad()) {
			System.out.println("Loading ships from " + INSTANCE.rollingDateFile.getCurrentFile().fileName());
			var shipsToRemove = new ArrayList<Agent>();
			context.getObjects(dk.au.bios.porpoise.Ship.class).forEach(shipsToRemove::add);
			shipsToRemove.forEach(context::remove);
			try (InputStream dataIS = new ByteArrayInputStream(INSTANCE.source.getRawData(INSTANCE.rollingDateFile.getCurrentFile().fileName()))) {
				loadFromStream(context, dataIS);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
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
		ShipStyleColorMap.initialize(shipsData.getShips());
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

}
