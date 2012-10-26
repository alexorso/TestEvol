/**
 * 
 */
package org.testevol.engine;

import java.io.File;
import java.util.List;

import org.testevol.domain.Project;
import org.testevol.domain.Version;

/**
 * @author orso
 * @author leandro
 * 
 */
public class DataAnalysis {

	private String testevolConfigRoot;
	private Project project;
	private List<Version> versions;
	private File reportFolder;
	
	
	public DataAnalysis(	String testevolConfigRoot, 
							Project project,
							List<Version> versions,
							File reportsFolder) {
		super();
		this.testevolConfigRoot = testevolConfigRoot;
		this.project = project;
		this.versions = versions;
		this.reportFolder = reportsFolder;
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
		
		for(Version version:versions){
			version.setUp(new File(testevolConfigRoot));
		}
		
		Compiler compiler = new Compiler(versions);
		Runner runner = new Runner(versions);
		Differ differ = new Differ(versions,testevolConfigRoot);
		Classifier classifier = new Classifier(versions, differ);
		ReportGenerator reportGenerator = new ReportGenerator(versions, classifier, project.getName(), reportFolder);

		boolean force = false;
		try {
			long init = System.currentTimeMillis();

			compiler.go(force);
			runner.go(force);
			differ.go(force);
			classifier.go(force);
			reportGenerator.go(force);
//
			long end = System.currentTimeMillis();

			System.out.println("\n\n TOTAL EXECUTION TIME:"
					+ ((end - init) / 1000) + "\n\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
