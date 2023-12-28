package dk.au.bios.porpoise.energetics;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.au.bios.porpoise.Agent;
import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.context.Context;

public class ReimplementationCheck {

	private final static int NUM_RECS = 25;
	private final static int SC_YEAR = 10;

	private final Check maintenanceCheck;
	private final Check locomotionCheck;
	private final Check pregnancyCostsCheck;
	private final Check pregnancyMassesCheck;
	private final Check lactationCostsCheck;
	private final Check lactationMassesCheck;
	private final Check growthCostsCheck;
	private final Check growthMassesCheck;
	private final Check storageLevelAgeCheck;
	private final Check storageLevelMonthCheck;
	private final Check intakeAgeCheck;
	private final Check intakeMonthCheck;
	private final Check intakeCalvesCheck;
	private final Check totalEnergyExpenditureCheck;

	public ReimplementationCheck() {
		maintenanceCheck = new Check("maintenance", (p, e) -> {
			if (e.weight <= 20.0d) return GroupingBin.BIN_1;
			else if (e.weight <= 40.0d) return GroupingBin.BIN_2;
			else if (e.weight <= 60.0d) return GroupingBin.BIN_3;
			else return GroupingBin.BIN_4;
		}, (p, e) -> {
			return e.mBMR;
		});

		locomotionCheck = new Check("locomotion", (p, e) -> {
			if (e.swimSpeed <= 0.5d) return GroupingBin.BIN_1;
			else if (e.swimSpeed <= 1.0d) return GroupingBin.BIN_2;
			else if (e.swimSpeed <= 1.5d) return GroupingBin.BIN_3;
			else return GroupingBin.BIN_4;
		}, (p, e) -> {
			return e.mLoco;
		});

		MapToBin pregnancyBinMapping = (p, e) -> {
			if (e.dsMating > 0 && e.dsMating <= 75) return GroupingBin.BIN_1;
			else if (e.dsMating > 75 && e.dsMating <= 150) return GroupingBin.BIN_2;
			else if (e.dsMating > 150 && e.dsMating <= 225) return GroupingBin.BIN_3;
			else if (e.dsMating >= 225) return GroupingBin.BIN_4;  // FIXME Overlap
			else return GroupingBin.BIN_IGNORE;
		};
		pregnancyCostsCheck = new Check("pregnancyCosts", pregnancyBinMapping, (p, e) -> {
			return e.mPreg;
		});
		pregnancyMassesCheck = new Check("pregnancyMasses", pregnancyBinMapping, (p, e) -> {
			return e.massF;
		});

		MapToBin lactationBinMapping = (p, e) -> {
			if (e.dsgBirth > 0 && e.dsgBirth <= 60) return GroupingBin.BIN_1;
			else if (e.dsgBirth > 60 && e.dsgBirth <= 120) return GroupingBin.BIN_2;
			else if (e.dsgBirth > 120 && e.dsgBirth <= 180) return GroupingBin.BIN_3;
			else if (e.dsgBirth >= 180) return GroupingBin.BIN_4;  // FIXME Overlap
			else return GroupingBin.BIN_IGNORE;
		};
		lactationCostsCheck = new Check("lactationCosts", lactationBinMapping, (p, e) -> {
			return e.mLact;
		});
		lactationMassesCheck = new Check("lactationMasses", lactationBinMapping, (p, e) -> {
			return e.massCalf;
		});

		MapToBin growthBinMapping = (p, e) -> {
			if (p.getAge() <= 3) return GroupingBin.BIN_1;
			else if (p.getAge() <= 6) return GroupingBin.BIN_2;
			else if (p.getAge() <= 9) return GroupingBin.BIN_3;
			else if (p.getAge() >= 9) return GroupingBin.BIN_4;  // FIXME Overlap
			else return GroupingBin.BIN_IGNORE;
		};
		growthCostsCheck = new Check("growthCosts", growthBinMapping, (p, e) -> {
			return e.mGrowth;
		});
		growthMassesCheck = new Check("growthMasses", growthBinMapping, (p, e) -> {
			return e.weight;
		});

		storageLevelAgeCheck = new Check("storageLevelAge", (p, e) -> {
			if (p.getAge() <= 1) return GroupingBin.BIN_1;
			else if (p.getAge() <= 2) return GroupingBin.BIN_2;
			else if (p.getAge() <= 3) return GroupingBin.BIN_3;
			else return GroupingBin.BIN_4;
		}, (p, e) -> {
			return e.storageLevel;
		});

		storageLevelMonthCheck = new Check("storageLevelMonth", (p, e) -> {
			if (SimulationTime.getMonthOfYear() == 3) return GroupingBin.BIN_1; // FIXME CHECK IF MONTH mapping is correct
			else if (SimulationTime.getMonthOfYear() == 6) return GroupingBin.BIN_2;
			else if (SimulationTime.getMonthOfYear() == 9) return GroupingBin.BIN_3;
			else if (SimulationTime.getMonthOfYear() == 12) return GroupingBin.BIN_4;
			else return GroupingBin.BIN_IGNORE;
		}, (p, e) -> {
			return e.storageLevel;
		});

		intakeAgeCheck = new Check("intakeAge", (p, e) -> {
			if (p.getAge() <= 3 && e.eAssim > 0) return GroupingBin.BIN_1;
			else if (p.getAge() > 3 && p.getAge() <= 6 && e.eAssim > 0) return GroupingBin.BIN_2;
			else if (p.getAge() > 6 && p.getAge() <= 9 && e.eAssim > 0) return GroupingBin.BIN_3;
			else if (p.getAge() >= 9 && e.eAssim > 0) return GroupingBin.BIN_4;
			else return GroupingBin.BIN_IGNORE;
		}, (p, e) -> {
			return e.eAssim;
		});

		intakeMonthCheck = new Check("intakeMonth", (p, e) -> {
			if (SimulationTime.getMonthOfYear() == 3 && e.eAssim > 0) return GroupingBin.BIN_1;
			else if (SimulationTime.getMonthOfYear() == 6 && e.eAssim > 0) return GroupingBin.BIN_2;
			else if (SimulationTime.getMonthOfYear() == 9 && e.eAssim > 0) return GroupingBin.BIN_3;
			else if (SimulationTime.getMonthOfYear() == 12 && e.eAssim > 0) return GroupingBin.BIN_4;
			else return GroupingBin.BIN_IGNORE;
		}, (p, e) -> {
			return e.eAssim;
		});

		intakeCalvesCheck = new Check("intakeCalves", (p, e) -> {
			if (e.dsgBirth >= 90 && e.dsgBirth <= 127 && e.eAssimCalf > 0) return GroupingBin.BIN_1;
			else if (e.dsgBirth > 127 && e.dsgBirth <= 164 && e.eAssimCalf > 0) return GroupingBin.BIN_2;
			else if (e.dsgBirth > 164 && e.dsgBirth <= 201 && e.eAssimCalf > 0) return GroupingBin.BIN_3;
			else if (e.dsgBirth >= 201 && e.eAssimCalf > 0) return GroupingBin.BIN_4;
			else return GroupingBin.BIN_IGNORE;
		}, (p, e) -> {
			return e.eAssimCalf;
		});

		totalEnergyExpenditureCheck = new Check("totalEnergyExpenditure", (p, e) -> {
			if (p.getAge() <= 3) return GroupingBin.BIN_1;
			else if (p.getAge() <= 6) return GroupingBin.BIN_2;
			else if (p.getAge() <= 9) return GroupingBin.BIN_3;
			else if (p.getAge() >= 9) return GroupingBin.BIN_4;
			else return GroupingBin.BIN_IGNORE;
		}, (p, e) -> {
			return e.mTot;
		});
	}

