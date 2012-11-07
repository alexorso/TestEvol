/**
 * 
 */
package org.testevol.engine;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testevol.domain.Project;
import org.testevol.domain.Version;
import org.testevol.engine.domain.TestEvolLog;

/**
 * @author orso
 * @author leandro
 * 
 */
public class DataAnalysis {

	private String testevolConfigRoot;
	private Project project;
	private List<Version> versions;
	private File executionFolder;
	private boolean skipCoverageAnalysis;

	public DataAnalysis(String testevolConfigRoot, Project project,
			List<Version> versions, File executionFolder,
			boolean skipCoverageAnalysis) {
		super();
		this.testevolConfigRoot = testevolConfigRoot;
		this.project = project;
		this.versions = versions;
		this.executionFolder = executionFolder;
		this.skipCoverageAnalysis = skipCoverageAnalysis;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Better arguments' parsing
		if (args.length < 4) {
			System.err
					.println("Usage: DataAnalysis <tool root> <subject root> <version dirs regexp> <force rerun (true|false|clean)");
			System.exit(-1);
		}
		String toolroot = args[0];
		String subjroot = args[1];
		String regexpVersionNames = args[2];

		String projectName = System.getProperty("project.name");
		System.exit(0);
	}

	public void start() throws Exception {

		long init = System.currentTimeMillis();
		TestEvolLog log = null;
		boolean setUpSuccessFully = false;
		try {
			log = new TestEvolLog(new File(executionFolder, "log.txt"));

			log.logStrong("Starting Execution...");			
			for (Version version : versions) {
				File versionCopy = new File(executionFolder, version.getName());
				FileUtils.copyDirectory(version.getDirectory(), versionCopy);
				version.setDirectory(versionCopy);
				log.log("Setting up version " + version.getName());
				if(!version.setUp(new File(testevolConfigRoot))){
					log.logError("Error while setting up version "+version.getName());
					throw new RuntimeException();
				}
			}
			setUpSuccessFully = true;
			Compiler compiler = new Compiler(versions, log);
			Runner runner = new Runner(versions, log);
			Differ differ = new Differ(versions, testevolConfigRoot, log);
			Classifier classifier = new Classifier(versions, differ, log);
			ReportGenerator reportGenerator = new ReportGenerator(versions,
					classifier, project.getName(), executionFolder, log, skipCoverageAnalysis);

			compiler.go();
			runner.go();
			differ.go();
			classifier.go();
			reportGenerator.go();

		}finally {
			if(setUpSuccessFully){
				for (Version version : versions) {
					FileUtils.deleteDirectory(version.getDirectory());
				}				
			}
			long end = System.currentTimeMillis();
			if(log != null){
				log.addLine();
				log.addLineSeparator(25);
				log.logStrong("Total execution time: " + formatTime(((end - init) / 1000)));
				log.addLineSeparator(25);
			}
		}
	}
	
	private String formatTime(long time){
		long numberOfHours = time/36000;
		long remainder = time % 3600;
		long numberOfMinutes = remainder/60;
		long numberOfSeconds = remainder % 60;
		
		StringWriter formatedTime = new StringWriter();
		if(numberOfHours > 0){
			formatedTime.append(String.valueOf(numberOfHours)).append(" ").append(" hr");
			if(numberOfHours > 1){
				formatedTime.append("s");	
			}
			formatedTime.append(" ");
		}
		if(numberOfMinutes > 0){
			formatedTime.append(String.valueOf(numberOfMinutes)).append(" ").append(" min");
			if(numberOfMinutes > 1){
				formatedTime.append("s");	
			}
			formatedTime.append(" ");
		}
		if(numberOfSeconds > 0){
			formatedTime.append(String.valueOf(numberOfSeconds)).append(" ").append(" sec");
			if(numberOfMinutes > 1){
				formatedTime.append("s");	
			}
		}
		
		return formatedTime.toString();
	}
	
}
