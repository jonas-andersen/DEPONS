package dk.au.bios.porpoise.energetics;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import dk.au.bios.porpoise.util.SimulationTime;

public class ExtendedEnergyDebugCapture {

	private static ExtendedEnergyDebugCapture I;

	private Writer out;
	private EnergyIntake energyIntake;
	private Maintenance maintenance;
	private Thermoregulation thermoregulation;
	private Locomotion locomotion;
	private Pregnancy pregnancy;
	private Lactation lactation;
	private Growth growth;
	private General general;

	private ExtendedEnergyDebugCapture(Writer out) {
		this.out = out;
	}

	public static void init() throws IOException {
		if (true) {  // FIXME JONAS TEMP
			return;
		}
		var timestamp = "" + System.currentTimeMillis();
		var out = new PrintWriter(new BufferedWriter(new FileWriter("energeticsdebug_extended_" + timestamp + ".csv")));

		I = new ExtendedEnergyDebugCapture(out);
		I.writeHeader();
	}
	record EnergyIntake(double IRrecord, double foodavailable, double IRreal, double IRrealcalf) {
		static void writeHeader(Writer out) throws IOException {
			out.write("IRrecord,foodavailable,IRreal,IRrealcalf");
		}
		void writeData(Writer out) throws IOException {
			out.write(IRrecord +"," + foodavailable + "," + IRreal + "," + IRrealcalf);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",,,");
		}
	}
	record Maintenance(double mBMR) {
		static void writeHeader(Writer out) throws IOException {
			out.write("mBMR");
		}
		void writeData(Writer out) throws IOException {
			out.write(Double.toString(mBMR));
		}		
		static void writeEmptyData(Writer out) throws IOException {
			out.write("");
		}
	}
	record Thermoregulation(double mthermo) {
		static void writeHeader(Writer out) throws IOException {
			out.write("mthermo");
		}
		void writeData(Writer out) throws IOException {
			out.write(Double.toString(mthermo));
		}		
		static void writeEmptyData(Writer out) throws IOException {
			out.write("");
		}
	}
	record Locomotion(double mloco, double swimspeed) {
		static void writeHeader(Writer out) throws IOException {
			out.write("mloco,swimspeed");
		}
		void writeData(Writer out) throws IOException {
			out.write(mloco + "," + swimspeed);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",");
		}
	}
	record Pregnancy(byte pregnancystatus, int dsmating, double massf, double mgrowthg, double eheatgest, double mpreg) {
		static void writeHeader(Writer out) throws IOException {
			out.write("pregnancystatus,dsmating,massf,mgrowthg,eheatgest,mpreg");
		}
		void writeData(Writer out) throws IOException {
			out.write(pregnancystatus + "," + dsmating + "," + massf + "," + mgrowthg + "," + eheatgest + "," + mpreg);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",,,,,");
		}
	}
	record Lactation(double dsbirth, double masscalf, double massstructcalf, double IRrecordcalf,
			double vblubcalf, double mBMRcalf, double lgthcalf, double mthermocalf,
			double maxgrowthcalf, double mgrowthcalf, double mblubcalf, double ecalf, 
			double mlact, double vitalcosts, double vitalcostscalf) {
		
		static void writeHeader(Writer out) throws IOException {
			out.write("dsbirth,masscalf,massstructcalf,IRrecordcalf,vblubcalf,mBMRcalf,lgthcalf,mthermocalf,maxgrowthcalf,mgrowthcalf,mblubcalf,ecalf,mlact,vitalcosts,vitalcostscalf");
		}
		void writeData(Writer out) throws IOException {
			out.write(dsbirth + "," + masscalf + "," + massstructcalf + "," + IRrecordcalf + "," + vblubcalf + ","
					+ mBMRcalf + "," + lgthcalf + "," + mthermocalf + "," + maxgrowthcalf + "," + mgrowthcalf + ","
					+ mblubcalf + "," + ecalf + "," + mlact + "," + vitalcosts + "," + vitalcostscalf);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",,,,,,,,,,,,,,");
		}
	}
	record Growth(double maxgrow, double mgrowth, double growthrate, double massstruct, double lgth) {
		static void writeHeader(Writer out) throws IOException {
			out.write("maxgrow,mgrowth,growthrate,massstruct,lgth");
		}
		void writeData(Writer out) throws IOException {
			out.write(maxgrow + "," + mgrowth + "," + growthrate + "," + massstruct + "," + lgth);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",,,,");
		}
	}
	record General(long porpId, double mtot, double storagelevel, double SLmean, double vBlub, double vBlubRepro, double weight, double age, int calvesBorn, int calvesWeaned, int abortions) {
		static void writeHeader(Writer out) throws IOException {
			out.write("porpId,mtot,storagelevel,SLmean,vBlub,vBlubRepro,weight,age,calvesBorn,calvesWeaned,abortions");
		}
		void writeData(Writer out) throws IOException {
			out.write(porpId + "," + mtot + "," + storagelevel + "," + SLmean + "," + vBlub + "," + vBlubRepro + "," + weight + "," + age + "," + calvesBorn + "," + calvesWeaned + "," + abortions);
		}
		static void writeEmptyData(Writer out) throws IOException {
			out.write(",,,,,,,,,,");
		}
	}
	
	public static void captureEnergyIntake(double IRrecord, double foodavailable, double IRreal, double IRrealcalf) {
		if (shouldCapture()) {
			I.energyIntake = new EnergyIntake(IRrecord, foodavailable, IRreal, IRrealcalf);
		}
	}

	public static void captureMaintenance(double mBMR) {
		if (shouldCapture()) {
			I.maintenance = new Maintenance(mBMR);
		}
	}

	public static void captureThermoregulation(double mthermo) {
		if (shouldCapture()) {
			I.thermoregulation = new Thermoregulation(mthermo);
		}
	}
	
	public static void captureLocomotion(double mloco, double swimspeed) {
		if (shouldCapture()) {
			I.locomotion = new Locomotion(mloco, swimspeed);
		}
	}

	public static void capturePregnancy(byte pregnancystatus, int dsmating, double massf, double mgrowthg, double eheatgest, double mpreg) {
		if (shouldCapture()) {
			I.pregnancy = new Pregnancy(pregnancystatus, dsmating, massf, mgrowthg, eheatgest, mpreg);
		}
	}

	public static void captureLactation(double dsbirth, double masscalf, double massstructcalf, double IRrecordcalf,
			double vblubcalf, double mBMRcalf, double lgthcalf, double mthermocalf,
			double maxgrowthcalf, double mgrowthcalf, double mblubcalf, double ecalf, 
			double mlact, double vitalcosts, double vitalcostscalf) {
		if (shouldCapture()) {
			I.lactation = new Lactation(dsbirth, masscalf, massstructcalf, IRrecordcalf, vblubcalf, mBMRcalf, lgthcalf,
				mthermocalf, maxgrowthcalf, mgrowthcalf, mblubcalf, ecalf, mlact, vitalcosts, vitalcostscalf);
		}
	}

	public static void captureGrowth(double maxgrow, double mgrowth, double growthrate, double massstruct, double lgth) {
		if (shouldCapture()) {
			I.growth = new Growth(maxgrow, mgrowth, growthrate, massstruct, lgth);
		}
	}

	public static void captureGeneral(long porpId, double mtot, double storagelevel, double SLmean, double vBlub, double vBlubRepro, double weight, double age, int calvesBorn, int calvesWeaned, int abortions) {
		if (shouldCapture()) {
			I.general = new General(porpId, mtot, storagelevel, SLmean, vBlub, vBlubRepro, weight, age, calvesBorn, calvesWeaned, abortions);
		}
	}

	static boolean shouldCapture() {
		return false;
//		return SimulationTime.isBeginningOfWeek();
	}
	
	private void writeHeader() throws IOException {
		out.write("tick,month,year,");
		General.writeHeader(out);
		out.write(",");
		EnergyIntake.writeHeader(out);
		out.write(",");
		Maintenance.writeHeader(out);
		out.write(",");
		Thermoregulation.writeHeader(out);
		out.write(",");
		Locomotion.writeHeader(out);
		out.write(",");
		Pregnancy.writeHeader(out);
		out.write(",");
		Lactation.writeHeader(out);
		out.write(",");
		Growth.writeHeader(out);
		out.write(System.lineSeparator());
		out.flush();
	}

	public static void writeData() {
		if (shouldCapture()) {
			I.writeDataInternal();
		}
	}

	private void writeDataInternal() {
		try {
			out.write(SimulationTime.getTick() + "," + SimulationTime.getMonthOfYear() + "," + SimulationTime.getYearOfSimulation());
			out.write(",");

			if (general != null) general.writeData(out); else General.writeEmptyData(out);
			out.write(",");
			if (energyIntake != null) energyIntake.writeData(out); else EnergyIntake.writeEmptyData(out);
			out.write(",");
			if (maintenance != null) maintenance.writeData(out); else Maintenance.writeEmptyData(out);
			out.write(",");
			if (thermoregulation != null) thermoregulation.writeData(out); else Thermoregulation.writeEmptyData(out);
			out.write(",");
			if (locomotion != null) locomotion.writeData(out); else Locomotion.writeEmptyData(out);
			out.write(",");
			if (pregnancy != null) pregnancy.writeData(out); else Pregnancy.writeEmptyData(out);
			out.write(",");
			if (lactation != null) lactation.writeData(out); else Lactation.writeEmptyData(out);
			out.write(",");
			if (growth != null) growth.writeData(out); else Growth.writeEmptyData(out);
			out.write(System.lineSeparator());
			out.flush();
	
			general = null;
			energyIntake = null;
			maintenance = null;
			thermoregulation = null;
			locomotion = null;
			pregnancy = null;
			lactation = null;
			growth = null;
		} catch (IOException e) {
			throw new RuntimeException("Error writing energetics data.", e);
		}
	}

}

