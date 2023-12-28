package dk.au.bios.porpoise.energetics;

import static dk.au.bios.porpoise.Agent.ndPointToGridPoint;

import java.util.ArrayList;
import java.util.List;

import dk.au.bios.porpoise.CauseOfDeath;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.behavior.PersistentSpatialMemory;
import dk.au.bios.porpoise.util.CircularBuffer;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public class CaraEnergetics implements PorpoiseEnergetics {

	// Porpoise own
	Porpoise porp;
	double lgth;                // Length in cm
	double weight;              // Weight in kg

	// TODO Change to enum?
	byte pregnancyStatus = 0;    // ; 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to mate)
	int dsMating = -99;
	int dsgBirth = -99;           // ; Days since giving birth. -99 if not with lactating calf
	boolean withLactCalf = false; // ; true/false, with lactating calf
	double storageLevel;        // Porpoises get energy by eating and loose energy by moving.
	double storageLevelSum;     // Sum of energy levels. Reset to 0 every day
	CircularBuffer<Double> storageLevelDaily;   // List with average energy for last ten days. Latest days first.

//	double ageDays;             // age in days
	public double swimSpeed;           // swimming speed in m s-1
	double eStorage;            // mobilizable energy available in storage in J
	double eRepoMin;            // minimum storage energy needed for reproduction in J
	double massStruct;          // structural mass (non-blubber) in kg
	double massStructCalf;      // structural mass of the calf in kg
	double vBlub;               // percent body mass represented by blubber
	double vBlubMean;           // mean value of blubber, representing porpoises in good body condition in cm3
	double vBlubMin;            // minimum value of blubber before starving in cm3
	double vBlubRepro;          // minimum blubber volume needed for reproduction in cm3
	double vBlubCalf;           // the calf's volume of blubber in cm3
	double vBlubCalfIdl;        // the ideal volume of blubber for the calf in cm3
	double SLMean;              // mean storage level as percentage of body mass
	String sexCalf;             // sex of calf
	double massF;               // mass of the fetus in kg
	double massCalf;            // mass of the calf in kg
	double mStrK;               // mass growth constant k, unitless
	double mStrInf;             // asymptotic mass in kg
	double lgthCalf;            // length of the calf in m
	double lgthInf;             // asymptotic length in cm
	double lgth0;               // minimum length  in cm
	double lgthK;               // length estimator k, unitless
	double surfaceArea;         // surface area of porpoise in m2

	// Energy intake
	double eAssim;              // assimliated energy in joules
	double eAssimGrow;          // e-assim available for growth (split between growth and blubber)
	double eAssimGrowCalf;      // e-assim available for calf growth
	double eAssimCalf;          // energy assimilated by feeding calves
	double IRRecord;            // maximum ingestion rate in kg
	double IRRecordCalf;        // ingestion rate of the dependant calf in kg

	// Maintenance
	double B0;                  // basal metabolic rate normalization constant, unitless

	// Thermoregulation
	double forConvecScalCoefList;    // forced convection scaling coefficient in W m−2 °C−1
	double hCLowLimCoefList;         // heat transfer coefficient lower limit W m−2 °C−1
	double TCore;                    // core temperature of the animal in °C
	double kB;                       // thermal conductivity of blood free blubber in W m−1 °C−1

	// Locomotion
	double lambda;                   // ratio of active to passive drag

	// Growth and Reproduction
	double maxGrow;                  // maximum growth as determined by gompertz relationship in kg timestep-1
	double maxGrowCalf;              // maximum growth of the calf in kg timestep-1
	double structMassPercPro;        // structural mass percent protein
	double structMassPercLip;        // structural mass percent lipid
	double DELip;                    // deposition efficiency of lipid as a %
	double DEPro;                    // deposition efficiency of protein as a %
	double EDLeanMass;               // energy density of lean mass in J kg-1
	double DELeanMass;               // deposition efficiency of lean mass as a %
	double weanScaleFact;            // weaning scale factor as a %
	double pregChance;

	// Storage
	double percLipBlub;              // percent lipid of blubber
	// Storage: blubber depth sites
	double axillary_D;                // axillary dorsal site blubber depth in cm
	double axillary_L;                // axillary lateral site blubber depth in cm
	double axillary_V;                // axillary ventral site blubber depth in cm
	double CrIDF_D;                   // cranial insertion of the dorsal fin dorsal site blubber depth in cm
	double CrIDF_L;                   // cranial insertion of the lateral fin dorsal site blubber depth in cm
	double CrIDF_V;                   // cranial insertion of the ventral fin dorsal site blubber depth in cm
	double CaIDF_D;                   // caudal insertion of the dorsal fin dorsal site blubber depth in cm
	double CaIDF_L;                   // caudal insertion of the lateral fin dorsal site blubber depth in cm
	double CaIDF_V;                   // caudal insertion of the ventral fin dorsal site blubber depth in cm

	// Storage: cone attributes used for thermo submodel
	double c2;                        // list containing average height, length, and blubber depth for cone 2
	double c3;                        // list containing average height, length, and blubber depth for cone 3

	// Energy budget outputs
	double mBMR;                      // basal metabolic rate in J timestep-1
	double mBMRCalf;                  // energy needed for calf BMR in J timestep-1
	double mThermo;                   // metabolic cost of thermoregulation in J timestep-1
	double mThermoCalf;               // energy needed to for calf thermoregulation in J timestep-1
	double mLoco;                     // metabolic cost of locomotion in J timestep-1
	double mLocoIneffPts;             // locomotion inefficiency from the previous timestep in J timestep-1
	double mGrowth;                   // metabolic cost of growth in J timestep-1
	double mGrowthF;                  // metabolic cost of growth for the fetus in J timestep-1
	double mGrowthCalf;               // energy needed for calf growth in J timestep-1
	double eHeatGest;                 // metabolic cost of the heat of gestation in J timestep-1
	double mPreg;                     // metabolic cost of pregnancy in J timestep-1
	double mBlubCalf;                 // energy needed to maintain calf's blubber stores in J timestep-1
	double blubCalf;                  // amount of blubber to be allocated to calves in cm3
	double eCalf;                     // total calf costs for lactating calves in J timestep-1
	double mLact;                     // metabolic cost of lactation in J timestep-1
	double mLactReal;                 // cost actually spent after allocation procedures in J timestep-1
	double mTot;                      // total metabolism in J timestep-1
	double vitalCosts;                // vital costs of calf to be covered by mother in J
	double vitalCostsCalf;            // vital costs of calf to be covered by calf in J

	// counters
	double abortionCount;             // abortion counter

	// For testing:
	double dailyFood;
	List<Double> foodIntakeList = new ArrayList<>();
	
	public void doStuff() {
		calcSwimSpeed();
		// assimilate energy
		energyIntake();
		// (netlogo BUT NOT IN USE):  set EA e-assim
		
// FIXME	    ; for reimplementation check:
//	        if time-step >= 1 and year = sc-year and remainder time-step 48 = 0 [ reimplementation-check ]

		// FIXME SHOULD THIS REALLY RUN FOR EACH PORPOISE??
		if (ReimplementationCheck.getInstance().shouldProduce()) {
			ReimplementationCheck.getInstance().produce(this.porp.getContext());
		}

		// then allocate it to:
		if (porp.isAlive()) {
			maintenance();
		}
		if (porp.isAlive()) {
			thermoregulation();
		}
		if (porp.isAlive()) {
			locomotion();
		}
		if (porp.isAlive()) {
			reproduction();
		}
		if (porp.isAlive()) {
			growth();
		}
		if (porp.isAlive()) {
			storage();
		}
		if (porp.isAlive()) {
			updStateVariables();
		}
	}

	@Override
	public void dailyTask() {
/*
    ; keep track of average energy intake - averaged over a week as lots of variablity between timesteps
    if (remainder sim-day 7 ) = 0 [
      set daily-food ((sum food-intake-list) / 7) * IR-to-EA
      set food-intake-list []
    ]

    let s-list-lgt length storage-level-daily                                   ; CHANGED BY CARA to reference storage level rather than energy level
    set storage-level-daily remove-item ( s-list-lgt - 1 ) storage-level-daily  ; CHANGED BY CARA
    let s-mean storage-level-sum / 48  ; 48 half-hour steps per day             ; CHANGED BY CARA
    set storage-level-daily fput (precision s-mean 3) storage-level-daily       ; CHANGED BY CARA
*/
		if (SimulationTime.isBeginningOfWeek()) {
			var foodIntakeSum = foodIntakeList.stream().mapToDouble(Double::doubleValue).sum();
			dailyFood = (foodIntakeSum / 7.0d) * Globals.IRToEA;
			foodIntakeList.clear();
		}
		
		final double sMean = this.storageLevelSum / 48.0;
		this.storageLevelDaily.add(Math.round(sMean * 1000.0) / 1000.0);
	}

	public CaraEnergetics(Porpoise porp) {
		this(porp, false);
	}

	public CaraEnergetics(Porpoise porp, boolean initialPopulation) {
		this.porp = porp;

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
//					this.daysSinceMating = Globals.getRandomSource().getInitialDaysSinceMating();
					
					// set mass-f random-normal 0.74 0.19  !!!
					massF = RandomHelper.createNormal(0.74d, 0.19d).nextDouble();
				} else {
					this.pregnancyStatus = 0;
				}
			}
		}
		
		porpsSetupParams();
	}

	/**
	 * Constructor for calf energetics
	 * Originally from "to wean-calf"
	 * @param mothersEnergetics
	 * 
	 * STATUS: InDev - nearly done
	 */
	public CaraEnergetics(CaraEnergetics me) {
		/*
		  hatch-porps  n-offspr [                                                   ; Create an independant porpoise agent representing weaned calf
		    setxy random-xcor random-ycor
		    set age age-calf                                                        ; Set porpoise age to calculated age
		    set mass-struct mass-calf - (v-blub-calf * dens-blub)                   ; Set structural mass to difference between mass of calf and blubber mass
		    set v-blub v-blub-calf                                                  ; Set blubber volume to calf blubber volume
		    set weight mass-calf                                                    ; Set weight as calf mass
		    set B0 11.13 + random-float 0.04 - random-float 0.04                    ; Normalization constant
		    set lambda random-normal 0.25 0.016                                     ; Ratio of active to passive drag
		    set DE-lip 0.74 + random-float 0.16                                     ; Deposition efficiency of lipid
		    set DE-pro 0.43 + random-float 0.13                                     ; Deposition efficiency of protein
		    set lgth-inf random-normal 158.12 4.68                                  ; Length asymptotic value
		    set lgth-0 random-normal 94.82 1.69                                     ; Length initial value
		    set lgth-k random-normal 0.41 0.06                                      ; Length k value
		    set lgth lgth-calf                                                      ; Set length to calf length
		    set surface-area 0.093 * weight ^ 0.57                                  ; Surface area
		    set v-blub-min (weight * 0.05) / dens-blub                              ; set minimum blubber as 10% of weight
		    set v-blub-repro (weight * repro-min-SL / dens-blub)                    ; set reproductive blubber volume threshold using minimum reproductive blubber volume
		    set e-repo-min (v-blub-repro - v-blub-min) * dens-blub * ED-lip         ; calculate energy required for reproductive threshold
		    set SL-mean ((lgth * -0.3059) + 0.7066)* IR-temp-mod                    ; mean storage level percentages from McLellan et al. 2002 for mature, calf, and immature porpoises
		    set v-blub-mean (weight * SL-mean / dens-blub)
		    set e-storage (v-blub - v-blub-min) * dens-blub * ED-lip                ; storage energy as the amount of energy stored in blubber over minimum threshold
		 */
		// FIXME setxy, set age (in constructor which takes PorpoiseEnergy)
		this.massStruct = me.massCalf - (me.vBlubCalf * Globals.densBlub);
		this.vBlub = me.vBlubCalf;
		this.weight = me.massCalf;
		this.B0 = 11.13d + RandomHelper.nextDoubleFromTo(0, 0.04) - RandomHelper.nextDoubleFromTo(0, 0.04); // FIXME from is exclusive in Repast but inclusive in netlogo
		this.lambda = RandomHelper.createNormal(0.25, 0.016).nextDouble();
		this.DELip = 0.74 + RandomHelper.nextDoubleFromTo(0, 0.16);
		this.DEPro = 0.43 + RandomHelper.nextDoubleFromTo(0, 0.13);
		this.lgthInf = RandomHelper.createNormal(158.12, 4.68).nextDouble();
		this.lgth0 = RandomHelper.createNormal(94.82, 1.69).nextDouble();
		this.lgthK = RandomHelper.createNormal(0.41, 0.06).nextDouble();
		this.lgth = me.lgthCalf;
		this.surfaceArea = 0.093 * Math.pow(weight, 0.57);
		this.vBlubMin = (weight * 0.05) / Globals.densBlub;
		this.vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		this.eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		this.SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		this.vBlubMean = (weight * SLMean / Globals.densBlub);
		this.eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		
		/*
		  ; initialize reproductive costs
		    set ds-mating -99                                                       ; Initial days since mating at -99
		    set dsg-birth -99                                                       ; Initial days since giving birth at -99
		    set pregnancy-status 0                                                  ; Unable to get pregnant, too young
		    set with-lact-calf? false
		    set m-preg 0
		    set m-BMR-calf 0
		    set lgth-calf 0
		    set mass-calf 0
		    set mass-struct-calf 0
		    set m-thermo-calf 0
		    set max-grow-calf 0
		    set m-growth-calf 0
		    set m-blub-calf 0
		    set v-blub-calf 0
		    set e-calf 0
		    set m-lact 0
		    set m-lact-real 0
		    set vital-costs 0
		    set vital-costs-calf 0
		    set wean-scale-fact 1
		    set IR-record-calf 0
		 */
		this.dsgBirth = -99;
		this.pregnancyStatus = 0;
		this.withLactCalf = false;
		this.mPreg = 0.0d;
		this.mBMRCalf = 0.0d;
		this.lgthCalf = 0.0d;
		this.massCalf = 0.0d;
		this.massStructCalf = 0.0d;
		this.mThermoCalf = 0.0d;
		this.maxGrowCalf = 0.0d;
		this.mGrowthCalf = 0.0d;
		this.mBlubCalf = 0.0d;
		this.vBlubCalf = 0.0d;
		this.eCalf = 0.0d;
		this.mLact = 0.0d;
		this.mLactReal = 0.0d;
		this.vitalCosts = 0.0d;
		this.vitalCostsCalf = 0.0d;
		this.weanScaleFact = 1.0d;
		this.IRRecordCalf = 0.0d;
		
		/*
		  ; run upd-blubber-depths to initialize blubber values
		    upd-blubber-depths		
		    */
		updBlubberDepths();
	}
	
	

	/**
	 * FIXME This is not in the "simple" version, only in the full
	 */
	private void updBlubberDepths() {
		// This is a no-op (to be removed) according to Cara
	}
	
	// New for energetics
	/*
	 * STATUS: Dev complete, need unit test
	 */
	private void calcSwimSpeed() {
		/*
to calc-swim-speed ; calculates swimming speed for use in the thermoregulation and locomotion processes
  ifelse ( disp-type = 0 )
  [
    let swim-speed-pres-mov ((((10 ^ pres-logmov) * 100)) / 1800)
    let swim-speed-pres-log-trans ln(swim-speed-pres-mov * 100)
    set swim-speed swim-speed-pres-log-trans * 0.346                                    ; from movement data analysis-converting from swim-speed estimated on 30min to 6s scale from dead reckoned data
    if swim-speed <= 0.01 [set swim-speed 0.01]                                         ; negative clamp
  ]
  [
    set swim-speed (mean-disp-dist * 1000 * (1 /(30 * 60)))                             ; convert dispersal distance in km 30min-1 to m s-1
  ]
end
		 */
		if (!porp.getDispersalBehaviour().isDispersing()) {
			double swimSpeedPresMov = (Math.pow(10.0d,  porp.getPresLogMov()) * 100.0d) / 1800.0d;
			double swimSpeedPresLogTrans = Math.log(swimSpeedPresMov * 100.0d);
			this.swimSpeed = swimSpeedPresLogTrans * 0.346d;
			if (this.swimSpeed <= 0.01d) {
				this.swimSpeed = 0.01d;
			}
		} else {
			swimSpeed = (Globals.meanDispDist * 1000.0d * (1.0d / (30.0d * 60.0d)));
		}
	}

	private void patchZeroCheck() {
	}
	
	/*
	 * STATUS: DevComplete
	 */
	private void energyIntake() {
/*
to energy-intake
  let IR-struct-mass ((IR-coef * (mass-struct ^ 0.75))  * IR-temp-mod)         ; EQN 13: Calculate max ingestion rate for tick based on mass
  let preg-IR-sup (m-preg / (AE-food * IR-to-EA))                              ; EQN 15: Pregnant females will increase their food intake to cover pregnancy costs - Rojano-Doñate et al. 2018
  let lact-IR-sup (m-lact-real /(AE-food * IR-to-EA))                          ; EQN 16: Lactating females will increase their food intake to cover lactation costs - Douhard et al. 2016

  ; Calculate the amount of food needed to be ingested by the calf based on its size - EQN 23
  let IR-struct-mass-calf 0
  if with-lact-calf? = true and wean-scale-fact < 1 [set IR-struct-mass-calf (((IR-coef * mass-struct-calf ^ 0.75) * (1.00 - wean-scale-fact)) * IR-temp-mod)]

  ; Add total ingestion rate for the timestep
  let IR-timestep IR-struct-mass + preg-IR-sup + lact-IR-sup                   ; EQN 17

  ; porpoises keep track of intake rate when they haven't encountered food to compensate - EQN 18
  set IR-record IR-record + IR-timestep
  if with-lact-calf? = TRUE and wean-scale-fact < 1 [ set IR-record-calf IR-record-calf + IR-struct-mass-calf ]

  ; Check food levels of patch here and adjust IR
  let IR-real 0
  let IR-real-calf  0
  let split 0
  let food-available [food-level] of patch (item 0 (item 1 pos-list)) (item 1 (item 1 pos-list))  ; item 0 pos-list was the last added element, i.e. the current position

  if (debug = 11) [ if food-available < 0 [ print (word "WARNING: Food-level of " (patch (item 0 (item 1 pos-list)) (item 1 (item 1 pos-list))) " < 0")]]
*/
		double IRStructMass = ((Globals.IRCoef * (Math.pow(massStruct, 0.75d)))  * Globals.IRTempMod);
		double pregIRSup = (mPreg / (Globals.AEFood * Globals.IRToEA));
		double lactIRSup = (mLactReal /(Globals.AEFood * Globals.IRToEA));
		double IRStructMassCalf = 0.0d;
		if (withLactCalf && weanScaleFact < 1) {  // FIXME Integer correct?
			IRStructMassCalf = (((Globals.IRCoef * Math.pow(massStructCalf, 0.75)) * (1.00 - weanScaleFact)) * Globals.IRTempMod);
		}
		double IRTimestep = IRStructMass + pregIRSup + lactIRSup;
		IRRecord = IRRecord + IRTimestep;
		if (withLactCalf && weanScaleFact < 1) {
			IRRecordCalf = IRRecordCalf + IRStructMassCalf;
		}
		double IRReal = 0.0d;
		double IRRealCalf = 0.0d;
		double split = 0.0d;
		double foodAvailable = Globals.getCellData().getFoodLevel(ndPointToGridPoint(this.porp.posList.get(1)));  // Original states that it should be item 0 but uses item 1!

		if (DebugLog.isEnabledFor(11)) {
			if (foodAvailable < 0.0d) {
				DebugLog.print("WARNING: Food-level of " + ndPointToGridPoint(this.porp.posList.get(1)) + " < 0");
			}
		}
		
/*
  ifelse with-lact-calf? = FALSE or wean-scale-fact = 1
  [ ifelse food-available > IR-record
    [set IR-real IR-record]
    [set IR-real food-available]
  ]
  [ ifelse food-available > IR-record + IR-record-calf
    [
      set IR-real IR-record
      set IR-real-calf IR-record-calf
    ]
    [ set split ( IR-record / (IR-record + IR-record-calf))
      set IR-real food-available * split
      set IR-real-calf food-available * (1 - split)
    ]]
*/
		if (withLactCalf == false || weanScaleFact == 1) { // FIXME Check that this == 1 is correct with doubles
			if (foodAvailable > IRRecord) {
				IRReal = IRRecord;
			} else {
				IRReal = foodAvailable;
			}
		} else {
			if (foodAvailable > IRRecord + IRRecordCalf) {
				IRReal = IRRecord;
				IRRealCalf = IRRecordCalf;
			} else {
				split = ( IRRecord / (IRRecord + IRRecordCalf));
				IRReal = foodAvailable * split;
				IRRealCalf = foodAvailable * (1.0d - split);
			}
		}
/*

  ; reduce IR-record by IR
  set IR-record IR-record - IR-real
  if with-lact-calf? = true and wean-scale-fact != 1 [ set IR-record-calf IR-record-calf - IR-real-calf ]

  ; adjust intake rate based on fatness
  let max-SL (lgth * -0.39635) + 1.02347                                ; EQN 19: Calculated from Galatius & Kinze, unps.
  let over-mean-SL (storage-level - SL-mean) / (max-SL - SL-mean)       ; EQN 20: Check if storage levels exceed mean storage levels
  if over-mean-SL > 1 [ set over-mean-SL 1 ]
  let IR-SL-mod 1

  if over-mean-SL > 0 [                                                 ; If they do,
    let FC (-1 * satiation-c)
    set IR-SL-mod exp(over-mean-SL * FC)                                ; EQN 21: Calculate hunger modifier based on how much fatter they are than the average
    if IR-SL-mod < 0 [ set IR-SL-mod 0 ]                                ; Negative clamp
    set IR-real IR-real * IR-SL-mod                                     ; Adjust ingestion rate using the calculated modifier
  ]
*/
		IRRecord = IRRecord - IRReal;
		if (withLactCalf && weanScaleFact != 1) {
			IRRecordCalf = IRRecordCalf - IRRealCalf;
		}
		double maxSL = (lgth * -0.39635d) + 1.02347d;
		double overMeanSL = (storageLevel - SLMean) / (maxSL - SLMean);
		if (overMeanSL > 0.0d) {
			overMeanSL = 1.0d;
		}
		double IRSLMod = 1.0d;
		if (overMeanSL > 0.0d) {
			double FC = (-1.0d * Globals.satiationC);
			IRSLMod = Math.exp(overMeanSL * FC);
			if (IRSLMod < 0.0d) {
				IRSLMod = 0.0d;
			}
			IRReal = IRReal * IRSLMod;
		}
/*

  let food-eaten 0
  ifelse with-lact-calf? = FALSE or wean-scale-fact = 1 [set food-eaten IR-real][set food-eaten IR-real + IR-real-calf]

  ; Add to list to calculate daily ingestion
  set food-intake-list lput IR-real food-intake-list

  ; Remove eaten food from patches
  ask patch (item 0 (item 1 pos-list)) (item 1 (item 1 pos-list))  ; item 0 pos-list was the last added element, i.e. the current position
  [
      if food-level > 0 [
      set food-level food-level - food-eaten
      if ( food-level < 0.01 ) [ set food-level 0.01 ]
      ; if ( food-level > 0 and food-level <= 0.02 * maxU ) [ set pcolor 45 ]    ; turned off for efficiency
      ; if ( food-level > 0.02 and food-level <= 0.2 * maxU ) [ set pcolor 27 ]
      ; if ( food-level > 0.2 * maxU and food-level <= 0.5 * maxU ) [ set pcolor 66 ]
      ; if ( food-level > 0.5 * maxU and food-level <= 0.9 * maxU ) [ set pcolor 63 ]
      ; if ( food-level > 0.9 * maxU and food-level < 1.1 * maxU ) [ set pcolor 61 ]
      ; if ( food-level > 1.1 * maxU ) [ set pcolor 61 ]
    ]
   ]

  set e-assim (IR-real * IR-to-EA * AE-food )                       ; EQN 22: Assimilate food of patch using the ingestion rate, the calibrated IR-real to EA conversion factor, and the assimilation efficiency of food
  set e-assim-calf (IR-real-calf * IR-to-EA * AE-food )             ; EQN 24: Calves assimilate food of patch using the ingestion rate, the calibrated IR-real to EA conversion factor, and the assimilation efficiency of food
*/
		double foodEaten = 0.0d;
		if (withLactCalf == false || weanScaleFact == 1) {
			foodEaten = IRReal;
		} else {
			foodEaten = IRReal + IRRealCalf;
		}
		
		foodIntakeList.add(IRReal);
		Globals.getCellData().eatFood(ndPointToGridPoint(this.porp.posList.get(1)), foodEaten);

		this.porp.getPersistentSpatialMemory().updateMemory(this.porp.getPosition(), IRReal);
		if (calfPsm != null && this.porp.getDispersalBehaviour().calfHasPSM()) {
			calfPsm.updateMemory(this.porp.getPosition(), IRRealCalf);
		}

		eAssim = (IRReal * Globals.IRToEA * Globals.AEFood);
		eAssimCalf = (IRRealCalf * Globals.IRToEA * Globals.AEFood);
/*
  ; Debug energy intake
  if precision sim-day 3 = floor sim-day [if (debug = 11) [ if (who = 0) [ if food-available > 0 [
    print "Debugging energy intake: focal follow"
    print word "ID                 " who
    let preg 0
    ifelse m-preg > 0 [set preg "true"] [set preg "false"]
    print word "Pregnant?          " preg
    print word "With-lact-calf?    " with-lact-calf?
    print word "IR-struct-mass     " IR-struct-mass
    print (word "Repro supps       " " Preg: " preg-IR-sup " Lact: "Lact-IR-sup)
    print word "IR-timestep        " IR-timestep
    print word "Food-available     " food-available
    print word "IR-real            " IR-real
    print word "IR-real-calf       " IR-real-calf
    print word "over-mean-SL       " over-mean-SL
    print word "IR-SL-mod          " IR-SL-mod
    print word "food-eaten         " food-eaten
    print word "e-assim            " e-assim
    print word "e-assim-calf       " e-assim-calf
    print "END: Debugging energy intake: focal follow"
    print ""
  ]]]]
end */
		// FIXME Omitted the debug part above
	}

	/*
	 * STATUS: DevComplete except for TODOs and FIXME below
	 */
	private void maintenance() {

		/*
  ;;;;; COST CALCULATION ;;;;;
  set m-BMR (B0 * (weight ^ 0.75) * 1800)                                                       ; EQN 26: Calculate BMR using the current weight and B0 value, convert from watts to BMR timestep-1 by multiplying by 1800 seconds

  ;;;;; ALLOCATION ;;;;;
  ifelse e-assim >= m-BMR                                                                       ; Check if the energy assimilated is sufficient to cover BMR
    [ set e-assim e-assim - m-BMR ]                                                             ; If so reduce the available energy by the BMR cost
    [ set e-storage e-storage + e-assim                                                         ; If e-assim is not sufficient to cover BMR then add e-assim to storage and
      ifelse e-storage > m-BMR                                                                  ; Check if updated storage can cover BMR costs
    [ set e-storage e-storage - m-BMR                                                           ; If so, mobilize stored energy to do so
      set v-blub v-blub - (((m-BMR - e-assim)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub )) ; Reduce the amount of blubber volume by the BMR loss
    ]
    [                                                                                           ; If not, die
      set list-of-dead-age lput (floor age) list-of-dead-age
      set list-of-dead-day lput (floor sim-day) list-of-dead-day

      if (debug = 10) [ print word who " died of low body condition-Maintenance" ]
      die
    ]
      set e-assim 0                                                                             ; Set e-assim to zero
    ]
		 * 
		 */
		mBMR = (B0 * (Math.pow(weight, 0.75d)) * 1800.0d);
//		System.out.println("e-storage: " + eStorage + ",  e-assim: " + eAssim + ",  m-BMR: " + mBMR);
		if (eAssim >= mBMR) {
			eAssim = eAssim - mBMR;
		} else {
			eStorage = eStorage + eAssim;
			if (eStorage > mBMR) {
				eStorage = eStorage - mBMR;
				vBlub = vBlub - (((mBMR - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
			} else {
				Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());

				if (DebugLog.isEnabledFor(10)) {
					DebugLog.print(porp.getId() + " died of low body condition-Maintenance");
				}
				System.out.println("DIE");
				porp.die(CauseOfDeath.Starvation); // FIXME Correct cause?
			}
			eAssim = 0.0d;
		}
	}

	/*
	 * STATUS: DevComplete except FIXMEs
	 */
	private void thermoregulation() {
		/*
to thermoregulation ;;; TO BE TOTALLY CHANGED
 ;;;;; COST CALCULATION ;;;;;

  ; Here pull value of e-thermo (the initial calculation of heat balance; positive means heat loss, negative more heat is produced than needed) from the lookup table (ThermoregulationLookupTable.csv)
  ; Costs should be based on water temperature,	swim speed,	mass,	and storage level
  ;; Input values used are:
  ;;; water temperature = -5,  0,  5, 10, 15, 20, 25, 30, 35
  ;;; swim speed = 0, 0.47, 0.94, 1.41, 1.88, 2.35, 2.82, 3.29, 3.76
  ;;; mass = 10, 20, 30, 40, 50, 60, 70, 80, 90
  ;;; storage level = 0.05, 0.0525, 0.055, 0.06, 0.08, 0.12, 0.2, 0.36, 0.68

  ifelse e-thermo >= 0                                                                                  ; If not double check that the estimated metabolic cost of thermoregulation is greater than zero (negative check)
   [ set m-thermo e-thermo ]                                                                            ; If it is, set thermo costs to the estimated metabolic costs
    [ set m-thermo 0 ]                                                                                  ; If not, set to zero

 ;;;;; ALLOCATION ;;;;;
  ifelse e-assim >= m-thermo                                                                         ; check if the energy assimilated is sufficient to cover thermoregulatory costs
    [ set e-assim e-assim - m-thermo ]                                                               ; if so reduce the available energy by the thermo cost
    [ set e-storage e-storage + e-assim                                                              ; if e-assim is not sufficient to cover thermoregulation then add e-assim to storage and
      ifelse e-storage > m-thermo                                                                    ; check if updated storage can cover thermo costs
    [ set e-storage e-storage - m-thermo                                                             ; if so, mobilize stored energy to do so
      set v-blub v-blub - (((m-thermo - e-assim) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))  ; reduce the amount of blubber volume by the thermo loss
    ]
    [                                                                                                ; if not, die
      set list-of-dead-age lput (floor age) list-of-dead-age
      set list-of-dead-day lput (floor sim-day) list-of-dead-day

      if (debug = 10) [
        print word who " died of low body condition-Frozen"
      ]
      die
    ]
      set e-assim 0                                                                                          ; set e-assim to zero
    ]
end
		 */
		final double waterTemp = Globals.getCellData().getTemperature(porp.getPosition());
		double eThermo = ThermoregulationLookupTable.getInstance().getValueInBin(waterTemp, swimSpeed, (int) Math.round(weight), storageLevel);  // FIXME Values to use here  -- int cast is not good
		if (eThermo >= 0.0d) {
			this.mThermo = eThermo;
		} else {
			this.mThermo = 0.0d;
		}
		
		// Allocation
		if (eAssim >= mThermo) {
			eAssim = eAssim - mThermo;
		} else {
			eStorage = eStorage + eAssim;
			if (eStorage > mThermo) {
				eStorage = eStorage - mThermo;
				vBlub = vBlub - (((mThermo - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
			} else {
				Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());

				if (DebugLog.isEnabledFor(10)) {
					DebugLog.print(porp.getId() + " died of low body condition-Frozen");
				}
				System.out.println("die thermo");
				porp.die(CauseOfDeath.Starvation); // FIXME Correct cause?
			}
			eAssim = 0.0d;
		}
	}

	/*
	 * STATUS: DevComplete
	 */
	private void locomotion() {
		/*
to locomotion
  ;;;;; COST CALCULATION ;;;;;
  let aero-eff 0.13478 + 0.441 * ((swim-speed / 4.2) ^ 3) - 0.422 * ((swim-speed / 4.2) ^ 6)                    ; EQN 36: Aerobic efficiency calculation from Hind & Gurney 1997 modified for a 25 % max eff and 4.2 m s-1 max speed

  ; Calculate the Reynold's number
  let Re (lgth * swim-speed) / kin-visc-w                                                                       ; EQN 37: As in Fish 1998
  set Re log Re 10                                                                                              ; Convert to log scale

  ; Calculate CD using the log transformed relationship found for white sided dolphins in Tanaka et al 2019 - EQN 38
  let drag-coef (-0.113188 * Re + -1.535837)
  set drag-coef (10 ^ drag-coef)

  ; Calculate costs of locomotion
  set m-loco (lambda * density-w * surface-area * drag-coef * (swim-speed ^ 3)) / (2 * aero-eff * prop-eff)     ; EQN 39: As in Hind & Gurney 1997
  set m-loco m-loco * 1800                                                                                      ; Convert from watts to cost per timestep-1 by multiplying by 1800 seconds
  set m-loco-ineff-pts (1 - aero-eff) * m-loco                                                                  ; EQN 27: Save the waste heat generated this step to use in the thermo calculations next step
*/
		double aeroEff = 0.13478d + 0.441d * (Math.pow((swimSpeed / 4.2), 3.0d)) - 0.422 * (Math.pow((swimSpeed / 4.2), 6.0d));
		double Re = (lgth * swimSpeed) / Globals.getCellData().getKinViscW(porp.getPosition());
		Re = Math.log10(Re);
		
		double dragCoef = (-0.113188d * Re + -1.535837d);
		dragCoef = Math.pow(10.0d, dragCoef);
		
		mLoco = (lambda * Globals.getCellData().getDensityW(porp.getPosition()) * surfaceArea * dragCoef * (Math.pow(swimSpeed, 3.0d))) / (2.0d * aeroEff * Globals.propEff);
		mLoco = mLoco * 1800.0d;
		mLocoIneffPts = (1.0d - aeroEff) * mLoco;
		
/*
  ;;;;; ALLOCATION ;;;;;
  ifelse e-assim >= m-loco                                                                           ; Check if the energy assimilated is sufficient to cover locomotive costs
    [ set e-assim e-assim - m-loco ]                                                                 ; If so reduce the available energy by the locomotive cost
    [ set e-storage e-storage + e-assim                                                              ; If e-assim is not sufficient to cover locomotion then add e-assim to storage and
      ifelse e-storage >= m-loco                                                                     ; Check if updated storage can cover locomotive costs
    [ set e-storage e-storage - m-loco                                                               ; If so, mobilize stored energy to do so
      set v-blub v-blub - (((m-loco - e-assim)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))     ; Reduce the amount of blubber volume by the locomotive loss
    ]
    [ set e-storage (v-blub-min * dens-blub * ED-lip) ]
      set e-assim 0                                                                                  ; Set e-assim to zero
      ; For now this does not slow down swimming speed, but could in the future
    ]
end		 */
		if (eAssim >= mLoco) {
			eAssim = eAssim - mLoco;
		} else {
			eStorage = eStorage + eAssim;
			if (eStorage >= mLoco) {
				eStorage = eStorage - mLoco;
				vBlub = vBlub - (((mLoco - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
			} else {
				eStorage = (vBlubMin * Globals.densBlub * Globals.EDLip);
			}
			eAssim = 0.0d;
			// For now this does not slow down swimming speed, but could in the future
		}
	
	}

	/*
	 * STATUS: DevComplete - exception fixme
	 */
	private void reproduction() {
		/*
to reproduction
   if with-lact-calf? = TRUE [
      lactation ]                                                                    ; Lactating females calculate lactation costs

  if pregnancy-status = 1 [
      pregnancy                                                                      ; Calculate pregnancy costs for pregnant females
      if round ds-mating =  t-gest [ give-birth ]]                                   ; Give birth
end
		 */
		if (withLactCalf) {
			lactation();
		}
		
		if (pregnancyStatus == 1) {
			pregnancy();
			if (dsMating == Globals.tGest) {
				giveBirth();
			}
			
			// FIXME Note in the original model this seems close to this in updPregnancyStatus():
			// 		if (this.pregnancyStatus == 1 && this.daysSinceMating == SimulationParameters.getGestationTime()) {
		}
	}

	/**
	 * STATUS: DevComplete - except for FIXME
	 */
	private void weanCalf() {
		/*
to wean-calf
  let n-offspr 0                                                                                  ; Create temporary variable for the number of offspring
  if (sex-calf = "female" ) [ set n-offspr 1 ]                                                    ; Only female calves 'hatch'
  if (debug = 9 ) [
    let tmp word who " hatching "
    print word tmp n-offspr
  ]

  let age-calf dsg-birth / 360
  set dsg-birth -99

*/
		int nOffspr = 0;

		if ("female".equals(sexCalf)) {
			nOffspr = 1;
		}
		if (DebugLog.isEnabledFor(9)) {
			DebugLog.print("{} hatching {}", porp.getId(), nOffspr);
		}

		double ageCalf = dsgBirth / 360.0d;
		dsgBirth = -99;
	/*
  hatch-porps  n-offspr [                                                   ; Create an independant porpoise agent representing weaned calf
    setxy random-xcor random-ycor
    set age age-calf                                                        ; Set porpoise age to calculated age
    set mass-struct mass-calf - (v-blub-calf * dens-blub)                   ; Set structural mass to difference between mass of calf and blubber mass
    set v-blub v-blub-calf                                                  ; Set blubber volume to calf blubber volume
    set weight mass-calf                                                    ; Set weight as calf mass
    set B0 11.13 + random-float 0.04 - random-float 0.04                    ; Normalization constant
    set lambda random-normal 0.25 0.016                                     ; Ratio of active to passive drag
    set DE-lip 0.74 + random-float 0.16                                     ; Deposition efficiency of lipid
    set DE-pro 0.43 + random-float 0.13                                     ; Deposition efficiency of protein
    set lgth-inf random-normal 158.12 4.68                                  ; Length asymptotic value
    set lgth-0 random-normal 94.82 1.69                                     ; Length initial value
    set lgth-k random-normal 0.41 0.06                                      ; Length k value
    set lgth lgth-calf                                                      ; Set length to calf length
    set surface-area 0.093 * weight ^ 0.57                                  ; Surface area
    set v-blub-min (weight * 0.05) / dens-blub                              ; set minimum blubber as 10% of weight
    set v-blub-repro (weight * repro-min-SL / dens-blub)                    ; set reproductive blubber volume threshold using minimum reproductive blubber volume
    set e-repo-min (v-blub-repro - v-blub-min) * dens-blub * ED-lip         ; calculate energy required for reproductive threshold
    set SL-mean ((lgth * -0.3059) + 0.7066)* IR-temp-mod                    ; mean storage level percentages from McLellan et al. 2002 for mature, calf, and immature porpoises
    set v-blub-mean (weight * SL-mean / dens-blub)
    set e-storage (v-blub - v-blub-min) * dens-blub * ED-lip                ; storage energy as the amount of energy stored in blubber over minimum threshold

  ; initialize reproductive costs
    set ds-mating -99                                                       ; Initial days since mating at -99
    set dsg-birth -99                                                       ; Initial days since giving birth at -99
    set pregnancy-status 0                                                  ; Unable to get pregnant, too young
    set with-lact-calf? false
    set m-preg 0
    set m-BMR-calf 0
    set lgth-calf 0
    set mass-calf 0
    set mass-struct-calf 0
    set m-thermo-calf 0
    set max-grow-calf 0
    set m-growth-calf 0
    set m-blub-calf 0
    set v-blub-calf 0
    set e-calf 0
    set m-lact 0
    set m-lact-real 0
    set vital-costs 0
    set vital-costs-calf 0
    set wean-scale-fact 1
    set IR-record-calf 0

  ; run upd-blubber-depths to initialize blubber values
    upd-blubber-depths
 ] */
		CaraEnergetics calfEnergetics = new CaraEnergetics(this);
		// FIXME Other DEPONS give-birth sets age to 0 (?)
		final Porpoise calf = new Porpoise(this.porp, ageCalf);
		calf.setEnergetics(calfEnergetics);
		calfEnergetics.setPorp(calf);
		calfEnergetics.porpsSetupParams(); // FIXME ADDED BY JONAS, Ok?
		ContextUtils.getContext(this.porp).add(calf);
		calf.setPosition(this.porp.getPosition()); // FIXME setxy random-xcor random-ycor??
		calf.moveAwayFromLand(); // Initializes the pos list. TODO: not nice to do here, should be done
		// elsewhere
		Globals.getMonthlyStats().addBirth();

	/*
  ; reset mother lactation variables
     set m-BMR-calf 0
     set lgth-calf 0
     set mass-calf 0
     set mass-struct-calf 0
     set m-thermo-calf 0
     set m-growth-calf 0
     set m-blub-calf 0
     set e-calf 0
     set m-lact 0
     set m-lact-real 0
     set vital-costs 0
     set vital-costs-calf 0
     set wean-scale-fact 1
     set with-lact-calf? false
     set IR-record-calf 0
end
		 */
		mBMRCalf = 0.0d;
		lgthCalf = 0.0d;
		massCalf = 0.0d;
		massStructCalf = 0.0d;
		mThermoCalf = 0.0d;
		mGrowthCalf = 0.0d;
		mBlubCalf = 0.0d;
		eCalf = 0.0d;
		mLact = 0.0d;
		mLactReal = 0.0d;
		vitalCosts = 0.0d;
		vitalCostsCalf = 0.0d;
		weanScaleFact = 1.0d;
		withLactCalf = false;
		IRRecordCalf = 0.0d;
	}

	private void setPorp(Porpoise porp) {
		this.porp = porp;
	}

	/*
	 * Status: DevComplete
	 */
	private void growth() {
/*
to growth

   if mass-struct < m-str-inf                                                                                  ; If smaller than max female mass
      [
*/
		if (massStruct < mStrInf) {
/*
        ;;;;; COST CALCULATION ;;;;;
        set e-assim-grow e-assim / 2                                                                           ; Split available energy between growth and blubber

        set max-grow  (m-str-k / 17280) * ((m-str-inf ^ (1 / 3) * mass-struct ^ (2 / 3)) - mass-struct)        ; EQN 53: Maximum growth in a timestep ; 17280 adjuster to convert from annual to 30min basis

        ; calculate costs of growth
        set m-growth (max-grow * (ED-lean-mass + ED-lean-mass * (1 - DE-lean-mass)))                           ; EQN 54: Convert max growth of mass in kg to energy in joules per 30min
        if m-growth < 0 [set m-growth 0]                                                                       ; Negative clamp
*/
			eAssimGrow = eAssim / 2.0d;
			maxGrow = (mStrK / 17280.0d) * ((Math.pow(Math.pow(mStrInf, (1.0d / 3.0d)) * massStruct, (2.0d / 3.0d))) - massStruct);
			
			mGrowth = (maxGrow * (EDLeanMass + EDLeanMass * (1.0d - DELeanMass)));
			if (mGrowth < 0.0d) {
				mGrowth = 0.0d;
			}
/*
        ;;;;; ALLOCATION ;;;;;
        ; EQN 57 within allocation possibilities
        ifelse e-assim-grow >= m-growth                                                                        ; Check if enough energy is available to cover completely
        [
          set e-assim e-assim - m-growth                                                                       ; If so, deplete available energy by the energy needed for growth
          set e-assim-grow 0                                                                                   ; Set e-assim for growth to zero
          set mass-struct mass-struct + max-grow                                                               ; Add growth mass to structural mass
          let growth-rate max-grow                                                                             ; Set growth rate to max growth rate
        ]
*/
			if (eAssimGrow >= mGrowth) {
				eAssim = eAssim - mGrowth;
				eAssimGrow = 0.0d;
				massStruct = massStruct + maxGrow;
				double growthRate = maxGrow;  // FIXME Not used??
			} else {
/*
        [
          ifelse v-blub >= v-blub-mean                                                                         ; Check if blubber is in good condition
          [                                                                                                    ; If body condition > threshold grow maximally and pull extra needed energy from storage
            set e-storage e-storage + (e-assim-grow - m-growth)                                                ; Reduce e-storage by difference between e-assim for growth and energy needed for growth
            set e-assim e-assim -  e-assim-grow                                                                ; Reduce e-assim by e-assim for growth
            set v-blub v-blub - (((m-growth - e-assim-grow) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub )) ; Reduce the amount of blubber volume by the growth difference
            set e-assim-grow 0                                                                                 ; Set e-assim for growth to zero
            set mass-struct mass-struct + max-grow                                                             ; Grow maximally
            let growth-rate max-grow                                                                           ; Set growth rate to max growth rate
           ]
           [
              let growth-rate (e-assim-grow / ED-lean-mass) * DE-lean-mass                                     ; Else, reduce growth rate and grow suboptimally; use only available energy for growth
              set mass-struct mass-struct + growth-rate                                                        ; Grow by adjusted growth rate
              set e-assim e-assim -  e-assim-grow                                                              ; Reduce e-assim by e-assim for growth
              set e-assim-grow 0                                                                               ; Set e-assim for growth to zero
           ]
        ]
      ]
*/
				if (vBlub >= vBlubMean) {
					eStorage = eStorage + (eAssimGrow - mGrowth);
					eAssim = eAssim - eAssimGrow;
					vBlub = vBlub - (((mGrowth - eAssimGrow) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					eAssimGrow = 0.0d;
					massStruct = massStruct + maxGrow;
					double growthRate = maxGrow;  // FIXME Not used?
				} else {
					double growthRate = (eAssimGrow / EDLeanMass) * DELeanMass;
					massStruct = massStruct + growthRate;
					eAssim = eAssim - eAssimGrow;
					eAssimGrow = 0.0d;
				}
			}
	}
/*
  let lgth-t ((lgth-inf * exp (ln(lgth-0 / lgth-inf)* exp((- lgth-k)* age)))/ 100)                             ; EQN 58: Update length based on age
  if lgth-t > lgth [set lgth lgth-t]

 end
  */
		double lgthT = ( (lgthInf * Math.exp(Math.log(lgth0 / lgthInf) * Math.exp((- lgthK)* this.porp.getAge()))) / 100.0d );
		if (lgthT > lgth) {
			lgth = lgthT;
		}
	}

	/*
	 * STATUS: DevComplete, except fixme
	 */
	private void storage() {
/*
to storage

  ; if any excess energy remains after allocation, save it to storage
  if e-assim >= 0 [                                                                        ; if any remaining energy
    let add-blub ((e-assim * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))              ; EQN 59: convert assimilated energy to blubber volume
    set v-blub v-blub + add-blub                                                           ; update blubber volume
    set e-assim 0]                                                                         ; set e-assim to 0


  ; update blubber depths once per day
  if ((remainder time-step 48) = 0) [
    set storage-level-sum 0                                                                ; reset daily ; CHANGED BY CARA
    upd-blubber-depths
  ]

end
 */
		if (eAssim >= 0.0d) {
			double addBlub = ((eAssim * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
			vBlub = vBlub + addBlub;
			eAssim = 0.0d;
		}
		// FIXME This should probably be moved to DailyTasks
		if (SimulationTime.isBeginningOfDay()) {
			storageLevelSum = 0.0d;
			updBlubberDepths();
		}
	}

	/*
	 * STATUS: DevComplete
	 */
	private void updStateVariables() {
/*
to upd-state-variables

  set m-tot m-BMR + m-loco + m-thermo + m-growth + m-preg + m-lact-real        ; EQN 25: Calculate total expended energy

  set weight mass-struct + (v-blub * dens-blub)                                ; EQN 65: Update weight

  set storage-level (weight - mass-struct) / weight                            ; EQN 66: Storage level update
  set storage-level-sum storage-level-sum + storage-level

  ifelse pregnancy-status != 1                                                 ; EQN 67: Calculate surface area - Worthy and Edwards 1990
  [set surface-area 0.093 * weight ^ 0.57]
  [set surface-area 0.093 * (weight + (2 * mass-f)) ^ 0.57]

  set SL-mean ((lgth * -0.3059) + 0.7066) * IR-temp-mod                        ; EQN 68: Mean storage level percentages from McLellan et al. 2002 for mature, calf, and immature porpoises
  set v-blub-mean (weight * SL-mean / dens-blub)                               ; EQN 69: Mean blubber volume
  set v-blub-min (weight * 0.05) / dens-blub                                   ; EQN 70: Update minimum blubber volume
  set v-blub-repro (weight * repro-min-SL / dens-blub)                         ; EQN 71: Update reproductive blubber volume threshold, mean - 2*SD from McLellan et al. 2002

  set e-storage (v-blub - v-blub-min) * dens-blub * ED-lip                     ; EQN 72: Update storage energy using the updated blubber volume and minimum blubber volume
  set e-repo-min (v-blub-repro - v-blub-min) * dens-blub * ED-lip              ; EQN 73: Update reproductive energy threshold

  if with-lact-calf? = TRUE
  [
    set mass-calf mass-struct-calf + (v-blub-calf * dens-blub)                 ; EQN 74: Update calf mass
    set v-blub-calf-idl (mass-calf * calf-idl-SL / dens-blub)                  ; EQN 75: Mean percent blubber for calves in McLellan et al. 2002
  ]

end
 */
		mTot = mBMR + mLoco + mThermo + mGrowth + mPreg + mLactReal;
		weight = massStruct + (vBlub * Globals.densBlub);
		storageLevel = (weight - massStruct) / weight;
		storageLevelSum = storageLevelSum + storageLevel;
		
		if (pregnancyStatus != 1) {
			surfaceArea = 0.093d * Math.pow(weight, 0.57d);
		} else {
			surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
		}
		
		SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		vBlubMean = (weight * SLMean / Globals.densBlub);
		vBlubMin = (weight * 0.05d) / Globals.densBlub;
		vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		
		eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		
		if (withLactCalf) {
			massCalf = massStructCalf + (vBlubCalf * Globals.densBlub);
			vBlubCalfIdl = (massCalf * Globals.calfIdlSL / Globals.densBlub);
		}
	}

	/**
	 * STATUS: DevComplete
	 */
	private void resetLactationVars() {
		/*
to reset-lactation-vars

  set list-of-dead-age-calves lput (floor dsg-birth / 360) list-of-dead-age-calves
  set list-of-dead-day-calves lput (floor sim-day) list-of-dead-day-calves
  if (debug = 9) [print word who "'s calf died"]
  ; reset mother lactation variables
  set m-BMR-calf 0
  set lgth-calf 0
  set mass-calf 0
  set blub-calf 0
  set mass-struct-calf 0
  set m-thermo-calf 0
  set max-grow-calf 0
  set m-growth-calf 0
  set m-blub-calf 0
  set e-calf 0
  set m-lact 0
  set m-lact-real 0
  set vital-costs 0
  set vital-costs-calf 0
  set wean-scale-fact 1
  set with-lact-calf? false
  set dsg-birth -99
  set IR-record-calf 0
  set n-calf-lost n-calf-lost + 1

end
		 */
		Globals.getListOfDeadAgeCalves().addLast((int) Math.floor(dsgBirth / 360.0d));
		Globals.getListOfDeadDayCalves().addLast(SimulationTime.getDayOfSimulation());
		if (DebugLog.isEnabledFor(9)) {
			DebugLog.print(porp.getId() + "'s calf died");
		}
	
		mBMRCalf = 0.0d;
		lgthCalf = 0.0d;
		massCalf = 0.0d;
		blubCalf = 0.0d;
		massStructCalf = 0.0d;
		mThermoCalf = 0.0d;
		maxGrowCalf = 0.0d;
		mGrowthCalf = 0.0d;
		mBlubCalf = 0.0d;
		eCalf = 0.0d;
		mLact = 0.0d;
		mLactReal = 0.0d;
		vitalCosts = 0.0d;
		vitalCostsCalf = 0.0d;
		weanScaleFact = 1.0d;
		withLactCalf = false;
		dsgBirth = -99;
		// porp.caraResetLactationVars(); The variables in this method has been moved here. Duplicate for OriginalEnergetics as well
		IRRecordCalf = 0.0d;
		Globals.nCalfLost = Globals.nCalfLost + 1;
	}

	/*
	 * STATUS: DevComplete, compare with original (netlogo versions)
	 */
	private void porpUpdPregnancyStatus() {
/*
to porp-upd-pregnancy-status
  ; 0 (unable to mate, young/low energy); 1 (unable to mate, pregnant); 2 (ready to mate)

  ; Become ready to mate:
  if (pregnancy-status = 0 and age >= age-of-maturity ) [set pregnancy-status 2 ]         ; If of age set pregnancy status as ready to mate
*/
		if (pregnancyStatus == 0 && porp.getAge() >= porp.getAgeOfMaturity()) {
			pregnancyStatus = 2;
		}
/*
  ; Mate:
  if (pregnancy-status = 2 and round (sim-day - 360 * (year - 1)) = mating-day) [         ; If ready to mate and mating season:
    let numT count(porps with [age >= age-of-maturity])                                   ; Total number of mature porpoises
    let numP round (numT * pregnancy-rate)                                                ; Use a temp variable to determine number of porpoises that should get pregnant
    let numR count porps with [v-blub >= v-blub-repro and age >= age-of-maturity]         ; Find total number above threshold that can get pregnant
    if preg-chance = 0 [set preg-chance random-float 1]
*/
		if (this.pregnancyStatus == 2 && SimulationTime.getDayOfYear() == this.porp.getMatingDay()) {
			var fems = (IndexedIterable<Porpoise>) ContextUtils.getContext(this.porp).getObjects(Porpoise.class);
			int numT = 0;
			int numR = 0;
			for (Porpoise p : fems) {
				if (p.getAge() >= porp.getAgeOfMaturity()) {
					numT++;
					if (p.getEnergetics() instanceof CaraEnergetics) {
						CaraEnergetics ce = (CaraEnergetics) p.getEnergetics();
						if (ce.vBlub >= ce.vBlubRepro) {
							numR++;
						}
					} else {
						throw new RuntimeException("Porpoise energetics of type " + p.getEnergetics().getClass().getSimpleName() + " is not supported here.");
					}
				}
			}
			
			long numP = Math.round(numT * Globals.pregnancyRate);
			
/*
   ifelse numP <= numR                                                                    ; If there are more porpoises with a blubber volume higher than the reproductive threshold than the pregnancy rate
    [
      if (debug = 9 ) [ print "numR > numP" ]
      if (v-blub >= v-blub-repro) and (preg-chance <= pregnancy-rate) [
      set pregnancy-status 1                                                              ; These animals get pregnant
      set mass-f 0.000001                                                                 ; Set initial fetal mass "0"
      if (debug = 9 ) [ print word who " pregnant" ]
      set ds-mating 0 ]
    ]
    [
      ifelse (v-blub >= v-blub-repro)[                                                    ; If not enough animals are over reproductive threshold then have all animals over threshold
      set pregnancy-status 1                                                              ; Get pregnant
      set mass-f 0.000001                                                                 ; Set initial fetal mass "0"
      if (debug = 9 ) [ print word who " pregnant" ]   ; debug
      set ds-mating 0 ]
      [
      let upd-percent-pregnant ((numP - numR) / (numT - numR))                            ; Then some animals under threshold will get pregnant to reach 67% pregnancy rate
      if (preg-chance <= upd-percent-pregnant) [
      set pregnancy-status 1                                                              ; These animals get pregnant
      set mass-f 0.000001                                                                 ; Set initial fetal mass "0"
      if (debug = 9 ) [ print word who " pregnant" ]   ; debug
      set ds-mating 0 ]
      ]
  ]
]
*/
			if (numP <= numR) {
				DebugLog.print9("numR > numP");
				if (vBlub >= vBlubRepro && pregChance <= Globals.pregnancyRate) {
					pregnancyStatus = 1;
					massF = 0.000001d;
					DebugLog.print9("{} pregnant", this.porp.getId());
					dsMating = 0;
				}
			} else {
				if (vBlub >= vBlubRepro) {
					pregnancyStatus = 1;
					massF = 0.000001d;
					DebugLog.print9("{} pregnant", this.porp.getId());
					dsMating = 0;
				} else {
					double updPercentPregnant = ((numP - numR) / (numT - numR));
					if (pregChance <= updPercentPregnant) {
						pregnancyStatus = 1;
						massF = 0.000001d;
						DebugLog.print9("{} pregnant", this.porp.getId());
						dsMating = 0;
					}
				}
			}
		}

/*

  if pregnancy-status = 1 [ set ds-mating ds-mating + 1 ]                                 ; Update date in pregnancy period
  if with-lact-calf? [ set dsg-birth dsg-birth + 1 ]                                      ; Update date in the lactation period
end
 */
		if (pregnancyStatus == 1) {
			dsMating = dsMating + 1;
		}
		if (withLactCalf) {
			dsgBirth = dsgBirth + 1;
		}
	}

	private void pregnancy() {
/*
to pregnancy
  ; Check if abortion/pregnancy occurred (without this sometimes a rare error will be produced)
  if mass-f <= 0 [
    set m-preg 0
    stop
  ]
*/
		if (massF <= 0.0d) {
			mPreg = 0.0d;
			return;
		}
/*
  ;;;;; COST CALCULATION ;;;;;
  let max-grow-f (3 * (f-growth-c) ^ 3 ) * (( mass-f ^ (1 / 3) / f-growth-c) ^ 2)/ 48                                    ; EQN 41: Maximum fetal growth calculation ; / 48 for converting to time step
  set m-growth-f ((max-grow-f * percent-lip-f * ED-lip) / DE-lip) + ((max-grow-f * percent-pro-f * ED-pro) / DE-pro)     ; EQN 42: Calculate fetal tissue investment costs
  set e-heat-gest ((4400 * (max-mass-f ^ 1.2)) * 4184) / 14400                                                           ; EQN 43: Calculate heat of gestation in J per timestep
  set m-preg e-heat-gest +  m-growth-f                                                                                   ; EQN 40: Costs of pregnancy as the cost heat of gestation and fetal tissue investment
*/
		double maxGrowF = (3.0d * Math.pow((Globals.fGrowthC), 3.0d) ) * (Math.pow(( Math.pow(massF, (1.0d / 3.0d)) / Globals.fGrowthC), 2.0d))/ 48.0d;
		mGrowthF = ((maxGrowF * Globals.percentLipF * Globals.EDLip) / DELip) + ((maxGrowF * Globals.percentProF * Globals.EDPro) / DEPro);
		eHeatGest = ((4400.0d * (Math.pow(Globals.maxMassF, 1.2d))) * 4184.0d) / 14400.0d;
		mPreg = eHeatGest + mGrowthF;
/*
  ;;;;; ALLOCATION ;;;;;
  ifelse e-assim >= m-preg                                                                                               ; Check if enough energy is available to cover pregnancy costs
    [ set mass-f mass-f + max-grow-f                                                                                     ; If yes, then fetus grows maximally
      set e-assim e-assim - m-preg ]                                                                                     ; Deplete available energy by pregnancy costs
    [ set e-storage e-storage + e-assim                                                                                  ; If e-assim is not sufficient to cover pregnancy costs then add e-assim to storage and
      ifelse e-storage >= m-preg + e-repo-min                                                                            ; Check if updated storage can cover pregnancy costs and if it is more than necessary to continue reproducing
       [ set e-storage e-storage - m-preg                                                                                ; If so, mobilize stored energy to do so
         set mass-f mass-f + max-grow-f                                                                                  ; Fetus grows maximally
         set v-blub v-blub - (((m-preg - e-assim) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                     ; Reduce the amount of blubber volume by the pregnancy loss
         set e-assim 0 ]                                                                                                 ; Set e-assim to zero
    [ if ( debug = 9 ) [print word who " aborted calf"]                                                                  ; If not, then abort calf
         set pregnancy-status 0                                                                                          ; Set pregnancy status to 0 as too low energy to conceive
         set m-preg 0                                                                                                    ; Reset pregnancy costs to 0
         set mass-f 0 ] ]                                                                                                ; Reset fetal mass to default (0)
end */
		if (eAssim >= mPreg) {
			massF = massF + maxGrowF;
			eAssim = eAssim - mPreg;
		} else {
			eStorage = eStorage + eAssim;
			if (eStorage >= mPreg + eRepoMin) {
				eStorage = eStorage - mPreg;
				massF = massF + maxGrowF;
				vBlub = vBlub - (((mPreg - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
				eAssim = 0.0d;
			} else {
				if (DebugLog.isEnabledFor(9)) {
					DebugLog.print(porp.getId() + " aborted calf");
				}

				pregnancyStatus = 0;
				mPreg = 0.0d;
				massF = 0.0d;
			}
		}
	}

	/*
	 * STATUS: DevComplete but check against original updPregnancyStatus (PSM etc)
	 */
	private void giveBirth() {
/*
to give-birth
    set mass-calf mass-f                                               ; Set calf mass to the final fetal mass
    set mass-f 0                                                       ; Reset fetal mass
    set v-blub-calf (mass-calf * 0.375) / dens-blub                    ; Set the blubber volume of the calf to 37.5 % of mass (as in McLellan et al. 2002)
    set mass-struct-calf mass-calf * (1 - 0.375)                       ; Take the structural mass as the difference
    set pregnancy-status 2                                             ; It is ready to mate even though it has a very young calf
    set m-preg 0                                                       ; Reset pregnancy variables
    set e-heat-gest 0
    set m-growth-f 0
    set IR-record-calf 0
    set with-lact-calf? true
    let n random 2                                                     ; pick a random number to assign calf sex
    ifelse n = 1 [set sex-calf "female"] [set sex-calf "male"]
    set ds-mating -99                                                  ; reset days since mating
    set dsg-birth 0                                                    ; set days since giving birth as 0
    ask porps with [preg-chance > 0] [set preg-chance 0]
    if (debug = 9 ) [ print word who " with lact calf" ]
end
 */
		massCalf = massF;
		massF = 0;
		vBlubCalf = (massCalf * 0.375d) / Globals.densBlub;
		massStructCalf = massCalf * (1.0d - 0.375d);
		pregnancyStatus = 2;
		mPreg = 0.0d;
		eHeatGest = 0.0d;
		mGrowthF = 0.0d;
		IRRecordCalf = 0.0d;
		withLactCalf = true;
		
		double calfPsmPrefDistance;
		if (this.porp.getDispersalBehaviour().calfInheritsPsmDist()) {
			calfPsmPrefDistance = this.porp.getPersistentSpatialMemory().getPreferredDistance();
		} else {
			calfPsmPrefDistance = PersistentSpatialMemory.generatedPreferredDistance();
		}
		this.calfPsm = new PersistentSpatialMemory(Globals.getWorldWidth(), Globals.getWorldHeight(),
				calfPsmPrefDistance);

		if (Globals.getRandomSource().nextPregnancyStatusBoyGirl(0, 1) > 0.5) { 
			sexCalf = "female";
		} else {
			sexCalf = "male";
		}
		dsMating = -99;
		dsgBirth = 0;
		pregChance = 0.0d;
		
		if (DebugLog.isEnabledFor(9)) {
			DebugLog.print9("{} with lact calf", this.porp.getId());
		}
	}
	
	/**
	 * STATUS: DevComplete
	 */
	private void lactation() {
		/*
to lactation
  ; Check if calf death occurred
  if mass-calf <= 0 [
    reset-lactation-vars
    stop
    ]
*/
		if (massCalf <= 0.0d) {
			resetLactationVars();
			return;
		}
/*
  ;;;;; COST CALCULATION ;;;;;
  ;;;; calf maintenance - EQN 44 ;;;;
  set m-BMR-calf (B0 * (mass-calf ^ 0.75)) * 1800
  */
		this.mBMRCalf = (B0 * (Math.pow(massCalf, 0.75d))) * 1800.0d;
  /*
  ; length calculations - EQN 45
  ifelse sex-calf = "female"
  [ let lgth-inf-f 116.3
    let lgth-0-f 89.2
    let lgth-k-f 3.3
    set lgth-calf ((lgth-inf-f * exp (ln(lgth-0-f / lgth-inf-f)* exp ((-1 * lgth-k-f)* (dsg-birth / 360))))/ 100)]                                                  ; Calculate length based on age for females; based on relationship from IDW dataset
  [ let lgth-inf-m 112.9
    let lgth-0-m 68.1
    let lgth-k-m 6.2
    set lgth-calf ((lgth-inf-m * exp (ln(lgth-0-m / lgth-inf-m)* exp ((-1 * lgth-k-m)* (dsg-birth / 360))))/ 100)]                                                  ; Calculate length based on age for males; based on relationship from IDW dataset
*/
		
		if ("female".equals(sexCalf)) {
			final double lgthInfF = 116.3d;
			final double lgth0F = 89.2d;
			final double lgthKF = 3.3d;
			lgthCalf = ((lgthInfF * Math.exp(Math.log(lgth0F / lgthInfF) * Math.exp((-1.0d * lgthKF) * (dsgBirth / 360.0d)) )) / 100.0d);
		} else {
			final double lgthInfM = 112.9d;
			final double lgth0M = 68.1d;
			final double lgthKM = 6.2d;
			lgthCalf = ((lgthInfM * Math.exp(Math.log(lgth0M / lgthInfM) * Math.exp ((-1.0d * lgthKM) * (dsgBirth / 360.0d))))/ 100.0d);
		}
/*
  ;;;; calf thermoregulatory costs - EQN 49 ;;;;

  ; Here pull value of e-thermo-calf (the initial calculation of heat balance; positive means heat loss, negative more heat is produced than needed) from the lookup table (ThermoregulationLookupTable.csv)
  ; Costs should be based on water temperature,	swim speed,	mass of the calf (mass-calf),	and storage level of the calf (storage-level-calf)
*/
		final double waterTemp = Globals.getCellData().getTemperature(porp.getPosition());
		double eThermoCalf = ThermoregulationLookupTable.getInstance().getValueInBin(waterTemp, swimSpeed, (int)weight, storageLevel);  // FIXME Values to use here!!!
/*
  ifelse e-therm-calf >= 0                                                                                                                                          ; If not, double check that the estimated metabolic cost of thermoregulation is greater than zero (positive clamp)
    [ set m-thermo-calf e-therm-calf ]                                                                                                                              ; If it is, set thermo costs to the estimated metabolic costs
    [ set m-thermo-calf 0 ]                                                                                                                                         ; If not, set thermo to 0
*/
		if (eThermoCalf >= 0) {
			mThermoCalf = eThermoCalf;
		} else {
			mThermoCalf = 0.0d;
		}
/*
  ;;;; calf growth costs - EQN 50 ;;;;
  let m-str-inf-c random-normal 15.43 0.59                                                                                                                          ; Von Bertalanffy fit from Galatius & Kinze, unps. dataset for calves < 0.9
  let m-str-k-c random-normal 20.95 4.07
  ifelse mass-struct-calf < m-str-inf-c
  [ set max-grow-calf (m-str-k-c / 17280) * ((m-str-inf-c ^ (1 / 3) * mass-struct-calf ^ (2 / 3)) - mass-struct-calf) ] [set max-grow-calf 0 ]                      ; Calculate max growth for calves
    set m-growth-calf (max-grow-calf * (ED-lean-mass + ED-lean-mass * (1 - DE-lean-mass)))                                                                          ; Calculate energy required for growth
*/
		double mStrInfC = RandomHelper.createNormal(15.43d, 0.59d).nextDouble();
		double mStrKC = RandomHelper.createNormal(20.95d, 4.07d).nextDouble();
		if (massStructCalf < mStrInfC) {
			maxGrowCalf = (mStrKC / 17280.0d) * ((Math.pow(mStrInfC, (1.0d / 3.0d)) * Math.pow(massStructCalf, (2.0d / 3.0d)) ) - massStructCalf);
		} else {
			maxGrowCalf = 0.0d;
		}
		mGrowthCalf = (maxGrowCalf * (EDLeanMass + EDLeanMass * (1.0d - DELeanMass)));  // FIXME Indentation is a bit special here, check if correct in original
/*
  ;;;; calf blubber requirement costs - EQN 51 ;;;;
  ifelse v-blub-calf <  v-blub-calf-idl                                                                                                                             ; Check that calves blubber stores are sufficient
  [ set m-blub-calf (((v-blub-calf-idl - v-blub-calf) * dens-blub * perc-lip-blub * ED-lip) / DE-lip)]                                                              ; If blubber stores are not at ideal levels, calculate costs of bringing stores to that level
  [ set m-blub-calf 0 ]                                                                                                                                             ; If at sufficient levels, cost of blubber maintenance = 0
*/
		if (vBlubCalf < vBlubCalfIdl) {
			mBlubCalf = (((vBlubCalfIdl - vBlubCalf) * Globals.densBlub * percLipBlub * Globals.EDLip) / DELip);
		} else {
			mBlubCalf = 0.0d;
		}
/*
  ;;;; total calf costs - EQN 52 ;;;;
  set e-calf (m-BMR-calf + m-thermo-calf + m-growth-calf + m-blub-calf) / lact-eff                                                                                  ; calculate total calf costs
  if e-calf >= 337080.2 [                                                                                                                                           ; check if costs are over what mom can produce via milk in a timestep (as calculated from max values in Oftedal 1997)
    let m ((m-BMR-calf + m-thermo-calf + m-growth-calf) / lact-eff)                                                                                                 ; temp variable containing other costs
    set m-blub-calf (337080.2 - m) * lact-eff                                                                                                                       ; reduce m-blub-calf by other costs
    if m-blub-calf < 0 [set m-blub-calf 0]
  ]
  set e-calf (m-BMR-calf + m-thermo-calf + m-growth-calf + m-blub-calf) / lact-eff                                                                                  ; Recalculate total calf costs, should be less than or equal to 337080.2
*/
		eCalf = (mBMRCalf + mThermoCalf + mGrowthCalf + mBlubCalf) / Globals.lactEff;
		if (eCalf >= 337080.2d) {
			double m = ((mBMRCalf + mThermoCalf + mGrowthCalf) / Globals.lactEff);
			mBlubCalf = (337080.2d - m) * Globals.lactEff;
			if (mBlubCalf < 0.0d) {
				mBlubCalf = 0.0d;
			}
		}
		eCalf = (mBMRCalf + mThermoCalf + mGrowthCalf + mBlubCalf) / Globals.lactEff;  // FIXME Why is this calculated again here?
/*
  ; weaning scale factor
  if round dsg-birth <= (floor (0.375 * t-nurs))                                                                                                                    ; If calf is less than 3 months old, it is considered totally dependant ; MAYBE CHANGE THIS TO BE MASS DEPENDANT
  [ set m-lact e-calf ]                                                                                                                                             ; Calculate lactation costs for total coverage
*/
		if (Math.round(dsgBirth) <= Math.floor(0.375d * Globals.tNurs)) {
			mLact = eCalf;
		}
/*
  if (round dsg-birth > (floor (0.375 * t-nurs))) and (round dsg-birth <= t-nurs)                                                                                   ; If calf is less between 3 and 8 months old, it becomes less dependant with age
  [ let adj-date dsg-birth - (floor (0.375 * t-nurs))                                                                                                               ; Normalize scale
    set wean-scale-fact 1.00 - (adj-date / (0.625 * t-nurs))                                                                                                        ; Calculate the weaning scale factor for calves
    if wean-scale-fact < 0 [set wean-scale-fact 0 ]                                                                                                                 ; If for some reason the calf were to go over 8 mo set to zero
    set m-lact e-calf * wean-scale-fact ]                                                                                                                           ; Calculate lactation costs while considering weaning
*/
		if (Math.round(dsgBirth) > Math.floor(0.375d * Globals.tNurs) && Math.round(dsgBirth) <= Globals.tNurs) {
			double adjDate = dsgBirth - (Math.floor(0.375d * Globals.tNurs));
			weanScaleFact = 1.00d - (adjDate / (0.625d * Globals.tNurs));
			if (weanScaleFact < 0) {
				weanScaleFact = 0.0d;
			}
			mLact = eCalf * weanScaleFact;
		}
/*
  if round dsg-birth = t-nurs [ wean-calf ]                                                                                                                         ; If older than 8 months, fully wean calf
*/
		if (Math.round(dsgBirth) == Globals.tNurs) {
			weanCalf();
		}
/*
  ifelse wean-scale-fact < 1                                                                                                                                        ; Check if calf is being weaned
  [
  set vital-costs ((m-BMR-calf * wean-scale-fact) + (m-thermo-calf * wean-scale-fact))/ lact-eff                                                                    ; If so, set vital costs to maintenance and thermo costs offset by weaning scale factor
  set vital-costs-calf ((m-BMR-calf * (1.00 - wean-scale-fact)) + (m-thermo-calf * (1.00 - wean-scale-fact)))                                                       ; Set the calf's portion of vital costs to the remainder
  ]
  [
  set vital-costs (m-BMR-calf + m-thermo-calf)/ lact-eff                                                                                                            ; If not, set vital costs to maintenance and thermo costs
  ]

 if vital-costs > 337080.2 [                                                                                                                                        ; Check if vital costs are over what mom can produce via milk in a timestep (as calculated from maximum values in Oftedal 1997)
    reset-lactation-vars
    stop
  ]
*/
		if (weanScaleFact < 1) {
			vitalCosts = ((mBMRCalf * weanScaleFact) + (mThermoCalf * weanScaleFact))/ Globals.lactEff;
			vitalCostsCalf = ((mBMRCalf * (1.00 - weanScaleFact)) + (mThermoCalf * (1.00 - weanScaleFact)));
		} else {
			vitalCosts = (mBMRCalf + mThermoCalf)/ Globals.lactEff;
		}
		
		if (vitalCosts > 337080.2d) {
			resetLactationVars();
			return;
		}
/*
  ;;;;; ALLOCATION ;;;;;
  ifelse e-assim >= m-lact                                                                                                              ; Check if enough energy is available to cover lactation costs

  [ set e-assim e-assim - m-lact                                                                                                        ; If so, deplete available energy to cover lactation costs
    set mass-struct-calf mass-struct-calf + (max-grow-calf * wean-scale-fact)                                                           ; Calf grows maximally
    set v-blub-calf v-blub-calf + (((m-blub-calf * wean-scale-fact)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))
    set m-lact-real m-lact                                                                                                              ; All costs covered so realized cost = total lact cost
    ]
*/
		if (eAssim >= mLact) {
			eAssim = eAssim - mLact;
			massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFact);
			vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFact)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
			mLactReal = mLact;
		} else {
/*
  [ ifelse e-assim >= vital-costs                                                                                                       ; If not enough to cover all lactation costs, check if vital costs can be covered
*/
			if (eAssim >= vitalCosts) {
/*
    [ ifelse v-blub >= v-blub-mean                                                                                                      ; If yes, first check if blubber volume is over mean values/animal is in good body condition
*/
				if (vBlub >= vBlubMean) {
/*          [ set e-storage e-storage + e-assim                                                                                           ; If yes, then add e-assim to storage
            set e-storage e-storage - m-lact                                                                                            ; And then cover all costs using updated storage
            set v-blub v-blub - (((m-lact - e-assim) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                                 ; Reduce the amount of blubber volume by the lactation loss
            set mass-struct-calf mass-struct-calf + (max-grow-calf * wean-scale-fact)
            set v-blub-calf v-blub-calf + (((m-blub-calf * wean-scale-fact)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))
            set m-lact-real m-lact                                                                                                      ; All costs covered so realized cost = total lact cost
            set e-assim 0                                                                                                               ; Set e-assim to zero
      ]
*/
					eStorage = eStorage + eAssim;
					eStorage = eStorage - mLact;
					vBlub = vBlub - (((mLact - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFact);
					vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFact)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					mLactReal = mLact;
					eAssim = 0.0d;
				} else {
/*
          [ set m-lact-real e-assim                                                                                                     ; Only using e-assim to cover costs so realized cost = e-assim
            set e-assim e-assim - vital-costs                                                                                           ; If storage isn't over mean levels then just cover vital lactation costs using e-assim and split the remaining e-assim to growth and blubber
            ifelse (e-assim / 2) >= (m-growth-calf * wean-scale-fact)                                                                   ; If half of remaining e-assim is more than enough to satisfy growth costs
            [ set e-assim-grow-calf (m-growth-calf * wean-scale-fact) ]                                                                 ; Then set e-assim for growth to the costs of growth and the remainder goes to calf storage
            [ set e-assim-grow-calf e-assim / 2 ]                                                                                       ; If not then e-assim is split equally between growth and storage
            let e-assim-blub-calf e-assim - e-assim-grow-calf                                                                           ; e-assim for blubber is set to the remainder of e-assim
            set e-assim 0                                                                                                               ; Set e-assim to zero
            let growth-rate-calf (e-assim-grow-calf / ED-lean-mass) * DE-lean-mass                                                      ; Else, reduce growth rate and grow suboptimally; use only available energy for growth
            set mass-struct-calf mass-struct-calf +  growth-rate-calf
            set blub-calf ((e-assim-blub-calf / (ED-lip * perc-lip-blub)) * DE-lip)/ dens-blub                                          ; Calculate the blubber volume covered by available energy for blubber
            if e-assim-blub-calf >= (m-blub-calf * wean-scale-fact)                                                                     ; Check if added blubber would put the calf over ideal value
            [
              let blub-calf-adj (((e-assim-blub-calf - (m-blub-calf * wean-scale-fact))/ (ED-lip * perc-lip-blub)) * DE-lip)/ dens-blub ; If so, calculate how much over
              let blub-calf-dif blub-calf - blub-calf-adj                                                                               ; Calculate difference between initial blub-calf and adjusted blub-calf
              set e-assim (blub-calf-dif * dens-blub * perc-lip-blub * ED-lip)                                                          ; And add that energy back to e-assim
              set blub-calf blub-calf-adj                                                                                               ; Set blub-calf to adjusted value
            ]
            set v-blub-calf v-blub-calf + blub-calf ]]                                                                                  ; Update calf blubber volume
*/
					mLactReal = eAssim;
					eAssim = eAssim - vitalCosts;
					if ((eAssim / 2.0d) >= (mGrowthCalf * weanScaleFact)) {
						eAssimGrowCalf = (mGrowthCalf * weanScaleFact);
					} else {
						eAssimGrowCalf = eAssim / 2.0d;
					}
					double eAssimBlubCalf = eAssim - eAssimGrowCalf;
					eAssim = 0.0d;
					double growthRateCalf = (eAssimGrowCalf / EDLeanMass) * DELeanMass;
					massStructCalf = massStructCalf + growthRateCalf;
					blubCalf = ((eAssimBlubCalf / (Globals.EDLip * percLipBlub)) * DELip)/ Globals.densBlub;
					if (eAssimBlubCalf >= (mBlubCalf * weanScaleFact)) {
						double blubCalfAdj = (((eAssimBlubCalf - (mBlubCalf * weanScaleFact))/ (Globals.EDLip * percLipBlub)) * DELip)/ Globals.densBlub;
						double blubCalfDif = blubCalf - blubCalfAdj;
						eAssim = (blubCalfDif * Globals.densBlub * percLipBlub * Globals.EDLip);
						blubCalf = blubCalfAdj;
					}
					vBlubCalf = vBlubCalf + blubCalf;
				}
			} else {
/*
      [ ifelse v-blub >= v-blub-mean                                                                                                    ; If yes, first check if blubber volume is over mean values/animal is in good body condition
        [ set e-storage e-storage + e-assim                                                                                             ; If yes, then add e-assim to storage
          set e-storage e-storage - m-lact                                                                                              ; And then cover all costs using updated storage
          set v-blub v-blub - (((m-lact - e-assim) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                                   ; Reduce the amount of blubber volume by the lactation loss
          set mass-struct-calf mass-struct-calf + (max-grow-calf * wean-scale-fact)
          set v-blub-calf v-blub-calf + (((m-blub-calf * wean-scale-fact)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))
          set m-lact-real m-lact                                                                                                        ; All costs covered so realized cost = total lact cost
          set e-assim 0                                                                                                                 ; Set e-assim to zero
        ]

     [ ifelse v-blub >= v-blub-repro                                                                                                    ; If yes, first check if blubber volume is over mean values/animal is over reproductive threshold
        [ set e-storage e-storage + e-assim                                                                                             ; If e-assim is not sufficient to cover non-growth related lactation costs then add e-assim to storage and
          set e-storage e-storage - vital-costs                                                                                         ; And then cover vital costs using updated storage
          set v-blub v-blub - (((vital-costs - e-assim) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                              ; Reduce the amount of blubber volume by the vital lactation loss
          set m-lact-real vital-costs                                                                                                   ; Only vital costs covered so realized cost = vital costs
          set e-assim 0                                                                                                                 ; Set e-assim to zero
        ]
        [ set m-lact-real 0                                                                                                             ; No costs covered so realized cost = 0
          set v-blub-calf v-blub-calf - ((vital-costs * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                                ; Have the calf use blubber to cover vital costs
          ]
  ]]]
*/
				if (vBlub >= vBlubMean) {
					eStorage = eStorage + eAssim;
					eStorage = eStorage - mLact;
					vBlub = vBlub - (((mLact - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFact);
					vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFact)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					mLactReal = mLact;
					eAssim = 0.0d;
				} else {
					if (vBlub >= vBlubRepro) {
						eStorage = eStorage + eAssim;
						eStorage = eStorage - vitalCosts;
						vBlub = vBlub - (((vitalCosts - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
						mLactReal = vitalCosts;
						eAssim = 0.0d;
					} else {
						mLactReal = 0.0d;
						vBlubCalf = vBlubCalf - ((vitalCosts * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					}
				}
			}		
		}

/*
  if wean-scale-fact < 1 [calf-feed]                                                                                                    ; If calf is being weaned trigger calf feeding procedure
end		 */
		if (weanScaleFact < 1) {
			calfFeed();
		}
	}

	/*
	 * STATUS: DevComplete
	 */
	private void calfFeed() {
/*
to calf-feed
  ; Calves over 3 mo also ingest food in patches to meet their metabolic needs
  let wean-scale-fact-calf (1.00 - wean-scale-fact) ; calf's portion of calf costs to cover
*/
		double weanScaleFactCalf = (1.00d - weanScaleFact);
/*
  ; Calf feeds and covers costs
  ifelse e-assim-calf >= (e-calf * wean-scale-fact-calf)                                                                                 ; Check if enough energy is available to cover total costs
  [ if (debug = 11) [if (who = 0) [ print word who "'s calf sufficiently fed - calf-feed" ]]
    set e-assim-calf e-assim-calf - (e-calf * wean-scale-fact-calf)                                                                      ; If so, deplete available energy to cover calf's share of costs
    set mass-struct-calf mass-struct-calf + (max-grow-calf * wean-scale-fact-calf)                                                       ; Calf grows maximally
    set v-blub-calf v-blub-calf + (((m-blub-calf * wean-scale-fact-calf)* DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))               ; Call adds storage energy to blubber
    if e-assim-calf > 0
    [ ifelse (v-blub-calf + ((e-assim-calf * DE-lip ) * DE-lip * perc-lip-blub) / (ED-lip * dens-blub )) < (mass-calf * 0.436 / dens-blub) ; If extra energy available check if blubber volume is not at max (Mean + 1 SD percent blubber for calves in McLellan et al. 2002)
        [
        set v-blub-calf v-blub-calf + ((e-assim-calf * DE-lip * perc-lip-blub) / (ED-lip * dens-blub ))                                  ; If not, add remaining energy to blubber volume
        set e-assim-calf 0
        ]
        [
        set e-assim-calf e-assim-calf - ((((mass-calf * 0.436 / dens-blub) - v-blub-calf)/ DE-lip ) * dens-blub * ED-lip)                ; If yes, set to max blubber volume
        set v-blub-calf (mass-calf * 0.436 / dens-blub)
        ]
    ]]
*/
		if (eAssimCalf >= (eCalf * weanScaleFactCalf)) {
			// Omitted debug if (debug = 11) [if (who = 0) [ print word who "'s calf sufficiently fed - calf-feed" ]]
			eAssimCalf = eAssimCalf - (eCalf * weanScaleFactCalf);
			massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFactCalf);
			vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFactCalf)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
			if (eAssimCalf > 0.0d) {
				if ((vBlubCalf + ((eAssimCalf * DELip ) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub )) < (massCalf * 0.436d / Globals.densBlub)) {
					vBlubCalf = vBlubCalf + ((eAssimCalf * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					eAssimCalf = 0.0d;
				} else {
					eAssimCalf = eAssimCalf - ((((massCalf * 0.436d / Globals.densBlub) - vBlubCalf)/ DELip ) * Globals.densBlub * Globals.EDLip);
					vBlubCalf = (massCalf * 0.436d / Globals.densBlub);
				}
			}
		} else {
/*
  [ ifelse e-assim-calf >= vital-costs-calf                                                                       ; If assimilated energy isn't enough to cover all of calf's portion of lactation costs, check if vital costs can be covered
*/
			if (eAssimCalf >= vitalCostsCalf) {
/*
    [ if (debug = 11) [if (who = 0) [  print word who "'s calf fed enough to cover calf portion of vital costs - calf-feed" ]]
      set e-assim-calf e-assim-calf - vital-costs-calf                                                            ; If storage isn't over mean levels then just cover vital lactation costs using e-assim-calf and split the remaining e-assim-calf to growth and blubber
      ifelse (e-assim-calf / 2) >= (m-growth-calf * wean-scale-fact-calf)                                         ; If half of remaining e-assim of the calf is more than enough to satisfy growth costs
      [ set e-assim-grow-calf (m-growth-calf * wean-scale-fact-calf) ]                                            ; Then set e-assim-calf for growth to the costs of growth and the remainder goes to calf storage
      [ set e-assim-grow-calf e-assim-calf / 2 ]                                                                  ; If not then e-assim-calf is split equally between growth and storage
            let e-assim-blub-calf e-assim-calf - e-assim-grow-calf                                                ; e-assim-calf for blubber is set to the remainder of e-assim-calf
            set e-assim-calf 0                                                                                    ; Set e-assim-calf to zero
            let growth-rate-calf (e-assim-grow-calf / ED-lean-mass) * DE-lean-mass                                ; Use available energy for growth
            set mass-struct-calf mass-struct-calf + growth-rate-calf
            set blub-calf ((e-assim-blub-calf / (ED-lip * perc-lip-blub) * DE-lip) / dens-blub)                   ; Calculate the blubber volume covered by available energy for blubber
            set v-blub-calf v-blub-calf + blub-calf                                                               ; Update calf blubber volume

    ]
*/
				// Omitted debug if (debug = 11) [if (who = 0) [  print word who "'s calf fed enough to cover calf portion of vital costs - calf-feed" ]]
				eAssimCalf = eAssimCalf - vitalCostsCalf;
				if ((eAssimCalf / 2.0d) >= (mGrowthCalf * weanScaleFactCalf)) {
					eAssimGrowCalf = (mGrowthCalf * weanScaleFactCalf);
				} else {
					eAssimGrowCalf = eAssimCalf / 2.0d;
				}
				double eAssimBlubCalf = eAssimCalf - eAssimGrowCalf;
				eAssimCalf = 0.0d;
				double growthRateCalf = (eAssimGrowCalf / EDLeanMass) * DELeanMass;
				massStructCalf = massStructCalf + growthRateCalf;
				blubCalf = ((eAssimBlubCalf / (Globals.EDLip * percLipBlub) * DELip) / Globals.densBlub);
				vBlubCalf = vBlubCalf + blubCalf;
			} else {
/*
    [ ifelse v-blub-calf >= (mass-calf * 0.313 / dens-blub)                                                                                            ; If e-assim-calf < vital-costs-calf check if calf is over mean percent blubber - 1 SD for calves in McLellan et al. 2002
          [ if (debug = 11) [if (who = 0) [ print word who "'s calf used blubber to cover costs - calf-feed" ]]
            let total-costs-calf e-calf - m-lact                                                                                                       ; Create a temp variable for the total calf costs
            let diff-calf-costs total-costs-calf - e-assim-calf                                                                                        ; Calculate the difference between this and the e-assim of the calf
            set v-blub-calf v-blub-calf - (((diff-calf-costs - (m-blub-calf * wean-scale-fact-calf))* DE-lip * perc-lip-blub) / (ED-lip * dens-blub )) ; Deplete blubber by difference
            set e-assim-calf 0                                                                                                                         ; Set e-assim-calf to zero
            set mass-struct-calf mass-struct-calf + (max-grow-calf * wean-scale-fact-calf)                                                             ; Calf grows maximally
           ]

          [                                                                                                     ; If e-assim-calf < vital-costs-calf, blubber volume is below mean, and energy is still needed pull energy for vital costs only from blubber
            if (debug = 11) [if (who = 0) [ print word who "'s calf used blubber to cover only vital costs - calf-feed" ]]
            let diff-calf-costs vital-costs-calf - e-assim-calf                                                 ; Find difference between vital costs and e-assim calf
            set v-blub-calf v-blub-calf - ((diff-calf-costs * DE-lip * perc-lip-blub) / (ED-lip * dens-blub))   ; Mobilize blubber to cover difference
            set e-assim-calf 0                                                                                  ; Set e-assim-calf to zero
           ]
      ]]
*/
				if (vBlubCalf >= (massCalf * 0.313d / Globals.densBlub)) {
					// Omitted debug if (debug = 11) [if (who = 0) [ print word who "'s calf used blubber to cover costs - calf-feed" ]]
					double totalCostsCalf = eCalf - mLact;
					double diffCalfCosts = totalCostsCalf - eAssimCalf;
					vBlubCalf = vBlubCalf - (((diffCalfCosts - (mBlubCalf * weanScaleFactCalf))* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
					eAssimCalf = 0.0d;
					massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFactCalf);
				} else {
					// Omitted debug if (debug = 11) [if (who = 0) [ print word who "'s calf used blubber to cover only vital costs - calf-feed" ]]
					double diffCalfCosts = vitalCostsCalf - eAssimCalf;
					vBlubCalf = vBlubCalf - ((diffCalfCosts * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
					eAssimCalf = 0.0d;
				}
			}
		}
/*
  ; Calf should die if storage level falls below 0.05 - also has a probability of dying at higher storage levels in porp-upd-mortality
  let storage-level-calf 0
  if mass-calf > 0 [set storage-level-calf (v-blub-calf * dens-blub) / mass-calf ]

  if (storage-level-calf < 0.05) [
    reset-lactation-vars
  ]
end */
		double storageLevelCalf = 0.0d;
		if (massCalf > 0.0d) {
			storageLevelCalf = (vBlubCalf * Globals.densBlub) / massCalf;
		}
		
		if (storageLevelCalf < 0.05d) {
			resetLactationVars();
		}
	}

	/*
	 * STATUS: DevComplete, except fixmes
	 * FIXME ACTUALLY CALL THIS!!!!!!
	 */
	public void updMortality() {
/*
to porp-upd-mortality

  ; Prob of dying increaseUpdates with decreasing body condition
  let m-mort-prob-const 10 ^ (2.176e-02 * x-surv-prob-const + -3.875e-04)                     ; EQN 63: obtained from fitting log-transformed possible values with a linear model in R

  let yearly-surv-prob (1 - (m-mort-prob-const * exp(- storage-level * x-surv-prob-const) )) ; EQN 62: Yearly survival probability
  if yearly-surv-prob < 0 [set yearly-surv-prob 0]
*/
		double mMortProbConst = Math.pow(10, (2.176e-02 * Globals.xSurvProbConst + -3.875e-04));
		double yearlySurvProb = (1.0d - (mMortProbConst * Math.exp(- storageLevel * Globals.xSurvProbConst) ));
		if (yearlySurvProb < 0) {
			yearlySurvProb = 0.0d;
		}
/*
  let step-surv-prob 0
  ifelse (storage-level > 0.05 and yearly-surv-prob > 0 )                                    ; daily survival probability
  [ set step-surv-prob exp( ln(yearly-surv-prob) / (360) )]                                  ; EQN 64
  [ set step-surv-prob 0]
*/
		double stepSurvProb = 0.0d;
		if (storageLevel > 0.05d && yearlySurvProb > 0.0d) {
			stepSurvProb = Math.exp(Math.log(yearlySurvProb) / (360.0d));
		} else {
			stepSurvProb = 0.0d;
		}
/*
  if ( random-float 1 >= step-surv-prob ) [
    if (not with-lact-calf? or storage-level <= 0) [
      set list-of-dead-age lput (floor age) list-of-dead-age
      set list-of-dead-day lput (floor sim-day) list-of-dead-day
      if (debug = 10) [print word who " died of low body condition-Upd Mortality"]
      die
  ]]
*/
		final double ran = Globals.getRandomSource().nextMortality(0, 1);
		if (ran >= stepSurvProb) {
			if (!withLactCalf || storageLevel <= 0.0d) {
				Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
				if (DebugLog.isEnabledFor(10)) {
					DebugLog.print(porp.getId() + " died of low body condition-Upd Mortality");
				}
				porp.die(this.porp.getAge() > SimulationParameters.getMaxAge() ? CauseOfDeath.OldAge : CauseOfDeath.ByCatch); // FIXME Check which value to pass here!
			}
		}
/*

  ; check for calf death
  if (with-lact-calf? = TRUE) [
    let storage-level-calf ((mass-calf - mass-struct-calf) / mass-calf)
    let yearly-surv-prob-calf (1 - (m-mort-prob-const * exp(- storage-level-calf * x-surv-prob-const) ))
    if yearly-surv-prob-calf < 0 [set yearly-surv-prob-calf 0]
    let step-surv-prob-calf 0
    ifelse (storage-level-calf > 0.05 and yearly-surv-prob-calf > 0) [ set step-surv-prob-calf exp( ln(yearly-surv-prob-calf) / 360 )] [set step-surv-prob-calf 0] ; if calf's storage level is above minimum storage level (0.05) calculate survival probability for the timestep
    if ( random-float 1 >= step-surv-prob-calf ) [
      reset-lactation-vars
    ]
  ]

end
 */
		if (withLactCalf) {
			double storageLevelCalf = ((massCalf - massStructCalf) / massCalf);
			double yearlySurvProbCalf = (1.0d - (mMortProbConst * Math.exp(- storageLevelCalf * Globals.xSurvProbConst) ));  // FIXME Is this '-' at start ok? One other occurence earlier
			if (yearlySurvProbCalf < 0.0d) {
				yearlySurvProbCalf = 0.0d;
			}
			double stepSurvProbCalf = 0.0d;
			if (storageLevelCalf > 0.05d && yearlySurvProbCalf > 0.0d) {
				stepSurvProbCalf = Math.exp( Math.log(yearlySurvProbCalf) / 360.0d );
			} else {
				stepSurvProbCalf = 0.0d;
			}
			
			if (Globals.getRandomSource().nextMortality(0, 1) >= stepSurvProbCalf) {
				resetLactationVars();
			}
		}
	}
	
	/*
	 * STATUS: DevComplete, check fixme
	 */
	private void porpsSetupParams() {
/*
to porps-setup-params

 ; Submodel: MAINTENANCE
  set B0 11.13 + random-float 0.04 - random-float 0.04               ; Maintenance normalization constant - calibrated

  ; Submodel: THERMOREGULATION
  set for-convec-scal-coef-list []                                   ; List setup to hold cone forced convection scaling coefficient values
  set hC-low-lim-coef-list []                                        ; List setup to hold cone heat transfer lower limit values
  set T-core random-normal 36.7 0.4                                  ; Core temperature - Blanchet, Wahlberg, and Acquarone 2008
  set kB random-normal 0.10 0.01                                     ; Thermal conductivity of blood free blubber - Worthy and Edwards 1990

  ; Submodel: LOCOMOTION
   set lambda random-normal 0.25 0.016                               ; Ratio of active to passive drag - calibrated
		
  ; Submodel: REPRODUCTION
  set m-preg 0                                                       ; Initialize pregnancy and lactation costs
  set m-lact 0
  set preg-chance 0                                                  ; Initialize pregnancy chance as 0
*/
		B0 = 11.13 + (Globals.getRandomSource().nextDouble() * 0.04d) - (Globals.getRandomSource().nextDouble() * 0.04d);
		// FIXME Seems this is no longer in use?: for-convec-scal-coef-list
		// FIXME Seems this is no longer in use?: hC-low-lim-coef-list
		// FIXME Seems this is no longer in use?: T-core
		// FIXME Seems this is no longer in use?: kB
		
		lambda = RandomHelper.createNormal(0.25d, 0.016d).nextDouble();

		mPreg = 0.0d;
		mLact = 0.0d;
		pregChance = 0.0d;
/*
  ; Submodel: GROWTH
  set struct-mass-perc-pro random-normal 0.2629 0.0086               ; Percent structural mass that is protein - Lockyer 1991 (sperm whale)
  set struct-mass-perc-lip random-normal 0.0288 0.0114               ; Percent structural mass that is lipid - Lockyer 1991 (sperm whale)
  set DE-lip 0.74 + random-float 0.16                                ; Deposition efficiency of lipid - Malavear 2002, Pullar & Webster 1977
  set DE-pro 0.43 + random-float 0.13                                ; Deposition efficiency of protein - Malavear 2002, Pullar & Webster 1977
  set ED-lean-mass ((struct-mass-perc-pro * ED-pro ) + (struct-mass-perc-lip * ED-lip))                                                 ; EQN 55: Lean mass energy density
  set DE-lean-mass ((struct-mass-perc-pro * DE-pro ) + (struct-mass-perc-lip * DE-lip))/ (struct-mass-perc-pro + struct-mass-perc-lip)  ; EQN 56: Lean mass deposition efficiency
  set m-str-k random-normal 1.16 0.33                                ; Mass growth constant for females from Galatius & Kinze unps. data fitted with a VB curve
  set m-str-inf random-normal 46.68 3.86                             ; Mass asymptotic value for females from Galatius & Kinze unps. data fitted with a VB curve
  set lgth-inf random-normal 158.12 4.68                             ; Length asymptotic value for females from Galatius & Kinze unps. data fitted with a gompertz curve
  set lgth-0 random-normal 94.82 1.69                                ; Length initial value for females from Galatius & Kinze unps. data fitted with a gompertz curve
  set lgth-k random-normal 0.41 0.06                                 ; Length k value from Galatius & Kinze unps. data fitted with a gompertz curve

  ; Submodel: STORAGE
  set perc-lip-blub random-normal 0.816 0.036                        ; Percent blubber that is lipid - Worthy and Edwards 1990
*/
		structMassPercPro = RandomHelper.createNormal(0.2629d, 0.0086d).nextDouble();
		structMassPercLip = RandomHelper.createNormal(0.0288d, 0.0114d).nextDouble();
		DELip = 0.74d + (Globals.getRandomSource().nextDouble() * 0.16d);
		DEPro = 0.43d + (Globals.getRandomSource().nextDouble() * 0.13d);
		EDLeanMass = ((structMassPercPro * Globals.EDPro ) + (structMassPercLip * Globals.EDLip));
		DELeanMass = ((structMassPercPro * DEPro ) + (structMassPercLip * DELip)) / (structMassPercPro + structMassPercLip);
		mStrK = RandomHelper.createNormal(1.16d, 0.33d).nextDouble();
		mStrInf = RandomHelper.createNormal(46.68d, 3.86d).nextDouble();
		lgthInf = RandomHelper.createNormal(158.12d, 4.68d).nextDouble();
		lgth0 = RandomHelper.createNormal(94.82d, 1.69d).nextDouble();
		lgthK = RandomHelper.createNormal(0.41d, 0.06d).nextDouble();
		
		percLipBlub = RandomHelper.createNormal(0.816d, 0.036d).nextDouble();

/*
  ; initialize morphometrics based on age - For all groups: 0, 1, 2, 3, 4, 5, 6, 7 & up
  ifelse age = 0
    [ let porp-init-0 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Zero.csv"  ; Import csv
      let porp-init-matrix-0 matrix:from-row-list porp-init-0                                               ; Create matrix from csv
      let n-porps-0 matrix:get-column porp-init-matrix-0 0                                                  ; Pull the first column as a list to get max row number
      let n-0 (1 + (random (length n-porps-0 - 1)))                                                         ; Generate random row number
      set lgth matrix:get porp-init-matrix-0 n-0 1                                                          ; Pull length from csv
      set weight matrix:get porp-init-matrix-0 n-0 2                                                        ; Pull mass from csv
      set v-blub (matrix:get porp-init-matrix-0 n-0 3) / dens-blub                                          ; Set volume of blubber as the blubber mass multiplied by the blubber density in cm3
      set mass-struct (matrix:get porp-init-matrix-0 n-0 2) - (matrix:get porp-init-matrix-0 n-0 3)         ; Set structural mass as the difference between weight and blubber mass in kg
  ]
  [ ifelse age = 1                                                                                        ; 1 year old initilization
    [ let porp-init-1 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_One.csv"
      let porp-init-matrix-1 matrix:from-row-list porp-init-1
      let n-porps-1 matrix:get-column porp-init-matrix-1 0
      let n-1 (1 + (random (length n-porps-1 - 1)))
      set lgth matrix:get porp-init-matrix-1 n-1 1
      set weight matrix:get porp-init-matrix-1 n-1 2
      set v-blub (matrix:get porp-init-matrix-1 n-1 3) / dens-blub
      set mass-struct (matrix:get porp-init-matrix-1 n-1 2) - (matrix:get porp-init-matrix-1 n-1 3)
    ]

    [ ifelse age = 2
      [ let porp-init-2 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Two.csv"
        let porp-init-matrix-2 matrix:from-row-list porp-init-2
        let n-porps-2 matrix:get-column porp-init-matrix-2 0
        let n-2 (1 + (random (length n-porps-2 - 1)))
        set lgth matrix:get porp-init-matrix-2 n-2 1
        set weight matrix:get porp-init-matrix-2 n-2 2
        set v-blub (matrix:get porp-init-matrix-2 n-2 3) / dens-blub
        set mass-struct (matrix:get porp-init-matrix-2 n-2 2) - (matrix:get porp-init-matrix-2 n-2 3)
      ]

      [ ifelse age = 3
        [ let porp-init-3 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Three.csv"
          let porp-init-matrix-3 matrix:from-row-list porp-init-3
          let n-porps-3 matrix:get-column porp-init-matrix-3 0
          let n-3 (1 + (random (length n-porps-3 - 1)))
          set lgth matrix:get porp-init-matrix-3 n-3 1
          set weight matrix:get porp-init-matrix-3 n-3 2
          set v-blub (matrix:get porp-init-matrix-3 n-3 3) / dens-blub
          set mass-struct (matrix:get porp-init-matrix-3 n-3 2) - (matrix:get porp-init-matrix-3 n-3 3)
        ]

        [ ifelse age = 4
          [ let porp-init-4 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Four.csv"
            let porp-init-matrix-4 matrix:from-row-list porp-init-4
            let n-porps-4 matrix:get-column porp-init-matrix-4 0
            let n-4 (1 + (random (length n-porps-4 - 1)))
            set lgth matrix:get porp-init-matrix-4 n-4 1
            set weight matrix:get porp-init-matrix-4 n-4 2
            set v-blub (matrix:get porp-init-matrix-4 n-4 3) / dens-blub
            set mass-struct (matrix:get porp-init-matrix-4 n-4 2) - (matrix:get porp-init-matrix-4 n-4 3)
          ]

          [ ifelse age = 5
            [ let porp-init-5 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Five.csv"
              let porp-init-matrix-5 matrix:from-row-list porp-init-5
              let n-porps-5 matrix:get-column porp-init-matrix-5 0
              let n-5 (1 + (random (length n-porps-5 - 1)))
              set lgth matrix:get porp-init-matrix-5 n-5 1
              set weight matrix:get porp-init-matrix-5 n-5 2
              set v-blub (matrix:get porp-init-matrix-5 n-5 3) / dens-blub
              set mass-struct (matrix:get porp-init-matrix-5 n-5 2) - (matrix:get porp-init-matrix-5 n-5 3)
            ]

            [ ifelse age = 6
              [ let porp-init-6 csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Six.csv"
                let porp-init-matrix-6 matrix:from-row-list porp-init-6
                let n-porps-6 matrix:get-column porp-init-matrix-6 0
                let n-6 (1 + (random (length n-porps-6 - 1)))
                set lgth matrix:get porp-init-matrix-6 n-6 1
                set weight matrix:get porp-init-matrix-6 n-6 2
                set v-blub (matrix:get porp-init-matrix-6 n-6 3) / dens-blub
                set mass-struct (matrix:get porp-init-matrix-6 n-6 2) - (matrix:get porp-init-matrix-6 n-6 3)
              ]


              [ let porp-init-7u csv:from-file "porpoise-initialization-files/PorpoiseInitializationBlubber_Seven_up.csv"
                let porp-init-matrix-7u matrix:from-row-list porp-init-7u
                let n-porps-7u matrix:get-column porp-init-matrix-7u 0
                let n-7u (1 + (random (length n-porps-7u - 1)))
                set lgth matrix:get porp-init-matrix-7u n-7u 1
                set weight matrix:get porp-init-matrix-7u n-7u 2
                set v-blub (matrix:get porp-init-matrix-7u n-7u 3) / dens-blub
                set mass-struct (matrix:get porp-init-matrix-7u n-7u 2) - (matrix:get porp-init-matrix-7u n-7u 3)
              ]

  ]]]]]]
*/
		// if-else cases for age is handled in PorpoiseInitializationBlubber
		var initEntry = PorpoiseInitializationBlubber.getRandomEntry((int)this.porp.getAge()); // FIXME Comparing int to double, should there be some rounding or something?
		lgth = initEntry.stndL;
		weight = initEntry.mass;
		vBlub = initEntry.blubberMass / Globals.densBlub;
		massStruct = initEntry.mass - initEntry.blubberMass;

/*
  set age age + 0.5                                                                                                                  ; Increase age by 0.5 to account for Jan 1st start date
  ifelse pregnancy-status != 1 [set surface-area 0.093 * weight ^ 0.57] [set surface-area 0.093 * (weight + (2 * mass-f)) ^ 0.57]    ; Calculate surface area - Worthy and Edwards 1990
  set v-blub-calf 0                                                                                                                  ; Initialize calf blubber volume as 0
  set v-blub-min (weight * 0.05) / dens-blub                                                                                         ; Minimum blubber as 5% of weight
  set v-blub-Repro (weight * repro-min-SL / dens-blub)
  set e-repo-min (v-blub-Repro - v-blub-min) * dens-blub * ED-lip                                                                    ; Calculate energy required for reproductive threshold
  set SL-mean ((lgth * -0.3059) + 0.7066)* IR-temp-mod                                                                               ; Mean storage level percentages from McLellan et al. 2002 for mature, calf, and immature porpoises fit with a linear relationship to length and adjusted using temperature modifier
  set v-blub-mean (weight * SL-mean / dens-blub)                                                                                     ; Converted to blubber volume
  set v-blub-calf-idl (mass-calf * 0.375 / dens-blub)                                                                                ; Mean percent blubber for calves in McLellan et al. 2002
  set e-storage (v-blub - v-blub-min) * dens-blub * ED-lip                                                                           ; Storage energy as the amount of energy stored in blubber over minimum threshold
  set storage-level (weight - mass-struct) / weight                                                                                  ; Storage level calculation
*/
		// FIXME Set age!
		if (pregnancyStatus != 1) {
			surfaceArea = 0.093d * Math.pow(weight, 0.57d);
		} else {
			surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
		}
		vBlubCalf = 0.0d;
		vBlubMin = (weight * 0.05d) / Globals.densBlub;
		vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		vBlubMean = (weight * SLMean / Globals.densBlub);
		vBlubCalfIdl = (massCalf * 0.375d / Globals.densBlub);
		eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		storageLevel = (weight - massStruct) / weight;
/*
  ; initialize tracked values as zero
  set daily-food 0
  set food-intake-list []
  set storage-level-sum 0
  set storage-level-daily ( list 0 0 0 0 0 0 0 0 0 0 )
  set IR-record 0
  set IR-record-calf 0
end
 */
		dailyFood = 0.0d;
		foodIntakeList.clear();
		storageLevelSum = 0.0d;
		storageLevelDaily = new CircularBuffer<>(10);
		for (int i = 0; i < 10; i++) {
			this.storageLevelDaily.add(0.0d);
		}
		IRRecord = 0.0d;
		IRRecordCalf = 0.0d;
	}

	public double getEnergyLevelSum() {
		return storageLevelSum;
	}

	public CircularBuffer<Double> getEnergyLevelDaily() {
		return storageLevelDaily;
	}

	@Override
	public void updEnergeticStatus() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updPregnancyStatus() {
		this.porpUpdPregnancyStatus();
	}

	@Override
	public void consumeEnergy(double energyAmount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getFoodEaten() {
		return foodIntakeList.stream().mapToDouble(Double::doubleValue).sum();
	}

	@Override
	public double getEnergyConsumedDaily() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getEnergyLevel() {
		return storageLevel;
	}

	@Override
	public int getCalvesBorn() {
		return this.calvesBorn;
	}

	@Override
	public int getCalvesWeaned() {
		return this.calvesWeaned;
	}

	@Override
	public PersistentSpatialMemory getCalfPersistentSpatialMemory() {
		return calfPsm;
	}

	@Override
	public int getLactatingCalf() {
		if (this.withLactCalf) {
			return 1;
		} else {
			return 0;
		}
	}

	@Override
	public byte getPregnancyStatus() {
		return pregnancyStatus;
	}

	private int calvesBorn = 0; // FIXME UPDATE // Counter for number of calves born
	private int calvesWeaned = 0; // FIXME UPDATE // Counter for number of calves weaned (successfully to completion)

	private PersistentSpatialMemory calfPsm = null; // If the porpoise is with calf, then this is the PSM it will use.

}
