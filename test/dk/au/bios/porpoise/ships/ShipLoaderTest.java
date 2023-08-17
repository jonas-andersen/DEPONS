/*
 * Copyright (C) 2022-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.AbstractSimulationBDDTest;
import dk.au.bios.porpoise.Ship;
import dk.au.bios.porpoise.landscape.LandscapeLoader;

public class ShipLoaderTest extends AbstractSimulationBDDTest {

	@Test
	public void loadJsonFromKattegatZip() throws Exception {
		aNewWorld(600, 1000);

		var landscapeLoader = new LandscapeLoader("Kattegat");
		landscapeLoader.loadShips(context);

		assertThat(context.getObjectsAsStream(Ship.class).count()).isEqualTo(5148);

		Ship ship = shipStream().filter(s -> "518100373-1".equals(s.getName())).findAny().orElseThrow();
		assertThat(ship.getName()).isEqualTo("518100373-1");
		assertThat(ship.getType()).isEqualTo(VesselClass.BULKER);
		assertThat(ship.getRoute().getName()).isEqualTo("Route_518100373-1");
		assertThat(ship.getRoute().getRoute()).hasSize(34);
	}

	@Test
	void fileMatcher2() {
		var shipsFilePattern = Pattern.compile("ships(\\d\\d\\d\\d)_(XX|\\d\\d)_(XX|\\d\\d)\\.json");
		assertThat(shipsFilePattern.matcher("ships0000_XX_XX.json").matches()).isTrue();
		assertThat(shipsFilePattern.matcher("ships0000_00_01.json").matches()).isTrue();
	}

	@Test
	void fileMatcher() {
		var shipsFilePattern = Pattern.compile("ships(\\d\\d\\d\\d)_(XX|\\d\\d)_(XX|\\d\\d)(_.*)?\\.json");
		assertThat(shipsFilePattern.matcher("ships0000_00_00.json").matches()).isTrue();
		assertThat(shipsFilePattern.matcher("ships0000_00_01_another.json").matches()).isTrue();
	}

}
