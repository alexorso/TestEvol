package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.testevol.domain.Version;

public class CategoryC2 extends Category {

	public CategoryC2(Version oldVersion, Version version) {
		super(CategoryClassification.TESTMODNOTREP, oldVersion, version);
	}

	public void init() {
		
		File buildDir = version.getBuildDir();
		
		File oldTestNewProgram = new File(buildDir, getPassResultFileName(OLD_TESTS,NEW_PROGRAM));
		File newTestNewProgram = new File(buildDir,  getPassResultFileName(NEW_TESTS,NEW_PROGRAM));

		BufferedReader fileReaderOldTestNewProgram = null;
		BufferedReader fileReaderNewTestNewProgram = null;

		try {
			
			//This variable contains all the old tests that pass the new program
			Set<String> oldTestNewProgramPassedTests = new HashSet<String>();

			fileReaderOldTestNewProgram = new BufferedReader(new FileReader(
					oldTestNewProgram));
			String test = null;
			while ((test = fileReaderOldTestNewProgram.readLine()) != null) {
				oldTestNewProgramPassedTests.add(test);
			}

			fileReaderNewTestNewProgram = new BufferedReader(new FileReader(
					newTestNewProgram));
			while ((test = fileReaderNewTestNewProgram.readLine()) != null) {
				if (oldTestNewProgramPassedTests.contains(test)) {
					addPossibleTest(test);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileReaderNewTestNewProgram != null) {
					fileReaderNewTestNewProgram.close();
				}
			} catch (IOException e) {
			}
			try {
				if (fileReaderOldTestNewProgram != null) {
					fileReaderOldTestNewProgram.close();
				}
			} catch (IOException e) {
			}
		}

	}
}
