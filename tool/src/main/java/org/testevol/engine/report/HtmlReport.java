package org.testevol.engine.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testevol.domain.Version;
import org.testevol.engine.domain.Category;
import org.testevol.engine.domain.CategoryC5;
import org.testevol.engine.domain.CategoryC8;
import org.testevol.engine.domain.Results;
import org.testevol.engine.report.js.JSFunction;
import org.testevol.engine.report.js.JavaCode;
import org.testevol.engine.util.Utils;

public class HtmlReport {

	private Version oldVersion;
	private Version version;
	private Results results;	

	public void generateReport(Version oldVersion, Version version,
			Results results, File reportsFolder) throws IOException {

		this.oldVersion = oldVersion;
		this.version = version;
		this.results = results;
		
		//prepareReportFolder(version);
		String script = createJavaScriptSummaryResult() + createJavaScriptFromTestsCode();
		
		File report = new File(reportsFolder, version.getName()+".js");
		report.createNewFile();
		FileOutputStream fos = new FileOutputStream(report);
		fos.write(script.getBytes());
		fos.close();
	}

	private String createJavaScriptSummaryResult() throws IOException {
//		File versionReportDir = new File(getReportFolder(version), version.getName());
//		
//		File resultJSFile = new File(versionReportDir, "result.js");
//		resultJSFile.createNewFile();
//
//		FileWriter fileWriter = new FileWriter(resultJSFile);

		double totalNumberOfTests = results.getTotalNumberOfTests();

		StringBuffer resultJS = new StringBuffer();
		resultJS.append("versionsSummary['"+version.getName()+"']="+Arrays.toString(getSummaryRawData(results)) + ";\n");
		
		resultJS.append(createJSFunction("getTotalOfTests", "return "
				+ ((int) totalNumberOfTests) + ";"));
		resultJS.append(createJSFunction("getCategories", "return "
				+ getTestsArray(results) + ";"));
		resultJS.append(createJSFunction("getVersionName",
				"return '" + version.getName() + "';"));
		resultJS.append(createJSFunction("getOldVersionName", "return '"
				+ oldVersion.getName() + "';"));

		
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter coverageLoss = new StringWriter();
		objectMapper.writeValue(coverageLoss, results.getCategoryC5().getNotCoveredLines());
		
		resultJS.append(createJSFunction("getCoverageLost", "return " + coverageLoss+ ";"));
		
		StringWriter coverageImprovement = new StringWriter();
		objectMapper.writeValue(coverageImprovement, results.getCategoryC8().getNewCoveredLines());
		resultJS.append(createJSFunction("getCoverageImprovement", "return " + coverageImprovement+ ";"));


//		fileWriter.write(resultJS.toString());
//		fileWriter.close();
		
		return resultJS.toString();
	}
	
	
	private String createJavaScriptFromTestsCode() throws IOException {
		
//		File versionReportDir = new File(getReportFolder(version),
//				version.getName());
//		
		//File resultJSFile = new File(versionReportDir, "java.js");
		//resultJSFile.createNewFile();

		//FileWriter fileWriter = new FileWriter(resultJSFile);

		StringBuffer resultJS = new StringBuffer();

		JSFunction javaCodeRepresentation = new JSFunction("loadJavaCodeRepresentation");

		writeJavaCode(results.getCategoryC1(), javaCodeRepresentation);
		writeJavaCode(results.getCategoryC2(), javaCodeRepresentation);
		
		
		resultJS.append(javaCodeRepresentation.toString());

		//fileWriter.write(resultJS.toString());
		//fileWriter.close();
	
		return resultJS.toString();
	}
	
