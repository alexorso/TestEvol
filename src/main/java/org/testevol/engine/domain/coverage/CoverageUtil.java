package org.testevol.engine.domain.coverage;

import java.io.File;

public class CoverageUtil {
	
	public static boolean deleteCoverageInfo(File destination) {
		File cobertura = new File(destination, "cobertura.ser");
		if (cobertura.exists()) {
			return cobertura.delete();
		}
		return true;
	}

}
