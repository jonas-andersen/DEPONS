/*
 * Copyright (C) 2017-2023 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise;

import java.util.LinkedList;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import dk.au.bios.porpoise.behavior.RandomSource;
import dk.au.bios.porpoise.landscape.CellData;
import dk.au.bios.porpoise.landscape.DataFileMetaData;
import dk.au.bios.porpoise.landscape.GridSpatialPartitioning;
import dk.au.bios.porpoise.util.SimulationTime;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;

/**
 * Placeholder for NETLOGO globals and various utility functionality.
 */
public final class Globals {

	public static final boolean ENERGETICS_USE_NEW = true;

	private Globals() {
		// Utility class, prevent instances.
	}

	private static CellData cellData;

	// The source for random values - if not null, we are doing a replay scenario.
	// public static String RANDOM_REPLAY_SOURCE = "data/RandomReplay/test.txt";
	private static String randomReplaySource = null; // disable

	private static RandomSource randomSource = null; // Defines the source of random numbers. Either generated or
	// replayed.

	private static PorpoiseStatistics monthlyStats = null;

	// Age of death for all animals that die. Reset every year
	private static LinkedList<Integer> listOfDeadAge = new LinkedList<Integer>();

	// Day of death for all animals that die. Reset every year
	private static LinkedList<Integer> listOfDeadDay = new LinkedList<Integer>();

	// Age of death for all calves that die. Reset every year
	private static LinkedList<Integer> listOfDeadAgeCalves = new LinkedList<Integer>();

	// Day of death for all calves that die. Reset every year
	private static LinkedList<Integer> listOfDeadDayCalves = new LinkedList<Integer>();

	// public static double[] MEAN_MAXENT_IN_QUATERS = {0.515686364223653, 0.888541219760357, 0.841346010536882, 1}; //
	// standardized average maxent level in each quarter
	private static double[] meanMaxEntInQuarters = { 1, 1, 1, 1 }; // standardized average maxent level in each quarter

	private static DataFileMetaData landscapeMetaData;

	private static Integer simYears = null; // Limit simulation to number of years.

	private static ContinuousSpace<Agent> space;
	private static Grid<Agent> grid;

	private static GridSpatialPartitioning spatialPartitioning;

	public static RandomSource getRandomSource() {
		return randomSource;
	}

	public static void setRandomSource(final RandomSource randomSource) {
		Globals.randomSource = randomSource;
	}

	public static String getRandomReplaySource() {
		return randomReplaySource;
	}

	public static PorpoiseStatistics getMonthlyStats() {
		return monthlyStats;
	}

	public static void resetMonthlyStats() {
		monthlyStats = new PorpoiseStatistics();
	}

	public static ContinuousSpace<Agent> getSpace() {
		return space;
	}

	public static void setSpace(ContinuousSpace<Agent> space) {
		Globals.space = space;
	}

	public static Grid<Agent> getGrid() {
		return grid;
	}

	public static void setGrid(Grid<Agent> grid) {
		Globals.grid = grid;
	}

	public static GridSpatialPartitioning getSpatialPartitioning() {
		return spatialPartitioning;
	}

	public static void setSpatialPartitioning(GridSpatialPartitioning gsp) {
		spatialPartitioning = gsp;
	}

	public static LinkedList<Integer> getListOfDeadAge() {
		return listOfDeadAge;
	}

	public static LinkedList<Integer> getListOfDeadDay() {
		return listOfDeadDay;
	}

	public static LinkedList<Integer> getListOfDeadAgeCalves() {
		return listOfDeadAgeCalves;
	}

	public static LinkedList<Integer> getListOfDeadDayCalves() {
		return listOfDeadDayCalves;
	}

	public static double getMeanMaxEntInCurrentQuarter() {
		return meanMaxEntInQuarters[SimulationTime.getQuarterOfYear()];
	}

	public static double getMeanMaxEntInQuarter(final int quarter) {
		return meanMaxEntInQuarters[quarter];
	}

