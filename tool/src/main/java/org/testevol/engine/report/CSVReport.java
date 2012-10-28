package org.testevol.engine.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.testevol.domain.Version;
import org.testevol.engine.domain.CategoryC1;
import org.testevol.engine.domain.CategoryC5;
import org.testevol.engine.domain.CategoryC8;
import org.testevol.engine.domain.Results;

public class CSVReport {

	public static void generateReport(Version oldVersion,  Version version,
			Results results, File reportsDir) throws IOException {
		
		File csvResults = new File(reportsDir, "results.txt");
		
		//File c1Results = new File(version.getDirectory().getParent(), "c1.txt");
		boolean fileAlreadyExists = csvResults.exists();

		if (!fileAlreadyExists) {
			csvResults.createNewFile();
		}
//		if (!c1Results.exists()) {
//			c1Results.createNewFile();
//		}
		FileWriter fileWriter = new FileWriter(csvResults, true);
		//FileWriter fileWriterC1 = new FileWriter(c1Results, true);

		String lineSeparator = System.getProperty("line.separator");
		if (fileAlreadyExists) {
			fileWriter.append(lineSeparator);
		}

		fileWriter.append(oldVersion.getName()).append(",");
		
		fileWriter.append(version.getName()).append(",");
		CategoryC1 categoryC1 = results.getCategoryC1();
		
		fileWriter.append(String.valueOf(categoryC1.getNumberOfTests())).append(",");//C1		
		
		fileWriter.append(String.valueOf(results.getCategoryC2().getNumberOfTests())).append(",");//C2
		fileWriter.append(String.valueOf(results.getCategoryC3().getNumberOfTests())).append(",");//C3
		fileWriter.append(String.valueOf(results.getCategoryC4().getNumberOfTests())).append(",");//C4
		
		CategoryC5 categoryC5 = results.getCategoryC5();
		int sameCoverage = categoryC5.getNumberOfTestsWithSameCoverage(); 
		int diffCoverage = categoryC5.getNumberOfTests() - sameCoverage;	
		fileWriter.append(String.valueOf(sameCoverage)).append(",");//C5
		fileWriter.append(String.valueOf(diffCoverage)).append(",");//C5
		
		fileWriter.append(String.valueOf(results.getCategoryC6().getNumberOfTests())).append(",");//C6
		fileWriter.append(String.valueOf(results.getCategoryC7().getNumberOfTests())).append(",");//C7
		
		CategoryC8 categoryC8 = results.getCategoryC8();
		sameCoverage = categoryC8.getNumberOfTestsWithSameCoverage(); 
		diffCoverage = categoryC8.getNumberOfTests() - sameCoverage;	
		fileWriter.append(String.valueOf(sameCoverage)).append(",");//C8
		fileWriter.append(String.valueOf(diffCoverage));//C8

		fileWriter.append("\n");
		fileWriter.append(categoryC1.toString());
		fileWriter.append(results.getCategoryC2().toString());
		fileWriter.append(results.getCategoryC3().toString());
		fileWriter.append(results.getCategoryC4().toString());
		fileWriter.append(categoryC5.toString());
		fileWriter.append(results.getCategoryC6().toString());
		fileWriter.append(results.getCategoryC7().toString());
		fileWriter.append(categoryC8.toString());
		
		fileWriter.close();
	//	fileWriterC1.close();

	}
}
