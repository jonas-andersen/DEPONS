package dk.au.bios.porpoise.energetics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ThermoregulationTest {

	@Test
	void test() {
		var sut = new ThermoregulationLookupTable();
		sut.loadCsv();
		assertThat(sut.getValueInBin(10,3.29,50,0.68)).isEqualTo(-456145.842202059d);
		assertThat(sut.getValueInBin(11,3.31,53,0.67)).isEqualTo(-456145.842202059d);
		assertThat(sut.getValueInBin(20,1.41,40,0.055)).isEqualTo(136641.945285902);
		assertThat(sut.getValueInBin(10,3.29,50,0.68)).isEqualTo(-456145.842202059d);
		assertThat(sut.getValueInBin(10,3.29,50,0.68)).isEqualTo(-456145.842202059d);
		assertThat(sut.getValueInBin(30,0.94,70,0.08)).isEqualTo(-352706.232783901);
		assertThat(sut.getValueInBin(10,3.29,50,0.68)).isEqualTo(-456145.842202059d);
	}
	
}
