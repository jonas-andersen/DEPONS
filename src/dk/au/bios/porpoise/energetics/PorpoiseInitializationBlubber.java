package dk.au.bios.porpoise.energetics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import repast.simphony.random.RandomHelper;

public class PorpoiseInitializationBlubber {

	private static final String CSV_SEPARATOR = ",";
	private static final String EXPECTED_CSV_HEADER = "\"Age\",\"StndL\",\"Weight\",\"Blubber.mass\",\"BlubPercent\",\"D1cm\",\"L1cm\",\"V1cm\",\"D2cm\",\"L2cm\",\"V2cm\",\"D3cm\",\"L3cm\",\"V3cm\",\"D4cm\",\"L4cm\",\"V4cm\",\"D5cm\",\"L5cm\",\"V5cm\""; 

	private List<Entry> entries = new ArrayList<>();

	PorpoiseInitializationBlubber(String csvFilename) {
		try (var fr = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(csvFilename)))) {
			String header = fr.readLine();
			if (!EXPECTED_CSV_HEADER.equals(header)) {
				throw new RuntimeException("Unexpected file header: " + header);
			}

			String line = fr.readLine();
			while (line != null) {
				var cols = line.split(CSV_SEPARATOR);

				double stndL = Double.valueOf(cols[1]);
				double mass = Double.valueOf(cols[2]);
				double blubberMass = Double.valueOf(cols[3]);
				// FIXME The CSV contains a lot more columns, they are no longer in use??

				var entry = new Entry(stndL, mass, blubberMass);
				entries.add(entry);

				line = fr.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Entry getEntry(int row) {
		return entries.get(row);
	}

	public Entry getRandomEntry() {
		var rowIdx = RandomHelper.nextIntFromTo(0, entries.size() - 1);
		return entries.get(rowIdx);
	}

	private static PorpoiseInitializationBlubber zero;
	private static PorpoiseInitializationBlubber one;
	private static PorpoiseInitializationBlubber two;
	private static PorpoiseInitializationBlubber three;
	private static PorpoiseInitializationBlubber four;
	private static PorpoiseInitializationBlubber five;
	private static PorpoiseInitializationBlubber six;
	private static PorpoiseInitializationBlubber sevenUp;
	
	public static void initialize() {
		zero = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Zero.csv");
		one = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_One.csv");
		two = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Two.csv");
		three = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Three.csv");
		four = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Four.csv");
		five = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Five.csv");
		six = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Six.csv");
		sevenUp = new PorpoiseInitializationBlubber("PorpoiseInitializationBlubber_Seven_up.csv");
	}

	static PorpoiseInitializationBlubber mapAgeToTable(int age) {
		if (age == 0) {
			return zero;
		} else if (age == 1) {
			return one;
		} else if (age == 2) {
			return two;
		} else if (age == 3) {
			return three;
		} else if (age == 4) {
			return four;
		} else if (age == 5) {
			return five;
		} else if (age == 6) {
			return six;
		} else {
			return sevenUp;
		}
	}

	public static Entry getEntry(int age, int row) {
		return mapAgeToTable(age).getEntry(row);
	}
	
	public static Entry getRandomEntry(int age) {
		return mapAgeToTable(age).getRandomEntry();
	}

	public static class Entry {
		public final double stndL;
		public final double mass;
		public final double blubberMass;

		public Entry(double stndL, double mass, double blubberMass) {
			this.stndL = stndL;
			this.mass = mass;
			this.blubberMass = blubberMass;
		}

	}
}