	private static ReimplementationCheck INSTANCE;
	
	public static ReimplementationCheck getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ReimplementationCheck(); // FIXME Handle re-initializtion
		}
		return INSTANCE;
	}
	
	public static void reset() {
		INSTANCE = null;
	}

	public boolean shouldProduce() {
		if (SimulationTime.getTick() >= 1.0d && SimulationTime.getYearOfSimulation() == SC_YEAR && SimulationTime.isBeginningOfDay()) {
			return true;
		}

		return false;
	}

	public void produce(Context<Agent> context) {
		maintenanceCheck.run(context);
		locomotionCheck.run(context);
		pregnancyCostsCheck.run(context);
		pregnancyMassesCheck.run(context);
		lactationCostsCheck.run(context);
		lactationMassesCheck.run(context);
		growthCostsCheck.run(context);
		growthMassesCheck.run(context);
		storageLevelAgeCheck.run(context);
		storageLevelMonthCheck.run(context);
		intakeAgeCheck.run(context);
		intakeMonthCheck.run(context);
		intakeCalvesCheck.run(context);
		totalEnergyExpenditureCheck.run(context);
	}

	private enum GroupingBin { BIN_1, BIN_2, BIN_3, BIN_4, BIN_IGNORE };
	
	@FunctionalInterface
	private interface MapToBin {
		GroupingBin map(Porpoise porp, CaraEnergetics energetics);
	}
	
	@FunctionalInterface
	private interface MapToValue {
		Double map(Porpoise porp, CaraEnergetics energetics);
	}
	
	private static class Check {
		private List<Double> binMeans = null;
		private List<Double> bin1 = new ArrayList<>();
		private List<Double> bin2 = new ArrayList<>();
		private List<Double> bin3 = new ArrayList<>();
		private List<Double> bin4 = new ArrayList<>();
		private final String name;
		private final MapToBin binMapping;
		private final MapToValue valueMapping;

		
		private Check(String name, MapToBin binMapping, MapToValue valueMapping) {
			this.name = name;
			this.binMapping = binMapping;
			this.valueMapping = valueMapping;
		}

		void run(Context<Agent> context) {
			if (binMeans == null) {
				var allPorps = context.getRandomObjectsAsStream(Porpoise.class, Long.MAX_VALUE);
				var grouped = allPorps.map(Porpoise.class::cast).collect(Collectors.groupingBy(p -> {
					var porp = (Porpoise) p;
					var energetics = (CaraEnergetics) porp.getEnergetics();
					return binMapping.map(porp, energetics);
				}));
				if (bin1.size() < NUM_RECS) {
					var bin = grouped.get(GroupingBin.BIN_1);
					if (bin != null) {
						var porp = (Porpoise) bin.get(0);
						var energetics = (CaraEnergetics) porp.getEnergetics();
						bin1.add(valueMapping.map(porp, energetics));
					}
				}
				if (bin2.size() < NUM_RECS) {
					var bin = grouped.get(GroupingBin.BIN_2);
					if (bin != null) {
						var porp = (Porpoise) bin.get(0);
						var energetics = (CaraEnergetics) porp.getEnergetics();
						bin2.add(valueMapping.map(porp, energetics));
					}
				}
				if (bin3.size() < NUM_RECS) {
					var bin = grouped.get(GroupingBin.BIN_3);
					if (bin != null) {
						var porp = (Porpoise) bin.get(0);
						var energetics = (CaraEnergetics) porp.getEnergetics();
						bin3.add(valueMapping.map(porp, energetics));
					}
				}
				if (bin4.size() < NUM_RECS) {
					var bin = grouped.get(GroupingBin.BIN_4);
					if (bin != null) {
						var porp = (Porpoise) bin.get(0);
						var energetics = (CaraEnergetics) porp.getEnergetics();
						bin4.add(valueMapping.map(porp, energetics));
					}
				}
				
				// FIXME Remove hack for maintenance
				if ((bin1.size() >= NUM_RECS || "maintenance".equals(name) ) && bin2.size() >= NUM_RECS && bin3.size() >= NUM_RECS && bin4.size() >= NUM_RECS) {
					binMeans = List.of(
							bin1.stream().mapToDouble(Double::doubleValue).average().getAsDouble(),
							bin2.stream().mapToDouble(Double::doubleValue).average().getAsDouble(),
							bin3.stream().mapToDouble(Double::doubleValue).average().getAsDouble(),
							bin4.stream().mapToDouble(Double::doubleValue).average().getAsDouble());
					bin1.clear();
					bin2.clear();
					bin3.clear();
					bin4.clear();

					System.err.println("RE-CHECK_" + name + ": " + binMeans);
				}
				
			}
		}
	}


