/*
 * Copyright (C) 2025 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

import java.awt.Color;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Ship;
import dk.au.bios.porpoise.ships.Buoy;
import dk.au.bios.porpoise.ships.VesselClass;

public class ShipStyleColorMap {

	private static final Color COLOR_PAUSED = new Color(128, 128, 128, 102); //Color.GRAY;
	private static final Color COLOR_HIDDEN = new Color(30,250,30, 0);
	private static final Color[] COLOR_VISIBLE = {
			// Color.GREEN, transparency 0 - 40% in 13 steps (5% change)
			new Color(0, 255, 0, 102),
			new Color(0, 255, 0, 115),
			new Color(0, 255, 0, 128),
			new Color(0, 255, 0, 140),
			new Color(0, 255, 0, 153),
			new Color(0, 255, 0, 166),
			new Color(0, 255, 0, 179),
			new Color(0, 255, 0, 191),
			new Color(0, 255, 0, 204),
			new Color(0, 255, 0, 217),
			new Color(0, 255, 0, 230),
			new Color(0, 255, 0, 242),
			new Color(0, 255, 0, 255)
	};
	//private static final Color COLOR_VISIBLE_FULL = COLOR_VISIBLE[COLOR_VISIBLE.length - 1];

	private Map<VesselClass, ShipSpeedRange> shipSpeedRanges = new EnumMap<>(VesselClass.class);
	private double minSpeed = Double.MAX_VALUE;
	private double maxSpeed = 0 - Double.MAX_VALUE;

	private static ShipStyleColorMap INSTANCE = new ShipStyleColorMap();
	
	public static void initialize(List<dk.au.bios.porpoise.ships.Ship> ships) {
		INSTANCE.shipSpeedRanges.clear();
		INSTANCE.minSpeed = Double.MAX_VALUE;
		INSTANCE.maxSpeed = 0 - Double.MAX_VALUE;

		for (dk.au.bios.porpoise.ships.Ship s : ships) {
			var speedRangeForType = INSTANCE.shipSpeedRanges.computeIfAbsent(s.getType(), type -> new ShipSpeedRange());
			s.getRoute().getRoute().stream().mapToDouble(Buoy::getSpeed).forEach(speedRangeForType::adjust);
		}

		for (ShipSpeedRange ssr : INSTANCE.shipSpeedRanges.values()) {
			if (ssr.maxSpeed > INSTANCE.maxSpeed) {
				INSTANCE.maxSpeed = ssr.maxSpeed;
			}
			if (ssr.minSpeed > 0 && ssr.minSpeed < INSTANCE.minSpeed) {
				INSTANCE.minSpeed = ssr.minSpeed;
			}
		}
	}

	public static Color getColorForShip(Ship ship) {
		if (shouldBeHidden(ship)) {
			return COLOR_HIDDEN;
		} else {
			if (ship.isPaused()) {
				return COLOR_PAUSED;
			} else {
				var rangeForVessel = INSTANCE.shipSpeedRanges.get(ship.getType());
				if (rangeForVessel == null) {
					return Color.RED;
				}
				var shipSpeed = ship.getSpeed();
				var shipSpeedOffset = shipSpeed - INSTANCE.minSpeed; //  rangeForVessel.minSpeed;
				var speedRange = INSTANCE.maxSpeed - INSTANCE.minSpeed; // rangeForVessel.maxSpeed - rangeForVessel.minSpeed;
				
				
				int shipSpeedIdx = (int) Math.round(shipSpeedOffset / speedRange);
				
				if (shipSpeedIdx < 0 || shipSpeedIdx >= COLOR_VISIBLE.length) {
					return Color.RED;
				}
				
				return COLOR_VISIBLE[shipSpeedIdx];
			}
		}
	}

	private static boolean shouldBeHidden(Ship ship) {
		var pos = ship.getPosition();
		final double lowerTolerance = -0.5 + (1.0 / (400.0 / 50.0));
		if (pos.getX() < lowerTolerance || pos.getY() < lowerTolerance) {
			return true;
		}
		final double upperTolerance = 0.5 + (1.0 / (400.0 / 50.0));
		if ((Globals.getWorldWidth() - pos.getX()) < upperTolerance
				|| (Globals.getWorldHeight() - pos.getY()) < upperTolerance) {
			return true;
		}

		return false;
	}

	private static class ShipSpeedRange {
		public double minSpeed;
		public double maxSpeed;

		ShipSpeedRange() {
			this.minSpeed = Double.MAX_VALUE;
			this.maxSpeed = - Double.MAX_VALUE;
		}
		
		void adjust(double speed) {
			if (speed <= 0.0d) {
				return; // ignore
			}
			if (speed < minSpeed) {
				minSpeed = speed;
			}
			if (speed > maxSpeed) {
				maxSpeed = speed;
			}
		}
	}
}
