package dk.au.bios.porpoise.energetics;

import static dk.au.bios.porpoise.Agent.ndPointToGridPoint;

import dk.au.bios.porpoise.CauseOfDeath;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationConstants;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory;
import dk.au.bios.porpoise.util.CircularBuffer;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.ReplayHelper;
import dk.au.bios.porpoise.util.SimulationTime;

public class OriginalEnergetics implements PorpoiseEnergetics {

	private Porpoise porp;
	private double energyLevel; // Porpoises get energy by eating and loose energy by moving.
	private double energyLevelSum; // Sum of energy levels. Reset to 0 every day
	private final CircularBuffer<Double> energyLevelDaily; // List with average energy for last ten days. Latest days
	private double energyConsumedDailyTemp; // The energy spent today by the porpoise - At the end of the day it becomes
	private double energyConsumedDaily; // The energy consumed yesterday by the porpoise
	private double foodEatenDailyTemp; // The energy so far today.
	private double foodEatenDaily; // The energy eaten yesterday by the porpoise

	private int daysSinceMating; // Days since mating. -99 if not pregnant
	private int daysSinceGivingBirth; // Days since giving birth. -99 if not with lactating calf
	private boolean withLactCalf; // true/false, with lactating calf
	private int calvesBorn = 0; // Counter for number of calves born
	private int calvesWeaned = 0; // Counter for number of calves weaned (successfully to completion)

	private PersistentSpatialMemory calfPsm = null; // If the porpoise is with calf, then this is the PSM it will use.

	public OriginalEnergetics(Porpoise porp) {
		this(porp, false);
	}

	public OriginalEnergetics(Porpoise porp, boolean initialPopulation) {
		this.porp = porp;
		this.energyLevelDaily = new CircularBuffer<Double>(10);
		for (int i = 0; i < 10; i++) {
			this.energyLevelDaily.add(0.0);
		}
		this.energyLevel = Globals.getRandomSource().nextEnergyNormal();
		
		if (initialPopulation) {
			if (porp.getAge() > 0) {
				// This is the model setup, there is a probability that the porpoise is with a lactating calf.
				// Notice: The probability is not dependent on the age of the porpoise if it is above the age of 0 .

				this.pregnancyStatus = 2;
				// become pregnanat with prob. taken from Read & Hohn 1995
				if (this.pregnancyStatus == 2
						&& Globals.getRandomSource().nextPregnancyStatusConceive(0, 1) < SimulationParameters
						.getConceiveProb()) {
					this.pregnancyStatus = 1;
					this.daysSinceMating = Globals.getRandomSource().getInitialDaysSinceMating();
				} else {
					this.pregnancyStatus = 0;
				}
			}
		}
	}

