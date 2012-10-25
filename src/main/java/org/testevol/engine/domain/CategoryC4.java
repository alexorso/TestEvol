package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import org.testevol.domain.Version;

public class CategoryC4 extends Category {

	public CategoryC4(Version oldVersion, Version version) {
		super(CategoryClassification.TESTDEL_CE, oldVersion, version);
	}

	public void init() {
		
		File buildDir = version.getBuildDir();
		
		Set<String> testsInOldProgram = oldVersion.getTestsList();
		
		File oldTestNewProgramCompilationError = new File(buildDir, getCompilationErrorResultFileName(OLD_TESTS, NEW_PROGRAM));

		BufferedReader fileReaderOldTestNewProgramCompilationError = null;

		try {
			String test = null;

			fileReaderOldTestNewProgramCompilationError = new BufferedReader(new FileReader(oldTestNewProgramCompilationError));
			while ((test = fileReaderOldTestNewProgramCompilationError
					.readLine()) != null) {
				if(test.endsWith(".*")){
					String testClass = test.substring(0, test.indexOf("*")); 
					for(String testAux:testsInOldProgram){
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
			closeBufferedReader(fileReaderOldTestNewProgramCompilationError);
		}

	}
}
