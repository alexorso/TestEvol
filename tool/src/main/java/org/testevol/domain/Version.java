package org.testevol.domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.testevol.engine.driver.TestCoverageDriver;
import org.testevol.engine.util.TestevolMavenCli;
import org.testevol.engine.util.Utils;
import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

public class Version {

	public static final String LIB_DIR = "lib.dir";
	public static final String TESTS_SRC = "tests.src";
	public static final String TESTS_RESOURCES = "tests.resources";
	public static final String JAVA_RESOURCES = "java.resources";
	public static final String JAVA_SRC = "java.src";
	public static final String JAVA_VERSION = "java.version";

	private File versionDir;
	private File baseBuildDir;
	private String name;
	private Properties properties;

	// Used in case of a maven project
	private Model mavenModel;
	private Properties testEvolProperties;
	private VersionSettings versionSettings;

	public Version(File versionDir) throws Exception {
		super();
		this.versionDir = versionDir;
		name = versionDir.getName();
		properties = new Properties();
		if (getPropertiesFile().exists()) {
			properties.load(new FileInputStream(getPropertiesFile()));
		}
		loadConfiguration();
	}

	private void deleteClassesFromResources(File dir) throws IOException {

		for (Object fileObj : FileUtils.listFiles(dir,
				new String[] { "class" }, true)) {
			if (fileObj instanceof File) {
				((File) fileObj).delete();
			}
		}
		deleteEmptyDirectories(dir);

	}