	@Override
	public void doStuff() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 1. Reduce food in the patch that the porp just has left. The amount eaten decreases linearly as the porp's energy
	 * level increases from 10 to 20 (=max e) this does not affect the porpoise's perception of the quality of the area,
	 * and therefore the movement is unaffected.
	 *
	 * 2. Adjust porpoise energy level based on amount of food found and time spent per half hour Increase food level in
	 * cells with food-level > 0 AFTERWARDS in order to calc. stored-util-list correctly.
	 */
	public void updEnergeticStatus() {
		double foodEaten = 0;
		double fractOfFoodToEat = 0;

		if (this.energyLevel < 20) {
			fractOfFoodToEat = (20.0 - energyLevel) / 10.0;
		}
		if (fractOfFoodToEat > 0.99) {
			fractOfFoodToEat = 0.99;
		}

		foodEaten += Globals.getCellData().eatFoodFraction(ndPointToGridPoint(this.porp.getPositionFromPosList(1)), fractOfFoodToEat);

		this.foodEatenDailyTemp += foodEaten;
		ReplayHelper.print("energy before eat food {0} eaten {1}", energyLevel, foodEaten);
		this.porp.getPersistentSpatialMemory().updateMemory(this.porp.getPosition(), foodEaten);
		if (calfPsm != null && this.porp.getDispersalBehaviour().calfHasPSM()) {
			calfPsm.updateMemory(this.porp.getPosition(), foodEaten);
		}
		this.energyLevel += foodEaten;

		// Scale e-use depending on season and lactation
		double scalingFactor = 1;

		// Animals have approximately 30% lower energy consumption when the water is cold, Nov-Mar, and approx.
		// 15% lower energy consumption in Oct+Apr (Lockyer et al 2003. Monitoring growth and energy utilization
		// of the harbour porpoise (Phocoena phocoena) in human care. Harbour porpoises in the North Atlantic
		// 5:143-175.)
		if (SimulationTime.getMonthOfYearWithOffset() == 4 || SimulationTime.getMonthOfYearWithOffset() == 10) {
			scalingFactor = 1.15;
		} else if (SimulationTime.getMonthOfYearWithOffset() > 4 && SimulationTime.getMonthOfYearWithOffset() < 10) {
			scalingFactor = SimulationParameters.getEWarm();
		}

		// Food consumption increases approx 40% when lactating, there is apparently no effect of pregnancy. (Magnus
		// Wahlberg <magnus@fjord-baelt.dk>, unpubl. data)
		if (this.withLactCalf) {
			scalingFactor *= SimulationParameters.getELact();
		}

		// Probability of dying increases with decreasing energy level
		final double yearlySurvProb = 1 - (SimulationConstants.M_MORT_PROB_CONST * Math.exp(-this.energyLevel
				* SimulationParameters.getXSurvivalProbConst()));
		double stepSurvProb = 0;

		if (this.energyLevel > 0) {
			stepSurvProb = Math.exp(Math.log(yearlySurvProb) / (360 * 48));
		}

		final double ran = Globals.getRandomSource().nextEnergeticUpdate(0, 1);
		ReplayHelper.print("porp-upd-energetic-status:{0}", ran);
		if (ran > stepSurvProb) {
			if (!this.withLactCalf || this.energyLevel <= 0) {
				Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
				this.porp.die(CauseOfDeath.Starvation);
			}
			// Better abandoning calf than dying
			if (this.withLactCalf) {
				this.withLactCalf = false;
				this.calfPsm = null;
			}
		}

		final double consumed = (0.001 * scalingFactor * SimulationParameters.getEUsePer30Min() + (Math.pow(10,
				this.porp.getPrevLogMov()) * 0.001 * scalingFactor * SimulationConstants.E_USE_PER_KM / 0.4));
		ReplayHelper.print("energy before consume food {0} consumed  {1} prev-logmov {2} scaling-factor {3}"
				+ " month {4} with-lact-calf {5}", energyLevel, consumed, this.porp.getPrevLogMov(), scalingFactor,
				SimulationTime.getMonthOfYearWithOffset(), withLactCalf);
		consumeEnergy(consumed);

		this.energyLevelSum += this.energyLevel;
	}

	private byte pregnancyStatus; // 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to