	private void writeJavaCode(Category category, JSFunction javaCodeRepresentation) throws IOException{
		for(String test:category.getTestsOnThisCategory()){

			String classOfTheTest = getClassOfTheTest(test);
			
			String javaCodeOldVersionStr = "";
			
			try{
				JavaCode javaCodeOldVersion = new JavaCode(classOfTheTest, new File(oldVersion.getTestsSourceDir(),classOfTheTest.replaceAll("[.]", File.separator)+".java"));				
				javaCodeOldVersionStr= escapeMethod(javaCodeOldVersion.getMethodCode(test));
			}
			catch (Exception e) {
				//Probably this category do not have an old version
			}
			
			JavaCode javaCodeVersion = new JavaCode(classOfTheTest, new File(version.getTestsSourceDir(),classOfTheTest.replaceAll("[.]", File.separator)+".java"));

			
			StringBuffer statement = new StringBuffer();
			statement.append("tests.push(");
			statement.append("{name:'").append(test).append("',");
			statement.append("old:'").append(javaCodeOldVersionStr).append("',");
			statement.append("new:'").append(escapeMethod(javaCodeVersion.getMethodCode(test))).append("'});");
			
			javaCodeRepresentation.addStatement(statement.toString());
		}

	}
	
	private String escapeMethod(String method){
		return method.replaceAll("\n", "[TREX_NEW_LINE]").replaceAll("'", "\\\\'").replaceAll("\"", "\\\\\"");
	}

	public void generateIndex(String projectName, File reportFolder,
			List<Version> versions) throws IOException {

		File resultJSFile = new File(reportFolder, "summary.js");
		resultJSFile.createNewFile();
		FileWriter fileWriter = new FileWriter(resultJSFile);

		StringBuffer resultJS = new StringBuffer();

		resultJS.append(createJSFunction("getVersions", "return " + getVersions(versions) + ";"));
		resultJS.append(createJSFunction("getProjectName", "return '" + projectName + "';"));

		fileWriter.write(resultJS.toString());
		fileWriter.close();

	}
	
	private String getVersions(List<Version> versions) throws JsonGenerationException, JsonMappingException, IOException {
		List<String> versionsName = new ArrayList<String>();
		for(Version version:versions){
			versionsName.add(version.getName());
		}
		ObjectMapper objectMapper = new ObjectMapper();
		StringWriter versionJSObject = new StringWriter();
		objectMapper.writeValue(versionJSObject, versionsName);		
		return versionJSObject.toString();
	}
	
	private int[] getSummaryRawData(Results results){
		int[] resultsData = new int[8];
		for (int i = 0; i < resultsData.length; i++) {
			resultsData[i] = results.getCategories().get(i).getNumberOfTests();
		}
		
		return resultsData;
	}

