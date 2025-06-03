package dk.au.bios.porpoise.energetics.processbased;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import dk.au.bios.porpoise.Porpoise;
import dk.au.bios.porpoise.util.SimulationTime;

public class EnergeticsDebugCapture {

	public static boolean CAPTURE = false;
	
	private static PrintWriter outWriter;

	public static void init() throws IOException {
		if (!CAPTURE) {
			return;
		}
		var timestamp = "" + System.currentTimeMillis();
		outWriter = new PrintWriter(new BufferedWriter(new FileWriter("energeticsdebug_" + timestamp + ".csv")));
//		outWriter.printf("tick,porp,swimSpeed,dispersal,mLoco,dsMating,mPreg,massF%n");
//		outWriter = new PrintWriter(new BufferedWriter(new FileWriter("swimspeeddebug_" + timestamp + ".csv")));
//		outWriter.printf("tick,porp,swimSpeed,dispersal,presLogMov%n");

//		outWriter.printf("tick,porp,prevMov,presHeading,bathy,salinity,presAngleBase,presAngleBathy,presAngleSalinity,presAngleIter");
//		outWriter.printf(",presAngleRan,presAngle,prevAngle,presLogMovLength,presLogMovBathy,presLogMovSalinity");
//		outWriter.printf(",presLogMovRan,presLogMovIter,prevLogMov,presMov,moveDistance");

		outWriter.printf("tick,porp,prevMov,presHeading,bathy,salinity");
		outWriter.printf(",presLogMovLength,presLogMovBathy,presLogMovSalinity");
		outWriter.printf(",presLogMovRan,presLogMovIter,prevLogMov,presMov,moveDistance,swimSpeed,dispersalMode");		
		outWriter.printf("%n");
	}

	public static void writeSwimspeed(Porpoise porp, double swimSpeed) {
		if (!CAPTURE) {
			return;
		}
		double tick = SimulationTime.getTick();
//		outWriter.printf("%.0f,%d,%f,%d,%f%n", 
//				tick, 
//				porp.getId(), 
//				swimSpeed, 
//				porp.getDispersalMode(), 
//				porp.getPresLogMov());
//		outWriter.flush();
	}

	public static void writeStdMov2(long porpId, double prevMov, double presHeading, double bathy, double salinity,
			double presLogMovLength, double presLogMovBathy,
			double presLogMovSalinity, double presLogMovRan, int presLogMovIter, double prevLogMov, double presMov,
			double moveDistance, double swimSpeed, int dispersalMode) {
		if (!CAPTURE) {
			return;
		}
		double tick = SimulationTime.getTick();
		outWriter.printf("%.0f,%d,%f,%f,%f,%f,%f,%f,%f,%f,%d,%f,%f,%f,%f,%d%n", 
				tick, 
				porpId, 
				prevMov, 
				presHeading,
				bathy,
				salinity,
				presLogMovLength,
				presLogMovBathy,
				presLogMovSalinity,
				presLogMovRan,
				presLogMovIter,
				prevLogMov,
				presMov,
				moveDistance,
				swimSpeed,
				dispersalMode
				);
		outWriter.flush();
	}

	public static void writeStdMov(long porpId, double prevMov, double presHeading, double bathy, double salinity,
			double presAngleBase, double presAngleBathy, double presAngleSalinity, int presAngleIter,
			double presAngleRan, double presAngle, double prevAngle, double presLogMovLength, double presLogMovBathy,
			double presLogMovSalinity, double presLogMovRan, int presLogMovIter, double prevLogMov, double presMov,
			double moveDistance) {
		if (!CAPTURE) {
			return;
		}
		double tick = SimulationTime.getTick();
		outWriter.printf("%.0f,%d,%f,%f,%f,%f,%f,%f,%f,%d,%f,%f,%f,%f,%f,%f,%f,%d,%f,%f,%f%n", 
				tick, 
				porpId, 
				prevMov, 
				presHeading,
				bathy,
				salinity,
				presAngleBase,
				presAngleBathy,
				presAngleSalinity,
				presAngleIter,
				presAngleRan,
				presAngle,
				prevAngle,
				presLogMovLength,
				presLogMovBathy,
				presLogMovSalinity,
				presLogMovRan,
				presLogMovIter,
				prevLogMov,
				presMov,
				moveDistance
				);
		outWriter.flush();
	}

	
	public static void writePorp(Porpoise porp) {
		if (!CAPTURE) {
			return;
		}
//		double tick = SimulationTime.getTick();
//		var energetics = (CaraEnergetics) porp.getEnergetics();
//		outWriter.printf("%.0f,%d,%f,%d,%f,%d,%f,%f%n", 
//				tick, 
//				porp.getId(), 
//				energetics.swimSpeed, 
//				porp.getDispersalMode(), 
//				energetics.mLoco,
//				energetics.dsMating,
//				energetics.mPreg,
//				energetics.massF);
//		outWriter.flush();
	}

}