	public void updPregnancyStatus() {
		// 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to mate)
		// Become ready to mate:
		if (this.pregnancyStatus == 0 && this.porp.getAge() >= this.porp.getAgeOfMaturity()) {
			this.pregnancyStatus = 2;
		}

		// Mate:
		if (this.pregnancyStatus == 2 && SimulationTime.getDayOfYear() == this.porp.getMatingDay()) {
			// become pregnanat with prob. taken from Read & Hohn 1995
			if (Globals.getRandomSource().nextPregnancyStatusConceive(0, 1) < SimulationParameters.getConceiveProb()) {
				this.pregnancyStatus = 1;
				if (DebugLog.isEnabledFor(9)) {
					DebugLog.print9("{} pregnant", this.porp.getId());
				}
				this.daysSinceMating = 0;
			}
		}

		// Give birth:
		// give birth. Gestation time = approx 10 mo (Lockyer 2003)
		if (this.pregnancyStatus == 1 && this.daysSinceMating == SimulationParameters.getGestationTime()) {
			this.pregnancyStatus = 2; // so it is ready to mate even though it has a very young calf
			this.withLactCalf = true;
			this.calvesBorn++;
			double calfPsmPrefDistance;
			if (this.porp.getDispersalBehaviour().calfInheritsPsmDist()) {
				calfPsmPrefDistance = this.porp.getPersistentSpatialMemory().getPreferredDistance();
			} else {
				calfPsmPrefDistance = PersistentSpatialMemory.generatedPreferredDistance();
			}
			this.calfPsm = new PersistentSpatialMemory(Globals.getWorldWidth(), Globals.getWorldHeight(),
					calfPsmPrefDistance);

			this.daysSinceMating = -99;
			this.daysSinceGivingBirth = 0;

			if (DebugLog.isEnabledFor(9)) {
				DebugLog.print9("{} with lact calf", this.porp.getId());
			}
		}

		// nursing for 8 months
		if (this.withLactCalf && this.daysSinceGivingBirth == SimulationParameters.getNursingTime()) {
			int nOffspr = 0;

			if (Globals.getRandomSource().nextPregnancyStatusBoyGirl(0, 1) > 0.5) { // assuming 50 % males and no
				// abortions
				nOffspr = 1;
			}

			if (DebugLog.isEnabledFor(9)) {
				DebugLog.print("{} hatching {}", this.porp.getId(), nOffspr);
			}

			if (nOffspr > 0) {
				final Porpoise calf = new Porpoise(this.porp);
				this.porp.getContext().add(calf);
				calf.setPosition(this.porp.getPosition());
				calf.moveAwayFromLand(); // Initializes the pos list. TODO: not nice to do here, should be done
				// elsewhere
				Globals.getMonthlyStats().addBirth();
			}
			this.withLactCalf = false;
			this.calvesWeaned++;
			this.calfPsm = null;
			this.daysSinceGivingBirth = -99;
		}

		if (this.pregnancyStatus == 1) {
			this.daysSinceMating++;
		}

		if (this.withLactCalf) {
			this.daysSinceGivingBirth++;
		}
	}

	public void consumeEnergy(final double energyAmount) {
		this.energyLevel -= energyAmount;
		this.energyConsumedDailyTemp += energyAmount;
	}

	@Override
	public void dailyTask() {
		this.foodEatenDaily = this.foodEatenDailyTemp;
		this.foodEatenDailyTemp = 0;

		this.energyConsumedDaily = this.energyConsumedDailyTemp;
		this.energyConsumedDailyTemp = 0;

		final double eMean = this.energyLevelSum / 48.0;
		this.energyLevelDaily.add(Math.round(eMean * 1000.0) / 1000.0);
		this.energyLevelSum = 0; // reset daily  // FIXME This was moved from after the dispersal, check if ok
	}

	public double getFoodEaten() {
		return this.foodEatenDaily;
	}

	public double getEnergyLevelSum() {
		return energyLevelSum;
	}

	public CircularBuffer<Double> getEnergyLevelDaily() {
		return energyLevelDaily;
	}

	public double getEnergyConsumedDaily() {
		return this.energyConsumedDaily;
	}

	public int getCalvesBorn() {
		return this.calvesBorn;
	}

	public int getCalvesWeaned() {
		return this.calvesWeaned;
	}

	public PersistentSpatialMemory getCalfPersistentSpatialMemory() {
		return calfPsm;
	}

	public int getLactatingCalf() {
		if (this.withLactCalf) {
			return 1;
		} else {
			return 0;
		}
	}

	public byte getPregnancyStatus() {
		return pregnancyStatus;
	}

	@Override
	public double getEnergyLevel() {
		return energyLevel;
	}

}
