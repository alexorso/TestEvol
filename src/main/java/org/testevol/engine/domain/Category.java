package org.testevol.engine.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testevol.domain.Version;
import org.testevol.engine.TestResult;
import org.testevol.engine.TestResult.TestOutcome;

public abstract class Category {

	protected static boolean OLD_TESTS = true;
	protected static boolean NEW_TESTS = false;
	protected static boolean OLD_PROGRAM = true;
	protected static boolean NEW_PROGRAM = false;

	protected Version oldVersion;
	protected Version version;
	
	
	private CategoryClassification classification;
	private Set<String> possibleTestsOnThisCategory;
	private Set<String> testsOnThisCategory;

	protected Category(CategoryClassification classification, Version oldVersion, Version version) {
		this.classification = classification;
		this.oldVersion = oldVersion;
		this.version = version;
		this.possibleTestsOnThisCategory = new HashSet<String>();
		this.testsOnThisCategory = new HashSet<String>();
		init();
	}
	
	

	protected abstract void init();
	
	public CategoryClassification getClassification() {
		return classification;
	}


	protected void closeBufferedReader(BufferedReader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (IOException e) {
		}

	}

	public int getNumberOfTests(){
		return testsOnThisCategory.size();
	}

	public boolean isACandidateTest(String test) {
		test = removeParenthesis(test);
		return possibleTestsOnThisCategory.contains(test);
	}

	public void confirmTestOnThisCategory(String test) {
		test = removeParenthesis(test);
		testsOnThisCategory.add(test);
	}

	public void removeTestsFromThisCategory(List<String> tests) {
		testsOnThisCategory.removeAll(tests);
	}

	protected void addPossibleTest(String test) {
		test = removeParenthesis(test);
		possibleTestsOnThisCategory.add(test);
	}
	
	protected String removeParenthesis(String test){
		if(test.contains("(")){
			test = test.substring(0, test.indexOf("("));
		}
		return test;
	}
	
	public Set<String> getTestsOnThisCategory() {
		return testsOnThisCategory;
	}

	protected String getAssertFailureResultFileName(boolean oldTest, boolean oldProgram) {
		return getResultFileName(TestOutcome.ASSERT_FAILURE, oldTest,
				oldProgram);
	}

	protected String getCompilationErrorResultFileName(boolean oldTest,
			boolean oldProgram) {
		return getResultFileName(TestOutcome.COMPILATION_ERROR, oldTest,
				oldProgram);
	}

	protected String getRuntimeErrorResultFileName(boolean oldTest,
			boolean oldProgram) {
		return getResultFileName(TestOutcome.RUNTIME_ERROR, oldTest, oldProgram);
	}

	protected String getPassResultFileName(boolean oldTest,
			boolean oldProgram) {
		return getResultFileName(TestOutcome.PASS, oldTest, oldProgram);
	}

	protected String getResultFileName(TestResult.TestOutcome testOutcome, boolean oldTest,
			boolean oldProgram) {
		StringWriter fileName = new StringWriter();
		fileName.append("data-testout-").append(testOutcome.toString())
				.append("-");
		if (oldTest) {
			fileName.append("T0");
		} else {
			fileName.append("T1");
		}

		if (oldProgram) {
			fileName.append("P0");
		} else {
			fileName.append("P1");
		}
		fileName.append(".txt");
		return fileName.toString();
	}
}