	private String getTestsArray(Results results) {
		StringWriter div = new StringWriter();
		div.append("{");
		div.append("'"+results.getCategoryC1().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC1()));
		div.append(",");
		div.append("'"+results.getCategoryC2().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC2()));
		div.append(",");
		div.append("'"+results.getCategoryC2().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC2()));
		div.append(",");
		div.append("'"+results.getCategoryC3().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC3()));
		div.append(",");
		div.append("'"+results.getCategoryC4().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC4()));
		div.append(",");
		div.append("'"+results.getCategoryC5().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC5()));
		div.append(",");
		div.append("'"+results.getCategoryC6().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC6()));
		div.append(",");
		div.append("'"+results.getCategoryC7().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC7()));
		div.append(",");
		div.append("'"+results.getCategoryC8().getClassification().getLabel()+"':"+getTestsForCategory(results.getCategoryC8()));
		div.append("}");
		return div.toString();
	}
	
	private String getTestsForCategory(Category category){
		List<String> tests = new ArrayList<String>(
				category.getTestsOnThisCategory());

		Collections.sort(tests);

		if(tests.isEmpty()){
			return "[]";
		}
		
		StringWriter packagesObject = new StringWriter();
		StringWriter classesObject = new StringWriter();

		StringWriter testsObject = new StringWriter();
		String currPackageName ="";
		String currClassName ="";		
		
		for(String test:tests){
			String[] parts = getParts(test);//package,class,testName
			String packageName = parts[0];
			String className = parts[1];
			String testName = parts[2];
			
			if(!packageName.equals(currPackageName)){
				if(!currPackageName.isEmpty()){
					if(packagesObject.getBuffer().length() > 0){
						packagesObject.append(",");	
					}
					packagesObject.append("{'name':'"+currPackageName+"','value':["+classesObject+"]}");
				}
				classesObject = new StringWriter();
			}
			else{
				if(!className.equals(currClassName)){
					if(!currClassName.isEmpty()){
						if(classesObject.getBuffer().length() > 0){
							classesObject.append(",");	
						}
						classesObject.append("{'name':'"+currClassName+"','value':["+testsObject+"]}");	
					}
					testsObject = new StringWriter();
				}
			}
			currPackageName = packageName;
			currClassName = className;
			if(testsObject.getBuffer().length() > 0){
				testsObject.append(",");	
			}
			boolean goodCoverage = true;
			if(category instanceof CategoryC5){
				goodCoverage = !((CategoryC5)category).coverageHasDecreasedWhenExcludedTest(test); 
			}
			if(category instanceof CategoryC8){
				goodCoverage = ((CategoryC8)category).coverageHasIncreasedWhenIncludedTest(test); 
			}
			testsObject.append("{'name':'"+testName+"','good_coverage':"+goodCoverage+"}");
			
		}
		if(classesObject.getBuffer().length() > 0){
			classesObject.append(",");	
		}
		classesObject.append("{'name':'"+currClassName+"','value':["+testsObject+"]}");
		if(packagesObject.getBuffer().length() > 0){
			packagesObject.append(",");	
		}
		packagesObject.append("{'name':'"+currPackageName+"','value':["+classesObject+"]}");
		return "["+packagesObject.toString() +"]";		
	}

	private String[] getParts(String test) {
		String[] parts = new String[3];
		parts[2] = test.substring(test.lastIndexOf(".")+1,test.length());//testName
		test = test.substring(0,test.lastIndexOf("."));
		parts[1] = test.substring(test.lastIndexOf(".")+1,test.length());//className
		parts[0] = test.substring(0,test.lastIndexOf("."));//package
		return parts;
	}
	
	private String getClassOfTheTest(String test){
		return test.substring(0,test.lastIndexOf("."));
	}

	private String createJSFunction(String name, String body) {
		StringWriter jsFunction = new StringWriter();
		jsFunction.append("function " + name + "(){\n");
		jsFunction.append(body);
		jsFunction.append("\n}\n\n");
		return jsFunction.toString();
	}

	private void prepareIndexFolder(String sourceDirectory)
			throws IOException {
		
		File reportFolder = new File(sourceDirectory, "report");
		reportFolder = new File(sourceDirectory, "report");
		if(!reportFolder.exists()){
			reportFolder.mkdir();	
		}

		InputStream zipFile = HtmlReport.class.getResourceAsStream("report_files.zip");
		Utils.unzip(reportFolder, zipFile);
	}

	private void prepareReportFolder(Version version) throws IOException {
		// Utils.removeFilesRecursively(version.getFile("report"));

		File reportFolder = null;//getReportFolder(version);

		File reportDir = new File(reportFolder, version.getName());// version.getFile("report"
																	// +
																	// File.separator);
		if(reportDir.exists()){
			Utils.removeFilesRecursively(reportDir);
		}
		reportDir.mkdir();
		writeInputStremToFile(
				HtmlReport.class.getResourceAsStream("report.html"), new File(
						reportDir, "report.html"));

	}


	private void writeInputStremToFile(InputStream from, File to)
			throws IOException {

		if (!to.exists()) {
			to.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(to);

		byte[] data = new byte[1024];
		int readBytes = 0;

		while ((readBytes = from.read(data)) > 0) {
			fileWriter.write(new String(data, 0, readBytes));
		}
		fileWriter.close();
	}

}
