package dk.au.bios.porpoise.landscape;

public class NoDataException extends RuntimeException {

	private static final long serialVersionUID = -3369894133521318225L;

	public NoDataException(String data, String fileName, int cellX, int cellY) {
		super("NoData value for " + data + " in file " + fileName + " at [" + cellX + "," + cellY + "]");
	}
}
