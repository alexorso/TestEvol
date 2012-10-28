/**
 * 
 */
package org.testevol.engine;

import java.io.File;
import java.util.List;

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

	public DataAnalysis(String testevolConfigRoot, Project project,
			List<Version> versions, File executionFolder) {
		super();
		this.testevolConfigRoot = testevolConfigRoot;
		this.project = project;
		this.versions = versions;
		this.executionFolder = executionFolder;
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
		try {
			log = new TestEvolLog(new File(executionFolder, "log.txt"));

			log.logStrong("Starting Execution...");			
			for (Version version : versions) {
				log.log("Setting up version " + version.getName());
				version.setUp(new File(testevolConfigRoot));
			}
			Compiler compiler = new Compiler(versions, log);
			Runner runner = new Runner(versions, log);
			Differ differ = new Differ(versions, testevolConfigRoot, log);
			Classifier classifier = new Classifier(versions, differ, log);
			ReportGenerator reportGenerator = new ReportGenerator(versions,
					classifier, project.getName(), executionFolder, log);

			compiler.go();
			runner.go();
			differ.go();
			classifier.go();
			reportGenerator.go();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			long end = System.currentTimeMillis();
			if(log != null){
				log.addLine();
				log.addLineSeparator(25);
				log.logStrong("Total execution time: " + ((end - init) / 1000) + " seconds");
				log.addLineSeparator(25);
			}

		}
	}
}
