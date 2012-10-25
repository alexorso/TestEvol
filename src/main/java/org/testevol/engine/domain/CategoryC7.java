package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.testevol.domain.Version;

public class CategoryC7 extends Category {

	public CategoryC7(Version oldVersion, Version version) {
		super(CategoryClassification.TESTADD_CE, oldVersion, version);
	}

	public void init() {
		
		Set<String> testsInNewProgram = version.getTestsList();
		
		File newTestOldProgramCompilationError = new File(version.getBuildDir(), getCompilationErrorResultFileName(NEW_TESTS, OLD_PROGRAM));

		BufferedReader fileReaderNewTestOldProgramCompilationError = null;

		try {
			String test = null;

			fileReaderNewTestOldProgramCompilationError = new BufferedReader(
					new FileReader(newTestOldProgramCompilationError));
			while ((test = fileReaderNewTestOldProgramCompilationError
					.readLine()) != null) {
				if(test.endsWith(".*")){
					String testClass = test.substring(0, test.indexOf("*")); 
					for(String testAux:testsInNewProgram){
						if(testAux.startsWith(testClass)){
							addPossibleTest(testAux);
						}	
					}
				}
				else{
					addPossibleTest(test);					
				}

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(fileReaderNewTestOldProgramCompilationError);
		}

	}
}
