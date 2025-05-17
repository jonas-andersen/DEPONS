package dk.au.bios.porpoise.energetics;

import dk.au.bios.porpoise.Globals;
import dk.au.bios.porpoise.landscape.LandscapeLoader;
import dk.au.bios.porpoise.landscape.MonthlyDataFile;
import dk.au.bios.porpoise.util.SimulationTime;

public class CaraEnergeticsDataFileListener implements MonthlyDataFile.Listener {

	@Override
	public void loaded(String filePrefx, String fileName) {
		if (!LandscapeLoader.TEMPERATURE_FILE_PREFIX.equals(filePrefx)) {
			return;
		}

//		var cellData = Globals.getCellData();
//		var meanTemp = cellData.calcMeanTemperature();
/*
  let xxxxx ((1 / 6.3661) * mean-temp) * (180 / pi)
  let yyyyy (cos xxxxx)
  set IR-temp-mod ((1 / 5)* yyyyy + 1)
 */
//		var xxxxx = ((1.0d / 6.3661d) * meanTemp) * (180.0d / Math.PI);
//		var yyyyy = Math.cos(Math.toRadians(xxxxx));
//		Globals.IRTempMod = ((1.0d / 5.0d)* yyyyy + 1.0d);// Globals.calculateIRTempMod();
		Globals.IRTempMod = Globals.calculateIRTempMod();
		System.out.println("IRTempMod.update: " + Globals.IRTempMod + " [" + SimulationTime.getDayOfYear() + "]");

//		System.out.println("meanTemp " + meanTemp);
//		System.out.println("Set IRTempMod to " + Globals.IRTempMod);
//		System.out.println("n");
	}

}
