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
import org.testevol.engine.util.Utils;

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
				log.log("Analysing coverage for pair "  + oldVersion.getName() + " - " + version.getName());
				CategoryC5 categoryC5 = results.getCategoryC5();
				if(categoryC5.getTestsOnThisCategory().isEmpty()){
					log.log("No tests in category TESTDEL (P), no coverage analysis needed.");
				}
				else{
					log.log(categoryC5.getTestsOnThisCategory().size()+" tests in category TESTDEL (P), starting coverage analysis for tests in this category.");
					categoryC5.analyseCoverage();
				}
				
	            //Set<String> modifiedMethodsLines = javaDiffCode.getSetOfLinesRelatedToModifiedMethods();
	            //Set<String> addedMethodsLines = javaDiffCode.getSetOfLinesRelatedToAddedMethods();
	            
//	        	AddedTestMethods addedTestMethods = new AddedTestMethods(oldVersion, version, results.getAddedMethods());
//	        	addedTestMethods.analyseCoverage();
	        	
				CategoryC8 categoryC8 = results.getCategoryC8();
				if(!categoryC8.getTestsOnThisCategory().isEmpty()){
					log.log(categoryC8.getTestsOnThisCategory().size()+" tests in category TESTADD (P), starting coverage analysis for tests in this category.");

		        	AddedTestMethods addedTestMethods = new AddedTestMethods(oldVersion, version, categoryC8.getTestsOnThisCategory());
		        	addedTestMethods.analyseCoverage();
		        	categoryC8.analyseCoverage(results,addedTestMethods);
				}
				else{
					log.log("No tests in category TESTADD (P), no coverage analysis needed.");
				}

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
            CSVReport.generateReport(oldVersion, version, results, reportsFolder, !skipCoverageAnalysis);
    		new HtmlReport().generateReport(oldVersion, version, results, reportsFolder);
        }
	
		return true;
	}
}
