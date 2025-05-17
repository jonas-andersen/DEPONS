package dk.au.bios.porpoise.energetics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.CauseOfDeath;
import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.SimulationParameters;
import dk.au.bios.porpoise.util.DebugLog;
import dk.au.bios.porpoise.util.SimulationTime;

class WhiteboxCaraEnergeticsTest {

	@Test
	void energyIntakeBasicNotPregnant() {
		double massStruct = 12.36d;
		double mPreg = 0d;
		double mLactReal = 0d;
		boolean withLactCalf = false;
		double weanScaleFact = 0.9866666666666667d;
		Globals.IRTempMod = 1.1723405029453509d;
		
		assertThat(Globals.IRCoef).isEqualTo(0.0004d);
		assertThat(Globals.AEFood).isEqualTo(0.82d);
		assertThat(SimulationParameters.getIrToEA()).isEqualTo(113750000d);

		double IRStructMass = ((Globals.IRCoef * (Math.pow(massStruct, 0.75d)))  * Globals.IRTempMod);
		assertThat(IRStructMass).isCloseTo(0.003091203624724662d, within(0.0001d));
		
		double pregIRSup = (mPreg / (Globals.AEFood * SimulationParameters.getIrToEA()));
		double lactIRSup = (mLactReal /(Globals.AEFood * SimulationParameters.getIrToEA()));
		
		assertThat(pregIRSup).isEqualTo(0d);
		assertThat(lactIRSup).isEqualTo(0d);
	}

	@Test
	void energyIntakeLactCalf() {
		double massStruct = 34.458530407307556d;
		double mPreg = 0d;
		boolean withLactCalf = true;
		double weanScaleFact = 0.98d;
		Globals.IRTempMod = 0.8089843011993222d;
		
		assertThat(Globals.IRCoef).isEqualTo(0.0004d);
		assertThat(Globals.AEFood).isEqualTo(0.82d);
		assertThat(SimulationParameters.getIrToEA()).isEqualTo(113750000d);

		double IRStructMass = ((Globals.IRCoef * (Math.pow(massStruct, 0.75d)))  * Globals.IRTempMod);
		assertThat(IRStructMass).isCloseTo(0.004602275884823897d, within(0.0001d));
		
		double mLactReal =  299321.8567898224d;
		double pregIRSup = (mPreg / (Globals.AEFood * SimulationParameters.getIrToEA()));
		double lactIRSup = (mLactReal /(Globals.AEFood * SimulationParameters.getIrToEA()));
		
		assertThat(pregIRSup).isEqualTo(0d);
		assertThat(lactIRSup).isCloseTo(0.0032090255351361285d, within(0.0001d));
		
		double IRStructMassCalf = 0.0d;
		double massStructCalf = 9.95717283728942d;
		if (withLactCalf && weanScaleFact < 1.0d) {
			IRStructMassCalf = (((Globals.IRCoef * Math.pow(massStructCalf, 0.75)) * (1.00 - weanScaleFact)) * Globals.IRTempMod);
			assertThat(IRStructMassCalf).isCloseTo(3.6277062666899697E-5d, within(0.0001d));
		}
		double IRTimestep = IRStructMass + pregIRSup + lactIRSup;
		assertThat(IRTimestep).isCloseTo(0.007811301419960025d, within(0.0001d));
		
		double IRRecord = 0.8916823891914357d;
		IRRecord = IRRecord + IRTimestep;
		assertThat(IRRecord).isCloseTo(0.8994936906113957d, within(0.0001d));
		double IRRecordCalf = 0.0023796920661973554d;
		if (withLactCalf && weanScaleFact < 1) {
			IRRecordCalf = IRRecordCalf + IRStructMassCalf;
			assertThat(IRRecordCalf).isCloseTo(0.0024159691288642552d, within(0.0001d));
		}
		double IRReal = 0.0d;
		double IRRealCalf = 0.0d;
		double split = 0.0d;
		double foodAvailable = 0.01d; // 0d; //Globals.getCellData().getFoodLevel(ndPointToGridPoint(this.porp.posList.get(1)));  // Original states that it should be item 0 but uses item 1!

		if (withLactCalf == false || weanScaleFact == 1.0d) { // FIXME Check that this == 1 is correct with doubles
			if (foodAvailable > IRRecord) {
				IRReal = IRRecord;
			} else {
				IRReal = foodAvailable;
			}
		} else {
			if (foodAvailable > IRRecord + IRRecordCalf) {
				IRReal = IRRecord;
				IRRealCalf = IRRecordCalf;
				assertThat(IRReal).isCloseTo(0.024562425849850023d, within(0.0001d));
				assertThat(IRRealCalf).isCloseTo(0.0081510003800817d, within(0.0001d));
			} else {
				split = ( IRRecord / (IRRecord + IRRecordCalf));
				IRReal = foodAvailable * split;
				IRRealCalf = foodAvailable * (1.0d - split);

//				// food-available == 0
//				assertThat(split).isCloseTo(0.9999782452811078d, within(0.0001d));
//				assertThat(IRReal).isEqualTo(0d);
//				assertThat(IRRealCalf).isEqualTo(0d);
				// food-available == 0.1452733001637911
				assertThat(split).isCloseTo(0.9973212736965695d, within(0.0001d));
				assertThat(IRReal).isCloseTo(0.009973212736965695d, within(0.0001d));
				assertThat(IRRealCalf).isCloseTo(2.6787263034304677E-5d, within(0.0001d));
			}
		}

		IRRecord = IRRecord - IRReal;
		if (withLactCalf && weanScaleFact != 1.0d) {
			IRRecordCalf = IRRecordCalf - IRRealCalf;
		}
		double lgth = 1.5274649961339992d;
		double storageLevel = 0.1938519103355234d;
		double SLMean = 0.1936291447815015d;
		double maxSL = (lgth * -0.39635d) + 1.02347d;
		double overMeanSL = (storageLevel - SLMean) / (maxSL - SLMean);
		assertThat(maxSL).isCloseTo(0.4180592487822895d, within(0.0001d));
		assertThat(overMeanSL).isCloseTo(9.925832143316898E-4d, within(0.0001d));

		if (overMeanSL > 0.0d) {
			overMeanSL = 1.0d;
		}
		double IRSLMod = 1.0d;

		overMeanSL = 1d;
		IRReal = 0.07335768066918492d;
		if (overMeanSL > 0.0d) {
			double FC = (-1.0d * Globals.satiationC);
			assertThat(FC).isEqualTo(-10d);
			IRSLMod = Math.exp(overMeanSL * FC);
			assertThat(IRSLMod).isCloseTo(4.539992976248485E-5d, within(0.0001d));
			if (IRSLMod < 0.0d) {
				IRSLMod = 0.0d;
			}
			IRReal = IRReal * IRSLMod;
			assertThat(IRReal).isCloseTo(3.330433549919788E-6d, within(0.0001d));
		}
		double foodEaten = 0.0d;
		withLactCalf = true;
		weanScaleFact = 0.9933333333333333d;
		IRReal = 0.0036186476586325955d;
		IRRealCalf = 1.2775293935144093E-5d;
		if (withLactCalf == false || weanScaleFact == 1.0d) {
			foodEaten = IRReal;
		} else {
			foodEaten = IRReal + IRRealCalf;
		}
		assertThat(IRReal).isCloseTo(0.0036314229525677396d, within(0.0001d));
		/*
		foodIntakeList.add(IRReal);
		Globals.getCellData().eatFood(ndPointToGridPoint(this.porp.posList.get(1)), foodEaten);

		this.porp.getPersistentSpatialMemory().updateMemory(this.porp.getPosition(), IRReal);
		if (calfPsm != null && this.porp.getDispersalBehaviour().calfHasPSM()) {
			calfPsm.updateMemory(this.porp.getPosition(), IRRealCalf);
		}
		*/

		IRReal = 0.010951033650613336d;
		IRRealCalf = 1.268130632454807E-5d;
		double eAssim = (IRReal * SimulationParameters.getIrToEA() * Globals.AEFood);
		double eAssimCalf = (IRRealCalf * SimulationParameters.getIrToEA() * Globals.AEFood);
		assertThat(eAssim).isCloseTo(1021457.6637609589d, within(0.0001d));
		assertThat(eAssimCalf).isCloseTo(1182.848847422221d, within(0.0001d));		
	}

