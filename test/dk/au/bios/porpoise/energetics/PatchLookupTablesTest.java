package dk.au.bios.porpoise.energetics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import dk.au.bios.porpoise.energetics.processbased.PatchLookupTables;

class PatchLookupTablesTest {

	@Test
	void densityValues() {
		var table = new PatchLookupTables();

		assertThat(table.getDensity(5.0d, 0.0d)).isEqualTo(1003.91820793687d);
		assertThat(table.getDensity(7.0d, 0.0d)).isEqualTo(1005.53674543563d);
		assertThat(table.getDensity(30.0d, 0.0d)).isEqualTo(1024.07632685225d);

		assertThat(table.getDensity(15.0d, 10.0d)).isEqualTo(1011.38969327929d);
		assertThat(table.getDensity(15.0d, 18.0d)).isEqualTo(1010.02498979702d);
		assertThat(table.getDensity(15.0d, 25.0d)).isEqualTo(1008.30554868946d);

		// Borders
		assertThat(table.getDensity(4.50001d, 0.0d)).isEqualTo(1003.91820793687d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDensity(-0.5001d, -5.0d));

		assertThat(table.getDensity(30.49999d, 0.0d)).isEqualTo(1024.076327d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDensity(30.50001d, 0.0d));

		assertThat(table.getDensity(15.0d, 25.49999d)).isEqualTo(1008.305549d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDensity(15.0d, 25.50001d));

		assertThat(table.getDensity(15.0d, -0.49999d)).isEqualTo(1011.991073d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDensity(15.0d, -0.50001d));
	}

	@Test
	void dynamicVisValues() {
		var table = new PatchLookupTables();

		assertThat(table.getDynamicVis(5.0d, 0.0d)).isEqualTo(0.001805604d);
		assertThat(table.getDynamicVis(7.0d, 0.0d)).isEqualTo(0.001811468d);
		assertThat(table.getDynamicVis(30.0d, 0.0d)).isEqualTo(0.001887119d);

		assertThat(table.getDynamicVis(15.0d, 10.0d)).isEqualTo(0.001342057d);
		assertThat(table.getDynamicVis(15.0d, 18.0d)).isEqualTo(0.001083981d);
		assertThat(table.getDynamicVis(15.0d, 25.0d)).isEqualTo(0.000917882d);

		// Borders
		assertThat(table.getDynamicVis(4.50001d, 0.0d)).isEqualTo(0.001805604d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDynamicVis(4.49999d, 0.0d));

		assertThat(table.getDynamicVis(30.49999d, 0.0d)).isEqualTo(0.001887119d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDynamicVis(30.50001d, 0.0d));

		assertThat(table.getDynamicVis(15.0d, 25.49999d)).isEqualTo(0.000917882d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDynamicVis(15.0d, 25.50001d));

		assertThat(table.getDynamicVis(15.0d, -0.49999d)).isEqualTo(0.001836067d);
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> table.getDynamicVis(15.0d, -0.50001d));
	}

}
