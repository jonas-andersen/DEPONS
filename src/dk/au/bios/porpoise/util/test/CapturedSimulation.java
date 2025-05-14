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

package dk.au.bios.porpoise.util.test;

import java.util.HashMap;

import dk.au.bios.porpoise.Porpoise;

public class CapturedSimulation {

	public static class SimParams {
		public HashMap<String, Object> parameters;
	}

	public static class SimTick {
		public double tick;
		public SimState sim;
		public SimPorpoise porp;
	}

	public static class SimState {
		public long populationSize;
	}

	public static class SimPorpoise {
		public long id;
		public double x;
		public double y;
		public double heading;
		public double prevAngle;
		public double prevLogMov;
		public int dispType;
		public double age;
		public double ageOfMaturity;
		public int pregnancyStatus;
		public int matingDay;
		public double energyLevel;
		public double energyLevelSum;
		public double deterTurbineStrength;
		public double deterShipStrength;
		public double[] VT;
		public double[] deterVt;
		public double[] deterShipVt;
//		public String VT;
//		public String deterVt;
		public String posList;
		
		public SimPorpoise() {
		}

		public SimPorpoise(Porpoise p, boolean includePosList) {
			this.id = p.getId();
			this.x = p.getPosition().getX();
			this.y = p.getPosition().getY();
			this.heading = p.getHeading();
			this.prevAngle = p.getPrevAngle();
			if (includePosList) {
				this.prevLogMov = p.getPrevLogMov();
			}
			this.dispType = p.getDispersalBehaviour().getDispersalType();
			this.age = p.getAge();
			this.ageOfMaturity = p.getAgeOfMaturity();
			this.pregnancyStatus = p.getPregnancyStatus();
			this.matingDay = p.getMatingDay();
			this.energyLevel = p.getEnergyLevel();
			this.energyLevelSum = p.getEnergyLevelSum();
			this.deterTurbineStrength = p.getDeterTurbineStrength();
			this.deterShipStrength = p.getDeterShipStrength();
			this.VT = p.getVT();
			this.deterVt = p.getTurbineDeterVector();
			this.deterShipVt = p.getShipDeterVector();
			if (includePosList) {
				this.posList = p.getPosList();
			}
		}
		
	}

}