	@Test
	void growth() {
		double massStruct = 31.139999999999997d;
		double mStrInf = 48.54411820239911d;
		double eAssim = 814037.4412729508d;
		double mStrK = 1.190380490988589d;
		double EDLeanMass = 7867635.144213394d;
		double DELeanMass = 0.5991571882462928d;
		
		double eStorage = 3.007507317459859E8d;
		double vBlub = 10493.069308750279d;
		double vBlubMean = 12242.645417999294d;
		double DELip = 0.8799162133280322d;
		double percLipBlub = 0.7847346821473725d;
		
		if (massStruct < mStrInf) {
			double eAssimGrow = eAssim / 2.0d;
			assertThat(eAssimGrow).isCloseTo(407018.7206364754d, within(0.0001d));

			double maxGrow = (mStrK / 17280.0d)
					* ((Math.pow(mStrInf, (1.0d / 3.0d)) * Math.pow(massStruct, (2.0d / 3.0d))) - massStruct);
			assertThat(maxGrow).isCloseTo(3.4216476868564216E-4d, within(0.0001d));		

			double mGrowth = (maxGrow * (EDLeanMass + EDLeanMass * (1.0d - DELeanMass)));
			if (mGrowth < 0.0d) {
				mGrowth = 0.0d;
			}
			assertThat(mGrowth).isCloseTo(3771.1074553801436d, within(0.0001d));		

			if (eAssimGrow >= mGrowth) {
				// TEST OK
				eAssim = eAssim - mGrowth;
				eAssimGrow = 0.0d;
				massStruct = massStruct + maxGrow;
				double growthRate = maxGrow; // FIXME Not used??
				assertThat(growthRate).isCloseTo(3.4216476868564216E-4d, within(0.0001d));							
			} else {

				if (vBlub >= vBlubMean) {
					// TEST OK
					eStorage = eStorage + (eAssimGrow - mGrowth);
					eAssim = eAssim - eAssimGrow;
					vBlub = vBlub
							- (((mGrowth - eAssimGrow) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
					eAssimGrow = 0.0d;
					massStruct = massStruct + maxGrow;
					double growthRate = maxGrow; // FIXME Not used?
					assertThat(growthRate).isCloseTo(1.6850565582401632E-4d, within(0.0001d));							
				} else {
					// TEST OK
					double growthRate = (eAssimGrow / EDLeanMass) * DELeanMass;
					assertThat(growthRate).isCloseTo(0d, within(0.0001d));							
					massStruct = massStruct + growthRate;
					eAssim = eAssim - eAssimGrow;
					eAssimGrow = 0.0d;
				}
			}

			assertThat(Globals.EDLip).isCloseTo(39500000d, within(0.0001d));		
			assertThat(Globals.densBlub).isCloseTo( 9.2E-4d, within(0.0001d));		

			assertThat(eAssim).isCloseTo(810266.3338175707d, within(0.0001d));		
			assertThat(eAssimGrow).isCloseTo(0d, within(0.0001d));		
			assertThat(massStruct).isCloseTo(31.140342164768683d, within(0.0001d));		
			assertThat(eStorage).isCloseTo(3.007507317459859E8d, within(0.0001d));		
			assertThat(vBlub).isCloseTo(10493.069308750279d, within(0.0001d));		
		} // else tested ok
		
		

		double lgthInf = 163.37993350282503d;
		double lgth0 = 94.07428363227986d;
		double lgthK = 0.39614651111830507d;
		double porpAge = 5.502777777777778d; // 0.5027777777777778d;   // UNDER AGE OF MATURITY???
		double lgth = 1.54d;
		
		double lgthT = ((lgthInf * Math.exp(Math.log(lgth0 / lgthInf) * Math.exp((-lgthK) * porpAge)))
				/ 100.0d);
		assertThat(lgthT).isCloseTo(1.5349596960127372d, within(0.0001d));
		if (lgthT > lgth) {
			lgth = lgthT;
		}
		assertThat(lgth).isCloseTo(1.54d, within(0.0001d));
	}

	@Test
	void storage() {
		double eAssim = 800840.8881063601d;
		double DELip = 0.8461495411667866d;
		double percLipBlub = 0.8497197952082973d;
		double vBlub = 6808.493291536895d;
		
		if (eAssim >= 0.0d) {
			double addBlub = ((eAssim * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
			assertThat(addBlub).isCloseTo(15.844705614219498d, within(0.0001d));
			vBlub = vBlub + addBlub;
			assertThat(vBlub).isCloseTo(6824.337997151115d, within(0.0001d));
			eAssim = 0.0d;
		}
	}

	@Test
	void thermoregulation() {
		double eAssim = 0d;
		double mThermo = 0d;
		double eStorage =  1.4263655027919248E8d;
		double vBlub =  5872.562830703841d;
		double DELip = 0.8663569929306915d;
		double percLipBlub = 0.7950794876344269d;
		
		// Allocation
		if (eAssim >= mThermo) {
			eAssim = eAssim - mThermo;
		} else {
			eStorage = eStorage + eAssim;
			if (eStorage > mThermo) {
				eStorage = eStorage - mThermo;
				vBlub = vBlub - (((mThermo - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
			} else {
//				Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
//				Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
//
//				if (DebugLog.isEnabledFor(10)) {
//					DebugLog.print(porp.getId() + " died of low body condition-Frozen");
//				}
				System.out.println("die thermo");
//				porp.die(CauseOfDeath.Starvation); // FIXME Correct cause?
			}
			eAssim = 0.0d;
		}
		
		assertThat(eAssim).isCloseTo(0d, within(0.0001d));
		assertThat(eStorage).isCloseTo(1.4263655027919248E8d, within(0.0001d));
		assertThat(vBlub).isCloseTo(5872.562830703841d, within(0.0001d));
	}

	@Test
	void thermoregulation2() {
	/*
e-assim: 0   m-thermo: 48147.04054354498
e-storage: 1.2807369516618416E8   v-blub: 4992.108399647177
m-thermo: 48147.04054354498   e-assim: 0   DE-lip: 0.8799332745548147  perc-lip-blub: 0.8518805754047727
ED-lip: 39500000   dens-blub: 9.2E-4
v-blub: 4991.115253571486
RESULTS
e-assim: 0   e-storage: 1.2802554812564062E8   v-blub: 4991.115253571486 */

		double eStorage = 1.2807369516618416E8d;
		double eAssim = 0d;
		eStorage = eStorage + eAssim;
		assertThat(eStorage).isEqualTo(1.2807369516618416E8d);
		double mThermo = 48147.04054354498d;
		if (eStorage > mThermo) {
			eStorage = eStorage - mThermo;
			double vBlub = 4992.108399647177d;
			double DELip = 0.8799332745548147d;
			double percLipBlub = 0.8518805754047727d;
			vBlub = vBlub - (((mThermo - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
			assertThat(vBlub).isEqualTo(4991.115253571486d);
		} else {
//			Globals.getListOfDeadAge().addLast((int) this.porp.getAge());
//			Globals.getListOfDeadDay().addLast(SimulationTime.getDayOfSimulation());
//
//			if (DebugLog.isEnabledFor(10)) {
//				DebugLog.print(porp.getId() + " died of low body condition-Frozen");
//			}
			System.out.println("die thermo");
//			porp.die(CauseOfDeath.Starvation); // FIXME Correct cause?
		}
		eAssim = 0.0d;

	}

	@Test
	void updMortality() {
		double mMortProbConst = Math.pow(10, (2.176e-02 * Globals.xSurvProbConst + -3.875e-04));  // FIXME If xSurvProbConst is never updated, then no need to calculate this again and again 
		assertThat(mMortProbConst).isEqualTo(1.9650449987660124d);

		double storageLevel = 0.16474187019955125d;
		double yearlySurvProb = (1.0d - (mMortProbConst * Math.exp(- storageLevel * Globals.xSurvProbConst) ));
		assertThat(yearlySurvProb).isEqualTo(0.7874334371020894d);
		
//		double stepSurvProb = Math.exp(Math.log(yearlySurvProb) / (360.0d));
//		assertThat(stepSurvProb).isEqualTo(0.999744672460099d);

		double massCalf = 10.8792755206026d;
		double massStructCalf = 6.801858580244558d;
		double storageLevelCalf = ((massCalf - massStructCalf) / massCalf);
		assertThat(storageLevelCalf).isEqualTo(0.37478754285029775d);
		double yearlySurvProbCalf = (1.0d - (mMortProbConst * Math.exp(- storageLevelCalf * Globals.xSurvProbConst) ));  // FIXME Is this '-' at start ok? One other occurence earlier
		assertThat(yearlySurvProbCalf).isEqualTo(0.9875260983247501d);
		
		double stepSurvProbCalf = Math.exp( Math.log(yearlySurvProbCalf) / 360.0d );
		assertThat(stepSurvProbCalf).isEqualTo(0.9999651329582228d);
		

	}

	@Test
	void lactation() {
		// mass-calf: 8.271138649562042   B0: 11.122052910135105   m-BMR-calf: 97640.82440839821
		double B0 = 11.122052910135105d;
		double massCalf = 8.271138649562042d;
		// set m-BMR-calf (B0 * (mass-calf ^ 0.75)) * 1800
		double mBMRCalf = (B0 * (Math.pow(massCalf, 0.75d))) * 1800.0d;
		assertThat(mBMRCalf).isEqualTo(97640.82440839821d);
		
		// female
		var sexCalf = "male";
		if ("female".equals(sexCalf)) {
			// lgth-inf-f: 116.3   lgth-0-f: 89.2   lgth-k-f: 3.3   dsg-birth: 27
			// lgth-calf: 0.9454219628548434
			final double lgthInfF = 116.3d;
			final double lgth0F = 89.2d;
			final double lgthKF = 3.3d;
			double dsgBirth = 27.0d;
			double lgthCalf = ((lgthInfF * Math.exp(Math.log(lgth0F / lgthInfF) * Math.exp((-1.0d * lgthKF) * (dsgBirth / 360.0d)) )) / 100.0d);
			assertThat(lgthCalf).isEqualTo(0.9454219628548434d);
		} else {
			// lgth-inf-m: 112.9   lgth-0-m: 68.1   lgth-k-m: 6.2   dsg-birth: 9
			// lgth-calf: 0.7322690935865261
			final double lgthInfM = 112.9d;
			final double lgth0M = 68.1d;
			final double lgthKM = 6.2d;
			double dsgBirth = 9.0d;
			double lgthCalf = ((lgthInfM * Math.exp(Math.log(lgth0M / lgthInfM) * Math.exp ((-1.0d * lgthKM) * (dsgBirth / 360.0d))))/ 100.0d);
			assertThat(lgthCalf).isEqualTo(0.7322690935865261d);
		}

		//  EQN 49 -- NOT done because using lookup table instead of calculation!
		
		
		// EQN 50
		// m-str-k-c: 23.832814143725734   m-str-inf-c: 15.597371109721333   mass-struct-calf: 2.326569644606145
		// max-grow-calf: 0.002841683928278783
		double mStrKC = 23.832814143725734d;
		double mStrInfC = 15.597371109721333d;
		double massStructCalf = 2.326569644606145d;
		double maxGrowCalf = (mStrKC / 17280.0d) * ((Math.pow(mStrInfC, (1.0d / 3.0d)) * Math.pow(massStructCalf, (2.0d / 3.0d)) ) - massStructCalf);
		assertThat(maxGrowCalf).isEqualTo(0.002841683928278783d);


		// max-grow-calf: 0.0021369585249765653   ED-lean-mass: 6712329.460393432   DE-lean-mass: 0.4668943567251276
		// m-growth-calf: 21990.82083706218
		maxGrowCalf = 0.0021369585249765653d;
		double EDLeanMass = 6712329.460393432;
		double DELeanMass = 0.4668943567251276d;
		double mGrowthCalf = (maxGrowCalf * (EDLeanMass + EDLeanMass * (1.0d - DELeanMass)));  // FIXME Indentation is a bit special here, check if correct in original
		assertThat(mGrowthCalf).isEqualTo(21990.82083706218d);
		
		// v-blub-calf-idl: 2141.5430337773823   v-blub-calf: 2139.019506854547   dens-blub: 9.2E-4   perc-lip-blub: 0.8148663676176721   ED-lip: 39500000   DE-lip: 0.746127589616906
		// m-blub-calf: 100153.50660236648
		double vBlubCalfIdl = 2141.5430337773823d;
		double vBlubCalf = 2139.019506854547d;
		double percLipBlub = 0.8148663676176721d;
		double DELip = 0.746127589616906d;
		double mBlubCalf = (((vBlubCalfIdl - vBlubCalf) * Globals.densBlub * percLipBlub * Globals.EDLip) / DELip);
		assertThat(mBlubCalf).isEqualTo(100153.50660236648d);
		
		// m-BMR-calf: 76106.83553029905   m-thermo-calf: 0   m-growth-calf: 29300.969957968624   lact-eff: 0.84
		// m: 125485.48272412819
		// m-blub-calf: 177739.56251173234
		mBMRCalf = 76106.83553029905d;
		double mThermoCalf = 0d;
		mGrowthCalf = 29300.969957968624d;
		double m = ((mBMRCalf + mThermoCalf + mGrowthCalf) / Globals.lactEff);
		mBlubCalf = (337080.2d - m) * Globals.lactEff;
		assertThat(m).isEqualTo(125485.48272412819d);
		assertThat(mBlubCalf).isEqualTo(177739.56251173234d);

		
		// m-BMR-calf: 67875.03347562873   m-thermo-calf: 0   m-growth-calf: 36118.07780973726    m-blub-calf: 96440.48792521152   lact-eff: 0.84
		// e-calf: 238611.4276316399
		mBMRCalf = 67875.03347562873d;
		mThermoCalf = 0;
		mGrowthCalf = 36118.07780973726d;
		mBlubCalf = 96440.48792521152d;
		double eCalf = (mBMRCalf + mThermoCalf + mGrowthCalf + mBlubCalf) / Globals.lactEff;  // FIXME Why is this calculated again here?
		assertThat(eCalf).isEqualTo(238611.4276316399d);
		
/*		double dsgBirth = 99d;	
		double adjDate = dsgBirth - (Math.floor(0.375d * Globals.tNurs));
		assertThat(adjDate).isEqualTo(9d);
		double weanScaleFact = 1.00d - (adjDate / (0.625d * Globals.tNurs));
		if (weanScaleFact < 0) {
			weanScaleFact = 0.0d;
		}
		assertThat(weanScaleFact).isEqualTo(0.94);
		eCalf = 337080.2d;
		double mLact = eCalf * weanScaleFact;
		assertThat(mLact).isEqualTo(316855.388d); */

		double dsgBirth = 105d;
		double adjDate = dsgBirth - (Math.floor(0.375d * Globals.tNurs));
		assertThat(adjDate).isEqualTo(15d);
		double weanScaleFact = 1.00d - (adjDate / (0.625d * Globals.tNurs));
		if (weanScaleFact < 0) {
			weanScaleFact = 0.0d;
		}
		assertThat(weanScaleFact).isEqualTo(0.9);
		eCalf = 337080.2d;
		double mLact = eCalf * weanScaleFact;
		assertThat(mLact).isEqualTo(303372.18d);

		// TODO Check to wean-calf

		weanScaleFact = 0.5;
		if (weanScaleFact < 1) {
			// m-BMR-calf: 154510.3148859606   wean-scale-fact: 0.98   m-thermo-calf: 0   lact-eff: 0.84
			// vital-costs: 180262.0340336207
			// vital-costs-calf: 3090.206297719215
			mBMRCalf = 154510.3148859606d;
			weanScaleFact = 0.98d;
			mThermoCalf = 0d;
			double vitalCosts = ((mBMRCalf * weanScaleFact) + (mThermoCalf * weanScaleFact))/ Globals.lactEff;
			double vitalCostsCalf = ((mBMRCalf * (1.00 - weanScaleFact)) + (mThermoCalf * (1.00 - weanScaleFact)));
			assertThat(vitalCosts).isEqualTo(180262.0340336207d);
			assertThat(vitalCostsCalf).isEqualTo(3090.206297719215d);
		} else {
			mBMRCalf = 80194.5823547703d;
			mThermoCalf = 0d;
			double vitalCosts = (mBMRCalf + mThermoCalf)/ Globals.lactEff;
			assertThat(vitalCosts).isEqualTo(95469.74089853608d);
		}

		/*
 e-assim: 1.6596849462138567E7   m-lact: 237156.49584226008
e-assim: 1.6359692966296308E7
mass-struct-calf: 4.855899561645279   max-grow-calf: 0.0020203989887421056   wean-scale-fact: 1
mass-struct-calf: 4.857919960634021
v-blub-calf: 3163.4474004863446   m-blub-calf: 81464.3326830991   wean-scale-fact: 1
DE-lip: 0.8166499351759913   perc-lip-blub: 0.8505963788058871   ED-lip: 39500000   dens-blub: 9.2E-4
  v-blub-calf: 3165.0045920520865
m-lact-real: 237156.49584226008
		 
		double eAssim = 1.6596849462138567E7d;
		mLact = 237156.49584226008d;
		eAssim = eAssim - mLact;
		assertThat(eAssim).isEqualTo(1.6359692966296308E7d);
		massStructCalf = 4.855899561645279d;
		maxGrowCalf = 0.0020203989887421056d;
		weanScaleFact = 1d;
		massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFact);
		assertThat(massStructCalf).isEqualTo(4.857919960634021d);
		vBlubCalf = 3163.4474004863446d;
		mBlubCalf = 81464.3326830991d;
		DELip = 0.8166499351759913d;
		percLipBlub = 0.8505963788058871d;
		vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFact)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		double mLactReal = mLact;
		assertThat(vBlubCalf).isEqualTo(3165.0045920520865d);
		assertThat(mLactReal).isEqualTo(237156.49584226008d);*/
		
		/*
  e-storage: 4.011685248148864E8   e-assim: 143740.158748683   m-lact: 220384.10932883716
  e-storage: 4.013122649736351E8
  e-storage: 4.010918808643062E8
  v-blub: 13533.154970218491  m-lact: 220384.10932883716   e-assim: 143740.158748683 DE-lip: 0.8094080293934177  perc-lip-blub: 0.8547830185356714  ED-lip: 39500000  dens-blub: 9.2E-4
  v-blub: 13531.695765070262
  mass-struct-calf: 3.2707075123631366 max-grow-calf: 0.0033469476946720047   wean-scale-fact: 1
mass-struct-calf: 3.2740544600578088
v-blub-calf: 2129.798379170126   m-blub-calf: 78475.08255995759  wean-scale-fact: 1  DE-lip: 0.8094080293934177
  perc-lip-blub: 0.8547830185356714  ED-lip: 39500000  dens-blub: 9.2E-4
v-blub-calf: 2131.292446785539
m-lact-real: 220384.10932883716
e-assim: 0

		double eStorage = 4.011685248148864E8d;
		double eAssim = 143740.158748683d;
		mLact = 220384.10932883716d;
		eStorage = eStorage + eAssim;
		assertThat(eStorage).isEqualTo(4.013122649736351E8d);
		eStorage = eStorage - mLact;
		assertThat(eStorage).isEqualTo(4.010918808643062E8d);
		double vBlub = 13533.154970218491d;
		DELip = 0.8094080293934177d;
		percLipBlub = 0.8547830185356714d;
		vBlub = vBlub - (((mLact - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlub).isEqualTo(13531.695765070262d);
		massStructCalf = 3.2707075123631366d;
		maxGrowCalf = 0.0033469476946720047d;
		weanScaleFact = 1d;
		massStructCalf = massStructCalf + (maxGrowCalf * weanScaleFact);
		assertThat(massStructCalf).isEqualTo(3.2740544600578088d);

		vBlubCalf = 2129.798379170126d;
		mBlubCalf = 78475.08255995759d;
		vBlubCalf = vBlubCalf + (((mBlubCalf * weanScaleFact)* DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlubCalf).isEqualTo(2131.292446785539d);
		double mLactReal = mLact;
		eAssim = 0.0d;*/

		
		/*
e-assim: 99724.55580014427  m-lact-real: 99724.55580014427
e-assim: 13623.98003460723  vital-costs: 86100.57576553704
  m-growth-calf: 11408.614113686914  wean-scale-fact: 0.42000000000000004
e-assim-grow-calf: 4791.617927748504
e-assim-blub-calf: 8832.362106858725

ED-lean-mass: 7565595.233845519   DE-lean-mass: 0.5119547193382732
growth-rate-calf: 3.2424301532846364E-4
mass-struct-calf: 13.067799678469529
mass-struct-calf: 13.068123921484856
ED-lip: 39500000  perc-lip-blub: 0.7879907250639978   DE-lip: 0.7672611909595582   dens-blub: 9.2E-4
blub-calf: 0.23665411949898527
e-assim: 0		

		double eAssim = 99724.55580014427d;
		double mLactReal = eAssim;
		double vitalCosts = 86100.57576553704d;
		eAssim = eAssim - vitalCosts;
		assertThat(eAssim).isEqualTo(13623.98003460723d);
		mGrowthCalf = 11408.614113686914d;
		weanScaleFact = 0.42000000000000004d;
		double eAssimGrowCalf;
		if ((eAssim / 2.0d) >= (mGrowthCalf * weanScaleFact)) {
			eAssimGrowCalf = (mGrowthCalf * weanScaleFact);
		} else {
			eAssimGrowCalf = eAssim / 2.0d;
		}
		assertThat(eAssimGrowCalf).isEqualTo(4791.617927748504);
		double eAssimBlubCalf = eAssim - eAssimGrowCalf;
		assertThat(eAssimBlubCalf).isEqualTo(8832.362106858725);
		eAssim = 0.0d;
		
		EDLeanMass = 7565595.233845519d;
		DELeanMass = 0.5119547193382732d;
		double growthRateCalf = (eAssimGrowCalf / EDLeanMass) * DELeanMass;
		assertThat(growthRateCalf).isEqualTo(3.2424301532846364E-4d);
		massStructCalf = 13.067799678469529d;
		massStructCalf = massStructCalf + growthRateCalf;
		assertThat(massStructCalf).isEqualTo(13.068123921484856d);
		percLipBlub = 0.7879907250639978d;
		DELip = 0.7672611909595582d;
		double blubCalf = ((eAssimBlubCalf / (Globals.EDLip * percLipBlub)) * DELip)/ Globals.densBlub;
		assertThat(blubCalf).isEqualTo(0.23665411949898527d);
		
		if (eAssimBlubCalf >= (mBlubCalf * weanScaleFact)) {
			double blubCalfAdj = (((eAssimBlubCalf - (mBlubCalf * weanScaleFact))/ (Globals.EDLip * percLipBlub)) * DELip)/ Globals.densBlub;
			double blubCalfDif = blubCalf - blubCalfAdj;
			eAssim = (blubCalfDif * Globals.densBlub * percLipBlub * Globals.EDLip);
			blubCalf = blubCalfAdj;
		}
		vBlubCalf = vBlubCalf + blubCalf;

		double eAssimBlubCalf = 8832.362106858725d;
		mBlubCalf =  96440.48792521152d;
		weanScaleFact = 0.42000000000000004d;
		percLipBlub = 0.7879907250639978d;
		DELip = 0.7672611909595582d;
		double blubCalf = 0.23665411949898527d;
		  
		double blubCalfAdj = (((eAssimBlubCalf - (mBlubCalf * weanScaleFact))/ (Globals.EDLip * percLipBlub)) * DELip)/ Globals.densBlub;
		assertThat(blubCalfAdj).isEqualTo(-0.8486361075891394d);
		double blubCalfDif = blubCalf - blubCalfAdj;
		assertThat(blubCalfDif).isEqualTo(1.0852902270881246d);
		double eAssim = (blubCalfDif * Globals.densBlub * percLipBlub * Globals.EDLip);
		assertThat(eAssim).isEqualTo(31077.91832133185d);
		blubCalf = blubCalfAdj;*/

		/*
e-storage: 3.600524523175931E8   e-assim: 0   vital-costs: 90952.19101037584
e-storage: 3.600524523175931E8
e-storage: 3.5996150012658274E8
v-blub: 12382.446584872407   DE-lip: 0.7543036216416219   perc-lip-blub: 0.8542613334144866   ED-lip: 39500000   dens-blub: 9.2E-4
v-blub: 12380.83384180115 
		double eStorage = 3.600524523175931E8d;
		double eAssim = 0d;
		double vitalCosts = 90952.19101037584d;
		
		eStorage = eStorage + eAssim;
		assertThat(eStorage).isEqualTo(3.600524523175931E8d);
		eStorage = eStorage - vitalCosts;
		assertThat(eStorage).isEqualTo(3.5996150012658274E8d);
		double vBlub = 12382.446584872407d;
		DELip = 0.7543036216416219d;
		percLipBlub = 0.8542613334144866d;
		vBlub = vBlub - (((vitalCosts - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlub).isEqualTo(12380.83384180115d);
		double mLactReal = vitalCosts;
		eAssim = 0.0d;

		
		vBlubCalf = vBlubCalf - ((vitalCosts * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
*/

//		v-blub-calf: 3371.8764085919897   vital-costs: 117874.81915363032   DE-lip: 0.8322253050355025   perc-lip-blub: 0.7968307374709311   ED-lip: 39500000   dens-blub: 9.2E-4
//		v-blub-calf: 3369.725395211296
		vBlubCalf = 3371.8764085919897d;
		double vitalCosts = 117874.81915363032d;
		DELip = 0.8322253050355025d;
		percLipBlub = 0.7968307374709311d;
		vBlubCalf = vBlubCalf - ((vitalCosts * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlubCalf).isEqualTo(3369.725395211296d);

	}

	@Test
	void maintenance() {
//		weight: 41.89059076646081   B0: 11.134094697436952
//		m-BMR: 330000.63810937415
//		double B0 = 11.134094697436952d;
//		double weight = 41.89059076646081d;
//		double mBMR = (B0 * (Math.pow(weight, 0.75d)) * 1800.0d);
//		assertThat(mBMR).isEqualTo(330000.63810937415d);
		
//		e-storage: 3.2338950082326436E8   e-assim: 0
//		e-storage: 3.2338950082326436E8
//		e-storage: 3.229885572875521E8
//		weight: 54.29165891600971   B0: 11.13681170129812
//		m-BMR: 400943.53571222106
//		v-blub: 11849.629256532293   m-BMR: 400943.53571222106   e-assim: 0 DE-lip: 0.8480853141015807
//		perc-lip-blub: 0.8733246689782965   ED-lip: 39500000    dens-blub: 9.2E-4
//		v-blub: 11841.457534908279
		
		double eStorage = 3.2338950082326436E8d;
		double eAssim = 0d;
		double B0 = 11.13681170129812d;
		double weight = 54.29165891600971d;
		double mBMR = (B0 * (Math.pow(weight, 0.75d)) * 1800.0d);
		assertThat(mBMR).isEqualTo(400943.53571222106d);
		eStorage = eStorage + eAssim;
		assertThat(eStorage).isEqualTo(3.2338950082326436E8d);
		eStorage = eStorage - mBMR;
		assertThat(eStorage).isEqualTo(3.229885572875521E8d);

		double vBlub = 11849.629256532293d;
		double DELip = 0.8480853141015807d;
		double percLipBlub = 0.8733246689782965d;
		vBlub = vBlub - (((mBMR - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub));
		assertThat(vBlub).isEqualTo(11841.457534908279d);
	}
	
	@Test
	void calcSwimSpeed() {
//		pres-logmov: 0.47979429798832696  swim-speed-pres-mov: 0.16769564832669964
//		swim-speed-pres-log-trans: 2.8195656263433913
//		swim-speed: 0.9755697067148132
		double presLogMov = 0.47979429798832696d;
		double swimSpeedPresMov = (Math.pow(10.0d, presLogMov) * 100.0d) / 1800.0d;
		assertThat(swimSpeedPresMov).isEqualTo(0.16769564832669964d);
		double swimSpeedPresLogTrans = Math.log(swimSpeedPresMov * 100.0d);
		assertThat(swimSpeedPresLogTrans).isEqualTo(2.8195656263433913d);
		double swimSpeed = swimSpeedPresLogTrans * 0.346d;
		assertThat(swimSpeed).isEqualTo(0.9755697067148132d);

//		mean-disp-dist: 1.6
//		swim-speed: 0.8888888888888888
		swimSpeed = (Globals.meanDispDist * 1000.0d * (1.0d / (30.0d * 60.0d)));
		assertThat(swimSpeed).isEqualTo(0.8888888888888888d);
	}

	@Test
	void locomotion() {
//		swim-speed: 1.2005278627002638   aero-eff: 0.14484912309890477
//		lgth: 1.37   swim-speed: 1.2005278627002638   kin-visc-w: 1.604437337725719E-6
//		Re: 1025109.0106334395
//		Re: 6.010770050952024
//		drag-coef: -2.216184040527158
//		drag-coef: 0.006078773471297386

		double swimSpeed = 1.2005278627002638d;
		double aeroEff = 0.13478d + 0.441d * (Math.pow((swimSpeed / 4.2), 3.0d)) - 0.422 * (Math.pow((swimSpeed / 4.2), 6.0d));
		assertThat(aeroEff).isEqualTo(0.14484912309890477d);
		double lgth = 1.37d;
		double kinViscW = 1.604437337725719E-6d;
		double Re = (lgth * swimSpeed) / kinViscW;
		assertThat(Re).isEqualTo(1025109.0106334395d);
		Re = Math.log10(Re);
		assertThat(Re).isEqualTo(6.010770050952025d);
		
		double dragCoef = (-0.113188d * Re + -1.535837d);
		assertThat(dragCoef).isEqualTo(-2.216184040527158d);
		dragCoef = Math.pow(10.0d, dragCoef);
		assertThat(dragCoef).isEqualTo(0.0060787734712973865d);
		
//		lambda: 0.26953218841217996  density-w: 1019.053198   surface-area: 0.7722026325712836  drag-coef: 0.006078773471297386
//		 swim-speed: 1.2005278627002638   aero-eff: 0.14484912309890477   prop-eff: 0.81
//		m-loco: 9.506936167045042
//		m-loco: 17112.485100681075
//		m-loco-ineff-pts: 14633.75663980435
		double lambda = 0.26953218841217996d;
		double densityW = 1019.053198d;
		double surfaceArea = 0.7722026325712836d;
		double mLoco = (lambda * densityW * surfaceArea * dragCoef * (Math.pow(swimSpeed, 3.0d))) / (2.0d * aeroEff * Globals.propEff);
		assertThat(mLoco).isEqualTo(9.506936167045042d);
		mLoco = mLoco * 1800.0d;
		assertThat(mLoco).isEqualTo(17112.485100681075d);
		double mLocoIneffPts = (1.0d - aeroEff) * mLoco;
		assertThat(mLocoIneffPts).isEqualTo(14633.75663980435d);

		
//		v-blub: 24871.34548728582  m-loco: 10280.831049601313   e-assim: 0
//		DE-lip: 0.8360322685936363  perc-lip-blub: 0.8438511996043796  ED-lip: 39500000   dens-blub: 9.2E-4
//		v-blub: 24871.14590030356
		mLoco = 10280.831049601313d;
		double eAssim = 0d;
		double DELip = 0.8360322685936363d;
		double percLipBlub = 0.8438511996043796d;
		double vBlub = 24871.34548728582d;
		vBlub = vBlub - (((mLoco - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlub).isEqualTo(24871.14590030356d);
	}

	@Test
	void pregnancy() {
//		f-growth-c: 0.0066858  mass-f: 0.7590640632201126  f-growth-c: 0.0066858
//		max-grow-f: 3.4771147142937287E-4
//		max-grow-f: 3.4771147142937287E-4   percent-lip-f: 0.285  ED-lip: 39500000   DE-lip: 0.8013242688788894
//		percent-pro-f: 0.139  ED-pro: 23600000  DE-pro: 0.5246467433011425
//		m-growth-f: 7058.9628857363
//		max-mass-f: 8
//		e-heat-gest: 15502.075390461012
//		m-preg: 22561.038276197312
		double massF = 0.7590640632201126d;
		double DELip = 0.8013242688788894d;
		double DEPro = 0.5246467433011425d;
		
		double maxGrowF = (3.0d * Math.pow((Globals.fGrowthC), 3.0d) ) * (Math.pow(( Math.pow(massF, (1.0d / 3.0d)) / Globals.fGrowthC), 2.0d))/ 48.0d;
		assertThat(maxGrowF).isEqualTo(3.4771147142937287E-4d);
		double mGrowthF = ((maxGrowF * Globals.percentLipF * Globals.EDLip) / DELip) + ((maxGrowF * Globals.percentProF * Globals.EDPro) / DEPro);
		assertThat(mGrowthF).isEqualTo(7058.9628857363d);
		double eHeatGest = ((4400.0d * (Math.pow(Globals.maxMassF, 1.2d))) * 4184.0d) / 14400.0d;
		assertThat(eHeatGest).isEqualTo(15502.075390461012d);
		double mPreg = eHeatGest + mGrowthF;
		assertThat(mPreg).isEqualTo(22561.038276197312d);

		
//		v-blub: 12126.348738655028  m-preg: 23148.87280980657  e-assim: 0
//		DE-lip: 0.8958107791185712  perc-lip-blub: 0.8103518869891796   ED-lip: 39500000   dens-blub: 9.2E-4
//		v-blub: 12125.886320520416
		double vBlub = 12126.348738655028d;
		mPreg = 23148.87280980657d;
		double eAssim = 0d;
		DELip = 0.8958107791185712d;
		double percLipBlub = 0.8103518869891796d;
		vBlub = vBlub - (((mPreg - eAssim) * DELip * percLipBlub) / (Globals.EDLip * Globals.densBlub ));
		assertThat(vBlub).isEqualTo(12125.886320520416d);
	}

	@Test
	void updStateVariables() {
		/*
m-BMR: 430759.58114798367  m-loco: 32948.286632455616   m-thermo: 0
m-growth: 710.4049323285311   m-preg: 0   m-lact-real: 0
m-tot: 464418.2727127678
mass-struct: 41.74013027264632   v-blub: 19394.597901268346   dens-blub: 9.2E-4
weight: 59.5831603418132
weight: 59.5831603418132  mass-struct: 41.74013027264632  weight: 59.5831603418132
storage-level: 0.2994643111712441
storage-level-sum: 0.2995644711428439   storage-level: 0.2994643111712441
storage-level-sum: 0.5990287823140881 */ 
		
		double mBMR = 430759.58114798367d;
		double mLoco = 32948.286632455616d;
		double mThermo = 0d;
		double mGrowth = 710.4049323285311d;
		double mPreg = 0d;
		double mLactReal = 0d;

		double mTot = mBMR + mLoco + mThermo + mGrowth + mPreg + mLactReal;
		assertThat(mTot).isEqualTo(464418.2727127678d);
		
		double massStruct = 41.74013027264632d;
		double vBlub = 19394.597901268346d;
		double weight = massStruct + (vBlub * Globals.densBlub);
		assertThat(weight).isEqualTo(59.5831603418132d);
		double storageLevel = (weight - massStruct) / weight;
		assertThat(storageLevel).isEqualTo(0.2994643111712441d);
		double storageLevelSum = 0.2995644711428439d;
		storageLevelSum = storageLevelSum + storageLevel;
		assertThat(storageLevelSum).isEqualTo(0.5990287823140881d);

		
		/* pregnancy-status: 2
		weight: 59.5831603418132
		surface-area: 0.9556599005516321 */
		int pregnancyStatus = 2;
		
		if (pregnancyStatus != 1) {
			double surfaceArea = 0.093d * Math.pow(weight, 0.57d);
			assertThat(surfaceArea).isEqualTo(0.9556599005516321d);
		} else {
			double massF = -1d;  // NOT TESTED
			double surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
		}

		/*
		lgth: 1.5248755367947593  IR-temp-mod: 1.17234050294535
		SL-mean: 0.281526520473639
		weight: 59.5831603418132   SL-mean: 0.281526520473639   dens-blub: 9.2E-4
		v-blub-mean: 18232.86935853651
		v-blub-min: 3238.2152359681086
		repro-min-SL: 0.1
		v-blub-repro: 6476.430471936217
		v-blub: 19394.597901268346   v-blub-min: 3238.2152359681086   dens-blub: 9.2E-4   ED-lip: 39500000
		e-storage: 5.871229460570107E8
		v-blub-repro: 6476.430471936217
		e-repo-min: 1.1767674167508107E8
				 */

		Globals.IRTempMod = 1.17234050294535d;
		double lgth = 1.5248755367947593d;
		double SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		assertThat(SLMean).isEqualTo(0.281526520473639d);

		double vBlubMean = (weight * SLMean / Globals.densBlub);
		assertThat(vBlubMean).isEqualTo(18232.86935853651d);
		double vBlubMin = (weight * 0.05d) / Globals.densBlub;
		assertThat(vBlubMin).isEqualTo(3238.2152359681086d);
		double vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		assertThat(vBlubRepro).isEqualTo(6476.430471936217d);
		
		double eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eStorage).isEqualTo(5.871229460570107E8d);
		double eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eRepoMin).isEqualTo(1.1767674167508107E8d);

		boolean withLactCalf = false;
		if (withLactCalf) {
			// NOT TESTED
			double massStructCalf = -1d;
			double vBlubCalf = -1d;
			double massCalf = massStructCalf + (vBlubCalf * Globals.densBlub);
			double vBlubCalfIdl = (massCalf * Globals.calfIdlSL / Globals.densBlub);
		}
	}

	@Test
	void updStateVariables2() {
		/*
m-BMR: 263432.1226937805  m-loco: 10753.262266046318   m-thermo: 0
m-growth: 4380.561068132265   m-preg: 22440.41231164057   m-lact-real: 0
m-tot: 301006.3583395997
mass-struct: 23.6   v-blub: 8032.337225817799   dens-blub: 9.2E-4
weight: 30.989750247752376
weight: 30.989750247752376  mass-struct: 23.6  weight: 30.989750247752376
storage-level: 0.23845788328959955
storage-level-sum: 0.23858426599609125   storage-level: 0.23845788328959955
storage-level-sum: 0.4770421492856908 */ 
		
		double mBMR = 263432.1226937805d;
		double mLoco = 10753.262266046318d;
		double mThermo = 0d;
		double mGrowth = 4380.561068132265d;
		double mPreg = 22440.41231164057d;
		double mLactReal = 0d;

		double mTot = mBMR + mLoco + mThermo + mGrowth + mPreg + mLactReal;
		assertThat(mTot).isEqualTo(301006.3583395997d);
		
		double massStruct = 23.6d;
		double vBlub = 8032.337225817799d;
		double weight = massStruct + (vBlub * Globals.densBlub);
		assertThat(weight).isEqualTo(30.989750247752376d);
		double storageLevel = (weight - massStruct) / weight;
		assertThat(storageLevel).isEqualTo(0.23845788328959955d);
		double storageLevelSum = 0.23858426599609125d;
		storageLevelSum = storageLevelSum + storageLevel;
		assertThat(storageLevelSum).isEqualTo(0.4770421492856908d);

		
		/* pregnancy-status: 1
           weight: 30.989750247752376   mass-f: 0.7703193682574472
           surface-area: 0.6768428176113869 */
		int pregnancyStatus = 1;
		
		if (pregnancyStatus != 1) {  // NOT TESTED
			double surfaceArea = 0.093d * Math.pow(weight, 0.57d);
			assertThat(surfaceArea).isEqualTo(0.9556599005516321d);
		} else {
			double massF = 0.7703193682574472d;
			double surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
			assertThat(surfaceArea).isEqualTo(0.6768428176113869d);
		}

		/*
		lgth: 1.5555547483307013  IR-temp-mod: 1.17234050294535
		SL-mean: 0.2705243735435713
		weight: 30.989750247752376   SL-mean: 0.2705243735435713   dens-blub: 9.2E-4
		v-blub-mean: 9112.481273961896
		v-blub-min: 1684.2255569430638
		repro-min-SL: 0.1
		v-blub-repro: 3368.4511138861276
		v-blub: 8032.337225817799   v-blub-min: 1684.2255569430638   dens-blub: 9.2E-4   ED-lip: 39500000
		e-storage: 2.306903780469079E8
		v-blub-repro: 3368.4511138861276
		e-repo-min: 6.120475673931094E7
				 */

		Globals.IRTempMod = 1.17234050294535d;
		double lgth = 1.5555547483307013d;
		double SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		assertThat(SLMean).isEqualTo(0.2705243735435713d);

		double vBlubMean = (weight * SLMean / Globals.densBlub);
		assertThat(vBlubMean).isEqualTo(9112.481273961896d);
		double vBlubMin = (weight * 0.05d) / Globals.densBlub;
		assertThat(vBlubMin).isEqualTo(1684.2255569430638d);
		double vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		assertThat(vBlubRepro).isEqualTo(3368.4511138861276d);
		
		double eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eStorage).isEqualTo(2.306903780469079E8d);
		double eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eRepoMin).isEqualTo(6.120475673931094E7d);

		boolean withLactCalf = false;
		if (withLactCalf) {
			// NOT TESTED
			double massStructCalf = -1d;
			double vBlubCalf = -1d;
			double massCalf = massStructCalf + (vBlubCalf * Globals.densBlub);
			double vBlubCalfIdl = (massCalf * Globals.calfIdlSL / Globals.densBlub);
		}
	}

	@Test
	void updStateVariables3() {
		/*
m-BMR: 395202.15478977887  m-loco: 17688.704229503026   m-thermo: 0
m-growth: 0   m-preg: 0   m-lact-real: 0
m-tot: 412890.8590192819
mass-struct: 42.730000000000004   v-blub: 11481.007841507975   dens-blub: 9.2E-4
weight: 53.292527214187345
weight: 53.292527214187345  mass-struct: 42.730000000000004  weight: 53.292527214187345
storage-level: 0.19819903026432986
storage-level-sum: 0   storage-level: 0.19819903026432986
storage-level-sum: 0.19819903026432986 */ 
		
		double mBMR = 395202.15478977887d;
		double mLoco = 17688.704229503026d;
		double mThermo = 0d;
		double mGrowth = 0d;
		double mPreg = 0d;
		double mLactReal = 0d;

		double mTot = mBMR + mLoco + mThermo + mGrowth + mPreg + mLactReal;
		assertThat(mTot).isEqualTo(412890.8590192819d);
		
		double massStruct = 42.730000000000004d;
		double vBlub = 11481.007841507975d;
		double weight = massStruct + (vBlub * Globals.densBlub);
		assertThat(weight).isEqualTo(53.292527214187345d);
		double storageLevel = (weight - massStruct) / weight;
		assertThat(storageLevel).isEqualTo(0.19819903026432986d);
		double storageLevelSum = 0d;
		storageLevelSum = storageLevelSum + storageLevel;
		assertThat(storageLevelSum).isEqualTo(0.19819903026432986d);

		
		/* pregnancy-status: 2
		   weight: 53.292527214187345
		   surface-area: 0.8967734633767085 */
		int pregnancyStatus = 2;
		
		if (pregnancyStatus != 1) {
			double surfaceArea = 0.093d * Math.pow(weight, 0.57d);
			assertThat(surfaceArea).isEqualTo(0.8967734633767087d);
		} else {
			double massF = 0.7703193682574472d;  // NOT TESTED
			double surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
			assertThat(surfaceArea).isEqualTo(0.6768428176113869d);
		}

		/*
		lgth: 1.592425176622531  IR-temp-mod: 0.8505563864582655
		SL-mean: 0.1866776818082368
		weight: 53.292527214187345   SL-mean: 0.1866776818082368   dens-blub: 9.2E-4
		v-blub-mean: 10813.614606572679
		v-blub-min: 2896.3330007710515
		repro-min-SL: 0.1
		v-blub-repro: 5792.666001542103
		v-blub: 11481.007841507975   v-blub-min: 2896.3330007710515   dens-blub: 9.2E-4   ED-lip: 39500000
		e-storage: 3.119670837123798E8
		v-blub-repro: 5792.666001542103
		e-repo-min: 1.0525274124802001E8
				 */

		Globals.IRTempMod = 0.8505563864582655d;
		double lgth = 1.592425176622531d;
		double SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		assertThat(SLMean).isEqualTo(0.1866776818082368d);

		double vBlubMean = (weight * SLMean / Globals.densBlub);
		assertThat(vBlubMean).isEqualTo(10813.614606572679d);
		double vBlubMin = (weight * 0.05d) / Globals.densBlub;
		assertThat(vBlubMin).isEqualTo(2896.3330007710515d);
		double vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		assertThat(vBlubRepro).isEqualTo(5792.666001542103d);
		
		double eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eStorage).isEqualTo(3.119670837123798E8d);
		double eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eRepoMin).isEqualTo(1.0525274124802001E8d);

		/*
with-lact-calf? = TRUE
mass-struct-calf: 5.34751211501332   v-blub-calf: 3487.5079010956433   dens-blub: 9.2E-4
mass-calf: 8.556019384021312
mass-calf: 8.556019384021312  calf-idl-SL: 0.375
v-blub-calf-idl: 3487.5079010956433 
		 */
		boolean withLactCalf = true;
		if (withLactCalf) {
			// NOT TESTED
			double massStructCalf = 5.34751211501332d;
			double vBlubCalf = 3487.5079010956433d;
			double massCalf = massStructCalf + (vBlubCalf * Globals.densBlub);
			assertThat(massCalf).isEqualTo(8.556019384021312d);
			double vBlubCalfIdl = (massCalf * Globals.calfIdlSL / Globals.densBlub);
			assertThat(vBlubCalfIdl).isEqualTo(3487.5079010956433d);
		}
	}

	@Test
	void updStateVariables4() {
		/*
m-BMR: 343601.9256891199  m-loco: 17258.179597623508   m-thermo: 0
m-growth: 3305.867264150493   m-preg: 0   m-lact-real: 82156.08921796153
m-tot: 446322.06176885543
mass-struct: 35.02398975813993   v-blub: 9879.003464664665   dens-blub: 9.2E-4
weight: 44.112672945631424
weight: 44.112672945631424  mass-struct: 35.02398975813993  weight: 44.112672945631424
storage-level: 0.20603338180602276
storage-level-sum: 9.200434135488207   storage-level: 0.20603338180602276
storage-level-sum: 9.40646751729423 */ 
		
		double mBMR = 343601.9256891199d;
		double mLoco = 17258.179597623508d;
		double mThermo = 0d;
		double mGrowth = 3305.867264150493d;
		double mPreg = 0d;
		double mLactReal = 82156.08921796153d;

		double mTot = mBMR + mLoco + mThermo + mGrowth + mPreg + mLactReal;
		assertThat(mTot).isEqualTo(446322.06176885543d);
		
		double massStruct = 35.02398975813993d;
		double vBlub = 9879.003464664665d;
		double weight = massStruct + (vBlub * Globals.densBlub);
		assertThat(weight).isEqualTo(44.112672945631424d);
		double storageLevel = (weight - massStruct) / weight;
		assertThat(storageLevel).isEqualTo(0.20603338180602276d);
		double storageLevelSum = 9.200434135488207d;
		storageLevelSum = storageLevelSum + storageLevel;
		assertThat(storageLevelSum).isEqualTo(9.40646751729423d);

		
		/* pregnancy-status: 2
		   weight: 44.112672945631424
		   surface-area: 0.805163486483289 */
		int pregnancyStatus = 2;
		
		if (pregnancyStatus != 1) {
			double surfaceArea = 0.093d * Math.pow(weight, 0.57d);
			assertThat(surfaceArea).isEqualTo(0.805163486483289d);
		} else {
			double massF = 0.7703193682574472d;  // NOT TESTED
			double surfaceArea = 0.093d * Math.pow((weight + (2.0d * massF)), 0.57d);
			assertThat(surfaceArea).isEqualTo(0.6768428176113869d);
		}

		/*
		lgth: 1.5910407528021244  IR-temp-mod: 0.9592892766568917
		SL-mean: 0.21094831985556936
		weight: 44.112672945631424   SL-mean: 0.21094831985556936   dens-blub: 9.2E-4
		v-blub-mean: 10114.667654586065
		v-blub-min: 2397.4278774799686
		repro-min-SL: 0.1
		v-blub-repro: 4794.855754959937
		v-blub: 9879.003464664665   v-blub-min: 2397.4278774799686   dens-blub: 9.2E-4   ED-lip: 39500000
		e-storage: 2.718804568382919E8
		v-blub-repro: 4794.855754959937
		e-repo-min: 8.712252906762207E7
				 */

		Globals.IRTempMod = 0.9592892766568917d;
		double lgth = 1.5910407528021244d;
		double SLMean = ((lgth * -0.3059d) + 0.7066d) * Globals.IRTempMod;
		assertThat(SLMean).isEqualTo(0.21094831985556936d);

		double vBlubMean = (weight * SLMean / Globals.densBlub);
		assertThat(vBlubMean).isEqualTo(10114.667654586065d);
		double vBlubMin = (weight * 0.05d) / Globals.densBlub;
		assertThat(vBlubMin).isEqualTo(2397.4278774799686d);
		double vBlubRepro = (weight * Globals.reproMinSL / Globals.densBlub);
		assertThat(vBlubRepro).isEqualTo(4794.855754959937d);
		
		double eStorage = (vBlub - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eStorage).isEqualTo(2.718804568382919E8d);
		double eRepoMin = (vBlubRepro - vBlubMin) * Globals.densBlub * Globals.EDLip;
		assertThat(eRepoMin).isEqualTo(8.712252906762207E7d);

		/*
with-lact-calf? = TRUE
mass-struct-calf: 3.245773366118277   v-blub-calf: 2112.704109549823   dens-blub: 9.2E-4
mass-calf: 5.189461146904114
mass-calf: 5.189461146904114  calf-idl-SL: 0.375
v-blub-calf-idl: 2115.26948922722
		 */
		boolean withLactCalf = true;
		if (withLactCalf) {
			// NOT TESTED
			double massStructCalf = 3.245773366118277d;
			double vBlubCalf = 2112.704109549823d;
			double massCalf = massStructCalf + (vBlubCalf * Globals.densBlub);
			assertThat(massCalf).isEqualTo(5.189461146904114d);
			double vBlubCalfIdl = (massCalf * Globals.calfIdlSL / Globals.densBlub);
			assertThat(vBlubCalfIdl).isEqualTo(2115.26948922722d);
		}
	}

}
