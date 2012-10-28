package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.testevol.domain.Version;

public class CategoryC3 extends Category {

	public CategoryC3(Version oldVersion, Version version) {
		super(CategoryClassification.TESTDEL_AE_RE, oldVersion, version);
	}

	public void init() {
		
		File buildDir = version.getBuildDir();
		
		File oldTestNewProgramRuntimeError = new File(buildDir, getRuntimeErrorResultFileName(OLD_TESTS, NEW_PROGRAM));
		File oldTestNewProgramAssertFailure = new File(buildDir, getAssertFailureResultFileName(OLD_TESTS, NEW_PROGRAM));

		BufferedReader fileReaderOldTestNewProgramRuntimeError = null;
		BufferedReader fileReaderOldTestNewProgramAssertionFailure = null;

		try {
			String test = null;

			fileReaderOldTestNewProgramRuntimeError = new BufferedReader(
					new FileReader(oldTestNewProgramRuntimeError));
			while ((test = fileReaderOldTestNewProgramRuntimeError.readLine()) != null) {
				addPossibleTest(test);
			}

			fileReaderOldTestNewProgramAssertionFailure = new BufferedReader(
					new FileReader(oldTestNewProgramAssertFailure));
			while ((test = fileReaderOldTestNewProgramAssertionFailure
					.readLine()) != null) {
				addPossibleTest(test);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(fileReaderOldTestNewProgramRuntimeError);
			closeBufferedReader(fileReaderOldTestNewProgramAssertionFailure);
		}
	}
}