	public static void setLandscapeMetadata(DataFileMetaData metadata) {
		Globals.landscapeMetaData = metadata;
	}

	public static double getXllCorner() {
		return Globals.landscapeMetaData.getXllcorner();
	}

	public static double getYllCorner() {
		return Globals.landscapeMetaData.getYllcorner();
	}

	public static int getWorldWidth() {
		return Globals.landscapeMetaData.getNcols();
	}

	public static int getWorldHeight() {
		return Globals.landscapeMetaData.getNrows();
	}

	public static CoordinateReferenceSystem getCoordinateReferenceSystem() {
		return Globals.landscapeMetaData.getCoordinateReferenceSystem();
	}

	public static Integer getSimYears() {
		return simYears;
	}

	public static void setSimYears(final Integer simYears) {
		Globals.simYears = simYears;
	}

	public static CellData getCellData() {
		return cellData;
	}

	public static void setCellData(final CellData cellData) {
		Globals.cellData = cellData;
	}

	public static double convertUtmXToGrid(final double utmX) {
		return ((utmX - Globals.getXllCorner()) / 400.0d) - 0.5d;
	}

	public static double convertUtmYToGrid(final double utmY) {
		return ((utmY - Globals.getYllCorner()) / 400.0d) - 0.5d;
	}

	public static double convertGridXToUtm(final double gridX) {
		return (gridX + 0.5d) * 400.0d + Globals.getXllCorner();
	}

	public static double convertGridYToUtm(final double gridY) {
		return (gridY + 0.5d) * 400.0d + Globals.getYllCorner();
	}

	public static double convertUtmDistanceToGrid(double utmDistance) {
		return utmDistance / SimulationConstants.REQUIRED_CELL_SIZE;
	}

	public static double convertGridDistanceToUtm(double gridDistance) {
		return gridDistance * SimulationConstants.REQUIRED_CELL_SIZE;
	}

