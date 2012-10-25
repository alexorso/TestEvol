package org.testevol.engine.domain.coverage;

import java.io.File;

public class CoverageUtil {
	
	public static boolean deleteCoverageInfo() {
		File cobertura = new File("cobertura.ser");
		if (cobertura.exists()) {
			return cobertura.delete();
		}
		return true;
	}

}
