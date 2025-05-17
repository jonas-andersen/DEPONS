/*
 * Copyright (C) 2017-2019 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.tasks;

import java.util.LinkedList;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.IAction;
import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.util.SimulationTime;

/**
 * A scheduled action to run some code once a (simulation) day.
 */
public class TickTask implements IAction {

	private final Context<Agent> context;

	public TickTask(final Context<Agent> context) {
		this.context = context;
	}

	/**
	 * Things to do daily or less often.
	 */
	@Override
	public void execute() {
//		dumpLandscapeEnergyLevel();
	}

	private void dumpLandscapeEnergyLevel() {
		long sum = 0;
//		var foodLevels = Globals.getCellData().getFoodValue();
		var foodLevels = Globals.getCellData().getMaxEnt();
		for (int i = 0; i < foodLevels.length; i++) {
			for (int j = 0; j < foodLevels[i].length; j++) {
				sum += foodLevels[i][j];
			}
		}
		System.err.println("FOODLEVEL.TICK: " + sum);
	}

	private void checkPorps() {
		double sum = 0.0d;
		int count = 0;
		for (final Agent a : this.context.getObjects(Porpoise.class)) {
			Porpoise p = (Porpoise) a;
			sum += p.getFoodEaten();
			count++;
		}
		
		double mean = sum / count;
		if (mean < 0.13d) {
			System.out.println("[" + SimulationTime.getTick() + "] low mean food eaten: " + mean);
		}
	}
}