	public static double convertGridDistanceToUtm(NdPoint start, NdPoint end) {
		return convertGridDistanceToUtm(getSpace().getDistance(start, end));
	}

	
	public static void caraSetupGlobalParameters() {
		/*
to setup-global-parameters  ; setup global parameters

  ;;; Energy budget parameters
  ; Submodel: Energy Intake
   set IR-coef 0.0004              ; Ingestion rate coefficient - estimated by ensuring IR-record sizes are not too large
   set satiation-c 10              ; Satiation constant - estimated using population storage levels
   set AE-food  0.82               ; Assimilation efficiency of food - Kriete 1995 -  killer whales fed fish diet
   set IR-to-EA 113750000          ; Ingested food to energy available - calibrated

  ; Submodel: LOCOMOTION
   set prop-eff 0.81               ; Propeller efficiency - Fish 1993
*/
		Globals.IRCoef = 0.0004d;
		Globals.satiationC = 10.0d;
		Globals.AEFood = 0.82d;
		Globals.IRToEA = 113750000d;

		Globals.propEff = 0.81d;
/*
  ; Submodel: REPRODUCTION
   set age-of-maturity 3.44        ; Age of maturity - Read (1990) and Caswell
   set max-mass-f 8                ; Max mass of fetus - Lockyer and Kinze 2003
   set f-growth-c 0.0066858          ; Fetal growth constant - calculated from Lockyer & Kinze 2003 relationship
   set percent-lip-f 0.285         ; Fetal blubber percent composition - Blubber percent from McLellan et al. 2002 and percent lipid of blubber as 68.3% for neonates from Lockyer 1995 ( Marine Mammales: Biology and Conservation pg 111)
   set percent-pro-f 0.139         ; Fetal protein percent composition - LM percent from McLellan et al. 2002 and percent protein of LM from Lockyer 1991 (sperm whale)
   set lact-eff 0.84               ; Efficiency of producing milk - Anderson and Fedak 1987 (grey seal)
   set repro-min-SL 0.10           ; Minimum storage level for reproductive energy allocation - Beltran et al. 2017
   set calf-idl-SL 0.375           ; Mean percent blubber for calves in McLellan et al. 2002
   set t-gest 300                  ; Gestation period - Lockyer et al., 2003
   set t-nurs 240                  ; Lactation period - Lockyer et al., 2003; Lockyer and Kinze, 2003
   set pregnancy-rate 0.67         ; Pregnancy rate - Sørensen & Kinze 1994
*/
		// FIXME ageOfMaturity
		Globals.maxMassF = 8;
		Globals.fGrowthC = 0.0066858d;
		Globals.percentLipF = 0.285d;
		Globals.percentProF = 0.139d;
		Globals.lactEff = 0.84d;
		Globals.reproMinSL = 0.10d;
		Globals.calfIdlSL = 0.375d;
		Globals.tGest = 300;
		Globals.tNurs = 240;
		Globals.pregnancyRate = 0.67d;
/*
  ; Submodel: GROWTH
   set ED-lip 39.5 * 1000000       ; Lipid energy density - Brody 1968, Blaxter 1989, Worthy 1982
   set ED-pro 23.6 * 1000000       ; Protein energy density - Brody 1968

  ; Submodel: STORAGE
   set dens-blub 0.00092           ; Density of lipid - Parry 1949 adjusted for water content

  ; Submodel: LIFE HISTORY
   set x-surv-prob-const 13.5      ; Survival probability constanct - calibrated
*/
		Globals.EDLip = 39.5 * 1000000;
		Globals.EDPro = 23.6 * 1000000;

		Globals.densBlub = 0.00092d;

		Globals.xSurvProbConst = 13.5d;

/*
  ;;; Environmental parameters - Slider params from Nabe-Nielsen et al. 2014 not changed in this version of the model
  ; dispersal params:
  set min-disp-depth 4.0           ; in m
  set n-disp-targets 12
  set mean-disp-dist 1.6           ; km / 30 min
  set min-dist-to-target 100       ; km

  set maxU 1.00
  set food-growth-rate 0.10        ; rU
  set gravity 9.8

end
		 */
		Globals.meanDispDist = 1.6d;
		

		/*
		  let xxxxx ((1 / 6.3661) * mean-temp) * (180 / pi)
		  let yyyyy (cos xxxxx)
		  set IR-temp-mod ((1 / 5)* yyyyy + 1)
		  */
		var meanTemp = 18.0d;// 3.3878634476026197d; //14.0; // FIXME
		var xxxxx = ((1.0d / 6.3661d) * meanTemp) * (180 / Math.PI);
		var yyyyy = Math.cos(xxxxx);
		Globals.IRTempMod = ((1.0d / 5.0d)* yyyyy + 1.0d); // FIXME calculated in "to landsc-setup" SPECIAL NOTE - may need to be recalculated with rotating landscape files
		System.out.println("IRTempMod: " + Globals.IRTempMod);
	}

	// Cara Energetics
	// FIXME Handle these properly (reset, etc)
	public static double IRCoef = 0.0004d;
	public static double satiationC = 10.0d;
	public static double AEFood = 0.82;
	public static double IRToEA = 113750000;
	public static double propEff = 0.81d;

	public static int maxMassF = 8;
	public static double fGrowthC = 0.0066858d;
	public static double percentLipF = 0.285d;
	public static double percentProF = 0.139d;
	public static double lactEff = 0.84d;
	public static double reproMinSL = 0.10d;
	public static double calfIdlSL = 0.375d;
	public static int tGest = 300;
	public static int tNurs = 240;
	public static double pregnancyRate = 0.67d;


	public static double EDLip = 39.5 * 1000000;                  // energy density of lipid, J kg-1
	public static double EDPro = 23.6 * 1000000;                  // energy density of protein, J kg-1
	public static double densBlub = 0.00092d;               // density of lipid, kg cm-3
	public static double xSurvProbConst = 13.5d;

	// n-calf-lost
	public static int nCalfLost = 0; // FIXME Remember to reset

	public static double meanDispDist = 1.6d;
	
	public static double IRTempMod;             // seasonal modifier of intake rate and mean storage level based on water temperature

}