/*

  ;;; pregnancy
  ; PregnancyCosts
  if preg-costs-check = 0 [
    if length p-c-c-1 < n-recs [ if any? porps with [ds-mating > 0 and ds-mating <= 75] [ ask one-of porps with [ds-mating > 0 and ds-mating <= 75] [ set p-c-c-1 lput m-preg p-c-c-1 ]]]
    if length p-c-c-2 < n-recs [ if any? porps with [ds-mating > 75 and ds-mating <= 150] [ ask one-of porps with [ds-mating > 75 and ds-mating <= 150] [ set p-c-c-2 lput m-preg p-c-c-2 ]]]
    if length p-c-c-3 < n-recs [ if any? porps with [ds-mating > 150 and ds-mating <= 225] [ ask one-of porps with [ds-mating > 150 and ds-mating <= 225] [ set p-c-c-3 lput m-preg p-c-c-3 ]]]
    if length p-c-c-4 < n-recs [ if any? porps with [ds-mating >= 225] [ ask one-of porps with [ds-mating >= 225] [ set p-c-c-4 lput m-preg p-c-c-4 ]]]

    if length p-c-c-1 >= n-recs and length p-c-c-2 >= n-recs and length p-c-c-3 >= n-recs and length p-c-c-4 >= n-recs [
      set preg-costs-check (list mean p-c-c-1 mean p-c-c-2 mean p-c-c-3 mean p-c-c-4)
      set p-c-c-1 "✓"
      set p-c-c-2 "✓"
      set p-c-c-3 "✓"
      set p-c-c-4 "✓"
    ]
  ]

  ; PregnancyMasses
  if preg-mass-check = 0 [
    if length p-m-c-1 < n-recs [ if any? porps with [ds-mating > 0 and ds-mating <= 75] [ ask one-of porps with [ds-mating > 0 and ds-mating <= 75] [ set p-m-c-1 lput mass-f p-m-c-1 ]]]
    if length p-m-c-2 < n-recs [ if any? porps with [ds-mating > 75 and ds-mating <= 150] [ ask one-of porps with [ds-mating > 75 and ds-mating <= 150] [ set p-m-c-2 lput mass-f p-m-c-2 ]]]
    if length p-m-c-3 < n-recs [ if any? porps with [ds-mating > 150 and ds-mating <= 225] [ ask one-of porps with [ds-mating > 150 and ds-mating <= 225] [ set p-m-c-3 lput mass-f p-m-c-3 ]]]
    if length p-m-c-4 < n-recs [ if any? porps with [ds-mating >= 225] [ ask one-of porps with [ds-mating >= 225] [ set p-m-c-4 lput mass-f p-m-c-4 ]]]

    if length p-m-c-1 >= n-recs and length p-m-c-2 >= n-recs and length p-m-c-3 >= n-recs and length p-m-c-4 >= n-recs [
      set preg-mass-check (list mean p-m-c-1 mean p-m-c-2 mean p-m-c-3 mean p-m-c-4)
      set p-m-c-1 "✓"
      set p-m-c-2 "✓"
      set p-m-c-3 "✓"
      set p-m-c-4 "✓"
    ]
  ]


  ;;; lactation
  ; LactationCosts
  if lact-costs-check = 0 [
    if length l-c-c-1 < n-recs [ if any? porps with [dsg-birth > 0 and dsg-birth <= 60] [ ask one-of porps with [dsg-birth > 0 and dsg-birth <= 60] [ set l-c-c-1 lput m-lact l-c-c-1 ]]]
    if length l-c-c-2 < n-recs [ if any? porps with [dsg-birth > 60 and dsg-birth <= 120] [ ask one-of porps with [dsg-birth > 60 and dsg-birth <= 120] [ set l-c-c-2 lput m-lact l-c-c-2 ]]]
    if length l-c-c-3 < n-recs [ if any? porps with [dsg-birth > 120 and dsg-birth <= 180] [ ask one-of porps with [dsg-birth > 120 and dsg-birth <= 180] [ set l-c-c-3 lput m-lact l-c-c-3 ]]]
    if length l-c-c-4 < n-recs [ if any? porps with [dsg-birth >= 180] [ ask one-of porps with [dsg-birth >= 180] [ set l-c-c-4 lput m-lact l-c-c-4 ]]]

    if length l-c-c-1 >= n-recs and length l-c-c-2 >= n-recs and length l-c-c-3 >= n-recs and length l-c-c-4 >= n-recs [
      set lact-costs-check (list mean l-c-c-1 mean l-c-c-2 mean l-c-c-3 mean l-c-c-4)
      set l-c-c-1 "✓"
      set l-c-c-2 "✓"
      set l-c-c-3 "✓"
      set l-c-c-4 "✓"
    ]
  ]

  ; LactationMasses
  if lact-mass-check = 0 [
    if length l-m-c-1 < n-recs [ if any? porps with [dsg-birth > 0 and dsg-birth <= 60] [ ask one-of porps with [dsg-birth > 0 and dsg-birth <= 60] [ set l-m-c-1 lput mass-calf l-m-c-1 ]]]
    if length l-m-c-2 < n-recs [ if any? porps with [dsg-birth > 60 and dsg-birth <= 120] [ ask one-of porps with [dsg-birth > 60 and dsg-birth <= 120] [ set l-m-c-2 lput mass-calf l-m-c-2 ]]]
    if length l-m-c-3 < n-recs [ if any? porps with [dsg-birth > 120 and dsg-birth <= 180] [ ask one-of porps with [dsg-birth > 120 and dsg-birth <= 180] [ set l-m-c-3 lput mass-calf l-m-c-3 ]]]
    if length l-m-c-4 < n-recs [ if any? porps with [dsg-birth >= 180] [ ask one-of porps with [dsg-birth >= 180] [ set l-m-c-4 lput mass-calf l-m-c-4 ]]]

    if length l-m-c-1 >= n-recs and length l-m-c-2 >= n-recs and length l-m-c-3 >= n-recs and length l-m-c-4 >= n-recs [
      set lact-mass-check (list mean l-m-c-1 mean l-m-c-2 mean l-m-c-3 mean l-m-c-4)
      set l-m-c-1 "✓"
      set l-m-c-2 "✓"
      set l-m-c-3 "✓"
      set l-m-c-4 "✓"
    ]
  ]

  ;;; growth
  ; GrowthCosts
  if growth-costs-check = 0 [
    if length g-c-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set g-c-c-1 lput m-growth g-c-c-1 ]]]
    if length g-c-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set g-c-c-2 lput m-growth g-c-c-2 ]]]
    if length g-c-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set g-c-c-3 lput m-growth g-c-c-3 ]]]
    if length g-c-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set g-c-c-4 lput m-growth g-c-c-4 ]]]

    if length g-c-c-1 >= n-recs and length g-c-c-2 >= n-recs and length g-c-c-3 >= n-recs and length g-c-c-4 >= n-recs [
      set growth-costs-check (list mean g-c-c-1 mean g-c-c-2 mean g-c-c-3 mean g-c-c-4)
      set g-c-c-1 "✓"
      set g-c-c-2 "✓"
      set g-c-c-3 "✓"
      set g-c-c-4 "✓"
    ]
  ]

  ; GrowthMasses
  if growth-mass-check = 0 [
    if length g-m-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set g-m-c-1 lput weight g-m-c-1 ]]]
    if length g-m-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set g-m-c-2 lput weight g-m-c-2 ]]]
    if length g-m-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set g-m-c-3 lput weight g-m-c-3 ]]]
    if length g-m-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set g-m-c-4 lput weight g-m-c-4 ]]]

    if length g-m-c-1 >= n-recs and length g-m-c-2 >= n-recs and length g-m-c-3 >= n-recs and length g-m-c-4 >= n-recs [
      set growth-mass-check (list mean g-m-c-1 mean g-m-c-2 mean g-m-c-3 mean g-m-c-4)
      set g-m-c-1 "✓"
      set g-m-c-2 "✓"
      set g-m-c-3 "✓"
      set g-m-c-4 "✓"
    ]
  ]

  ;;; storage
  ; StorageLevelAge
  if storage-level-age-check = 0 [
    if length sl-a-c-1 < n-recs [ if any? porps with [age <= 1] [ ask one-of porps with [age <= 1] [ set sl-a-c-1 lput storage-level sl-a-c-1 ]]]
    if length sl-a-c-2 < n-recs [ if any? porps with [age > 1 and age <= 2] [ ask one-of porps with [age > 1 and age <= 2] [ set sl-a-c-2 lput storage-level sl-a-c-2 ]]]
    if length sl-a-c-3 < n-recs [ if any? porps with [age > 2 and age <= 3] [ ask one-of porps with [age > 2 and age <= 3] [ set sl-a-c-3 lput storage-level sl-a-c-3 ]]]
    if length sl-a-c-4 < n-recs [ if any? porps with [age >= 3] [ ask one-of porps with [age >= 3] [ set sl-a-c-4 lput storage-level sl-a-c-4 ]]]

    if length sl-a-c-1 >= n-recs and length sl-a-c-2 >= n-recs and length sl-a-c-3 >= n-recs and length sl-a-c-4 >= n-recs [
      set storage-level-age-check (list mean sl-a-c-1 mean sl-a-c-2 mean sl-a-c-3 mean sl-a-c-4)
      set sl-a-c-1 "✓"
      set sl-a-c-2 "✓"
      set sl-a-c-3 "✓"
      set sl-a-c-4 "✓"
    ]
  ]

  ; StorageLevelMonth
  if storage-level-month-check = 0 [
    if length sl-m-c-1 < n-recs [ if month = 3 [ ask one-of porps [ set sl-m-c-1 lput storage-level sl-m-c-1 ]]]
    if length sl-m-c-2 < n-recs [ if month = 6 [ ask one-of porps [ set sl-m-c-2 lput storage-level sl-m-c-2 ]]]
    if length sl-m-c-3 < n-recs [ if month = 9 [ ask one-of porps [ set sl-m-c-3 lput storage-level sl-m-c-3 ]]]
    if length sl-m-c-4 < n-recs [ if month = 12 [ ask one-of porps [ set sl-m-c-4 lput storage-level sl-m-c-4 ]]]

    if length sl-m-c-1 >= n-recs and length sl-m-c-2 >= n-recs and length sl-m-c-3 >= n-recs and length sl-m-c-4 >= n-recs [
      set storage-level-month-check (list mean sl-m-c-1 mean sl-m-c-2 mean sl-m-c-3 mean sl-m-c-4)
      set sl-m-c-1 "✓"
      set sl-m-c-2 "✓"
      set sl-m-c-3 "✓"
      set sl-m-c-4 "✓"
    ]
  ]


  ;;; energy intake
  ; IntakeAge
    if intake-age-check = 0 [
    if length ei-a-c-1 < n-recs [ if any? porps with [age <= 3 and e-assim > 0] [ ask one-of porps with [age <= 3 and e-assim > 0] [ set ei-a-c-1 lput e-assim ei-a-c-1 ]]]
    if length ei-a-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6 and e-assim > 0] [ ask one-of porps with [age > 3 and age <= 6 and e-assim > 0] [ set ei-a-c-2 lput e-assim ei-a-c-2 ]]]
    if length ei-a-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9 and e-assim > 0] [ ask one-of porps with [age > 6 and age <= 9 and e-assim > 0] [ set ei-a-c-3 lput e-assim ei-a-c-3 ]]]
    if length ei-a-c-4 < n-recs [ if any? porps with [age >= 9 and e-assim > 0] [ ask one-of porps with [age >= 9 and e-assim > 0] [ set ei-a-c-4 lput e-assim ei-a-c-4 ]]]

    if length ei-a-c-1 >= n-recs and length ei-a-c-2 >= n-recs and length ei-a-c-3 >= n-recs and length ei-a-c-4 >= n-recs [
      set intake-age-check (list mean ei-a-c-1 mean ei-a-c-2 mean ei-a-c-3 mean ei-a-c-4)
      set ei-a-c-1 "✓"
      set ei-a-c-2 "✓"
      set ei-a-c-3 "✓"
      set ei-a-c-4 "✓"
    ]
  ]

  ; IntakeMonth
    if intake-month-check = 0 [
    if length ei-m-c-1 < n-recs [ if month = 3 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [ set ei-m-c-1 lput e-assim ei-m-c-1 ]]]]
    if length ei-m-c-2 < n-recs [ if month = 6 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [ set ei-m-c-2 lput e-assim ei-m-c-2 ]]]]
    if length ei-m-c-3 < n-recs [ if month = 9 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [  set ei-m-c-3 lput e-assim ei-m-c-3 ]]]]
    if length ei-m-c-4 < n-recs [ if month = 12 [ if any? porps with [e-assim > 0] [ ask one-of porps with [e-assim > 0] [  set ei-m-c-4 lput e-assim ei-m-c-4 ]]]]

    if length ei-m-c-1 >= n-recs and length ei-m-c-2 >= n-recs and length ei-m-c-3 >= n-recs and length ei-m-c-4 >= n-recs [
      set intake-month-check (list mean ei-m-c-1 mean ei-m-c-2 mean ei-m-c-3 mean ei-m-c-4)
      set ei-m-c-1 "✓"
      set ei-m-c-2 "✓"
      set ei-m-c-3 "✓"
      set ei-m-c-4 "✓"
    ]
  ]

  ; IntakeCalves
  if calf-intake-check = 0 [
    if length ei-c-c-1 < n-recs [ if any? porps with [dsg-birth >= 90 and dsg-birth <= 127 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 90 and dsg-birth <= 127 and e-assim-calf > 0] [ set ei-c-c-1 lput e-assim-calf ei-c-c-1 ]]]
    if length ei-c-c-2 < n-recs [ if any? porps with [dsg-birth > 127 and dsg-birth <= 164 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 127 and dsg-birth <= 164 and e-assim-calf > 0] [ set ei-c-c-2 lput e-assim-calf ei-c-c-2 ]]]
    if length ei-c-c-3 < n-recs [ if any? porps with [dsg-birth > 164 and dsg-birth <= 201 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth > 164 and dsg-birth <= 201 and e-assim-calf > 0] [ set ei-c-c-3 lput e-assim-calf ei-c-c-3 ]]]
    if length ei-c-c-4 < n-recs [ if any? porps with [dsg-birth >= 201 and e-assim-calf > 0] [ ask one-of porps with [dsg-birth >= 201 and e-assim-calf > 0] [ set ei-c-c-4 lput e-assim-calf ei-c-c-4 ]]]

    if length ei-c-c-1 >= n-recs and length ei-c-c-2 >= n-recs and length ei-c-c-3 >= n-recs and length ei-c-c-4 >= n-recs [
      set calf-intake-check (list mean ei-c-c-1 mean ei-c-c-2 mean ei-c-c-3 mean ei-c-c-4)
      set ei-c-c-1 "✓"
      set ei-c-c-2 "✓"
      set ei-c-c-3 "✓"
      set ei-c-c-4 "✓"
    ]
  ]

  ;;; TotalEnergyExpenditure
  if total-expenditure-check = 0 [
    if length tee-c-1 < n-recs [ if any? porps with [age <= 3] [ ask one-of porps with [age <= 3] [ set tee-c-1 lput m-tot tee-c-1 ]]]
    if length tee-c-2 < n-recs [ if any? porps with [age > 3 and age <= 6] [ ask one-of porps with [age > 3 and age <= 6] [ set tee-c-2 lput m-tot tee-c-2 ]]]
    if length tee-c-3 < n-recs [ if any? porps with [age > 6 and age <= 9] [ ask one-of porps with [age > 6 and age <= 9] [ set tee-c-3 lput m-tot tee-c-3 ]]]
    if length tee-c-4 < n-recs [ if any? porps with [age >= 9] [ ask one-of porps with [age >= 9] [ set tee-c-4 lput m-tot tee-c-4 ]]]

    if length tee-c-1 >= n-recs and length tee-c-2 >= n-recs and length tee-c-3 >= n-recs and length tee-c-4 >= n-recs [
      set total-expenditure-check (list mean tee-c-1 mean tee-c-2 mean tee-c-3 mean tee-c-4)
      set tee-c-1 "✓"
      set tee-c-2 "✓"
      set tee-c-3 "✓"
      set tee-c-4 "✓"
    ]
  ]


end
 */

}
