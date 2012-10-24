package org.testevol.domain;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.testevol.versioncontrol.UpdateResult;
import org.testevol.versioncontrol.VersionControlSystem;

public class Version {

	private File versionDir;
	private String name;
	private Properties properties;

	public Version(File versionDir) {
		super();
		this.versionDir = versionDir;
		name = versionDir.getName();
		properties = new Properties();
		try {
			if (getPropertiesFile().exists()) {
				properties.load(new FileInputStream(getPropertiesFile()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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

	// private void getMavenConfiguration() {
	//
	// MavenXpp3Reader m2pomReader = new MavenXpp3Reader();
	// try {
	// Model model = m2pomReader.read( new FileReader( new File(versionDir,
	// "pom.xml") ) );
	// String srcDir = model.getBuild().getSourceDirectory();
	// if(srcDir == null){
	// srcDir = "src/main/java";
	// }
	//
	// String testDir = model.getBuild().getTestSourceDirectory();
	// if(testDir == null){
	// testDir = "src/test/java";
	// }
	//
	//
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

	public void saveProperties() {
		try {
			properties.store(new FileOutputStream(getPropertiesFile()), "TestEvol properties for branch "
					+ versionDir.getName());
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

}
