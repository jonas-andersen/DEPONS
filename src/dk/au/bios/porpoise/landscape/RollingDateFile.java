/*
 * Copyright (C) 2025 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.landscape;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import dk.au.bios.porpoise.util.SimulationTime;

public class RollingDateFile {

	private final String filePrefix;
	private final CellDataSource source;
	private final List<RollingFile> files;
	private Year currentYear;
	private RollingFile currentFile;
	private double lastLoadCheckTick = -9999.0d;

	public RollingDateFile(final String filePrefix, final String fileExtension, final CellDataSource source) throws IOException {
		this.filePrefix = filePrefix;
		this.source = source;

		final String pattern = getPatternForFile(filePrefix, fileExtension);
		Pattern p = Pattern.compile(pattern);

		var fileNames = source.getNamesMatching(pattern);
		files = new ArrayList<>(fileNames.size());

		for (String f : fileNames) {
			var m = p.matcher(f);
			if (!m.matches()) {
				throw new RuntimeException("Unexpected matching error for filename: " + f);
			}
			var yearStr = m.group(1);
			var monthStr = m.group(2);
			var dayStr = m.group(3);
			
			int year = Integer.parseInt(yearStr);
			int month = "XX".equals(monthStr) ? -1 : Integer.parseInt(monthStr);
			int day = "XX".equals(dayStr) ? -1 : Integer.parseInt(dayStr);

			var rf = new RollingFile(f, year, month, day);
			files.add(rf);
		}
		
		files.sort(RollingDateFile::compareDataFilesTime);

		var groupedByYear = files.stream().collect(Collectors.groupingBy(RollingFile::year));  // Not really great since not ordered by year...
		var yearIndex = groupedByYear.entrySet().stream().map(e -> {
			var groupedByMonth = e.getValue().stream().collect(Collectors.groupingBy(RollingFile::month));
			var months = groupedByMonth.entrySet().stream().map(me -> {
				return new Month(me.getKey(), me.getValue());
			}).collect(Collectors.toList());
			return new Year(e.getKey(), months);
		}).collect(Collectors.toList());

		// traverse in reverse, which ends up with the first year being the last item in the list
		yearIndex.sort(Comparator.comparingInt(Year::getYear).reversed());
		Year nextYear = yearIndex.get(0);
		for (Year y : yearIndex) {
			y.setNextYear(nextYear);
			nextYear = y;
		}
		currentYear = nextYear;
	}

	public String getFilePrefix() {
		return filePrefix;
	}

	public RollingFile getCurrentFile() {
		return currentFile;
	}

	public boolean shouldLoad() throws IOException {
		var now = SimulationTime.getTick();
		if (lastLoadCheckTick >= now) {
			return false;
		}
		
		lastLoadCheckTick = now;
		if (now >= currentYear.getNextYear().getStartTick()) {
			// Advance to next year
			currentYear = currentYear.getNextYear();
		}

		Month currentMonth;
		if (currentYear.getMonths().size() == 1) {
			currentMonth = currentYear.getMonths().get(0);
		} else {
			var monthOfYear = SimulationTime.getMonthOfYear();
			currentMonth = currentYear.getMonths().get(monthOfYear - 1);			
		}
		
		RollingFile file;
		if (currentMonth.files.size() == 1) {
			file = currentMonth.files.get(0);
		} else {
			var dayOfMonth = SimulationTime.getDayOfMonth();
			file = currentMonth.files.get(dayOfMonth);
		}

		if (!file.equals(currentFile)) {
			if (source.hasData(file.fileName())) {
				currentFile = file;
				return true;
			} else {
				throw new IOException(String.format("Could not load date from %s%n", file.fileName()));
			}
		}

		return false;
	}

	public static String getPatternForFile(String filePrefix, String fileExtension) {
		return "^" + filePrefix + "(\\d{4})_(\\d{2}|XX)_(\\d{2}|XX)(_.*)?\\" + fileExtension + "$";
	}

	public static class RollingFile {
		private String fileName;
		private final int year;
		private final int month;
		private final int day;
		

		public RollingFile(String fileName, int year, int month, int day) {
			this.fileName = fileName;
			this.year = year;
			this.month = month;
			this.day = day;
		}
		
		public int year() {
			return year;
		}
		
		public int month() {
			return month;
		}
		
		public int day() {
			return day;
		}
		
		public String fileName() {
			return fileName;
		}
	}

	public Year getYear() {
		return currentYear;
	}

	protected static int compareDataFilesTime(RollingFile f1, RollingFile f2) {
		if (f1.year < f2.year) {
			return -1;
		} else if (f1.year == f2.year) {
			if (f1.month < f2.month) {
				return -1;
			} else if (f1.month == f2.month) {
				if (f1.day < f2.day) {
					return -1;
				} else if (f1.day == f2.day) {
					throw new RuntimeException("Data file time clash. File1: " + f1.fileName + ", file2: " + f2.fileName);
				} else {
					return 1;
				}
			} else {
				return 1;
			}
		} else {
			return 1;
		}
	}

	public static class Year {
		private final int year;
		private final List<Month> months;
		private final long startTick;
		private Year nextYear;
		
		public Year(int year, List<Month> months) {
			if (!(months.size() == 1) && !(months.size() == 12)) {
				throw new RuntimeException("A year must have either a single or 12 monthly files");
			}
			this.year = year;
			this.months= months;

			this.startTick = SimulationTime.convertToTick(year, 1, 0);
			this.months.sort(Comparator.comparing(Month::getMonth));
		}

		public int getYear() {
			return year;
		}

		public List<Month> getMonths() {
			return months;
		}

		public long getStartTick() {
			return startTick;
		}

		public Year getNextYear() {
			return nextYear;
		}

		public void setNextYear(Year nextYear) {
			this.nextYear = nextYear;
		}

	}

	public static class Month {
		private final int month;
		private final List<RollingFile> files;

		public Month(int month, List<RollingFile> files) {
			super();
			this.month = month;
			this.files = files;
		}

		public int getMonth() {
			return month;
		}

		public List<RollingFile> getFiles() {
			return files;
		}

	}

}
