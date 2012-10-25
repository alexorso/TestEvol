package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.testevol.domain.Version;

public class CategoryC6 extends Category {

	public CategoryC6(Version oldVersion, Version version) {
		super(CategoryClassification.TESTADD_AE_RE, oldVersion, version);
	}

	public void init() {
		File buildDir = version.getBuildDir();
		
		File newTestOldProgramRuntimeError = new File(buildDir, getRuntimeErrorResultFileName(NEW_TESTS, OLD_PROGRAM));
		File newTestOldProgramAssertFailure = new File(buildDir, getAssertFailureResultFileName(NEW_TESTS, OLD_PROGRAM));

		BufferedReader fileReaderNewTestOldProgramRuntimeError = null;
		BufferedReader fileReaderNewTestOldProgramAssertionFailure = null;

		try {
			String test = null;

			fileReaderNewTestOldProgramRuntimeError = new BufferedReader(
					new FileReader(newTestOldProgramRuntimeError));
			while ((test = fileReaderNewTestOldProgramRuntimeError.readLine()) != null) {
				addPossibleTest(test);
			}

			fileReaderNewTestOldProgramAssertionFailure = new BufferedReader(
					new FileReader(newTestOldProgramAssertFailure));
			while ((test = fileReaderNewTestOldProgramAssertionFailure
					.readLine()) != null) {
				addPossibleTest(test);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(fileReaderNewTestOldProgramRuntimeError);
			closeBufferedReader(fileReaderNewTestOldProgramAssertionFailure);
		}
	}
}
