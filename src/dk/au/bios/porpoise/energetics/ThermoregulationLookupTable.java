package dk.au.bios.porpoise.energetics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import cern.jet.random.Normal;
import repast.simphony.random.RandomHelper;

public class ThermoregulationLookupTable {

	/*
	 * Create a four dimensional array keyed by the bins (bin index, not bin value).
	 * When populating from the CSV, convert each value to a bin-index.
	 * When retrieving, calculate the bin for each value.
	 * Then look up the value in the four-dimensional array based on the indexes.
	 */

	private static final String CSV_SEPARATOR = ",";
	private static final String EXPECTED_CSV_HEADER = "\"waterTemperature\",\"swimSpeed\",\"mass\",\"storageLevel\",\"meanThermo\",\"sdThermo\""; 

	public void loadCsv() {
//		var entries = new ArrayList<Entry>();
		try (var fr = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("ThermoregulationLookupTable.csv")))) {
			String header = fr.readLine();
			if (!EXPECTED_CSV_HEADER.equals(header)) {
				throw new RuntimeException("Unexpected file header: " + header);
			}

			String line = fr.readLine();
			while (line != null) {
				var cols = line.split(CSV_SEPARATOR);
				
				int widx = waterTempCsvToBin(cols[0]);
				int sidx = swimSpeedCsvToBin(cols[1]);
				int midx = massCsvToBin(cols[2]);
				int slidx = storageLevelCsvToBin(cols[3]);

				// TODO Check value not already assigned!
				if (lookupTable[widx][sidx][midx][slidx] != null) {
					throw new RuntimeException("duplicate entry");
				}
				// TODO Store object with meanThermo and sdThero (and potentially the associated RandomHelper.createXXXXX() object)
				lookupTable[widx][sidx][midx][slidx] = new Entry(Double.valueOf(cols[4]), Double.valueOf(cols[5]));
				
//				entries.add(new Entry(
//						Integer.valueOf(cols[0]),
//						Double.valueOf(cols[1]),
//						Integer.valueOf(cols[2]),
//						Double.valueOf(cols[3]),
//						Double.valueOf(cols[4]),
//						Double.valueOf(cols[5])));

				line = fr.readLine();
			}

			// Completeness check
			var missing = new ArrayList<String>();
			for (int i1 = 0; i1 < lookupTable.length; i1++) {
				for (int i2 = 0; i2 < lookupTable[i1].length; i2++) {
					for (int i3 = 0; i3 < lookupTable[i1][i2].length; i3++) {
						for (int i4 = 0; i4 < lookupTable[i1][i2][i3].length; i4++) {
							if (lookupTable[i1][i2][i3][i4] == null) {
								missing.add(String.format("missing: %d,%d,%d,%d", i1, i2, i3, i4));
							}
						}
					}
				}
			}
			if (missing.size() > 0) {
				System.out.println("Missing entries:");
				missing.stream().forEach(System.out::println);
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double getValueInBin(double waterTemp, double swimSpeed, int mass, double storageLevel) {
		return getEntryInBin(waterTemp, swimSpeed, mass, storageLevel).meanThermo;		
	}

	public Entry getEntryInBin(double waterTemp, double swimSpeed, int mass, double storageLevel) {
		int widx = waterTempValueToBin(waterTemp);
		int sidx = swimSpeedValueToBin(swimSpeed);
		int midx = massValueToBin(mass);
		int slidx = storageLevelValueToBin(storageLevel);
		
		return lookupTable[widx][sidx][midx][slidx];		
	}

	private static final int WATERTMP_MAX = 9;
	private static final int SWIMSPEED_MAX = 9;
	private static final int MASS_MAX = 9;
	private static final int STORAGELEVEL_MAX = 9;
	Entry[][][][] lookupTable = new Entry[WATERTMP_MAX][SWIMSPEED_MAX][MASS_MAX][STORAGELEVEL_MAX];
	
	private int waterTempValueToBin(double waterTemp) {
		// -5,  0,  5, 10, 15, 20, 25, 30, 35
		// [-∞, -2.5] (-2.5, 2.5] (2.5, 7.5] (7.5, 12.5] (12.5, 17.5] (17.5, 22.5] (22.5, 27.5] (27.5, 32.5] (32.5, ∞]
		if (waterTemp <= -2.5) {
			return 0;
		} else if (waterTemp <= 2.5) {
			return 1; 
		} else if (waterTemp <= 7.5) {
			return 2;
		} else if (waterTemp <= 12.5) {
			return 3;
		} else if (waterTemp <= 17.5) {
			return 4;
		} else if (waterTemp <= 22.5) {
			return 5;
		} else if (waterTemp <= 27.5) {
			return 6;
		} else if (waterTemp <= 32.5) {
			return 7;
		} else {
			return 8;
		}
	}

	private int waterTempCsvToBin(String waterTemp) {
		// -5,  0,  5, 10, 15, 20, 25, 30, 35
		// [-∞, -2.5] (-2.5, 2.5] (2.5, 7.5] (7.5, 12.5] (12.5, 17.5] (17.5, 22.5] (22.5, 27.5] (27.5, 32.5] (32.5, ∞]
		if ("-5".equals(waterTemp)) {
			return 0;
		} else if ("0".equals(waterTemp)) {
			return 1; 
		} else if ("5".equals(waterTemp)) {
			return 2;
		} else if ("10".equals(waterTemp)) {
			return 3;
		} else if ("15".equals(waterTemp)) {
			return 4;
		} else if ("20".equals(waterTemp)) {
			return 5;
		} else if ("25".equals(waterTemp)) {
			return 6;
		} else if ("30".equals(waterTemp)) {
			return 7;
		} else if ("35".equals(waterTemp)) {
			return 8;
		} else {
			throw new RuntimeException("Invalid csv value for waterTemp: " + waterTemp);
		}
	}

	private int swimSpeedValueToBin(double swimSpeed) {
		// 0.00, 0.47, 0.94, 1.41, 1.88, 2.35, 2.82, 3.29, 3.76
		// [-∞, 0.235] (0.235, 0.705] (0.705, 1.175] (1.175, 1.645] (1.645, 2.115] (2.115, 2.585] (2.585, 3.055] (3.055, 3.525] (3.525, ∞]
		if (swimSpeed <= 0.235) {
			return 0;
		} else if (swimSpeed <= 0.705) {
			return 1;
		} else if (swimSpeed <= 1.175) {
			return 2;
		} else if (swimSpeed <= 1.645) {
			return 3;
		} else if (swimSpeed <= 2.115) {
			return 4;
		} else if (swimSpeed <= 2.585) {
			return 5;
		} else if (swimSpeed <= 3.055) {
			return 6;
		} else if (swimSpeed <= 3.525) {
			return 7;
		} else {
			return 8;
		}
	}
	
	private int swimSpeedCsvToBin(String swimSpeed) {
		// 0.00, 0.47, 0.94, 1.41, 1.88, 2.35, 2.82, 3.29, 3.76
		// [-∞, 0.235] (0.235, 0.705] (0.705, 1.175] (1.175, 1.645] (1.645, 2.115] (2.115, 2.585] (2.585, 3.055] (3.055, 3.525] (3.525, ∞]
		if ("0".equals(swimSpeed)) {
			return 0;
		} else if ("0.47".equals(swimSpeed)) {
			return 1;
		} else if ("0.94".equals(swimSpeed)) {
			return 2;
		} else if ("1.41".equals(swimSpeed)) {
			return 3;
		} else if ("1.88".equals(swimSpeed)) {
			return 4;
		} else if ("2.35".equals(swimSpeed)) {
			return 5;
		} else if ("2.82".equals(swimSpeed)) {
			return 6;
		} else if ("3.29".equals(swimSpeed)) {
			return 7;
		} else if ("3.76".equals(swimSpeed)) {
			return 8;
		} else {
			throw new RuntimeException("Invalid csv value for swimSpeed: " + swimSpeed);
		}
	}
	
	private int massValueToBin(int mass) {
		// 10, 20, 30, 40, 50, 60, 70, 80, 90
		// [-∞, 15] (15, 25] (25, 35] (35, 45] (45, 55] (55, 65] (65, 75] (75, 85] (85, ∞]
		if (mass <= 15) {
			return 0;
		} else if (mass <= 25) {
			return 1;
		} else if (mass <= 35) {
			return 2;
		} else if (mass <= 45) {
			return 3;
		} else if (mass <= 55) {
			return 4;
		} else if (mass <= 65) {
			return 5;
		} else if (mass <= 75) {
			return 6;
		} else if (mass <= 85) {
			return 7;
		} else {
			return 8;
		}
	}

	private int massCsvToBin(String mass) {
		// 10, 20, 30, 40, 50, 60, 70, 80, 90
		// [-∞, 15] (15, 25] (25, 35] (35, 45] (45, 55] (55, 65] (65, 75] (75, 85] (85, ∞]
		if ("10".equals(mass)) {
			return 0;
		} else if ("20".equals(mass)) {
			return 1;
		} else if ("30".equals(mass)) {
			return 2;
		} else if ("40".equals(mass)) {
			return 3;
		} else if ("50".equals(mass)) {
			return 4;
		} else if ("60".equals(mass)) {
			return 5;
		} else if ("70".equals(mass)) {
			return 6;
		} else if ("80".equals(mass)) {
			return 7;
		} else if ("90".equals(mass)) {
			return 8;
		} else {
			throw new RuntimeException("Invalid csv value for mass: " + mass);
		}
	}

	private int storageLevelValueToBin(double storageLevel) {
		// 0.05, 0.0525, 0.055, 0.06, 0.08, 0.12, 0.20, 0.36, 0.68
    	// [-∞, 0.05125] (0.05125, 0.05375] (0.05375, 0.0575] (0.0575, 0.07] (0.07, 0.1] (0.1, 0.16] (0.16, 0.28] (0.28, 0.52] (0.52, ∞]
		if (storageLevel <= 0.05125) {
			return 0;
		} else if (storageLevel <= 0.05375) {
			return 1;
		} else if (storageLevel <= 0.0575) {
			return 2;
		} else if (storageLevel <= 0.07) {
			return 3;
		} else if (storageLevel <= 0.1) {
			return 4;
		} else if (storageLevel <= 0.16) {
			return 5;
		} else if (storageLevel <= 0.28) {
			return 6;
		} else if (storageLevel <= 0.52) {
			return 7;
		} else {
			return 8;
		}
	}
	
	private int storageLevelCsvToBin(String storageLevel) {
		// 0.05, 0.0525, 0.055, 0.06, 0.08, 0.12, 0.20, 0.36, 0.68
    	// [-∞, 0.05125] (0.05125, 0.05375] (0.05375, 0.0575] (0.0575, 0.07] (0.07, 0.1] (0.1, 0.16] (0.16, 0.28] (0.28, 0.52] (0.52, ∞]
		if ("0.05".equals(storageLevel)) {
			return 0;
		} else if ("0.0525".equals(storageLevel)) {
			return 1;
		} else if ("0.055".equals(storageLevel)) {
			return 2;
		} else if ("0.06".equals(storageLevel)) {
			return 3;
		} else if ("0.08".equals(storageLevel)) {
			return 4;
		} else if ("0.12".equals(storageLevel)) {
			return 5;
		} else if ("0.2".equals(storageLevel)) {
			return 6;
		} else if ("0.36".equals(storageLevel)) {
			return 7;
		} else if ("0.68".equals(storageLevel)) {
			return 8;
		} else {
			throw new RuntimeException("Invalid csv value for storageLevel: " + storageLevel);
		}
	}
	
	public static class Entry {
		private double meanThermo;
		private double sdThermo;
		private Normal normalRandom;

		private Entry(double meanThermo, double sdThermo) {
			this.meanThermo = meanThermo;
			this.sdThermo = sdThermo;
			// Not sure if anything really gained by instance over using static Normal.staticNextDouble(mean, sd)
			normalRandom = RandomHelper.createNormal(meanThermo, sdThermo);
		}		

		public double meanThermo() {
			return meanThermo;
		}

		public double sdThermo() {
			return sdThermo;
		}

		public double getValue() {
			return normalRandom.nextDouble();
		}
	}
}
