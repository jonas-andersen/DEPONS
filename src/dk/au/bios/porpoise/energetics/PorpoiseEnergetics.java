package dk.au.bios.porpoise.energetics;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory;
import dk.au.bios.porpoise.util.CircularBuffer;

public interface PorpoiseEnergetics {

	void updEnergeticStatus(); // Original
	void doStuff(); // New energetics

	void updPregnancyStatus();
	void consumeEnergy(final double energyAmount); // Original, FIXME Where is this in new? Used by dispersal as well
	
	void dailyTask();

	double getFoodEaten();
	double getEnergyConsumedDaily();
	double getEnergyLevel();
	double getEnergyLevelSum();
	CircularBuffer<Double> getEnergyLevelDaily();

	int getCalvesBorn();
	int getCalvesWeaned();
	
	PersistentSpatialMemory getCalfPersistentSpatialMemory();
	int getLactatingCalf();
	byte getPregnancyStatus();
	
	static PorpoiseEnergetics createEnergetics(Porpoise porp) {
		if (Globals.ENERGETICS_USE_NEW) {
			return new CaraEnergetics(porp);
		} else {
			return new OriginalEnergetics(porp);
		}
	}

	static PorpoiseEnergetics createEnergeticsInitialPopulation(Porpoise porp) {
		if (Globals.ENERGETICS_USE_NEW) {
			return new CaraEnergetics(porp, true);
		} else {
			return new OriginalEnergetics(porp, true);
		}
	}

}
