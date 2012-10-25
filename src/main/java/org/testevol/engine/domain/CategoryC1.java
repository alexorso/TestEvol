package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.testevol.domain.Version;
import org.testevol.engine.TestResult.TestOutcome;

public class CategoryC1 extends Category{
	
	private Set<String> assertionFailures;
	private Set<String> compilationFailures;
	private Set<String> runtimeFailures;

	public CategoryC1(Version oldVersion, Version version) {
		super(CategoryClassification.TESTREP, oldVersion, version);
	}

	
	public void init() {

		assertionFailures = new HashSet<String>();
		compilationFailures = new HashSet<String>();
		runtimeFailures = new HashSet<String>();
		
		File buildDir = version.getBuildDir();
		
		//Get all old tests that fail in the new program
		File oldTestNewProgramCompilationError = new File(buildDir, getCompilationErrorResultFileName(OLD_TESTS, NEW_PROGRAM));
		File oldTestNewProgramRuntimeError = new File(buildDir, getRuntimeErrorResultFileName(OLD_TESTS, NEW_PROGRAM));
		File oldTestNewProgramAssertFailure = new File(buildDir, getAssertFailureResultFileName(OLD_TESTS,NEW_PROGRAM));

		//get all tests that passe in the new program
		File newTestNewProgram = new File(buildDir, getPassResultFileName(NEW_TESTS, NEW_PROGRAM));

		BufferedReader fileReaderOldTestNewProgramCompilationError = null;
		BufferedReader fileReaderOldTestNewProgramRuntimeError = null;
		BufferedReader fileReaderOldTestNewProgramAssertionFailure = null;
		BufferedReader fileReaderNewTestNewProgram = null;

		try {
			Set<String> newTestNewProgramPassedTests = new HashSet<String>();

			fileReaderNewTestNewProgram = new BufferedReader(new FileReader(
					newTestNewProgram));
			String test = null;
			while ((test = fileReaderNewTestNewProgram.readLine()) != null) {
				newTestNewProgramPassedTests.add(test);
			}

			fileReaderOldTestNewProgramCompilationError = new BufferedReader(
					new FileReader(oldTestNewProgramCompilationError));
			while ((test = fileReaderOldTestNewProgramCompilationError.readLine()) != null) {
				if (newTestNewProgramPassedTests.contains(test)) {
					addPossibleTest(test, TestOutcome.COMPILATION_ERROR);
				}
			}

			fileReaderOldTestNewProgramRuntimeError = new BufferedReader(
					new FileReader(oldTestNewProgramRuntimeError));
			while ((test = fileReaderOldTestNewProgramRuntimeError.readLine()) != null) {
				if (newTestNewProgramPassedTests.contains(test)) {
					addPossibleTest(test, TestOutcome.RUNTIME_ERROR);
				}
			}

			fileReaderOldTestNewProgramAssertionFailure = new BufferedReader(
					new FileReader(oldTestNewProgramAssertFailure));
			while ((test = fileReaderOldTestNewProgramAssertionFailure
					.readLine()) != null) {
				if (newTestNewProgramPassedTests.contains(test)) {
					addPossibleTest(test, TestOutcome.RUNTIME_ERROR);
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeBufferedReader(fileReaderOldTestNewProgramCompilationError);
			closeBufferedReader(fileReaderOldTestNewProgramRuntimeError);
			closeBufferedReader(fileReaderOldTestNewProgramAssertionFailure);
			closeBufferedReader(fileReaderNewTestNewProgram);
		}
	}

	public int getNumberOfAssertionFailures(){
		int count = 0;
		for(String test:getTestsOnThisCategory()){
			if(assertionFailures.contains(test)){
				count++;
			}
		}
		return count;
	}

	public int getNumberOfCompilationFailures(){
		int count = 0;
		for(String test:getTestsOnThisCategory()){
			if(compilationFailures.contains(test)){
				count++;
			}
		}
		return count;
	}
	
	public int getNumberOfRuntimeFailures(){
		int count = 0;
		for(String test:getTestsOnThisCategory()){
			if(runtimeFailures.contains(test)){
				count++;
			}
		}
		return count;
	}		
	
	private void addPossibleTest(String test, TestOutcome testOutcome) {
		super.addPossibleTest(test);
		test = removeParenthesis(test);
		switch (testOutcome) {
		case RUNTIME_ERROR:
			runtimeFailures.add(test);
			break;
		case ASSERT_FAILURE:
			assertionFailures.add(test);
			break;
		case COMPILATION_ERROR:
			compilationFailures.add(test);
			break;
		default:
			break;
		}
	}

	public void confirmTestOnThisCategory(String test, int changeType) {
		super.confirmTestOnThisCategory(test);
	}

	
	///////////////////////////////////////////
	
	
	
	private int numberOfMethodCallAdded = 0;
	private int numberOfMethodCallDeleted = 0;
	private int numberOfMethodCallModified= 0;
	private int numberOfMethodCallParameterAdded = 0;
	private int numberOfMethodCallParameterDeleted = 0;
	
	private int numberOfAssertionAdded = 0;
	private int numberOfAssertionDeleted = 0;
	private int numberOfAssertionModified = 0;
	private int numberOfAssertionExpectedValueModified = 0;
	private int numberOfAssertionChangesdOnly = 0;
	
	public int getNumberOfMethodCallAdded() {
		return numberOfMethodCallAdded;
	}


	public int getNumberOfMethodCallDeleted() {
		return numberOfMethodCallDeleted;
	}


	public int getNumberOfMethodCallModified() {
		return numberOfMethodCallModified;
	}


	public int getNumberOfMethodCallParameterAdded() {
		return numberOfMethodCallParameterAdded;
	}


	public int getNumberOfMethodCallParameterDeleted() {
		return numberOfMethodCallParameterDeleted;
	}

	public int getNumberOfAssertionAdded() {
		return numberOfAssertionAdded;
	}

	public int getNumberOfAssertionDeleted() {
		return numberOfAssertionDeleted;
	}


	public int getNumberOfAssertionModified() {
		return numberOfAssertionModified;
	}


	public int getNumberOfAssertionExpectedValueModified() {
		return numberOfAssertionExpectedValueModified;
	}


	public int getNumberOfAssertionChangesdOnly() {
		return numberOfAssertionChangesdOnly;
	}
	
}
