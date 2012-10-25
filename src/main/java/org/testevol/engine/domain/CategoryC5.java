package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testevol.domain.Version;
import org.testevol.engine.domain.coverage.Coverage;
import org.testevol.engine.domain.coverage.CoverageUtil;
import org.testevol.engine.driver.TestCoverageDriver;
import org.testevol.engine.util.Utils;

public class CategoryC5 extends Category {

	public Map<String, Boolean> coverageHasDecreased;
	public Map<String, List<String>> notCoveredLines;
	
	public CategoryC5(Version oldVersion, Version version) {
		super(CategoryClassification.TESTDEL_P, oldVersion, version);
		coverageHasDecreased = new HashMap<String, Boolean>();
		notCoveredLines = new HashMap<String, List<String>>();
	}

	public void init() {
		
		File buildDir = version.getBuildDir();
		
		File oldTestNewProgramPassedTests = new File(buildDir, getPassResultFileName(OLD_TESTS, NEW_PROGRAM));

		BufferedReader fileReaderOldTestNewProgramPassed = null;

		try {
			String test = null;

			fileReaderOldTestNewProgramPassed = new BufferedReader(
					new FileReader(oldTestNewProgramPassedTests));
			while ((test = fileReaderOldTestNewProgramPassed.readLine()) != null) {
				addPossibleTest(test);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(fileReaderOldTestNewProgramPassed);
		}
	}
	
	public void analyseCoverage() throws FileNotFoundException, IOException{
		if(getTestsOnThisCategory().isEmpty()){
			Utils.println("No tests in category C5, no coverage analysis needed.");
			return;
		}
		Utils.println(getTestsOnThisCategory().size()+" tests in category C5, starting coverage analysis.");
		
		CoverageUtil.deleteCoverageInfo();
		
		List<String> code = new ArrayList<String>();
		code.add(version.getCodeJar().getAbsolutePath());
		
		List<String> classpath = new ArrayList<String>();
		for(String path:version.getClassPath().split(File.pathSeparator)){
			if(path.endsWith(".jar")){
				classpath.add(path);
			}
		}
		
		String[] srcDirs = new String[]{version.getSourceDir().getAbsolutePath()};
		
		Coverage testSuiteCoverage = TestCoverageDriver.computeCoverage(	code,
																			srcDirs,
																			classpath,
																			Arrays.asList(version.getTestsJar().getAbsolutePath()),
																			null,
																			getTestsOnThisCategory(),
																			version.getDirectory().getAbsolutePath(),
																			true);
		
		System.out.println("\nTest Suite coverageCoverage:"+testSuiteCoverage.toString()+"\n");
        
		for(String test:getTestsOnThisCategory()){
			
	        CoverageUtil.deleteCoverageInfo();
			
			String testName = test.substring(test.lastIndexOf(".") + 1);
			
			String fullClassName = test.substring(0, test.lastIndexOf("."));			
			
			Utils.println("Running only method:"+fullClassName+"."+testName);
			
			Set testsToInclude = new HashSet();
	        testsToInclude.add(fullClassName+"."+testName);

        
			Coverage testCoverage = TestCoverageDriver.computeCoverage(	code,
																		srcDirs,
																		classpath,
																		Arrays.asList(oldVersion.getTestsJar().getAbsolutePath()),
																		testsToInclude,
																		null,
																		version.getDirectory().getAbsolutePath(),
																		true);

			List<String> notCoveredLines = hasDecreasedCoverage(testSuiteCoverage, testCoverage);
			coverageHasDecreased.put(test, 	notCoveredLines.isEmpty());
			this.notCoveredLines.put(test, notCoveredLines);
			System.out.println("\nCoverage:"+testCoverage.toString()+"\n");
		}
	}
	
	public int getNumberOfTestsWithSameCoverage(){
		int testsSameCoverage=0;
		for(String test:getTestsOnThisCategory()){
			if(!coverageHasDecreasedWhenExcludedTest(test)){
				testsSameCoverage++;
			}
		}
		
		return testsSameCoverage;
	}
	
	public List<String> hasDecreasedCoverage(Coverage testSuiteCoverage,
			Coverage testCoverage) {
		Set<String> linesCoveredByTestSuite = testSuiteCoverage.getCoveredLines();
		boolean hasDecreasedCoverage = false;
		List<String> notCoveredLines = new ArrayList<String>();
		for (String coveredLine : testCoverage.getCoveredLines()) {
			if (!linesCoveredByTestSuite.contains(coveredLine)) {
				notCoveredLines.add(coveredLine);
			}
		}

		return notCoveredLines;
	}
	
	public boolean coverageHasDecreasedWhenExcludedTest(String test){
		return coverageHasDecreased.containsKey(test) && coverageHasDecreased.get(test);
	}
	
	public Map<String, List<String>> getNotCoveredLines() {
		return notCoveredLines;
	}
}
