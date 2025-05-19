package dk.au.bios.porpoise.energetics.processbased;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PatchLookupTables {

	private static final String CSV_SEPARATOR = ",";
	private static final String EXPECTED_CSV_HEADER = "\"Salinity (g kg-1)\",\"-5\",\"-4\",\"-3\",\"-2\",\"-1\",\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\",\"10\",\"11\",\"12\",\"13\",\"14\",\"15\",\"16\",\"17\",\"18\",\"19\",\"20\",\"21\",\"22\",\"23\",\"24\",\"25\",\"26\",\"27\",\"28\",\"29\",\"30\",\"31\",\"32\",\"33\",\"34\",\"35\""; 
//	private static final String EXPECTED_CSV_HEADER = "-99,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25"; 
	private static final int EXPECTED_NUM_COLS = 42;
	private static final int ROW_OFFSET = 0; // 5;

	private static PatchLookupTables INSTANCE;
	
	private final double[][] densityTable;
	private final double[][] dynamicVisTable;
	
	public static void initialize() {
		INSTANCE = new PatchLookupTables();
	}
	public static PatchLookupTables getInstance() {
		return INSTANCE;
	}

	public PatchLookupTables() {
		densityTable = loadCsv("DensityTable.csv");
		dynamicVisTable = loadCsv("DynamicVis.csv");
	}
	
	public double[][] loadCsv(String file) {
		double[][] values = new double[41][41];

		try (var fr = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(file)))) {
			String header = fr.readLine();
			if (!EXPECTED_CSV_HEADER.equals(header)) {
				System.out.println("Expected: " + EXPECTED_CSV_HEADER);
				System.out.println("     Was: " + header);
				throw new RuntimeException("Unexpected file header: " + header);
			}

			String line = fr.readLine();
			int rowIdx = 0;
			while (line != null) {
				var cols = line.split(CSV_SEPARATOR);
				
				if (cols.length != EXPECTED_NUM_COLS) {
					System.err.println("" + rowIdx + ": " + line);
					throw new RuntimeException("Unexpected number of columns (" + cols.length + ") in row " + rowIdx);
				}
				if (Integer.parseInt(cols[0]) != ROW_OFFSET + rowIdx) {
					System.out.println("Expected value " + (ROW_OFFSET + rowIdx) + " but was " + Integer.parseInt(cols[0]));
					throw new RuntimeException("Unexpected first value in row " + rowIdx);
				}

				for (int i = 1; i < cols.length; i++) {
					values[rowIdx][i-1] = Double.parseDouble(cols[i]);
				}

				line = fr.readLine();
				rowIdx++;
			}

			return values;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public double getDensity(double salinity, double temperature) {
		return getValue(salinity, temperature, densityTable);
	}

	public double getDynamicVis(double salinity, double temperature) {
		return getValue(salinity, temperature, dynamicVisTable);
	}

	private double getValue(double salinity, double temperature, double[][] values) {
		// Homogeneous landscape! FIXME
		//if (temperature == -9999) temperature = 10;
		
		
		int salinityIndex = (int) Math.round(salinity) - ROW_OFFSET;
		int temperatureIndex = (int) Math.round(temperature) + 5;

		try {
			return values[salinityIndex][temperatureIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.printf("salinity: %f (%d), temperature %f (%d)", salinity, salinityIndex, temperature, temperatureIndex);
			throw e;
		}
	}
}
