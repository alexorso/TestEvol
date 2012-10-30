package org.testevol.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testevol.domain.Version;
import org.testevol.engine.domain.AddedTestMethods;
import org.testevol.engine.domain.CategoryC5;
import org.testevol.engine.domain.CategoryC8;
import org.testevol.engine.domain.Results;
import org.testevol.engine.domain.TestEvolLog;
import org.testevol.engine.domain.VersionPair;
import org.testevol.engine.report.CSVReport;
import org.testevol.engine.report.HtmlReport;

public class ReportGenerator extends Task {

	private Classifier classifier;
	private String projectName;
	private File reportsFolder;
	private boolean skipCoverageAnalysis;
	
	public ReportGenerator(List<Version> versions, Classifier classifier,
			String projectName, File reportsFolder, TestEvolLog log,
			boolean skipCoverageAnalysis) {
		super(versions, log);
		this.classifier = classifier;
		this.projectName = projectName;
		this.reportsFolder = reportsFolder;
		this.skipCoverageAnalysis = skipCoverageAnalysis;
	}


	@Override
	public boolean go() throws Exception {
		Version oldVersion = null;

		Map<VersionPair, Results> resultsMap = classifier.getResults();
        List<VersionPair> versionPairs = new ArrayList<VersionPair>();
        
        if(skipCoverageAnalysis){
        	log.logStrong("Skipping coverage analysis...");
        }
        else{
        	log.logStrong("Starting coverage analysis...");
        }
        
        
		for (Version version:versions) {
			
			if (oldVersion == null) {
				oldVersion = version;
				continue;
			}

			VersionPair versionPair = new VersionPair(oldVersion, version);
			versionPairs.add(versionPair);
			Results results = resultsMap.get(versionPair);
			
			if(!skipCoverageAnalysis){
				log.log("Analysing covergae for pair "  + oldVersion.getName() + " - " + version.getName());
				CategoryC5 categoryC5 = results.getCategoryC5();
				categoryC5.analyseCoverage();
				
				CategoryC8 categoryC8 = results.getCategoryC8();

	            //Set<String> modifiedMethodsLines = javaDiffCode.getSetOfLinesRelatedToModifiedMethods();
	            //Set<String> addedMethodsLines = javaDiffCode.getSetOfLinesRelatedToAddedMethods();
	            
	        	AddedTestMethods addedTestMethods = new AddedTestMethods(oldVersion, version, results.getAddedMethods());
	        	addedTestMethods.analyseCoverage();
	        	
	        	
	            //AddedTestMethods addedTestMethods = new AddedTestMethods(oldVersion, version, categoryC8.getTestsOnThisCategory());
	        	//addedTestMethods.analyseCoverageBasedOnUpdatedMethods(modifiedMethodsLines, addedMethodsLines);
	        	//addedTestMethods.saveSummary();
	        	
	        	
	            categoryC8.analyseCoverage(results,addedTestMethods);

	           // TypesOfModificationsReport.generateReport(oldVersion, version, categoryC1);	
			}		
			oldVersion = version;
		}
		
        log.logStrong("Generating report...");

        new HtmlReport().generateIndex(projectName, reportsFolder,versions);
        
        for(int i=0;i < versionPairs.size();i++){
        	VersionPair versionPair = versionPairs.get(i);
        	oldVersion = versionPair.getOldVersion();        	
        	Version version = versionPair.getVersion();
        	log.log("Generating report for pair " + oldVersion.getName() + " - " + version.getName());
        	Results results = resultsMap.get(versionPair);
            CSVReport.generateReport(oldVersion, version, results, reportsFolder);
    		new HtmlReport().generateReport(oldVersion, version, results, reportsFolder);
        }
	
		return true;
	}
}
