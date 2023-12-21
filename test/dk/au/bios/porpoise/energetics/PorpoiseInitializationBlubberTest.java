package dk.au.bios.porpoise.energetics;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PorpoiseInitializationBlubberTest {

	@Test
	void test() {
		PorpoiseInitializationBlubber.initialize();

		assertThat(PorpoiseInitializationBlubber.getEntry(0, 0)).satisfies(entry -> {
			assertThat(entry.stndL).isEqualTo(1.14d);
			assertThat(entry.mass).isEqualTo(29.0d);
			assertThat(entry.blubberMass).isEqualTo(16.98d);
		});

		assertThat(PorpoiseInitializationBlubber.getEntry(0, 30)).satisfies(entry -> {
			assertThat(entry.stndL).isEqualTo(1.185d);
			assertThat(entry.mass).isEqualTo(26.6d);
			assertThat(entry.blubberMass).isEqualTo(6.34d);
		});

		var randomEntry = PorpoiseInitializationBlubber.getRandomEntry(0);
		assertThat(randomEntry).isNotNull();
	}
}
