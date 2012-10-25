package org.testevol.engine;

import org.testevol.engine.diff.ClassCorrespondence;

public class JavaTestDifferencer extends JavaDifferencer{

	public JavaTestDifferencer( String configFile1, String configFile2, String exclFile ) {
		super(configFile1, configFile2, exclFile);
	}

	@Override
	protected ClassCorrespondence getClassCorrespondence() {
		return new ClassCorrespondence( classLoader1.getTestClasses(), classLoader2.getTestClasses());
	}
}
