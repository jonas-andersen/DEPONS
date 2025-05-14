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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

class RollingDateDataFileTest {

	@Test
	void basic() throws Exception {
		var fileNames = List.of(
				"prey0000_XX_XX.asc",
				"prey0001_XX_XX.asc",
				"prey0011_XX_XX.asc"
		);

		var cellDataSourceMock = mock(CellDataSource.class);
		when(cellDataSourceMock.getNamesMatching(any())).thenReturn(fileNames);
		
		RollingDateDataFile rddf = new RollingDateDataFile("unittest", "prey", cellDataSourceMock);
		assertThat(rddf.getRollingDateFile().getYear().getYear()).isEqualTo(0);

		assertThat(rddf.getRollingDateFile().getYear().getYear()).isEqualTo(0);
		assertThat(rddf.getRollingDateFile().getYear().getStartTick()).isEqualTo(0);

		assertThat(rddf.getRollingDateFile().getYear().getNextYear().getYear()).isEqualTo(1);
		assertThat(rddf.getRollingDateFile().getYear().getNextYear().getStartTick()).isEqualTo(17280);

		assertThat(rddf.getRollingDateFile().getYear().getNextYear().getNextYear().getYear()).isEqualTo(11);
		assertThat(rddf.getRollingDateFile().getYear().getNextYear().getNextYear().getStartTick()).isEqualTo(190080);
	}

	@Test
	void clashing() throws Exception {
		var fileNames = List.of(
				"prey0000_XX_XX.asc",
				"prey0000_XX_XX.asc"
		);

		var cellDataSourceMock = mock(CellDataSource.class);
		when(cellDataSourceMock.getNamesMatching(any())).thenReturn(fileNames);

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> new RollingDateDataFile("unittest", "prey", cellDataSourceMock));
	}

}
