package org.testevol.engine.domain;

import java.io.File;
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

public class AddedTestMethods {

	private Set<String> addedTestMethods;
	private Version oldVersion;
	private Version version;
	private Map<String, Coverage> coveragesInfo;
	private int numberOfAddedTestMethodsThatCoverModifiedMethods;
	private int numberOfAddedTestMethodsThatCoverAddedMethods;
	private int numberOfAddedTestMethodsThatCoverAddedOrModifiedMethods;

	public AddedTestMethods(Version oldVersion, Version version,
			Set<String> addedMethods) {
		this.addedTestMethods = new HashSet();
		this.addedTestMethods.addAll(addedMethods);
		this.oldVersion = oldVersion;
		this.version = version;
	}

	public void analyseCoverage() {
		if (addedTestMethods.isEmpty()) {
			Utils.println("No test methods were added, no coverage analysis needed.");
			return;
		}
		Utils.print(addedTestMethods.size() + " test methods added ");

		CoverageUtil.deleteCoverageInfo();

		coveragesInfo = new HashMap<String, Coverage>();

		List<String> code = new ArrayList<String>();
		code.add(version.getCodeJar().getAbsolutePath());

		List<String> classpath = new ArrayList<String>();
		for (String path : version.getClassPath().split(File.pathSeparator)) {
			if (path.endsWith(".jar")) {
				classpath.add(path);
			}
		}

		String[] srcDirs = new String[] { version.getSourceDir()
				.getAbsolutePath() };
		numberOfAddedTestMethodsThatCoverModifiedMethods = 0;
		numberOfAddedTestMethodsThatCoverAddedMethods = 0;
		numberOfAddedTestMethodsThatCoverAddedOrModifiedMethods = 0;

		for (String test : addedTestMethods) {

			CoverageUtil.deleteCoverageInfo();// FileUtils.copyFile(new
												// File("cobertura.bk.ser"), new
												// File("cobertura.ser"));

			Set testsToInclude = new HashSet();
			testsToInclude.add(test);

			Coverage testCoverage = TestCoverageDriver.computeCoverage(code,
					srcDirs, classpath, Arrays.asList(version.getTestsJar()
							.getAbsolutePath()), testsToInclude, null, version
							.getDirectory().getAbsolutePath(), true);

			coveragesInfo.put(test, testCoverage);
			System.out.println("Coverage:" + testCoverage.toString());

		}
	}

	public void analyseCoverageBasedOnUpdatedMethods(
			Set<String> modifiedMethodsLines, Set<String> addedMethodsLines) {
		if (modifiedMethodsLines != null && modifiedMethodsLines.isEmpty()) {
			Utils.println("No modified methods, no coverage analysis needed.");
			return;
		}
		if (addedTestMethods.isEmpty()) {
			Utils.println("No test methods were added, no coverage analysis needed.");
			return;
		}
		Utils.print(addedTestMethods.size() + " test methods added ");
		Utils.println(modifiedMethodsLines.size()
				+ " modified methods, starting coverage analysis.");

		CoverageUtil.deleteCoverageInfo();

		coveragesInfo = new HashMap<String, Coverage>();

		List<String> code = new ArrayList<String>();
		code.add(version.getCodeJar().getAbsolutePath());

		List<String> classpath = new ArrayList<String>();
		for (String path : version.getClassPath().split(File.pathSeparator)) {
			if (path.endsWith(".jar")) {
				classpath.add(path);
			}
		}

		String[] srcDirs = new String[] { version.getSourceDir()
				.getAbsolutePath() };
		numberOfAddedTestMethodsThatCoverModifiedMethods = 0;
		numberOfAddedTestMethodsThatCoverAddedMethods = 0;
		numberOfAddedTestMethodsThatCoverAddedOrModifiedMethods = 0;

		for (String test : addedTestMethods) {

			CoverageUtil.deleteCoverageInfo();// FileUtils.copyFile(new
												// File("cobertura.bk.ser"), new
												// File("cobertura.ser"));

			Set testsToInclude = new HashSet();
			testsToInclude.add(test);

			Coverage testCoverage = TestCoverageDriver.computeCoverage(code,
					srcDirs, classpath, Arrays.asList(version.getTestsJar()
							.getAbsolutePath()), testsToInclude, null, version
							.getDirectory().getAbsolutePath(), true);

			coveragesInfo.put(test, testCoverage);

			boolean coversAModifiedMethod = coversAModifiedMethod(test,
					modifiedMethodsLines);
			boolean coversAnAddedMethod = coversAnAddedMethod(test,
					addedMethodsLines);

			if (coversAModifiedMethod) {
				numberOfAddedTestMethodsThatCoverModifiedMethods++;
			}
			if (coversAnAddedMethod) {
				numberOfAddedTestMethodsThatCoverAddedMethods++;
			}

			if (coversAModifiedMethod || coversAnAddedMethod) {
				numberOfAddedTestMethodsThatCoverAddedOrModifiedMethods++;
			}

			System.out.println("Coverage:" + testCoverage.toString());

		}
	}

	private boolean coversAModifiedMethod(String test,
			Set<String> modifiedMethodsLines) {
		Coverage coverage = coveragesInfo.get(test);
		for (String line : coverage.getCoveredLines()) {
			if (modifiedMethodsLines.contains(line)) {
				return true;
			}
		}
		return false;
	}

	private boolean coversAnAddedMethod(String test,
			Set<String> addedMethodsLines) {
		Coverage coverage = coveragesInfo.get(test);
		for (String line : coverage.getCoveredLines()) {
			if (addedMethodsLines.contains(line)) {
				return true;
			}
		}
		return false;
	}

	public int getNumberOfAddedTestMethodsThatCoverModifiedMethods() {
		return numberOfAddedTestMethodsThatCoverModifiedMethods;
	}

	public int getNumberOfAddedTestMethodsThatCoverAddedMethods() {
		return numberOfAddedTestMethodsThatCoverAddedMethods;
	}

	public Coverage getCoverage(String addedTest) {
		return coveragesInfo.get(addedTest);
	}

	public int getNumberOfAddedTests() {
		return addedTestMethods.size();
	}

}
