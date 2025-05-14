/*
 * Copyright (C) 2017-2025 Jacob Nabe-Nielsen <jnn@bios.au.dk>
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

package dk.au.bios.porpoise.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import dk.au.bios.porpoise.landscape.DataFileMetaData;

/**
 * Utility class to load data from ASCII (text) files.
 */
public final class ASCUtil {

	private static final String METADATA_NCOLS = "NCOLS"; 
	private static final String METADATA_NROWS = "NROWS";
	private static final String METADATA_XLLCORNER = "XLLCORNER";
	private static final String METADATA_YLLCORNER = "YLLCORNER";
	private static final String METADATA_CELLSIZE = "CELLSIZE";
	private static final String METADATA_NODATA = "NODATA_VALUE";

	private ASCUtil() {
		// Utility class, prevent instances.
	}

	public static double[][] loadDoubleAscFile(InputStream in) throws IOException {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
			var metadata = loadMetaData(reader);

			double[][] data = new double[metadata.getNcols()][metadata.getNrows()];
			int y = 0;
			String line;
			while ((line = reader.readLine()) != null) {
				final String[] points = line.split(" ");
				for (int x = 0; x < points.length; x++) {
					data[x][data[x].length - y - 1] = Double.parseDouble(points[x]);
				}
				y++;
			}
			return data;
		}
	}

	public static DataFileMetaData loadMetaData(InputStream in) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII))) {
			return loadMetaData(reader);
		}
	}

	private static DataFileMetaData loadMetaData(BufferedReader reader) throws IOException {
		try {
			int ncols = Integer.parseInt(extractFieldValue(METADATA_NCOLS, reader.readLine()));
			int nrows = Integer.parseInt(extractFieldValue(METADATA_NROWS, reader.readLine()));
			double xllcorner = Double.parseDouble(extractFieldValue(METADATA_XLLCORNER, reader.readLine()));
			double yllcorner = Double.parseDouble(extractFieldValue(METADATA_YLLCORNER, reader.readLine()));
			double cellsize = Double.parseDouble(extractFieldValue(METADATA_CELLSIZE, reader.readLine()));
			double noDataValue = Double.parseDouble(extractFieldValue(METADATA_NODATA, reader.readLine()));
	
			return new DataFileMetaData(ncols, nrows, xllcorner, yllcorner, cellsize, noDataValue, null); // Unknown CRS in ASC files
		} catch (NumberFormatException e) {
			throw new IOException("Invalid metadata", e);
		}
	}

	private static String extractFieldValue(String fieldName, String line) throws IOException {
		if (!line.toUpperCase().startsWith(fieldName)) {
			throw new IOException("Invalid metadata in file");
		}
		String value = line.substring(fieldName.length() + 1).trim();
		return value;
	}
}
