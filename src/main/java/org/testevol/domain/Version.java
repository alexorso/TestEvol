package org.testevol.domain;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testevol.engine.util.Utils;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

public class Version {

	private File versionDir;
	private File buildDir;
	private String name;
	private Properties properties;

	// Used in case of a maven project
	private Model mavenModel;

	public Version(File versionDir) throws Exception {
		super();
		this.versionDir = versionDir;
		this.buildDir = new File(versionDir, "build");
		name = versionDir.getName();
		properties = new Properties();
		if (getPropertiesFile().exists()) {
			properties.load(new FileInputStream(getPropertiesFile()));
		}
		loadConfiguration();
	}

	private File getPropertiesFile() {
		return new File(versionDir, "testevol.properties");
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return Integer.parseInt(properties.getProperty("index"));
	}

	public void setIndex(int index) {
		properties.setProperty("index", String.valueOf(index));
	}

	public String getType() {
		if (isMavenProject()) {
			return "Maven";
		}

		return null;
	}

	private boolean isMavenProject() {
		return new File(versionDir, "pom.xml").exists();
	}

	public void loadConfiguration() throws Exception {
		if (isMavenProject()) {
			loadMavenConfiguration();
		}
	}

	private void loadMavenConfiguration() throws Exception {

		MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
		mavenModel = m2pomReader.read(new FileReader(new File(versionDir,
				"pom.xml")));
	}

	public void saveProperties() {
		try {
			properties.store(new FileOutputStream(getPropertiesFile()),
					"TestEvol properties for branch " + versionDir.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public UpdateResult updateLocalFilesFromRepository() throws Exception {
		VersionControlSystem vcs = VersionControlSystem.getInstance(
				getRepoType(), versionDir);
		return vcs.update();

	}

	private String getRepoType() {
		File gitDir = new File(versionDir, ".git");
		if (gitDir.exists()) {
			return VersionControlSystem.GIT;
		}
		return null;
	}

	public File getSourceDir() {
		if (isMavenProject()) {
			String srcDir = mavenModel.getBuild().getSourceDirectory();
			if (srcDir == null) {
				srcDir = "src/main/java";
			}
			return new File(versionDir, srcDir);
		}
		return null;
	}

	public File getTestsSourceDir() {
		if (isMavenProject()) {
			String srcDir = mavenModel.getBuild().getSourceDirectory();
			if (srcDir == null) {
				srcDir = "src/test/java";
			}
			return new File(versionDir, srcDir);
		}
		return null;
	}

	
	public String getClassPath() {
		return Utils.getClassPathInDir(versionDir);
	}

	public File getBinTmpDir() {
		return new File(buildDir, "bintmp");
	}

	public File getBinTestTmpDir() {
		return new File(buildDir, "bintesttmp");
	}
	
	public File getTestslistFile() {
		return new File(buildDir, "data-testslist.txt");
	}
	
	public File getBinDir() {
		return new File(buildDir, "bin");
	}
	
	public File getBinTestDir() {
		return new File(buildDir, "bintest");
	}
	
	public File getCodeJar(){
		return new File(buildDir, "code.jar");
	}
	
	public File getTestsJar(){
		return new File(buildDir, "tests.jar");
	}
	
	public File getSrcResourcesDir(){
		if(isMavenProject()){
			return new File(buildDir, ".src-resources");
		}
		return new File(versionDir, "src/main/resources");
	}
	
	public File getTestResourcesDir(){
		if(isMavenProject()){
			return new File(buildDir, ".src-test-resources");
		}
		return new File(versionDir, "src/test/resources");
	}

	
	
	public String getJavaVersion() {
		if (isMavenProject()) {
			for (Plugin plugin : mavenModel.getBuild().getPlugins()) {
				String groupId = plugin.getGroupId();
				String artifactId = plugin.getArtifactId();
				if ("org.apache.maven.plugins".equals(groupId)
						&& "maven-compiler-plugin".equals(artifactId)) {
					Xpp3Dom dom = ((Xpp3Dom)plugin.getConfiguration());
					if("configuration".equals(dom.getName())){
						for(Xpp3Dom children:dom.getChildren()){
							if(children.getName().matches("target|source")){
								return children.getValue();
							}
						}
					}
				}
			}
			return "1.5";
		}

		File jvfile = new File(versionDir, "javaversion.txt");
		String javaVersion = "";
		if (jvfile.exists()) {
			javaVersion = Utils.getFileContentAsString(jvfile);
		}
		return javaVersion;
	}

	public boolean setUp() throws Exception{
		boolean result = true;
		
		if(buildDir.exists()){
			FileUtils.deleteDirectory(buildDir);
		}
		
		getBinTmpDir().mkdirs();
		getBinDir().mkdirs();
		getBinTestTmpDir().mkdirs();
		getBinTestDir().mkdirs();
		
		
		if(isMavenProject()){
			result = setUpMavenProject();
		}
		return result;
	}
	
	private boolean setUpMavenProject() throws IOException {
		File srcResources = getSrcResourcesDir();
		File testResources = getTestResourcesDir();
		
		MavenCli maven = new MavenCli();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		maven.doMain(new String[]{"clean","compile","test","dependency:copy-dependencies"}, versionDir.getAbsolutePath(), ps, ps);
		
		String outputDirectory = mavenModel.getBuild().getOutputDirectory();
		if(outputDirectory == null){
			outputDirectory = "target/classes";
		}
		
		FileUtils.copyDirectory(new File(versionDir,outputDirectory), srcResources); 
		deleteClassesFromResources(srcResources);
		
		String testOutputDirectoryName = mavenModel.getBuild().getTestOutputDirectory();
		if(testOutputDirectoryName == null){
			testOutputDirectoryName = "target/test-classes";
		}
		File testOutputDirectory = new File(versionDir,testOutputDirectoryName);
		if(testOutputDirectory.exists()){
			FileUtils.copyDirectory(testOutputDirectory, testResources); 
			deleteClassesFromResources(testResources);			
		}		
		
		File lib = new File(versionDir,"lib");
		if(lib.exists()){
			FileUtils.deleteDirectory(lib);
		}
		FileUtils.copyDirectory(new File(versionDir, "target"+File.separatorChar+"dependency"), lib);			
		return baos.toString().contains("BUILD SUCCESS");
		//maven.doMain(new String[]{"clean"}, versionDir.getAbsolutePath(), ps, ps);		
	}

	private void deleteClassesFromResources(File dir) throws IOException{
		
		
		for(Object fileObj : FileUtils.listFiles(dir, new String[]{"class"}, true)){
			if(fileObj instanceof File){
				((File) fileObj).delete();
			}
		}
		deleteEmptyDirectories(dir);
		
	}
	
	private boolean deleteEmptyDirectories(File dir){
		boolean empty = true;
		for(File file:dir.listFiles()){
			if(file.isDirectory()){
				empty = deleteEmptyDirectories(file) && empty;
			}
			else{
				empty = false;
			}
		}
		if(empty){
			dir.delete();
		}
		return empty;
	}

	public File getDirectory() {
		return versionDir;
	}

	public File getBuildDir() {
		return buildDir;
	}
}
