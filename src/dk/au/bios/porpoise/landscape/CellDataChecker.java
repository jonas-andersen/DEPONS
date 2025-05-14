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

public class CellDataChecker {

	public static boolean check(CellDataSource source) throws IOException {
		var bathyData = source.getData(LandscapeLoader.BATHY_FILE);
		
		var preyFileNames = source.getNamesMatching(RollingDateFile.getPatternForFile(LandscapeLoader.PREY_FILE_PREFIX, LandscapeLoader.FILE_EXT_ASC));
		var salinityFileNames = source.getNamesMatching(RollingDateFile.getPatternForFile(LandscapeLoader.SALINITY_FILE_PREFIX, LandscapeLoader.FILE_EXT_ASC));

		var filesToCheck = new ArrayList<String>(4 + preyFileNames.size() + salinityFileNames.size());
		filesToCheck.add(LandscapeLoader.BLOCKS_FILE);
		filesToCheck.add(LandscapeLoader.DISTTOCOAST_FILE);
		filesToCheck.add(LandscapeLoader.PATCHES_FILE);
		filesToCheck.add(LandscapeLoader.SEDIMENT_FILE);
		filesToCheck.addAll(preyFileNames);
		filesToCheck.addAll(salinityFileNames);

		boolean allFilesValid = true;
		for (String sourceFile : filesToCheck) {
			System.out.println("Checking " + sourceFile);
			var fileData = source.getData(sourceFile);
			if (!checkData(sourceFile, bathyData, fileData)) {
				System.err.println("Error in file: " + sourceFile);
				allFilesValid = false;
			}
		}

		return allFilesValid;
	}
	
	private static boolean checkData(String file, double[][] bathyData, double[][] fileData) {
		// verify fileData against bathyData
		for (int x = 0; x < bathyData.length; x++) {
			for (int y = 0; y < bathyData[x].length; y++) {
				if (bathyData[x][y] != -9999) {
					if (fileData[x][y] == -9999) {
						return false;
					}
				}
			}
		}
		
		return true;
	}

}
