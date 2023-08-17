/*
 * Copyright (C) 2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.ui;

import static dk.au.bios.porpoise.Globals.convertGridXToUtm;
import static dk.au.bios.porpoise.Globals.convertGridYToUtm;
import static dk.au.bios.porpoise.ships.VesselClass.CONTAINERSHIP;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.AbstractSimulationBDDTest;
import dk.au.bios.porpoise.Ship;
import dk.au.bios.porpoise.ships.Buoy;
import dk.au.bios.porpoise.ships.Route;
import repast.simphony.space.continuous.NdPoint;

public class ShipStyleTest extends AbstractSimulationBDDTest {

	@Test
	void visibleOrHidden() throws Exception {
		aNewWorld(100, 100);
		var shipStyle = new ShipStyle();

		var buoys = new ArrayList<Buoy>();
		buoys.add(new Buoy(convertGridXToUtm(20.0), convertGridYToUtm(50.0), 10.0, 0));
		buoys.add(new Buoy(convertGridXToUtm(-0.4), convertGridYToUtm(55.0), 12.0, 2));
		buoys.add(new Buoy(convertGridXToUtm(55.0), convertGridYToUtm(-0.4), 10.0, 1));
		buoys.add(new Buoy(convertGridXToUtm(55.0), convertGridYToUtm(50), 10.0, 1));
		buoys.add(new Buoy(convertGridXToUtm(99.4), convertGridYToUtm(50), 10.0, 1));
		Route route = new Route("route1", buoys);
		var ship = new Ship("ship1", CONTAINERSHIP, 366.00, route, 0, 99999999);
		context.add(ship);
		ship.initialize();
		schedule.schedule(ship);
		
		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(20.0, 50.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(255);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(-0.4, 55.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);
		
		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(-0.4, 55.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(-0.4, 55.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(55.0, -0.4));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(55.0, -0.4));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(55.0, 50.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(255);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(55.0, 50.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(255);

		schedule.execute();
		assertThat(ship.getPosition()).isEqualTo(new NdPoint(99.4, 50.0));
		assertThat(shipStyle.getColor(ship).getAlpha()).isEqualTo(0);
	}

}
