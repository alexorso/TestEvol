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
	private Version version;
	private Map<String, Coverage> coveragesInfo;

	public AddedTestMethods(Version oldVersion, Version version,
			Set<String> addedMethods) {
		this.addedTestMethods = new HashSet();
		this.addedTestMethods.addAll(addedMethods);
		this.version = version;
	}

	public void analyseCoverage() {
		if (addedTestMethods.isEmpty()) {
			Utils.println("No test methods were added, no coverage analysis needed.");
			return;
		}
		Utils.print(addedTestMethods.size() + " test methods added ");

		CoverageUtil.deleteCoverageInfo(version.getBuildDir());

		coveragesInfo = new HashMap<String, Coverage>();

		List<String> code = new ArrayList<String>();
		code.add(version.getCodeJar().getAbsolutePath());

		List<String> classpath = new ArrayList<String>();
		for (String path : version.getClassPath().split(File.pathSeparator)) {
			if (path.endsWith(".jar")) {
				classpath.add(path);
			}
		}

		String[] srcDirs = new String[] { version.getSourceDir().getAbsolutePath() };
		for (String test : addedTestMethods) {

			CoverageUtil.deleteCoverageInfo(version.getBuildDir());

			Set testsToInclude = new HashSet();
			testsToInclude.add(test);

			Coverage testCoverage = TestCoverageDriver.computeCoverage(code,
					srcDirs, classpath, Arrays.asList(version.getTestsJar().getAbsolutePath()), testsToInclude, null, version
							.getBuildDir().getAbsolutePath(), true);

			coveragesInfo.put(test, testCoverage);
			System.out.println("Coverage:" + testCoverage.toString());

		}
	}

	public Coverage getCoverage(String addedTest) {
		return coveragesInfo.get(addedTest);
	}

	public int getNumberOfAddedTests() {
		return addedTestMethods.size();
	}

}
