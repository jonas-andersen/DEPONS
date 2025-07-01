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

import static dk.au.bios.porpoise.landscape.LandscapeLoader.FILE_EXT;

import java.io.IOException;

import dk.au.bios.porpoise.Globals;

public class RollingDateDataFile extends AbstractDataFile {

	private final CellDataSource source;
	private final RollingDateFile rollingDateFile;
	private volatile DataFileMetaData metadata;
	private volatile double[][] data = null;

	public RollingDateDataFile(String landscape, final String filePrefix, final CellDataSource source) throws IOException {
		super(landscape);

		this.source = source;
		this.rollingDateFile = new RollingDateFile(filePrefix, FILE_EXT, source);
	}

	public double[][] getData() throws IOException {
		if (rollingDateFile.shouldLoad()) {
			System.out.printf("Loading %s data from file %s%n", rollingDateFile.getFilePrefix(), rollingDateFile.getCurrentFile().fileName());
			metadata = source.getMetaData(rollingDateFile.getCurrentFile().fileName());
			data = source.getData(rollingDateFile.getCurrentFile().fileName());
			Globals.dataFileListener.ifPresent(l -> l.loaded(rollingDateFile.getFilePrefix(), rollingDateFile.getCurrentFile().fileName()));
		}

		return data;
	}

	public boolean isNoData(double dataValue) {
		return dataValue == metadata.getNoDataValue();
	}

	public RollingDateFile getRollingDateFile() {
		return rollingDateFile;
	}
}