	private boolean deleteEmptyDirectories(File dir) {
		boolean empty = true;
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				empty = deleteEmptyDirectories(file) && empty;
			} else {
				empty = false;
			}
		}
		if (empty) {
			dir.delete();
		}
		return empty;
	}

	public File getBinDir() {
		return new File(getBuildDir(), "bin");
	}

	public File getBinTestDir() {
		return new File(getBuildDir(), "bintest");
	}

	public File getBinTestTmpDir() {
		return new File(getBuildDir(), "bintesttmp");
	}

	public File getBinTmpDir() {
		return new File(getBuildDir(), "bintmp");
	}

	public File getTestEvolConfigFile() {
		return new File(versionDir, "testevol.config");
	}

	public VersionSettings getVersionSettings() {
		Properties properties = getTestEvolConfig();
		if (properties == null) {
			return null;
		}
		if (versionSettings == null) {
			versionSettings = new VersionSettings(
					properties.getProperty(JAVA_SRC),
					properties.getProperty(JAVA_RESOURCES),
					properties.getProperty(TESTS_SRC),
					properties.getProperty(TESTS_RESOURCES),
					properties.getProperty(LIB_DIR),
					properties.getProperty(JAVA_VERSION));

		}
		return versionSettings;

	}

	private Properties getTestEvolConfig() {
		File testEvolConfig = getTestEvolConfigFile();
		if (testEvolProperties == null) {
			testEvolProperties = new Properties();
			try {
				testEvolProperties.load(new FileInputStream(testEvolConfig));
			} catch (Exception e) {
				//file does not exist
				//e.printStackTrace();
			}
		}
		return testEvolProperties;
	}

	public File getBuildDir() {
		if (baseBuildDir == null) {
			return new File(versionDir, "build");
		}
		return new File(baseBuildDir, "build" + File.separator + getName());
	}

	private File getLibrariesDir() {
		String libDir = "lib";
		if (hasTestEvolConfig()) {
			String libDirAux = getProperty(LIB_DIR);
			if (libDirAux != null) {
				libDir = libDirAux;
			}
		}
		return new File(versionDir, libDir);
	}

	private String getProperty(String propertyName) {
		Properties properties = getTestEvolConfig();
		if (properties.containsKey(propertyName)) {
			String value = properties.getProperty(propertyName);
			if (value != null && !value.trim().isEmpty()) {
				return value;
			}
		}
		return null;
	}

	public String getClassPath() {
		String classpath = ".";
		File cpfile = new File(versionDir, "classpath.txt");
		File libDir = getLibrariesDir();

		if (!cpfile.exists()) {
			// No classpath.txt file in verDir
			// Using all jar files under "lib"
			for (File jar : Utils.getMatchingFiles(libDir, ".*jar$")) {
				classpath = classpath + File.pathSeparator
						+ jar.getAbsolutePath();
			}
		} else {
			classpath = Utils.makePathsAbsolute(
					Utils.getFileContentAsString(cpfile),
					versionDir.getAbsolutePath());
		}
		return classpath;
	}

	public File getCodeJar() {
		return new File(getBuildDir(), "code.jar");
	}

	public File getDirectory() {
		return versionDir;
	}

	public int getIndex() {
		return Integer.parseInt(properties.getProperty("index"));
	}

	public String getJavaVersion() {
		if (isMavenProject()) {
			for (Plugin plugin : mavenModel.getBuild().getPlugins()) {
				String groupId = plugin.getGroupId();
				String artifactId = plugin.getArtifactId();
				if ("org.apache.maven.plugins".equals(groupId)
						&& "maven-compiler-plugin".equals(artifactId)) {
					Xpp3Dom dom = ((Xpp3Dom) plugin.getConfiguration());
					if ("configuration".equals(dom.getName())) {
						for (Xpp3Dom children : dom.getChildren()) {
							if (children.getName().matches("target|source")) {
								return children.getValue();
							}
						}
					}
				}
			}
			return "1.5";
		}

		String javaVersion = "1.5";
		String javaVersionAux = getProperty(JAVA_VERSION);
		if (javaVersionAux != null) {
			javaVersion = javaVersionAux;
		}

		return javaVersion;
	}

	public String getName() {
		return name;
	}

	private File getPropertiesFile() {
		return new File(versionDir, "testevol.properties");
	}

	private String getRepoType() {
		File gitDir = new File(versionDir, ".git");
		if (gitDir.exists()) {
			return VersionControlSystem.GIT;
		}
		return null;
	}

	public File getSourceDir() {
		String srcDir = "src/main/java";
		if (hasTestEvolConfig()) {
			String srcDirAux = getProperty(JAVA_SRC);
			if (srcDirAux != null) {
				srcDir = srcDirAux;
			}
		}
		if (isMavenProject()) {
			String srcDirAux = mavenModel.getBuild().getSourceDirectory();
			if (srcDirAux != null) {
				srcDir = srcDirAux;
			}
		}
		return new File(versionDir, srcDir);
	}

	public File getSrcResourcesDir() {
		if (isMavenProject()) {
			return new File(getBuildDir(), ".src-resources");
		}

		String dir = "src/main/resources";
		if (hasTestEvolConfig()) {
			String dirAux = getProperty(JAVA_RESOURCES);
			if (dirAux != null) {
				dir = dirAux;
			}
		}
		return new File(versionDir, dir);
	}

	public File getTestResourcesDir() {
		if (isMavenProject()) {
			return new File(getBuildDir(), ".src-test-resources");
		}
		String dir = "src/test/resources";
		if (hasTestEvolConfig()) {
			String dirAux = getProperty(TESTS_RESOURCES);
			if (dirAux != null) {
				dir = dirAux;
			}
		}
		return new File(versionDir, dir);
	}

	public File getTestsJar() {
		return new File(getBuildDir(), "tests.jar");
	}

	public Set<String> getTestsList() {
		Set<String> tests = new HashSet<String>();

		File testsFile = getTestslistFile();

		BufferedReader reader = null;

		try {
			String test = null;

			reader = new BufferedReader(new FileReader(testsFile));
			while ((test = reader.readLine()) != null) {
				tests.add(test);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
		return tests;
	}

	public File getTestslistFile() {
		return new File(getBuildDir(), "data-testslist.txt");
	}

	public File getTestsSourceDir() {
		String srcDir = "src/test/java";
		if (hasTestEvolConfig()) {
			String srcDirAux = getProperty(TESTS_SRC);
			if (srcDirAux != null) {
				srcDir = srcDirAux;
			}
		}
		if (isMavenProject()) {
			String srcDirAux = mavenModel.getBuild().getTestSourceDirectory();
			if (srcDirAux != null) {
				srcDir = srcDirAux;
			}
		}
		return new File(versionDir, srcDir);
	}

	public String getConfigurationType() {
		if (!getSourceDir().exists() || !getLibrariesDir().exists()) {
			return "Invalid";
		}
		if (hasTestEvolConfig()) {
			return "TestEvol";
		}
		if (isMavenProject()) {
			return "Maven";
		}
		System.out.println(getLibrariesDir().getAbsolutePath());

		return null;
	}

	private boolean isMavenProject() {
		return new File(versionDir, "pom.xml").exists();
	}

	private boolean hasTestEvolConfig() {
		return getTestEvolConfigFile().exists();
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

	public void setBaseBuildDir(File baseBuildDir) {
		this.baseBuildDir = baseBuildDir;
	}

	public void setIndex(int index) {
		properties.setProperty("index", String.valueOf(index));
	}

	public boolean setUp(File configDir) throws Exception {
		boolean result = true;

		File buildDir = getBuildDir();
		if (buildDir.exists()) {
			FileUtils.deleteDirectory(buildDir);
		}
		buildDir.mkdirs();

		getBinTmpDir().mkdirs();
		getBinDir().mkdirs();
		getBinTestTmpDir().mkdirs();
		getBinTestDir().mkdirs();

		if (isMavenProject()) {
			result = setUpMavenProject();
		}

		File libDir = getLibrariesDir();
		if (libDir.exists()) {
			FileUtils.copyDirectory(new File(configDir,
					TestCoverageDriver.COVERAGE_JARS), libDir);
		}

		return result;
	}

	private boolean setUpMavenProject() throws IOException {
		File srcResources = getSrcResourcesDir();
		File testResources = getTestResourcesDir();

		
		String output;
		try {
			output = TestevolMavenCli.execMavenProcess(new String[] { "clean", "compile", "test",
					"dependency:copy-dependencies" }, versionDir);
		} catch (Exception e) {
			e.printStackTrace();
			return false;			
		}
		boolean result = output.contains("BUILD SUCCESS");
		if (!result) {
			return false;
		}

		String outputDirectory = mavenModel.getBuild().getOutputDirectory();
		if (outputDirectory == null) {
			outputDirectory = "target/classes";
		}

		FileUtils.copyDirectory(new File(versionDir, outputDirectory),
				srcResources);
		deleteClassesFromResources(srcResources);

		String testOutputDirectoryName = mavenModel.getBuild()
				.getTestOutputDirectory();
		if (testOutputDirectoryName == null) {
			testOutputDirectoryName = "target/test-classes";
		}

		File testOutputDirectory = new File(versionDir, testOutputDirectoryName);
		if (testOutputDirectory.exists()) {
			FileUtils.copyDirectory(testOutputDirectory, testResources);
			deleteClassesFromResources(testResources);
		}

		File lib = new File(versionDir, "lib");
		if (lib.exists()) {
			FileUtils.deleteDirectory(lib);
		}
		FileUtils.copyDirectory(new File(versionDir, "target"
				+ File.separatorChar + "dependency"), lib);
		return true;
		// maven.doMain(new String[]{"clean"}, versionDir.getAbsolutePath(), ps,
		// ps);
	}

	public UpdateResult updateLocalFilesFromRepository() throws Exception {
		VersionControlSystem vcs = VersionControlSystem.getInstance(
				getRepoType(), versionDir);
		return vcs.update();

	}

	@Override
	public String toString() {
		return getName();
	}
}
